package com.example.flexifitapp

import okhttp3.ResponseBody
import retrofit2.Response

class SettingsRepository(private val api: SettingsApiService) {

    // --- NOTIFICATIONS ---
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
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Update failed: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- ACCOUNT MANAGEMENT (ROADMAP #2) ---

    // Reset Progress
    suspend fun resetProgress(): Result<Unit> {
        return try {
            val response = api.resetProgress()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Reset failed: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Export Data
    suspend fun exportData(): Result<ResponseBody> {
        return try {
            val response = api.exportData()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Export failed"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // Manual Email Update
    suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            val response = api.updateEmail(UpdateEmailDto(newEmail))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Email update failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Google Account Re-linking
    suspend fun linkGoogle(email: String, uid: String): Result<Unit> {
        return try {
            val response = api.linkGoogle(UpdateGoogleEmailDto(email, uid))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Google linking failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Terminate Account
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = api.deleteAccount()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Delete failed"))
        } catch (e: Exception) { Result.failure(e) }
    }
}