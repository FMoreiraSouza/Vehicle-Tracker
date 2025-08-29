package infrastructure.api

import domain.model.Vehicle
import domain.repository.VehicleRepository
import infrastructure.serialization.MoshiConfig
import com.squareup.moshi.Types
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

class VehicleRepositoryImpl(
    private val apiClient: APIClient
) : VehicleRepository {
    private val moshi = MoshiConfig.moshi

    override suspend fun getVehicles(): List<Vehicle>? {
        val url = "vehicles"
        val response = apiClient.makeRequest(url, "GET")
        if (!response.isSuccessful) throw IOException("Código inesperado $response")

        val type = Types.newParameterizedType(List::class.java, Vehicle::class.java)
        val adapter = moshi.adapter<List<Vehicle>>(type)
        return adapter.fromJson(response.body!!.string())
    }

    override suspend fun getVehicleByPlateNumber(plateNumber: String): Vehicle? {
        val url = "vehicles?plate_number=eq.$plateNumber"
        val response = apiClient.makeRequest(url, "GET")

        if (!response.isSuccessful) {
            println("Falha ao buscar veículo pela placa $plateNumber. Código: ${response.code}")
            return null
        }

        val type = Types.newParameterizedType(List::class.java, Vehicle::class.java)
        val adapter = moshi.adapter<List<Vehicle>>(type)
        val vehicles = adapter.fromJson(response.body!!.string())
        return vehicles?.firstOrNull()
    }

    override suspend fun updateMileage(vehicleId: Int, mileage: Double): Boolean {
        val url = "vehicles?id=eq.$vehicleId"
        val jsonBody = """
            {
                "mileage": $mileage
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar quilometragem do veículo $vehicleId. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    override suspend fun updateDefectStatus(vehicleId: Int, hasDefect: Boolean): Boolean {
        val url = "vehicles?id=eq.$vehicleId"
        val jsonBody = """
            {
                "hasDefect": $hasDefect
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar status de defeito do veículo $vehicleId. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }
}