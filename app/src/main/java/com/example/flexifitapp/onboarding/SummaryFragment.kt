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
import com.example.flexifitapp.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SummaryFragment : Fragment(R.layout.obd_fragment_summary) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindSummary(view)

        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)
        btnConfirm.setOnClickListener {
            submitProfile()
        }
    }

    private fun bindSummary(view: View) {
        val ctx = requireContext()

        val tvSumAge = view.findViewById<TextView>(R.id.tvSumAge)
        val tvSumGender = view.findViewById<TextView>(R.id.tvSumGender)
        val tvSumHeight = view.findViewById<TextView>(R.id.tvSumHeight)
        val tvSumWeight = view.findViewById<TextView>(R.id.tvSumWeight)
        val tvSumTargetWeight = view.findViewById<TextView>(R.id.tvSumTargetWeight)
        val tvSumHealth = view.findViewById<TextView>(R.id.tvSumHealth)
        val tvSumLifestyle = view.findViewById<TextView>(R.id.tvSumLifestyle)
        val tvSumLevel = view.findViewById<TextView>(R.id.tvSumLevel)
        val tvSumGoal = view.findViewById<TextView>(R.id.tvSumGoal)
        val tvSumBodyComp = view.findViewById<TextView>(R.id.tvSumBodyComp)
        val tvSumDiet = view.findViewById<TextView>(R.id.tvSumDiet)

        val chipGym = view.findViewById<TextView>(R.id.chipGym)
        val chipHome = view.findViewById<TextView>(R.id.chipHome)

        val programsWrap = view.findViewById<LinearLayout>(R.id.programsWrap)
        val tvProgramsEmpty = view.findViewById<TextView>(R.id.tvProgramsEmpty)

        val age = OnboardingStore.getInt(ctx, "age", 0)
        val gender = OnboardingStore.getString(ctx, "gender")
        val height = OnboardingStore.getInt(ctx, "height_cm", 0)
        val weight = OnboardingStore.getInt(ctx, "weight_kg", 0)
        val targetWeight = OnboardingStore.getInt(ctx, "target_weight_kg", 0)

        val lifestyle = OnboardingStore.getString(ctx, "fitness_lifestyle")
        val level = OnboardingStore.getString(ctx, "fitness_level")
        val bodyGoal = OnboardingStore.getString(ctx, "bodycomp_goal")
        val dietType = OnboardingStore.getString(ctx, "dietary_type")

        val environments = OnboardingStore.getStringSet(ctx, "environment")
        val goals = OnboardingStore.getStringSet(ctx, "fitness_goal")
        val programs = OnboardingStore.getStringSet(ctx, "selected_programs")

        val healthItems = mutableListOf<String>()
        if (OnboardingStore.getBoolean(ctx, "upper_body_injury", false)) healthItems.add("Upper Body Injury")
        if (OnboardingStore.getBoolean(ctx, "lower_body_injury", false)) healthItems.add("Lower Body Injury")
        if (OnboardingStore.getBoolean(ctx, "joint_problems", false)) healthItems.add("Joint Problems")
        if (OnboardingStore.getBoolean(ctx, "short_breath", false)) healthItems.add("Shortness of Breath")
        if (OnboardingStore.getBoolean(ctx, "health_none", false)) healthItems.add("None")

        tvSumAge.text = if (age > 0) age.toString() else "-"
        tvSumGender.text = gender.ifBlank { "-" }
        tvSumHeight.text = if (height > 0) height.toString() else "-"
        tvSumWeight.text = if (weight > 0) weight.toString() else "-"
        tvSumTargetWeight.text = if (targetWeight > 0) targetWeight.toString() else "-"
        tvSumHealth.text = if (healthItems.isNotEmpty()) healthItems.joinToString(", ") else "-"
        tvSumLifestyle.text = lifestyle.ifBlank { "-" }
        tvSumLevel.text = level.ifBlank { "-" }
        tvSumGoal.text = if (goals.isNotEmpty()) goals.joinToString(", ") else "-"
        tvSumBodyComp.text = bodyGoal.ifBlank { "-" }
        tvSumDiet.text = dietType.ifBlank { "-" }

        chipGym.isVisible = environments.contains("Gym") || environments.contains("GYM")
        chipHome.isVisible = environments.contains("Home") || environments.contains("HOME")

        programsWrap.removeAllViews()
        if (programs.isEmpty()) {
            tvProgramsEmpty.isVisible = true
        } else {
            tvProgramsEmpty.isVisible = false
            programs.forEach { program ->
                val tv = TextView(ctx).apply {
                    text = program
                    setTextColor(resources.getColor(R.color.colorPrimary, null))
                    textSize = 14f
                    setPadding(24, 12, 24, 12)
                    background = resources.getDrawable(R.drawable.flexifit_border, null)
                    gravity = android.view.Gravity.CENTER
                }

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = 8
                lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
                tv.layoutParams = lp

                programsWrap.addView(tv)
            }
        }
    }

    private fun submitProfile() {
        val ctx = requireContext()

        Log.d(
            "FlexiFitSubmit",
            """
            age=${OnboardingStore.getInt(ctx, "age", 0)}
            gender=${OnboardingStore.getString(ctx, "gender")}
            height_cm=${OnboardingStore.getInt(ctx, "height_cm", 0)}
            weight_kg=${OnboardingStore.getInt(ctx, "weight_kg", 0)}
            target_weight_kg=${OnboardingStore.getInt(ctx, "target_weight_kg", 0)}
            upper_body_injury=${OnboardingStore.getBoolean(ctx, "upper_body_injury", false)}
            lower_body_injury=${OnboardingStore.getBoolean(ctx, "lower_body_injury", false)}
            joint_problems=${OnboardingStore.getBoolean(ctx, "joint_problems", false)}
            short_breath=${OnboardingStore.getBoolean(ctx, "short_breath", false)}
            health_none=${OnboardingStore.getBoolean(ctx, "health_none", false)}
            fitness_lifestyle=${OnboardingStore.getString(ctx, "fitness_lifestyle")}
            fitness_level=${OnboardingStore.getString(ctx, "fitness_level")}
            environment=${OnboardingStore.getStringSet(ctx, "environment")}
            fitness_goal=${OnboardingStore.getStringSet(ctx, "fitness_goal")}
            bodycomp_goal=${OnboardingStore.getString(ctx, "bodycomp_goal")}
            dietary_type=${OnboardingStore.getString(ctx, "dietary_type")}
            selected_programs=${OnboardingStore.getStringSet(ctx, "selected_programs")}
            """.trimIndent()
        )

        val request = OnboardingProfileRequest(
            age = OnboardingStore.getInt(ctx, "age", 0),
            gender = OnboardingStore.getString(ctx, "gender"),

            heightCm = OnboardingStore.getInt(ctx, "height_cm", 0).toDouble(),
            weightKg = OnboardingStore.getInt(ctx, "weight_kg", 0).toDouble(),
            targetWeightKg = OnboardingStore.getInt(ctx, "target_weight_kg", 0).toDouble(),

            upperBodyInjury = OnboardingStore.getBoolean(ctx, "upper_body_injury", false),
            lowerBodyInjury = OnboardingStore.getBoolean(ctx, "lower_body_injury", false),
            jointProblems = OnboardingStore.getBoolean(ctx, "joint_problems", false),
            shortBreath = OnboardingStore.getBoolean(ctx, "short_breath", false),
            healthNone = OnboardingStore.getBoolean(ctx, "health_none", false),

            activityLevel = OnboardingStore.getString(ctx, "fitness_lifestyle"),
            fitnessLevel = OnboardingStore.getString(ctx, "fitness_level"),

            environment = OnboardingStore.getStringSet(ctx, "environment").toList(),
            fitnessGoals = OnboardingStore.getStringSet(ctx, "fitness_goal").toList(),

            bodyGoal = OnboardingStore.getString(ctx, "bodycomp_goal"),
            dietType = OnboardingStore.getString(ctx, "dietary_type"),

            selectedPrograms = OnboardingStore.getStringSet(ctx, "selected_programs").toList()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.get(ctx).create(MobileApi::class.java)
                val res = api.submitProfile(request)

                if (res.isSuccessful) {
                    Toast.makeText(ctx, "Profile submitted successfully", Toast.LENGTH_SHORT).show()
                    (activity as? OnboardingActivity)?.goToMain()
                } else {
                    val errorBody = res.errorBody()?.string().orEmpty()
                    Log.e("FlexiFitSubmit", "HTTP ${res.code()} | $errorBody")
                    Toast.makeText(
                        ctx,
                        "Submit failed: HTTP ${res.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("FlexiFitSubmit", "Network error", e)
                Toast.makeText(
                    ctx,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}