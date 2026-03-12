package com.example.flexifitapp.workout

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
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

    companion object {
        private const val IMAGE_BASE_URL = "https://your-api-domain.com/images/workouts/"

        const val ARG_WORKOUT_ID = "workout_id"
        const val ARG_WORKOUT_NAME = "workout_name"
        const val ARG_WORKOUT_IMAGE_FILE_NAME = "workout_image_file_name"
        const val ARG_WORKOUT_MUSCLE_GROUP = "workout_muscle_group"
        const val ARG_WORKOUT_SETS = "workout_sets"
        const val ARG_WORKOUT_REPS = "workout_reps"
        const val ARG_WORKOUT_REST_SECONDS = "workout_rest_seconds"
        const val ARG_WORKOUT_DURATION_MINUTES = "workout_duration_minutes"
        const val ARG_WORKOUT_CALORIES = "workout_calories"
        const val ARG_WORKOUT_DESCRIPTION = "workout_description"
        const val ARG_WORKOUT_VIDEO_URL = "workout_video_url"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        bindWorkoutData()
        setupClicks()
    }

    private fun bindViews(view: View) {
        btnBackWorkoutDetail = view.findViewById(R.id.btnBackWorkoutDetail)
        btnHelpWorkoutDetail = view.findViewById(R.id.btnHelpWorkoutDetail)
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

    private fun bindWorkoutData() {
        val args = arguments ?: return

        val workoutName = args.getString(ARG_WORKOUT_NAME).orEmpty()
        val imageFileName = args.getString(ARG_WORKOUT_IMAGE_FILE_NAME).orEmpty()
        val muscleGroup = args.getString(ARG_WORKOUT_MUSCLE_GROUP).orEmpty()
        val sets = args.getInt(ARG_WORKOUT_SETS, 0)
        val reps = args.getInt(ARG_WORKOUT_REPS, 0)
        val restSeconds = args.getInt(ARG_WORKOUT_REST_SECONDS, 0)
        val durationMinutes = args.getInt(ARG_WORKOUT_DURATION_MINUTES, 0)
        val calories = args.getInt(ARG_WORKOUT_CALORIES, 0)
        val description = args.getString(ARG_WORKOUT_DESCRIPTION).orEmpty()
        videoUrl = args.getString(ARG_WORKOUT_VIDEO_URL)

        tvWorkoutDetailTitle?.text = workoutName
        tvWorkoutMuscleGroup?.text = muscleGroup
        tvWorkoutSets?.text = "$sets Sets"
        tvWorkoutReps?.text = "$reps Reps"
        tvWorkoutRest?.text = "$restSeconds sec"
        tvWorkoutDuration?.text = "$durationMinutes mins"
        tvWorkoutCalories?.text = "$calories kcal"
        tvWorkoutDetailDescription?.text = description

        loadWorkoutImage(imageFileName)
    }

    // Hanapin ang loadWorkoutImage function sa loob ng WorkoutDetailFragment.kt
    private fun loadWorkoutImage(imageFileName: String) {
        if (imageFileName.isBlank()) {
            ivWorkoutHeroImage?.setImageResource(R.drawable.ic_launcher_foreground)
            return
        }

        // Direct use of imageFileName as it is now a full URL from the C# API
        ivWorkoutHeroImage?.let { imageView ->
            Glide.with(this)
                .load(imageFileName)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView)
        }
    }

    private fun setupClicks() {
        btnBackWorkoutDetail?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnHelpWorkoutDetail?.setOnClickListener { anchor ->
            showHelpPopup(anchor)
        }

        btnOpenWorkoutTutorial?.setOnClickListener {
            openVideoTutorial()
        }
    }

    private fun showHelpPopup(anchor: View) {
        helpPopupWindow?.dismiss()

        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_workout_help, null, false)

        val btnPopupOpenTutorial =
            popupView.findViewById<MaterialButton>(R.id.btnPopupOpenTutorial)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.elevation = 10f

        btnPopupOpenTutorial.setOnClickListener {
            popupWindow.dismiss()
            openVideoTutorial()
        }

        helpPopupWindow = popupWindow
        popupWindow.showAsDropDown(anchor, -180, 12)
    }

    private fun openVideoTutorial() {
        val url = videoUrl

        if (url.isNullOrBlank()) {
            Toast.makeText(requireContext(), "No tutorial available.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Unable to open tutorial.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        helpPopupWindow?.dismiss()
        helpPopupWindow = null

        btnBackWorkoutDetail = null
        btnHelpWorkoutDetail = null
        btnOpenWorkoutTutorial = null
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