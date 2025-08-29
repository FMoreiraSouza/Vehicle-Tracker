package domain.usecase

import domain.repository.VehicleCoordinatesRepository
import kotlin.random.Random

class HandleVehiclePause(
    private val coordinatesRepository: VehicleCoordinatesRepository
) {
    suspend fun execute(vehicle: domain.model.Vehicle, isDefect: Boolean) {
        coordinatesRepository.updateStatus(vehicle.imei, true)
        coordinatesRepository.updateSpeed(vehicle.imei, 0.0)

        if (isDefect) {
            println("Veículo ${vehicle.plateNumber} parado devido a defeito técnico.")
        } else {
            println("Veículo ${vehicle.plateNumber} pausado para abastecimento.")
        }
    }

    fun shouldPause(): Boolean = Random.nextDouble() < 0.5
    fun isDefect(): Boolean = Random.nextDouble() < 0.1
    fun calculatePauseTime(): Long = System.currentTimeMillis() + Random.nextLong(10000, 30000)
}