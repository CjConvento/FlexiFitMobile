package com.example.flexifitapp

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    @Volatile private var retrofit: Retrofit? = null

    fun get(ctx: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: build(ctx).also { retrofit = it }
        }
    }

    private fun build(ctx: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val token = TokenStore.getIdToken(ctx)

                val req = if (!token.isNullOrBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else original

                chain.proceed(req)
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

}