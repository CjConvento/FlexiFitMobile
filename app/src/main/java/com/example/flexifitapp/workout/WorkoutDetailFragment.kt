package com.example.flexifitapp.workout

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.flexifitapp.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class WorkoutDetailFragment : Fragment(R.layout.fragment_workout_detail) {

    // UI References
    private var btnBackWorkoutDetail: ImageButton? = null
    private var btnHelpWorkoutDetail: ImageButton? = null
    private var btnOpenWorkoutTutorial: MaterialButton? = null
    private var btnMarkDone: MaterialButton? = null
    private var btnSkipExercise: MaterialButton? = null

    private var ivWorkoutHeroImage: ImageView? = null
    private var tvWorkoutDetailTitle: TextView? = null
    private var tvWorkoutMuscleGroup: TextView? = null
    private var tvWorkoutSets: TextView? = null
    private var tvWorkoutReps: TextView? = null
    private var tvWorkoutRest: TextView? = null
    private var tvWorkoutDuration: TextView? = null
    private var tvWorkoutCalories: TextView? = null
    private var tvWorkoutDetailDescription: TextView? = null

    private var videoUrl: String? = null
    private var helpPopupWindow: PopupWindow? = null

    // Ito yung susi para maging "By Item" ang tracking natin babe
    private var currentExerciseId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        displayExerciseDetails()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnBackWorkoutDetail = view.findViewById(R.id.btnBackWorkoutDetail)
        btnHelpWorkoutDetail = view.findViewById(R.id.btnOpenWorkoutTutorial)
        btnOpenWorkoutTutorial = view.findViewById(R.id.btnOpenWorkoutTutorial)

        // Eto yung buttons sa labas ng card na scrollable
        btnMarkDone = view.findViewById(R.id.btnLogMeal)
        btnSkipExercise = view.findViewById(R.id.btnSkipMeal)

        ivWorkoutHeroImage = view.findViewById(R.id.ivWorkoutHeroImage)
        tvWorkoutDetailTitle = view.findViewById(R.id.tvWorkoutDetailTitle)
        tvWorkoutMuscleGroup = view.findViewById(R.id.tvWorkoutMuscleGroup)
        tvWorkoutSets = view.findViewById(R.id.tvWorkoutSets)
        tvWorkoutReps = view.findViewById(R.id.tvWorkoutReps)
        tvWorkoutRest = view.findViewById(R.id.tvWorkoutRest)
        tvWorkoutDuration = view.findViewById(R.id.tvWorkoutDuration)
        tvWorkoutCalories = view.findViewById(R.id.tvWorkoutCalories)
        tvWorkoutDetailDescription = view.findViewById(R.id.tvWorkoutDetailDescription)
    }

    private fun displayExerciseDetails() {
        // Kunin ang ID ng specific na item
        currentExerciseId = arguments?.getInt("id") ?: -1

        tvWorkoutDetailTitle?.text = arguments?.getString("name")
        tvWorkoutMuscleGroup?.text = arguments?.getString("muscleGroup") ?: "Full Body"
        tvWorkoutSets?.text = "${arguments?.getInt("sets")} Sets"
        tvWorkoutReps?.text = "${arguments?.getInt("reps")} Reps"
        tvWorkoutRest?.text = "${arguments?.getInt("restSeconds")}s Rest"
        tvWorkoutDuration?.text = "${arguments?.getInt("durationMinutes")} mins"
        tvWorkoutCalories?.text = "${arguments?.getInt("calories")} kcal"
        tvWorkoutDetailDescription?.text = arguments?.getString("description")
        videoUrl = arguments?.getString("videoUrl")

        Glide.with(this)
            .load(arguments?.getString("imageFileName"))
            .placeholder(R.drawable.ic_food_placeholder)
            .into(ivWorkoutHeroImage!!)

        // CHECK BY ITEM STATUS: Kung tapos na itong specific item na 'to, itago ang buttons
        val isCompleted = arguments?.getBoolean("isCompleted") ?: false
        if (isCompleted) {
            btnMarkDone?.isVisible = false
            btnSkipExercise?.isVisible = false
            // Optional: Maglagay ng "Completed" text or badge
            tvWorkoutDetailTitle?.append(" (Done ✅)")
        }
    }

    private fun setupClickListeners() {
        btnBackWorkoutDetail?.setOnClickListener { parentFragmentManager.popBackStack() }
        btnOpenWorkoutTutorial?.setOnClickListener { openVideoTutorial() }
        btnHelpWorkoutDetail?.setOnClickListener { showHelpPopup(it) }

        // Mark Done click
        btnMarkDone?.setOnClickListener {
            updateItemStatus("DONE")
        }

        // Skip click
        btnSkipExercise?.setOnClickListener {
            updateItemStatus("SKIPPED")
        }
    }

    private fun updateItemStatus(status: String) {
        if (currentExerciseId == -1) {
            Toast.makeText(requireContext(), "Invalid Exercise ID", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // TODO: Tawagin ang Repository para i-update ang SPECIFIC exercise ID sa database
                // Example: val success = WorkoutRepository.updateExerciseStatus(currentExerciseId, status)

                // Temporary feedback
                val emoji = if (status == "DONE") "🦾" else "🚩"
                Toast.makeText(requireContext(), "Exercise $status! $emoji", Toast.LENGTH_SHORT).show()

                // Balik sa listahan para makita yung update (checkmark o strikethrough)
                parentFragmentManager.popBackStack()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showHelpPopup(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_workout_help, null)
        val popupWindow = PopupWindow(popupView, 600, ViewGroup.LayoutParams.WRAP_CONTENT)
        val btnPopupOpenTutorial = popupView.findViewById<MaterialButton>(R.id.btnPopupOpenTutorial)

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.elevation = 10f

        btnPopupOpenTutorial.setOnClickListener {
            popupWindow.dismiss()
            openVideoTutorial()
        }
        popupWindow.showAsDropDown(anchor, -180, 12)
        helpPopupWindow = popupWindow
    }

    private fun openVideoTutorial() {
        if (videoUrl.isNullOrBlank()) {
            Toast.makeText(requireContext(), "No tutorial available.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
        startActivity(intent)
    }

    override fun onDestroyView() {
        helpPopupWindow?.dismiss()
        // Cleanup UI references
        btnBackWorkoutDetail = null
        btnHelpWorkoutDetail = null
        btnOpenWorkoutTutorial = null
        btnMarkDone = null
        btnSkipExercise = null
        ivWorkoutHeroImage = null
        tvWorkoutDetailTitle = null
        tvWorkoutMuscleGroup = null
        tvWorkoutSets = null
        tvWorkoutReps = null
        tvWorkoutRest = null
        tvWorkoutDuration = null
        tvWorkoutCalories = null
        tvWorkoutDetailDescription = null
        super.onDestroyView()
    }
}