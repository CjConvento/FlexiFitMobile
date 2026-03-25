import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.ApiService
import com.example.flexifitapp.LoginActivity
import com.example.flexifitapp.RefreshTokenRequest
import com.example.flexifitapp.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CountDownLatch

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val token = UserPrefs.getToken(context)

        // Attach token if present
        if (token.isNotEmpty()) {
            request = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(request)

        // If 401 and we have a token, try to refresh
        if (response.code == 401 && token.isNotEmpty()) {
            response.close()
            return refreshTokenAndRetry(chain, request)
        }

        return response
    }

    private fun refreshTokenAndRetry(chain: Interceptor.Chain, originalRequest: okhttp3.Request): Response {
        val newToken = refreshTokenSynchronously()
        return if (newToken != null) {
            // Retry original request with new token
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            chain.proceed(newRequest)
        } else {
            // Refresh failed – clear auth and redirect to login
            UserPrefs.clearAuth(context)
            // Redirect to LoginActivity on main thread
            Handler(Looper.getMainLooper()).post {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
            // Return a dummy response or throw exception
            originalRequest.newBuilder()
                .header("Authorization", "Bearer invalid")
                .build()
                .let { chain.proceed(it) }
        }
    }

    private fun refreshTokenSynchronously(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return null

        // Force refresh Firebase token
        var firebaseToken: String? = null
        val latch = CountDownLatch(1)
        firebaseUser.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseToken = task.result?.token
                }
                latch.countDown()
            }
        latch.await() // Wait for Firebase to finish

        // ✅ Assign to a local val to avoid smart cast error
        val token = firebaseToken
        if (token.isNullOrBlank()) return null

        // Call refresh endpoint to get new backend JWT
        return runBlocking {
            try {
                val api = ApiClient.get().create(ApiService::class.java)
                val refreshRequest = RefreshTokenRequest(token)
                val response = api.refreshToken(refreshRequest)
                if (response.isSuccessful) {
                    val auth = response.body()
                    if (auth != null) {
                        // Save new auth data
                        UserPrefs.saveAuth(
                            ctx = context,
                            token = auth.token,
                            userId = auth.userId,
                            role = auth.role,
                            status = auth.status,
                            isVerified = auth.isVerified,
                            name = auth.name ?: "",
                            photoUrl = auth.photoUrl ?: ""
                        )
                        auth.token
                    } else null
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}