package com.example.flexifitapp.utils

import com.example.flexifitapp.ApiConfig

object ImageHelper {

    /**
     * Resolves an image URL for Glide.
     *
     * Backend (Approach A) returns full Appwrite URLs like:
     *   https://cloud.appwrite.io/v1/storage/buckets/foods/files/abc/view?project=x
     *
     * If the value is null/blank, we fall back to a placeholder.
     * If the value is already a full URL, we pass it through.
     *
     * @param fileName  Full URL (from API) or null/blank
     * @param container One of "workouts", "foods", "avatars" — used only for placeholder selection
     */
    fun getImageUrl(fileName: String?, container: String): String {
        // 1. Null/blank → placeholder
        if (fileName.isNullOrBlank()) {
            return placeholderFor(container)
        }

        // 2. Full URL → pass through
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return fileName
        }

        // 3. Legacy relative path or unexpected value → wrap on BASE_URL (safety net)
        // This handles any old records still returning "foods/abc.png" style paths
        // while backend migration is in progress.
        return ApiConfig.BASE_URL.trimEnd('/') + "/" + fileName.trimStart('/')
    }

    private fun placeholderFor(container: String): String = when (container) {
        "workouts" -> ApiConfig.PLACEHOLDER_WORKOUT
        "foods" -> ApiConfig.PLACEHOLDER_FOOD
        "avatars" -> ApiConfig.PLACEHOLDER_AVATAR
        else -> ApiConfig.PLACEHOLDER_FOOD
    }
}