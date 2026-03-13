package com.example.flexifitapp.onboarding

import android.content.Context
import com.example.flexifitapp.OnboardingProfileRequest
import androidx.core.content.edit

object OnboardingStore {
    private const val PREFS = "onboarding_prefs"

    // =========================================================
    // HYDRATION LOGIC (Phase 1 Sync)
    // =========================================================
    fun hydrateFromProfile(ctx: Context, profile: OnboardingProfileRequest) {
        putInt(ctx, FlexiFitKeys.AGE, profile.age)
        putString(ctx, FlexiFitKeys.GENDER, profile.gender)
        putInt(ctx, FlexiFitKeys.HEIGHT_CM, profile.heightCm)
        putInt(ctx, FlexiFitKeys.WEIGHT_KG, profile.weightKg)
        putInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG, profile.targetWeightKg)

        putBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY, profile.upperBodyInjury)
        putBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY, profile.lowerBodyInjury)
        putBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS, profile.jointProblems)
        putBoolean(ctx, FlexiFitKeys.SHORT_BREATH, profile.shortBreath)
        putBoolean(ctx, FlexiFitKeys.HEALTH_NONE, profile.healthNone)

        putString(ctx, FlexiFitKeys.FITNESS_LIFESTYLE, profile.activityLevel)
        putString(ctx, FlexiFitKeys.FITNESS_LEVEL, profile.fitnessLevel)

        putStringSet(ctx, FlexiFitKeys.ENVIRONMENT, profile.environment.toSet())

        // Siguraduhin na 'FITNESS_GOALS' ang gamit (consistent with Pg5/Pg8)
        putStringSet(ctx, FlexiFitKeys.FITNESS_GOALS, profile.fitnessGoals.toSet())

        putString(ctx, FlexiFitKeys.BODYCOMP_GOAL, profile.bodyGoal)
        putString(ctx, FlexiFitKeys.DIETARY_TYPE, profile.dietType)
    }

    // =========================================================
    // GENERIC GETTERS & SETTERS (Enhanced)
    // =========================================================

    fun putString(ctx: Context, key: String, value: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putString(key, value) }
    }

    fun getString(ctx: Context, key: String, def: String = ""): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key, def)?.trim() ?: def
    }

    fun putBoolean(ctx: Context, key: String, value: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putBoolean(key, value) }
    }

    fun getBoolean(ctx: Context, key: String, def: Boolean = false): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(key, def)

    fun putStringSet(ctx: Context, key: String, value: Set<String>) {
        // Mahalaga: I-wrap sa HashSet para siguradong nare-recognize ng Android ang update
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(key, HashSet(value)).apply()
    }

    fun getStringSet(ctx: Context, key: String): Set<String> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, emptySet()) ?: emptySet()
        // DEFENSIVE COPY: Napaka-importante nito para hindi mag-crash o mag-leak ang data
        return set.toSet()
    }

    fun putInt(ctx: Context, key: String, value: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putInt(key, value) }
    }

    fun getInt(ctx: Context, key: String, def: Int = 0): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(key, def)

    fun clearAll(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}