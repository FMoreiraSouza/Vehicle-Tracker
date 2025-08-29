package domain.model

import com.squareup.moshi.Json
import java.io.Serializable

data class Notification(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "plate_number") val plateNumber: String,
    @Json(name = "message") val message: String,
    @Json(name = "created_at") val createdAt: String? = null
) : Serializable