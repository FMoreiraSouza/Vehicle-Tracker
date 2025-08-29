package domain.usecase

import domain.repository.NotificationRepository
import domain.repository.VehicleCoordinatesRepository

class HandleVehicleDefect(
    private val coordinatesRepository: VehicleCoordinatesRepository,
    private val notificationRepository: NotificationRepository
) {
    private val defectNotificationCount = mutableMapOf<String, Int>()
    private val lastNotificationTime = mutableMapOf<String, Long>()

    suspend fun execute(vehicle: domain.model.Vehicle, hasDefect: Boolean) {
        if (hasDefect) {
            handleDefect(vehicle)
        } else {
            resetDefectState(vehicle.imei)
        }
    }

    private suspend fun handleDefect(vehicle: domain.model.Vehicle) {
        coordinatesRepository.updateStatus(vehicle.imei, true)
        coordinatesRepository.updateSpeed(vehicle.imei, 0.0)

        val notificationCount = defectNotificationCount[vehicle.imei] ?: 0
        val lastTime = lastNotificationTime[vehicle.imei] ?: 0L
        val currentTime = System.currentTimeMillis()

        if (notificationCount < 2 && currentTime - lastTime >= 30000) {
            val message = if (notificationCount == 0) {
                "Veículo ${vehicle.plateNumber} parado devido a defeito técnico. Necessita de assistência."
            } else {
                "Veículo ${vehicle.plateNumber} parado por defeito técnico. Por favor, solicite suporte."
            }

            notificationRepository.insertNotification(vehicle.plateNumber, message)
            defectNotificationCount[vehicle.imei] = notificationCount + 1
            lastNotificationTime[vehicle.imei] = currentTime

            println("Notificação enviada para defeito do veículo ${vehicle.plateNumber}")
        }

        println("Veículo ${vehicle.plateNumber} parado devido a defeito técnico.")
    }

    private fun resetDefectState(imei: String) {
        defectNotificationCount[imei] = 0
        lastNotificationTime[imei] = 0L
    }

    fun initializeDefectState(imei: String) {
        defectNotificationCount[imei] = defectNotificationCount[imei] ?: 0
        lastNotificationTime[imei] = lastNotificationTime[imei] ?: 0L
    }
}