package com.example.flexifitapp.workout

import com.example.flexifitapp.ApiService

class WorkoutRepository(
    private val apiService: ApiService
) {

    suspend fun getCurrentWorkoutSession(userId: Int): WorkoutSessionResponse? {
        val response = apiService.getCurrentWorkoutSession(userId)

        if (response.isSuccessful) {
            return response.body()
        }

        return null
    }
}