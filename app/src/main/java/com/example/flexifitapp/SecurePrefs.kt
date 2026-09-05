package com.example.flexifitapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {

    private const val PREFS_NAME = "secure_prefs"
    private lateinit var prefs: SharedPreferences
    private var isInitialized = false
    private var isSecure = false

    // ✅ Called automatically when first used
    private fun init(context: Context) {
        if (isInitialized) return

        try {
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
            isSecure = true
            isInitialized = true
            Log.d("SecurePrefs", "✅ SecurePrefs initialized (encrypted)")
        } catch (e: Exception) {
            Log.e("SecurePrefs", "❌ Encryption failed: ${e.message}", e)
            // ✅ FALLBACK: Use plain SharedPreferences for non-sensitive data only
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            isSecure = false
            isInitialized = true
            Log.w("SecurePrefs", "⚠️ Fallback to plain SharedPreferences (non-sensitive only)")
        }
    }

    // ✅ Auto-initialize when first used
    private fun ensureInit() {
        if (isInitialized) return
        val context = FlexiFitApp.getAppContext()
            ?: throw IllegalStateException("Application context not available")
        init(context)
    }

    // ✅ Check if secure storage is available
    fun isSecureStorageAvailable(): Boolean {
        ensureInit()
        return isSecure
    }

    // ⚠️ SENSITIVE METHODS – Only work if secure
    fun putString(key: String, value: String) {
        ensureInit()
        if (!isSecure) {
            Log.e("SecurePrefs", "❌ Cannot save sensitive data: $key (insecure device)")
            return // Silently ignore – better than crashing
        }
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, def: String = ""): String {
        ensureInit()
        if (!isSecure) {
            Log.e("SecurePrefs", "❌ Cannot read sensitive data: $key (insecure device)")
            return def
        }
        return prefs.getString(key, def) ?: def
    }

    fun putInt(key: String, value: Int) {
        ensureInit()
        if (!isSecure) return
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, def: Int = 0): Int {
        ensureInit()
        if (!isSecure) return def
        return prefs.getInt(key, def)
    }

    fun putBoolean(key: String, value: Boolean) {
        ensureInit()
        if (!isSecure) return
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, def: Boolean = false): Boolean {
        ensureInit()
        if (!isSecure) return def
        return prefs.getBoolean(key, def)
    }

    fun putStringSet(key: String, value: Set<String>) {
        ensureInit()
        if (!isSecure) return
        prefs.edit().putStringSet(key, value).apply()
    }

    fun getStringSet(key: String): Set<String> {
        ensureInit()
        if (!isSecure) return emptySet()
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun remove(key: String) {
        ensureInit()
        if (!isSecure) return
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        ensureInit()
        if (!isSecure) return
        prefs.edit().clear().apply()
    }

    // ✅ NON-SENSITIVE METHODS – Always work (e.g., theme, read mode)
    fun putStringNonSensitive(key: String, value: String) {
        ensureInit()
        prefs.edit().putString(key, value).apply()
    }

    fun getStringNonSensitive(key: String, def: String = ""): String {
        ensureInit()
        return prefs.getString(key, def) ?: def
    }
}