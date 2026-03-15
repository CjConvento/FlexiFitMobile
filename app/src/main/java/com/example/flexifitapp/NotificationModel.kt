package com.example.flexifitapp

// Para sa Unresolved reference 'NotificationType'
enum class NotificationType {
    WATER, MEAL, WORKOUT, ACHIEVEMENT
}

// Para sa Unresolved reference 'NotificationItem'
data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType
)