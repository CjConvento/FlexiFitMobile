package com.example.flexifitapp

class SettingsRepository(private val api: SettingsApiService) {

    suspend fun getNotificationSettings(): Result<NotificationSettingsDto> {
        return try {
            Result.success(api.getNotificationSettings())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationSettings(dto: NotificationSettingsDto): Result<Unit> {
        return try {
            val response = api.updateNotificationSettings(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Update failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}