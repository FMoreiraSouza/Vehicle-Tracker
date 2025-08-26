package domain.model

import com.squareup.moshi.Json
import java.io.Serializable

data class VehicleCoordinates(
    @Json(name = "imei") val imei: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "speed") val speed: Double,
    @Json(name = "isStopped") val isStopped: Boolean,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
) : Serializable