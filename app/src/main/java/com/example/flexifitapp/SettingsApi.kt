package com.example.flexifitapp

import android.content.Context

object SettingsApi {
    fun create(context: Context): SettingsApiService {
        return ApiClient.get(context).create(SettingsApiService::class.java)
    }
}