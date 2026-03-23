package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.ApiService
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.OnboardingProfileRequest
import com.example.flexifitapp.DetailedProgram
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.example.flexifitapp.profile.UpdateOnboardingRequest
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import retrofit2.Response // SIGURADUHIN NA ITO ANG IMPORT BABE

class SummaryFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_summary
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        bindSummary(view, isUpdate)

        view.findViewById<MaterialButton>(R.id.btnFinish)?.setOnClickListener {
            if (isUpdate) {
                updateProfile()
            } else {
                submitProfile()
            }
        }
    }

    private fun bindSummary(view: View, isUpdate: Boolean) {
        val ctx = requireContext()

        // Load all data from either UserPrefs or OnboardingStore based on isUpdate
        val age = if (isUpdate) {
            UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0)
        } else {
            OnboardingStore.getInt(ctx, FlexiFitKeys.AGE, 0)
        }
        val gender = if (isUpdate) {
            UserPrefs.getString(ctx, UserPrefs.KEY_GENDER, "")
        } else {
            OnboardingStore.getString(ctx, FlexiFitKeys.GENDER)
        }
        val height = if (isUpdate) {
            UserPrefs.getInt(ctx, UserPrefs.KEY_HEIGHT_CM, 0)
        } else {
            OnboardingStore.getInt(ctx, FlexiFitKeys.HEIGHT_CM, 0)
        }
        val weight = if (isUpdate) {
            UserPrefs.getInt(ctx, UserPrefs.KEY_WEIGHT_KG, 0)
        } else {
            OnboardingStore.getInt(ctx, FlexiFitKeys.WEIGHT_KG, 0)
        }
        val targetWeight = if (isUpdate) {
            UserPrefs.getInt(ctx, "target_weight_kg", 0)
        } else {
            OnboardingStore.getInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG, 0)
        }
        val level = if (isUpdate) {
            UserPrefs.getString(ctx, "fitness_level", "")
        } else {
            OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL)
        }
        val lifestyle = if (isUpdate) {
            UserPrefs.getString(ctx, "fitness_lifestyle", "")
        } else {
            OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LIFESTYLE)
        }
        val diet = if (isUpdate) {
            UserPrefs.getString(ctx, "dietary_type", "")
        } else {
            OnboardingStore.getString(ctx, FlexiFitKeys.DIETARY_TYPE)
        }
        val bodyComp = if (isUpdate) {
            UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "")
        } else {
            OnboardingStore.getString(ctx, FlexiFitKeys.BODYCOMP_GOAL)
        }
        val goals = if (isUpdate) {
            UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET) ?: emptySet()
        } else {
            OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS)
        }
        val programs = if (isUpdate) {
            UserPrefs.getStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS) ?: emptySet()
        } else {
            OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS)
        }
        val envs = if (isUpdate) {
            UserPrefs.getStringSet(ctx, "environment") ?: emptySet()
        } else {
            OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT)
        }

        // Bind to UI (same as before)
        view.findViewById<TextView>(R.id.tvSumAge)?.text = if (age > 0) age.toString() else "N/A"
        view.findViewById<TextView>(R.id.tvSumGender)?.text = gender.ifBlank { "N/A" }.capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumHeight)?.text = "$height cm"
        view.findViewById<TextView>(R.id.tvSumWeight)?.text = "$weight kg"
        view.findViewById<TextView>(R.id.tvSumTargetWeight)?.text = "$targetWeight kg"
        view.findViewById<TextView>(R.id.tvSumLevel)?.text = level.capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumLifestyle)?.text = lifestyle.capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumDiet)?.text = diet.capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumBodyComp)?.text = bodyComp.replace("_", " ").capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumGoal)?.text = goals.joinToString(", ") { it.replace("_", " ").capitalizeWords() }

        view.findViewById<TextView>(R.id.chipGym)?.isVisible = envs.contains("gym") || envs.contains("GYM")
        view.findViewById<TextView>(R.id.chipHome)?.isVisible = envs.contains("home") || envs.contains("HOME")

        // Render program chips
        val programsWrap = view.findViewById<LinearLayout>(R.id.programsWrap)
        val tvEmpty = view.findViewById<TextView>(R.id.tvProgramsEmpty)
        programsWrap?.removeAllViews()

        if (programs.isEmpty()) {
            tvEmpty?.isVisible = true
        } else {
            tvEmpty?.isVisible = false
            programs.forEach { programId ->
                val itemView = layoutInflater.inflate(R.layout.item_program_summary, programsWrap, false)
                val info = ProgramNameParser.parse(programId)

                itemView.findViewById<TextView>(R.id.tvProgramTitle)?.text = "${info.category} Program"
                itemView.findViewById<TextView>(R.id.tvProgramMeta)?.text = "${info.level} • ${info.environment}"

                programsWrap?.addView(itemView)
            }
        }
    }

    private fun submitProfile() {
        val ctx = requireContext()
        val btnFinish = view?.findViewById<MaterialButton>(R.id.btnFinish)
        btnFinish?.isEnabled = false

        // Build request from OnboardingStore (same as original)
        val rawBodyGoal = OnboardingStore.getString(ctx, FlexiFitKeys.BODYCOMP_GOAL).lowercase()
        val rawDietType = OnboardingStore.getString(ctx, FlexiFitKeys.DIETARY_TYPE).lowercase()

        val mappedBodyGoal = when {
            rawBodyGoal.contains("gain") -> "GAIN"
            rawBodyGoal.contains("lose") -> "LOSE"
            else -> "MAINTAIN"
        }

        val mappedDietType = when {
            rawDietType.contains("balanced") -> "BALANCED"
            rawDietType.contains("protein") -> "HIGH_PROTEIN"
            rawDietType.contains("keto") -> "KETO"
            rawDietType.contains("vegan") -> "VEGAN"
            rawDietType.contains("vegetarian") -> "VEGETARIAN"
            rawDietType.contains("lactose") -> "LACTOSE_FREE"
            else -> "BALANCED"
        }

        // ✅ Convert environment set to list of strings
        val environmentSet = OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT)
        val environmentList = environmentSet.map { it.uppercase() }

        val detailedPrograms = OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS).map { id ->
            val info = ProgramNameParser.parse(id)
            DetailedProgram(
                category = info.category.uppercase().replace(" ", "_"),
                level = info.level.uppercase(),
                environment = info.environment.uppercase(),
                rawName = id
            )
        }

        val request = OnboardingProfileRequest(
            name = OnboardingStore.getString(ctx, "user_name", "User"),
            username = OnboardingStore.getString(ctx, "user_handle", "user"),
            bodyGoal = mappedBodyGoal,
            dietType = mappedDietType,
            age = OnboardingStore.getInt(ctx, FlexiFitKeys.AGE),
            gender = OnboardingStore.getString(ctx, FlexiFitKeys.GENDER).uppercase(),
            heightCm = OnboardingStore.getInt(ctx, FlexiFitKeys.HEIGHT_CM),
            weightKg = OnboardingStore.getInt(ctx, FlexiFitKeys.WEIGHT_KG),
            targetWeightKg = OnboardingStore.getInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG),
            activityLevel = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LIFESTYLE).uppercase(),
            fitnessLevel = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL).uppercase(),
            upperBodyInjury = OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY),
            lowerBodyInjury = OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY),
            jointProblems = OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS),
            shortBreath = OnboardingStore.getBoolean(ctx, FlexiFitKeys.SHORT_BREATH),
            healthNone = OnboardingStore.getBoolean(ctx, FlexiFitKeys.HEALTH_NONE),
            environment = environmentList,  // ✅ CHANGE: use list of strings (already correct)
            fitnessGoals = OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS).map { it.uppercase() },
            selectedPrograms = detailedPrograms,
            isRehab = OnboardingStore.getBoolean(ctx, FlexiFitKeys.IS_REHAB_USER)
        )

        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val res = api.submitProfile(request)

                if (res.isSuccessful) {
                    OnboardingStore.clearAll(ctx)
                    UserPrefs.setOnboardingDone(ctx, true)
                    Toast.makeText(ctx, "Welcome to FlexiFit!", Toast.LENGTH_SHORT).show()
                    (activity as? OnboardingActivity)?.goToMain()
                } else {
                    btnFinish?.isEnabled = true
                    val errorMsg = res.errorBody()?.string() ?: "Unknown Server Error"
                    Toast.makeText(ctx, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                btnFinish?.isEnabled = true
                Toast.makeText(ctx, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        val ctx = requireContext()
        val btnFinish = view?.findViewById<MaterialButton>(R.id.btnFinish)
        btnFinish?.isEnabled = false

        // Convert environment set to comma-separated string
        val environmentSet = UserPrefs.getStringSet(ctx, "environment") ?: emptySet()
        val environmentString = environmentSet.joinToString(",")

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
            environment = environmentString,  // ✅ CHANGE: use comma-separated string
            bodyCompGoal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "MAINTAIN_WEIGHT"),
            dietaryType = UserPrefs.getString(ctx, "dietary_type", "STANDARD"),
            fitnessGoals = UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET).toList(),
            selectedPrograms = UserPrefs.getStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS)
                .toList(),
            isRehabUser = UserPrefs.getBool(ctx, "is_rehab_user", false),
            name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, ""),
            username = UserPrefs.getString(ctx, UserPrefs.KEY_USERNAME, "")
        )

        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val response = api.updateFullProfile(request)

                if (response.isSuccessful) {
                    Toast.makeText(ctx, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    (activity as? OnboardingActivity)?.finish()
                } else {
                    btnFinish?.isEnabled = true
                    Toast.makeText(ctx, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                btnFinish?.isEnabled = true
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun String.capitalizeWords() = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
}