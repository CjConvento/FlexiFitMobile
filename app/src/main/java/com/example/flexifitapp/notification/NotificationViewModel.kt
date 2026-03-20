package com.example.flexifitapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.flexifitapp.notification.NotificationItem
import com.example.flexifitapp.notification.NotificationType
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val response = ApiClient.api(context).getNotifications()

                if (response.isSuccessful) {
                    val notifications = response.body()?.map { dto ->
                        NotificationItem(
                            id = dto.id,
                            title = dto.title,
                            message = dto.message,
                            time = dto.time,
                            type = when (dto.type.uppercase()) {
                                "WORKOUT" -> NotificationType.WORKOUT
                                "MEAL" -> NotificationType.MEAL
                                "WATER" -> NotificationType.WATER
                                "ACHIEVEMENT" -> NotificationType.ACHIEVEMENT
                                else -> NotificationType.ACHIEVEMENT
                            }
                        )
                    } ?: emptyList()
                    _notifications.value = notifications
                } else {
                    loadDummyNotifications()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDummyNotifications()
            }
        }
    }

    private fun loadDummyNotifications() {
        _notifications.value = listOf(
            NotificationItem(
                id = 1,
                title = "🎉 Welcome to FlexiFit!",
                message = "Start your fitness journey today!",
                time = "Just now",
                type = NotificationType.ACHIEVEMENT
            ),
            NotificationItem(
                id = 2,
                title = "🏋️ Workout Time!",
                message = "Don't forget your workout today!",
                time = "10:30 AM",
                type = NotificationType.WORKOUT
            ),
            NotificationItem(
                id = 3,
                title = "🍽️ Meal Time",
                message = "Time to log your breakfast",
                time = "8:00 AM",
                type = NotificationType.MEAL
            ),
            NotificationItem(
                id = 4,
                title = "💧 Stay Hydrated",
                message = "Drink a glass of water",
                time = "2:00 PM",
                type = NotificationType.WATER
            )
        )
    }
}