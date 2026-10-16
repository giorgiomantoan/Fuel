package com.example.fuel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.fuel.ui.screens.MainScreen
import com.example.fuel.ui.theme.FuelTheme
import com.example.fuel.ui.viewmodel.FuelViewModel
import com.example.fuel.util.LocationHelper

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: FuelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[FuelViewModel::class.java]

        // NON richiedere permesso qui: lo fa la UI (PermissionRequestScreen)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            FuelTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Quando l'utente torna dall'aver concesso il permesso in Impostazioni,
        // riprova a ottenere la posizione
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationHelper.getLastLocation(this, viewModel)
        }
    }
}
