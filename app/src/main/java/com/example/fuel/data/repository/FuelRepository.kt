package com.example.fuel.data.repository

import com.example.fuel.data.local.FuelDatabase
import com.example.fuel.data.model.ImpiantoConPrezzo
import com.example.fuel.service.FuelDownloader
import com.example.fuel.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Repository principale. Gestisce download dati e query di ricerca.
 * Le query usano JOIN diretto per evitare N+1.
 *
 * Il download automatico dei CSV avviene solo se sono passate 24h
 * dall'ultimo aggiornamento (i dati MIMIT vengono pubblicati dopo le 8:00
 * una volta al giorno). Il pulsante manuale forza sempre il download.
 */
class FuelRepository(
    private val database: FuelDatabase,
    private val preferences: PreferencesManager
) {

    private val downloader = FuelDownloader(database)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _tipiCarburante = MutableStateFlow<List<String>>(emptyList())
    val tipiCarburante: StateFlow<List<String>> = _tipiCarburante.asStateFlow()

    sealed class DownloadState {
        data object Idle : DownloadState()
        data object Downloading : DownloadState()
        data class Success(val anagrafica: Int, val prezzi: Int) : DownloadState()
        data class Error(val message: String) : DownloadState()
    }

    /**
     * Scarica i dati se necessario. Se [force] è true, scarica sempre.
     * Se [force] è false (automatico all'avvio), scarica solo se sono
     * passate più di 24 ore dall'ultimo download.
     */
    suspend fun refreshData(force: Boolean = false) {
        if (!force && !preferences.isDownloadExpired()) {
            // Dati recenti: carica solo i tipi carburante dal DB esistente
            _tipiCarburante.value = database.prezzoDao().getTipiCarburante()
            return
        }

        _downloadState.value = DownloadState.Downloading

        val result = downloader.downloadAndStore()

        if (result.error != null) {
            _downloadState.value = DownloadState.Error(result.error)
        } else {
            // Salva timestamp download riuscito
            preferences.saveLastDownloadNow()

            _downloadState.value = DownloadState.Success(
                anagrafica = result.anagraficaCount,
                prezzi = result.prezziCount
            )
            _tipiCarburante.value = database.prezzoDao().getTipiCarburante()
        }
    }

    /**
     * Carica i tipi di carburante dal database esistente.
     */
    suspend fun loadTipiCarburante() {
        _tipiCarburante.value = database.prezzoDao().getTipiCarburante()
    }

    /**
     * Determina i parametri di ricerca LIKE per il tipo di carburante.
     */
    private fun getFuelSearchParams(tipo: String): Triple<String, String, String?> {
        return when (tipo) {
            "Benzina" -> Triple("Benzina%", "", null)
            "Diesel" -> Triple("%Diesel%", "Gasolio", "%Blue%")
            "GPL" -> Triple("GPL", "", null)
            "Metano" -> Triple("Metano", "", null)
            else -> Triple("%$tipo%", "", null)
        }
    }

    /**
     * Cerca distributori per tipo carburante, filtrati per raggio dalla posizione.
     */
    suspend fun searchByFuelTypeNearby(
        carburante: String,
        lat: Double,
        lon: Double,
        raggioKm: Double,
        soloSelf: Boolean = true
    ): List<ImpiantoConPrezzo> {
        val (pattern, exact, pattern2) = getFuelSearchParams(carburante)

        val results = if (soloSelf) {
            database.prezzoDao().searchByFuelTypeSelf(pattern, exact, pattern2)
        } else {
            database.prezzoDao().searchByFuelTypeAll(pattern, exact, pattern2)
        }

        return results.map { result ->
            result.copy().also {
                it.distanzaKm = calcolaDistanza(lat, lon, result.latitudine, result.longitudine)
            }
        }.filter { it.distanzaKm!! <= raggioKm }
    }

    /**
     * Cerca distributori per tipo carburante vicino a un indirizzo.
     */
    suspend fun searchByFuelTypeNearAddress(
        carburante: String,
        comune: String,
        indirizzo: String = "",
        raggioKm: Double,
        soloSelf: Boolean = true
    ): List<ImpiantoConPrezzo> {
        val impianti = database.anagraficaDao().searchByAddress(comune, indirizzo)
        if (impianti.isEmpty()) return emptyList()

        val avgLat = impianti.map { it.latitudine }.average()
        val avgLon = impianti.map { it.longitudine }.average()

        return searchByFuelTypeNearby(carburante, avgLat, avgLon, raggioKm, soloSelf)
    }

    companion object {
        fun calcolaDistanza(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c
        }
    }
}
