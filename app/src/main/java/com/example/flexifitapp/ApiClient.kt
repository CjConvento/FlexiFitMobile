package com.example.flexifitapp

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private lateinit var appContext: Context
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: build().also { retrofit = it }
        }
    }

    fun profileApi(): ProfileApi = get().create(ProfileApi::class.java)
    fun api(): ApiService = get().create(ApiService::class.java)

    private fun build(): Retrofit {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Authorization interceptor – adds the JWT token to every request
        // Inside ApiClient.build() – authorization interceptor
        clientBuilder.addInterceptor { chain ->
            val original = chain.request()
            val token = UserPrefs.getToken(appContext)
            Log.d("ApiClient", "Token length: ${token.length}")
            val req = if (token.isNotBlank()) {
                val newRequest = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                Log.d("ApiClient", "Authorization header added")
                newRequest
            } else {
                Log.e("ApiClient", "Token is empty – request will be unauthorized")
                original
            }
            chain.proceed(req)
        }


        // Logging interceptor (optional, for debugging)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        clientBuilder.addInterceptor(logging)

        // Add the authenticator to handle 401 responses
        clientBuilder.authenticator(TokenAuthenticator(appContext))

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}