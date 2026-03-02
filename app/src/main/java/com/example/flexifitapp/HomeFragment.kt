package com.example.flexifitapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flexifitapp.custom.WaterGlassView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator

class HomeFragment : Fragment() {

    // SAMPLE DATA (palitan mo later from backend/database)
    private var intakeCalories = 760
    private var burnedCalories = 100
    private var goalCalories = 2000

    private var waterCurrent = 0
    private val waterMax = 8

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setupCalorieTrack(view)
        setupWaterCard(view)

        return view
    }

    // -----------------------------
    // CALORIE TRACK (Ring)
    // -----------------------------
    private fun setupCalorieTrack(view: View) {
        val tvNet = view.findViewById<TextView>(R.id.tvNetCalories)
        val tvLeft = view.findViewById<TextView>(R.id.tvLeftCalories)
        val ring = view.findViewById<CircularProgressIndicator>(R.id.progressRing)

        fun render() {
            val net = intakeCalories - burnedCalories
            val left = (goalCalories - intakeCalories).coerceAtLeast(0)

            tvNet.text = "$net kcal"
            tvLeft.text = "$left kcal\nleft"

            ring.max = goalCalories.coerceAtLeast(1)
            ring.setProgress(intakeCalories.coerceIn(0, ring.max), true)
        }

        render()
    }

    // -----------------------------
    // WATER INTAKE (Glass)
    // -----------------------------
    private fun setupWaterCard(view: View) {
        val glass = view.findViewById<com.example.flexifitapp.custom.WaterGlassView>(R.id.waterGlass)
        glass.setMaxGlasses(8)
        glass.setCurrentGlasses(1)
        val tvCount = view.findViewById<TextView>(R.id.txtWaterCount)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAddWater)

        glass.setMaxGlasses(waterMax)

        fun render() {
            glass.setCurrentGlasses(waterCurrent)
            tvCount.text = "$waterCurrent/$waterMax"
        }

        render()

        btnAdd.setOnClickListener {
            if (waterCurrent < waterMax) {
                waterCurrent++
                render()
            }
        }
    }
}