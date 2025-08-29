package presentation

import application.service.VehicleSimulationService
import domain.repository.VehicleCoordinatesRepository
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