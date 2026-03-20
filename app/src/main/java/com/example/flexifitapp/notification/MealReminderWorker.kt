package com.example.flexifitapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class MealReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val notificationService = NotificationService(applicationContext)
        notificationService.showNotification(
            "🍽️ Meal Time!",
            "Don't forget to log your meal and stay on track with your nutrition goals.",
            NotificationService.NOTIFICATION_ID_MEAL
        )
        return Result.success()
    }
}