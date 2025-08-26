package domain.repository

import domain.model.Vehicle
import domain.model.VehicleCoordinates

interface VehicleRepository {
    suspend fun getVehicles(): List<Vehicle>?
    suspend fun updateMileage(vehicleId: Int, mileage: Double): Boolean
    suspend fun updateCoordinates(imei: String, coordinates: VehicleCoordinates): Boolean
    suspend fun updateStatus(imei: String, isStopped: Boolean): Boolean
    suspend fun updateSpeed(imei: String, speed: Double): Boolean
}