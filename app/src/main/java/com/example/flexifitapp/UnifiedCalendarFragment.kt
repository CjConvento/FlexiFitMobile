package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class UnifiedCalendarFragment : Fragment(R.layout.fragment_unified_calendar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val month = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        val sourceTab = arguments?.getString(NavKeys.ARG_SOURCE_TAB) ?: "WORKOUT"

        // Use ddMonth instead of txtMonthTitle
        val ddMonth = view.findViewById<MaterialAutoCompleteTextView>(R.id.ddMonth)
        ddMonth.setText("Month $month", false)

        // Back button
        val btnOpenCalendar = view.findViewById<ImageButton>(R.id.btnOpenCalendar)
        btnOpenCalendar.setOnClickListener {
            if (sourceTab == "NUTRITION") {
                findNavController().navigate(R.id.nutritionTabRootFragment)
            } else {
                findNavController().navigate(R.id.workoutTabRootFragment)
            }
        }

        // Calendar grid
        val rv = view.findViewById<RecyclerView>(R.id.rvCalendar)

        val items = (1..28).map { dayNum ->
            CalendarDay(dayNumber = dayNum, isClickable = true)
        }

        rv.adapter = CalendarAdapter(items) { day ->
            val b = bundleOf(
                NavKeys.ARG_SOURCE_TAB to sourceTab,
                NavKeys.ARG_MONTH to month,
                NavKeys.ARG_DAY to day
            )
            findNavController().navigate(
                R.id.action_unifiedCalendarFragment_to_dayHostFragment,
                b
            )
        }
    }
}