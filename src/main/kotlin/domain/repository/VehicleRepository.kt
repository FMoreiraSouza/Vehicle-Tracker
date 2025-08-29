package domain.repository

import domain.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(): List<Vehicle>?
    suspend fun getVehicleByPlateNumber(plateNumber: String): Vehicle?
    suspend fun updateMileage(vehicleId: Int, mileage: Double): Boolean
    suspend fun updateDefectStatus(vehicleId: Int, hasDefect: Boolean): Boolean
}