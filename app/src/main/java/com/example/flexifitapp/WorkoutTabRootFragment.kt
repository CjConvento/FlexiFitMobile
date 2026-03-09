    package com.example.flexifitapp

    import android.os.Bundle
    import android.view.View
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.ProgressBar
    import android.widget.TextView
    import android.widget.Toast
    import androidx.core.os.bundleOf
    import androidx.core.view.isVisible
    import androidx.fragment.app.Fragment
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.flexifitapp.profile.AchievementEngine
    import com.example.flexifitapp.workout.WorkoutAdapter
    import com.example.flexifitapp.workout.WorkoutDetailFragment
    import com.example.flexifitapp.workout.WorkoutItem
    import com.example.flexifitapp.workout.WorkoutProgram
    import com.example.flexifitapp.workout.WorkoutRepository
    import com.google.android.material.button.MaterialButton
    import kotlinx.coroutines.launch
    import java.time.LocalDate
    import java.time.temporal.ChronoUnit

    class WorkoutTabRootFragment : Fragment(R.layout.fragment_workout) {

        private var day: Int = -1
        private var fromHost: Boolean = false

        private var btnOpenCalendar: ImageButton? = null
        private var btnPrevProgram: ImageButton? = null
        private var btnNextProgram: ImageButton? = null
        private var btnToggleWarmup: ImageButton? = null
        private var btnToggleWorkouts: ImageButton? = null
        private var btnRetryWorkout: MaterialButton? = null
        private var btnSkipWorkoutSession: MaterialButton? = null
        private var btnDoneWorkoutSession: MaterialButton? = null

        private var tvWorkoutDayTitle: TextView? = null
        private var tvWorkoutPlanDate: TextView? = null
        private var tvProgramName: TextView? = null
        private var tvProgramLevel: TextView? = null
        private var tvProgramDescription: TextView? = null
        private var tvWorkoutSessionStatus: TextView? = null
        private var tvWorkoutError: TextView? = null
        private var tvEmptyWorkout: TextView? = null

        private var ivProgramEnvironment: ImageView? = null

        private var progressWorkoutLoading: ProgressBar? = null

        private var layoutWarmupHeader: View? = null
        private var layoutWorkoutHeader: View? = null
        private var layoutWorkoutSessionBottomActions: View? = null

        private var rvWarmupItems: RecyclerView? = null
        private var rvWorkoutItems: RecyclerView? = null

        private var warmupExpanded = true
        private var workoutExpanded = true

        companion object {
            private const val IMAGE_BASE_URL = "https://your-api-domain.com/images/workouts/"
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            readArgs()
            bindViews(view)
            setupCalendarButton()
            setupTitle()
            setupRecyclerViews()
            setupExpandCollapse()
            setupProgramNavigation()
            setupSessionButtons()
            setupRetry()

            fetchWorkoutSession()
        }

        private fun readArgs() {
            day = arguments?.getInt(NavKeys.ARG_DAY, -1) ?: -1
            fromHost = arguments?.getBoolean(NavKeys.ARG_FROM_HOST, false) ?: false
        }

        private fun bindViews(view: View) {
            btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar)
            btnPrevProgram = view.findViewById(R.id.btnPrevProgram)
            btnNextProgram = view.findViewById(R.id.btnNextProgram)
            btnToggleWarmup = view.findViewById(R.id.btnToggleWarmup)
            btnToggleWorkouts = view.findViewById(R.id.btnToggleWorkouts)
            btnRetryWorkout = view.findViewById(R.id.btnRetryWorkout)
            btnSkipWorkoutSession = view.findViewById(R.id.btnSkipWorkoutSession)
            btnDoneWorkoutSession = view.findViewById(R.id.btnDoneWorkoutSession)

            tvWorkoutDayTitle = view.findViewById(R.id.tvWorkoutDayTitle)
            tvWorkoutPlanDate = view.findViewById(R.id.tvWorkoutPlanDate)
            tvProgramName = view.findViewById(R.id.tvProgramName)
            tvProgramLevel = view.findViewById(R.id.tvProgramLevel)
            tvProgramDescription = view.findViewById(R.id.tvProgramDescription)
            tvWorkoutSessionStatus = view.findViewById(R.id.tvWorkoutSessionStatus)
            tvWorkoutError = view.findViewById(R.id.tvWorkoutError)
            tvEmptyWorkout = view.findViewById(R.id.tvEmptyWorkout)

            ivProgramEnvironment = view.findViewById(R.id.ivProgramEnvironment)

            progressWorkoutLoading = view.findViewById(R.id.progressWorkoutLoading)

            layoutWarmupHeader = view.findViewById(R.id.layoutWarmupHeader)
            layoutWorkoutHeader = view.findViewById(R.id.layoutWorkoutHeader)
            layoutWorkoutSessionBottomActions =
                view.findViewById(R.id.layoutWorkoutSessionBottomActions)

            rvWarmupItems = view.findViewById(R.id.rvWarmupItems)
            rvWorkoutItems = view.findViewById(R.id.rvWorkoutItems)
        }

        private fun setupCalendarButton() {
            btnOpenCalendar?.visibility = if (fromHost) View.GONE else View.VISIBLE

            if (fromHost) {
                btnOpenCalendar?.setOnClickListener(null)
                return
            }

            btnOpenCalendar?.setOnClickListener {
                val bundle = bundleOf(
                    NavKeys.ARG_SOURCE_TAB to "WORKOUT"
                )

                findNavController().navigate(
                    R.id.action_workoutTabRootFragment_to_unifiedCalendarFragment,
                    bundle
                )
            }
        }

        private fun setupTitle() {
            tvWorkoutDayTitle?.text = if (day > 0) {
                "Workout - Day $day"
            } else {
                "Workout Session"
            }
        }

        private fun setupRecyclerViews() {
            rvWarmupItems?.layoutManager = LinearLayoutManager(requireContext())
            rvWorkoutItems?.layoutManager = LinearLayoutManager(requireContext())
        }

        private fun setupExpandCollapse() {
            btnToggleWarmup?.setOnClickListener {
                warmupExpanded = !warmupExpanded
                rvWarmupItems?.isVisible = warmupExpanded
                btnToggleWarmup?.rotation = if (warmupExpanded) 0f else -90f
            }

            btnToggleWorkouts?.setOnClickListener {
                workoutExpanded = !workoutExpanded
                rvWorkoutItems?.isVisible = workoutExpanded
                btnToggleWorkouts?.rotation = if (workoutExpanded) 0f else -90f
            }
        }

        private fun setupProgramNavigation() {
            btnPrevProgram?.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Previous program coming soon.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            btnNextProgram?.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Next program coming soon.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun setupSessionButtons() {
            btnDoneWorkoutSession?.setOnClickListener {
                val ctx = requireContext()

                // 1. increment completed workouts
                val currentWorkouts = UserPrefs.getInt(ctx, UserPrefs.KEY_COMPLETED_WORKOUTS_COUNT, 0)
                UserPrefs.putInt(ctx, UserPrefs.KEY_COMPLETED_WORKOUTS_COUNT, currentWorkouts + 1)

                AchievementEngine.updateAchievements(ctx)

                // 2. streak tracking
                val today = LocalDate.now()
                val lastWorkoutDate = UserPrefs.getString(ctx, UserPrefs.KEY_LAST_WORKOUT_DATE, "")
                val currentStreak = UserPrefs.getInt(ctx, UserPrefs.KEY_ACTIVE_STREAK_DAYS, 0)

                val newStreak = if (lastWorkoutDate.isBlank()) {
                    1
                } else {
                    try {
                        val lastDate = LocalDate.parse(lastWorkoutDate)
                        val diff = ChronoUnit.DAYS.between(lastDate, today)

                        when {
                            diff == 0L -> currentStreak      // same day, no double count
                            diff == 1L -> currentStreak + 1  // consecutive day
                            else -> 1                        // streak broken
                        }
                    } catch (_: Exception) {
                        1
                    }
                }

                UserPrefs.putInt(ctx, UserPrefs.KEY_ACTIVE_STREAK_DAYS, newStreak)
                UserPrefs.putString(ctx, UserPrefs.KEY_LAST_WORKOUT_DATE, today.toString())

                // 3. update achievements
                AchievementEngine.updateAchievements(ctx)

                Toast.makeText(
                    ctx,
                    "Workout marked done.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            btnSkipWorkoutSession?.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Workout skipped (API coming soon).",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun setupRetry() {
            btnRetryWorkout?.setOnClickListener {
                fetchWorkoutSession()
            }
        }

        private fun fetchWorkoutSession() {
            lifecycleScope.launch {
                showLoading()

                try {
                    val userId = UserPrefs.getUserId(requireContext())

                    if (userId <= 0) {
                        showError("User session not found.")
                        return@launch
                    }

                    val api = ApiClient.api(requireContext())
                    val repository = WorkoutRepository(api)
                    val response = repository.getCurrentWorkoutSession(userId)

                    if (response == null) {
                        showError("Failed to load workout session.")
                        return@launch
                    }

                    bindProgram(response.program)
                    bindWorkoutLists(response.warmups, response.workouts)

                    val hasNoItems = response.warmups.isEmpty() && response.workouts.isEmpty()

                    if (hasNoItems) {
                        showEmpty()
                    } else {
                        showContent()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to load workout session.")
                }
            }
        }

        private fun bindProgram(program: WorkoutProgram) {
            tvProgramName?.text = program.programName
            tvProgramLevel?.text = program.level
            tvProgramDescription?.text = program.description
            tvWorkoutSessionStatus?.text = program.status
            tvWorkoutPlanDate?.text = "Month ${program.month} - Week ${program.week} - Day ${program.day}"

            ivProgramEnvironment?.setImageResource(
                getEnvironmentIcon(program.environment)
            )

            if (day <= 0) {
                tvWorkoutDayTitle?.text = "Workout - Day ${program.day}"
            }
        }

        private fun getEnvironmentIcon(environment: String?): Int {
            return when (environment?.trim()?.lowercase()) {
                "gym" -> android.R.drawable.ic_menu_compass
                "home" -> android.R.drawable.ic_menu_myplaces
                "outdoor" -> android.R.drawable.ic_menu_mapmode
                else -> android.R.drawable.ic_menu_help
            }
        }

        private fun bindWorkoutLists(
            warmups: List<WorkoutItem>,
            workouts: List<WorkoutItem>
        ) {
            rvWarmupItems?.adapter = WorkoutAdapter(
                items = warmups,
                imageBaseUrl = IMAGE_BASE_URL,
                onClick = ::openWorkoutDetail
            )

            rvWorkoutItems?.adapter = WorkoutAdapter(
                items = workouts,
                imageBaseUrl = IMAGE_BASE_URL,
                onClick = ::openWorkoutDetail
            )
        }

        private fun showLoading() {
            progressWorkoutLoading?.isVisible = true
            tvWorkoutError?.isVisible = false
            btnRetryWorkout?.isVisible = false
            tvEmptyWorkout?.isVisible = false

            layoutWarmupHeader?.isVisible = false
            rvWarmupItems?.isVisible = false
            layoutWorkoutHeader?.isVisible = false
            rvWorkoutItems?.isVisible = false
            layoutWorkoutSessionBottomActions?.isVisible = false
        }

        private fun showContent() {
            progressWorkoutLoading?.isVisible = false
            tvWorkoutError?.isVisible = false
            btnRetryWorkout?.isVisible = false
            tvEmptyWorkout?.isVisible = false

            layoutWarmupHeader?.isVisible = true
            layoutWorkoutHeader?.isVisible = true
            layoutWorkoutSessionBottomActions?.isVisible = true

            rvWarmupItems?.isVisible = warmupExpanded
            rvWorkoutItems?.isVisible = workoutExpanded
        }

        private fun showEmpty() {
            progressWorkoutLoading?.isVisible = false
            tvWorkoutError?.isVisible = false
            btnRetryWorkout?.isVisible = false
            tvEmptyWorkout?.isVisible = true

            layoutWarmupHeader?.isVisible = false
            rvWarmupItems?.isVisible = false
            layoutWorkoutHeader?.isVisible = false
            rvWorkoutItems?.isVisible = false
            layoutWorkoutSessionBottomActions?.isVisible = false
        }

        private fun showError(message: String) {
            progressWorkoutLoading?.isVisible = false
            tvWorkoutError?.isVisible = true
            tvWorkoutError?.text = message
            btnRetryWorkout?.isVisible = true
            tvEmptyWorkout?.isVisible = false

            layoutWarmupHeader?.isVisible = false
            rvWarmupItems?.isVisible = false
            layoutWorkoutHeader?.isVisible = false
            rvWorkoutItems?.isVisible = false
            layoutWorkoutSessionBottomActions?.isVisible = false
        }

        private fun openWorkoutDetail(item: WorkoutItem) {
            val bundle = bundleOf(
                WorkoutDetailFragment.ARG_WORKOUT_ID to item.id,
                WorkoutDetailFragment.ARG_WORKOUT_NAME to item.name,
                WorkoutDetailFragment.ARG_WORKOUT_IMAGE_FILE_NAME to item.imageFileName,
                WorkoutDetailFragment.ARG_WORKOUT_MUSCLE_GROUP to item.muscleGroup,
                WorkoutDetailFragment.ARG_WORKOUT_SETS to item.sets,
                WorkoutDetailFragment.ARG_WORKOUT_REPS to item.reps,
                WorkoutDetailFragment.ARG_WORKOUT_REST_SECONDS to item.restSeconds,
                WorkoutDetailFragment.ARG_WORKOUT_DURATION_MINUTES to item.durationMinutes,
                WorkoutDetailFragment.ARG_WORKOUT_CALORIES to item.calories,
                WorkoutDetailFragment.ARG_WORKOUT_DESCRIPTION to item.description,
                WorkoutDetailFragment.ARG_WORKOUT_VIDEO_URL to item.videoUrl
            )

            findNavController().navigate(
                R.id.workoutDetailFragment,
                bundle
            )
        }

        override fun onDestroyView() {
            btnOpenCalendar = null
            btnPrevProgram = null
            btnNextProgram = null
            btnToggleWarmup = null
            btnToggleWorkouts = null
            btnRetryWorkout = null
            btnSkipWorkoutSession = null
            btnDoneWorkoutSession = null

            tvWorkoutDayTitle = null
            tvWorkoutPlanDate = null
            tvProgramName = null
            tvProgramLevel = null
            tvProgramDescription = null
            tvWorkoutSessionStatus = null
            tvWorkoutError = null
            tvEmptyWorkout = null

            ivProgramEnvironment = null

            progressWorkoutLoading = null

            layoutWarmupHeader = null
            layoutWorkoutHeader = null
            layoutWorkoutSessionBottomActions = null

            rvWarmupItems = null
            rvWorkoutItems = null

            super.onDestroyView()
        }
    }