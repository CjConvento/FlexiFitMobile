package com.example.flexifitapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var imgAvatar: ImageView? = null

    private val pickAvatar =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult

            // preview agad
            imgAvatar?.let { imageView ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(imageView)
            }

            // upload agad to ASP.NET
            uploadAvatar(uri)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_profileff, container, false)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        imgAvatar?.setOnClickListener {
            pickAvatar.launch("image/*")
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imgAvatar = null
    }

    private fun uploadAvatar(uri: Uri) {
        val file = uriToFile(uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val userId = 1 // TODO: replace with logged-in user id

        lifecycleScope.launch {
            try {
                val api = ApiClient.profileApi(requireContext())
                val response = api.uploadAvatar(body, userId)

                if (response.isSuccessful) {
                    val avatarUrl = response.body()?.url

                    if (!avatarUrl.isNullOrBlank()) {
                        // load from server (persisted)
                        Glide.with(this@ProfileFragment)
                            .load(ApiConfig.BASE_URL.trimEnd('/') + "/" + avatarUrl.trimStart('/'))
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .circleCrop()
                            .into(imgAvatar!!)
                    }
                } else {
                    // optional: revert to placeholder or keep preview
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "avatar_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}


// TODO: Next steps (workflow and rule based logic)
// 1. Kunin user profile data (age, gender, height, weight, body composition goal, fitness level, selected diet type, selected programs)
// 2. I-filter ang recommended programs depende sa fitness goal, body composition goal, workout location and fitness level)
// 3. Palitan ang "Day's Workout" content based on selected program
