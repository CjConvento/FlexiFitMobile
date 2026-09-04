package com.example.flexifitapp.utils

import com.example.flexifitapp.ApiConfig

object ImageHelper {
    fun getImageUrl(fileName: String?, container: String): String {
        return if (fileName.isNullOrEmpty()) {
            // Placeholder
            when (container) {
                "workouts" -> ApiConfig.PLACEHOLDER_WORKOUT
                "foods" -> ApiConfig.PLACEHOLDER_FOOD
                "avatars" -> ApiConfig.PLACEHOLDER_AVATAR
                else -> ApiConfig.PLACEHOLDER_FOOD
            }
        } else {
            // Actual image from Azure Blob
            when (container) {
                "workouts" -> "${ApiConfig.WORKOUT_IMAGE_URL}${fileName}"
                "foods" -> "${ApiConfig.FOOD_IMAGE_URL}${fileName}"
                "avatars" -> "${ApiConfig.AVATAR_IMAGE_URL}${fileName}"
                else -> "${ApiConfig.FOOD_IMAGE_URL}${fileName}"
            }
        }
    }
}