package com.example.flexifitapp

import android.content.Context

object UserPrefs {
    private const val PREF = "ff_user_profile"

    // =========================================================
    // FLAGS
    // =========================================================
    const val KEY_ONBOARDING_DONE = "onboarding_done"

    // =========================================================
    // AUTH
    // =========================================================
    const val KEY_NAME = "name"
    const val KEY_USER_NAME = "username"
    const val KEY_USER_EMAIL = "email"
    const val KEY_USER_ID = "userId"
    const val KEY_JWT_TOKEN = "jwt_token"
    const val KEY_ROLE = "role"
    const val KEY_STATUS = "status"
    const val KEY_IS_VERIFIED = "is_verified"

    // =========================================================
    // BASIC PROFILE
    // =========================================================
    const val KEY_AVATAR_URL = "avatar_url"
    const val KEY_AGE = "age"
    const val KEY_GENDER = "gender"
    const val KEY_HEIGHT_CM = "height_cm"
    const val KEY_WEIGHT_KG = "weight_kg"
    const val KEY_TARGET_WEIGHT_KG = "target_weight_kg"

    // Optional computed/display fields
    const val KEY_BMI = "bmi"
    const val KEY_BMR = "bmr"
    const val KEY_TDEE = "tdee"

    // =========================================================
    // HEALTH
    // =========================================================
    const val KEY_HAS_INJURY = "has_injury"
    const val KEY_HAS_MEDICAL_CONDITION = "has_medical_condition"

    // Optional detailed health notes / selections
    const val KEY_HEALTH_NOTES = "health_notes"
    const val KEY_INJURY_LIST = "injury_list"
    const val KEY_MEDICAL_CONDITION_LIST = "medical_condition_list"

    // =========================================================
    // FITNESS BACKGROUND
    // =========================================================
    const val KEY_FITNESS_LIFESTYLE = "fitness_lifestyle"
    const val KEY_FITNESS_LIFESTYLE_INDEX = "fitness_lifestyle_index"

    const val KEY_FITNESS_LEVEL = "fitness_level"
    const val KEY_FITNESS_LEVEL_INDEX = "fitness_level_index"

    // =========================================================
    // WORKOUT ENVIRONMENT
    // =========================================================
    const val KEY_ENVIRONMENT = "environment"

    // =========================================================
    // GOALS
    // =========================================================
    const val KEY_FITNESS_GOAL_SET = "fitness_goal"
    const val KEY_BODYCOMP_GOAL = "bodycomp_goal"

    // Optional extra goal keys
    const val KEY_PRIMARY_GOAL = "primary_goal"
    const val KEY_GOAL_DEADLINE = "goal_deadline"

    // =========================================================
    // DIET / NUTRITION PREFERENCES
    // =========================================================
    const val KEY_DIETARY_TYPE = "dietary_type"
    const val KEY_MEAL_PATTERN = "meal_pattern"
    const val KEY_ALLERGIES = "allergies"
    const val KEY_FOOD_DISLIKES = "food_dislikes"

    // =========================================================
    // PROGRAMS
    // =========================================================
    const val KEY_SELECTED_PROGRAMS = "selected_programs"
    const val KEY_SELECTED_WORKOUT_DAYS = "selected_workout_days"

    // =========================================================
    // NUTRITION TARGETS
    // =========================================================
    const val KEY_TARGET_CAL = "target_calories"
    const val KEY_TARGET_P = "target_protein_g"
    const val KEY_TARGET_C = "target_carbs_g"
    const val KEY_TARGET_F = "target_fats_g"
    const val KEY_WATER_TARGET_ML = "water_target_ml"

    // =========================================================
    // PROGRESS / TRACKING
    // =========================================================
    const val KEY_START_WEIGHT_KG = "start_weight_kg"
    const val KEY_LATEST_WEIGHT_KG = "latest_weight_kg"
    const val KEY_GOAL_PROGRESS_PERCENT = "goal_progress_percent"

    // =========================================================
    // BASIC PUT / GET
    // =========================================================
    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun putString(ctx: Context, k: String, v: String) =
        prefs(ctx).edit().putString(k, v).apply()

    fun putInt(ctx: Context, k: String, v: Int) =
        prefs(ctx).edit().putInt(k, v).apply()

    fun putFloat(ctx: Context, k: String, v: Float) =
        prefs(ctx).edit().putFloat(k, v).apply()

    fun putBool(ctx: Context, k: String, v: Boolean) =
        prefs(ctx).edit().putBoolean(k, v).apply()

    fun putStringSet(ctx: Context, k: String, v: Set<String>) =
        prefs(ctx).edit().putStringSet(k, v).apply()

    fun getString(ctx: Context, k: String, def: String = ""): String =
        prefs(ctx).getString(k, def) ?: def

    fun getInt(ctx: Context, k: String, def: Int = 0): Int =
        prefs(ctx).getInt(k, def)

    fun getFloat(ctx: Context, k: String, def: Float = 0f): Float =
        prefs(ctx).getFloat(k, def)

    fun getBool(ctx: Context, k: String, def: Boolean = false): Boolean =
        prefs(ctx).getBoolean(k, def)

    fun getStringSet(ctx: Context, k: String): Set<String> =
        prefs(ctx).getStringSet(k, emptySet()) ?: emptySet()

    fun remove(ctx: Context, k: String) =
        prefs(ctx).edit().remove(k).apply()

    fun clearAll(ctx: Context) =
        prefs(ctx).edit().clear().apply()

    // =========================================================
    // ONBOARDING STATE HELPERS
    // =========================================================
    fun setOnboardingDone(ctx: Context, done: Boolean) =
        putBool(ctx, KEY_ONBOARDING_DONE, done)

    fun isOnboardingDone(ctx: Context): Boolean =
        getBool(ctx, KEY_ONBOARDING_DONE, false)

    // =========================================================
    // AUTH HELPERS
    // =========================================================
    fun saveAuth(
        ctx: Context,
        token: String,
        userId: Int,
        role: String?,
        status: String?,
        isVerified: Boolean
    ) {
        prefs(ctx).edit()
            .putString(KEY_JWT_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role ?: "")
            .putString(KEY_STATUS, status ?: "")
            .putBoolean(KEY_IS_VERIFIED, isVerified)
            .apply()
    }

    fun getToken(ctx: Context): String =
        getString(ctx, KEY_JWT_TOKEN, "")

    fun getUserId(ctx: Context): Int =
        getInt(ctx, KEY_USER_ID, 0)

    fun getRole(ctx: Context): String =
        getString(ctx, KEY_ROLE, "")

    fun getStatus(ctx: Context): String =
        getString(ctx, KEY_STATUS, "")

    fun isVerified(ctx: Context): Boolean =
        getBool(ctx, KEY_IS_VERIFIED, false)

    fun isLoggedIn(ctx: Context): Boolean =
        getToken(ctx).isNotBlank() && getInt(ctx, KEY_USER_ID, 0) > 0

    fun clearAuth(ctx: Context) {
        prefs(ctx).edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ROLE)
            .remove(KEY_STATUS)
            .remove(KEY_IS_VERIFIED)
            .remove(KEY_NAME)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
    }
}