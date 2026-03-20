package com.example.flexifitapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WorkoutReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            val notificationService = NotificationService(applicationContext)
            notificationService.showNotification(
                "🏋️ Workout Time!",
                "Time to crush your workout! Don't skip today's session.",
                NotificationService.NOTIFICATION_ID_WORKOUT
            )
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}