package com.example.flexifitapp

import android.content.Context
import com.example.flexifitapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    @Volatile private var retrofit: Retrofit? = null

    fun get(ctx: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: build(ctx).also { retrofit = it }
        }
    }

    fun profileApi(ctx: Context): ProfileApi =
        get(ctx).create(ProfileApi::class.java)

    private fun build(ctx: Context): Retrofit {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val clientBuilder = OkHttpClient.Builder()

        // add logging only in debug mode
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(logging)
        }

        clientBuilder.addInterceptor { chain ->
            val original = chain.request()
            val token = TokenStore.getIdToken(ctx)

            val req = if (!token.isNullOrBlank()) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else original

            chain.proceed(req)
        }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

}