package com.example.flexifitapp.progress

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flexifitapp.R
import com.example.flexifitapp.databinding.FragmentProgressTrackerBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ProgressTrackerFragment : Fragment(R.layout.fragment_progress_tracker) {

    private var _binding: FragmentProgressTrackerBinding? = null
    private val binding get() = _binding!!

    private var currentRange = Range.WEEKLY

    enum class Range {
        WEEKLY, MONTHLY
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressTrackerBinding.bind(view)

        setupToggle()
        setupAchievements()
        renderUi(currentRange)
    }

    private fun setupToggle() {
        binding.toggleRange.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnWeekly -> {
                    currentRange = Range.WEEKLY
                    styleRangeButtons(isWeekly = true)
                    renderUi(Range.WEEKLY)
                }
                R.id.btnMonthly -> {
                    currentRange = Range.MONTHLY
                    styleRangeButtons(isWeekly = false)
                    renderUi(Range.MONTHLY)
                }
            }
        }

        styleRangeButtons(isWeekly = true)
    }

    private fun styleRangeButtons(isWeekly: Boolean) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val surface = ContextCompat.getColor(requireContext(), R.color.surface)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.textPrimary)

        if (isWeekly) {
            binding.btnWeekly.setBackgroundColor(primary)
            binding.btnWeekly.setTextColor(white)

            binding.btnMonthly.setBackgroundColor(surface)
            binding.btnMonthly.setTextColor(textPrimary)
        } else {
            binding.btnMonthly.setBackgroundColor(primary)
            binding.btnMonthly.setTextColor(white)

            binding.btnWeekly.setBackgroundColor(surface)
            binding.btnWeekly.setTextColor(textPrimary)
        }
    }

    private fun setupAchievements() {
        val items = listOf(
            ProgressAchievement("🏆", "Level 3 Complete", "Completed current level"),
            ProgressAchievement("🎯", "Nutrition Goal Met", "Stayed within target calories"),
            ProgressAchievement("💧", "Hydration Goal Hit", "Reached water goal this week")
        )

        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAchievements.adapter = ProgressAchievementAdapter(items)
    }

    private fun renderUi(range: Range) {
        when (range) {
            Range.WEEKLY -> renderWeekly()
            Range.MONTHLY -> renderMonthly()
        }
    }

    private fun renderWeekly() {
        binding.tvCompliance.text = "75%"
        binding.progressCompliance.progress = 75
        binding.tvComplianceMeta.text = "3 / 4 Sessions"

        binding.tvAvgCalories.text = "2,150 kcal"
        binding.tvAvgCaloriesSub.text = "+200 vs last week"

        binding.tvWater.text = "2.3 L"
        binding.tvWaterSub.text = "+0.5L vs last week"

        binding.tvMealsCompleted.text = "18 / 21"
        binding.tvMealsCompletedSub.text = "85% completion"

        binding.tvWeight.text = "76.8 kg"
        binding.tvWeightChange.text = "Change: -1.6 kg"

        binding.tvCaloriesBurned.text = "Avg: 520 kcal"
        binding.tvStreak.text = "🔥 7 Days"
        binding.tvStreakSub.text = "Keep it up!"

        setupWeightChartWeekly()
        setupCaloriesChartWeekly()
    }

    private fun renderMonthly() {
        binding.tvCompliance.text = "81%"
        binding.progressCompliance.progress = 81
        binding.tvComplianceMeta.text = "13 / 16 Sessions"

        binding.tvAvgCalories.text = "2,040 kcal"
        binding.tvAvgCaloriesSub.text = "-110 vs last month"

        binding.tvWater.text = "2.1 L"
        binding.tvWaterSub.text = "+0.2L vs last month"

        binding.tvMealsCompleted.text = "77 / 90"
        binding.tvMealsCompletedSub.text = "86% completion"

        binding.tvWeight.text = "75.9 kg"
        binding.tvWeightChange.text = "Change: -2.4 kg"

        binding.tvCaloriesBurned.text = "Avg: 498 kcal"
        binding.tvStreak.text = "🔥 21 Days"
        binding.tvStreakSub.text = "Strong consistency!"

        setupWeightChartMonthly()
        setupCaloriesChartMonthly()
    }

    private fun setupWeightChartWeekly() {
        val brand = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = listOf(
            Entry(0f, 78.4f),
            Entry(1f, 78.0f),
            Entry(2f, 77.6f),
            Entry(3f, 77.1f),
            Entry(4f, 76.9f),
            Entry(5f, 76.8f),
            Entry(6f, 76.8f)
        )

        val dataSet = LineDataSet(entries, "Weight").apply {
            color = brand
            valueTextColor = Color.TRANSPARENT
            lineWidth = 2.5f
            circleRadius = 4f
            setCircleColor(brand)
            setDrawCircleHole(false)
            setDrawFilled(true)
            fillColor = brand
            fillAlpha = 35
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.lineChartWeight.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setPinchZoom(false)
            setScaleEnabled(false)
            axisRight.isEnabled = false

            axisLeft.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                granularity = 1f
            }

            animateX(500)
            invalidate()
        }
    }

    private fun setupWeightChartMonthly() {
        val brand = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = listOf(
            Entry(0f, 78.5f),
            Entry(1f, 77.9f),
            Entry(2f, 77.2f),
            Entry(3f, 76.7f)
        )

        val dataSet = LineDataSet(entries, "Weight").apply {
            color = brand
            valueTextColor = Color.TRANSPARENT
            lineWidth = 2.5f
            circleRadius = 4f
            setCircleColor(brand)
            setDrawCircleHole(false)
            setDrawFilled(true)
            fillColor = brand
            fillAlpha = 35
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.lineChartWeight.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            axisRight.isEnabled = false

            axisLeft.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("W1", "W2", "W3", "W4"))
                granularity = 1f
            }

            animateX(500)
            invalidate()
        }
    }

    private fun setupCaloriesChartWeekly() {
        val brand = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = listOf(
            BarEntry(0f, 320f),
            BarEntry(1f, 450f),
            BarEntry(2f, 520f),
            BarEntry(3f, 600f),
            BarEntry(4f, 500f),
            BarEntry(5f, 380f),
            BarEntry(6f, 340f)
        )

        val dataSet = BarDataSet(entries, "Calories").apply {
            color = brand
            valueTextColor = Color.TRANSPARENT
        }

        binding.barChartCalories.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.55f
            }

            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            axisRight.isEnabled = false

            axisLeft.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                granularity = 1f
            }

            animateY(500)
            invalidate()
        }
    }

    private fun setupCaloriesChartMonthly() {
        val brand = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val entries = listOf(
            BarEntry(0f, 460f),
            BarEntry(1f, 510f),
            BarEntry(2f, 540f),
            BarEntry(3f, 480f)
        )

        val dataSet = BarDataSet(entries, "Calories").apply {
            color = brand
            valueTextColor = Color.TRANSPARENT
        }

        binding.barChartCalories.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.55f
            }

            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            axisRight.isEnabled = false

            axisLeft.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("W1", "W2", "W3", "W4"))
                granularity = 1f
            }

            animateY(500)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}