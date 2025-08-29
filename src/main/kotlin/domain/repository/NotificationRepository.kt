package domain.repository

interface NotificationRepository {
    suspend fun insertNotification(plateNumber: String, message: String): Boolean
}