package lab.jhrodriguezi.nearbyspots.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import lab.jhrodriguezi.nearbyspots.ui.PoiType
import lab.jhrodriguezi.nearbyspots.ui.PointOfInterest
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApi {
    @GET("interpreter")
    suspend fun getPointsOfInterest(@Query("data") query: String): OverpassResponse
}

@JsonClass(generateAdapter = true)
data class OverpassResponse(
    @Json(name = "elements")
    val elements: List<OverpassElement>
)

@JsonClass(generateAdapter = true)
data class OverpassElement(
    @Json(name = "id")
    val id: Long,
    @Json(name = "type")
    val type: String,
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lon")
    val lon: Double,
    @Json(name = "tags")
    val tags: Map<String, String>
)

fun OverpassElement.toPointOfInterest(): PointOfInterest? {
    val name = tags["name"] ?: return null

    val type = when {
        tags["amenity"] == "hospital" -> PoiType.HOSPITAL
        tags["tourism"] == "attraction" ||
                tags["tourism"] == "museum" -> PoiType.TOURIST_ATTRACTION
        tags["amenity"] == "restaurant" -> PoiType.RESTAURANT
        tags["tourism"] == "hotel" -> PoiType.HOTEL
        else -> return null
    }

    return PointOfInterest(
        name = name,
        latitude = lat,
        longitude = lon,
        type = type
    )
}