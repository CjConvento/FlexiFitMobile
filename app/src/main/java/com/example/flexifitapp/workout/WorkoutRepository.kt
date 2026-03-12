package com.example.flexifitapp.workout

import com.example.flexifitapp.ApiService

class WorkoutRepository(
    private val apiService: ApiService
) {
    /**
     * Kukunin ang workout session para sa kasalukuyang araw.
     * Ang backend ang nagbubuo ng full Media URLs (IP + Filename).
     */
    suspend fun getTodayWorkout(): WorkoutSessionResponse? {
        return try {
            val response = apiService.getTodayWorkout()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}