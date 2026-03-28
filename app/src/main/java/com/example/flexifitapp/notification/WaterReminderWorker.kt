package com.example.flexifitapp

import android.content.Context
import android.util.Log
import androidx.work.*
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("ReminderWorker", "Water reminder executed")

        try {
            // Show notification
            val notificationService = NotificationService(applicationContext)
            notificationService.showNotification(
                "💧 Drink Water!",
                "Stay hydrated! Time to drink a glass of water.",
                NotificationService.NOTIFICATION_ID_WATER
            )

            // Get scheduling parameters
            val intervalMinutes = inputData.getInt("interval_minutes", 60)
            val endHour = inputData.getInt("end_hour", 20)
            val endMinute = inputData.getInt("end_minute", 0)

            // Calculate next reminder time
            val now = LocalTime.now()
            val nextTime = now.plusMinutes(intervalMinutes.toLong())
            val end = LocalTime.of(endHour, endMinute)

            if (nextTime.isBefore(end) || nextTime.equals(end)) {
                val delay = (nextTime.hour - now.hour) * 3600000L +
                        (nextTime.minute - now.minute) * 60000L

                if (delay > 0) {
                    val nextInputData = Data.Builder()
                        .putInt("interval_minutes", intervalMinutes)
                        .putInt("end_hour", endHour)
                        .putInt("end_minute", endMinute)
                        .build()

                    val nextWork = OneTimeWorkRequestBuilder<WaterReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(nextInputData)
                        .build()

                    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                        "water_reminder_${nextTime.hour}:${nextTime.minute}",
                        ExistingWorkPolicy.REPLACE,
                        nextWork
                    )
                    Log.d("ReminderWorker", "Next water reminder scheduled at $nextTime")
                }
            } else {
                Log.d("ReminderWorker", "End of water reminders for today")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Water reminder failed", e)
            return Result.failure()
        }
    }
}