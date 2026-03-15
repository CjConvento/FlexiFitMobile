package com.example.flexifitapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Ginawa nating AndroidViewModel para makakuha ng context para sa ApiClient
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                // Gamitin ang existing ApiClient mo babe
                val context = getApplication<Application>().applicationContext
                val result = ApiClient.api(context).getNotifications()
                _notifications.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}