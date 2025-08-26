package infrastructure.api

import domain.model.Notification
import domain.repository.NotificationRepository
import infrastructure.serialization.MoshiConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class NotificationRepositoryImpl(
    private val apiClient: APIClient
) : NotificationRepository {
    private val moshi = MoshiConfig.moshi

    override suspend fun insertNotification(plateNumber: String, message: String): Boolean {
        val url = "notifications"

        val notification = Notification(
            plateNumber = plateNumber,
            message = message
        )

        val jsonBody = moshi.adapter(Notification::class.java).toJson(notification)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val response = apiClient.makeRequest(url, "POST", requestBody)

        if (!response.isSuccessful) {
            println("Falha ao inserir notificação para o veículo $plateNumber. Código: ${response.code}. Resposta: ${response.body?.string()}")
        }

        return response.isSuccessful
    }
}