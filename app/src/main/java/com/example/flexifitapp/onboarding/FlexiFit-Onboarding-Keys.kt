package com.example.flexifitapp.onboarding

object FlexiFitKeys {

    // --- PG1: PROFILE ---
    const val AGE = "age"
    const val GENDER = "gender"

    // --- PG1.5: METRICS ---
    const val HEIGHT_CM = "height_cm"
    const val WEIGHT_KG = "weight_kg"
    const val TARGET_WEIGHT_KG = "target_weight_kg"

    // --- PG2: HEALTH CHECK ---
    const val UPPER_BODY_INJURY = "upper_body_injury"
    const val LOWER_BODY_INJURY = "lower_body_injury"
    const val JOINT_PROBLEMS = "joint_problems"
    const val SHORT_BREATH = "short_breath"
    const val HEALTH_NONE = "health_none"

    // --- PG3: FITNESS BACKGROUND ---
    const val FITNESS_LIFESTYLE = "fitness_lifestyle"
    // Note: Ito ang magiging "Main Level" base sa hierarchy (Cardio/Muscle > Rehab)
    const val FITNESS_LEVEL = "fitness_level"

    // --- PG4: LOCATION ---
    const val ENVIRONMENT = "environment"

    // --- PG5: FITNESS GOAL ---
    const val FITNESS_GOALS = "fitness_goal"

    // --- PG6: BODY COMPOSITION GOAL ---
    const val BODYCOMP_GOAL = "bodycomp_goal"

    // --- PG7: DIETARY TYPE ---
    const val DIETARY_TYPE = "dietary_type"

    // --- PG8: RECOMMENDED PROGRAMS ---
    const val SELECTED_PROGRAMS = "selected_programs"

    // =========================================================
    // NEW: REHAB LOGIC FLAGS
    // =========================================================
    /** * True kung pinili ni user ang 'Recovery' (rehab) sa Goal screen.
     * Ginagamit ito para sa filtering sa Dashboard at API submission.
     */
    const val IS_REHAB_USER = "is_rehab_user"
}