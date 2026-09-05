package com.example.flexifitapp

import android.content.Context
import android.util.Log
import com.example.flexifitapp.BuildConfig
import com.example.flexifitapp.utils.AppLogger

object UserPrefs {
    // =========================================================
    // BASIC PUT / GET (Forward to SecurePrefs)
    // =========================================================
    fun putString(ctx: Context, k: String, v: String) = SecurePrefs.putString(k, v)
    fun putInt(ctx: Context, k: String, v: Int) = SecurePrefs.putInt(k, v)
    fun putFloat(ctx: Context, k: String, v: Float) = SecurePrefs.putString(k, v.toString())
    fun putBool(ctx: Context, k: String, v: Boolean) = SecurePrefs.putBoolean(k, v)
    fun putStringSet(ctx: Context, k: String, v: Set<String>) = SecurePrefs.putStringSet(k, v)

    fun getString(ctx: Context, k: String, def: String = ""): String = SecurePrefs.getString(k, def)
    fun getInt(ctx: Context, k: String, def: Int = 0): Int = SecurePrefs.getInt(k, def)
    fun getFloat(ctx: Context, k: String, def: Float = 0f): Float {
        val str = SecurePrefs.getString(k, "")
        return str.toFloatOrNull() ?: def
    }
    fun getBool(ctx: Context, k: String, def: Boolean = false): Boolean = SecurePrefs.getBoolean(k, def)
    fun getStringSet(ctx: Context, k: String): Set<String> = SecurePrefs.getStringSet(k)

    fun remove(ctx: Context, k: String) = SecurePrefs.remove(k)
    fun clearAll(ctx: Context) = SecurePrefs.clear()

    // =========================================================
    // AUTH KEYS (Keep these as-is)
    // =========================================================
    const val KEY_ONBOARDING_DONE = "onboarding_done"
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
    const val KEY_AVATAR_URL = "avatar_url"
    const val KEY_AGE = "age"
    const val KEY_GENDER = "gender"
    const val KEY_HEIGHT_CM = "height_cm"
    const val KEY_WEIGHT_KG = "weight_kg"
    const val KEY_TARGET_WEIGHT_KG = "target_weight_kg"
    const val KEY_COMPLETED_WORKOUTS = "completed_workouts_count"
    const val KEY_CURRENT_STREAK = "current_streak"
    const val KEY_HAS_WEIGHT_LOG = "has_weight_log"
    const val KEY_ACTIVE_STREAK_DAYS = "active_streak_days"
    const val KEY_LAST_WORKOUT_DATE = "last_workout_date"
    const val KEY_COMPLETED_WORKOUTS_COUNT = "completed_workouts_count"
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
    const val KEY_BMI = "bmi"
    const val KEY_BMR = "bmr"
    const val KEY_TDEE = "tdee"
    const val KEY_HAS_INJURY = "has_injury"
    const val KEY_HAS_MEDICAL_CONDITION = "has_medical_condition"
    const val KEY_IS_REHAB_USER = "is_rehab_user"
    const val KEY_HEALTH_NOTES = "health_notes"
    const val KEY_INJURY_LIST = "injury_list"
    const val KEY_MEDICAL_CONDITION_LIST = "medical_condition_list"
    const val KEY_FITNESS_LIFESTYLE = "fitness_lifestyle"
    const val KEY_FITNESS_LIFESTYLE_INDEX = "fitness_lifestyle_index"
    const val KEY_FITNESS_LEVEL = "fitness_level"
    const val KEY_FITNESS_LEVEL_INDEX = "fitness_level_index"
    const val KEY_ENVIRONMENT = "environment"
    const val KEY_FITNESS_GOAL_SET = "fitness_goal"
    const val KEY_BODYCOMP_GOAL = "bodycomp_goal"
    const val KEY_PRIMARY_GOAL = "primary_goal"
    const val KEY_GOAL_DEADLINE = "goal_deadline"
    const val KEY_DIETARY_TYPE = "dietary_type"
    const val KEY_MEAL_PATTERN = "meal_pattern"
    const val KEY_ALLERGIES = "allergies"
    const val KEY_FOOD_DISLIKES = "food_dislikes"
    const val KEY_SELECTED_PROGRAMS = "selected_programs"
    const val KEY_SELECTED_WORKOUT_DAYS = "selected_workout_days"
    const val KEY_TARGET_CAL = "target_calories"
    const val KEY_TARGET_P = "target_protein_g"
    const val KEY_TARGET_C = "target_carbs_g"
    const val KEY_TARGET_F = "target_fats_g"
    const val KEY_WATER_TARGET_ML = "water_target_ml"
    const val KEY_START_WEIGHT_KG = "start_weight_kg"
    const val KEY_LATEST_WEIGHT_KG = "latest_weight_kg"
    const val KEY_GOAL_PROGRESS_PERCENT = "goal_progress_percent"

    // =========================================================
    // CLEAR AUTH
    // =========================================================
    fun clearAuth(ctx: Context) {
        SecurePrefs.remove(KEY_JWT_TOKEN)
        SecurePrefs.remove(KEY_USER_ID)
        SecurePrefs.remove(KEY_ROLE)
        SecurePrefs.remove(KEY_STATUS)
        SecurePrefs.remove(KEY_IS_VERIFIED)
        SecurePrefs.remove(KEY_NAME)
        SecurePrefs.remove(KEY_USER_NAME)
        SecurePrefs.remove(KEY_USER_EMAIL)
        SecurePrefs.remove(KEY_AVATAR_URL)
        SecurePrefs.remove(KEY_FIREBASE_TOKEN)
    }

    // =========================================================
    // SAVE AUTH (Now using SecurePrefs)
    // =========================================================
    fun saveAuth(
        ctx: Context,
        token: String,
        userId: Int,
        role: String?,
        status: String?,
        isVerified: Boolean,
        name: String?,
        photoUrl: String?,
        firebaseToken: String? = null
    ) {
        SecurePrefs.putString(KEY_JWT_TOKEN, token)
        SecurePrefs.putInt(KEY_USER_ID, userId)
        SecurePrefs.putString(KEY_ROLE, role ?: "")
        SecurePrefs.putString(KEY_STATUS, status ?: "")
        SecurePrefs.putBoolean(KEY_IS_VERIFIED, isVerified)
        SecurePrefs.putString(KEY_NAME, name ?: "")
        SecurePrefs.putString(KEY_AVATAR_URL, photoUrl ?: "")
        SecurePrefs.putString(KEY_FIREBASE_TOKEN, firebaseToken ?: "")

        // ✅ Only log in debug mode, and DON'T log the full token
        if (BuildConfig.DEBUG) {
            AppLogger.d("UserPrefs", "Token saved (encrypted). userId=$userId, name=$name")
        }
    }

    fun getToken(ctx: Context): String {
        return SecurePrefs.getString(KEY_JWT_TOKEN, "")
    }

    fun getUserId(ctx: Context): Int = SecurePrefs.getInt(KEY_USER_ID, 0)
    fun getRole(ctx: Context): String = SecurePrefs.getString(KEY_ROLE, "")
    fun getStatus(ctx: Context): String = SecurePrefs.getString(KEY_STATUS, "")
    fun isVerified(ctx: Context): Boolean = SecurePrefs.getBoolean(KEY_IS_VERIFIED, false)
    fun isLoggedIn(ctx: Context): Boolean = getToken(ctx).isNotBlank() && getUserId(ctx) > 0

    // =========================================================
    // ONBOARDING HELPERS
    // =========================================================
    fun setOnboardingDone(ctx: Context, done: Boolean) = putBool(ctx, KEY_ONBOARDING_DONE, done)
    fun isOnboardingDone(ctx: Context): Boolean = getBool(ctx, KEY_ONBOARDING_DONE, false)

    fun saveOnboardingResult(ctx: Context, isRehab: Boolean) {
        putBool(ctx, KEY_ONBOARDING_DONE, true)
        putBool(ctx, KEY_IS_REHAB_USER, isRehab)
    }
}