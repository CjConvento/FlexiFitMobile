package com.example.flexifitapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val notificationService = NotificationService(applicationContext)
        notificationService.showNotification(
            "💧 Drink Water!",
            "Stay hydrated! Time to drink a glass of water.",
            NotificationService.NOTIFICATION_ID_WATER
        )
        return Result.success()
    }
}