package domain.model

import com.squareup.moshi.Json
import java.io.Serializable

data class Vehicle(
    @Json(name = "id") val id: Int,
    @Json(name = "plate_number") val plateNumber: String,
    @Json(name = "brand") val brand: String,
    @Json(name = "model") val model: String,
    @Json(name = "mileage") val mileage: Double,
    @Json(name = "imei") val imei: String,
    @Json(name = "hasDefect") val hasDefect: Boolean = false
) : Serializable