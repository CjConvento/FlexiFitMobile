package com.example.flexifitapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.flexifitapp.ApiClient.api
import com.example.flexifitapp.databinding.FragmentProfileffBinding
import com.example.flexifitapp.profile.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(R.layout.fragment_profileff) {

    private var _binding: FragmentProfileffBinding? = null
    private val binding get() = _binding!!
    private var latestProfile: UserProfileResponse? = null

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadAvatar(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileffBinding.bind(view)

        setupClicks()
        setupResultListeners()
        syncProfileFromServer()
        loadLocalProfileData() // Show cached data immediately
    }

    private fun setupClicks() {
        binding.rowPersonalData.setOnClickListener {
            PersonalDataDialogFragment().show(parentFragmentManager, "PersonalDataDialog")
        }
        binding.rowWorkoutData.setOnClickListener {
            WorkoutDataDialogFragment().show(parentFragmentManager, "WorkoutDataDialog")
        }
        // ✅ Updated click listener with data passing
        binding.rowNutritionData.setOnClickListener {
            val dialog = NutritionalDataDialogFragment()
            if (latestProfile != null) {
                val args = Bundle().apply {
                    putInt("age", latestProfile!!.age)
                    putDouble("height", latestProfile!!.heightCm)
                    putDouble("weight", latestProfile!!.weightKg)
                    putDouble("targetWeight", latestProfile!!.targetWeightKg ?: 0.0)
                    putDouble("bmi", latestProfile!!.bmi)
                    putString("bmiCategory", latestProfile!!.bmiCategory)
                    putString("nutritionGoal", latestProfile!!.nutritionGoal ?: "")
                }
                dialog.arguments = args
            } else {
                // Fallback: no data yet – show dialog without arguments; it will read from prefs
                Log.d("ProfileFragment", "latestProfile is null, opening dialog without args")
            }
            dialog.show(parentFragmentManager, "NutritionDataDialog")
        }
        binding.rowTrackProgress.setOnClickListener {
            findNavController().navigate(R.id.nav_progresstracker)
        }
        binding.btnEdit.setOnClickListener {
            WeightQuickEditDialogFragment().show(parentFragmentManager, "WeightQuickEditDialog")
        }
        binding.btnUpdateProfile.setOnClickListener {
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            intent.putExtra("isUpdate", true)
            startActivity(intent)
        }
        binding.imgAvatar.setOnClickListener {
            pickAvatar.launch("image/*")
        }
    }

    private fun setupResultListeners() {
        parentFragmentManager.setFragmentResultListener(WeightQuickEditDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            syncProfileFromServer()
        }
        parentFragmentManager.setFragmentResultListener(PersonalDataDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            syncProfileFromServer()
        }
    }

    internal fun syncProfileFromServer() {
        lifecycleScope.launch {
            try {
                val response = api().getFullProfile()   // ← added parentheses
                if (response.isSuccessful && response.body() != null) {
                    latestProfile = response.body()!!
                    storeProfileData(latestProfile!!)
                    loadLocalProfileData()
                } else {
                    Log.e("ProfileFragment", "Sync failed: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Sync exception: ${e.message}")
            }
        }
    }

    private fun storeProfileData(data: UserProfileResponse) {
        val ctx = requireContext()
        // Basic info
        UserPrefs.putString(ctx, UserPrefs.KEY_NAME, data.name)
        UserPrefs.putString(ctx, UserPrefs.KEY_USERNAME, data.username)
        UserPrefs.putString(ctx, UserPrefs.KEY_GENDER, data.gender)
        UserPrefs.putInt(ctx, UserPrefs.KEY_AGE, data.age)
        UserPrefs.putFloat(ctx, UserPrefs.KEY_HEIGHT_CM, data.heightCm.toFloat())
        UserPrefs.putFloat(ctx, UserPrefs.KEY_WEIGHT_KG, data.weightKg.toFloat())
        data.targetWeightKg?.let { UserPrefs.putFloat(ctx, UserPrefs.KEY_TARGET_WEIGHT_KG, it.toFloat()) }

        // Log basic info
        Log.d("ProfileFragment", "Stored basic: name=${data.name}, username=${data.username}, age=${data.age}, height=${data.heightCm}, weight=${data.weightKg}")

        // Goals & achievements
        UserPrefs.putString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, data.goalSubtitle)
        UserPrefs.putString(ctx, "nutritional_goal", data.nutritionGoal ?: "")
        UserPrefs.putInt(ctx, "total_sessions", data.totalSessions)
        UserPrefs.putInt(ctx, "total_workouts", data.totalWorkouts)
        UserPrefs.putInt(ctx, "completed_sessions", data.completedSessions)
        UserPrefs.putInt(ctx, "total_program_sessions", data.totalProgramSessions)

        Log.d("ProfileFragment", "Stored goals: bodyComp=${data.goalSubtitle}, nutritionGoal=${data.nutritionGoal}, sessions=$data.totalSessions, workouts=$data.totalWorkouts")

        // Programs & goals (for WorkoutData dialog)
        UserPrefs.putStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS, data.selectedPrograms.toSet())
        UserPrefs.putStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET, data.fitnessGoals.toSet())

        Log.d("ProfileFragment", "Stored programs: ${data.selectedPrograms}, fitnessGoals: ${data.fitnessGoals}")

        // Nutrition targets
        UserPrefs.putInt(ctx, "daily_calorie_target", data.dailyCalorieTarget)
        UserPrefs.putFloat(ctx, "protein_g", data.proteinG.toFloat())
        UserPrefs.putFloat(ctx, "carbs_g", data.carbsG.toFloat())
        UserPrefs.putFloat(ctx, "fats_g", data.fatsG.toFloat())

        // Achievements & BMI
        UserPrefs.putInt(ctx, "achievement_count", data.achievementCount)
        UserPrefs.putStringSet(ctx, "unlocked_badges", data.unlockedBadges.toSet())
        UserPrefs.putStringSet(ctx, "unlocked_badge_keys", data.unlockedBadgeKeys.toSet())
        UserPrefs.putFloat(ctx, "bmi_value", data.bmi.toFloat())
        UserPrefs.putString(ctx, "bmi_category", data.bmiCategory)

        Log.d("ProfileFragment", "Stored nutrition: calorieTarget=${data.dailyCalorieTarget}, protein=${data.proteinG}, carbs=${data.carbsG}, fats=${data.fatsG}")
        Log.d("ProfileFragment", "Stored BMI: ${data.bmi}, category=${data.bmiCategory}")
    }

    private fun loadLocalProfileData() {
        val ctx = requireContext()
        val name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, "User Name")
        val goal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "User Goal")
        val age = UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0)
        val height = UserPrefs.getFloat(ctx, UserPrefs.KEY_HEIGHT_CM, 0f).toInt()
        val weight = UserPrefs.getFloat(ctx, UserPrefs.KEY_WEIGHT_KG, 0f).toInt()

        binding.tvName.text = name
        binding.tvGoalSubtitle.text = prettifyValue(goal)
        binding.tvAgeValue.text = if (age > 0) "$age yo" else "-"
        binding.tvHeightValue.text = if (height > 0) "$height cm" else "-"
        binding.tvWeightValue.text = if (weight > 0) "$weight kg" else "-"

        val avatarUrl = UserPrefs.getString(ctx, "avatar_url", "")
        if (avatarUrl.isNotBlank()) {
            Glide.with(this)
                .load(if (avatarUrl.startsWith("http")) avatarUrl else ApiConfig.BASE_URL + avatarUrl)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(binding.imgAvatar)
        }
    }

    private fun performFullProfileUpdate() {
        val ctx = requireContext()
        val request = UpdateOnboardingRequest(
            age = UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0),
            gender = UserPrefs.getString(ctx, UserPrefs.KEY_GENDER, "MALE"),
            heightCm = UserPrefs.getFloat(ctx, UserPrefs.KEY_HEIGHT_CM, 0f).toDouble(),
            weightKg = UserPrefs.getFloat(ctx, UserPrefs.KEY_WEIGHT_KG, 0f).toDouble(),
            targetWeightKg = UserPrefs.getInt(ctx, "target_weight_kg", 0).toDouble(),
            upperBodyInjury = UserPrefs.getBool(ctx, "upper_body_injury", false),
            lowerBodyInjury = UserPrefs.getBool(ctx, "lower_body_injury", false),
            jointProblems = UserPrefs.getBool(ctx, "joint_problems", false),
            shortBreath = UserPrefs.getBool(ctx, "short_breath", false),
            fitnessLifestyle = UserPrefs.getString(ctx, "fitness_lifestyle", "ACTIVE"),
            fitnessLevel = UserPrefs.getString(ctx, "fitness_level", "BEGINNER"),
            environment = UserPrefs.getString(ctx, "environment", "GYM"),
            bodyCompGoal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "MAINTAIN_WEIGHT"),
            dietaryType = UserPrefs.getString(ctx, "dietary_type", "STANDARD"),
            fitnessGoals = UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET).toList(),
            selectedPrograms = UserPrefs.getStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS).toList(),
            isRehabUser = UserPrefs.getBool(ctx, "is_rehab_user", false),
            name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, ""),
            username = UserPrefs.getString(ctx, UserPrefs.KEY_USERNAME, "")
        )

        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val response = api.updateFullProfile(request)
                if (response.isSuccessful) {
                    Toast.makeText(ctx, "Profile updated", Toast.LENGTH_SHORT).show()
                    syncProfileFromServer()
                } else {
                    Toast.makeText(ctx, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadAvatar(uri: Uri) {
        val context = requireContext()
        lifecycleScope.launch {
            try {
                val file = uriToFile(uri)
                val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                val api = ApiClient.api()
                val response = api.uploadAvatar(body)
                if (response.isSuccessful) {
                    val newUrl = response.body()?.url
                    UserPrefs.putString(context, "avatar_url", newUrl ?: "")
                    loadLocalProfileData()
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Upload error", Toast.LENGTH_SHORT).show()
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}