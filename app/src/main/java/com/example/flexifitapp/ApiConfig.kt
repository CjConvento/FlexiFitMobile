package com.example.flexifitapp

object ApiConfig {

    const val BASE_URL = "https://flexifitapinet.shares.zrok.io/"

    // Azure Blob Storage URLs
    const val WORKOUT_IMAGE_URL = "https://flexifitstorage.blob.core.windows.net/workouts/"
    const val FOOD_IMAGE_URL = "https://flexifitstorage.blob.core.windows.net/foods/"
    const val AVATAR_IMAGE_URL = "https://flexifitstorage.blob.core.windows.net/avatars/"

    // Placeholder Images (from API wwwroot)
    const val PLACEHOLDER_WORKOUT = "${BASE_URL}images/workouts/default.png"
    const val PLACEHOLDER_FOOD = "${BASE_URL}images/foods/default.png"
    const val PLACEHOLDER_AVATAR = "${BASE_URL}uploads/avatars/default.png"
}