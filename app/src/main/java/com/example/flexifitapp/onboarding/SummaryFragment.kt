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
import com.example.flexifitapp.MobileApi
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.OnboardingProfileRequest
import com.example.flexifitapp.DetailedProgram
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SummaryFragment : Fragment(R.layout.obd_fragment_summary) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindSummary(view)

        view.findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener {
            submitProfile()
        }
    }

    private fun bindSummary(view: View) {
        val ctx = requireContext()

        // --- FETCHING DATA ---
        val age = OnboardingStore.getInt(ctx, FlexiFitKeys.AGE, 0)
        val gender = OnboardingStore.getString(ctx, FlexiFitKeys.GENDER)
        val height = OnboardingStore.getInt(ctx, FlexiFitKeys.HEIGHT_CM, 0)
        val weight = OnboardingStore.getInt(ctx, FlexiFitKeys.WEIGHT_KG, 0)
        val targetWeight = OnboardingStore.getInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG, 0)
        val lifestyle = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LIFESTYLE)
        val level = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL)
        val bodyGoal = OnboardingStore.getString(ctx, FlexiFitKeys.BODYCOMP_GOAL)
        val dietType = OnboardingStore.getString(ctx, FlexiFitKeys.DIETARY_TYPE)
        val environments = OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT)

        // ✅ BUG FIX: Added 'S' to match your FlexiFitKeys.FITNESS_GOALS
        val goals = OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS)
        val programs = OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS)

        // --- HEALTH LOGIC ---
        val healthItems = mutableListOf<String>()
        if (OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY)) healthItems.add("Upper Body")
        if (OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY)) healthItems.add("Lower Body")
        if (OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS)) healthItems.add("Joints")
        if (OnboardingStore.getBoolean(ctx, FlexiFitKeys.SHORT_BREATH)) healthItems.add("Breathing")
        if (healthItems.isEmpty()) healthItems.add("None")

        // --- BINDING TO UI ---
        view.findViewById<TextView>(R.id.tvSumAge).text = if (age > 0) age.toString() else "N/A"
        view.findViewById<TextView>(R.id.tvSumGender).text = gender.ifBlank { "N/A" }
        view.findViewById<TextView>(R.id.tvSumHeight).text = "$height cm"
        view.findViewById<TextView>(R.id.tvSumWeight).text = "$weight kg"
        view.findViewById<TextView>(R.id.tvTargetWeight).text = "$targetWeight kg"
        view.findViewById<TextView>(R.id.tvSumHealth).text = healthItems.joinToString(", ")
        view.findViewById<TextView>(R.id.tvSumLifestyle).text = lifestyle.replace("_", " ").capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumLevel).text = level.capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumGoal).text = goals.joinToString(", ") { it.replace("_", " ").capitalizeWords() }
        view.findViewById<TextView>(R.id.tvSumBodyComp).text = bodyGoal.replace("_", " ").capitalizeWords()
        view.findViewById<TextView>(R.id.tvSumDiet).text = dietType.replace("_", " ").capitalizeWords()

        view.findViewById<TextView>(R.id.chipGym).isVisible = environments.any { it.equals("gym", true) }
        view.findViewById<TextView>(R.id.chipHome).isVisible = environments.any { it.equals("home", true) }

        // --- RENDER PROGRAMS ---
        val programsWrap = view.findViewById<LinearLayout>(R.id.programsWrap)
        programsWrap.removeAllViews()
        if (programs.isEmpty()) {
            view.findViewById<TextView>(R.id.tvProgramsEmpty).isVisible = true
        } else {
            view.findViewById<TextView>(R.id.tvProgramsEmpty).isVisible = false
            programs.forEach { id ->
                val tv = TextView(ctx).apply {
                    text = id.replace("_", " ").uppercase()
                    setTextColor(resources.getColor(R.color.white, null))
                    setPadding(24, 12, 24, 12)
                    background = resources.getDrawable(R.drawable.bg_option_tile_selected, null)
                }
                programsWrap.addView(tv)
            }
        }
    }

    private fun submitProfile() {
        val ctx = requireContext()
        val btnConfirm = view?.findViewById<MaterialButton>(R.id.btnConfirm)
        btnConfirm?.isEnabled = false

        // 1. Parsing Programs using your ProgramNameParser
        val detailedPrograms = OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS).map { id ->
            val info = ProgramNameParser.parse(id)
            DetailedProgram(category = info.category, level = info.level, environment = info.environment, rawName = id)
        }

        // 2. Build Request Object
        val request = OnboardingProfileRequest(
            age = OnboardingStore.getInt(ctx, FlexiFitKeys.AGE),
            gender = OnboardingStore.getString(ctx, FlexiFitKeys.GENDER),
            heightCm = OnboardingStore.getInt(ctx, FlexiFitKeys.HEIGHT_CM),
            weightKg = OnboardingStore.getInt(ctx, FlexiFitKeys.WEIGHT_KG),
            targetWeightKg = OnboardingStore.getInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG),
            upperBodyInjury = OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY),
            lowerBodyInjury = OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY),
            jointProblems = OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS),
            shortBreath = OnboardingStore.getBoolean(ctx, FlexiFitKeys.SHORT_BREATH),
            healthNone = OnboardingStore.getBoolean(ctx, FlexiFitKeys.HEALTH_NONE),
            activityLevel = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LIFESTYLE),
            fitnessLevel = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL),
            environment = OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT).toList(),
            fitnessGoals = OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS).toList(),
            bodyGoal = OnboardingStore.getString(ctx, FlexiFitKeys.BODYCOMP_GOAL),
            dietType = OnboardingStore.getString(ctx, FlexiFitKeys.DIETARY_TYPE),
            selectedPrograms = detailedPrograms,
            isRehab = OnboardingStore.getBoolean(ctx, FlexiFitKeys.IS_REHAB_USER)
        )

        // 3. POST to Server
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.get(ctx).create(MobileApi::class.java)
                val res = api.submitProfile(request)

                if (res.isSuccessful) {
                    // Success! Clear onboarding cache
                    OnboardingStore.clearAll(ctx)
                    UserPrefs.setOnboardingDone(ctx, true) // Update main app state

                    Toast.makeText(ctx, "Profile Synced!", Toast.LENGTH_SHORT).show()
                    (activity as? OnboardingActivity)?.goToMain()
                } else {
                    btnConfirm?.isEnabled = true
                    Toast.makeText(ctx, "Server Error: ${res.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                btnConfirm?.isEnabled = true
                Log.e("Summary", "Submission failed", e)
                Toast.makeText(ctx, "Check your internet connection.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun String.capitalizeWords() = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}