package lab.jhrodriguezi.nearbyspots.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.jhrodriguezi.nearbyspots.data.CachedPointsOfInterestRepository
import lab.jhrodriguezi.nearbyspots.data.LocationRepository
import lab.jhrodriguezi.nearbyspots.data.PointsOfInterestRepository
import lab.jhrodriguezi.nearbyspots.data.PreferencesRepository

class MainViewModel(
    private val locationRepository: LocationRepository,
    private val poiRepository: CachedPointsOfInterestRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load saved search radius from preferences
            preferencesRepository.getSearchRadius()
                .collect { radius ->
                    _uiState.update { it.copy(searchRadius = radius) }
                }
        }

        // Start location updates
        viewModelScope.launch {
            locationRepository.locationFlow
                .collect { location ->
                    Log.i("MainViewModel", "Location updated: $location")
                    _uiState.update { it.copy(currentLocation = location) }
                    updatePointsOfInterest(location)
                }
        }
    }

    private suspend fun updatePointsOfInterest(location: Location) {
        val poi = poiRepository.getPointsOfInterest(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusKm = _uiState.value.searchRadius
        )
        _uiState.update { it.copy(pointsOfInterest = poi) }
    }

    fun updateSearchRadius(radiusKm: Float) {
        viewModelScope.launch {
            preferencesRepository.saveSearchRadius(radiusKm)
        }
    }
}

class MainViewModelFactory(
    private val locationRepository: LocationRepository,
    private val poiRepository: CachedPointsOfInterestRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(locationRepository, poiRepository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}