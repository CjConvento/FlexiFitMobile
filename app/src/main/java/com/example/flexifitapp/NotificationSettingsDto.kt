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

// Siguraduhin na ganito ang structure para sa Glass at Calories
    val dailyWaterGoal: Int?, // Halimbawa: 2000 (ml)
    val glassSizeMl: Int?,    // Ito yung binago natin, halimbawa: 250 (ml)
    val calorieDisplayMode: String? // "Remaining" vs "Consumed"
)