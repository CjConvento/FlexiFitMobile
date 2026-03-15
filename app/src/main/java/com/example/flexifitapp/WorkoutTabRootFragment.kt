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
import com.example.flexifitapp.workout.WorkoutItem
import com.example.flexifitapp.workout.WorkoutRepository
import com.example.flexifitapp.workout.WorkoutSessionResponse
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.time.LocalDate

class WorkoutTabRootFragment : Fragment(R.layout.fragment_workout) {

    private var day: Int = -1
    private var fromHost: Boolean = false

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

    private var tvWorkoutDayTitle: TextView? = null
    private var tvWorkoutPlanDate: TextView? = null
    private var tvProgramHeader: TextView? = null
    private var tvCurrentProgramName: TextView? = null
    private var tvProgramProgress: TextView? = null
    private var tvWorkoutError: TextView? = null

    // Summary Fields (Important for Totals!)
    private var tvTotalTime: TextView? = null
    private var tvTotalCalories: TextView? = null

    private var progressWorkoutLoading: ProgressBar? = null
    private var layoutWarmupHeader: View? = null
    private var layoutWorkoutHeader: View? = null
    private var layoutWorkoutSessionBottomActions: View? = null

    private var rvWarmupItems: RecyclerView? = null
    private var rvWorkoutItems: RecyclerView? = null

    private var warmupExpanded = true
    private var workoutExpanded = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readArgs()
        bindViews(view)
        setupCalendarButton()
        setupRecyclerViews()
        setupExpandCollapse()
        setupProgramNavigation()
        setupSessionButtons()
        setupRetry()

        fetchWorkoutSession()
    }

    private fun readArgs() {
        day = arguments?.getInt("ARG_DAY", -1) ?: -1
        fromHost = arguments?.getBoolean("ARG_FROM_HOST", false) ?: false
    }

    private fun bindViews(view: View) {
        btnBackWorkoutPlan = view.findViewById(R.id.btnBackWorkoutPlan)
        btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar)
        tvWorkoutPlanDate = view.findViewById(R.id.tvWorkoutPlanDate)
        tvWorkoutDayTitle = view.findViewById(R.id.tvSessionLabel)

        btnPrevProgram = view.findViewById(R.id.btnPrevProgram)
        btnNextProgram = view.findViewById(R.id.btnNextProgram)
        tvProgramHeader = view.findViewById(R.id.tvProgramHeader)
        tvCurrentProgramName = view.findViewById(R.id.tvCurrentProgramName)
        tvProgramProgress = view.findViewById(R.id.tvProgramProgress)

        // I-bind natin yung totals para sa duration at calories card
        tvTotalTime = view.findViewById(R.id.tvWorkoutDuration)
        tvTotalCalories = view.findViewById(R.id.tvWorkoutCalories)

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

    private fun fetchWorkoutSession() {
        lifecycleScope.launch {
            showLoading()
            try {
                val api = ApiClient.api(requireContext())
                val repository = WorkoutRepository(api)
                val monthArg = arguments?.getInt("ARG_MONTH", 1) ?: 1

                val response = if (fromHost && day > 0) {
                    repository.getWorkoutByDate(day, monthArg)
                } else {
                    repository.getTodayWorkout()
                }

                if (response != null) {
                    updateUI(response)
                    showContent()

                    // Kung historical view (from calendar), itago ang buttons
                    if (fromHost) {
                        layoutWorkoutSessionBottomActions?.visibility = View.GONE
                    }
                } else {
                    showError("No record found for this day.")
                }
            } catch (e: Exception) {
                showError("Connection error. Check your API, babe!")
            }
        }
    }

    private fun updateUI(response: WorkoutSessionResponse) {
        // 1. Header Information
        tvWorkoutPlanDate?.text = "Month ${response.program.month} - Week ${response.program.week} - Day ${response.dayNo}"
        tvProgramHeader?.text = "Program ${response.program.programId}"
        tvCurrentProgramName?.text = response.program.programName
        tvProgramProgress?.text = response.program.description

        // 2. Display Totals from C# Calculations
        tvTotalTime?.text = "${response.totalDuration} mins"
        tvTotalCalories?.text = "${response.estimatedCalories} kcal"

        // 3. Status handling para sa "Complete" Button
        val status = response.program.status
        if (status.equals("Completed", ignoreCase = true)) {
            btnDoneWorkoutSession?.isEnabled = false
            btnDoneWorkoutSession?.text = "Session Completed"
            btnSkipWorkoutSession?.isVisible = false
        } else {
            btnDoneWorkoutSession?.isEnabled = true
            btnDoneWorkoutSession?.text = "Complete"
            btnSkipWorkoutSession?.isVisible = true
        }

        tvWorkoutDayTitle?.text = if (fromHost && day > 0) "Workout - Day $day" else "Workout - Day ${response.dayNo}"

        // 4. Set Adapters
        rvWarmupItems?.adapter = WorkoutAdapter(response.warmups) { item -> openWorkoutDetail(item) }
        rvWorkoutItems?.adapter = WorkoutAdapter(response.workouts) { item -> openWorkoutDetail(item) }
    }

    private fun openWorkoutDetail(item: com.example.flexifitapp.workout.WorkoutItem) {
        // Bridge from List to Details: Siguraduhin na match ang keys dito sa getString/getInt ng DetailsFragment
        val bundle = bundleOf(
            "workoutId" to item.id,
            "workoutName" to item.name,
            "image" to item.imageFileName,
            "muscleGroup" to item.muscleGroup,
            "sets" to item.sets,
            "reps" to item.reps,
            "rest" to item.restSeconds,
            "duration" to item.durationMinutes, // Mapasa ang duration galing C# logic
            "calories" to item.calories,         // Mapasa ang calories galing C# logic
            "description" to item.description,
            "videoUrl" to item.videoUrl
        )
        findNavController().navigate(R.id.workoutDetailFragment, bundle)
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
        btnDoneWorkoutSession?.setOnClickListener {
            val ctx = requireContext()
            // Mark as done logic (Locally + Achievement fire)
            UserPrefs.putInt(ctx, "COMPLETED_WORKOUTS_COUNT", UserPrefs.getInt(ctx, "COMPLETED_WORKOUTS_COUNT", 0) + 1)
            UserPrefs.putString(ctx, "LAST_WORKOUT_DATE", LocalDate.now().toString())

            AchievementEngine.updateAchievementsLocally(ctx)
            Toast.makeText(ctx, "Workout marked done! ✨", Toast.LENGTH_SHORT).show()

            // Dito mo pwedeng tawagin yung API POST /api/workout/complete kung gusto mo i-save sa DB
        }

        btnBackWorkoutPlan?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showLoading() {
        progressWorkoutLoading?.isVisible = true
        layoutWarmupHeader?.isVisible = false
        layoutWorkoutHeader?.isVisible = false
        layoutWorkoutSessionBottomActions?.isVisible = false
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

    private fun setupCalendarButton() {
        btnOpenCalendar?.isVisible = !fromHost
        btnOpenCalendar?.setOnClickListener {
            val bundle = bundleOf("ARG_SOURCE_TAB" to "WORKOUT")
            findNavController().navigate(R.id.action_workoutTabRootFragment_to_unifiedCalendarFragment, bundle)
        }
    }

    private fun setupProgramNavigation() {
        btnPrevProgram?.setOnClickListener { Toast.makeText(requireContext(), "Prev day logic here", Toast.LENGTH_SHORT).show() }
        btnNextProgram?.setOnClickListener { Toast.makeText(requireContext(), "Next day logic here", Toast.LENGTH_SHORT).show() }
    }

    private fun setupRetry() { btnRetryWorkout?.setOnClickListener { fetchWorkoutSession() } }

    override fun onDestroyView() {
        // Clean up references to prevent memory leaks
        btnBackWorkoutPlan = null; btnOpenCalendar = null; btnPrevProgram = null; btnNextProgram = null
        btnToggleWarmup = null; btnToggleWorkouts = null; tvWorkoutDayTitle = null; tvWorkoutPlanDate = null
        tvProgramHeader = null; tvCurrentProgramName = null; tvProgramProgress = null; rvWarmupItems = null
        rvWorkoutItems = null; layoutWarmupHeader = null; layoutWorkoutHeader = null
        layoutWorkoutSessionBottomActions = null; progressWorkoutLoading = null
        tvTotalTime = null; tvTotalCalories = null
        super.onDestroyView()
    }
}