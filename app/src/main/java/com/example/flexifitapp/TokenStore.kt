package com.example.flexifitapp

import android.content.Context
import com.example.flexifitapp.utils.AppLogger

object TokenStore {
    private const val KEY_ID_TOKEN = "firebase_id_token"

    fun saveIdToken(ctx: Context, token: String) {
        SecurePrefs.putString(KEY_ID_TOKEN, token)
    }

    fun getIdToken(ctx: Context): String? {
        val token = SecurePrefs.getString(KEY_ID_TOKEN, "")
        return if (token.isNotEmpty()) token else null
    }

    fun clear(ctx: Context) {
        SecurePrefs.remove(KEY_ID_TOKEN)
    }
}