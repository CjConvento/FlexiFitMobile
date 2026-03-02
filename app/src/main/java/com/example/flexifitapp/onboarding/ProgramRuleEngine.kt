package com.example.flexifitapp.onboarding

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
        // OPTION A: Joint problem forces rehab-only, level hidden/ignored
        if (inputs.safety == HealthSafety.JOINT_PROBLEM) {
            return inputs.locations.mapNotNull { loc ->
                when (loc) {
                    Location.GYM -> "Rehab Gym Program"
                    Location.HOME -> "Rehab Home Program"
                    Location.OUTDOOR -> "Rehab Outdoor Program"
                }
            }.distinct()
        }

        val safetyPrefix = when (inputs.safety) {
            HealthSafety.UPPER_INJURY -> "Upper Body Injury Safe"
            HealthSafety.LOWER_INJURY -> "Lower Body Injury Safe"
            else -> ""
        }

        val levelText = when (inputs.level) {
            Level.BEGINNER -> "Beginner"
            Level.INTERMEDIATE -> "Intermediate"
            Level.ADVANCED -> "Advanced"
        }

        fun locText(loc: Location) = when (loc) {
            Location.GYM -> "Gym"
            Location.HOME -> "Home"
            Location.OUTDOOR -> "Outdoor"
        }

        val cards = mutableListOf<String>()

        for (goal in inputs.goals) {
            for (loc in inputs.locations) {
                val locT = locText(loc)

                val programName = when {
                    // Rehab normal
                    goal == Goal.REHAB && safetyPrefix.isBlank() ->
                        "Rehab $locT Program"

                    // Rehab injury-safe
                    goal == Goal.REHAB && safetyPrefix.isNotBlank() ->
                        "$safetyPrefix Rehab ($locT)"

                    // Cardio/Muscle normal
                    safetyPrefix.isBlank() ->
                        "${goalName(goal)} $levelText $locT Program"

                    // Cardio/Muscle injury-safe
                    else ->
                        "$safetyPrefix ${goalName(goal)} $levelText ($locT)"
                }

                cards.add(programName)
            }
        }

        return cards.distinct()
    }

    private fun goalName(goal: Goal): String = when (goal) {
        Goal.CARDIO -> "Cardio"
        Goal.MUSCLE_GAIN -> "MuscleGain"
        Goal.REHAB -> "Rehab"
    }
}