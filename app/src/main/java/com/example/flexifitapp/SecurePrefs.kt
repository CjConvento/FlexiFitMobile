package com.example.flexifitapp

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {

    private const val PREFS_NAME = "secure_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    // Generic methods
    fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun getString(key: String, def: String = ""): String = prefs.getString(key, def) ?: def
    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun getInt(key: String, def: Int = 0): Int = prefs.getInt(key, def)
    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, def: Boolean = false): Boolean = prefs.getBoolean(key, def)
    fun putStringSet(key: String, value: Set<String>) = prefs.edit().putStringSet(key, value).apply()
    fun getStringSet(key: String): Set<String> = prefs.getStringSet(key, emptySet()) ?: emptySet()
    fun remove(key: String) = prefs.edit().remove(key).apply()
    fun clear() = prefs.edit().clear().apply()
}