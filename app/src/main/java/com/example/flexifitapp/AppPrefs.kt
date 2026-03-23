package com.example.flexifitapp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object AppPrefs {
    private const val PREFS_NAME = "flexifit_app_prefs"
    private const val KEY_NIGHT_MODE = "night_mode"
    private const val KEY_READ_MODE = "read_mode"

    fun setNightMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_NIGHT_MODE, mode).apply()
    }

    fun getNightMode(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun setReadMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_READ_MODE, enabled).apply()
    }

    fun isReadModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_READ_MODE, false)
    }
}