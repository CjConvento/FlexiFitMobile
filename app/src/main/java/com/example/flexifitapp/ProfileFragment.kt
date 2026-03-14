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
import com.example.flexifitapp.profile.AchievementEngine
import com.example.flexifitapp.profile.AchievementsDialogFragment
import com.example.flexifitapp.profile.PersonalDataDialogFragment
import com.example.flexifitapp.profile.WeightQuickEditDialogFragment
import com.example.flexifitapp.profile.NutritionalDataDialogFragment
import com.example.flexifitapp.profile.UpdateOnboardingRequest
import com.example.flexifitapp.profile.WorkoutDataDialogFragment
import com.example.flexifitapp.profile.UserManagementResponse
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
                Glide.with(this).load(uri).circleCrop().into(imageView)
            }
            uploadAvatar(uri)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupResultListeners()
        setupClicks()

        // Initial load from local storage (Fast)
        loadLocalProfileData()
        loadLocalAvatar()

        // Sync from Server (Fresh)
        syncProfileFromServer()

        btnUpdateProfile?.setOnClickListener {
            performFullProfileUpdate()
        }
    }

    override fun onResume() {
        super.onResume()
        syncProfileFromServer()
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

    private fun setupClicks() {
        imgAvatar?.setOnClickListener { pickAvatar.launch("image/*") }

        btnEdit?.setOnClickListener {
            WeightQuickEditDialogFragment().show(parentFragmentManager, "WeightQuickEditDialog")
        }

        rowPersonalData?.setOnClickListener {
            PersonalDataDialogFragment().show(parentFragmentManager, "PersonalDataDialog")
        }

        rowWorkoutData?.setOnClickListener {
            WorkoutDataDialogFragment().show(parentFragmentManager, "WorkoutDataDialog")
        }

        rowNutritionData?.setOnClickListener {
            NutritionalDataDialogFragment().show(parentFragmentManager, "NutritionDataDialog")
        }

        rowAchievements?.setOnClickListener {
            AchievementsDialogFragment().show(parentFragmentManager, "AchievementsDialog")
        }

        rowTrackProgress?.setOnClickListener {
            try { findNavController().navigate(R.id.nav_progtr) }
            catch (e: Exception) { toast("Progress screen coming soon!") }
        }
    }

    private fun syncProfileFromServer() {
        val ctx = context ?: return

        // Launch sa coroutine para hindi mag-lag ang UI
        lifecycleScope.launch {
            try {
                // Siguraduhin na ang ApiService mo ay may getFullProfile()
                val api = ApiClient.get(ctx).create(ApiService::class.java)
                val response = api.getFullProfile()

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // 1. DITO NAGAGAMIT YUNG SYNCWITHSERVER!
                    // Ang 'data.unlockedBadges' ay dapat List<String> galing sa C#
                    AchievementEngine.syncWithServer(ctx, data.unlockedBadges)

                    // 2. I-update ang Local Prefs (UserPrefs)
                    UserPrefs.putString(ctx, UserPrefs.KEY_NAME, data.name)
                    UserPrefs.putString(ctx, UserPrefs.KEY_USER_NAME, data.username)
                    UserPrefs.putInt(ctx, UserPrefs.KEY_AGE, data.age)
                    UserPrefs.putInt(ctx, UserPrefs.KEY_HEIGHT_CM, data.heightCm.toInt())
                    UserPrefs.putInt(ctx, UserPrefs.KEY_WEIGHT_KG, data.weightKg.toInt())
                    UserPrefs.putString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, data.goalSubtitle)

                    // Dagdag natin 'tong dalawa para sa dialogs mo
                    UserPrefs.putInt(ctx, "total_sessions", data.totalSessions)
                    UserPrefs.putString(ctx, "bmi_category", data.bmiCategory)

                    // 3. I-refresh ang UI kapag tapos na ang sync
                    if (isAdded) loadLocalProfileData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadLocalProfileData() {
        val ctx = context ?: return
        val name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, "User Name")
        val age = UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0)
        val height = UserPrefs.getInt(ctx, UserPrefs.KEY_HEIGHT_CM, 0)
        val weight = UserPrefs.getInt(ctx, UserPrefs.KEY_WEIGHT_KG, 0)
        val goal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "User Goal")

        tvName?.text = name
        tvGoalSubtitle?.text = if (goal.isNotBlank()) prettifyValue(goal) else "User Goal"
        tvAgeValue?.text = "$age yo"
        tvHeightValue?.text = "$height cm"
        tvWeightValue?.text = "$weight kg"
    }

    private fun performFullProfileUpdate() {
        val ctx = context ?: return

        lifecycleScope.launch {
            try {
                // 1. I-construct ang DTO gamit ang current data sa UserPrefs
                val request = UpdateOnboardingRequest(
                    age = UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0),
                    gender = UserPrefs.getString(ctx, UserPrefs.KEY_GENDER, "MALE"),
                    heightCm = UserPrefs.getInt(ctx, UserPrefs.KEY_HEIGHT_CM, 0).toDouble(),
                    weightKg = UserPrefs.getInt(ctx, UserPrefs.KEY_WEIGHT_KG, 0).toDouble(),
                    targetWeightKg = UserPrefs.getInt(ctx, "target_weight", 0).toDouble(),

                    // Health Flags (Pwede mong i-default or i-save din sa prefs)
                    upperBodyInjury = false,
                    lowerBodyInjury = false,
                    jointProblems = false,
                    shortBreath = false,

                    // Lifestyle & Goals
                    fitnessLifestyle = "ACTIVE",
                    fitnessLevel = "BEGINNER",
                    environment = "GYM",
                    bodyCompGoal = UserPrefs.getString(
                        ctx,
                        UserPrefs.KEY_BODYCOMP_GOAL,
                        "MAINTAIN_WEIGHT"
                    ),
                    dietaryType = "STANDARD",

                    fitnessGoals = emptyList(),
                    selectedPrograms = emptyList(),
                    isRehabUser = false
                )

                // 2. API Call
                val api = ApiClient.get(ctx).create(ApiService::class.java)
                val response = api.updateFullProfile(request) // Siguraduhin na nasa ApiService ito

                if (response.isSuccessful) {
                    toast("Profile & Macros updated!")
                    syncProfileFromServer() // Re-sync para makuha yung bagong calculated calories
                } else {
                    toast("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                toast("Update failed: ${e.message}")
            }
        }
    }

    private fun loadLocalAvatar() {
        val ctx = context ?: return
        val localAvatar = UserPrefs.getString(ctx, "avatar_url", "")
        imgAvatar?.let {
            Glide.with(this)
                .load(if (localAvatar.startsWith("http")) localAvatar else ApiConfig.BASE_URL + localAvatar)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(it)
        }
    }

    private fun uploadAvatar(uri: Uri) {
        val context = context ?: return
        lifecycleScope.launch {
            try {
                val file = uriToFile(uri)
                val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                val api = ApiClient.get(context).create(ApiService::class.java)
                val response = api.uploadAvatar(body)
                if (response.isSuccessful) {
                    val newUrl = response.body()?.url
                    UserPrefs.putString(context, "avatar_url", newUrl ?: "")
                    loadLocalAvatar()
                    toast("Avatar updated!")
                }
            } catch (e: Exception) { toast("Upload error") }
        }
    }

    // Helper functions (onDestroyView, uriToFile, prettifyValue, etc.) remain same as your original
    override fun onDestroyView() {
        super.onDestroyView()
        imgAvatar = null; tvName = null; tvGoalSubtitle = null; tvAgeValue = null
        tvHeightValue = null; tvWeightValue = null; rowPersonalData = null
        rowWorkoutData = null; rowNutritionData = null; rowAchievements = null
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
        return file
    }

    private fun prettifyValue(value: String): String {
        return value.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun toast(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupResultListeners() {
        parentFragmentManager.setFragmentResultListener(WeightQuickEditDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val newWeight = bundle.getInt(WeightQuickEditDialogFragment.BUNDLE_NEW_WEIGHT, 0)
            if (newWeight > 0) tvWeightValue?.text = "$newWeight kg"
            syncProfileFromServer()
        }
    }
}