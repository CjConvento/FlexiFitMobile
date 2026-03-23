package com.example.flexifitapp.notification

import com.google.gson.annotations.SerializedName

data class NotificationItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("time") val time: String,
    @SerializedName("type") val type: String
)