package com.example.flexifitapp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MealReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("ReminderWorker", "Meal reminder executed")
        try {
            val notificationService = NotificationService(applicationContext)
            notificationService.showNotification(
                "🍽️ Meal Time!",
                "Don't forget to log your meal and stay on track with your nutrition goals.",
                NotificationService.NOTIFICATION_ID_MEAL
            )
            return Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Meal reminder failed", e)
            return Result.failure()
        }
    }
}