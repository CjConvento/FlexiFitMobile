package com.example.flexifitapp.workout

import android.util.Log
import com.example.flexifitapp.ApiService
import com.example.flexifitapp.CalendarHistoryDto

class WorkoutRepository(private val apiService: ApiService) {

    private val TAG = "WORKOUT_REPO"

    // 1. Get today's workout
    suspend fun getTodayWorkout(): WorkoutSessionResponse? {
        return try {
            val response = apiService.getTodayWorkout()
            if (response.isSuccessful) {
                AppLogger.d(TAG, "Today's Workout Success: ${response.body()}")
                response.body()
            } else {
                AppLogger.e(TAG, "Today's Workout Failed: Code ${response.code()}, errorBody: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Today's Workout Exception: ${e.message}")
            null
        }
    }

    // 2. Get workout by date (for calendar)
    suspend fun getWorkoutByDate(day: Int, month: Int): WorkoutSessionResponse? {
        return try {
            AppLogger.d(TAG, "Fetching Workout for Day: $day, Month: $month")
            val response = apiService.getWorkoutHistoryDetail(day, month)
            if (response.isSuccessful) {
                AppLogger.d(TAG, "Workout Detail Success")
                response.body()
            } else {
                AppLogger.e(TAG, "Workout Detail Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Workout Detail Exception: ${e.message}")
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
            AppLogger.d(TAG, "Completing/Skipping Session: $sessionId, Status: $status")
            val request = WorkoutSessionCompleteDto(
                sessionId = sessionId,
                totalCalories = totalCalories,
                totalMinutes = totalMinutes,
                status = status,
                skipReason = skipReason
            )
            val response = apiService.completeWorkout(request)
            if (response.isSuccessful) {
                AppLogger.d(TAG, "Session completed/skipped successfully")
                response.body()
            } else {
                AppLogger.e(TAG, "Complete/Skip Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Complete/Skip Exception: ${e.message}")
            null
        }
    }

    // 4. ✅ CHECK IF USER CAN SKIP TODAY
    suspend fun canSkipToday(): CanSkipResponse? {
        return try {
            AppLogger.d(TAG, "Checking if can skip today...")
            val response = apiService.canSkipToday()
            if (response.isSuccessful) {
                response.body()
            } else {
                AppLogger.e(TAG, "CanSkip Failed: Code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "CanSkip Exception: ${e.message}")
            null
        }
    }

    suspend fun getCalendarHistory(month: Int): List<CalendarHistoryDto>? {
        return try {
            val response = apiService.getCalendarHistoryWithMonth(month)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}