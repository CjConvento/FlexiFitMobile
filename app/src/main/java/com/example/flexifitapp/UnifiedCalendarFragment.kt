package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView

class UnifiedCalendarFragment : Fragment(R.layout.fragment_unified_calendar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val month = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        val sourceTab = arguments?.getString(NavKeys.ARG_SOURCE_TAB) ?: "WORKOUT"

        // ✅ Use the month title in calendar layout (NOT tv_nutri_title)
        val monthTitle = view.findViewById<TextView>(R.id.txtMonthTitle)
        monthTitle.text = "Month $month"

        // ✅ Back button (btnOpenCalendar) - returns to the source tab
        val btnOpenCalendar = view.findViewById<ImageButton>(R.id.btnOpenCalendar)
        btnOpenCalendar.setOnClickListener {
            if (sourceTab == "NUTRITION") {
                findNavController().navigate(R.id.nutritionTabRootFragment)
            } else {
                findNavController().navigate(R.id.workoutTabRootFragment)
            }
        }

        // ✅ Setup calendar grid
        val rv = view.findViewById<RecyclerView>(R.id.rvCalendar)

        // Simple 28-day month grid (no blanks). Pwede natin lagyan blanks later.
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

        // Optional: prev/next month buttons (placeholder; ikaw mag-rules)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)

        btnPrev.setOnClickListener {
            // TODO: month-1 logic (optional)
        }

        btnNext.setOnClickListener {
            // TODO: month+1 logic (optional)
        }
    }
}