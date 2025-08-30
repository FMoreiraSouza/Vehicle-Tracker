package domain.usecase

import domain.repository.VehicleCoordinatesRepository
import domain.repository.VehicleRepository
import kotlin.random.Random

class HandleVehiclePause(
    private val coordinatesRepository: VehicleCoordinatesRepository,
    private val vehicleRepository: VehicleRepository
) {
    private val defectStatusUpdated = mutableMapOf<String, Boolean>()

    suspend fun execute(vehicle: domain.model.Vehicle, isDefect: Boolean) {
        coordinatesRepository.updateStatus(vehicle.imei, true)
        coordinatesRepository.updateSpeed(vehicle.imei, 0.0)

        if (isDefect) {
            if (defectStatusUpdated[vehicle.imei] != true) {
                vehicleRepository.updateDefectStatus(vehicle.id, true)
                defectStatusUpdated[vehicle.imei] = true
                println("Status de defeito do veículo ${vehicle.plateNumber} atualizado para true.")
            }
            println("Veículo ${vehicle.plateNumber} parado devido a defeito técnico.")
        } else {
            println("Veículo ${vehicle.plateNumber} pausado para abastecimento.")
        }
    }

    fun shouldPause(): Boolean = Random.nextDouble() < 0.2
    fun isDefect(): Boolean = Random.nextDouble() < 0.2
    fun calculatePauseTime(): Long = System.currentTimeMillis() + Random.nextLong(10000, 30000)
}