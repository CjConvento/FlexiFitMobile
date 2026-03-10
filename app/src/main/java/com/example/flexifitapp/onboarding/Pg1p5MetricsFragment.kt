package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
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

    private lateinit var rvHeight: RecyclerView
    private lateinit var rvWeight: RecyclerView
    private lateinit var rvTargetWeight: RecyclerView

    private lateinit var adapterHeight: NumberPickerAdapter
    private lateinit var adapterWeight: NumberPickerAdapter
    private lateinit var adapterTargetWeight: NumberPickerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHeight = view.findViewById(R.id.rvHeightPicker)
        rvWeight = view.findViewById(R.id.rvWeightPicker)
        rvTargetWeight = view.findViewById(R.id.rvTargetWeightPicker)

        val heightValues = (MIN_HEIGHT..MAX_HEIGHT).toList()
        val weightValues = (MIN_WEIGHT..MAX_WEIGHT).toList()
        val targetWeightValues = (MIN_TARGET_WEIGHT..MAX_TARGET_WEIGHT).toList()

        val savedHeight = OnboardingStore.getInt(requireContext(), "height_cm", 170)
            .coerceIn(MIN_HEIGHT, MAX_HEIGHT)

        val savedWeight = OnboardingStore.getInt(requireContext(), "weight_kg", 70)
            .coerceIn(MIN_WEIGHT, MAX_WEIGHT)

        val savedTargetWeight = OnboardingStore.getInt(requireContext(), "target_weight_kg", 65)
            .coerceIn(MIN_TARGET_WEIGHT, MAX_TARGET_WEIGHT)

        adapterHeight = NumberPickerAdapter(heightValues)
        adapterWeight = NumberPickerAdapter(weightValues)
        adapterTargetWeight = NumberPickerAdapter(targetWeightValues)

        setupPicker(
            recyclerView = rvHeight,
            adapter = adapterHeight,
            selectedNumber = savedHeight,
            storeKey = "height_cm"
        )

        setupPicker(
            recyclerView = rvWeight,
            adapter = adapterWeight,
            selectedNumber = savedWeight,
            storeKey = "weight_kg"
        )

        setupPicker(
            recyclerView = rvTargetWeight,
            adapter = adapterTargetWeight,
            selectedNumber = savedTargetWeight,
            storeKey = "target_weight_kg"
        )
    }

    private fun setupPicker(
        recyclerView: RecyclerView,
        adapter: NumberPickerAdapter,
        selectedNumber: Int,
        storeKey: String
    ) {
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER

        val snapHelper = LinearSnapHelper()
        if (recyclerView.onFlingListener == null) {
            snapHelper.attachToRecyclerView(recyclerView)
        }

        adapter.onBindNumber = { tv: TextView, number: Int, isSelected: Boolean ->
            tv.text = number.toString()
            tv.alpha = if (isSelected) 1f else 0.35f
            tv.textSize = if (isSelected) 24f else 16f
            tv.setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }

        adapter.onNumberClick = { virtualPos, number ->
            recyclerView.smoothScrollToPosition(virtualPos)
            adapter.setSelectedVirtualPos(virtualPos)
            OnboardingStore.putInt(requireContext(), storeKey, number)
        }

        val initialPos = adapter.getVirtualPosForNumber(selectedNumber)
        adapter.setSelectedVirtualPos(initialPos)
        recyclerView.scrollToPosition(initialPos)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                super.onScrollStateChanged(rv, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snappedView = snapHelper.findSnapView(layoutManager) ?: return
                    val snappedPos = layoutManager.getPosition(snappedView)
                    if (snappedPos == RecyclerView.NO_POSITION) return

                    adapter.setSelectedVirtualPos(snappedPos)
                    val selected = adapter.getNumberAtVirtualPos(snappedPos)
                    OnboardingStore.putInt(requireContext(), storeKey, selected)
                }
            }
        })
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