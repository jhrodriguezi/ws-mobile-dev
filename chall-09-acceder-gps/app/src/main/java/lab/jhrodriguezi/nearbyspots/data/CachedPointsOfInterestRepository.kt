package lab.jhrodriguezi.nearbyspots.data

import lab.jhrodriguezi.nearbyspots.ui.PointOfInterest

class CachedPointsOfInterestRepository constructor(
    private val poiRepository: PointsOfInterestRepository
) {
    private val cache = mutableMapOf<String, CachedData>()

    suspend fun getPointsOfInterest(
        latitude: Double,
        longitude: Double,
        radiusKm: Float
    ): List<PointOfInterest> {
        val cacheKey =  "${"%.1f".format(latitude)}_${"%.1f".format(longitude)}_${radiusKm}"
        val cachedData = cache[cacheKey]

        // Check if cache is valid (less than 5 minutes old)
        if (cachedData != null &&
            System.currentTimeMillis() - cachedData.timestamp < 5 * 60 * 1000) {
            return cachedData.data
        }

        // Fetch new data
        return poiRepository.getPointsOfInterest(latitude, longitude, radiusKm)
            .also { pois ->
                cache[cacheKey] = CachedData(
                    data = pois,
                    timestamp = System.currentTimeMillis()
                )
            }
    }

    private data class CachedData(
        val data: List<PointOfInterest>,
        val timestamp: Long
    )
}