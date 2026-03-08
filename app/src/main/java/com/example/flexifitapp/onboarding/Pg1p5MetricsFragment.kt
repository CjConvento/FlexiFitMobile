package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.flexifitapp.R

class Pg1p5MetricsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg1p5_metrics,
    nextActionId = R.id.a2,
    isFirst = false
) {

    private val MIN_HEIGHT = 120
    private val MAX_HEIGHT = 220

    private val MIN_WEIGHT = 30
    private val MAX_WEIGHT = 200

    private val MIN_TARGET_WEIGHT = 30
    private val MAX_TARGET_WEIGHT = 200

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== HEIGHT VIEWS =====
        val tvHeightPrev = view.findViewById<TextView>(R.id.tvHeightPrev)
        val tvHeightSelected = view.findViewById<TextView>(R.id.tvHeightSelected)
        val tvHeightNext = view.findViewById<TextView>(R.id.tvHeightNext)
        val btnHeightUp = view.findViewById<ImageButton>(R.id.btnHeightUp)
        val btnHeightDown = view.findViewById<ImageButton>(R.id.btnHeightDown)

        // ===== CURRENT WEIGHT VIEWS =====
        val tvWeightPrev = view.findViewById<TextView>(R.id.tvWeightPrev)
        val tvWeightSelected = view.findViewById<TextView>(R.id.tvWeightSelected)
        val tvWeightNext = view.findViewById<TextView>(R.id.tvWeightNext)
        val btnWeightUp = view.findViewById<ImageButton>(R.id.btnWeightUp)
        val btnWeightDown = view.findViewById<ImageButton>(R.id.btnWeightDown)

        // ===== TARGET WEIGHT VIEWS =====
        val tvTargetWeightPrev = view.findViewById<TextView>(R.id.tvTargetWeightPrev)
        val tvTargetWeightSelected = view.findViewById<TextView>(R.id.tvTargetWeightSelected)
        val tvTargetWeightNext = view.findViewById<TextView>(R.id.tvTargetWeightNext)
        val btnTargetWeightUp = view.findViewById<ImageButton>(R.id.btnTargetWeightUp)
        val btnTargetWeightDown = view.findViewById<ImageButton>(R.id.btnTargetWeightDown)

        // Restore saved values
        var height = OnboardingStore.getInt(requireContext(), "height_cm", 170)
            .coerceIn(MIN_HEIGHT, MAX_HEIGHT)

        var weight = OnboardingStore.getInt(requireContext(), "weight_kg", 70)
            .coerceIn(MIN_WEIGHT, MAX_WEIGHT)

        var targetWeight = OnboardingStore.getInt(requireContext(), "target_weight_kg", 65)
            .coerceIn(MIN_TARGET_WEIGHT, MAX_TARGET_WEIGHT)

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

        fun renderTargetWeight() {
            tvTargetWeightSelected.text = targetWeight.toString()
            tvTargetWeightPrev.text = (targetWeight - 1).coerceAtLeast(MIN_TARGET_WEIGHT).toString()
            tvTargetWeightNext.text = (targetWeight + 1).coerceAtMost(MAX_TARGET_WEIGHT).toString()

            btnTargetWeightUp.isEnabled = targetWeight > MIN_TARGET_WEIGHT
            btnTargetWeightDown.isEnabled = targetWeight < MAX_TARGET_WEIGHT
            btnTargetWeightUp.alpha = if (btnTargetWeightUp.isEnabled) 1f else 0.3f
            btnTargetWeightDown.alpha = if (btnTargetWeightDown.isEnabled) 1f else 0.3f

            OnboardingStore.putInt(requireContext(), "target_weight_kg", targetWeight)
        }

        // Initial draw
        renderHeight()
        renderWeight()
        renderTargetWeight()

        // Height buttons
        btnHeightUp.setOnClickListener {
            if (height > MIN_HEIGHT) {
                height -= 1
                renderHeight()
            }
        }

        btnHeightDown.setOnClickListener {
            if (height < MAX_HEIGHT) {
                height += 1
                renderHeight()
            }
        }

        // Current weight buttons
        btnWeightUp.setOnClickListener {
            if (weight > MIN_WEIGHT) {
                weight -= 1
                renderWeight()
            }
        }

        btnWeightDown.setOnClickListener {
            if (weight < MAX_WEIGHT) {
                weight += 1
                renderWeight()
            }
        }

        // Target weight buttons
        btnTargetWeightUp.setOnClickListener {
            if (targetWeight > MIN_TARGET_WEIGHT) {
                targetWeight -= 1
                renderTargetWeight()
            }
        }

        btnTargetWeightDown.setOnClickListener {
            if (targetWeight < MAX_TARGET_WEIGHT) {
                targetWeight += 1
                renderTargetWeight()
            }
        }
    }

    override fun validateBeforeNext(): String? {
        val height = OnboardingStore.getInt(requireContext(), "height_cm", 0)
        val weight = OnboardingStore.getInt(requireContext(), "weight_kg", 0)
        val targetWeight = OnboardingStore.getInt(requireContext(), "target_weight_kg", 0)

        return when {
            height !in MIN_HEIGHT..MAX_HEIGHT ->
                "Please select a valid height (${MIN_HEIGHT}-${MAX_HEIGHT} cm)."

            weight !in MIN_WEIGHT..MAX_WEIGHT ->
                "Please select a valid weight (${MIN_WEIGHT}-${MAX_WEIGHT} kg)."

            targetWeight !in MIN_TARGET_WEIGHT..MAX_TARGET_WEIGHT ->
                "Please select a valid target weight (${MIN_TARGET_WEIGHT}-${MAX_TARGET_WEIGHT} kg)."

            weight == targetWeight ->
                "Current weight and target weight should not be the same."

            else -> null
        }
    }
}