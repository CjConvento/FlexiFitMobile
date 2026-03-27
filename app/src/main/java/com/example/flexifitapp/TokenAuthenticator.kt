package com.example.flexifitapp

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(private val context: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenAuthenticator", "authenticate called with code ${response.code}")
        if (response.code != 401) return null

        Log.d("TokenAuthenticator", "Token expired, attempting refresh")
        val newToken = getNewTokenBlocking() ?: run {
            // Refresh failed – clear local data and force logout
            Log.e("TokenAuthenticator", "Refresh failed, clearing auth")
            UserPrefs.clearAuth(context)
            // Also sign out Firebase to clean up
            FirebaseAuth.getInstance().signOut()
            // Notify the user (optional) – we can start LoginActivity
            // but we are on a background thread, so we need to post to main.
            // Instead, we return null and let the request fail; the app will then show the login screen.
            null
        }

        if (newToken != null) {
            UserPrefs.putString(context, UserPrefs.KEY_JWT_TOKEN, newToken)
            Log.d("TokenAuthenticator", "New token saved, retrying request")
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
        return null
    }

    private fun getNewTokenBlocking(): String? = runBlocking {
        try {
            val firebaseToken = UserPrefs.getString(context, UserPrefs.KEY_FIREBASE_TOKEN, "")

            // 🔽 Add logging here
            Log.d("TokenAuthenticator", "Stored Firebase token: ${firebaseToken.take(20)}")

            if (firebaseToken.isBlank()) {
                Log.e("TokenAuthenticator", "No stored Firebase token, cannot refresh")
                return@runBlocking null
            }

            // Create a separate client to avoid recursion
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            val api = retrofit.create(ApiService::class.java)

            val request = RefreshTokenRequest(firebaseToken)
            val response = api.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("TokenAuthenticator", "Refresh succeeded, new token: ${response.body()!!.token.take(20)}")
                response.body()!!.token
            } else {
                Log.e("TokenAuthenticator", "Refresh failed with code ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Refresh exception", e)
            null
        }
    }
}