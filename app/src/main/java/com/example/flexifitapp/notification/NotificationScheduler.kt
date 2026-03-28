package com.example.flexifitapp

import android.content.Context
import android.util.Log
import androidx.work.*
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleWorkoutReminder(time: String, enabled: Boolean) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork("workout_reminder")
            return
        }

        try {
            val parts = time.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val delay = calculateDelay(hour, minute)
                Log.d("NotificationScheduler", "Workout delay: $delay ms")
                val workRequest = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "workout_reminder",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Workout reminder scheduling failed", e)
        }
    }

    fun scheduleMealReminder(time: String, enabled: Boolean) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork("meal_reminder")
            return
        }

        try {
            val parts = time.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val delay = calculateDelay(hour, minute)
                Log.d("NotificationScheduler", "Meal delay: $delay ms")
                val workRequest = OneTimeWorkRequestBuilder<MealReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "meal_reminder",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Meal reminder scheduling failed", e)
        }
    }

    fun scheduleWaterReminders(
        enabled: Boolean,
        startTime: String,
        endTime: String,
        intervalMinutes: Int
    ) {
        if (!enabled) {
            // Cancel any existing water reminder chains
            WorkManager.getInstance(context).cancelUniqueWork("water_reminder")
            WorkManager.getInstance(context).cancelUniqueWork("water_reminder_first")
            return
        }

        try {
            val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
            val (endHour, endMinute) = endTime.split(":").map { it.toInt() }

            // Cancel any existing water reminders to avoid overlapping chains
            WorkManager.getInstance(context).cancelUniqueWork("water_reminder")
            WorkManager.getInstance(context).cancelUniqueWork("water_reminder_first")

            // Build input data for the first reminder
            val inputData = Data.Builder()
                .putInt("interval_minutes", intervalMinutes)
                .putInt("end_hour", endHour)
                .putInt("end_minute", endMinute)
                .build()

            // Calculate delay for the first reminder
            val now = LocalTime.now()
            val target = LocalTime.of(startHour, startMinute)
            val end = LocalTime.of(endHour, endMinute)

            val delay = if (target.isAfter(end)) {
                // Start time is after end time (e.g., 22:00 to 06:00?) – treat as tomorrow
                calculateDelay(startHour, startMinute)
            } else if (target.isBefore(now)) {
                // Already past today's start time – schedule for tomorrow
                calculateDelay(startHour, startMinute)
            } else {
                calculateDelay(startHour, startMinute)
            }

            if (delay > 0) {
                val workRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "water_reminder_first",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
                Log.d("NotificationScheduler", "First water reminder scheduled with delay $delay ms")
            } else {
                Log.e("NotificationScheduler", "Water reminder delay <= 0, no work scheduled")
            }
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Water reminder scheduling failed", e)
        }
    }

    private fun calculateDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        Log.d("NotificationScheduler", "Calculated delay for $hour:$minute = $delay ms")
        return delay
    }
}