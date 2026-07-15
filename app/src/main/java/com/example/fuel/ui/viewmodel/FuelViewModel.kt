package com.example.fuel.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuel.FuelApplication
import com.example.fuel.data.model.ImpiantoConPrezzo
import com.example.fuel.data.repository.FuelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FuelApplication
    private val repository = app.repository

    // === Stato download ===
    val downloadState = repository.downloadState

    // === Tipi di carburante disponibili ===
    val tipiCarburante: StateFlow<List<String>> = repository.tipiCarburante

    // === Stato ricerca ===
    private val _searchMode = MutableStateFlow(SearchMode.POSITION)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _selectedFuel = MutableStateFlow("")
    val selectedFuel: StateFlow<String> = _selectedFuel.asStateFlow()

    private val _radiusKm = MutableStateFlow(5.0)  // default 5 km
    val radiusKm: StateFlow<Double> = _radiusKm.asStateFlow()

    private val _addressQuery = MutableStateFlow("")
    val addressQuery: StateFlow<String> = _addressQuery.asStateFlow()

    private val _results = MutableStateFlow<List<ImpiantoConPrezzo>>(emptyList())
    val results: StateFlow<List<ImpiantoConPrezzo>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Posizione utente (default: Milano)
    private val _userLat = MutableStateFlow(45.4642)
    val userLat: StateFlow<Double> = _userLat.asStateFlow()

    private val _userLon = MutableStateFlow(9.1900)
    val userLon: StateFlow<Double> = _userLon.asStateFlow()

    private val _userLocationName = MutableStateFlow("")
    val userLocationName: StateFlow<String> = _userLocationName.asStateFlow()

    // Trigger per centrare la mappa sulla posizione utente (auto dopo concessione permessi)
    private val _centerOnUserTrigger = MutableStateFlow(0)
    val centerOnUserTrigger: StateFlow<Int> = _centerOnUserTrigger.asStateFlow()

    enum class SearchMode {
        POSITION,   // cerca vicino alla posizione corrente
        ADDRESS     // cerca per indirizzo/città
    }

    init {
        // Carica i dati all'avvio (solo se scaduti >24h)
        viewModelScope.launch {
            repository.refreshData(force = false)
        }
    }

    fun setSearchMode(mode: SearchMode) {
        _searchMode.value = mode
        _searchError.value = null
    }

    fun setSelectedFuel(fuel: String) {
        _selectedFuel.value = fuel
    }

    fun setRadius(km: Double) {
        _radiusKm.value = km.coerceIn(1.0, 25.0)
    }

    fun setAddressQuery(query: String) {
        _addressQuery.value = query
    }

    fun setUserPosition(lat: Double, lon: Double, name: String = "") {
        _userLat.value = lat
        _userLon.value = lon
        _userLocationName.value = name
        // Chiedi alla mappa di centrare sulla posizione (una tantum)
        _centerOnUserTrigger.value = _centerOnUserTrigger.value + 1
    }

    /**
     * Esegue la ricerca in base alla modalità corrente.
     */
    fun search() {
        val fuel = _selectedFuel.value
        if (fuel.isBlank()) {
            _searchError.value = "Seleziona un tipo di carburante"
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            try {
                val resultsList = when (_searchMode.value) {
                    SearchMode.POSITION -> {
                        repository.searchByFuelTypeNearby(
                            carburante = fuel,
                            lat = _userLat.value,
                            lon = _userLon.value,
                            raggioKm = _radiusKm.value,
                            soloSelf = true
                        )
                    }
                    SearchMode.ADDRESS -> {
                        val query = _addressQuery.value.trim()
                        if (query.isBlank()) {
                            _searchError.value = "Inserisci un indirizzo o comune"
                            _isSearching.value = false
                            return@launch
                        }
                        repository.searchByFuelTypeNearAddress(
                            carburante = fuel,
                            comune = query,
                            indirizzo = query,
                            raggioKm = _radiusKm.value,
                            soloSelf = true
                        )
                    }
                }

                _results.value = resultsList
                if (resultsList.isEmpty()) {
                    _searchError.value = "Nessun distributore trovato nei paraggi"
                }
            } catch (e: Exception) {
                _searchError.value = "Errore ricerca: ${e.localizedMessage}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearResults() {
        _results.value = emptyList()
        _searchError.value = null
    }

    /**
     * Forza il refresh dei dati (pulsante manuale).
     */
    fun refreshData() {
        viewModelScope.launch {
            repository.refreshData(force = true)
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
