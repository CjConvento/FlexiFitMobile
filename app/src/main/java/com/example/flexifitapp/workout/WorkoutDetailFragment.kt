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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.flexifitapp.R
import com.google.android.material.button.MaterialButton

class WorkoutDetailFragment : Fragment(R.layout.fragment_workout_detail) {

    private var btnBackWorkoutDetail: ImageButton? = null
    private var btnHelpWorkoutDetail: ImageButton? = null
    private var btnOpenWorkoutTutorial: MaterialButton? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        displayExerciseDetails()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnBackWorkoutDetail = view.findViewById(R.id.btnBackWorkoutDetail)
        btnOpenWorkoutTutorial = view.findViewById(R.id.btnOpenWorkoutTutorial)

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
        // Retrieve arguments with the correct keys
        tvWorkoutDetailTitle?.text = arguments?.getString("workoutName")
        tvWorkoutMuscleGroup?.text = arguments?.getString("muscleGroup") ?: "Full Body"
        tvWorkoutSets?.text = "${arguments?.getInt("sets")} Sets"
        tvWorkoutReps?.text = "${arguments?.getInt("reps")} Reps"
        tvWorkoutRest?.text = "${arguments?.getInt("rest")}s Rest"
        tvWorkoutDuration?.text = "${arguments?.getInt("duration")} mins"
        tvWorkoutCalories?.text = "${arguments?.getInt("calories")} kcal"
        tvWorkoutDetailDescription?.text = arguments?.getString("description")
        videoUrl = arguments?.getString("videoUrl")

        val imageUrl = arguments?.getString("image")
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_workout)
            .into(ivWorkoutHeroImage!!)

        val isCompleted = arguments?.getBoolean("isCompleted") ?: false
        if (isCompleted) {
            tvWorkoutDetailTitle?.append(" (Completed ✓)")
        }
    }

    private fun setupClickListeners() {
        btnBackWorkoutDetail?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        btnHelpWorkoutDetail?.setOnClickListener {
            showHelpPopup(it)
        }
        btnOpenWorkoutTutorial?.setOnClickListener {
            openVideoTutorial()
        }
    }

    private fun showHelpPopup(anchor: View) {
        helpPopupWindow?.dismiss()

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
            Toast.makeText(requireContext(), "No tutorial available", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
        startActivity(intent)
    }

    override fun onDestroyView() {
        helpPopupWindow?.dismiss()
        helpPopupWindow = null
        super.onDestroyView()
    }
}