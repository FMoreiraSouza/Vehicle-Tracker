package domain.usecase

import domain.model.Vehicle
import domain.model.VehicleCoordinates
import domain.model.VehicleState
import domain.repository.VehicleRepository
import domain.repository.VehicleCoordinatesRepository
import domain.repository.VehicleStateRepository

class UpdateVehicleState(
    private val vehicleRepository: VehicleRepository,
    private val coordinatesRepository: VehicleCoordinatesRepository,
    private val vehicleStateRepository: VehicleStateRepository
) {
    suspend fun execute(vehicle: Vehicle, newState: VehicleState, speed: Double) {
        val coordinates = VehicleCoordinates(
            imei = vehicle.imei,
            latitude = newState.latitude,
            longitude = newState.longitude,
            speed = speed,
            isStopped = false
        )

        val previousState = vehicleStateRepository.loadState(vehicle.imei)
        val distanceTraveled = if (previousState != null) {
            newState.mileage - previousState.mileage
        } else {
            0.0
        }

        val isCoordinatesUpdated = coordinatesRepository.updateCoordinates(vehicle.imei, coordinates)
        val isMileageUpdated = vehicleRepository.updateMileage(vehicle.id, newState.mileage)
        val isSpeedUpdated = coordinatesRepository.updateSpeed(vehicle.imei, speed)

        logResults(
            vehicle,
            newState,
            speed,
            distanceTraveled,
            isCoordinatesUpdated,
            isMileageUpdated,
            isSpeedUpdated
        )
        vehicleStateRepository.saveState(vehicle.imei, newState)
    }

    private fun logResults(
        vehicle: Vehicle,
        newState: VehicleState,
        speed: Double,
        distanceTraveled: Double,
        isCoordinatesUpdated: Boolean,
        isMileageUpdated: Boolean,
        isSpeedUpdated: Boolean
    ) {
        val formattedMileage = String.format("%.2f", newState.mileage)
        val formattedSpeed = String.format("%.2f", speed)
        val formattedDistance = String.format("%.2f", distanceTraveled)

        if (isCoordinatesUpdated) {
            println("Veículo ${vehicle.plateNumber} atualizado para coordenadas (${String.format("%.6f", newState.latitude)}, ${String.format("%.6f", newState.longitude)})")
        } else {
            println("Não foi possível atualizar as coordenadas do veículo ${vehicle.plateNumber}.")
        }

        if (isMileageUpdated) {
            println("Veículo ${vehicle.plateNumber} atualizado com êxito com quilometragem $formattedMileage Km")
            println("Velocidade atual: $formattedSpeed Km/h")
            println("Distância percorrida neste intervalo: $formattedDistance Km")
        } else {
            println("Falha ao atualizar quilometragem do veículo ${vehicle.plateNumber}.")
        }

        if (isSpeedUpdated) {
            println("Veículo ${vehicle.plateNumber} atualizado com êxito com velocidade $formattedSpeed Km/h")
        } else {
            println("Falha ao atualizar velocidade do veículo ${vehicle.plateNumber}.")
        }
    }
}