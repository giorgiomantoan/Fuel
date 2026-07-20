package com.example.fuel.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fuel.data.model.ImpiantoConPrezzo
import com.example.fuel.data.repository.FuelRepository
import com.example.fuel.ui.viewmodel.FuelViewModel
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FuelViewModel) {
    val context = LocalContext.current
    val downloadState by viewModel.downloadState.collectAsState()
    val tipiCarburante by viewModel.tipiCarburante.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val selectedFuel by viewModel.selectedFuel.collectAsState()
    val radiusKm by viewModel.radiusKm.collectAsState()
    val addressQuery by viewModel.addressQuery.collectAsState()
    val results by viewModel.results.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val userLat by viewModel.userLat.collectAsState()
    val userLon by viewModel.userLon.collectAsState()
    val userLocationName by viewModel.userLocationName.collectAsState()
    val hasLocationPerm = viewModel.hasLocationPermission()

    var showFuelDropdown by remember { mutableStateOf(false) }
    var showResultsPanel by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    var centerMapTrigger by remember { mutableStateOf(0) }
    val autoCenterTrigger by viewModel.centerOnUserTrigger.collectAsState()
    var selectedStationId by remember { mutableStateOf(-1) }  // -1 = nessuno selezionato
    val snackbarHostState = remember { SnackbarHostState() }

    // Quando il ViewModel richiede di centrare sulla posizione (permesso concesso)
    LaunchedEffect(autoCenterTrigger) {
        if (autoCenterTrigger > 0) centerMapTrigger++
    }

    LaunchedEffect(searchError) {
        searchError?.let { snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short) }
    }

    // === Se permesso non concesso: schermata di richiesta (prima di tutto) ===
    if (!hasLocationPerm) {
        PermissionRequestScreen()
        return
    }

    // === Dialog crediti ===
    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            title = { Text("Fuel v1.2.0", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Sviluppato da ravem (github.com/ravem)\n" +
                        "Licenza MIT – Copyright © 2026 ravem\n\n" +
                        "This software is provided \"as is\", without warranty of any kind.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Componenti e licenze:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    val components = listOf(
                        "Jetpack Compose BOM 2024.12.01" to "Apache 2.0",
                        "Compose UI / Material3 / Material Icons" to "Apache 2.0",
                        "Activity Compose 1.9.3" to "Apache 2.0",
                        "Navigation Compose 2.8.5" to "Apache 2.0",
                        "Lifecycle + ViewModel Compose 2.8.7" to "Apache 2.0",
                        "Room Database 2.6.1" to "Apache 2.0",
                        "OkHttp 4.12.0" to "Apache 2.0",
                        "OSMDroid 6.1.18" to "Apache 2.0",
                        "Google Play Services Location 21.3.0" to "Apache 2.0",
                        "Kotlin Coroutines 1.9.0" to "Apache 2.0",
                        "DataStore Preferences 1.1.1" to "Apache 2.0",
                        "Core KTX 1.15.0" to "Apache 2.0",
                        "Google Play Review 2.0.2" to "Apache 2.0"
                    )

                    components.forEach { (name, license) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text(license, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Dati e servizi di terze parti:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• OpenStreetMap dati cartografici — © OpenStreetMap contributors (ODbL)\n" +
                        "• Thunderforest tile server — mappe © Thunderforest, dati © OSM contributors\n" +
                        "• Dati prezzi carburanti — Ministero delle Imprese e del Made in Italy (MIMIT) — " +
                        "Dati pubblici italiani (CC BY 4.0, salvo diversa indicazione)",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Per i testi completi delle licenze visita:\n" +
                        "https://www.apache.org/licenses/LICENSE-2.0\n" +
                        "https://opendatacommons.org/licenses/odbl/",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showCreditsDialog = false }) { Text("Chiudi") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Fuel", style = MaterialTheme.typography.titleLarge)
                            if (userLocationName.isNotBlank())
                                Text(userLocationName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    when (val state = downloadState) {
                        is FuelRepository.DownloadState.Downloading -> {
                            Text("Aggiornamento…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                        else -> {
                            IconButton(onClick = { viewModel.refreshData() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Aggiorna dati")
                            }
                        }
                    }
                    IconButton(onClick = { showCreditsDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info app")
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = results.isNotEmpty() || showResultsPanel, enter = fadeIn(), exit = fadeOut()) {
                ResultsPanel(
                    results = results,
                    onToggle = { showResultsPanel = !showResultsPanel },
                    isExpanded = showResultsPanel,
                    onStationClick = { station ->
                        selectedStationId = station.idImpianto
                    },
                    onNavigate = { impianto ->
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("google.navigation:q=${impianto.latitudine},${impianto.longitudine}")).apply {
                            setPackage("com.google.android.apps.maps")
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            SearchPanel(
                searchMode = searchMode, selectedFuel = selectedFuel, tipiCarburante = tipiCarburante,
                radiusKm = radiusKm, addressQuery = addressQuery, showFuelDropdown = showFuelDropdown,
                isSearching = isSearching,
                onSearchModeChange = { viewModel.setSearchMode(it) },
                onFuelSelect = { viewModel.setSelectedFuel(it); showFuelDropdown = false },
                onFuelDropdownToggle = { showFuelDropdown = !showFuelDropdown },
                onRadiusChange = { viewModel.setRadius(it) },
                onAddressQueryChange = { viewModel.setAddressQuery(it) },
                onSearch = { viewModel.search(); showResultsPanel = true },
                onVicinoAMe = { viewModel.setSearchMode(FuelViewModel.SearchMode.POSITION); viewModel.search(); showResultsPanel = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Mappa con clipping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { clip = true }
            ) {
                OsmMapView(
                    userLat = userLat, userLon = userLon, results = results,
                    centerTrigger = centerMapTrigger,
                    selectedStationId = selectedStationId,
                    onStationSelected = { id -> selectedStationId = id },
                    modifier = Modifier.fillMaxSize()
                )
                if (userLat != 0.0 || userLon != 0.0) {
                    SmallFloatingActionButton(
                        onClick = { centerMapTrigger++ },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Centra posizione")
                    }
                }
            }
        }
    }
}

// ==================== PERMISSION REQUEST SCREEN ====================

@Composable
private fun PermissionRequestScreen() {
    val ctx = LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (!fine && !coarse) {
            // Permesso negato: apri impostazioni
            ctx.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Permesso posizione necessario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Fuel ha bisogno del permesso di posizione per mostrarti la tua " +
                "posizione sulla mappa e cercare distributori nelle vicinanze.\n\n" +
                "Senza questo permesso l'app non può funzionare.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    launcher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Concedi permesso", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ==================== SEARCH PANEL ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchPanel(
    searchMode: FuelViewModel.SearchMode, selectedFuel: String, tipiCarburante: List<String>,
    radiusKm: Double, addressQuery: String, showFuelDropdown: Boolean, isSearching: Boolean,
    onSearchModeChange: (FuelViewModel.SearchMode) -> Unit, onFuelSelect: (String) -> Unit,
    onFuelDropdownToggle: () -> Unit, onRadiusChange: (Double) -> Unit,
    onAddressQueryChange: (String) -> Unit, onSearch: () -> Unit, onVicinoAMe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            ExposedDropdownMenuBox(expanded = showFuelDropdown, onExpandedChange = { onFuelDropdownToggle() }) {
                OutlinedTextField(
                    value = selectedFuel.ifBlank { "Seleziona carburante" },
                    onValueChange = {}, readOnly = true, label = { Text("Tipo carburante") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFuelDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(expanded = showFuelDropdown, onDismissRequest = { onFuelDropdownToggle() }) {
                    tipiCarburante.forEach { tipo -> DropdownMenuItem(text = { Text(tipo) }, onClick = { onFuelSelect(tipo) }) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = searchMode == FuelViewModel.SearchMode.POSITION,
                    onClick = { if (selectedFuel.isNotBlank()) onVicinoAMe() else onSearchModeChange(FuelViewModel.SearchMode.POSITION) },
                    label = { Text("Vicino a me") }, leadingIcon = { Icon(Icons.Default.MyLocation, null, Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f))
                FilterChip(selected = searchMode == FuelViewModel.SearchMode.ADDRESS,
                    onClick = { onSearchModeChange(FuelViewModel.SearchMode.ADDRESS) },
                    label = { Text("Cerca indirizzo") }, leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("Raggio: ${radiusKm.toInt()} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(value = radiusKm.toFloat(), onValueChange = { onRadiusChange(it.toDouble()) }, valueRange = 1f..25f, steps = 23, modifier = Modifier.fillMaxWidth())
            AnimatedVisibility(visible = searchMode == FuelViewModel.SearchMode.ADDRESS) {
                OutlinedTextField(value = addressQuery, onValueChange = { onAddressQueryChange(it) },
                    label = { Text("Via, città…") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true, leadingIcon = { Icon(Icons.Default.Place, null) }, textStyle = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onSearch, modifier = Modifier.fillMaxWidth(), enabled = !isSearching && selectedFuel.isNotBlank()) {
                if (isSearching) { CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (isSearching) "Ricerca in corso…" else "Cerca")
            }
        }
    }
}

// ==================== MAPPA ====================

private fun createCircleDrawable(ctx: android.content.Context, color: Int, size: Int = 48): android.graphics.drawable.Drawable {
    val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888); val c = Canvas(b)
    val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f }
    val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE; style = Paint.Style.FILL }
    c.drawCircle(size / 2f, size / 2f, size / 2f - 2f, outer)
    c.drawCircle(size / 2f, size / 2f, size / 2f - 4f, border)
    c.drawCircle(size / 2f, size / 2f, size / 5f, inner)
    return BitmapDrawable(ctx.resources, b)
}

@Composable
private fun OsmMapView(
    userLat: Double, userLon: Double, results: List<ImpiantoConPrezzo>,
    centerTrigger: Int, selectedStationId: Int,
    onStationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val userDrw = remember { createCircleDrawable(ctx, android.graphics.Color.parseColor("#1976D2")) }       // blu
    val stationDrw = remember { createCircleDrawable(ctx, android.graphics.Color.parseColor("#43A047")) }    // verde
    val selectedDrw = remember { createCircleDrawable(ctx, android.graphics.Color.parseColor("#E53935")) }  // rosso
    val mapRef = remember { mutableStateOf<MapView?>(null) }

    // LaunchedEffect separato: anima quando cambia selectedStationId o centerTrigger
    LaunchedEffect(selectedStationId, centerTrigger) {
        val map = mapRef.value ?: return@LaunchedEffect
        if (selectedStationId >= 0) {
            val st = results.find { it.idImpianto == selectedStationId }
            if (st != null) map.controller.animateTo(GeoPoint(st.latitudine, st.longitudine), 14.0, 400L)
        } else if (centerTrigger > 0) {
            map.controller.animateTo(GeoPoint(userLat, userLon), 11.0, 500L)
            onStationSelected(-1)
        }
    }

    // Tile source Thunderforest (OpenStreetMap-based, richiede API key gratuita)
    val thunderforestTileSource = remember {
        object : OnlineTileSourceBase(
            "Thunderforest-Landscape", // name (usato per cache)
            1,  // zoom min
            18, // zoom max
            256, // tile size in pixels
            ".png", // image filename ending
            arrayOf("https://tile.thunderforest.com/landscape/") // base URL(s)
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val zoom = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                // URL formato: https://tile.thunderforest.com/landscape/{z}/{x}/{y}.png?apikey=KEY
                return "${getBaseUrl()}$zoom/$x/$y$mImageFilenameEnding?apikey=YOUR_THUNDERFOREST_API_KEY"
            }
        }
    }

    AndroidView(modifier = modifier, factory = { c ->
        MapView(c).apply {
            setTileSource(thunderforestTileSource)
            setMultiTouchControls(true)
            controller.setZoom(12.0); controller.setCenter(GeoPoint(userLat, userLon))
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
            clipChildren = true; clipToPadding = true
            mapRef.value = this
        }
    }, update = { map ->
        map.overlays.clear()
        map.overlays.add(Marker(map).apply {
            position = GeoPoint(userLat, userLon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "La tua posizione"; icon = userDrw
        })
        results.forEach { imp ->
            val isSelected = imp.idImpianto == selectedStationId
            map.overlays.add(Marker(map).apply {
                position = GeoPoint(imp.latitudine, imp.longitudine)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "${imp.nomeImpianto} - ${imp.prezzoFormattato}"
                snippet = "${imp.indirizzo}, ${imp.comune}"
                icon = if (isSelected) selectedDrw else stationDrw
                setOnMarkerClickListener { m, _ ->
                    map.controller.animateTo(m.position); onStationSelected(imp.idImpianto); true
                }
            })
        }
        if (results.isNotEmpty() && selectedStationId < 0) {
            try {
                val pts = results.map { GeoPoint(it.latitudine, it.longitudine) } + GeoPoint(userLat, userLon)
                if (pts.size >= 2) map.zoomToBoundingBox(BoundingBox.fromGeoPoints(pts).increaseByScale(1.2f), true)
            } catch (_: Exception) { /* fallback */}
        }
        map.invalidate()
    })
}

// ==================== RISULTATI ====================

@Composable
private fun ResultsPanel(
    results: List<ImpiantoConPrezzo>,
    onToggle: () -> Unit, isExpanded: Boolean,
    onStationClick: (ImpiantoConPrezzo) -> Unit,
    onNavigate: (ImpiantoConPrezzo) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.heightIn(max = 300.dp), shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${results.size} distributori trovati", style = MaterialTheme.typography.titleSmall)
                Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, "Toggle")
            }
            if (isExpanded) LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                items(results) { item ->
                    ImpiantoCard(item,
                        onNameClick = { onStationClick(item) },
                        onNavigate = { onNavigate(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpiantoCard(
    impianto: ImpiantoConPrezzo,
    onNameClick: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                // Nome: cliccabile -> centra mappa
                Text(impianto.nomeImpianto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onNameClick() })
                Spacer(Modifier.height(2.dp))
                Text("${impianto.bandiera} · ${impianto.gestore}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("${impianto.indirizzo}, ${impianto.comune} (${impianto.provincia})",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (impianto.distanzaKm != null)
                    Text("${String.format("%.1f", impianto.distanzaKm)} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format("%.3f", impianto.prezzo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("€/L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(impianto.modalita, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                // Solo questo bottone apre Google Maps
                SmallFloatingActionButton(onClick = onNavigate, modifier = Modifier.padding(top = 4.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                    Icon(Icons.Default.Navigation, "Naviga", Modifier.size(18.dp))
                }
            }
        }
    }
}
