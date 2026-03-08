package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.google.android.material.snackbar.Snackbar

class Pg8ProgramsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg8_program,
    nextActionId = R.id.a9
) {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ProgramCardAdapter

    private val selectedPrograms = linkedSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvprogramgoal)

        selectedPrograms.clear()
        selectedPrograms.addAll(
            OnboardingStore.getStringSet(requireContext(), KEY_SELECTED_PROGRAMS)
        )

        val inputs = ProgramInputs(
            goals = loadGoals(),
            locations = loadLocations(),
            level = loadLevel(),
            safety = loadSafety()
        )

        val programs = ProgramRuleEngine.generate(inputs)

        if (programs.isEmpty()) {
            showBannerIfExists(
                view,
                "No programs generated. Please go back and complete your goals, location, and health selections."
            )
        }

        if (inputs.safety == HealthSafety.JOINT_PROBLEM) {
            selectedPrograms.clear()
            selectedPrograms.addAll(programs)
            OnboardingStore.putStringSet(requireContext(), KEY_SELECTED_PROGRAMS, selectedPrograms)
            showBannerIfExists(view, "Since you have joint problems, Rehab Program is required.")
        } else if (inputs.safety == HealthSafety.SHORT_BREATH) {
            showBannerIfExists(view, "Short breath selected: choose intensity level carefully.")
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = ProgramCardAdapter(
            items = programs,
            selected = selectedPrograms,
            onToggle = { programName, isSelected ->
                if (isSelected) selectedPrograms.add(programName) else selectedPrograms.remove(programName)
                OnboardingStore.putStringSet(requireContext(), KEY_SELECTED_PROGRAMS, selectedPrograms)
            },
            isLocked = (inputs.safety == HealthSafety.JOINT_PROBLEM)
        )

        rv.adapter = adapter
        rv.setHasFixedSize(false)
    }

    override fun validateBeforeNext(): String? {
        return when {
            ProgramRuleEngine.generate(
                ProgramInputs(loadGoals(), loadLocations(), loadLevel(), loadSafety())
            ).isEmpty() -> "No programs available. Please go back and complete your goals, location, and health selections."

            selectedPrograms.isEmpty() -> "Please select at least one program to proceed."
            else -> null
        }
    }

    private fun loadSafety(): HealthSafety {
        val ctx = requireContext()

        val none = OnboardingStore.getBoolean(ctx, "health_none", false)
        val joint = OnboardingStore.getBoolean(ctx, "joint_problems", false)
        val shortBreath = OnboardingStore.getBoolean(ctx, "short_breath", false)
        val upper = OnboardingStore.getBoolean(ctx, "upper_body_injury", false)
        val lower = OnboardingStore.getBoolean(ctx, "lower_body_injury", false)

        return when {
            none -> HealthSafety.NONE
            joint -> HealthSafety.JOINT_PROBLEM
            shortBreath -> HealthSafety.SHORT_BREATH
            upper -> HealthSafety.UPPER_INJURY
            lower -> HealthSafety.LOWER_INJURY
            else -> HealthSafety.NONE
        }
    }

    private fun loadLevel(): Level {
        val raw = OnboardingStore.getString(requireContext(), "fitness_level").trim()

        return when (raw.uppercase()) {
            "BEGINNER" -> Level.BEGINNER
            "INTERMEDIATE" -> Level.INTERMEDIATE
            "ADVANCED" -> Level.ADVANCED
            else -> Level.BEGINNER
        }
    }

    private fun loadGoals(): Set<Goal> {
        val raw = OnboardingStore.getStringSet(requireContext(), "fitness_goal")
        if (raw.isEmpty()) return emptySet()

        return raw.mapNotNull { s ->
            when (s.trim().lowercase()) {
                "cardio" -> Goal.CARDIO
                "muscle_gain" -> Goal.MUSCLE_GAIN
                "rehab" -> Goal.REHAB
                else -> null
            }
        }.toSet()
    }

    private fun loadLocations(): Set<Location> {
        val raw = OnboardingStore.getStringSet(requireContext(), "environment")
        if (raw.isEmpty()) return emptySet()

        return raw.mapNotNull { s ->
            when (s.trim().lowercase()) {
                "gym" -> Location.GYM
                "home" -> Location.HOME
                "outdoor" -> Location.OUTDOOR
                else -> null
            }
        }.toSet()
    }

    private fun showBannerIfExists(view: View, message: String) {
        val tv = view.findViewById<TextView?>(R.id.tvProgramWarning)
        if (tv != null) {
            tv.visibility = View.VISIBLE
            tv.text = message
        } else {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val KEY_SELECTED_PROGRAMS = "selected_programs"
    }
}