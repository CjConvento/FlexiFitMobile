package com.example.flexifitapp.onboarding

import android.util.Log

data class ProgramInfo(
    val category: String,
    val level: String,
    val environment: String,
    val rawName: String
)

object ProgramNameParser {

    fun parse(name: String): ProgramInfo {
        val trimmed = name.trim().replace(Regex("\\s+"), " ")
        Log.d("FLEXIFIT_DEBUG", "Parsing Program: '$trimmed'")

        // 1. INJURY SAFE CHECK (Format: "Upper Body Injury Safe Muscle Gain Beginner (Gym)")
        val injuryPrefix1 = "Upper Body Injury Safe"
        val injuryPrefix2 = "Lower Body Injury Safe"

        if (trimmed.startsWith(injuryPrefix1) || trimmed.startsWith(injuryPrefix2)) {
            val safetyLabel = if (trimmed.startsWith(injuryPrefix1)) "Upper body injury-safe" else "Lower body injury-safe"

            // Alisin ang prefix at kunin ang environment sa loob ng ()
            val content = trimmed.removePrefix(injuryPrefix1).removePrefix(injuryPrefix2).trim()
            val env = extractParen(content) ?: "Unknown"

            // Alisin ang (Gym) part para makuha ang Category at Level
            val mainBody = content.replace("($env)", "").trim()
            val parts = mainBody.split(" ").filter { it.isNotBlank() }

            val (category, level) = parseCategoryAndLevel(parts)

            return ProgramInfo(category, level, env, safetyLabel).also { logResult(it) }
        }

        // 2. REHAB CHECK (Format: "Rehab Gym Program")
        if (trimmed.startsWith("Rehab", ignoreCase = true)) {
            val parts = trimmed.split(" ").filter { it.isNotBlank() }
            // Karaniwang [Rehab, Gym, Program]
            val env = parts.getOrNull(1) ?: "Unknown"
            return ProgramInfo("Rehab", "Rehab", env, "").also { logResult(it) }
        }

        // 3. NORMAL CHECK (Format: "Muscle Gain Beginner Gym Program" o "Cardio Beginner Home Program")
        val cleanName = trimmed.replace(" Program", "").trim()
        val parts = cleanName.split(" ").filter { it.isNotBlank() }

        // Matalinong pag-extract: Ang dulo ay laging Environment
        val env = parts.lastOrNull() ?: "Unknown"
        // Ang natitirang parts bago ang huli ay Category at Level
        val remainingParts = parts.dropLast(1)

        val (category, level) = parseCategoryAndLevel(remainingParts)

        return ProgramInfo(category, level, env, "").also { logResult(it) }
    }

    private fun parseCategoryAndLevel(parts: List<String>): Pair<String, String> {
        return when {
            // Case: ["Muscle", "Gain", "Beginner"]
            isMuscleGainTwoWords(parts) -> {
                val level = parts.getOrNull(2) ?: "Beginner"
                "Muscle Gain" to level
            }
            // Case: ["Cardio", "Intermediate"]
            parts.size >= 2 -> {
                val category = normalizeCategory(parts[0])
                val level = parts[1]
                category to level
            }
            // Fallback
            else -> {
                val category = normalizeCategory(parts.getOrNull(0) ?: "General")
                category to "Beginner"
            }
        }
    }

    private fun isMuscleGainTwoWords(parts: List<String>): Boolean {
        return parts.size >= 2 &&
                parts[0].equals("Muscle", ignoreCase = true) &&
                parts[1].equals("Gain", ignoreCase = true)
    }

    private fun normalizeCategory(raw: String): String {
        return when (raw.lowercase()) {
            "musclegain", "muscle_gain" -> "Muscle Gain"
            "cardio" -> "Cardio"
            "rehab" -> "Rehab"
            else -> raw.replaceFirstChar { it.uppercase() }
        }
    }

    private fun extractParen(s: String): String? {
        val start = s.indexOf('(')
        val end = s.indexOf(')')
        if (start >= 0 && end > start) return s.substring(start + 1, end).trim()
        return null
    }

    private fun logResult(info: ProgramInfo) {
        Log.d("FLEXIFIT_DEBUG", "Parsed Result -> Cat: ${info.category}, Lvl: ${info.level}, Env: ${info.environment}")
    }
}