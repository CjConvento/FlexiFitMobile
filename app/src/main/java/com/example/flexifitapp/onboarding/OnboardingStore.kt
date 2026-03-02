package com.example.flexifitapp.onboarding

import android.content.Context

object OnboardingStore {
    private const val PREFS = "onboarding_prefs"

    fun putString(ctx: Context, key: String, value: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(key, value).apply()
    }

    fun getString(ctx: Context, key: String): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key, "")?.trim().orEmpty()

    fun putBool(ctx: Context, key: String, value: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(key, value).apply()
    }

    fun getBool(ctx: Context, key: String): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(key, false)

    // ✅ ADD THESE (for multi-select tiles)
    fun putStringSet(ctx: Context, key: String, value: Set<String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(key, value).apply()
    }

    fun getStringSet(ctx: Context, key: String): Set<String> =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(key, emptySet()) ?: emptySet()

    fun putInt(ctx: Context, key: String, value: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }

    fun getInt(ctx: Context, key: String, def: Int = 0): Int {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(key, def)
    }
}