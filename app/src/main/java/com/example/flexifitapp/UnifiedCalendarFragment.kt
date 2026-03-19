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
        val year = 2024 // Pwedeng Calendar.getInstance().get(Calendar.YEAR) babe

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling API: month=$month, year=$year, type=$sourceTab")
                val response = api.getCalendarHistory(month, year, sourceTab)

                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()
                    Log.d(TAG, "API Success: Found ${historyList.size} records from DB")

                    val historyMap = historyList.associateBy { it.day }
                    val calendarItems = mutableListOf<CalendarDay>()

                    // 1. Alignment Logic
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, month - 1, 1)
                    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

                    // Petsa Ngayon (Para sa timeline logic)
                    val todayDate = LocalDate.now()
                    val isCurrentMonth = (month == todayDate.monthValue && year == todayDate.year)

                    // 2. Blank cells (No logs needed here, babe)
                    for (i in 1 until firstDayOfWeek) {
                        calendarItems.add(CalendarDay(dayNumber = null, isClickable = false, status = "BLANK"))
                    }

                    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

                    // 3. Loop with History/Present/Future Logic
                    for (i in 1..daysInMonth) {
                        val record = historyMap[i]

                        // 🔥 THE TIMELINE LOGIC:
                        val finalStatus = when {
                            // May record sa Database (History)
                            record != null -> record.status

                            // History: Tapos na ang araw pero walang record sa DB
                            (month < todayDate.monthValue) || (isCurrentMonth && i < todayDate.dayOfMonth) -> "SKIPPED"

                            // Present: Ngayong araw mismo
                            (isCurrentMonth && i == todayDate.dayOfMonth) -> "PENDING"

                            // Future: Darating pa lang
                            else -> "NOT_STARTED"
                        }

                        // CCTV Log para sa bawat araw
                        Log.v(TAG, "Day $i -> DB_Record=${record != null}, Final_Status=$finalStatus")

                        calendarItems.add(
                            CalendarDay(
                                dayNumber = i,
                                // Clickable lang kung tapos na o ngayong araw
                                isClickable = (month < todayDate.monthValue) || (isCurrentMonth && i <= todayDate.dayOfMonth),
                                status = finalStatus
                            )
                        )
                    }

                    rv.adapter = CalendarAdapter(calendarItems) { selectedDay ->
                        Log.d(TAG, "Clicked Day: $selectedDay, Navigating to DayHost...")
                        val b = bundleOf(
                            NavKeys.ARG_SOURCE_TAB to sourceTab,
                            NavKeys.ARG_MONTH to month,
                            NavKeys.ARG_DAY to selectedDay
                        )
                        android.util.Log.d("CCTV_CALENDAR", "🚀 Sending to DayHost: Day=$selectedDay, Tab=$sourceTab")
                        findNavController().navigate(R.id.action_unifiedCalendarFragment_to_dayHostFragment, b)
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "CRASH SA CALENDAR: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}