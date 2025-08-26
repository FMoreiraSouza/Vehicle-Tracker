package domain.repository

import domain.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(): List<Vehicle>?
    suspend fun updateMileage(vehicleId: Int, mileage: Double): Boolean
}