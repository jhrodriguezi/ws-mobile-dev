package lab.jhrodriguezi.nearbyspots.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.jhrodriguezi.nearbyspots.ui.PointOfInterest
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class PointsOfInterestRepository constructor() {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val overpassApi = Retrofit.Builder()
        .baseUrl("https://overpass-api.de/api/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OverpassApi::class.java)

    suspend fun getPointsOfInterest(
        latitude: Double,
        longitude: Double,
        radiusKm: Float
    ): List<PointOfInterest> = withContext(Dispatchers.IO) {
        try {
            val radiusMeters = (radiusKm * 1000).toInt()
            val query = buildOverpassQuery(latitude, longitude, radiusMeters)
            val response = overpassApi.getPointsOfInterest(query)
            return@withContext response.elements.mapNotNull { it.toPointOfInterest() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun buildOverpassQuery(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): String = """
        [out:json][timeout:25];
        (
          node["amenity"="hospital"](around:$radiusMeters,$latitude,$longitude);
          node["tourism"="attraction"](around:$radiusMeters,$latitude,$longitude);
          node["tourism"="museum"](around:$radiusMeters,$latitude,$longitude);
          node["amenity"="restaurant"](around:$radiusMeters,$latitude,$longitude);
          node["tourism"="hotel"](around:$radiusMeters,$latitude,$longitude);
        );
        out body;
        >;
        out skel qt;
    """.trimIndent()
}