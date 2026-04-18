package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.flexifitapp.onboarding.FlexiFitKeys
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
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SummaryFragment : Fragment(R.layout.obd_fragment_summary) {

    private var rootView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        Log.d("FLEXIFIT_DEBUG", "--- Final Summary Page ---")

        bindSummary(view)

        view.findViewById<TextView>(R.id.btnFinish)?.setOnClickListener {
            submitProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI when fragment becomes visible
        rootView?.let { bindSummary(it) }
    }

    private fun bindSummary(view: View) {
        val ctx = requireContext()

        // 1. DATA EXTRACTION
        val age = OnboardingStore.getInt(ctx, FlexiFitKeys.AGE, 0)
        val gender = OnboardingStore.getString(ctx, FlexiFitKeys.GENDER)
        val height = OnboardingStore.getInt(ctx, FlexiFitKeys.HEIGHT_CM, 0)
        val weight = OnboardingStore.getInt(ctx, FlexiFitKeys.WEIGHT_KG, 0)
        val targetWeight = OnboardingStore.getInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG, 0)
        val level = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL)
        val lifestyle = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LIFESTYLE)
        val diet = OnboardingStore.getString(ctx, FlexiFitKeys.DIETARY_TYPE)
        val bodyComp = OnboardingStore.getString(ctx, FlexiFitKeys.BODYCOMP_GOAL)
        val goals = OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS)
        val programs = OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS)

        // Convert environment to lowercase for case‑insensitive matching
        val envs = OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT)
            .map { it.lowercase() }
            .toSet()

        // Allergies – filter out blank entries to avoid trailing comma
        val allergies = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ALLERGIES_LIST)
            .filter { it.isNotBlank() }
        view.findViewById<TextView>(R.id.tvSumAllergies).text = if (allergies.isEmpty()) "None" else allergies.joinToString(", ")

        // 2. UI BINDING
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

        // Workout location chips (case‑insensitive)
        view.findViewById<TextView>(R.id.chipGym)?.isVisible = envs.contains("gym")
        view.findViewById<TextView>(R.id.chipHome)?.isVisible = envs.contains("home")

        // Allergies (only once)
        view.findViewById<TextView>(R.id.tvSumAllergies).text = if (allergies.isEmpty()) "None" else allergies.joinToString(", ")

        // Health & Safety Check
        val upperBody = OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY)
        val lowerBody = OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY)
        val joint = OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS)
        val shortBreath = OnboardingStore.getBoolean(ctx, FlexiFitKeys.SHORT_BREATH)
        val healthNone = OnboardingStore.getBoolean(ctx, FlexiFitKeys.HEALTH_NONE)

        val healthIssues = mutableListOf<String>()
        if (upperBody) healthIssues.add("Upper Body Injury")
        if (lowerBody) healthIssues.add("Lower Body Injury")
        if (joint) healthIssues.add("Joint Problems")
        if (shortBreath) healthIssues.add("Short Breath")
        if (healthNone) healthIssues.add("None")

        view.findViewById<TextView>(R.id.tvSumHealth).text = if (healthIssues.isEmpty()) "None" else healthIssues.joinToString(", ")

        // 3. RENDER PROGRAM CHIPS
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
        val btnFinish = view?.findViewById<TextView>(R.id.btnFinish)
        btnFinish?.isEnabled = false

        // 1. PRE-MAPPING
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

        val detailedPrograms = OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS).map { id ->
            val info = ProgramNameParser.parse(id)
            DetailedProgram(
                category = info.category.uppercase().replace(" ", "_"),
                level = info.level.uppercase(),
                environment = info.environment.uppercase(),
                rawName = id
            )
        }

        // Retrieve allergies
        val allergies = OnboardingStore.getStringSet(ctx, FlexiFitKeys.ALLERGIES_LIST).toList()

        // 🔥 Send empty strings – backend will keep existing name/username from usr_users
        val finalName = ""
        val safeUsername = ""

        // 🔥 CRITICAL: Use the computed name and username, NOT the old hardcoded ones
        val request = OnboardingProfileRequest(
            name = finalName,
            username = safeUsername,
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
            environment = OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT).map { it.uppercase() },
            fitnessGoals = OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS).map { it.uppercase() },
            selectedPrograms = detailedPrograms,
            isRehab = OnboardingStore.getBoolean(ctx, FlexiFitKeys.IS_REHAB_USER),
            allergies = allergies   // Added here
        )

        Log.d("FLEXIFIT_DEBUG", "SUBMITTING -> BodyGoal: ${request.bodyGoal}, Diet: ${request.dietType}, Username: ${request.username}, Name: ${request.name}")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.get().create(ApiService::class.java)
                val res = api.submitProfile(request)

                if (res.isSuccessful) {
                    OnboardingStore.clearAll(ctx)
                    UserPrefs.setOnboardingDone(ctx, true)
                    Toast.makeText(ctx, "Welcome to FlexiFit!", Toast.LENGTH_SHORT).show()
                    (activity as? OnboardingActivity)?.goToMain()
                } else {
                    btnFinish?.isEnabled = true
                    val errorMsg = res.errorBody()?.string() ?: "Unknown Server Error"
                    Log.e("FLEXIFIT_DEBUG", "--- SERVER ERROR ---")
                    Log.e("FLEXIFIT_DEBUG", "Detail: $errorMsg")
                    Toast.makeText(ctx, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                btnFinish?.isEnabled = true
                Log.e("FLEXIFIT_DEBUG", "Request Failed", e)
                Toast.makeText(ctx, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun String.capitalizeWords() = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
}