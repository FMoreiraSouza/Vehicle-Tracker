package application.service

import domain.model.RouteArea
import domain.model.Vehicle
import domain.model.VehicleState
import domain.repository.VehicleRepository
import domain.repository.VehicleStateRepository
import domain.repository.VehicleCoordinatesRepository
import domain.repository.NotificationRepository
import domain.usecase.*
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
    private val defectStartTime = mutableMapOf<String, Long>()
    private val lastDefectCheckTime = mutableMapOf<String, Long>()
    private val returnActivityNotificationCount = mutableMapOf<String, Int>()
    private val maxReturnNotifications = 2

    private val minSpeed = 40.0
    private val maxSpeed = 150.0
    private val speedVariationRange = 45.0
    private val movementVariationChance = 0.25
    private val defectCheckInterval = 30000L

    private val handleDefect = HandleVehicleDefect(coordinatesRepository, notificationRepository, vehicleRepository)
    private val handlePause = HandleVehiclePause(coordinatesRepository, vehicleRepository)
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
            val (initialLat, initialLon) = RouteArea.generateRandomCoordinate()
            val initialMileage = Random.nextDouble(0.0, 10000.0)
            vehicleStateRepository.saveState(
                vehicle.imei,
                VehicleState(initialLat, initialLon, initialMileage)
            )
        }

        vehicleSpeeds[vehicle.imei] = vehicleSpeeds[vehicle.imei] ?: Random.nextDouble(minSpeed, maxSpeed)
        vehiclePaused[vehicle.imei] = vehiclePaused[vehicle.imei] ?: false
        vehiclePauseTime[vehicle.imei] = vehiclePauseTime[vehicle.imei] ?: 0L
        vehicleLastUpdateTime[vehicle.imei] = vehicleLastUpdateTime[vehicle.imei] ?: System.currentTimeMillis()
        vehicleHasDefect[vehicle.imei] = vehicleHasDefect[vehicle.imei] ?: vehicle.hasDefect
        lastDefectCheckTime[vehicle.imei] = lastDefectCheckTime[vehicle.imei] ?: System.currentTimeMillis()
        returnActivityNotificationCount[vehicle.imei] = returnActivityNotificationCount[vehicle.imei] ?: 0
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

        if (shouldCheckDefectStatus(vehicle.imei, currentTime)) {
            checkDefectStatusFromDatabase(vehicle)
            lastDefectCheckTime[vehicle.imei] = currentTime
        }

        coordinatesRepository.updateStatus(vehicle.imei, false)

        if (vehicleHasDefect[vehicle.imei] == false && defectStartTime.containsKey(vehicle.imei)) {
            handleDefectResolvedByApp(vehicle)
        }

        if (vehicleHasDefect[vehicle.imei] == true) {
            handleDefectResolution(vehicle, currentTime)
            if (vehicleHasDefect[vehicle.imei] == true) {
                handleDefect.execute(vehicle, true)
                return
            }
        }

        if (vehiclePaused[vehicle.imei] == true) {
            handlePauseResume(vehicle, currentTime)
            if (vehiclePaused[vehicle.imei] == true) return

            generateNewSpeed(vehicle.imei)
            println(
                "Veículo ${vehicle.plateNumber} retomou movimento com velocidade: ${
                    String.format(
                        "%.2f",
                        vehicleSpeeds[vehicle.imei]
                    )
                } km/h"
            )
        }

        if (handlePause.shouldPause()) {
            handleNewPause(vehicle, currentTime)
            return
        }

        handleVehicleMovement(vehicle, state, speed, currentTime)
    }

    private fun shouldCheckDefectStatus(imei: String, currentTime: Long): Boolean {
        val lastCheck = lastDefectCheckTime[imei] ?: 0L
        return currentTime - lastCheck > defectCheckInterval
    }

    private suspend fun checkDefectStatusFromDatabase(vehicle: Vehicle) {
        try {
            val currentVehicle = vehicleRepository.getVehicleByPlateNumber(vehicle.plateNumber)
            if (currentVehicle != null && currentVehicle.hasDefect != vehicleHasDefect[vehicle.imei]) {
                vehicleHasDefect[vehicle.imei] = currentVehicle.hasDefect

                if (!currentVehicle.hasDefect) {
                    val currentCount = returnActivityNotificationCount[vehicle.imei] ?: 0
                    if (currentCount < maxReturnNotifications) {
                        println("Veículo ${vehicle.plateNumber} voltou à atividade via aplicativo!")
                        returnActivityNotificationCount[vehicle.imei] = currentCount + 1
                    }
                    defectStartTime.remove(vehicle.imei)
                } else {
                    println("Veículo ${vehicle.plateNumber} entrou em defeito via aplicativo!")
                    defectStartTime[vehicle.imei] = System.currentTimeMillis()
                }
            }
        } catch (e: Exception) {
            println("Erro ao verificar status de defeito do veículo ${vehicle.plateNumber}: ${e.message}")
        }
    }

    private suspend fun handleDefectResolvedByApp(vehicle: Vehicle) {
        val currentCount = returnActivityNotificationCount[vehicle.imei] ?: 0
        if (currentCount < maxReturnNotifications) {
            println("🎉 Veículo ${vehicle.plateNumber} retornando à atividade após suporte via app!")
            returnActivityNotificationCount[vehicle.imei] = currentCount + 1
        }

        defectStartTime.remove(vehicle.imei)

        coordinatesRepository.updateStatus(vehicle.imei, false)
        coordinatesRepository.updateSpeed(vehicle.imei, Random.nextDouble(minSpeed, maxSpeed))

        generateNewSpeed(vehicle.imei)
        println(
            "Veículo ${vehicle.plateNumber} retomou movimento com velocidade: ${
                String.format(
                    "%.2f",
                    vehicleSpeeds[vehicle.imei]
                )
            } km/h"
        )
    }

    private suspend fun handleDefectResolution(vehicle: Vehicle, currentTime: Long) {
        val defectDuration = currentTime - (defectStartTime[vehicle.imei] ?: currentTime)

        if (defectDuration > Random.nextLong(120000, 600000)) {
            vehicleHasDefect[vehicle.imei] = false
            defectStartTime.remove(vehicle.imei)
            handleDefect.execute(vehicle, false)
            println("Veículo ${vehicle.plateNumber} saiu do estado de defeito após ${defectDuration / 1000} segundos.")
        }
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

    private suspend fun handleNewPause(vehicle: Vehicle, currentTime: Long) {
        if (vehicleHasDefect[vehicle.imei] == true) {
            return
        }

        vehiclePaused[vehicle.imei] = true
        vehiclePauseTime[vehicle.imei] = handlePause.calculatePauseTime()

        val isDefect = handlePause.isDefect()
        if (isDefect) {
            vehicleHasDefect[vehicle.imei] = true
            defectStartTime[vehicle.imei] = currentTime
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
            val variation = Random.nextDouble(-8.0, 8.0)
            val variedSpeed = (currentSpeed + variation).coerceIn(minSpeed, maxSpeed)
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