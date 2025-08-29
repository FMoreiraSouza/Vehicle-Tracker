package presentation

import application.service.VehicleSimulationService
import domain.repository.VehicleCoordinatesRepository
import domain.repository.VehicleRepository
import domain.usecase.SimulateVehicleMovement
import infrastructure.api.APIClient
import infrastructure.api.VehicleRepositoryImpl
import infrastructure.api.NotificationRepositoryImpl
import infrastructure.api.VehicleCoordinatesRepositoryImpl
import infrastructure.persistence.VehicleStateRepositoryImpl
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask

class SimulationRunner {

    suspend fun startSimulation() {
        val apiClient = APIClient()
        val vehicleRepository = VehicleRepositoryImpl(apiClient)
        val coordinatesRepository = VehicleCoordinatesRepositoryImpl(apiClient)
        val notificationRepository = NotificationRepositoryImpl(apiClient)
        val vehicleStateRepository = VehicleStateRepositoryImpl()
        val simulateMovement = SimulateVehicleMovement()

        resetAllVehiclesToNoDefect(vehicleRepository)

        val simulationService = VehicleSimulationService(
            vehicleRepository = vehicleRepository,
            coordinatesRepository = coordinatesRepository,
            notificationRepository = notificationRepository,
            vehicleStateRepository = vehicleStateRepository,
            simulateMovement = simulateMovement
        )

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runBlocking {
                    simulationService.simulateAllVehicles()
                }
            }
        }, 0, 5000)

        setupShutdownHook(coordinatesRepository, vehicleRepository.getVehicles())
    }

    private suspend fun resetAllVehiclesToNoDefect(vehicleRepository: VehicleRepository) {
        try {
            val vehicles = vehicleRepository.getVehicles()
            if (vehicles != null) {
                var successCount = 0
                var failCount = 0

                vehicles.forEach { vehicle ->
                    if (vehicle.hasDefect) {
                        val success = vehicleRepository.updateDefectStatus(vehicle.id, false)
                        if (success) {
                            successCount++
                        } else {
                            failCount++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Erro ao resetar veículos: ${e.message}")
        }
    }

    private fun setupShutdownHook(
        coordinatesRepository: VehicleCoordinatesRepository,
        vehicles: List<domain.model.Vehicle>?
    ) {
        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                vehicles?.forEach { vehicle ->
                    coordinatesRepository.updateStatus(vehicle.imei, true)
                    coordinatesRepository.updateSpeed(vehicle.imei, 0.0)
                }
                println("Estados dos veículos salvos e aplicação parada.")
            }
        })
    }
}