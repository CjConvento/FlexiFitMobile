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

        // 1) Restore selection
        selectedPrograms.clear()
        selectedPrograms.addAll(
            OnboardingStore.getStringSet(requireContext(), "selected_programs")
        )

        // 2) Build inputs
        val inputs = ProgramInputs(
            goals = loadGoals(),
            locations = loadLocations(),
            level = loadLevel(),
            safety = loadSafety()
        )

        // 3) Generate programs
        val programs = ProgramRuleEngine.generate(inputs)

        // Optional edge: no generated programs
        if (programs.isEmpty()) {
            showBannerIfExists(view, "No programs generated. Please go back and select goals and locations.")
        }

        // 4) Joint problem rule
        if (inputs.safety == HealthSafety.JOINT_PROBLEM) {
            selectedPrograms.clear()
            selectedPrograms.addAll(programs)
            OnboardingStore.putStringSet(requireContext(), "selected_programs", selectedPrograms)
            showBannerIfExists(view, "Since you have joint problems, Rehab Program is required.")
        } else if (inputs.safety == HealthSafety.SHORT_BREATH) {
            showBannerIfExists(view, "Short breath selected: choose intensity level carefully.")
        }

        // 5) RecyclerView setup (✅ GRID)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = ProgramCardAdapter(
            items = programs,
            selected = selectedPrograms,
            onToggle = { programName, isSelected ->
                if (isSelected) selectedPrograms.add(programName) else selectedPrograms.remove(programName)
                OnboardingStore.putStringSet(requireContext(), "selected_programs", selectedPrograms)
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
            ).isEmpty() -> "No programs available. Please go back and select goals and locations."

            selectedPrograms.isEmpty() -> "Please select at least one program to proceed."
            else -> null
        }
    }

    // ------- Loaders -------

    private fun loadSafety(): HealthSafety {
        val raw = OnboardingStore.getString(requireContext(), "health_safety").trim()
        return when (raw) {
            "UPPER_INJURY" -> HealthSafety.UPPER_INJURY
            "LOWER_INJURY" -> HealthSafety.LOWER_INJURY
            "JOINT_PROBLEM" -> HealthSafety.JOINT_PROBLEM
            "SHORT_BREATH" -> HealthSafety.SHORT_BREATH
            else -> HealthSafety.NONE
        }
    }

    private fun loadLevel(): Level {
        val raw = OnboardingStore.getString(requireContext(), "fitness_level").trim()
        return when (raw) {
            "BEGINNER" -> Level.BEGINNER
            "INTERMEDIATE" -> Level.INTERMEDIATE
            "ADVANCED" -> Level.ADVANCED
            else -> Level.BEGINNER
        }
    }

    private fun loadGoals(): Set<Goal> {
        val raw = OnboardingStore.getStringSet(requireContext(), "selected_goals")
        if (raw.isEmpty()) return emptySet()
        return raw.mapNotNull { s ->
            when (s) {
                "CARDIO" -> Goal.CARDIO
                "MUSCLE_GAIN" -> Goal.MUSCLE_GAIN
                "REHAB" -> Goal.REHAB
                else -> null
            }
        }.toSet()
    }

    private fun loadLocations(): Set<Location> {
        val raw = OnboardingStore.getStringSet(requireContext(), "selected_locations")
        if (raw.isEmpty()) return emptySet()
        return raw.mapNotNull { s ->
            when (s) {
                "GYM" -> Location.GYM
                "HOME" -> Location.HOME
                "OUTDOOR" -> Location.OUTDOOR
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
}