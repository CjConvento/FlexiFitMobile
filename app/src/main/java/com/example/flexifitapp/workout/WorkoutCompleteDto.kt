package com.example.flexifitapp.workout

import com.google.gson.annotations.SerializedName

data class WorkoutSessionCompleteDto(
    @SerializedName("sessionId")
    val sessionId: Int,

    @SerializedName("totalCalories")
    val totalCalories: Int,

    @SerializedName("totalMinutes")
    val totalMinutes: Int,

    @SerializedName("status")
    val status: String,  // "COMPLETED" or "SKIPPED"

    @SerializedName("skipReason")
    val skipReason: String? = null
)

data class WorkoutSessionResultDto(
    @SerializedName("message")
    val message: String,

    @SerializedName("currentDay")
    val currentDay: Int,

    @SerializedName("nextDay")
    val nextDay: Int,

    @SerializedName("isProgramFinished")
    val isProgramFinished: Boolean,

    @SerializedName("status")
    val status: String,

    @SerializedName("wasSkipped")
    val wasSkipped: Boolean,

    @SerializedName("skipMessage")
    val skipMessage: String?
)

data class CanSkipResponse(
    @SerializedName("canSkip")
    val canSkip: Boolean,

    @SerializedName("isRestDay")
    val isRestDay: Boolean,

    @SerializedName("alreadyProcessed")
    val alreadyProcessed: Boolean,

    @SerializedName("currentDay")
    val currentDay: Int,

    @SerializedName("message")
    val message: String
)

data class WorkoutHistoryDto(
    @SerializedName("workoutDay")
    val workoutDay: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("startedAt")
    val startedAt: String?
)