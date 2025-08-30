package domain.usecase

import domain.model.RouteArea
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
        val maxDisplacementKm = 2.0

        val limitedDistance = distance.coerceAtMost(maxDisplacementKm)

        val displacementFactor = 0.2
        val (newLat, newLon) = if (random.nextDouble() < 0.7) {
            val displacementLat = random.nextDouble(-displacementFactor, displacementFactor) * limitedDistance
            val displacementLon = random.nextDouble(-displacementFactor, displacementFactor) * limitedDistance

            val tentativeLat = prevState.latitude + displacementLat
            val tentativeLon = prevState.longitude + displacementLon

            if (!RouteArea.isInRouteArea(tentativeLat, tentativeLon)) {
                adjustToRouteArea(tentativeLat, tentativeLon)
            } else {
                Pair(tentativeLat, tentativeLon)
            }
        } else {
            RouteArea.generateNearbyCoordinate(prevState.latitude, prevState.longitude)
        }

        val newMileage = prevState.mileage + limitedDistance

        return VehicleState(
            latitude = newLat,
            longitude = newLon,
            mileage = newMileage
        )
    }

    private fun adjustToRouteArea(lat: Double, lon: Double): Pair<Double, Double> {
        val adjustedLat = lat.coerceIn(RouteArea.minLat, RouteArea.maxLat)
        val adjustedLon = lon.coerceIn(RouteArea.minLon, RouteArea.maxLon)
        return Pair(adjustedLat, adjustedLon)
    }
}