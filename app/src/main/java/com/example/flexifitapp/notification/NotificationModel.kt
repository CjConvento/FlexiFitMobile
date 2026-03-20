package com.example.flexifitapp.notification

enum class NotificationType {
    WORKOUT, MEAL, WATER, ACHIEVEMENT
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType
)