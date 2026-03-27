package com.example.flexifitapp

import android.content.Context
import android.util.Log
import com.example.flexifitapp.onboarding.FlexiFitKeys
import com.example.flexifitapp.OnboardingProfileRequest

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
    const val KEY_USERNAME = "username"
    const val KEY_USER_EMAIL = "email"
    const val KEY_USER_ID = "userId"
    const val KEY_JWT_TOKEN = "jwt_token"

    const val KEY_FIREBASE_TOKEN = "firebase_token"
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

    // ACHIEVEMENTS - (STAY LANG ITO, HINDI KO TANGGAL)
    const val KEY_COMPLETED_WORKOUTS = "completed_workouts_count"
    const val KEY_CURRENT_STREAK = "current_streak"
    const val KEY_HAS_WEIGHT_LOG = "has_weight_log"
    const val KEY_ACTIVE_STREAK_DAYS = "active_streak_days"
    const val KEY_LAST_WORKOUT_DATE = "last_workout_date"
    const val KEY_COMPLETED_WORKOUTS_COUNT = "completed_workouts_count"

    // =========================================================
    // ACHIEVEMENTS / BADGES - (STAY LANG ITO, HINDI KO TANGGAL)
    // =========================================================
    const val KEY_FIRST_PROGRAM_COMPLETED = "first_program_completed"
    const val KEY_SEVEN_WORKOUTS_WEEK = "seven_workouts_week"

    const val BADGE_FIRST_WORKOUT = "badge_first_workout"
    const val BADGE_5_WORKOUTS = "badge_5_workouts"
    const val BADGE_10_WORKOUTS = "badge_10_workouts"
    const val BADGE_25_WORKOUTS = "badge_25_workouts"
    const val BADGE_50_WORKOUTS = "badge_50_workouts"

    const val BADGE_STREAK_3 = "badge_streak_3"
    const val BADGE_STREAK_7 = "badge_streak_7"
    const val BADGE_STREAK_14 = "badge_streak_14"
    const val BADGE_STREAK_30 = "badge_streak_30"

    const val BADGE_FIRST_WEIGHT_LOG = "badge_first_weight_log"
    const val BADGE_BMI_UPDATED = "badge_bmi_updated"
    const val BADGE_TARGET_WEIGHT = "badge_target_weight"

    const val BADGE_FIRST_PROGRAM_COMPLETED = "badge_first_program_completed"
    const val BADGE_SEVEN_WORKOUTS_WEEK = "badge_seven_workouts_week"
    const val BADGE_30_WORKOUTS_TOTAL = "badge_30_workouts_total"

    // Optional computed/display fields
    const val KEY_BMI = "bmi"
    const val KEY_BMR = "bmr"
    const val KEY_TDEE = "tdee"

    // =========================================================
    // HEALTH
    // =========================================================
    const val KEY_HAS_INJURY = "has_injury"
    const val KEY_HAS_MEDICAL_CONDITION = "has_medical_condition"
    const val KEY_IS_REHAB_USER = "is_rehab_user"

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
    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun putString(ctx: Context, k: String, v: String) = prefs(ctx).edit().putString(k, v).apply()
    fun putInt(ctx: Context, k: String, v: Int) = prefs(ctx).edit().putInt(k, v).apply()
    fun putFloat(ctx: Context, k: String, v: Float) = prefs(ctx).edit().putFloat(k, v).apply()
    fun putBool(ctx: Context, k: String, v: Boolean) = prefs(ctx).edit().putBoolean(k, v).apply()
    fun putStringSet(ctx: Context, k: String, v: Set<String>) = prefs(ctx).edit().putStringSet(k, v).apply()

    fun getString(ctx: Context, k: String, def: String = ""): String = prefs(ctx).getString(k, def) ?: def
    fun getInt(ctx: Context, k: String, def: Int = 0): Int = prefs(ctx).getInt(k, def)
    fun getFloat(ctx: Context, k: String, def: Float = 0f): Float = prefs(ctx).getFloat(k, def)
    fun getBool(ctx: Context, k: String, def: Boolean = false): Boolean = prefs(ctx).getBoolean(k, def)
    fun getStringSet(ctx: Context, k: String): Set<String> = prefs(ctx).getStringSet(k, emptySet()) ?: emptySet()

    fun remove(ctx: Context, k: String) = prefs(ctx).edit().remove(k).apply()

    fun clearAll(ctx: Context) {
        // Burahin ang main profile prefs
        ctx.getSharedPreferences("ff_user_profile", Context.MODE_PRIVATE).edit().clear().apply()
        // Burahin din ang theme prefs kung gusto mo talagang fresh start
        ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        // Burahin din yung ginamit mo sa SignupActivity
        ctx.getSharedPreferences("flexifit_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    // =========================================================
    // HELPERS
    // =========================================================

    fun setOnboardingDone(ctx: Context, done: Boolean) = putBool(ctx, KEY_ONBOARDING_DONE, done)
    fun isOnboardingDone(ctx: Context): Boolean = getBool(ctx, KEY_ONBOARDING_DONE, false)

    // REVISED: Idinagdag sa tamang pwesto para sa SummaryFragment
    fun saveOnboardingResult(ctx: Context, isRehab: Boolean) {
        prefs(ctx).edit()
            .putBoolean(KEY_ONBOARDING_DONE, true)
            .putBoolean(KEY_IS_REHAB_USER, isRehab)
            .commit()
    }

    fun saveAuth(
        ctx: Context,
        token: String,
        userId: Int,
        role: String?,
        status: String?,
        isVerified: Boolean,
        name: String?,      // Idagdag ito babe
        photoUrl: String?,   // Idagdag din ito
        firebaseToken: String? = null
    ) {
        prefs(ctx).edit()
            .putString(KEY_JWT_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role ?: "")
            .putString(KEY_STATUS, status ?: "")
            .putBoolean(KEY_IS_VERIFIED, isVerified)
            .putString(KEY_NAME, name ?: "")          // I-save ang Name
            .putString(KEY_AVATAR_URL, photoUrl ?: "") // I-save ang Avatar URL
            .putString(KEY_FIREBASE_TOKEN, firebaseToken ?:"")
            .commit()
        Log.d("UserPrefs", "Token saved: ${token.take(20)}... (userId=$userId, name=$name)")
        Log.d("UserPrefs", "Firebase token saved: ${firebaseToken?.take(20) ?: "NULL"}")
    }

    fun getToken(ctx: Context): String {
        val token = getString(ctx, KEY_JWT_TOKEN, "")
        Log.d("UserPrefs", "Token retrieved from prefs: ${if (token.isNotBlank()) "${token.take(20)}..." else "EMPTY"}")
        return token
    }
    fun getUserId(ctx: Context): Int = getInt(ctx, KEY_USER_ID, 0)
    fun getRole(ctx: Context): String = getString(ctx, KEY_ROLE, "")
    fun getStatus(ctx: Context): String = getString(ctx, KEY_STATUS, "")
    fun isVerified(ctx: Context): Boolean = getBool(ctx, KEY_IS_VERIFIED, false)
    fun isLoggedIn(ctx: Context): Boolean = getToken(ctx).isNotBlank() && getUserId(ctx) > 0

    fun clearAuth(ctx: Context) {
        Log.e("UserPrefs", "clearAuth called! Stack trace:", Exception("Stack trace"))
        prefs(ctx).edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ROLE)
            .remove(KEY_STATUS)
            .remove(KEY_IS_VERIFIED)
            .remove(KEY_NAME)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_AVATAR_URL)
            .remove(KEY_FIREBASE_TOKEN)
            .apply()
        Log.d("UserPrefs", "Clearing auth")
    }
}