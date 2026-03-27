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
            WorkManager.getInstance(context).cancelUniqueWork("water_reminder")
            return
        }

        try {
            val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
            val (endHour, endMinute) = endTime.split(":").map { it.toInt() }

            val now = LocalTime.now()
            val start = LocalTime.of(startHour, startMinute)
            val end = LocalTime.of(endHour, endMinute)

            if (now.isAfter(end)) {
                val delay = calculateDelay(startHour, startMinute)
                scheduleNextWaterReminder(startHour, startMinute, intervalMinutes, delay)
            } else if (now.isBefore(start)) {
                val delay = calculateDelay(startHour, startMinute)
                scheduleNextWaterReminder(startHour, startMinute, intervalMinutes, delay)
            } else {
                val minutesSinceStart = (now.hour - startHour) * 60 + (now.minute - startMinute)
                val nextInterval = ((minutesSinceStart / intervalMinutes) + 1) * intervalMinutes
                val nextTime = start.plusMinutes(nextInterval.toLong())

                if (nextTime.isBefore(end)) {
                    val delay = (nextTime.hour - now.hour) * 3600000L +
                            (nextTime.minute - now.minute) * 60000L
                    scheduleNextWaterReminder(nextTime.hour, nextTime.minute, intervalMinutes, delay)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Water reminder scheduling failed", e)
        }
    }

    private fun scheduleNextWaterReminder(hour: Int, minute: Int, intervalMinutes: Int, delay: Long) {
        try {
            val workRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "water_reminder_${hour}_$minute",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            val nextHour = (hour + (intervalMinutes / 60)) % 24
            val nextMinute = minute + (intervalMinutes % 60)
            val nextDelay = intervalMinutes * 60 * 1000L

            val nextWorkRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
                .setInitialDelay(nextDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "water_reminder_${nextHour}_$nextMinute",
                ExistingWorkPolicy.REPLACE,
                nextWorkRequest
            )
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "scheduleNextWaterReminder failed", e)
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
        return calendar.timeInMillis - System.currentTimeMillis()
    }
}