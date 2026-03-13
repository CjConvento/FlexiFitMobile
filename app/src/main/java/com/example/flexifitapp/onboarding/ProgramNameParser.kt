package com.example.flexifitapp.onboarding

data class ProgramInfo(
    val category: String,
    val level: String,
    val environment: String,
    val rawName: String
)

object ProgramNameParser {

    fun parse(name: String): ProgramInfo {
        val trimmed = name.trim().replace(Regex("\\s+"), " ")

        // 1. Injury Safe Check
        val injuryPrefix1 = "Upper Body Injury Safe "
        val injuryPrefix2 = "Lower Body Injury Safe "

        if (trimmed.startsWith(injuryPrefix1) || trimmed.startsWith(injuryPrefix2)) {
            val safety = if (trimmed.startsWith(injuryPrefix1)) "Upper body injury-safe" else "Lower body injury-safe"
            val withoutPrefix = trimmed.removePrefix(injuryPrefix1).removePrefix(injuryPrefix2)

            val env = extractParen(withoutPrefix) ?: "Unknown"
            val main = withoutPrefix.replace("($env)", "").trim()

            if (main.startsWith("Rehab", ignoreCase = true)) {
                return ProgramInfo("Rehab", "Rehab", env, safety)
            }

            val parts = main.split(" ").filter { it.isNotBlank() }
            val (program, level) = parseProgramAndLevel(parts)
            return ProgramInfo(program, level, env, safety)
        }

        // 2. Normal Rehab Check (e.g., "Rehab Gym Program")
        if (trimmed.startsWith("Rehab ", ignoreCase = true)) {
            val env = trimmed
                .replace("Rehab", "", ignoreCase = true)
                .replace("Program", "", ignoreCase = true)
                .trim()
                .ifBlank { "Unknown" }

            return ProgramInfo("Rehab", "Rehab", env, "")
        }

        // 3. Normal Cardio/Muscle Gain (e.g., "Muscle Gain Beginner Gym Program")
        // REVISION: Alisin muna lahat ng fillers para malinis ang parts
        val cleanName = trimmed.replace(" Program", "").trim()
        val parts = cleanName.split(" ").filter { it.isNotBlank() }

        val (program, level) = parseProgramAndLevel(parts)

        // REVISION: Kunin ang huling part bilang environment para flexible kahit 1 or 2 words ang category
        val env = parts.lastOrNull() ?: "Unknown"

        return ProgramInfo(program, level, env, "")
    }

    private fun parseProgramAndLevel(parts: List<String>): Pair<String, String> {
        return if (isMuscleGainTwoWords(parts)) {
            // Index 0: Muscle, Index 1: Gain, Index 2: Level
            val level = parts.getOrNull(2) ?: "Beginner"
            "MuscleGain" to level
        } else {
            // Index 0: Program (Cardio), Index 1: Level
            val program = normalizeProgram(parts.getOrNull(0) ?: "Program")
            val level = parts.getOrNull(1) ?: "Beginner"
            program to level
        }
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
            "musclegain", "muscle_gain", "muscle" -> "MuscleGain"
            "cardio" -> "Cardio"
            "rehab" -> "Rehab"
            else -> raw
        }
    }
}