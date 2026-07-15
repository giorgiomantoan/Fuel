package com.example.fuel.service

import androidx.room.withTransaction
import com.example.fuel.data.local.FuelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Scarica i dataset CSV del MIMIT, li parserizza e li salva su Room.
 * Eseguito all'avvio dell'app.
 */
class FuelDownloader(private val database: FuelDatabase) {

    companion object {
        private const val URL_ANAGRAFICA =
            "https://www.mimit.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv"
        private const val URL_PREZZI =
            "https://www.mimit.gov.it/images/exportCSV/prezzo_alle_8.csv"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Risultato del download
     */
    data class DownloadResult(
        val anagraficaCount: Int = 0,
        val prezziCount: Int = 0,
        val error: String? = null
    )

    /**
     * Scarica entrambi i CSV, li parserizza e popola il database.
     * Esegue una transazione: cancella i vecchi dati e inserisce i nuovi.
     */
    suspend fun downloadAndStore(): DownloadResult {
        return withContext(Dispatchers.IO) {
            try {
                // Download in parallelo (sequenziale per semplicità)
                val anagraficaContent = downloadCsvBlocking(URL_ANAGRAFICA)
                val prezziContent = downloadCsvBlocking(URL_PREZZI)

                if (anagraficaContent == null) {
                    return@withContext DownloadResult(error = "Errore download anagrafica impianti")
                }
                if (prezziContent == null) {
                    return@withContext DownloadResult(error = "Errore download prezzi carburanti")
                }

                // Parsing
                val impianti = CsvParser.parseAnagrafica(anagraficaContent)
                val prezzi = CsvParser.parsePrezzi(prezziContent)

                if (impianti.isEmpty()) {
                    return@withContext DownloadResult(error = "Nessun impianto trovato nel file CSV")
                }
                if (prezzi.isEmpty()) {
                    return@withContext DownloadResult(error = "Nessun prezzo trovato nel file CSV")
                }

                // Scrittura su database in transazione
                database.withTransaction {
                    val anagraficaDao = database.anagraficaDao()
                    val prezzoDao = database.prezzoDao()

                    anagraficaDao.deleteAll()
                    prezzoDao.deleteAll()

                    anagraficaDao.insertAll(impianti)
                    prezzoDao.insertAll(prezzi)
                }

                DownloadResult(
                    anagraficaCount = impianti.size,
                    prezziCount = prezzi.size
                )
            } catch (e: Exception) {
                DownloadResult(error = "Errore: ${e.localizedMessage ?: "sconosciuto"}")
            }
        }
    }

    /**
     * Download bloccante (chiamato dentro withContext(IO)).
     */
    private fun downloadCsvBlocking(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "FuelApp/1.0 (Android)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return null
            }
            response.body?.string()
        } catch (e: Exception) {
            null
        }
    }
}
