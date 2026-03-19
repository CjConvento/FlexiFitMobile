package com.example.flexifitapp.onboarding

import android.util.Log

enum class Goal { CARDIO, MUSCLE_GAIN, REHAB }
enum class Level { BEGINNER, INTERMEDIATE, ADVANCED }
enum class Location { GYM, HOME, OUTDOOR }
enum class HealthSafety { NONE, UPPER_INJURY, LOWER_INJURY, JOINT_PROBLEM, SHORT_BREATH }

data class ProgramInputs(
    val goals: Set<Goal>,
    val locations: Set<Location>,
    val level: Level,
    val safety: HealthSafety
)

object ProgramRuleEngine {

    fun generate(inputs: ProgramInputs): List<String> {
        Log.d("FLEXIFIT_DEBUG", "--- Rule Engine: Processing Inputs ---")
        Log.d("FLEXIFIT_DEBUG", "Goals: ${inputs.goals}, Safety: ${inputs.safety}, Level: ${inputs.level}")

        // 1. JOINT/CRITICAL SAFETY CHECK: Force Rehab regardless of goals
        if (inputs.safety == HealthSafety.JOINT_PROBLEM) {
            Log.w("FLEXIFIT_DEBUG", "Joint Problem Detected: Forcing Rehab-only programs.")
            return inputs.locations.map { loc ->
                "Rehab ${locText(loc)} Program"
            }.distinct()
        }

        // 2. SAFETY PREFIX LOGIC
        val safetyPrefix = when (inputs.safety) {
            HealthSafety.UPPER_INJURY -> "Upper Body Injury Safe"
            HealthSafety.LOWER_INJURY -> "Lower Body Injury Safe"
            HealthSafety.SHORT_BREATH -> "Light" // Special tag para sa breath issues
            else -> ""
        }

        val levelText = when (inputs.level) {
            Level.BEGINNER -> "Beginner"
            Level.INTERMEDIATE -> "Intermediate"
            Level.ADVANCED -> "Advanced"
        }

        val cards = mutableListOf<String>()

        // 3. GENERATION LOOP
        for (goal in inputs.goals) {
            for (loc in inputs.locations) {
                val locT = locText(loc)

                val programName = when {
                    // Rehab Path
                    goal == Goal.REHAB -> {
                        if (safetyPrefix.isBlank()) "Rehab $locT Program"
                        else "$safetyPrefix Rehab ($locT)"
                    }

                    // Cardio/Muscle Path
                    else -> {
                        val gName = goalName(goal)
                        if (safetyPrefix.isBlank()) {
                            // Format: "Muscle Gain Beginner Gym Program"
                            "$gName $levelText $locT Program"
                        } else {
                            // Format: "Upper Body Injury Safe Muscle Gain Beginner (Gym)"
                            "$safetyPrefix $gName $levelText ($locT)"
                        }
                    }
                }
                cards.add(programName)
            }
        }

        val result = cards.distinct()
        Log.d("FLEXIFIT_DEBUG", "Engine Generated ${result.size} programs.")
        return result
    }

    private fun goalName(goal: Goal): String = when (goal) {
        Goal.CARDIO -> "Cardio"
        Goal.MUSCLE_GAIN -> "Muscle Gain"
        Goal.REHAB -> "Rehab"
    }

    private fun locText(loc: Location) = when (loc) {
        Location.GYM -> "Gym"
        Location.HOME -> "Home"
        Location.OUTDOOR -> "Outdoor"
    }
}