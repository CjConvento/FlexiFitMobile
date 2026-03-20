package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class NotificationSettingsDto(
    @SerializedName("workoutReminderEnabled")
    val workoutReminderEnabled: Boolean = true,

    @SerializedName("workoutReminderTime")
    val workoutReminderTime: String? = "08:00",

    @SerializedName("mealReminderEnabled")
    val mealReminderEnabled: Boolean = true,

    @SerializedName("mealReminderTime")
    val mealReminderTime: String? = "12:00",

    @SerializedName("waterReminderEnabled")
    val waterReminderEnabled: Boolean = true,

    @SerializedName("waterStartTime")
    val waterStartTime: String? = "08:00",

    @SerializedName("waterEndTime")
    val waterEndTime: String? = "20:00",

    @SerializedName("waterIntervalMinutes")
    val waterIntervalMinutes: Int? = 60,

    @SerializedName("dailyWaterGoal")
    val dailyWaterGoal: Int? = 2000,

    @SerializedName("glassSizeMl")
    val glassSizeMl: Int? = 250,

    @SerializedName("calorieDisplayMode")
    val calorieDisplayMode: String? = "Remaining"
)

data class UpdateNotificationSettingsRequest(
    @SerializedName("settings")
    val settings: NotificationSettingsDto
)