package com.example.flexifitapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.nutri.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class NutritionTabRootFragment : Fragment(R.layout.fragment_nutri) {

    private val viewModel: NutritionViewModel by activityViewModels()
    private lateinit var sectionAdapter: MealSectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarButton(view)
        setupMealPlanRecyclerView(view)

        // 1. TRIGGER THE API FETCH: Hihigop ng data mula sa ASP.NET
        viewModel.fetchTodayPlan(requireContext())

        // 2. OBSERVE SECTIONS: Para sa listahan ng Breakfast, Lunch, etc.
        viewModel.sections.observe(viewLifecycleOwner) { updatedSections ->
            sectionAdapter.updateData(updatedSections ?: mutableListOf())
        }

        // 3. OBSERVE SUMMARY: Para sa PieChart at Progress Bars (Galing sa C# DTO)
        viewModel.nutritionSummary.observe(viewLifecycleOwner) { summary ->
            updateDashboardUI(view, summary)
        }
    }

    private fun updateDashboardUI(view: View, summary: NutritionResponse) {
        // TEXT VALUES: Target vs Consumed
        view.findViewById<TextView>(R.id.tvCaloriesValue).text =
            "${summary.consumedCalories.toInt()} / ${summary.targetCalories.toInt()} kcal"

        view.findViewById<TextView>(R.id.tvCaloriesBurned).text =
            "${summary.consumedCalories.toInt()} kcal"

        // MACRO TEXTS (Cons vs Target direct from API)
        view.findViewById<TextView>(R.id.tvProteinValue).text = "${summary.consumedProtein.toInt()}g / ${summary.targetProtein.toInt()}g"
        view.findViewById<TextView>(R.id.tvCarbsValue).text = "${summary.consumedCarbs.toInt()}g / ${summary.targetCarbs.toInt()}g"
        view.findViewById<TextView>(R.id.tvFatsValue).text = "${summary.consumedFats.toInt()}g / ${summary.targetFats.toInt()}g"

        // PROGRESS BARS
        view.findViewById<ProgressBar>(R.id.pbCalories).progress = percent(summary.consumedCalories.toInt(), summary.targetCalories.toInt())
        view.findViewById<ProgressBar>(R.id.pbProtein).progress = percent(summary.consumedProtein.toInt(), summary.targetProtein.toInt())
        view.findViewById<ProgressBar>(R.id.pbCarbs).progress = percent(summary.consumedCarbs.toInt(), summary.targetCarbs.toInt())
        view.findViewById<ProgressBar>(R.id.pbFats).progress = percent(summary.consumedFats.toInt(), summary.targetFats.toInt())

        // PIE CHART UPDATE
        val chart = view.findViewById<PieChart>(R.id.nutritionPieChart)
        updateChartData(
            chart,
            summary.consumedProtein.toInt(),
            summary.consumedCarbs.toInt(),
            summary.consumedFats.toInt(),
            summary.consumedCalories.toInt()
        )
    }

    private fun setupCalendarButton(view: View) {
        view.findViewById<ImageButton>(R.id.btnOpenCalendar).setOnClickListener {
            val b = bundleOf("source_tab" to "NUTRITION")
            findNavController().navigate(R.id.unifiedCalendarFragment, b)
        }
    }

    private fun setupMealPlanRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvMealSections)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.setHasFixedSize(false)

        sectionAdapter = MealSectionAdapter(mutableListOf()) { mealType, food ->
            // I-pass lahat ng kailangan ng Details screen, pati description
            val b = bundleOf(
                "mealType" to mealType, // <--- DAGDAG MO ITO
                "mealItemId" to food.mealItemId,
                "foodId" to food.foodId,
                "name" to food.name,
                "description" to food.description,
                "servingLabel" to food.servingLabel,
                "qty" to food.qty,
                "calories" to food.calories,
                "protein" to food.protein,
                "carbs" to food.carbs,
                "fats" to food.fats
            )
            findNavController().navigate(R.id.foodDetailsFragment, b)
        }
        rv.adapter = sectionAdapter
    }

    private fun updateChartData(chart: PieChart, p: Int, c: Int, f: Int, totalKcal: Int) {
        val entries = ArrayList<PieEntry>()

        if (p == 0 && c == 0 && f == 0) {
            entries.add(PieEntry(1f, ""))
            chart.centerText = "0\nkcal"
        } else {
            entries.add(PieEntry(p.toFloat(), "Protein"))
            entries.add(PieEntry(c.toFloat(), "Carbs"))
            entries.add(PieEntry(f.toFloat(), "Fats"))
            chart.centerText = "$totalKcal\nkcal"
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#4facfe"), // Protein (Blue)
                Color.parseColor("#D1D9E0"), // Carbs (Glaucous)
                Color.parseColor("#9EB9D4")  // Fats (Muted Blue)
            )
            setDrawValues(false)
            sliceSpace = 3f
        }

        chart.apply {
            data = PieData(dataSet)
            setCenterTextSize(18f)
            setCenterTextColor(Color.BLACK)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 60f
            transparentCircleRadius = 65f
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            setDrawEntryLabels(false)
            invalidate()
        }
    }

    private fun percent(value: Int, target: Int): Int {
        if (target <= 0) return 0
        return ((value * 100f) / target).toInt().coerceIn(0, 100)
    }
}