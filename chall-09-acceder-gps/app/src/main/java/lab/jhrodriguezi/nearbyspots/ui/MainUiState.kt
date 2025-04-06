package lab.jhrodriguezi.nearbyspots.ui

data class UiState(
    val currentLocation: Location? = null,
    val pointsOfInterest: List<PointOfInterest> = emptyList(),
    val searchRadius: Float = 5f
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class PointOfInterest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: PoiType
)

enum class PoiType {
    HOSPITAL,
    TOURIST_ATTRACTION,
    RESTAURANT,
    HOTEL
}