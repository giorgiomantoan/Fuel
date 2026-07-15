package com.example.fuel

import android.app.Application
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

        // Inizializzazione osmdroid
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = filesDir
            osmdroidTileCache = cacheDir
        }

        // Inizializzazione database, preferences e repository
        database = FuelDatabase.getInstance(this)
        preferences = PreferencesManager(this)
        repository = FuelRepository(database, preferences)
    }
}
