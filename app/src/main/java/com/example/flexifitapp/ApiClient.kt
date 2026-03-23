    package com.example.flexifitapp

    import android.content.Context
    import android.util.Log
    import kotlinx.coroutines.runBlocking
    import okhttp3.Authenticator
    import okhttp3.OkHttpClient
    import okhttp3.logging.HttpLoggingInterceptor
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import java.util.concurrent.TimeUnit

    object ApiClient {

        private lateinit var appContext: Context
        private var retrofit: Retrofit? = null

        // Call this once in your Application class
        fun init(context: Context) {
            appContext = context.applicationContext
        }

        // No context parameter needed – uses appContext from init
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

            // Authorization interceptor uses appContext
            clientBuilder.addInterceptor { chain ->
                val original = chain.request()
                val token = UserPrefs.getToken(appContext)
                Log.d("ApiClient", "Token being added: ${token.take(20)}...")
                val req = if (token.isNotBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(req)
            }



            // ✅ Logging interceptor – logs the final request (including headers)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS   // set to HEADERS to see auth header
            }
            clientBuilder.addInterceptor(logging)

            // ✅ Authenticator for token refresh
            clientBuilder.authenticator(Authenticator { route, response ->
                if (response.code == 401) {
                    // Do not retry the login endpoint itself
                    val requestPath = response.request.url.encodedPath
                    if (requestPath.contains("/api/auth/login")) {
                        return@Authenticator null
                    }

                    // Attempt to refresh the token (synchronously, using runBlocking)
                    val success = runBlocking {
                        TokenManager.refreshTokenIfNeeded(appContext)
                    }
                    if (success) {
                        val newToken = TokenManager.getToken()
                        if (!newToken.isNullOrBlank()) {
                            // Retry the original request with the new token
                            return@Authenticator response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                        }
                    }
                }
                null
            })

            val client = clientBuilder.build()


            return Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
    }