package com.example.fuel.util

import android.content.Context
import android.location.Geocoder
import com.example.fuel.ui.viewmodel.FuelViewModel
import com.google.android.gms.location.LocationServices
import java.util.Locale

/**
 * Helper per ottenere la posizione dell'utente.
 */
object LocationHelper {

    /**
     * Recupera l'ultima posizione nota e la passa al ViewModel.
     */
    fun getLastLocation(context: Context, viewModel: FuelViewModel) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude

                    // Tenta di ottenere il nome della città dal Geocoder
                    val cityName = try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            listOfNotNull(
                                addr.locality,
                                addr.adminArea
                            ).joinToString(", ")
                        } else ""
                    } catch (e: Exception) {
                        ""
                    }

                    viewModel.setUserPosition(lat, lon, cityName)
                }
            }
        } catch (e: SecurityException) {
            // Permesso non concesso
        }
    }
}
