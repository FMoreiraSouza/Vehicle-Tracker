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

    private val minSpeed = 20.0
    private val maxSpeed = 100.0
    private val speedVariationRange = 25.0
    private val movementVariationChance = 0.25

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

        vehicleSpeeds[vehicle.imei] = vehicleSpeeds[vehicle.imei] ?:
                Random.nextDouble(minSpeed, maxSpeed)
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

            generateNewSpeed(vehicle.imei)
            println("Veículo ${vehicle.plateNumber} retomou movimento com velocidade: ${String.format("%.2f", vehicleSpeeds[vehicle.imei])} km/h")
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

        val currentSpeed = applySpeedVariation(vehicle.imei, speed)

        val newState = simulateMovement.execute(state, currentSpeed, elapsedTimeHours, Random)
        updateState.execute(vehicle, newState, currentSpeed)

        vehicleLastUpdateTime[vehicle.imei] = currentTime
    }

    private fun applySpeedVariation(imei: String, currentSpeed: Double): Double {
        if (Random.nextDouble() < movementVariationChance) {
            val smallVariation = Random.nextDouble(-8.0, 8.0)
            val variedSpeed = (currentSpeed + smallVariation).coerceIn(minSpeed, maxSpeed)
            vehicleSpeeds[imei] = variedSpeed
            return variedSpeed
        }
        return currentSpeed
    }

    private fun generateNewSpeed(imei: String) {
        val currentSpeed = vehicleSpeeds[imei] ?: Random.nextDouble(minSpeed, maxSpeed)

        val variation = Random.nextDouble(-speedVariationRange, speedVariationRange)
        var newSpeed = currentSpeed + variation

        newSpeed = newSpeed.coerceIn(minSpeed, maxSpeed)

        vehicleSpeeds[imei] = newSpeed
    }

    private fun isApplicationRunning(): Boolean = true
}