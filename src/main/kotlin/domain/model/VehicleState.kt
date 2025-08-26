package domain.model

import java.io.Serializable

data class VehicleState(
    val latitude: Double,
    val longitude: Double,
    val mileage: Double
) : Serializable