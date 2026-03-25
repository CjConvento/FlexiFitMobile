package com.example.flexifitapp

import android.os.Bundle
import android.util.Log
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
import com.example.flexifitapp.workout.WorkoutAdapter
import com.example.flexifitapp.workout.WorkoutItem
import com.example.flexifitapp.workout.WorkoutRepository
import com.example.flexifitapp.workout.WorkoutSessionResponse
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class WorkoutTabRootFragment : Fragment(R.layout.fragment_workout) {

    private var day: Int = -1
    private var fromHost: Boolean = false
    private var monthArg: Int = 1
    private var currentSessionId: Int = 0
    private var currentResponse: WorkoutSessionResponse? = null

    // UI Variables
    private var btnBackWorkoutPlan: ImageButton? = null
    private var btnOpenCalendar: ImageButton? = null
    private var btnPrevProgram: ImageButton? = null
    private var btnNextProgram: ImageButton? = null
    private var btnToggleWarmup: ImageView? = null
    private var btnToggleWorkouts: ImageView? = null
    private var btnRetryWorkout: MaterialButton? = null
    private var btnSkipWorkoutSession: MaterialButton? = null
    private var btnDoneWorkoutSession: MaterialButton? = null

    private var tvWorkoutPlanDate: TextView? = null
    private var tvProgramHeader: TextView? = null
    private var tvCurrentProgramName: TextView? = null
    private var tvProgramProgress: TextView? = null
    private var tvWorkoutDayTitle: TextView? = null
    private var tvWorkoutStatus: TextView? = null
    private var ivStatusCycle: ImageView? = null
    private var tvWorkoutError: TextView? = null

    private var progressWorkoutLoading: ProgressBar? = null
    private var layoutWarmupHeader: View? = null
    private var layoutWorkoutHeader: View? = null
    private var layoutWorkoutSessionBottomActions: View? = null

    private var rvWarmupItems: RecyclerView? = null
    private var rvWorkoutItems: RecyclerView? = null

    private var warmupExpanded = true
    private var workoutExpanded = true

    // ─────────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore state from saved instance
        if (savedInstanceState != null) {
            day = savedInstanceState.getInt("ARG_DAY", -1)
            monthArg = savedInstanceState.getInt("ARG_MONTH", 1)
            fromHost = savedInstanceState.getBoolean("ARG_FROM_HOST", false)
            // Recreate arguments so that readArgs() works
            arguments = Bundle().apply {
                putInt("ARG_DAY", day)
                putInt("ARG_MONTH", monthArg)
                putBoolean("ARG_FROM_HOST", fromHost)
            }
            Log.d("WORKOUT_TAB", "Restored from savedInstanceState: Day=$day, Month=$monthArg, fromHost=$fromHost")
        } else if (arguments == null) {
            // Fallback: try to get arguments from parent fragment
            val parent = parentFragment
            if (parent is DayHostFragment) {
                val parentDay = parent.arguments?.getInt("ARG_DAY", -1) ?: -1
                val parentMonth = parent.arguments?.getInt("ARG_MONTH", 1) ?: 1
                val parentFromHost = parent.arguments?.getBoolean("ARG_FROM_HOST", false) ?: false
                arguments = Bundle().apply {
                    putInt("ARG_DAY", parentDay)
                    putInt("ARG_MONTH", parentMonth)
                    putBoolean("ARG_FROM_HOST", parentFromHost)
                }
                Log.d("WORKOUT_TAB", "Recovered arguments from parent: Day=$parentDay, Month=$parentMonth")
            } else {
                // Default (should only happen when opened from bottom navigation)
                arguments = Bundle().apply {
                    putInt("ARG_DAY", -1)
                    putInt("ARG_MONTH", 1)
                    putBoolean("ARG_FROM_HOST", false)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("ARG_DAY", day)
        outState.putInt("ARG_MONTH", monthArg)
        outState.putBoolean("ARG_FROM_HOST", fromHost)
        Log.d("WORKOUT_TAB", "Saving state: Day=$day, Month=$monthArg, fromHost=$fromHost")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArgs()
        bindViews(view)
        setupCalendarButton()
        setupRecyclerViews()
        setupExpandCollapse()
        setupSessionButtons()
        setupRetry()
        fetchWorkoutSession()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ARGUMENTS
    // ─────────────────────────────────────────────────────────────────────────────

    private fun readArgs() {
        day = arguments?.getInt("ARG_DAY", -1) ?: -1
        monthArg = arguments?.getInt("ARG_MONTH", 1) ?: 1
        fromHost = arguments?.getBoolean("ARG_FROM_HOST", false) ?: false
        Log.i("WORKOUT_TAB", "Args: Day=$day, Month=$monthArg, fromHost=$fromHost")
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI BINDING
    // ─────────────────────────────────────────────────────────────────────────────

    private fun bindViews(view: View) {
        btnBackWorkoutPlan = view.findViewById(R.id.btnBackWorkoutPlan)
        btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar)
        btnPrevProgram = view.findViewById(R.id.btnPrevProgram)
        btnNextProgram = view.findViewById(R.id.btnNextProgram)

        tvWorkoutPlanDate = view.findViewById(R.id.tvWorkoutPlanDate)
        tvProgramHeader = view.findViewById(R.id.tvProgramHeader)
        tvCurrentProgramName = view.findViewById(R.id.tvCurrentProgramName)
        tvProgramProgress = view.findViewById(R.id.tvProgramProgress)
        tvWorkoutDayTitle = view.findViewById(R.id.tvSessionLabel)
        tvWorkoutStatus = view.findViewById(R.id.tvWorkoutStatus)
        ivStatusCycle = view.findViewById(R.id.ivStatusCycle)

        layoutWarmupHeader = view.findViewById(R.id.layoutWarmupHeader)
        btnToggleWarmup = view.findViewById(R.id.btnToggleWarmup)
        layoutWorkoutHeader = view.findViewById(R.id.layoutWorkoutHeader)
        btnToggleWorkouts = view.findViewById(R.id.btnToggleWorkouts)
        rvWarmupItems = view.findViewById(R.id.rvWarmupItems)
        rvWorkoutItems = view.findViewById(R.id.rvWorkoutItems)

        btnSkipWorkoutSession = view.findViewById(R.id.btnSkipWorkoutSession)
        btnDoneWorkoutSession = view.findViewById(R.id.btnDoneWorkoutSession)
        layoutWorkoutSessionBottomActions = view.findViewById(R.id.layoutWorkoutSessionBottomActions)
        progressWorkoutLoading = view.findViewById(R.id.progressWorkoutLoading)
        tvWorkoutError = view.findViewById(R.id.tvWorkoutError)
        btnRetryWorkout = view.findViewById(R.id.btnRetryWorkout)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FETCH WORKOUT
    // ─────────────────────────────────────────────────────────────────────────────

    private fun fetchWorkoutSession() {
        lifecycleScope.launch {
            showLoading()
            try {
                val api = ApiClient.api()
                val repository = WorkoutRepository(api)

                val response = if (fromHost) {
                    if (day > 0) {
                        repository.getWorkoutByDate(day, monthArg)
                    } else {
                        // Guard against invalid day when coming from host
                        showError("Invalid day number.")
                        null
                    }
                } else {
                    repository.getTodayWorkout()
                }

                if (response != null) {
                    currentResponse = response
                    currentSessionId = response.sessionId
                    updateUI(response)
                    showContent()

                    monthArg = response.program.month
                    if (fromHost) {
                        layoutWorkoutSessionBottomActions?.visibility = View.GONE
                    }
                } else {
                    showError("No record found for Day ${if (fromHost) day else "today"}.")
                }
            } catch (e: Exception) {
                Log.e("WORKOUT_TAB", "Error: ${e.message}")
                showError("Connection error. Check your API!")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI UPDATES
    // ─────────────────────────────────────────────────────────────────────────────

    private fun updateUI(response: WorkoutSessionResponse) {
        tvWorkoutPlanDate?.text = "Month ${response.program.month} - Week ${response.program.week} - Day ${response.dayNo}"
        tvProgramHeader?.text = "Program ${response.program.programNumber}"
        tvCurrentProgramName?.text = response.program.programName
        tvProgramProgress?.text = response.program.description
        tvWorkoutDayTitle?.text = "Workout - Day ${response.dayNo}"

        // Update status
        tvWorkoutStatus?.text = response.status
        if (response.status.equals("COMPLETED", ignoreCase = true)) {
            ivStatusCycle?.setImageResource(R.drawable.rounded_checkbox)
            btnDoneWorkoutSession?.isEnabled = false
            btnDoneWorkoutSession?.text = "Completed"
            btnSkipWorkoutSession?.isVisible = false
        } else {
            // Update button states based on canSkip
            btnSkipWorkoutSession?.isVisible = response.canSkip
            if (response.canSkip) {
                btnSkipWorkoutSession?.isEnabled = true
                btnSkipWorkoutSession?.text = "Skip"
            } else {
                btnSkipWorkoutSession?.isEnabled = false
                btnSkipWorkoutSession?.text = response.skipMessage ?: "Cannot Skip"
            }
        }

        // Set adapters
        rvWarmupItems?.adapter = WorkoutAdapter(response.warmups) { item -> openWorkoutDetail(item) }
        rvWorkoutItems?.adapter = WorkoutAdapter(response.workouts) { item -> openWorkoutDetail(item) }

        // Show/hide warmup section
        layoutWarmupHeader?.isVisible = response.warmups.isNotEmpty()
    }

    private fun openWorkoutDetail(item: WorkoutItem) {
        val bundle = bundleOf(
            "workoutName" to item.name,
            "image" to item.imageFileName,
            "sets" to item.sets,
            "reps" to item.reps,
            "rest" to item.restSeconds,
            "duration" to item.durationMinutes,
            "calories" to item.calories,
            "description" to item.description,
            "videoUrl" to item.videoUrl
        )
        findNavController().navigate(R.id.action_workoutTabRootFragment_to_workoutDetailFragment, bundle)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    private fun showLoading() {
        progressWorkoutLoading?.isVisible = true
        layoutWarmupHeader?.isVisible = false
        layoutWorkoutHeader?.isVisible = false
        layoutWorkoutSessionBottomActions?.isVisible = false
        rvWarmupItems?.isVisible = false
        rvWorkoutItems?.isVisible = false
    }

    private fun showContent() {
        progressWorkoutLoading?.isVisible = false
        layoutWarmupHeader?.isVisible = true
        layoutWorkoutHeader?.isVisible = true
        layoutWorkoutSessionBottomActions?.isVisible = true
        rvWarmupItems?.isVisible = warmupExpanded
        rvWorkoutItems?.isVisible = workoutExpanded
    }

    private fun showError(message: String) {
        progressWorkoutLoading?.isVisible = false
        tvWorkoutError?.isVisible = true
        tvWorkoutError?.text = message
        btnRetryWorkout?.isVisible = true
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

    private fun setupSessionButtons() {
        // COMPLETE BUTTON
        btnDoneWorkoutSession?.setOnClickListener {
            if (currentSessionId == 0) {
                Toast.makeText(requireContext(), "No active session found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = ApiClient.api()
                    val repository = WorkoutRepository(api)

                    val result = repository.completeWorkout(
                        sessionId = currentSessionId,
                        totalCalories = currentResponse?.totalCalories ?: 0,
                        totalMinutes = currentResponse?.totalDuration ?: 0,
                        status = "COMPLETED"
                    )

                    if (result != null) {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()

                        // If calories were burned, show a short toast with the amount
                        if (result.totalCalories > 0) {
                            Toast.makeText(requireContext(), "🔥 You burned ${result.totalCalories} kcal!", Toast.LENGTH_SHORT).show()
                        }

                        if (fromHost) {
                            fetchWorkoutSession()
                        } else {
                            findNavController().popBackStack()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to complete workout", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // SKIP BUTTON
        btnSkipWorkoutSession?.setOnClickListener {
            if (currentSessionId == 0) {
                Toast.makeText(requireContext(), "No active session found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = ApiClient.api()
                    val repository = WorkoutRepository(api)

                    val canSkipResponse = repository.canSkipToday()
                    if (canSkipResponse != null && canSkipResponse.canSkip) {
                        val result = repository.completeWorkout(
                            sessionId = currentSessionId,
                            totalCalories = 0,
                            totalMinutes = 0,
                            status = "SKIPPED",
                            skipReason = "User skipped workout"
                        )

                        if (result != null) {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()

                            if (fromHost) {
                                fetchWorkoutSession()
                            } else {
                                findNavController().popBackStack()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to skip workout", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val message = canSkipResponse?.message ?: "Cannot skip this workout"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnBackWorkoutPlan?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCalendarButton() {
        btnOpenCalendar?.isVisible = !fromHost
        btnOpenCalendar?.setOnClickListener {
            val bundle = bundleOf(
                NavKeys.ARG_SOURCE_TAB to "WORKOUT",
                NavKeys.ARG_MONTH to monthArg
            )
            try {
                findNavController().navigate(
                    R.id.action_workoutTabRootFragment_to_unifiedCalendarFragment,
                    bundle
                )
            } catch (e: Exception) {
                Log.e("WORKOUT_TAB", "Navigation failed: ${e.message}")
            }
        }
    }

    private fun setupRetry() {
        btnRetryWorkout?.setOnClickListener { fetchWorkoutSession() }
    }

    override fun onDestroyView() {
        rvWarmupItems?.adapter = null
        rvWorkoutItems?.adapter = null
        super.onDestroyView()
    }
}