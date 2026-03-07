package com.example.flexifitapp

data class NotificationSettingsDto(
    val workoutReminderEnabled: Boolean,
    val workoutReminderTime: String?,

    val mealReminderEnabled: Boolean,
    val mealReminderTime: String?,

    val waterReminderEnabled: Boolean,
    val waterStartTime: String?,
    val waterEndTime: String?,
    val waterIntervalMinutes: Int?,

    val dailyWaterGoal: Int?,
    val glassSizeMl: Int?,
    val calorieDisplayMode: String?
)