package domain.model

import com.squareup.moshi.Json
import java.io.Serializable

data class VehicleState(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "mileage") val mileage: Double
) : Serializable