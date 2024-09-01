package controller

import model.Vehicle
import network.ApiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

class VehicleController {

    private val apiClient = ApiClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getVehicles(): List<Vehicle>? {
        val url = "vehicles"
        val response = apiClient.makeRequest(url, "GET")

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val type = Types.newParameterizedType(List::class.java, Vehicle::class.java)
        val adapter = moshi.adapter<List<Vehicle>>(type)
        return adapter.fromJson(response.body!!.string())
    }

    fun updateVehicleCoordinates(imei: String, latitude: Double, longitude: Double): Boolean {
        return if (checkIfCoordinatesExist(imei)) {
            updateCoordinates(imei, latitude, longitude)
        } else {
            insertCoordinates(imei, latitude, longitude)
        }
    }

    private fun checkIfCoordinatesExist(imei: String): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"
        val response = apiClient.makeRequest(url, "GET")

        if (!response.isSuccessful) {
            println("Falha ao verificar coordenadas do rastreador $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
            return false
        }

        val jsonResponse = response.body?.string()
        val coordinates = moshi.adapter<List<Map<String, Any>>>(Types.newParameterizedType(List::class.java, Map::class.java)).fromJson(jsonResponse)
        return coordinates?.isNotEmpty() == true
    }

    private fun updateCoordinates(imei: String, latitude: Double, longitude: Double): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"
        val jsonBody = """
            {
                "latitude": $latitude,
                "longitude": $longitude
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar as coordenadas do rastreador $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    private fun insertCoordinates(imei: String, latitude: Double, longitude: Double): Boolean {
        val url = "vehicle_coordinates"
        val jsonBody = """
            {
                "imei": "$imei",
                "latitude": $latitude,
                "longitude": $longitude
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val response = apiClient.makeRequest(url, "POST", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao inserir coordenadas para o veículo com imei $imei. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    fun updateMileage(vehicleId: Int, mileage: Double): Boolean {
        val url = "vehicles?id=eq.$vehicleId"

        val jsonBody = """
        {
            "mileage": $mileage
        }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar a quilometragem do veículo $vehicleId. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }

    fun updateVehicleStatus(vehicleId: Int, imei:String, isStopped: Boolean): Boolean {
        val url = "vehicle_coordinates?imei=eq.$imei"

        val jsonBody = """
        {
            "isStopped": $isStopped
        }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val response = apiClient.makeRequest(url, "PATCH", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao atualizar o status do veículo $vehicleId. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }
}
