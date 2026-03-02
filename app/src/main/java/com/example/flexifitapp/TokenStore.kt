package com.example.flexifitapp

import android.content.Context

object TokenStore {
    private const val PREF = "flexifit_tokens"
    private const val KEY_ID_TOKEN = "firebase_id_token"

    fun saveIdToken(ctx: Context, token: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ID_TOKEN, token)
            .apply()
    }

    fun getIdToken(ctx: Context): String? {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ID_TOKEN, null)
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }
}