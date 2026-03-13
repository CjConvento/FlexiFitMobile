package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg8ProgramsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg8_program,
    nextActionId = R.id.a9 // Ito na yung final submit action mo
) {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ProgramCardAdapter
    private val selectedPrograms = linkedSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvprogramgoal)

        // 1. Load Data with Fail-fast logic
        val goals = loadGoals()
        val hasRehab = goals.contains(Goal.REHAB)
        val isRehabOnly = goals.size == 1 && hasRehab

        // 2. Save UI flags to store
        OnboardingStore.putBoolean(requireContext(), FlexiFitKeys.IS_REHAB_USER, hasRehab)

        // 3. Process Level Hierarchy
        val rawLevel = OnboardingStore.getString(requireContext(), FlexiFitKeys.FITNESS_LEVEL)

        // Fail-fast debug check
        if (rawLevel.isBlank()) {
            showBannerIfExists(view, "DEBUG: Fitness Level not found in store!")
        }

        val finalLevelEnum = if (isRehabOnly) {
            Level.BEGINNER
        } else {
            parseToLevelEnum(rawLevel)
        }

        // 4. Generate Programs base sa logic (Rule Engine)
        val inputs = ProgramInputs(
            goals = goals,
            locations = loadLocations(),
            level = finalLevelEnum,
            safety = loadSafety()
        )

        // 4. Generate Programs base sa logic (Rule Engine)
        val programs = ProgramRuleEngine.generate(inputs) // List<String> ito

        // Dahil List<String> na ito, convert lang natin sa Set para sa validation
        val validGeneratedIds = programs.toSet()

        // 5. HYDRATION: Restore saved programs
        val savedSelections = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.SELECTED_PROGRAMS)

        // 5.5 SYNC LOGIC: I-filter ang stored selections laban sa generated strings
        selectedPrograms.clear()
        val cleanedSelections = savedSelections.filter { it in validGeneratedIds }
        selectedPrograms.addAll(cleanedSelections)

        // I-save agad ang 'cleaned' list para up-to-date ang Store
        OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.SELECTED_PROGRAMS, selectedPrograms)

        // 6. Setup RecyclerView
        if (programs.isEmpty()) {
            showBannerIfExists(view, "No programs available for your current selection.")
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 1)
        adapter = ProgramCardAdapter(
            items = programs,
            selected = selectedPrograms,
            onToggle = { programId, isChecked ->
                if (isChecked) {
                    selectedPrograms.add(programId)
                } else {
                    selectedPrograms.remove(programId)
                }

                // ✅ AUTOSAVE
                OnboardingStore.putStringSet(requireContext(), FlexiFitKeys.SELECTED_PROGRAMS, selectedPrograms)
            },
            isLocked = false // <--- DAGDAGAN MO ITO BABE PARA MAWALA ANG ERROR
        )
        rv.adapter = adapter
    }

    private fun loadGoals(): Set<Goal> {
        // Updated to use FlexiFitKeys.FITNESS_GOALS (with S) para consistent sa Pg5
        val raw = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)
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
        val raw = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)
        return raw.mapNotNull { s ->
            when (s.trim().lowercase()) {
                "gym" -> Location.GYM
                "home" -> Location.HOME
                "outdoor" -> Location.OUTDOOR
                else -> null
            }
        }.toSet()
    }

    private fun loadSafety(): HealthSafety {
        val ctx = requireContext()
        val upper = OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY)
        val lower = OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY)
        val joint = OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS)
        val breath = OnboardingStore.getBoolean(ctx, FlexiFitKeys.SHORT_BREATH)

        return when {
            joint -> HealthSafety.JOINT_PROBLEM
            upper && lower -> HealthSafety.JOINT_PROBLEM
            upper -> HealthSafety.UPPER_INJURY
            lower -> HealthSafety.LOWER_INJURY
            breath -> HealthSafety.SHORT_BREATH
            else -> HealthSafety.NONE
        }
    }

    private fun parseToLevelEnum(levelStr: String): Level {
        return when (levelStr.uppercase()) {
            "INTERMEDIATE" -> Level.INTERMEDIATE
            "ADVANCED" -> Level.ADVANCED
            else -> Level.BEGINNER
        }
    }

    private fun showBannerIfExists(view: View, message: String) {
        val tv = view.findViewById<TextView?>(R.id.tvProgramWarning)
        tv?.apply {
            visibility = View.VISIBLE
            text = message
        } ?: Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun validateBeforeNext(): String? {
        val selected = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.SELECTED_PROGRAMS)
        return if (selected.isEmpty()) "Please select at least one program to continue." else null
    }
}