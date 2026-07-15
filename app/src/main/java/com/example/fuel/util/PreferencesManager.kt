package com.example.fuel.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fuel_preferences")

/**
 * Gestisce le preferenze dell'app tramite DataStore.
 * Usato per tracciare l'ultimo download dei dati.
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val LAST_DOWNLOAD_KEY = longPreferencesKey("last_download_timestamp")
    }

    /** Timestamp dell'ultimo download (epoch millis). 0 = mai scaricato. */
    val lastDownloadTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_DOWNLOAD_KEY] ?: 0L
    }

    /** Salva il timestamp corrente come ultimo download. */
    suspend fun saveLastDownloadNow() {
        val now = System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            prefs[LAST_DOWNLOAD_KEY] = now
        }
    }

    /**
     * Restituisce true se i dati vanno scaricati.
     * Condizioni:
     * - Mai scaricato (primo avvio)
     * - Sono passate più di 24 ore
     * - Sono passate le 10:30 e l'ultimo download non è di oggi
     *   (i dati MIMIT vengono pubblicati dopo le 8:00)
     */
    suspend fun isDownloadExpired(): Boolean {
        val last = lastDownloadTimestamp.first()
        if (last == 0L) return true  // mai scaricato

        val now = System.currentTimeMillis()
        val elapsed = now - last

        // 1. Sono passate più di 24 ore?
        if (elapsed > 24 * 60 * 60 * 1000L) return true

        // 2. Sono passate le 10:30 e l'ultimo download è di un giorno precedente?
        val calNow = java.util.Calendar.getInstance()
        val calLast = java.util.Calendar.getInstance().apply { timeInMillis = last }

        val isAfter1030 = calNow.get(java.util.Calendar.HOUR_OF_DAY) >= 10 &&
                          calNow.get(java.util.Calendar.MINUTE) >= 30
        val isDifferentDay = calNow.get(java.util.Calendar.DAY_OF_YEAR) != calLast.get(java.util.Calendar.DAY_OF_YEAR) ||
                             calNow.get(java.util.Calendar.YEAR) != calLast.get(java.util.Calendar.YEAR)

        return isAfter1030 && isDifferentDay
    }
}
