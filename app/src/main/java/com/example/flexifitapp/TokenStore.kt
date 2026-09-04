package com.example.flexifitapp

import android.content.Context

object TokenStore {
    private const val KEY_ID_TOKEN = "firebase_id_token"

    fun saveIdToken(ctx: Context, token: String) {
        SecurePrefs.putString(KEY_ID_TOKEN, token)
    }

    fun getIdToken(ctx: Context): String? {
        return SecurePrefs.getString(KEY_ID_TOKEN, null)
    }

    fun clear(ctx: Context) {
        SecurePrefs.remove(KEY_ID_TOKEN)
    }
}