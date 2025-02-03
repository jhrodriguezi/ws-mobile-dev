package lab.jhrodriguezi.nearbyspots

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import lab.jhrodriguezi.nearbyspots.data.CachedPointsOfInterestRepository
import lab.jhrodriguezi.nearbyspots.data.LocationRepository
import lab.jhrodriguezi.nearbyspots.data.PointsOfInterestRepository
import lab.jhrodriguezi.nearbyspots.data.PreferencesRepository
import lab.jhrodriguezi.nearbyspots.ui.Location
import lab.jhrodriguezi.nearbyspots.ui.MainViewModel
import lab.jhrodriguezi.nearbyspots.ui.MainViewModelFactory
import lab.jhrodriguezi.nearbyspots.ui.PoiType
import lab.jhrodriguezi.nearbyspots.ui.PointOfInterest
import lab.jhrodriguezi.nearbyspots.ui.theme.NearbySpotsTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            // Initialize repositories
            val locationRepository = remember {
                LocationRepository(
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                )
            }
            val poiRepository = remember { CachedPointsOfInterestRepository(
                PointsOfInterestRepository()
            ) }
            val preferencesRepository = remember {
                PreferencesRepository(dataStore = context.dataStore)
            }

            val viewModelFactory = remember {
                MainViewModelFactory(
                    locationRepository = locationRepository,
                    poiRepository = poiRepository,
                    preferencesRepository = preferencesRepository
                )
            }

            NearbySpotsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionWrapper {
                        NearbySpotsApp(viewModelFactory.create(MainViewModel::class.java))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(
    content: @Composable () -> Unit
) {
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    when {
        // If all permissions are granted, show the content
        locationPermissions.allPermissionsGranted -> {
            content()
        }
        // If user denied any permission but can still request, show rationale
        locationPermissions.shouldShowRationale || !locationPermissions.allPermissionsGranted -> {
            PermissionRationaleDialog(
                onConfirm = { locationPermissions.launchMultiplePermissionRequest() }
            )
        }
        // First time asking for permission
        else -> {
            LaunchedEffect(Unit) {
                locationPermissions.launchMultiplePermissionRequest()
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Dialog cannot be dismissed */ },
        title = { Text("Location Permission Required") },
        text = {
            Text(
                "This app needs access to your location to show nearby points of interest. " +
                        "Please grant location permission to continue using the app."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Grant Permission")
            }
        }
    )
}

@Composable
fun MapView(
    currentLocation: Location?,
    pointsOfInterest: List<PointOfInterest>,
    searchRadius: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showDialog by remember { mutableStateOf(false) }
    var poiSelected by remember { mutableStateOf<PointOfInterest?>(null) }

    // Initialize Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Título del lugar
                    Text(
                        text = poiSelected?.name ?: "Lugar desconocido",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    // Detalles adicionales del lugar
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tipo: ${poiSelected?.type ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Latitud: ${poiSelected?.latitude ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Longitud: ${poiSelected?.longitude ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Botón de cerrar
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    // Create and remember MapView
    val mapView = remember { createMapView(context) }

    // Remember the last location
    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> { /* no-op */
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Location overlay
    val myLocationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        // Update map when location changes
        currentLocation?.let { location ->
            val newLocation = GeoPoint(location.latitude, location.longitude)

            // Check if we should update the map center
            val shouldUpdateCenter = lastLocation?.let { last ->
                calculateDistance(
                    last.latitude, last.longitude,
                    newLocation.latitude, newLocation.longitude
                ) > 100 // meters
            } ?: true

            if (shouldUpdateCenter) {
                view.controller.apply {
                    animateTo(newLocation)
                    setZoom(15.0)
                }
                lastLocation = newLocation
            }

            // Update overlays
            view.overlays.clear()
            view.overlays.add(myLocationOverlay)

            // Add POI markers
            pointsOfInterest.forEach { poi ->
                addPoiMarker(view, poi, context) {
                    poiSelected = it
                    showDialog = true
                }
            }

            view.invalidate()
        }
    }
}

private fun createMapView(context: Context): MapView {
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        isTilesScaledToDpi = true

        controller.apply {
            setZoom(15.0)
            setCenter(GeoPoint(0.0, 0.0)) // Default center, will be updated
        }
    }
}

private fun addPoiMarker(
    mapView: MapView,
    poi: PointOfInterest,
    context: Context,
    onMarkerClick: (PointOfInterest) -> Unit = {}
) {
    Marker(mapView).apply {
        position = GeoPoint(poi.latitude, poi.longitude)
        title = poi.name
        snippet = poi.type.toString()

        // Set different icons based on POI type
        icon = when (poi.type) {
            PoiType.HOSPITAL -> context.getDrawable(R.drawable.ic_hospital)
            PoiType.TOURIST_ATTRACTION -> context.getDrawable(R.drawable.ic_tourist)
            PoiType.RESTAURANT -> context.getDrawable(R.drawable.ic_restaurant)
            PoiType.HOTEL -> context.getDrawable(R.drawable.ic_hotel)
        }

        icon.setTint(context.getColor(R.color.purple_500))

        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        // Add marker interaction
        setOnMarkerClickListener { _, _ ->
            onMarkerClick(poi)
            true
        }

        mapView.overlays.add(this)
    }
}

haversineDistance
fun calculateDistance(
    latitude1: Double, longitude1: Double,
    latitude2: Double, longitude2: Double
): Double {
    val earthRadius = 6371e3 // Radio de la Tierra en metros

    val latRad1 = Math.toRadians(latitude1)
    val latRad2 = Math.toRadians(latitude2)
    val deltaLat = Math.toRadians(latitude2 - latitude1)
    val deltaLon = Math.toRadians(longitude2 - longitude1)

    val a = sin(deltaLat / 2).pow(2) +
            cos(latRad1) * cos(latRad2) *
            sin(deltaLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c // Distancia en metros
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

@Composable
fun NearbySpotsApp(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MapView(
            currentLocation = uiState.currentLocation,
            pointsOfInterest = uiState.pointsOfInterest,
            searchRadius = uiState.searchRadius,
            modifier = Modifier.weight(1f)
        )

        SettingsPanel(
            searchRadius = uiState.searchRadius,
            onRadiusChange = viewModel::updateSearchRadius
        )
    }
}

// SettingsPanel.kt
@Composable
fun SettingsPanel(
    searchRadius: Float,
    onRadiusChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .zIndex(1f)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Search Radius: ${searchRadius.roundToInt()} km",
            style = MaterialTheme.typography.bodyLarge
        )

        Slider(
            value = searchRadius,
            onValueChange = onRadiusChange,
            valueRange = 1f..50f,
            steps = 49
        )
    }
}