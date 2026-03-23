package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class Pg8ProgramsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg8_program,
) {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ProgramCardAdapter
    private val selectedPrograms = linkedSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FLEXIFIT_DEBUG", "--- Page 8: Recommendations ---")

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val ctx = requireContext()

        // 1. INITIALIZE UI
        val tvWarning = view.findViewById<TextView>(R.id.tvProgramWarning)
        rv = view.findViewById(R.id.rvprogramgoal)

        // Load data from appropriate source
        val goals = loadGoals(isUpdate)
        val locations = loadLocations(isUpdate)
        val safety = loadSafety(isUpdate)
        val rawLevel = if (isUpdate) {
            UserPrefs.getString(ctx, "fitness_level", "Beginner")
        } else {
            OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL)
        }

        // 3. WARNING LOGIC: Hide if NONE, Show if injured
        if (safety == HealthSafety.NONE) {
            tvWarning.visibility = View.GONE
        } else {
            tvWarning.visibility = View.VISIBLE
            tvWarning.text = when (safety) {
                HealthSafety.JOINT_PROBLEM -> "⚠️ Joint concerns detected: Selection locked to Rehab for your safety."
                HealthSafety.UPPER_INJURY -> "💡 Note: Programs are modified to be Upper-Body Injury Safe."
                HealthSafety.LOWER_INJURY -> "💡 Note: Programs are modified to be Lower-Body Injury Safe."
                HealthSafety.SHORT_BREATH -> "🌬️ Note: Programs are set to a lighter pace for breathing comfort."
                else -> ""
            }
        }

        // 4. GENERATE PROGRAMS (Rule Engine)
        val isRehabOnly = goals.size == 1 && goals.contains(Goal.REHAB)
        val finalLevelEnum = if (isRehabOnly) Level.BEGINNER else parseToLevelEnum(rawLevel)

        val inputs = ProgramInputs(goals, locations, finalLevelEnum, safety)
        val generatedPrograms = ProgramRuleEngine.generate(inputs)

        // 5. SYNC SELECTIONS (Clear old selections that don't match new generated programs)
        val savedSelections = if (isUpdate) {
            UserPrefs.getStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS) ?: emptySet()
        } else {
            OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS)
        }
        selectedPrograms.clear()
        selectedPrograms.addAll(savedSelections.filter { it in generatedPrograms })

        if (isUpdate) {
            UserPrefs.putStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS, selectedPrograms)
        } else {
            OnboardingStore.putStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS, selectedPrograms)
        }

        // 6. SETUP RECYCLERVIEW
        rv.layoutManager = GridLayoutManager(ctx, 1)

        // If JOINT_PROBLEM, lock selection (optional logic)
        val isLocked = (safety == HealthSafety.JOINT_PROBLEM)

        adapter = ProgramCardAdapter(
            items = generatedPrograms,
            selected = selectedPrograms,
            isLocked = isLocked,
            onToggle = { programId, isChecked ->
                if (isChecked) selectedPrograms.add(programId) else selectedPrograms.remove(programId)
                if (isUpdate) {
                    UserPrefs.putStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS, selectedPrograms)
                } else {
                    OnboardingStore.putStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS, selectedPrograms)
                }
            },
            onLimitReached = {
                Toast.makeText(ctx, "You can only select up to 4 programs.", Toast.LENGTH_SHORT).show()
            }
        )
        rv.adapter = adapter
    }

    // Helper to map strings to Enums
    private fun loadGoals(isUpdate: Boolean): Set<Goal> {
        val ctx = requireContext()
        val goalStrings = if (isUpdate) {
            UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET) ?: emptySet()
        } else {
            OnboardingStore.getStringSet(ctx, FlexiFitKeys.FITNESS_GOALS)
        }
        return goalStrings.mapNotNull { s ->
            when (s.lowercase()) {
                "cardio" -> Goal.CARDIO
                "muscle_gain" -> Goal.MUSCLE_GAIN
                "rehab" -> Goal.REHAB
                else -> null
            }
        }.toSet()
    }

    private fun loadLocations(isUpdate: Boolean): Set<Location> {
        val ctx = requireContext()
        val envStrings = if (isUpdate) {
            UserPrefs.getStringSet(ctx, "environment") ?: emptySet()
        } else {
            OnboardingStore.getStringSet(ctx, FlexiFitKeys.ENVIRONMENT)
        }
        return envStrings.mapNotNull { s ->
            when (s.lowercase()) {
                "gym" -> Location.GYM
                "home" -> Location.HOME
                "outdoor" -> Location.OUTDOOR
                else -> null
            }
        }.toSet()
    }

    private fun loadSafety(isUpdate: Boolean): HealthSafety {
        val ctx = requireContext()
        val upper = if (isUpdate) {
            UserPrefs.getBool(ctx, "upper_body_injury", false)
        } else {
            OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY)
        }
        val lower = if (isUpdate) {
            UserPrefs.getBool(ctx, "lower_body_injury", false)
        } else {
            OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY)
        }
        val joint = if (isUpdate) {
            UserPrefs.getBool(ctx, "joint_problems", false)
        } else {
            OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS)
        }
        return when {
            joint || (upper && lower) -> HealthSafety.JOINT_PROBLEM
            upper -> HealthSafety.UPPER_INJURY
            lower -> HealthSafety.LOWER_INJURY
            else -> HealthSafety.NONE
        }
    }

    private fun parseToLevelEnum(lvl: String) = when (lvl.uppercase()) {
        "INTERMEDIATE" -> Level.INTERMEDIATE
        "ADVANCED" -> Level.ADVANCED
        else -> Level.BEGINNER
    }

    override fun validateBeforeNext(): String? =
        if (selectedPrograms.isEmpty()) "Please select at least one program." else null
}