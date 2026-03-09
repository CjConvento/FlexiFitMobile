package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        val btnFinish = view.findViewById<MaterialButton>(R.id.btnFinish)

        btnFinish.setOnClickListener {
            submitProfile()
        }
    }

    private fun submitProfile() {
        val ctx = requireContext()

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
                    (activity as? OnboardingActivity)?.goToMain()
                } else {
                    Toast.makeText(ctx, "Failed to submit profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}