package domain.repository

import domain.model.VehicleState

interface VehicleStateRepository {
    fun saveState(imei: String, state: VehicleState)
    fun loadState(imei: String): VehicleState?
}