package domain.model

data class VehicleCoordinates(
    val imei: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val isStopped: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)