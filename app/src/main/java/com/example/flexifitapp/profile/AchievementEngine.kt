package com.example.flexifitapp.profile

import android.content.Context
import com.example.flexifitapp.UserPrefs

object AchievementEngine {

    fun updateAchievements(ctx: Context) {

        val workouts = UserPrefs.getInt(ctx, UserPrefs.KEY_COMPLETED_WORKOUTS_COUNT, 0)
        val streak = UserPrefs.getInt(ctx, UserPrefs.KEY_ACTIVE_STREAK_DAYS, 0)

        // WORKOUT BADGES
        if (workouts >= 1) unlock(ctx, UserPrefs.BADGE_FIRST_WORKOUT)
        if (workouts >= 5) unlock(ctx, UserPrefs.BADGE_5_WORKOUTS)
        if (workouts >= 10) unlock(ctx, UserPrefs.BADGE_10_WORKOUTS)
        if (workouts >= 25) unlock(ctx, UserPrefs.BADGE_25_WORKOUTS)
        if (workouts >= 50) unlock(ctx, UserPrefs.BADGE_50_WORKOUTS)

        // STREAK BADGES
        if (streak >= 3) unlock(ctx, UserPrefs.BADGE_STREAK_3)
        if (streak >= 7) unlock(ctx, UserPrefs.BADGE_STREAK_7)
        if (streak >= 14) unlock(ctx, UserPrefs.BADGE_STREAK_14)
        if (streak >= 30) unlock(ctx, UserPrefs.BADGE_STREAK_30)

        // WEIGHT LOG BADGES
        val hasWeightLog = UserPrefs.getBool(ctx, UserPrefs.KEY_HAS_WEIGHT_LOG, false)
        if (hasWeightLog) unlock(ctx, UserPrefs.BADGE_FIRST_WEIGHT_LOG)

        // BMI BADGE
        val bmi = UserPrefs.getFloat(ctx, UserPrefs.KEY_BMI, 0f)
        if (bmi > 0f) unlock(ctx, UserPrefs.BADGE_BMI_UPDATED)

        // TARGET WEIGHT
        val target = UserPrefs.getInt(ctx, UserPrefs.KEY_TARGET_WEIGHT_KG, 0)
        val current = UserPrefs.getInt(ctx, UserPrefs.KEY_WEIGHT_KG, 0)

        if (target > 0 && current > 0 && current <= target) {
            unlock(ctx, UserPrefs.BADGE_TARGET_WEIGHT)
        }

        // PROGRAM / CONSISTENCY BADGES (RULE BASED)
        if (streak >= 7) {
            unlock(ctx, UserPrefs.BADGE_SEVEN_WORKOUTS_WEEK)
        }

        // PROGRAM COMPLETION RULE
        if (workouts >= 30) {
            unlock(ctx, UserPrefs.BADGE_FIRST_PROGRAM_COMPLETED)
            unlock(ctx, UserPrefs.BADGE_30_WORKOUTS_TOTAL)
        }
    }

    private fun unlock(ctx: Context, key: String) {
        if (!UserPrefs.getBool(ctx, key, false)) {
            UserPrefs.putBool(ctx, key, true)
        }
    }
}