package domain.usecase

import domain.model.VehicleState
import kotlin.random.Random

class SimulateVehicleMovement {
    fun execute(
        prevState: VehicleState,
        speed: Double,
        elapsedTimeHours: Double,
        random: Random
    ): VehicleState {
        val distance = speed * elapsedTimeHours
        val displacementLat = random.nextDouble(-0.01, 0.01) * distance
        val displacementLon = random.nextDouble(-0.01, 0.01) * distance
        val newLat = prevState.latitude + displacementLat
        val newLon = prevState.longitude + displacementLon
        val newMileage = prevState.mileage + distance
        return VehicleState(
            latitude = newLat,
            longitude = newLon,
            mileage = newMileage
        )
    }
}