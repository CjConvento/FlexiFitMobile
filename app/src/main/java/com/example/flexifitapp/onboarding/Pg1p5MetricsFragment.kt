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

    private val MIN_HEIGHT = 120; private val MAX_HEIGHT = 220
    private val MIN_WEIGHT = 30;  private val MAX_WEIGHT = 200

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup Picker gamit ang FlexiFitKeys
        setupPicker(
            recyclerView = view.findViewById(R.id.rvHeightPicker),
            range = (MIN_HEIGHT..MAX_HEIGHT).toList(),
            storeKey = FlexiFitKeys.HEIGHT_CM,
            defaultVal = 170
        )

        setupPicker(
            recyclerView = view.findViewById(R.id.rvWeightPicker),
            range = (MIN_WEIGHT..MAX_WEIGHT).toList(),
            storeKey = FlexiFitKeys.WEIGHT_KG,
            defaultVal = 70
        )

        setupPicker(
            recyclerView = view.findViewById(R.id.rvTargetWeightPicker),
            range = (MIN_WEIGHT..MAX_WEIGHT).toList(),
            storeKey = FlexiFitKeys.TARGET_WEIGHT_KG,
            defaultVal = 65
        )
    }

    private fun setupPicker(
        recyclerView: RecyclerView,
        range: List<Int>,
        storeKey: String,
        defaultVal: Int
    ) {
        val adapter = NumberPickerAdapter(range)
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        val snapHelper = LinearSnapHelper()
        if (recyclerView.onFlingListener == null) {
            snapHelper.attachToRecyclerView(recyclerView)
        }

        adapter.onBindNumber = { tv, _, isSelected ->
            tv.alpha = if (isSelected) 1f else 0.35f
            tv.textSize = if (isSelected) 24f else 16f
            tv.setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }

        // --- PRE-FILLING LOGIC ---
        val savedValue = OnboardingStore.getInt(requireContext(), storeKey, defaultVal)
            .coerceIn(range.first(), range.last())

        val initialPos = adapter.getVirtualPosForNumber(savedValue)
        adapter.setSelectedVirtualPos(initialPos)

        // Gamit ang post para sigurado ang scroll alignment
        recyclerView.post {
            layoutManager.scrollToPositionWithOffset(initialPos, 0)
            OnboardingStore.putInt(requireContext(), storeKey, savedValue)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snappedView = snapHelper.findSnapView(layoutManager) ?: return
                    val snappedPos = layoutManager.getPosition(snappedView)

                    adapter.setSelectedVirtualPos(snappedPos)
                    val selected = adapter.getNumberAtVirtualPos(snappedPos)
                    OnboardingStore.putInt(requireContext(), storeKey, selected)
                }
            }
        })
    }

    override fun validateBeforeNext(): String? {
        val height = OnboardingStore.getInt(requireContext(), FlexiFitKeys.HEIGHT_CM, 0)
        val weight = OnboardingStore.getInt(requireContext(), FlexiFitKeys.WEIGHT_KG, 0)
        val targetWeight = OnboardingStore.getInt(requireContext(), FlexiFitKeys.TARGET_WEIGHT_KG, 0)

        return when {
            height == 0 || weight == 0 || targetWeight == 0 -> "Please complete all metrics."
            weight == targetWeight -> "Current weight and target weight should not be the same."
            else -> null
        }
    }
}