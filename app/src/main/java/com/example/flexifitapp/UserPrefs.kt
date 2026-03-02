package com.example.flexifitapp

import android.content.Context

object UserPrefs {
    private const val PREF = "ff_user_profile"

    // flags
    const val KEY_ONBOARDING_DONE = "onboarding_done"

    // basic profile
    const val KEY_AGE = "age"
    const val KEY_GENDER = "gender"
    const val KEY_HEIGHT_CM = "height_cm"
    const val KEY_WEIGHT_KG = "weight_kg"

    // health
    const val KEY_HAS_INJURY = "has_injury"
    const val KEY_HAS_MEDICAL_CONDITION = "has_medical_condition"

    // background
    // (match your onboarding naming)
    const val KEY_FITNESS_LIFESTYLE = "fitness_lifestyle"
    const val KEY_FITNESS_LIFESTYLE_INDEX = "fitness_lifestyle_index"

    const val KEY_FITNESS_LEVEL = "fitness_level"
    const val KEY_FITNESS_LEVEL_INDEX = "fitness_level_index"

    // location/environment
    const val KEY_ENVIRONMENT = "environment" // home/gym/outdoor (based on your selected.id)

    // goals
    // you used StringSet on onboarding for fitness_goal (multi-select)
    const val KEY_FITNESS_GOAL_SET = "fitness_goal"          // store as StringSet
    const val KEY_BODYCOMP_GOAL = "bodycomp_goal"

    // diet
    const val KEY_DIETARY_TYPE = "dietary_type"              // match onboarding KEY_DIET

    // programs (multi-select)
    const val KEY_SELECTED_PROGRAMS = "selected_programs"    // store as StringSet

    // nutrition targets (optional / later computed)
    const val KEY_TARGET_CAL = "target_calories"
    const val KEY_TARGET_P = "target_protein_g"
    const val KEY_TARGET_C = "target_carbs_g"
    const val KEY_TARGET_F = "target_fats_g"

    // ===== basic put/get =====
    fun putString(ctx: Context, k: String, v: String) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(k, v).apply()

    fun putInt(ctx: Context, k: String, v: Int) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt(k, v).apply()

    fun putBool(ctx: Context, k: String, v: Boolean) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(k, v).apply()

    fun putStringSet(ctx: Context, k: String, v: Set<String>) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putStringSet(k, v).apply()

    fun getString(ctx: Context, k: String, def: String = "") =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(k, def) ?: def

    fun getInt(ctx: Context, k: String, def: Int = 0) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(k, def)

    fun getBool(ctx: Context, k: String, def: Boolean = false) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(k, def)

    fun getStringSet(ctx: Context, k: String): Set<String> =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getStringSet(k, emptySet()) ?: emptySet()

    // onboarding state helpers
    fun setOnboardingDone(ctx: Context, done: Boolean) =
        putBool(ctx, KEY_ONBOARDING_DONE, done)

    fun isOnboardingDone(ctx: Context) =
        getBool(ctx, KEY_ONBOARDING_DONE, false)
}