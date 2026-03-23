package com.example.flexifitapp

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SettingsApiService {

    // --- NOTIFICATIONS (Roadmap #1) ---
    @GET("api/settings/notifications")
    suspend fun getNotificationSettings(): NotificationSettingsDto

    @PUT("api/settings/notifications")
    suspend fun updateNotificationSettings(@Body dto: NotificationSettingsDto): Response<Unit>

    // --- ACCOUNT & DATA MANAGEMENT (Roadmap #2) ---
    @POST("api/settings/account/reset-progress")
    suspend fun resetProgress(): Response<Unit>

    @GET("api/settings/account/export")
    suspend fun exportData(): Response<ResponseBody>

    @PUT("api/settings/account/email")
    suspend fun updateEmail(@Body dto: UpdateEmailDto): Response<Unit>

    @PUT("api/settings/account/link-google")
    suspend fun linkGoogle(@Body dto: UpdateGoogleEmailDto): Response<Unit>

    @DELETE("api/settings/account/terminate")
    suspend fun deleteAccount(): Response<Unit>

    companion object {
        fun create(context: android.content.Context): SettingsApiService {
            // Gamitin ang .get(context) dahil yun ang nasa ApiClient.kt mo babe
            return ApiClient.get().create(SettingsApiService::class.java)
        }
    } // Isara ang companion object
} // Isara ang interface