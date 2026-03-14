package com.example.flexifitapp.profile

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flexifitapp.ApiConfig
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class AchievementsDialogFragment : DialogFragment(R.layout.dialog_achievements) {

    private var btnBack: ImageView? = null
    private var ivUserAvatar: ImageView? = null

    private var tvUserName: TextView? = null
    private var tvUserGoal: TextView? = null

    private var tvUnlockedCount: TextView? = null
    private var tvStreakCount: TextView? = null
    private var tvCompletedWorkoutsCount: TextView? = null

    private var tabUnlockedBadges: TextView? = null
    private var tabLockedBadges: TextView? = null
    private var tvBadgeSectionTitle: TextView? = null

    private var rvBadges: RecyclerView? = null
    private var layoutEmptyState: LinearLayout? = null

    private lateinit var badgeAdapter: AchievementBadgeAdapter

    private val allBadges by lazy { buildBadges() }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AchievementEngine.updateAchievementsLocally(requireContext())

        bindViews(view)
        bindHeader()
        bindStats()
        setupRecycler()
        setupTabs()

        btnBack?.setOnClickListener { dismiss() }

        showUnlockedTab()
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar)

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserGoal = view.findViewById(R.id.tvUserGoal)

        tvUnlockedCount = view.findViewById(R.id.tvUnlockedCount)
        tvStreakCount = view.findViewById(R.id.tvStreakCount)
        tvCompletedWorkoutsCount = view.findViewById(R.id.tvCompletedWorkoutsCount)

        tabUnlockedBadges = view.findViewById(R.id.tabUnlockedBadges)
        tabLockedBadges = view.findViewById(R.id.tabLockedBadges)
        tvBadgeSectionTitle = view.findViewById(R.id.tvBadgeSectionTitle)

        rvBadges = view.findViewById(R.id.rvBadges)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
    }

    private fun bindHeader() {
        val ctx = requireContext()

        val name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, "")
            .ifBlank { UserPrefs.getString(ctx, UserPrefs.KEY_USER_NAME, "User") }

        val bodyGoal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "")
        val fitnessGoals = UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET)

        val goalText = when {
            bodyGoal.isNotBlank() -> prettify(bodyGoal)
            fitnessGoals.isNotEmpty() -> fitnessGoals.joinToString(", ") { prettify(it) }
            else -> "No goal yet"
        }

        tvUserName?.text = name
        tvUserGoal?.text = goalText

        val avatarUrl = UserPrefs.getString(ctx, UserPrefs.KEY_AVATAR_URL, "")
        val avatarView = ivUserAvatar ?: return

        if (avatarUrl.isNotBlank()) {
            Glide.with(this)
                .load(
                    if (avatarUrl.startsWith("http", true)) {
                        avatarUrl
                    } else {
                        ApiConfig.BASE_URL.trimEnd('/') + "/" + avatarUrl.trimStart('/')
                    }
                )
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(avatarView)
        } else {
            Glide.with(this)
                .load(R.drawable.ic_person)
                .circleCrop()
                .into(avatarView)
        }
    }

    private fun bindStats() {
        val unlockedCount = allBadges.count { it.unlocked }
        val totalWorkouts = UserPrefs.getInt(requireContext(), "completed_workouts_count", 0)
        val activeStreak = UserPrefs.getInt(requireContext(), "active_streak_days", 0)

        tvUnlockedCount?.text = unlockedCount.toString()
        tvStreakCount?.text = activeStreak.toString()
        tvCompletedWorkoutsCount?.text = totalWorkouts.toString()
    }

    private fun setupRecycler() {
        badgeAdapter = AchievementBadgeAdapter(emptyList())
        rvBadges?.layoutManager = LinearLayoutManager(requireContext())
        rvBadges?.adapter = badgeAdapter
    }

    private fun setupTabs() {
        tabUnlockedBadges?.setOnClickListener { showUnlockedTab() }
        tabLockedBadges?.setOnClickListener { showLockedTab() }
    }

    private fun showUnlockedTab() {
        val unlocked = allBadges.filter { it.unlocked }
        applyTabState(isUnlockedSelected = true)
        tvBadgeSectionTitle?.text = "Unlocked Badges"
        badgeAdapter.submitList(unlocked)
        updateEmptyState(unlocked.isEmpty())
    }

    private fun showLockedTab() {
        val locked = allBadges.filter { !it.unlocked }
        applyTabState(isUnlockedSelected = false)
        tvBadgeSectionTitle?.text = "Locked Badges"
        badgeAdapter.submitList(locked)
        updateEmptyState(locked.isEmpty())
    }

    private fun applyTabState(isUnlockedSelected: Boolean) {
        if (isUnlockedSelected) {
            tabUnlockedBadges?.setBackgroundResource(R.drawable.bg_achievement_tab_selected)
            tabUnlockedBadges?.setTextColor(resources.getColor(R.color.white, null))

            tabLockedBadges?.background = null
            tabLockedBadges?.setTextColor(resources.getColor(R.color.textSecondary, null))
        } else {
            tabLockedBadges?.setBackgroundResource(R.drawable.bg_achievement_tab_selected)
            tabLockedBadges?.setTextColor(resources.getColor(R.color.white, null))

            tabUnlockedBadges?.background = null
            tabUnlockedBadges?.setTextColor(resources.getColor(R.color.textSecondary, null))
        }
    }

    private fun updateEmptyState(showEmpty: Boolean) {
        layoutEmptyState?.visibility = if (showEmpty) View.VISIBLE else View.GONE
        rvBadges?.visibility = if (showEmpty) View.GONE else View.VISIBLE
    }

    private fun buildBadges(): List<AchievementBadge> {
        val ctx = requireContext()

        return listOf(
            AchievementBadge(
                "First Workout",
                "Complete your first workout session.",
                R.drawable.ic_badge_workout,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_FIRST_WORKOUT, false)
            ),
            AchievementBadge(
                "5 Workouts",
                "Complete 5 workout sessions.",
                R.drawable.ic_badge_workout,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_5_WORKOUTS, false)
            ),
            AchievementBadge(
                "10 Workouts",
                "Complete 10 workout sessions.",
                R.drawable.ic_badge_workout,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_10_WORKOUTS, false)
            ),
            AchievementBadge(
                "25 Workouts",
                "Complete 25 workout sessions.",
                R.drawable.ic_badge_workout,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_25_WORKOUTS, false)
            ),
            AchievementBadge(
                "50 Workouts",
                "Complete 50 workout sessions.",
                R.drawable.ic_badge_workout,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_50_WORKOUTS, false)
            ),

            AchievementBadge(
                "3 Day Streak",
                "Stay active for 3 straight days.",
                R.drawable.ic_badge_streak,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_STREAK_3, false)
            ),
            AchievementBadge(
                "7 Day Streak",
                "Stay active for 7 straight days.",
                R.drawable.ic_badge_streak,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_STREAK_7, false)
            ),
            AchievementBadge(
                "14 Day Streak",
                "Stay active for 14 straight days.",
                R.drawable.ic_badge_streak,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_STREAK_14, false)
            ),
            AchievementBadge(
                "30 Day Streak",
                "Stay active for 30 straight days.",
                R.drawable.ic_badge_streak,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_STREAK_30, false)
            ),

            AchievementBadge(
                "First Weight Log",
                "Log your weight for the first time.",
                R.drawable.ic_badge_goal,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_FIRST_WEIGHT_LOG, false)
            ),
            AchievementBadge(
                "BMI Updated",
                "Have a computed BMI in your profile.",
                R.drawable.ic_badge_goal,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_BMI_UPDATED, false)
            ),
            AchievementBadge(
                "Target Weight Reached",
                "Reach your target weight.",
                R.drawable.ic_badge_goal,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_TARGET_WEIGHT, false)
            ),

            AchievementBadge(
                "First Program Completed",
                "Finish your first full program.",
                R.drawable.ic_badge_program,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_FIRST_PROGRAM_COMPLETED, false)
            ),
            AchievementBadge(
                "7 Workouts Week",
                "Complete 7 workouts in one week.",
                R.drawable.ic_badge_program,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_SEVEN_WORKOUTS_WEEK, false)
            ),
            AchievementBadge(
                "30 Workouts Total",
                "Accumulate 30 total workouts.",
                R.drawable.ic_badge_program,
                UserPrefs.getBool(ctx, UserPrefs.BADGE_30_WORKOUTS_TOTAL, false)
            )
        )
    }    private fun prettify(value: String): String {
        return value
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { part ->
                part.replaceFirstChar { ch -> ch.uppercase() }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        btnBack = null
        ivUserAvatar = null
        tvUserName = null
        tvUserGoal = null
        tvUnlockedCount = null
        tvStreakCount = null
        tvCompletedWorkoutsCount = null
        tabUnlockedBadges = null
        tabLockedBadges = null
        tvBadgeSectionTitle = null
        rvBadges = null
        layoutEmptyState = null
    }
}