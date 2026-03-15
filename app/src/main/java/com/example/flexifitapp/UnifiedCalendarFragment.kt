package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.CalendarDay
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch

class UnifiedCalendarFragment : Fragment(R.layout.fragment_unified_calendar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val month = arguments?.getInt("ARG_MONTH", 1) ?: 1
        val sourceTab = arguments?.getString("ARG_SOURCE_TAB") ?: "WORKOUT"

        val ddMonth = view.findViewById<MaterialAutoCompleteTextView>(R.id.ddMonth)
        ddMonth.setText("Month $month", false)

        val btnOpenCalendar = view.findViewById<ImageButton>(R.id.btnOpenCalendar)
        btnOpenCalendar.setOnClickListener {
            if (sourceTab == "NUTRITION") {
                findNavController().navigate(R.id.nutritionTabRootFragment)
            } else {
                findNavController().navigate(R.id.workoutTabRootFragment)
            }
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvCalendar)

        // 1. Dito na tayo tatawag sa API babe
        fetchCalendarData(rv, month, sourceTab)
    }

    private fun fetchCalendarData(rv: RecyclerView, month: Int, sourceTab: String) {
        val api = ApiClient.api(requireContext())
        val year = 2024 // Pwedeng gawing dynamic ito babe

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getCalendarHistory(month, year, sourceTab)
                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()

                    // I-map ang history sa isang Map para mabilis i-check ang status ng bawat araw
                    val historyMap = historyList.associateBy { it.day }

                    val calendarItems = mutableListOf<CalendarDay>()

                    // 1. Alamin ang "Day 1" ng buwan para sa tamang alignment
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, month - 1, 1)
                    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

                    // 2. Mag-add ng BLANK cells sa simula
                    for (i in 1 until firstDayOfWeek) {
                        calendarItems.add(CalendarDay(dayNumber = null, isClickable = false, isCompleted = false))
                    }

                    // 3. Alamin kung ilang araw meron sa buwan
                    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

                    // 4. I-generate ang mga araw na may tamang status
                    for (i in 1..daysInMonth) {
                        val record = historyMap[i]
                        calendarItems.add(
                            CalendarDay(
                                dayNumber = i,
                                isClickable = record != null,
                                isCompleted = record?.isCompleted ?: false
                            )
                        )
                    }

                    rv.adapter = CalendarAdapter(calendarItems) { selectedDay ->
                        val b = bundleOf(
                            "ARG_SOURCE_TAB" to sourceTab,
                            "ARG_MONTH" to month,
                            "ARG_DAY" to selectedDay
                        )
                        findNavController().navigate(R.id.action_unifiedCalendarFragment_to_dayHostFragment, b)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}