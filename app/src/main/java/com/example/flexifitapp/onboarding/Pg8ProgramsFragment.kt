package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg8ProgramsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg8_program,
    nextActionId = R.id.a9
) {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ProgramCardAdapter
    private val selectedPrograms = linkedSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FLEXIFIT_DEBUG", "--- Page 8: Recommendations ---")

        // 1. INITIALIZE UI
        rv = view.findViewById(R.id.rvprogramgoal)
        val tvWarning = view.findViewById<TextView>(R.id.tvProgramWarning) //

        // 2. LOAD DATA & SAFETY
        val ctx = requireContext()
        val goals = loadGoals()
        val locations = loadLocations()
        val safety = loadSafety() //
        val rawLevel = OnboardingStore.getString(ctx, FlexiFitKeys.FITNESS_LEVEL)

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

        // 5. SYNC SELECTIONS (Linisin ang dati kung hindi na match sa bago)
        val savedSelections = OnboardingStore.getStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS)
        selectedPrograms.clear()
        selectedPrograms.addAll(savedSelections.filter { it in generatedPrograms })
        OnboardingStore.putStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS, selectedPrograms)

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
                OnboardingStore.putStringSet(ctx, FlexiFitKeys.SELECTED_PROGRAMS, selectedPrograms)
            },
            onLimitReached = {
                Toast.makeText(ctx, "You can only select up to 4 programs.", Toast.LENGTH_SHORT).show()
            }
        )
        rv.adapter = adapter
    }

    // Helper to map strings to Enums
    private fun loadGoals(): Set<Goal> = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.FITNESS_GOALS)
        .mapNotNull { s -> when(s.lowercase()) { "cardio" -> Goal.CARDIO; "muscle_gain" -> Goal.MUSCLE_GAIN; "rehab" -> Goal.REHAB; else -> null } }.toSet()

    private fun loadLocations(): Set<Location> = OnboardingStore.getStringSet(requireContext(), FlexiFitKeys.ENVIRONMENT)
        .mapNotNull { s -> when(s.lowercase()) { "gym" -> Location.GYM; "home" -> Location.HOME; "outdoor" -> Location.OUTDOOR; else -> null } }.toSet()

    private fun loadSafety(): HealthSafety {
        val ctx = requireContext()
        val upper = OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY)
        val lower = OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY)
        val joint = OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS)
        return when {
            joint || (upper && lower) -> HealthSafety.JOINT_PROBLEM
            upper -> HealthSafety.UPPER_INJURY
            lower -> HealthSafety.LOWER_INJURY
            else -> HealthSafety.NONE
        }
    }

    private fun parseToLevelEnum(lvl: String) = when(lvl.uppercase()){ "INTERMEDIATE"->Level.INTERMEDIATE; "ADVANCED"->Level.ADVANCED; else->Level.BEGINNER }

    override fun validateBeforeNext(): String? =
        if (selectedPrograms.isEmpty()) "Please select at least one program." else null

    private fun showBannerIfExists(view: View, msg: String) {
        view.findViewById<TextView>(R.id.tvProgramWarning)?.apply { visibility = View.VISIBLE; text = msg }
    }
}