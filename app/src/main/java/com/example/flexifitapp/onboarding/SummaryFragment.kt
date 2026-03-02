package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.google.android.material.button.MaterialButton

class SummaryFragment : Fragment(R.layout.obd_fragment_summary) {

    // Match your constants (from your message)
    private val KEY_LIFESTYLE = "fitness_lifestyle"
    private val KEY_LEVEL = "fitness_level"
    private val KEY_ENVIRONMENT = "environment"
    private val KEY_FITNESS_GOAL = "fitness_goal"      // StringSet
    private val KEY_BODYCOMP_GOAL = "bodycomp_goal"
    private val KEY_DIET = "dietary_type"
    private val KEY_PROGRAMS = "selected_programs"     // StringSet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()

        val tvSumAge = view.findViewById<TextView>(R.id.tvSumAge)
        val tvSumGender = view.findViewById<TextView>(R.id.tvSumGender)
        val tvSumHeight = view.findViewById<TextView>(R.id.tvSumHeight)
        val tvSumWeight = view.findViewById<TextView>(R.id.tvSumWeight)

        // These are optional (only if you added them in the summary layout)
        val tvSumHealth = view.findViewById<TextView?>(R.id.tvSumHealth)
        val tvSumLifestyle = view.findViewById<TextView?>(R.id.tvSumLifestyle)
        val tvSumLevel = view.findViewById<TextView?>(R.id.tvSumLevel)
        val tvSumGoal = view.findViewById<TextView?>(R.id.tvSumGoal)
        val tvSumBodyComp = view.findViewById<TextView?>(R.id.tvSumBodyComp)
        val tvSumDiet = view.findViewById<TextView?>(R.id.tvSumDiet)

        val programsWrap = view.findViewById<LinearLayout>(R.id.programsWrap)
        val tvProgramsEmpty = view.findViewById<TextView>(R.id.tvProgramsEmpty)
        val btnFinish = view.findViewById<MaterialButton>(R.id.btnFinish)

        // ===== render basic values =====
        val age = OnboardingStore.getInt(ctx, "age", 0)
        val gender = OnboardingStore.getString(ctx, "gender")
        val height = OnboardingStore.getInt(ctx, "height_cm", 0)
        val weight = OnboardingStore.getInt(ctx, "weight_kg", 0)

        tvSumAge.text = if (age > 0) age.toString() else "-"
        tvSumGender.text = if (gender.isNotBlank()) gender else "-"
        tvSumHeight.text = if (height > 0) height.toString() else "-"
        tvSumWeight.text = if (weight > 0) weight.toString() else "-"

        // ===== render optional values =====
        val injury = OnboardingStore.getString(ctx, "has_injury")
        val medical = OnboardingStore.getString(ctx, "has_medical_condition")
        tvSumHealth?.text = when {
            injury.isBlank() && medical.isBlank() -> "-"
            else -> "Injury: ${injury.ifBlank { "-" }} | Medical: ${medical.ifBlank { "-" }}"
        }

        tvSumLifestyle?.text = OnboardingStore.getString(ctx, KEY_LIFESTYLE).ifBlank { "-" }
        tvSumLevel?.text = OnboardingStore.getString(ctx, KEY_LEVEL).ifBlank { "-" }

        val goals = OnboardingStore.getStringSet(ctx, KEY_FITNESS_GOAL)
        tvSumGoal?.text = if (goals.isEmpty()) "-" else goals.joinToString(", ")

        tvSumBodyComp?.text = OnboardingStore.getString(ctx, KEY_BODYCOMP_GOAL).ifBlank { "-" }
        tvSumDiet?.text = OnboardingStore.getString(ctx, KEY_DIET).ifBlank { "-" }

        // ===== Programs (PG8) =====
        val programs = OnboardingStore.getStringSet(ctx, KEY_PROGRAMS)
        programsWrap.removeAllViews()

        if (programs.isEmpty()) {
            tvProgramsEmpty.visibility = View.VISIBLE
        } else {
            tvProgramsEmpty.visibility = View.GONE

            programs.forEach { name ->
                // If item_program_summary exists, you can inflate it.
                // If not, we create a simple bordered TextView.
                val row = try {
                    val v = layoutInflater.inflate(R.layout.item_program_summary, programsWrap, false)
                    v.findViewById<TextView>(R.id.tvProgramTitle).text = name
                    v.findViewById<TextView>(R.id.tvProgramMeta).text = "Recommended based on your answers"
                    v
                } catch (_: Exception) {
                    TextView(ctx).apply {
                        text = name
                        setPadding(24, 16, 24, 16)
                        setBackgroundResource(R.drawable.flexifit_border)
                    }
                }

                programsWrap.addView(row)
            }
        }

        // ===== finish logic =====
        btnFinish.setOnClickListener {
            val missing = getMissingRequiredFields()

            if (missing.isNotEmpty()) {
                AlertDialog.Builder(ctx)
                    .setTitle("Incomplete Information")
                    .setMessage(
                        "Please complete all the required pages to proceed.\n\nMissing:\n- " +
                                missing.joinToString("\n- ")
                    )
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            commitToUserPrefs()
            (activity as? OnboardingActivity)?.goToMain()
        }
    }

    private fun commitToUserPrefs() {
        val ctx = requireContext()

        // Basic
        UserPrefs.putInt(ctx, UserPrefs.KEY_AGE, OnboardingStore.getInt(ctx, "age", 0))
        UserPrefs.putString(ctx, UserPrefs.KEY_GENDER, OnboardingStore.getString(ctx, "gender"))
        UserPrefs.putInt(ctx, UserPrefs.KEY_HEIGHT_CM, OnboardingStore.getInt(ctx, "height_cm", 0))
        UserPrefs.putInt(ctx, UserPrefs.KEY_WEIGHT_KG, OnboardingStore.getInt(ctx, "weight_kg", 0))

        // Health
        UserPrefs.putString(ctx, UserPrefs.KEY_HAS_INJURY, OnboardingStore.getString(ctx, "has_injury"))
        UserPrefs.putString(ctx, UserPrefs.KEY_HAS_MEDICAL_CONDITION, OnboardingStore.getString(ctx, "has_medical_condition"))

        // Background
        UserPrefs.putString(ctx, UserPrefs.KEY_FITNESS_LIFESTYLE, OnboardingStore.getString(ctx, KEY_LIFESTYLE))
        UserPrefs.putInt(ctx, UserPrefs.KEY_FITNESS_LIFESTYLE_INDEX, OnboardingStore.getInt(ctx, "fitness_lifestyle_index", 0))
        UserPrefs.putString(ctx, UserPrefs.KEY_FITNESS_LEVEL, OnboardingStore.getString(ctx, KEY_LEVEL))
        UserPrefs.putInt(ctx, UserPrefs.KEY_FITNESS_LEVEL_INDEX, OnboardingStore.getInt(ctx, "fitness_level_index", 0))

        // Environment
        UserPrefs.putString(ctx, UserPrefs.KEY_ENVIRONMENT, OnboardingStore.getString(ctx, KEY_ENVIRONMENT))

        // Goals
        UserPrefs.putStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET, OnboardingStore.getStringSet(ctx, KEY_FITNESS_GOAL))
        UserPrefs.putString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, OnboardingStore.getString(ctx, KEY_BODYCOMP_GOAL))

        // Diet
        UserPrefs.putString(ctx, UserPrefs.KEY_DIETARY_TYPE, OnboardingStore.getString(ctx, KEY_DIET))

        // Programs
        UserPrefs.putStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS, OnboardingStore.getStringSet(ctx, KEY_PROGRAMS))

        // Mark done
        UserPrefs.setOnboardingDone(ctx, true)
    }

    private fun getMissingRequiredFields(): List<String> {
        val ctx = requireContext()
        val missing = mutableListOf<String>()

        fun reqInt(key: String, label: String) {
            val v = OnboardingStore.getInt(ctx, key, 0)
            if (v <= 0) missing += label
        }

        fun reqStr(key: String, label: String) {
            val v = OnboardingStore.getString(ctx, key)
            if (v.isBlank()) missing += label
        }

        fun reqSet(key: String, label: String) {
            val v = OnboardingStore.getStringSet(ctx, key)
            if (v.isEmpty()) missing += label
        }

        // PG1
        reqInt("age", "PG1 - Age")
        val g = OnboardingStore.getString(ctx, "gender")
        if (g != "Male" && g != "Female") missing += "PG1 - Gender"

        // PG1.5
        reqInt("height_cm", "PG1.5 - Height")
        reqInt("weight_kg", "PG1.5 - Weight")

        // PG2
        reqStr("has_injury", "PG2 - Injury status")
        reqStr("has_medical_condition", "PG2 - Medical condition status")

        // PG3
        reqStr(KEY_LIFESTYLE, "PG3 - Daily lifestyle")
        reqStr(KEY_LEVEL, "PG3 - Fitness level")

        // PG4
        reqStr(KEY_ENVIRONMENT, "PG4 - Workout environment")

        // PG5
        reqSet(KEY_FITNESS_GOAL, "PG5 - Fitness goal")

        // PG6
        reqStr(KEY_BODYCOMP_GOAL, "PG6 - Body composition goal")

        // PG7
        reqStr(KEY_DIET, "PG7 - Diet type")

        // PG8
        reqSet(KEY_PROGRAMS, "PG8 - Program selection")

        return missing
    }
}