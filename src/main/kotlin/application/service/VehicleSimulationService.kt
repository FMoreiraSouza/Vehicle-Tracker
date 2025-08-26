package application.service

import domain.model.Vehicle
import domain.model.VehicleCoordinates
import domain.model.VehicleState
import domain.repository.VehicleRepository
import domain.repository.VehicleStateRepository
import domain.usecase.SimulateVehicleMovement
import kotlin.random.Random

class VehicleSimulationService(
    private val vehicleRepository: VehicleRepository,
    private val vehicleStateRepository: VehicleStateRepository,
    private val simulateMovement: SimulateVehicleMovement
) {
    private val vehicleSpeeds = mutableMapOf<String, Double>()
    private val vehiclePaused = mutableMapOf<String, Boolean>()
    private val vehiclePauseTime = mutableMapOf<String, Long>()
    private val vehicleLastUpdateTime = mutableMapOf<String, Long>()
    private val vehicleHasDefect = mutableMapOf<String, Boolean>()
    private val vehicleIsStopped = mutableMapOf<String, Boolean>()

    suspend fun simulateAllVehicles() {
        val vehicles = vehicleRepository.getVehicles() ?: return
        vehicles.forEach { vehicle ->
            initializeVehicleState(vehicle)
            simulateVehicle(vehicle)
        }
    }

    private fun initializeVehicleState(vehicle: Vehicle) {
        if (vehicleStateRepository.loadState(vehicle.imei) == null) {
            val initialLat = Random.nextDouble(-90.0, 90.0)
            val initialLon = Random.nextDouble(-180.0, 180.0)
            val initialMileage = Random.nextDouble(0.0, 10000.0)
            vehicleStateRepository.saveState(
                vehicle.imei,
                VehicleState(initialLat, initialLon, initialMileage)
            )
        }
        vehicleSpeeds[vehicle.imei] = vehicleSpeeds[vehicle.imei] ?: Random.nextDouble(40.0, 80.0)
        vehiclePaused[vehicle.imei] = vehiclePaused[vehicle.imei] ?: false
        vehiclePauseTime[vehicle.imei] = vehiclePauseTime[vehicle.imei] ?: 0L
        vehicleLastUpdateTime[vehicle.imei] = vehicleLastUpdateTime[vehicle.imei] ?: System.currentTimeMillis()
        vehicleHasDefect[vehicle.imei] = vehicleHasDefect[vehicle.imei] ?: false
        vehicleIsStopped[vehicle.imei] = vehicleIsStopped[vehicle.imei] ?: false
    }

    private suspend fun simulateVehicle(vehicle: Vehicle) {
        val currentTime = System.currentTimeMillis()
        val state = vehicleStateRepository.loadState(vehicle.imei) ?: return
        val speed = vehicleSpeeds[vehicle.imei]!!

        // Check if backend is down
        if (!isApplicationRunning()) {
            vehicleIsStopped[vehicle.imei] = true
            vehicleRepository.updateStatus(vehicle.imei, true)
            vehicleRepository.updateSpeed(vehicle.imei, 0.0)
            println("Veículo ${vehicle.plateNumber} parado devido a falha na aplicação.")
            return
        }

        // Reset stopped status if backend is online
        vehicleIsStopped[vehicle.imei] = false
        vehicleRepository.updateStatus(vehicle.imei, false)

        // Check for defect
        if (vehicleHasDefect[vehicle.imei] == true) {
            vehicleIsStopped[vehicle.imei] = true
            vehicleRepository.updateStatus(vehicle.imei, true)
            vehicleRepository.updateSpeed(vehicle.imei, 0.0)
            println("Veículo ${vehicle.plateNumber} parado devido a defeito técnico.")
            return
        }

        // Check for pause
        if (vehiclePaused[vehicle.imei] == true) {
            if (currentTime >= vehiclePauseTime[vehicle.imei]!!) {
                vehiclePaused[vehicle.imei] = false
                println("Veículo ${vehicle.plateNumber} retomou o movimento após pausa.")
            } else {
                vehicleIsStopped[vehicle.imei] = true
                vehicleRepository.updateStatus(vehicle.imei, true)
                vehicleRepository.updateSpeed(vehicle.imei, 0.0)
                println("Veículo ${vehicle.plateNumber} está pausado e não atualizando coordenadas.")
                return
            }
        }

        // Randomly decide to pause
        if (Random.nextDouble() < 0.5) {
            vehiclePaused[vehicle.imei] = true
            vehiclePauseTime[vehicle.imei] = currentTime + Random.nextLong(10000, 30000)
            if (Random.nextDouble() < 0.0) { // 0% chance for defect (as in original)
                vehicleHasDefect[vehicle.imei] = true
                vehicleIsStopped[vehicle.imei] = true
                vehicleRepository.updateStatus(vehicle.imei, true)
                vehicleRepository.updateSpeed(vehicle.imei, 0.0)
                println("Veículo ${vehicle.plateNumber} parado devido a defeito técnico.")
                return
            } else {
                vehicleIsStopped[vehicle.imei] = true
                vehicleRepository.updateStatus(vehicle.imei, true)
                vehicleRepository.updateSpeed(vehicle.imei, 0.0)
                println("Veículo ${vehicle.plateNumber} pausado por motivos normais (abastecimento, descarga, etc.).")
                return
            }
        }

        // Calculate new state
        val elapsedTimeHours = (currentTime - vehicleLastUpdateTime[vehicle.imei]!!) / 3600000.0
        val newState = simulateMovement.execute(state, speed, elapsedTimeHours, Random)

        // Update database
        val coordinates = VehicleCoordinates(
            imei = vehicle.imei,
            latitude = newState.latitude,
            longitude = newState.longitude,
            speed = speed,
            isStopped = false
        )
        val isCoordinatesUpdated = vehicleRepository.updateCoordinates(vehicle.imei, coordinates)
        val isMileageUpdated = vehicleRepository.updateMileage(vehicle.id, newState.mileage)
        val isSpeedUpdated = vehicleRepository.updateSpeed(vehicle.imei, speed)

        // Log results
        val formattedMileage = String.format("%.2f", newState.mileage)
        val formattedSpeed = String.format("%.2f", speed)
        val formattedDistance = String.format("%.2f", speed * elapsedTimeHours)

        if (isCoordinatesUpdated) {
            println("Veículo ${vehicle.plateNumber} atualizado com sucesso para coordenadas (${newState.latitude}, ${newState.longitude})")
        } else {
            println("Falha ao atualizar coordenadas do veículo ${vehicle.plateNumber}.")
        }

        if (isMileageUpdated) {
            println("Veículo ${vehicle.plateNumber} atualizado com sucesso com quilometragem $formattedMileage Km")
            println("Velocidade atual: $formattedSpeed Km/h")
            println("Distância percorrida neste intervalo: $formattedDistance Km")
        } else {
            println("Falha ao atualizar quilometragem do veículo ${vehicle.plateNumber}.")
        }

        if (isSpeedUpdated) {
            println("Veículo ${vehicle.plateNumber} atualizado com sucesso com velocidade $formattedSpeed Km/h")
        } else {
            println("Falha ao atualizar velocidade do veículo ${vehicle.plateNumber}.")
        }

        // Update state
        vehicleStateRepository.saveState(vehicle.imei, newState)
        vehicleLastUpdateTime[vehicle.imei] = currentTime
    }

    private fun isApplicationRunning(): Boolean {
        return true // Simulate backend status check
    }
}