package com.example.flexifitapp

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.flexifitapp.profile.AchievementsDialogFragment
import com.example.flexifitapp.profile.PersonalDataDialogFragment
import com.example.flexifitapp.profile.WeightQuickEditDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(R.layout.fragment_profileff) {

    private var imgAvatar: ImageView? = null

    private var tvName: TextView? = null
    private var tvGoalSubtitle: TextView? = null
    private var tvAgeValue: TextView? = null
    private var tvHeightValue: TextView? = null
    private var tvWeightValue: TextView? = null

    private var btnEdit: MaterialButton? = null
    private var btnUpdateProfile: MaterialButton? = null

    private var rowPersonalData: LinearLayout? = null
    private var rowWorkoutData: LinearLayout? = null
    private var rowNutritionData: LinearLayout? = null
    private var rowAchievements: LinearLayout? = null
    private var rowTrackProgress: LinearLayout? = null

    private val pickAvatar =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult

            imgAvatar?.let { imageView ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(imageView)
            }

            uploadAvatar(uri)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupResultListeners()
        setupClicks()
        loadProfileData()
        loadSavedAvatar()
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
        loadSavedAvatar()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        imgAvatar = null

        tvName = null
        tvGoalSubtitle = null
        tvAgeValue = null
        tvHeightValue = null
        tvWeightValue = null

        btnEdit = null
        btnUpdateProfile = null

        rowPersonalData = null
        rowWorkoutData = null
        rowNutritionData = null
        rowAchievements = null
        rowTrackProgress = null
    }

    private fun bindViews(view: View) {
        imgAvatar = view.findViewById(R.id.imgAvatar)

        tvName = view.findViewById(R.id.tvName)
        tvGoalSubtitle = view.findViewById(R.id.tvGoalSubtitle)
        tvAgeValue = view.findViewById(R.id.tvAgeValue)
        tvHeightValue = view.findViewById(R.id.tvHeightValue)
        tvWeightValue = view.findViewById(R.id.tvWeightValue)

        btnEdit = view.findViewById(R.id.btnEdit)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)

        rowPersonalData = view.findViewById(R.id.rowPersonalData)
        rowWorkoutData = view.findViewById(R.id.rowWorkoutData)
        rowNutritionData = view.findViewById(R.id.rowNutritionData)
        rowAchievements = view.findViewById(R.id.rowAchievements)
        rowTrackProgress = view.findViewById(R.id.rowTrackProgress)
    }

    private fun setupResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            WeightQuickEditDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val newWeight = bundle.getInt(
                WeightQuickEditDialogFragment.BUNDLE_NEW_WEIGHT,
                0
            )

            if (newWeight > 0) {
                tvWeightValue?.text = "$newWeight kg"
            } else {
                loadProfileData()
            }
        }
    }

    private fun setupClicks() {
        imgAvatar?.setOnClickListener {
            pickAvatar.launch("image/*")
        }

        btnEdit?.setOnClickListener {
            WeightQuickEditDialogFragment()
                .show(parentFragmentManager, "WeightQuickEditDialog")
        }

        btnUpdateProfile?.setOnClickListener {
            toast("Update Profile tapped")
        }

        rowPersonalData?.setOnClickListener {
            PersonalDataDialogFragment()
                .show(parentFragmentManager, "PersonalDataDialog")
        }

        rowWorkoutData?.setOnClickListener {
            toast("Workout Data dialog next")
        }

        rowNutritionData?.setOnClickListener {
            toast("Nutritional Data dialog next")
        }

        rowAchievements?.setOnClickListener {
            try {
                AchievementsDialogFragment()
                    .show(parentFragmentManager, "AchievementsDialog")
            } catch (e: Exception) {
                toast("Achievements dialog not wired yet")
            }
        }

        rowTrackProgress?.setOnClickListener {
            try {
                findNavController().navigate(R.id.nav_progtr)
            } catch (e: Exception) {
                toast("Progress Tracking screen not wired yet")
            }
        }
    }

    private fun loadProfileData() {
        val ctx = requireContext()

        val name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, "")
        val username = UserPrefs.getString(ctx, UserPrefs.KEY_USER_NAME, "")

        val displayName = when {
            name.isNotBlank() -> name
            username.isNotBlank() -> username
            else -> "User Name"
        }

        val age = UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0)
        val heightCm = UserPrefs.getInt(ctx, UserPrefs.KEY_HEIGHT_CM, 0)
        val weightKg = UserPrefs.getInt(ctx, UserPrefs.KEY_WEIGHT_KG, 0)

        val bodyGoal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "")
        val fitnessGoals = UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET)

        val subtitle = when {
            bodyGoal.isNotBlank() -> prettifyValue(bodyGoal)
            fitnessGoals.isNotEmpty() -> fitnessGoals.joinToString(", ") { prettifyValue(it) }
            else -> "User Goal"
        }

        tvName?.text = displayName
        tvGoalSubtitle?.text = subtitle
        tvAgeValue?.text = if (age > 0) "$age yo" else "0 yo"
        tvHeightValue?.text = if (heightCm > 0) "$heightCm cm" else "0 cm"
        tvWeightValue?.text = if (weightKg > 0) "$weightKg kg" else "0 kg"
    }

    private fun loadSavedAvatar() {
        val ctx = requireContext()
        val localAvatar = UserPrefs.getString(ctx, "avatar_url", "")

        val avatarView = imgAvatar ?: return

        if (!localAvatar.isNullOrBlank()) {
            Glide.with(this)
                .load(
                    if (localAvatar.startsWith("http", true)) {
                        localAvatar
                    } else {
                        ApiConfig.BASE_URL.trimEnd('/') + "/" + localAvatar.trimStart('/')
                    }
                )
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(avatarView)
        } else {
            Glide.with(this)
                .load(R.drawable.profile)
                .circleCrop()
                .into(avatarView)
        }
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
                        UserPrefs.putString(requireContext(), "avatar_url", avatarUrl)

                        imgAvatar?.let {
                            Glide.with(this@ProfileFragment)
                                .load(
                                    ApiConfig.BASE_URL.trimEnd('/') + "/" +
                                            avatarUrl.trimStart('/')
                                )
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .circleCrop()
                                .into(it)
                        }
                    }
                } else {
                    toast("Avatar upload failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toast("Avatar upload error")
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

    private fun prettifyValue(value: String): String {
        return value
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { part ->
                part.replaceFirstChar { ch -> ch.uppercase() }
            }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}