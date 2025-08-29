package domain.repository

import domain.model.VehicleCoordinates

interface VehicleCoordinatesRepository {
    suspend fun updateCoordinates(imei: String, coordinates: VehicleCoordinates): Boolean
    suspend fun updateStatus(imei: String, isStopped: Boolean): Boolean
    suspend fun updateSpeed(imei: String, speed: Double): Boolean
    suspend fun checkIfCoordinatesExist(imei: String): Boolean
}