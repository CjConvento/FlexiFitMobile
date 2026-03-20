package com.example.flexifitapp.workout

import android.util.Log
import com.example.flexifitapp.ApiService
import com.example.flexifitapp.CalendarHistoryDto

class WorkoutRepository(private val apiService: ApiService) {

    private val TAG = "WORKOUT_REPO"

    // 1. Get today's workout
    suspend fun getTodayWorkout(): WorkoutSessionResponse? {
        return try {
            Log.d(TAG, "Fetching Today's Workout...")
            val response = apiService.getTodayWorkout()
            if (response.isSuccessful) {
                Log.d(TAG, "Today's Workout Success")
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

    // 2. Get workout by date (for calendar)
    suspend fun getWorkoutByDate(day: Int, month: Int): WorkoutSessionResponse? {
        return try {
            Log.d(TAG, "Fetching Workout for Day: $day, Month: $month")
            val response = apiService.getWorkoutHistoryDetail(day, month)
            if (response.isSuccessful) {
                Log.d(TAG, "Workout Detail Success")
                response.body()
            } else {
                Log.e(TAG, "Workout Detail Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Workout Detail Exception: ${e.message}")
            null
        }
    }

    // 3. ✅ COMPLETE OR SKIP WORKOUT
    suspend fun completeWorkout(
        sessionId: Int,
        totalCalories: Int,
        totalMinutes: Int,
        status: String,
        skipReason: String? = null
    ): WorkoutSessionResultDto? {
        return try {
            Log.d(TAG, "Completing/Skipping Session: $sessionId, Status: $status")
            val request = WorkoutSessionCompleteDto(
                sessionId = sessionId,
                totalCalories = totalCalories,
                totalMinutes = totalMinutes,
                status = status,
                skipReason = skipReason
            )
            val response = apiService.completeWorkout(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Session completed/skipped successfully")
                response.body()
            } else {
                Log.e(TAG, "Complete/Skip Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Complete/Skip Exception: ${e.message}")
            null
        }
    }

    // 4. ✅ CHECK IF USER CAN SKIP TODAY
    suspend fun canSkipToday(): CanSkipResponse? {
        return try {
            Log.d(TAG, "Checking if can skip today...")
            val response = apiService.canSkipToday()
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "CanSkip Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "CanSkip Exception: ${e.message}")
            null
        }
    }

    // 5. Get calendar map
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