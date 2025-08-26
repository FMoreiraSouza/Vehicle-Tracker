package infrastructure.api

import domain.model.VehicleCoordinates
import infrastructure.serialization.MoshiConfig
import com.squareup.moshi.Types
import domain.repository.VehicleCoordinatesRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class VehicleCoordinatesRepositoryImpl(
    private val apiClient: APIClient
) : VehicleCoordinatesRepository {
    private val moshi = MoshiConfig.moshi

    override suspend fun updateCoordinates(imei: String, coordinates: VehicleCoordinates): Boolean {
        return if (checkIfCoordinatesExist(imei)) {
            updateExistingCoordinates(imei, coordinates)
        } else {
            insertCoordinates(imei, coordinates)
        }
    }

    override suspend fun updateStatus(imei: String, isStopped: Boolean): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"
        val jsonBody = """
            {
                "isStopped": $isStopped
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar status do veículo com imei $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    override suspend fun updateSpeed(imei: String, speed: Double): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"
        val jsonBody = """
            {
                "speed": $speed
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar velocidade do veículo com imei $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    override suspend fun checkIfCoordinatesExist(imei: String): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"
        val response = apiClient.makeRequest(url, "GET")

        if (!response.isSuccessful) {
            println("Falha ao verificar coordenadas do rastreador $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
            return false
        }

        val jsonResponse = response.body?.string()
        val coordinates =
            jsonResponse?.let {
                moshi.adapter<List<Map<String, Any>>>(Types.newParameterizedType(List::class.java, Map::class.java))
                    .fromJson(it)
            }
        return coordinates?.isNotEmpty() == true
    }

    private fun updateExistingCoordinates(imei: String, coordinates: VehicleCoordinates): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"
        val jsonBody = """
            {
                "latitude": ${coordinates.latitude},
                "longitude": ${coordinates.longitude},
                "speed": ${coordinates.speed}
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar coordenadas e velocidade do rastreador $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    private fun insertCoordinates(imei: String, coordinates: VehicleCoordinates): Boolean {
        val url = "vehicle_coordinates"
        val jsonBody = """
            {
                "imei": "$imei",
                "latitude": ${coordinates.latitude},
                "longitude": ${coordinates.longitude},
                "speed": ${coordinates.speed}
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val response = apiClient.makeRequest(url, "POST", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao inserir coordenadas e velocidade do veículo com imei $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }
}