package com.example.flexifitapp

import android.os.Bundle
import android.util.Log // Import natin ito babe para sa CCTV
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
import java.time.LocalDate

class UnifiedCalendarFragment : Fragment(R.layout.fragment_unified_calendar) {

    private val TAG = "DEBUG_CALENDAR"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val month = arguments?.getInt("ARG_MONTH", 1) ?: 1
        val sourceTab = arguments?.getString("ARG_SOURCE_TAB") ?: "WORKOUT"

        Log.d(TAG, "Fragment Started: Month=$month, Tab=$sourceTab")

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
        fetchCalendarData(rv, month, sourceTab)
    }

    private fun fetchCalendarData(rv: RecyclerView, month: Int, sourceTab: String) {
        val api = ApiClient.api(requireContext())
        val year = 2024

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling API: month=$month, year=$year, type=$sourceTab")
                val response = api.getCalendarHistory(month, year, sourceTab)

                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()
                    Log.d(TAG, "API Success: Found ${historyList.size} records")

                    // ✅ Use the new CalendarHistoryDto with full status info
                    val calendarItems = generateCalendarDays(historyList, month, year)

                    rv.adapter = CalendarAdapter(calendarItems) { selectedDay ->
                        Log.d(TAG, "Clicked Day: $selectedDay")
                        val b = bundleOf(
                            NavKeys.ARG_SOURCE_TAB to sourceTab,
                            NavKeys.ARG_MONTH to month,
                            NavKeys.ARG_DAY to selectedDay
                        )
                        findNavController().navigate(R.id.action_unifiedCalendarFragment_to_dayHostFragment, b)
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }}