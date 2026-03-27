package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg1p5MetricsFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg1p5_metrics,
    nextActionId = R.id.a2,
    isFirst = false
) {

    // Range definitions based on standard metrics
    private val MIN_HEIGHT = 120; private val MAX_HEIGHT = 220
    private val MIN_WEIGHT = 30;  private val MAX_WEIGHT = 200

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Height Picker Setup
        setupPicker(
            recyclerView = view.findViewById(R.id.rvHeightPicker),
            range = (MIN_HEIGHT..MAX_HEIGHT).toList(),
            storeKey = FlexiFitKeys.HEIGHT_CM,
            defaultVal = 170
        )

        // 2. Current Weight Picker
        setupPicker(
            recyclerView = view.findViewById(R.id.rvWeightPicker),
            range = (MIN_WEIGHT..MAX_WEIGHT).toList(),
            storeKey = FlexiFitKeys.WEIGHT_KG,
            defaultVal = 70
        )

        // 3. Target Weight Picker
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
        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        recyclerView.layoutManager = lm
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        val snapHelper = LinearSnapHelper()
        if (recyclerView.onFlingListener == null) {
            snapHelper.attachToRecyclerView(recyclerView)
        }

        adapter.onBindNumber = { tv, _, isSelected ->
            tv.alpha = if (isSelected) 1f else 0.35f
            tv.textSize = if (isSelected) 24f else 18f
            tv.setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }

        // PRE-FILL: Kunin ang data mula sa OnboardingStore
        val savedValue = OnboardingStore.getInt(requireContext(), storeKey, defaultVal)
            .coerceIn(range.first(), range.last())

        val initialPos = adapter.getVirtualPosForNumber(savedValue)
        adapter.setSelectedVirtualPos(initialPos)

        recyclerView.post {
            lm.scrollToPositionWithOffset(initialPos, 0)
            // Siguraduhing naka-save agad ang initial value sa store
            OnboardingStore.putInt(requireContext(), storeKey, savedValue)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snappedView = snapHelper.findSnapView(lm) ?: return
                    val pos = lm.getPosition(snappedView)

                    val selectedNumber = adapter.getNumberAtVirtualPos(pos)
                    adapter.setSelectedVirtualPos(pos)

                    // Auto-save sa bawat scroll
                    OnboardingStore.putInt(requireContext(), storeKey, selectedNumber)
                }
            }
        })
    }

    override fun validateBeforeNext(): String? {
        val ctx = requireContext()
        val h = OnboardingStore.getInt(ctx, FlexiFitKeys.HEIGHT_CM, 0)
        val w = OnboardingStore.getInt(ctx, FlexiFitKeys.WEIGHT_KG, 0)
        val t = OnboardingStore.getInt(ctx, FlexiFitKeys.TARGET_WEIGHT_KG, 0)

        return when {
            h == 0 || w == 0 || t == 0 -> "Please select all metrics before proceeding."
            else -> null
        }
    }
}