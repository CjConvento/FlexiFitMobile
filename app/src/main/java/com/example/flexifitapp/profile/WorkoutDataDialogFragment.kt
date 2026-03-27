package com.example.flexifitapp.profile

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class WorkoutDataDialogFragment : DialogFragment(R.layout.dialog_workout_data) {

    private var btnBack: ImageView? = null

    private var rowProgramsHeader: LinearLayout? = null
    private var rowGoalsHeader: LinearLayout? = null
    private var rowTotalWorkoutsHeader: LinearLayout? = null
    private var rowTotalSessionsHeader: LinearLayout? = null

    private var ivProgramsAction: ImageView? = null
    private var ivGoalsAction: ImageView? = null
    private var ivTotalWorkoutsAction: ImageView? = null
    private var ivTotalSessionsAction: ImageView? = null

    private var layoutProgramsContent: LinearLayout? = null
    private var layoutGoalsContent: LinearLayout? = null
    private var layoutTotalWorkoutsContent: LinearLayout? = null
    private var layoutTotalSessionsContent: LinearLayout? = null

    private var tvProgramsValue: TextView? = null
    private var tvGoalsValue: TextView? = null
    private var tvTotalWorkoutsValue: TextView? = null
    private var tvTotalSessionsValue: TextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupClicks()

        // TAWAGIN MO ITONG DALAWA BABE:
        loadWorkoutData()  // Para sa Programs at Goals
        loadWorkoutStats() // Para sa Numbers (Sessions/Workouts)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        btnBack = null

        rowProgramsHeader = null
        rowGoalsHeader = null
        rowTotalWorkoutsHeader = null
        rowTotalSessionsHeader = null

        ivProgramsAction = null
        ivGoalsAction = null
        ivTotalWorkoutsAction = null
        ivTotalSessionsAction = null

        layoutProgramsContent = null
        layoutGoalsContent = null
        layoutTotalWorkoutsContent = null
        layoutTotalSessionsContent = null

        tvProgramsValue = null
        tvGoalsValue = null
        tvTotalWorkoutsValue = null
        tvTotalSessionsValue = null
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)

        rowProgramsHeader = view.findViewById(R.id.rowProgramsHeader)
        rowGoalsHeader = view.findViewById(R.id.rowGoalsHeader)
        rowTotalWorkoutsHeader = view.findViewById(R.id.rowTotalWorkoutsHeader)
        rowTotalSessionsHeader = view.findViewById(R.id.rowTotalSessionsHeader)

        ivProgramsAction = view.findViewById(R.id.ivProgramsAction)
        ivGoalsAction = view.findViewById(R.id.ivGoalsAction)
        ivTotalWorkoutsAction = view.findViewById(R.id.ivTotalWorkoutsAction)
        ivTotalSessionsAction = view.findViewById(R.id.ivTotalSessionsAction)

        layoutProgramsContent = view.findViewById(R.id.layoutProgramsContent)
        layoutGoalsContent = view.findViewById(R.id.layoutGoalsContent)
        layoutTotalWorkoutsContent = view.findViewById(R.id.layoutTotalWorkoutsContent)
        layoutTotalSessionsContent = view.findViewById(R.id.layoutTotalSessionsContent)

        tvProgramsValue = view.findViewById(R.id.tvProgramsValue)
        tvGoalsValue = view.findViewById(R.id.tvGoalsValue)
        tvTotalWorkoutsValue = view.findViewById(R.id.tvTotalWorkoutsValue)
        tvTotalSessionsValue = view.findViewById(R.id.tvTotalSessionsValue)
    }

    private fun setupClicks() {
        btnBack?.setOnClickListener { dismiss() }

        rowProgramsHeader?.setOnClickListener {
            toggleSection(layoutProgramsContent, ivProgramsAction)
        }

        rowGoalsHeader?.setOnClickListener {
            toggleSection(layoutGoalsContent, ivGoalsAction)
        }

        rowTotalWorkoutsHeader?.setOnClickListener {
            toggleSection(layoutTotalWorkoutsContent, ivTotalWorkoutsAction)
        }

        rowTotalSessionsHeader?.setOnClickListener {
            toggleSection(layoutTotalSessionsContent, ivTotalSessionsAction)
        }
    }

    private fun loadWorkoutData() {
        val ctx = requireContext()

        val selectedPrograms = UserPrefs.getStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS)
        val fitnessGoals = UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET)

// 🔽 Add these two lines here
        Log.d("WorkoutDialog", "selectedPrograms = $selectedPrograms")
        Log.d("WorkoutDialog", "fitnessGoals = $fitnessGoals")

        tvProgramsValue?.text = if (selectedPrograms.isNotEmpty()) {
            selectedPrograms.joinToString("\n") { prettifyValue(it) }
        } else {
            "Not set"
        }

        tvGoalsValue?.text = if (fitnessGoals.isNotEmpty()) {
            fitnessGoals.joinToString("\n") { prettifyValue(it) }
        } else {
            "Not set"
        }
    }

    private fun loadWorkoutStats() {
        val ctx = context ?: return
        val sessions = UserPrefs.getInt(ctx, "total_sessions", 0)
        val workouts = UserPrefs.getInt(ctx, "total_workouts", 0)
        renderWorkoutStats(totalWorkouts = workouts, totalSessions = sessions)
    }

    private fun renderWorkoutStats(totalWorkouts: Int, totalSessions: Int) {
        tvTotalWorkoutsValue?.text = totalWorkouts.toString()
        tvTotalSessionsValue?.text = totalSessions.toString()
    }

    private fun toggleSection(content: View?, icon: ImageView?) {
        val isVisible = content?.visibility == View.VISIBLE

        if (isVisible) {
            content?.visibility = View.GONE
            icon?.setImageResource(R.drawable.baseline_arrow_right_ios_new_24)
        } else {
            content?.visibility = View.VISIBLE
            icon?.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
        }
    }

    private fun prettifyValue(value: String): String {
        return value
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { part ->
                part.replaceFirstChar { ch -> ch.uppercase() }
            }
    }
}