package domain.usecase

import application.service.RouteVerificationService
import domain.model.RouteArea
import domain.model.VehicleState
import kotlin.random.Random

class SimulateVehicleMovement {
    private val routeVerificationService = RouteVerificationService()

    fun execute(
        prevState: VehicleState,
        speed: Double,
        elapsedTimeHours: Double,
        random: Random
    ): VehicleState {
        val distance = speed * elapsedTimeHours
        val displacementFactor = 0.05
        val displacementLat = random.nextDouble(-displacementFactor, displacementFactor) * distance
        val displacementLon = random.nextDouble(-displacementFactor, displacementFactor) * distance

        var newLat = prevState.latitude + displacementLat
        var newLon = prevState.longitude + displacementLon

        if (!routeVerificationService.isOnRoute(newLat, newLon) ||
            !RouteArea.isInRouteArea(newLat, newLon)) {

            val (routeLat, routeLon) = RouteArea.getNearestRouteCoordinate(newLat, newLon)
            newLat = routeLat
            newLon = routeLon
        }

        val newMileage = prevState.mileage + distance

        return VehicleState(
            latitude = newLat,
            longitude = newLon,
            mileage = newMileage
        )
    }
}