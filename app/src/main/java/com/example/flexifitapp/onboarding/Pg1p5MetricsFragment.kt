package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.flexifitapp.R

class Pg1p5MetricsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg1p5_metrics,   // palitan kung iba name ng xml mo
    nextActionId = R.id.a2,                           // palitan ayon sa nav action mo
    isFirst = false
) {

    private val MIN_HEIGHT = 120
    private val MAX_HEIGHT = 220

    private val MIN_WEIGHT = 30
    private val MAX_WEIGHT = 200

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== HEIGHT VIEWS =====
        val tvHeightPrev = view.findViewById<TextView>(R.id.tvHeightPrev)
        val tvHeightSelected = view.findViewById<TextView>(R.id.tvHeightSelected)
        val tvHeightNext = view.findViewById<TextView>(R.id.tvHeightNext)
        val btnHeightUp = view.findViewById<ImageButton>(R.id.btnHeightUp)
        val btnHeightDown = view.findViewById<ImageButton>(R.id.btnHeightDown)

        // ===== WEIGHT VIEWS =====
        val tvWeightPrev = view.findViewById<TextView>(R.id.tvWeightPrev)
        val tvWeightSelected = view.findViewById<TextView>(R.id.tvWeightSelected)
        val tvWeightNext = view.findViewById<TextView>(R.id.tvWeightNext)
        val btnWeightUp = view.findViewById<ImageButton>(R.id.btnWeightUp)
        val btnWeightDown = view.findViewById<ImageButton>(R.id.btnWeightDown)

        // Restore saved (defaults)
        var height = OnboardingStore.getInt(requireContext(), "height_cm", 170)
            .coerceIn(MIN_HEIGHT, MAX_HEIGHT)

        var weight = OnboardingStore.getInt(requireContext(), "weight_kg", 70)
            .coerceIn(MIN_WEIGHT, MAX_WEIGHT)

        fun renderHeight() {
            tvHeightSelected.text = height.toString()
            tvHeightPrev.text = (height - 1).coerceAtLeast(MIN_HEIGHT).toString()
            tvHeightNext.text = (height + 1).coerceAtMost(MAX_HEIGHT).toString()

            btnHeightUp.isEnabled = height > MIN_HEIGHT
            btnHeightDown.isEnabled = height < MAX_HEIGHT
            btnHeightUp.alpha = if (btnHeightUp.isEnabled) 1f else 0.3f
            btnHeightDown.alpha = if (btnHeightDown.isEnabled) 1f else 0.3f

            OnboardingStore.putInt(requireContext(), "height_cm", height)
        }

        fun renderWeight() {
            tvWeightSelected.text = weight.toString()
            tvWeightPrev.text = (weight - 1).coerceAtLeast(MIN_WEIGHT).toString()
            tvWeightNext.text = (weight + 1).coerceAtMost(MAX_WEIGHT).toString()

            btnWeightUp.isEnabled = weight > MIN_WEIGHT
            btnWeightDown.isEnabled = weight < MAX_WEIGHT
            btnWeightUp.alpha = if (btnWeightUp.isEnabled) 1f else 0.3f
            btnWeightDown.alpha = if (btnWeightDown.isEnabled) 1f else 0.3f

            OnboardingStore.putInt(requireContext(), "weight_kg", weight)
        }

        // initial draw
        renderHeight()
        renderWeight()

        // Button logic (safe because buttons disable at limits)
        btnHeightUp.setOnClickListener {
            height -= 1
            renderHeight()
        }
        btnHeightDown.setOnClickListener {
            height += 1
            renderHeight()
        }

        btnWeightUp.setOnClickListener {
            weight -= 1
            renderWeight()
        }
        btnWeightDown.setOnClickListener {
            weight += 1
            renderWeight()
        }
    }

    override fun validateBeforeNext(): String? {
        val height = OnboardingStore.getInt(requireContext(), "height_cm", 0)
        val weight = OnboardingStore.getInt(requireContext(), "weight_kg", 0)

        return when {
            height !in MIN_HEIGHT..MAX_HEIGHT ->
                "Please select a valid height (${MIN_HEIGHT}-${MAX_HEIGHT} cm)."

            weight !in MIN_WEIGHT..MAX_WEIGHT ->
                "Please select a valid weight (${MIN_WEIGHT}-${MAX_WEIGHT} kg)."

            else -> null
        }
    }
}