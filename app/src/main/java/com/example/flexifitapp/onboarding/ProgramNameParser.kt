package com.example.flexifitapp.onboarding

data class ProgramInfo(
    val program: String,
    val level: String,
    val environment: String,
    val safetyNote: String = ""
)

object ProgramNameParser {

    fun parse(name: String): ProgramInfo {
        val trimmed = name.trim().replace(Regex("\\s+"), " ")

        val injuryPrefix1 = "Upper Body Injury Safe "
        val injuryPrefix2 = "Lower Body Injury Safe "

        // =========================
        // 1) Injury-safe formats
        // =========================
        if (trimmed.startsWith(injuryPrefix1) || trimmed.startsWith(injuryPrefix2)) {

            val safety = if (trimmed.startsWith(injuryPrefix1)) {
                "Upper body injury-safe."
            } else {
                "Lower body injury-safe."
            }

            val withoutPrefix = trimmed
                .removePrefix(injuryPrefix1)
                .removePrefix(injuryPrefix2)

            val env = extractParen(withoutPrefix) ?: "Unknown"
            val main = withoutPrefix.replace("($env)", "").trim()

            // injury-safe rehab: "Rehab (Gym)"
            if (main.startsWith("Rehab", ignoreCase = true)) {
                return ProgramInfo(
                    program = "Rehab",
                    level = "Rehab",
                    environment = env,
                    safetyNote = safety
                )
            }

            val parts = main.split(" ").filter { it.isNotBlank() }

            val (program, level) = parseProgramAndLevel(parts)

            return ProgramInfo(
                program = program,
                level = level,
                environment = env,
                safetyNote = safety
            )
        }

        // =========================
        // 2) Normal Rehab formats
        // =========================
        if (trimmed.startsWith("Rehab ", ignoreCase = true)) {
            val env = trimmed
                .removePrefix("Rehab ")
                .removeSuffix(" Program")
                .trim()
                .ifBlank { "Unknown" }

            return ProgramInfo(
                program = "Rehab",
                level = "Rehab",
                environment = env
            )
        }

        // =========================
        // 3) Normal Cardio/MuscleGain formats
        // =========================
        val noSuffix = trimmed.removeSuffix(" Program").trim()
        val parts = noSuffix.split(" ").filter { it.isNotBlank() }

        val (program, level) = parseProgramAndLevel(parts)

        // After program+level, env should be next token if exists
        // Example: Cardio Beginner Gym -> env = Gym
        val envIndex = when {
            isMuscleGainTwoWords(parts) -> 3 // "Muscle Gain" + Level + Env
            else -> 2                        // Program + Level + Env
        }
        val env = parts.getOrNull(envIndex) ?: "Unknown"

        return ProgramInfo(program, level, env)
    }

    // ---- Helpers ----

    private fun parseProgramAndLevel(parts: List<String>): Pair<String, String> {
        if (isMuscleGainTwoWords(parts)) {
            val program = "MuscleGain"
            val level = parts.getOrNull(2) ?: "Beginner"
            return program to level
        }

        val program = normalizeProgram(parts.getOrNull(0) ?: "Program")
        val level = parts.getOrNull(1) ?: "Beginner"
        return program to level
    }

    private fun isMuscleGainTwoWords(parts: List<String>): Boolean {
        return parts.size >= 2 &&
                parts[0].equals("Muscle", ignoreCase = true) &&
                parts[1].equals("Gain", ignoreCase = true)
    }

    private fun extractParen(s: String): String? {
        val start = s.indexOf('(')
        val end = s.indexOf(')')
        if (start >= 0 && end > start) return s.substring(start + 1, end).trim()
        return null
    }

    private fun normalizeProgram(raw: String): String {
        return when (raw.lowercase()) {
            "musclegain", "muscle_gain" -> "MuscleGain"
            "cardio" -> "Cardio"
            "rehab" -> "Rehab"
            "muscle" -> "MuscleGain" // fallback if engine outputs just "Muscle"
            else -> raw
        }
    }
}