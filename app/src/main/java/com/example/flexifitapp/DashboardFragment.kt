package com.example.flexifitapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.custom.WaterGlassView
import com.example.flexifitapp.dashboard.BmiDetailsDialog
import com.example.flexifitapp.dashboard.ProfileStatusResponse // Gamitin yung bagong model babe
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // ===== Views (Null-safe handling) =====
    private var txtLevel: TextView? = null
    private var imgLevelIcon: ImageView? = null
    private var txtCalorieIntake: TextView? = null
    private var txtCaloriesBurned: TextView? = null
    private var tvNetCalories: TextView? = null
    private var tvLeftCalories: TextView? = null
    private var progressRing: CircularProgressIndicator? = null
    private var progressIntake: LinearProgressIndicator? = null
    private var progressBurned: LinearProgressIndicator? = null
    private var tvBMIStatus: TextView? = null
    private var txtBMIScore: TextView? = null
    private var btnBMIViewMore: Button? = null

    // Water
    private var txtWaterCount: TextView? = null
    private var btnAddWater: MaterialButton? = null
    private var waterGlass: WaterGlassView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        // DITO TAYO KUKUHA NG DATA NGAYON BABE
        fetchDashboardData()
    }

    private fun bindViews(view: View) {
        txtLevel = view.findViewById(R.id.txtLevel)
        imgLevelIcon = view.findViewById(R.id.imgLevelIcon)
        txtCalorieIntake = view.findViewById(R.id.txtCalorieIntake)
        txtCaloriesBurned = view.findViewById(R.id.txtCaloriesBurned)
        progressIntake = view.findViewById(R.id.progressIntake)
        progressBurned = view.findViewById(R.id.progressBurned)
        tvNetCalories = view.findViewById(R.id.tvNetCalories)
        tvLeftCalories = view.findViewById(R.id.tvLeftCalories)
        progressRing = view.findViewById(R.id.progressRing)
        txtBMIScore = view.findViewById(R.id.txtBMIScore)
        tvBMIStatus = view.findViewById(R.id.tvBMIStatus)
        btnBMIViewMore = view.findViewById(R.id.btnBMIViewMore)
        txtWaterCount = view.findViewById(R.id.txtWaterCount)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        waterGlass = view.findViewById(R.id.waterGlass)
    }

    private fun fetchDashboardData() {
        lifecycleScope.launch {
            try {
                // Gamitin ang existing ApiClient mo babe
                val response = ApiClient.api(requireContext()).getProfileStatus()

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    updateUI(data)
                } else {
                    Toast.makeText(context, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(data: ProfileStatusResponse) {
        // 1. Header & Level
        txtLevel?.text = "Level: ${data.fitnessLevel}"
        updateLevelIcon(data.fitnessLevel ?: "Beginner")

        // 2. BMI Card (API DRIVEN NA BABE!)
        txtBMIScore?.text = String.format("%.1f", data.bmiData?.value ?: 0.0)
        tvBMIStatus?.text = data.bmiData?.status ?: "No data"

        btnBMIViewMore?.setOnClickListener {
            val dialog = BmiDetailsDialog(
                data.bmiData?.value ?: 0.0,
                data.bmiData?.status ?: "No data"
            )
            dialog.show(parentFragmentManager, "bmi_details")
        }

        // 3. Nutrition Engine (The Circles and Bars)
        val nutri = data.nutrition ?: return

        // Progress Bars
        progressIntake?.max = nutri.target.toInt()
        progressIntake?.setProgress(nutri.intake.toInt(), true)
        txtCalorieIntake?.text = "${nutri.intake.toInt()} / ${nutri.target.toInt()} kcal"

        progressBurned?.progress = nutri.burned.toInt()
        txtCaloriesBurned?.text = "${nutri.burned.toInt()} kcal"

        // Middle Circle (Net Calories)
        tvNetCalories?.text = "${nutri.net.toInt()} kcal"
        tvLeftCalories?.text = "${nutri.remaining.toInt()} kcal\nleft"
        progressRing?.max = nutri.target.toInt()
        progressRing?.setProgress(nutri.net.toInt(), true)

        // 4. Water
        txtWaterCount?.text = "${nutri.waterGlasses}/${nutri.waterTarget}"
        waterGlass?.setCurrentGlasses(nutri.waterGlasses)
    }

    private fun updateLevelIcon(level: String) {
        val color = when (level.lowercase()) {
            "beginner" -> "#4CAF50"
            "intermediate" -> "#FF9800"
            "advanced" -> "#F44336"
            else -> "#2196F3"
        }
        imgLevelIcon?.setColorFilter(Color.parseColor(color))
    }

    override fun onDestroyView() {
        // Cleanup to prevent memory leaks
        txtLevel = null; imgLevelIcon = null; txtCalorieIntake = null
        txtCaloriesBurned = null; tvNetCalories = null; tvLeftCalories = null
        progressRing = null; progressIntake = null; progressBurned = null
        txtBMIScore = null; tvBMIStatus = null; btnBMIViewMore = null
        txtWaterCount = null; btnAddWater = null; waterGlass = null
        super.onDestroyView()
    }
}