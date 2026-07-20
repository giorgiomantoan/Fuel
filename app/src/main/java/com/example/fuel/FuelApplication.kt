package com.example.fuel

import android.app.Application
import android.content.pm.PackageManager
import com.example.fuel.data.local.FuelDatabase
import com.example.fuel.data.repository.FuelRepository
import com.example.fuel.util.PreferencesManager
import org.osmdroid.config.Configuration

class FuelApplication : Application() {

    lateinit var database: FuelDatabase
        private set

    lateinit var repository: FuelRepository
        private set

    lateinit var preferences: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Legge la versione dall'AndroidManifest (senza dipendere da BuildConfig)
        val appVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }

        // Inizializzazione osmdroid
        Configuration.getInstance().apply {
            // User-Agent descrittivo richiesto da OSM Tile Usage Policy:
            // deve identificare l'app e fornire un contatto
            userAgentValue = "Fuel/$appVersion (Android ${
                android.os.Build.VERSION.RELEASE
            }; contatto: transalp98@gmail.com)"
            osmdroidBasePath = filesDir
            osmdroidTileCache = cacheDir
        }

        // Inizializzazione database, preferences e repository
        database = FuelDatabase.getInstance(this)
        preferences = PreferencesManager(this)
        repository = FuelRepository(database, preferences)
    }
}
