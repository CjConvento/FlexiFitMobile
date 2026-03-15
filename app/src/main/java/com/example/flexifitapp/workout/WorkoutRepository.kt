package com.example.flexifitapp.workout

import com.example.flexifitapp.ApiService

// WorkoutRepository.kt
class WorkoutRepository(private val apiService: ApiService) {

    // Para sa normal na pagbukas ng app
    suspend fun getTodayWorkout(): WorkoutSessionResponse? {
        return try {
            val response = apiService.getTodayWorkout()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    // GUDIS UPDATE: Para sa pag-click sa Calendar
    suspend fun getWorkoutByDate(day: Int, month: Int): WorkoutSessionResponse? {
        return try {
            val response = apiService.getWorkoutHistoryDetail(day, month)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }
}