package com.example.flexifitapp.workout

import android.util.Log
import com.example.flexifitapp.ApiService
import com.example.flexifitapp.CalendarHistoryDto

class WorkoutRepository(private val apiService: ApiService) {

    private val TAG = "DEBUG_REPO"

    // 1. Para sa normal na pagbukas ng app (Today's Plan)
    suspend fun getTodayWorkout(): WorkoutSessionResponse? {
        return try {
            Log.d(TAG, "Fetching Today's Workout...")
            val response = apiService.getTodayWorkout()
            if (response.isSuccessful) {
                Log.d(TAG, "Today's Workout Success: ${response.body()?.program?.programName}")
                response.body()
            } else {
                Log.e(TAG, "Today's Workout Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Today's Workout Exception: ${e.message}")
            null
        }
    }

    // 2. Para sa pag-click sa Calendar (Specific History)
    suspend fun getWorkoutByDate(day: Int, month: Int): WorkoutSessionResponse? {
        return try {
            Log.d(TAG, "Fetching History Detail for Day: $day, Month: $month")
            val response = apiService.getWorkoutHistoryDetail(day, month)
            if (response.isSuccessful) {
                Log.d(TAG, "History Detail Success: ${response.body()?.dayType}")
                response.body()
            } else {
                Log.e(TAG, "History Detail Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "History Detail Exception: ${e.message}")
            null
        }
    }

    // 3. DAGDAG NATIN: Para makuha yung mga dots (Status Map)
    suspend fun getCalendarMap(month: Int, year: Int, type: String): List<CalendarHistoryDto>? {
        return try {
            Log.d(TAG, "Fetching Calendar Map: Month=$month, Year=$year, Type=$type")
            val response = apiService.getCalendarHistory(month, year, type)
            if (response.isSuccessful) {
                Log.d(TAG, "Calendar Map Success: Found ${response.body()?.size} records")
                response.body()
            } else {
                Log.e(TAG, "Calendar Map Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Calendar Map Exception: ${e.message}")
            null
        }
    }
}