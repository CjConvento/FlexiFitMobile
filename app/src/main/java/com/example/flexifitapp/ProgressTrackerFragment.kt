package com.example.flexifitapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.databinding.FragmentProgressTrackerBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class ProgressTrackerFragment : Fragment(R.layout.fragment_progress_tracker) {

    private var _binding: FragmentProgressTrackerBinding? = null
    private val binding get() = _binding!!
    private var currentRange = "weekly"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressTrackerBinding.bind(view)

        setupToggle()
        fetchProgressData()
    }

    private fun setupToggle() {
        binding.toggleRange.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentRange = if (checkedId == R.id.btnWeekly) "weekly" else "monthly"
                fetchProgressData()
            }
        }
    }

    private fun fetchProgressData() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val response = api.getProgressStats(currentRange)

                if (response.isSuccessful) {
                    response.body()?.let { data ->

                        Log.d("PROGRESS", "Data received: compliance=${data.compliancePercentage}, avgCalories=${data.avgCalories}, streak=${data.currentStreak}")

                        updateUI(data)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load progress data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(data: ProgressTrackerDto) {
        binding.apply {
            // Compliance card
            tvCompliance.text = "${data.compliancePercentage.toInt()}%"
            progressCompliance.progress = data.compliancePercentage.toInt()
            tvComplianceMeta.text = data.complianceSessions

            // Calories card
            tvAvgCalories.text = "${data.avgCalories} kcal"
            val calorieChange = if (data.calorieHistory.size >= 2) {
                data.calorieHistory.last().value.toInt() - data.calorieHistory.first().value.toInt()
            } else 0
            tvAvgCaloriesSub.text = when {
                calorieChange > 0 -> "+$calorieChange vs last week"
                calorieChange < 0 -> "$calorieChange vs last week"
                else -> "No change"
            }

            // Water card
            tvWater.text = "${data.avgWaterIntake}L"
            // Calculate water change (optional)
            tvWaterSub.text = "Average daily intake"

            // Meals card
            val mealPercentage = if (data.totalMeals > 0) {
                (data.mealsCompleted.toDouble() / data.totalMeals * 100).toInt()
            } else 0
            tvMealsCompleted.text = "${data.mealsCompleted} / ${data.totalMeals}"
            tvMealsCompletedSub.text = "$mealPercentage% completion"

            // Weight card
            tvWeight.text = "${data.currentWeight} kg"
            val weightChangeText = if (data.weightChange > 0) "+${data.weightChange}" else data.weightChange.toString()
            tvWeightChange.text = "Change: $weightChangeText kg"

            // Streak card
            tvStreak.text = "${data.currentStreak} Days"
            tvStreakSub.text = if (data.currentStreak > 0) "Keep it up!" else "Start your streak today!"

            // Calories burned summary
            tvCaloriesBurned.text = "Avg: ${data.avgCalories} kcal"

            // Render charts
            renderWeightChart(data.weightHistory)
            renderCaloriesChart(data.calorieHistory)
        }
    }

    private fun renderWeightChart(history: List<ChartEntryDto>) {
        if (history.isEmpty()) {
            binding.lineChartWeight.clear()
            binding.lineChartWeight.setNoDataText("No weight data available")
            return
        }

        val brandColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = history.mapIndexed { index, item -> Entry(index.toFloat(), item.value) }

        val lineDataSet = LineDataSet(entries, "Weight (kg)").apply {
            color = brandColor
            setCircleColor(brandColor)
            circleRadius = 4f
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = brandColor
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(true)
            valueTextSize = 10f
        }

        binding.lineChartWeight.apply {
            data = LineData(lineDataSet)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(history.map { it.label })
            xAxis.labelRotationAngle = -45f
            xAxis.textSize = 10f
            animateX(800)
            invalidate()
        }
    }

    private fun renderCaloriesChart(history: List<ChartEntryDto>) {
        if (history.isEmpty()) {
            binding.barChartCalories.clear()
            binding.barChartCalories.setNoDataText("No calorie data available")
            return
        }

        val brandColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = history.mapIndexed { index, item -> BarEntry(index.toFloat(), item.value) }

        val barDataSet = BarDataSet(entries, "Calories (kcal)").apply {
            color = brandColor
            setDrawValues(true)
            valueTextSize = 10f
        }

        binding.barChartCalories.apply {
            data = BarData(barDataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(history.map { it.label })
            xAxis.labelRotationAngle = -45f
            xAxis.textSize = 10f
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}