package presentation

import application.service.VehicleSimulationService
import domain.usecase.SimulateVehicleMovement
import infrastructure.api.APIClient
import infrastructure.api.SupabaseVehicleRepositoryImpl
import infrastructure.persistence.FileVehicleStateRepositoryImpl
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask

class SimulationRunner {

    fun startSimulation() {
        val vehicleRepository = SupabaseVehicleRepositoryImpl(APIClient())
        val vehicleStateRepository = FileVehicleStateRepositoryImpl()
        val simulateMovement = SimulateVehicleMovement()
        val simulationService = VehicleSimulationService(
            vehicleRepository,
            vehicleStateRepository,
            simulateMovement
        )

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runBlocking {
                    simulationService.simulateAllVehicles()
                }
            }
        }, 0, 5000)

        setupShutdownHook(vehicleRepository)
    }

    private fun setupShutdownHook(vehicleRepository: SupabaseVehicleRepositoryImpl) {
        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                val vehicles = vehicleRepository.getVehicles() ?: emptyList()
                vehicles.forEach { vehicle ->
                    vehicleRepository.updateStatus(vehicle.imei, true)
                    vehicleRepository.updateSpeed(vehicle.imei, 0.0)
                }
                println("Estados dos veículos salvos e aplicação parada.")
            }
        })
    }
}