package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class WorkoutTabRootFragment : Fragment(R.layout.fragment_workout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val day = arguments?.getInt(NavKeys.ARG_DAY, -1) ?: -1
        val fromHost = arguments?.getBoolean(NavKeys.ARG_FROM_HOST, false) ?: false

        val btnOpenCalendar = view.findViewById<ImageButton>(R.id.btnOpenCalendar)
        val title = view.findViewById<TextView?>(R.id.tv_today_title) // if meron ka

        // If inside DayHost: hide calendar button
        btnOpenCalendar.visibility = if (fromHost) View.GONE else View.VISIBLE

        // Optional title change (if your layout has txtWorkoutDayTitle)
        title?.text = if (day == -1) "Workout" else "Workout - Day $day"

        // Only root mode needs calendar open
        if (!fromHost) {
            btnOpenCalendar.setOnClickListener {
                val b = bundleOf(NavKeys.ARG_SOURCE_TAB to "WORKOUT")
                findNavController().navigate(
                    R.id.action_workoutTabRootFragment_to_unifiedCalendarFragment,
                    b
                )
            }
        }
    }
}