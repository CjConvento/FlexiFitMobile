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
    private var monthArg: Int = 1 // Idagdag mo 'to babe!

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

    // Sa readArgs() function babe
    private fun readArgs() {
        // Gamitin ang NavKeys para sa CCTV check
        day = arguments?.getInt(NavKeys.ARG_DAY, -1) ?: -1
        monthArg = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        fromHost = arguments?.getBoolean(NavKeys.ARG_FROM_HOST, false) ?: false

        android.util.Log.i("CCTV_WORKOUT", "🔍 Reading Args: Day=$day, Month=$monthArg, fromHost=$fromHost")
    }

    private fun bindViews(view: View) {
        // Essential Buttons
        btnBackWorkoutPlan = view.findViewById(R.id.btnBackWorkoutPlan)
        btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar)
        btnPrevProgram = view.findViewById(R.id.btnPrevProgram)
        btnNextProgram = view.findViewById(R.id.btnNextProgram)

        // Text Views
        tvWorkoutPlanDate = view.findViewById(R.id.tvWorkoutPlanDate)
        tvProgramHeader = view.findViewById(R.id.tvProgramHeader)
        tvCurrentProgramName = view.findViewById(R.id.tvCurrentProgramName)
        tvProgramProgress = view.findViewById(R.id.tvProgramProgress)
        tvWorkoutDayTitle = view.findViewById(R.id.tvSessionLabel)
        tvWorkoutStatus = view.findViewById(R.id.tvWorkoutStatus)
        ivStatusCycle = view.findViewById(R.id.ivStatusCycle)

        // List Containers & Toggles
        layoutWarmupHeader = view.findViewById(R.id.layoutWarmupHeader)
        btnToggleWarmup = view.findViewById(R.id.btnToggleWarmup)
        layoutWorkoutHeader = view.findViewById(R.id.layoutWorkoutHeader)
        btnToggleWorkouts = view.findViewById(R.id.btnToggleWorkouts)
        rvWarmupItems = view.findViewById(R.id.rvWarmupItems)
        rvWorkoutItems = view.findViewById(R.id.rvWorkoutItems)

        // Actions & Feedback
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

                // Dito babe, gagamitin na lang natin yung monthArg na galing sa readArgs()
                android.util.Log.d("CCTV_WORKOUT", "📡 Fetching: Day $day, Month $monthArg")

                val response = if (fromHost && day > 0) {
                    repository.getWorkoutByDate(day, monthArg)
                } else {
                    repository.getTodayWorkout()
                }

                if (response != null) {
                    updateUI(response)
                    showContent()

                    // I-update ang argument para sa navigation later
                    arguments?.putInt(NavKeys.ARG_MONTH, response.program.month)

                    if (fromHost) {
                        layoutWorkoutSessionBottomActions?.visibility = View.GONE
                    }
                } else {
                    showError("No record found for Day $day.")
                }
            } catch (e: Exception) {
                android.util.Log.e("CCTV_WORKOUT", "Error: ${e.message}")
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

        // 2. Status UI Update
        val status = response.program.status
        tvWorkoutStatus?.text = status
        if (status.equals("Completed", ignoreCase = true)) {
            ivStatusCycle?.setImageResource(R.drawable.rounded_checkbox) // Palitan mo to babe pag may check icon ka na
            btnDoneWorkoutSession?.isEnabled = false
            btnDoneWorkoutSession?.text = "Session Completed"
            btnSkipWorkoutSession?.isVisible = false
        }

        tvWorkoutDayTitle?.text = "Workout - Day ${response.dayNo}"

        // 3. Set Adapters
        rvWarmupItems?.adapter = WorkoutAdapter(response.warmups) { item -> openWorkoutDetail(item) }
        rvWorkoutItems?.adapter = WorkoutAdapter(response.workouts) { item -> openWorkoutDetail(item) }
    }

    private fun openWorkoutDetail(item: WorkoutItem) {
        // Gamitin ang action ID para sa animations babe!
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
        // 1. CCTV Check: Alamin kung ano ang monthArg bago lumipat
        android.util.Log.d("CCTV_NAV", "Setup Calendar Button. Current monthArg value: $monthArg")

        // Itago ang calendar icon kung tinitignan natin ang history (fromHost)
        btnOpenCalendar?.isVisible = !fromHost

        btnOpenCalendar?.setOnClickListener {
            // 2. Gamitin ang NavKeys para siguradong walang typo
            val bundle = bundleOf(
                NavKeys.ARG_SOURCE_TAB to "WORKOUT",
                NavKeys.ARG_MONTH to monthArg // Ito yung global variable na nilagay natin sa taas
            )

            android.util.Log.i("CCTV_NAV", "🚀 Navigating to Calendar. Passing Month: $monthArg")

            // Gamitin ang ID mula sa nav_graph mo babe!
            try {
                findNavController().navigate(
                    R.id.action_workoutTabRootFragment_to_unifiedCalendarFragment,
                    bundle
                )
            } catch (e: Exception) {
                android.util.Log.e("CCTV_NAV", "❌ Navigation failed: ${e.message}")
            }
        }
    }

    private fun setupProgramNavigation() {
        btnPrevProgram?.setOnClickListener { Toast.makeText(requireContext(), "Prev day logic here", Toast.LENGTH_SHORT).show() }
        btnNextProgram?.setOnClickListener { Toast.makeText(requireContext(), "Next day logic here", Toast.LENGTH_SHORT).show() }
    }

    private fun setupRetry() { btnRetryWorkout?.setOnClickListener { fetchWorkoutSession() } }

    override fun onDestroyView() {
        // 1. Linisin ang Adapters (Important babe!)
        rvWarmupItems?.adapter = null
        rvWorkoutItems?.adapter = null

        // 2. I-null lahat ng UI references para ma-garbage collect
        btnBackWorkoutPlan = null
        btnOpenCalendar = null
        btnPrevProgram = null
        btnNextProgram = null
        btnToggleWarmup = null
        btnToggleWorkouts = null
        btnRetryWorkout = null // Dagdag natin 'to babe
        btnSkipWorkoutSession = null
        btnDoneWorkoutSession = null

        tvWorkoutDayTitle = null
        tvWorkoutPlanDate = null
        tvProgramHeader = null
        tvCurrentProgramName = null
        tvProgramProgress = null
        tvWorkoutError = null

        progressWorkoutLoading = null
        layoutWarmupHeader = null
        layoutWorkoutHeader = null
        layoutWorkoutSessionBottomActions = null

        rvWarmupItems = null
        rvWorkoutItems = null

        // 3. Always call super last para tapos na ang sarili nating cleanup
        super.onDestroyView()
    }
}