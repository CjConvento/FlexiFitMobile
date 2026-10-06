package com.example.flexifitapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.CalendarHistoryDto
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.flexifitapp.utils.AppLogger

class UnifiedCalendarFragment : Fragment(R.layout.fragment_unified_calendar) {

    private val TAG = "UNIFIED_CALENDAR"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments (passed from navigation)
        val month = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
        val sourceTab = arguments?.getString(NavKeys.ARG_SOURCE_TAB) ?: "WORKOUT"

        AppLogger.d(TAG, "Fragment Started: Month=$month, Tab=$sourceTab")

        // Month dropdown
        val ddMonth = view.findViewById<MaterialAutoCompleteTextView>(R.id.ddMonth)
        ddMonth.setText("Month $month", false)

        // Back button
        val btnOpenCalendar = view.findViewById<ImageButton>(R.id.btnOpenCalendar)
        btnOpenCalendar.setOnClickListener {
            // Navigate back to the appropriate tab
            if (sourceTab == "NUTRITION") {
                findNavController().navigate(R.id.nutritionTabRootFragment)
            } else {
                findNavController().navigate(R.id.WorkoutTabRootFragment)
            }
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvCalendar)
        rv.layoutManager = GridLayoutManager(requireContext(), 7)

        fetchCalendarData(rv, sourceTab)
    }

    private fun fetchCalendarData(rv: RecyclerView, sourceTab: String) {
        val api = ApiClient.api()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                AppLogger.d(TAG, "Calling getCalendarHistory()")
                val response = api.getCalendarHistory()

                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()
                    AppLogger.d(TAG, "API Success: Found ${historyList.size} records")

                    val calendarItems = generateCalendarDays(historyList)

                    rv.adapter = CalendarAdapter(calendarItems) { selectedDay ->
                        val month = arguments?.getInt(NavKeys.ARG_MONTH, 1) ?: 1
                        val bundle = bundleOf(
                            NavKeys.ARG_SOURCE_TAB to sourceTab,
                            NavKeys.ARG_DAY to selectedDay,
                            NavKeys.ARG_MONTH to month
                        )
                        findNavController().navigate(R.id.action_unifiedCalendarFragment_to_dayHostFragment, bundle)
                    }

                } else {
                    AppLogger.e(TAG, "API Error: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load calendar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Generate 28 days (weeks 1-4) and align them with the first day of the month (Sunday/Monday).
     * Uses the history data to set status colors and icons.
     */
    private fun generateCalendarDays(historyList: List<CalendarHistoryDto>): List<CalendarDay> {
        val calendarDays = mutableListOf<CalendarDay>()
        val today = LocalDate.now()

        // Determine currentDay.
        // Fallback: find the first "PENDING" or "NOT_STARTED" day. If all completed, default to 28.
        val currentDay = historyList.firstOrNull {
            it.status == "PENDING" || it.status == "NOT_STARTED"
        }?.day ?: 28

        // Gamitin ang month argument kung meron, fallback sa current month
        val monthArg = arguments?.getInt(NavKeys.ARG_MONTH, today.monthValue) ?: today.monthValue
        val currentYear = today.year
        val firstDayOfMonth = LocalDate.of(currentYear, monthArg, 1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // 0 = Sunday

        // Add blank cells for days before the 1st
        for (i in 0 until firstDayOfWeek) {
            calendarDays.add(
                CalendarDay(
                    dayNumber = null,
                    isClickable = false,
                    status = "BLANK"
                )
            )
        }

        // Create a map for quick lookup of day data (days 1–28)
        val historyMap = historyList.associateBy { it.day }

        // Days 1–28 (all days in the program)
        for (day in 1..28) {
            val history = historyMap[day]

            // Clickable LANG kung araw na 'to o nakaraan na (relative sa currentDay ng program)
            val isClickable = day <= currentDay

            // I-highlight LANG ang actual current day ng program, hindi today.dayOfMonth
            val isCurrentDay = day == currentDay

            // Build the CalendarDay object using the full data from history
            calendarDays.add(
                CalendarDay(
                    dayNumber = day,
                    isClickable = isClickable,
                    status = history?.status ?: "NOT_STARTED",
                    workoutStatus = history?.workoutStatus,
                    nutritionStatus = history?.nutritionStatus,
                    dayType = history?.dayType,
                    summary = history?.summary,
                    isCurrentDay = isCurrentDay
                )
            )
        }

        calendarDays.forEachIndexed { index, day ->
            AppLogger.d("CALENDAR_DEBUG", "Index $index: dayNumber=${day.dayNumber}, isClickable=${day.isClickable}, isCurrentDay=${day.isCurrentDay}")
        }

        // Optionally, add empty cells at the end to fill the grid (not necessary, RecyclerView will handle)
        return calendarDays

    }
}