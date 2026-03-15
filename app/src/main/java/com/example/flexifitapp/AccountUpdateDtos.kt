package com.example.flexifitapp

// Para sa manual email update
data class UpdateEmailDto(
    val newEmail: String
)

// Para sa Google Account re-linking
data class UpdateGoogleEmailDto(
    val newEmail: String,
    val newFirebaseUid: String
)