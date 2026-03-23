package com.example.flexifitapp

import android.content.Context
import com.example.flexifitapp.auth.LoginRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object TokenManager {
    private var currentToken: String? = null
    private val refreshMutex = Mutex()
    private var isRefreshing = false

    fun getToken(): String? = currentToken

    fun setToken(token: String) {
        currentToken = token
    }

    fun clearToken() {
        currentToken = null
    }

    suspend fun refreshTokenIfNeeded(context: Context): Boolean {
        return refreshMutex.withLock {
            if (isRefreshing) {
                // Already refreshing; wait a moment and then check again (or return false)
                // For simplicity, return false and let the caller retry later
                return@withLock false
            }
            isRefreshing = true
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) return@withLock false

                // Force refresh the Firebase token
                val firebaseToken = suspendCancellableCoroutine { cont ->
                    firebaseUser.getIdToken(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                cont.resume(task.result?.token ?: "")
                            } else {
                                cont.resume("")
                            }
                        }
                }
                if (firebaseToken.isEmpty()) return@withLock false

                val api = ApiClient.api()
                val loginRequest = LoginRequest(firebaseIdToken = firebaseToken, fcmToken = null)
                val response = api.login(loginRequest)
                if (response.isSuccessful && response.body() != null) {
                    val newToken = response.body()!!.token
                    currentToken = newToken
                    // Also persist to SharedPreferences
                    UserPrefs.putString(context, UserPrefs.KEY_JWT_TOKEN, newToken)
                    return@withLock true
                }
                return@withLock false
            } finally {
                isRefreshing = false
            }
        }
    }
}