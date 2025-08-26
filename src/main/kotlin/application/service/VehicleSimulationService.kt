package application.service

import domain.model.Vehicle
import domain.model.VehicleState
import domain.repository.VehicleRepository
import domain.repository.NotificationRepository
import domain.repository.VehicleCoordinatesRepository
import domain.repository.VehicleStateRepository
import domain.usecase.SimulateVehicleMovement
import domain.usecase.HandleVehicleDefect
import domain.usecase.HandleVehiclePause
import domain.usecase.UpdateVehicleState
import kotlin.random.Random

class VehicleSimulationService(
    private val vehicleRepository: VehicleRepository,
    private val coordinatesRepository: VehicleCoordinatesRepository,
    private val vehicleStateRepository: VehicleStateRepository,
    private val simulateMovement: SimulateVehicleMovement,
    notificationRepository: NotificationRepository
    ) {
    private val vehicleSpeeds = mutableMapOf<String, Double>()
    private val vehiclePaused = mutableMapOf<String, Boolean>()
    private val vehiclePauseTime = mutableMapOf<String, Long>()
    private val vehicleLastUpdateTime = mutableMapOf<String, Long>()
    private val vehicleHasDefect = mutableMapOf<String, Boolean>()

    private val handleDefect = HandleVehicleDefect(coordinatesRepository, notificationRepository)
    private val handlePause = HandleVehiclePause(coordinatesRepository)
    private val updateState = UpdateVehicleState(vehicleRepository, coordinatesRepository, vehicleStateRepository)

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

        handleDefect.initializeDefectState(vehicle.imei)
    }

    private suspend fun simulateVehicle(vehicle: Vehicle) {
        val currentTime = System.currentTimeMillis()
        val state = vehicleStateRepository.loadState(vehicle.imei) ?: return
        val speed = vehicleSpeeds[vehicle.imei]!!

        if (!isApplicationRunning()) {
            handleBackendFailure(vehicle)
            return
        }

        coordinatesRepository.updateStatus(vehicle.imei, false)

        handleDefect.execute(vehicle, vehicleHasDefect[vehicle.imei] == true)
        if (vehicleHasDefect[vehicle.imei] == true) return

        if (vehiclePaused[vehicle.imei] == true) {
            handlePauseResume(vehicle, currentTime)
            if (vehiclePaused[vehicle.imei] == true) return
        }

        if (handlePause.shouldPause()) {
            handleNewPause(vehicle)
            return
        }

        handleVehicleMovement(vehicle, state, speed, currentTime)
    }

    private suspend fun handleBackendFailure(vehicle: Vehicle) {
        coordinatesRepository.updateStatus(vehicle.imei, true)
        coordinatesRepository.updateSpeed(vehicle.imei, 0.0)
        println("Veículo ${vehicle.plateNumber} parado devido a falha na aplicação.")
    }

    private suspend fun handlePauseResume(vehicle: Vehicle, currentTime: Long) {
        if (currentTime >= vehiclePauseTime[vehicle.imei]!!) {
            vehiclePaused[vehicle.imei] = false
            println("Veículo ${vehicle.plateNumber} retomou o movimento após pausa.")
        } else {
            handlePause.execute(vehicle, false)
        }
    }

    private suspend fun handleNewPause(vehicle: Vehicle) {
        vehiclePaused[vehicle.imei] = true
        vehiclePauseTime[vehicle.imei] = handlePause.calculatePauseTime()

        val isDefect = handlePause.isDefect()
        if (isDefect) {
            vehicleHasDefect[vehicle.imei] = true
            handleDefect.execute(vehicle, true)
        } else {
            handlePause.execute(vehicle, false)
        }
    }

    private suspend fun handleVehicleMovement(vehicle: Vehicle, state: VehicleState, speed: Double, currentTime: Long) {
        val elapsedTimeHours = (currentTime - vehicleLastUpdateTime[vehicle.imei]!!) / 3600000.0
        val newState = simulateMovement.execute(state, speed, elapsedTimeHours, Random)

        updateState.execute(vehicle, newState, speed)

        vehicleLastUpdateTime[vehicle.imei] = currentTime
    }

    private fun isApplicationRunning(): Boolean = true
}