package com.example.flexifitapp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.flexifitapp.utils.AppLogger

class WorkoutReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        AppLogger.d("ReminderWorker", "Workout reminder executed")
        try {
            val notificationService = NotificationService(applicationContext)
            notificationService.showNotification(
                "🏋️ Workout Time!",
                "Time to crush your workout! Don't skip today's session.",
                NotificationService.NOTIFICATION_ID_WORKOUT
            )
            return Result.success()
        } catch (e: Exception) {
            AppLogger.e("ReminderWorker", "Workout reminder failed", e)
            return Result.failure()
        }
    }
}