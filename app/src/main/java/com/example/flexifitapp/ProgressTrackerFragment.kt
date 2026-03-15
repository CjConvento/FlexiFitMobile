package com.example.flexifitapp.progress

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.ChartEntryDto
import com.example.flexifitapp.ProgressTrackerDto
import com.example.flexifitapp.R
import com.example.flexifitapp.databinding.FragmentProgressTrackerBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class ProgressTrackerFragment : Fragment(R.layout.fragment_progress_tracker) {

    private var _binding: FragmentProgressTrackerBinding? = null
    private val binding get() = _binding!!
    private var currentRange = Range.WEEKLY

    enum class Range { WEEKLY, MONTHLY }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressTrackerBinding.bind(view)

        setupToggle()
        setupAchievementsList()
        fetchProgressData()
    }

    private fun setupToggle() {
        binding.toggleRange.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentRange = if (checkedId == R.id.btnWeekly) Range.WEEKLY else Range.MONTHLY
                fetchProgressData()
            }
        }
    }

    private fun fetchProgressData() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api(requireContext())
                val response = api.getProgressStats(currentRange.name.lowercase())

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        updateUI(data)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(data: ProgressTrackerDto) {
        binding.apply {
            // Match ito sa XML IDs mo babe
            tvCompliance.text = "${data.compliancePercentage}%"
            progressCompliance.progress = data.compliancePercentage.toInt()
            tvComplianceMeta.text = data.complianceSessions

            tvAvgCalories.text = "${data.avgCalories} kcal"
            tvWater.text = "${data.avgWaterIntake}L"

            // Inayos ko ito: tvMealsCompleted ang nasa XML mo
            tvMealsCompleted.text = "${data.mealsCompleted} / ${data.totalMeals}"
            tvMealsCompletedSub.text = "${((data.mealsCompleted.toDouble() / data.totalMeals) * 100).toInt()}% completion"

            tvWeight.text = "${data.currentWeight} kg"
            tvWeightChange.text = "Change: ${data.weightChange} kg"

            tvStreak.text = "${data.currentStreak} Days"
            tvCaloriesBurned.text = "Avg: ${data.avgCalories} kcal"

            // Charts Rendering
            renderWeightChart(data.weightHistory)
            renderCaloriesChart(data.calorieHistory)
        }
    }

    private fun renderWeightChart(history: List<ChartEntryDto>) {
        if (history.isEmpty()) return

        val brandColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = history.mapIndexed { index, item -> Entry(index.toFloat(), item.value) }

        val lineDataSet = LineDataSet(entries, "Weight").apply {
            color = brandColor
            setCircleColor(brandColor)
            lineWidth = 3f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.btn_gradient) // Siguraduhin may drawable ka nito or delete this line
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        binding.lineChartWeight.apply {
            this.data = LineData(lineDataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(history.map { it.label })
            animateX(800)
            invalidate()
        }
    }

    private fun renderCaloriesChart(history: List<ChartEntryDto>) {
        if (history.isEmpty()) return

        val brandColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = history.mapIndexed { index, item -> BarEntry(index.toFloat(), item.value) }

        val barDataSet = BarDataSet(entries, "Calories").apply {
            color = brandColor
        }

        binding.barChartCalories.apply {
            this.data = BarData(barDataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(history.map { it.label })
            animateY(1000)
            invalidate()
        }
    }

    private fun setupAchievementsList() {
        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        // Dito natin isasalpak yung adapter sa susunod
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}