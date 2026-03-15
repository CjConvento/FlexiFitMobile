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

    private var txtHeaderName: TextView? = null    // Para mawala red sa txtHeaderName
    private var txtHeaderEmail: TextView? = null   // Para mawala red sa txtHeaderEmail

    // I-dagdag sa declarations sa itaas ng class
    private var txtWorkoutName1: TextView? = null
    private var txtWorkoutl1: TextView? = null
    private var txtWorkoutName2: TextView? = null
    private var txtWorkoutl2: TextView? = null

    private var btnUpdateWater: TextView? = null

    // Water
    private var txtWaterCount: TextView? = null
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
        waterGlass = view.findViewById(R.id.waterGlass)

        // --- DAGDAG MO ITO PARA LUMABAS YUNG NAME AT EMAIL ---
        txtHeaderName = view.findViewById(R.id.txtHeaderName)
        txtHeaderEmail = view.findViewById(R.id.txtHeaderEmail)

        // DITO MO ILALAGAY YUNG BINAGO NATIN BABE
        btnUpdateWater = view.findViewById(R.id.btnUpdateWater) // I-bind ang TextView
        btnUpdateWater?.setOnClickListener {
            // Lilipat sa Nutrition tab shortcut
            (requireActivity() as? MainActivity)?.navigateToNutritionTab()
        }

        txtWorkoutName1 = view.findViewById(R.id.txtWorkoutName1)
        txtWorkoutl1 = view.findViewById(R.id.txtWorkoutl1) // Ito yung 'txtWorkoutl1' mo babe
        txtWorkoutName2 = view.findViewById(R.id.txtWorkoutName2)
        txtWorkoutl2 = view.findViewById(R.id.txtWorkoutl2)
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
        // 1. I-sync ang Name at Email sa Header
        // 'data.user' o kung ano man ang tawag sa field ng user object sa response mo
        txtHeaderName?.text = data.userName ?: "FlexiFit User"
        txtHeaderEmail?.text = data.userEmail ?: "user@flexifit.com"

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

        // 5. ETO NA YUNG DINAGDAG NATIN BABE (Upcoming Workout)
        val program = data.program
        if (program != null) {
            // Unang Card (Program Name at Day No)
            txtWorkoutName1?.text = program.programName ?: "No Active Program"
            txtWorkoutl1?.text = "Day ${program.dayNo}" // Ito yung label sa ilalim

            // Pangalawang Card (Status update)
            if (program.isWorkoutDay) {
                txtWorkoutName2?.text = "Today is Workout Day!"
                txtWorkoutl2?.text = "Tap to see exercises 🔥"
            } else {
                txtWorkoutName2?.text = "Rest Day"
                txtWorkoutl2?.text = "Recovery time, babe! 💤"
            }
        }
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
        txtWaterCount = null; btnUpdateWater = null; waterGlass = null
        super.onDestroyView()
    }
}