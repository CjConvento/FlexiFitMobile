package com.example.flexifitapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SettingsApiService {

    @GET("api/settings/notifications")
    suspend fun getNotificationSettings(): NotificationSettingsDto

    @PUT("api/settings/notifications")
    suspend fun updateNotificationSettings(
        @Body body: NotificationSettingsDto
    ): Response<Unit>
}