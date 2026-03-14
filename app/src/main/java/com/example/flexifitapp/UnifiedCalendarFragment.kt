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
        val api = ApiClient.api(requireContext()) // Gamit ang ApiClient mo

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Kunwari ang year ay 2024 muna
                val response = api.getCalendarHistory(month, 2024, sourceTab)

                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()

                    // 2. I-convert ang DTO papuntang CalendarDay model
                    val calendarItems = historyList.map { dto ->
                        CalendarDay(
                            dayNumber = dto.day,
                            isClickable = true // O base sa logic mo
                        )
                    }

                    // 3. I-set ang adapter gamit ang totoong data
                    rv.adapter = CalendarAdapter(calendarItems) { selectedDay ->
                        val b = bundleOf(
                            "ARG_SOURCE_TAB" to sourceTab,
                            "ARG_MONTH" to month,
                            "ARG_DAY" to selectedDay
                        )
                        findNavController().navigate(
                            R.id.action_unifiedCalendarFragment_to_dayHostFragment,
                            b
                        )
                    }
                } else {
                    Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}