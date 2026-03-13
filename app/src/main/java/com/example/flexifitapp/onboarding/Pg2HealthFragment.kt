package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.example.flexifitapp.R
import com.google.android.material.card.MaterialCardView

class Pg2HealthFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg2_health,
    nextActionId = R.id.a3
) {

    private var cbUpperBodyInjury: CheckBox? = null
    private var cbLowerBodyInjury: CheckBox? = null
    private var cbJointProblems: CheckBox? = null
    private var cbShortBreath: CheckBox? = null
    private var cbNone: CheckBox? = null

    private var cardHealthWarning: MaterialCardView? = null
    private var tvHealthWarning: TextView? = null

    private var suppressListener = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cbUpperBodyInjury = view.findViewById(R.id.cbHBi)
        cbLowerBodyInjury = view.findViewById(R.id.cbLBi)
        cbJointProblems = view.findViewById(R.id.cbJoint)
        cbShortBreath = view.findViewById(R.id.cbSB)
        cbNone = view.findViewById(R.id.cbNone)

        cardHealthWarning = view.findViewById(R.id.cardHealthWarning)
        tvHealthWarning = view.findViewById(R.id.tvHealthWarning)

        // 1. Restore state from store (Hydration)
        preloadSelections()
        // 2. Bind listeners for auto-save and mutual exclusion
        bindListeners()
        // 3. Initial UI update for warnings
        updateWarnings()
    }

    override fun validateBeforeNext(): String? {
        val anyChecked = cbUpperBodyInjury?.isChecked == true ||
                cbLowerBodyInjury?.isChecked == true ||
                cbJointProblems?.isChecked == true ||
                cbShortBreath?.isChecked == true ||
                cbNone?.isChecked == true

        return if (!anyChecked) "Please select at least one option." else null
    }

    private fun bindListeners() {
        cbNone?.setOnCheckedChangeListener { _, isChecked ->
            if (suppressListener) return@setOnCheckedChangeListener
            if (isChecked) {
                suppressListener = true
                cbUpperBodyInjury?.isChecked = false
                cbLowerBodyInjury?.isChecked = false
                cbJointProblems?.isChecked = false
                cbShortBreath?.isChecked = false
                suppressListener = false
            }
            saveSelections()
            updateWarnings()
        }

        val medicalListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (suppressListener) return@OnCheckedChangeListener
            if (isChecked) {
                suppressListener = true
                cbNone?.isChecked = false
                suppressListener = false
            }
            saveSelections()
            updateWarnings()
        }

        cbUpperBodyInjury?.setOnCheckedChangeListener(medicalListener)
        cbLowerBodyInjury?.setOnCheckedChangeListener(medicalListener)
        cbJointProblems?.setOnCheckedChangeListener(medicalListener)
        cbShortBreath?.setOnCheckedChangeListener(medicalListener)
    }

    private fun preloadSelections() {
        val ctx = requireContext()
        suppressListener = true
        cbUpperBodyInjury?.isChecked = OnboardingStore.getBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY)
        cbLowerBodyInjury?.isChecked = OnboardingStore.getBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY)
        cbJointProblems?.isChecked = OnboardingStore.getBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS)
        cbShortBreath?.isChecked = OnboardingStore.getBoolean(ctx, FlexiFitKeys.SHORT_BREATH)
        cbNone?.isChecked = OnboardingStore.getBoolean(ctx, FlexiFitKeys.HEALTH_NONE)
        suppressListener = false
    }

    private fun saveSelections() {
        val ctx = requireContext()
        OnboardingStore.putBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY, cbUpperBodyInjury?.isChecked == true)
        OnboardingStore.putBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY, cbLowerBodyInjury?.isChecked == true)
        OnboardingStore.putBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS, cbJointProblems?.isChecked == true)
        OnboardingStore.putBoolean(ctx, FlexiFitKeys.SHORT_BREATH, cbShortBreath?.isChecked == true)
        OnboardingStore.putBoolean(ctx, FlexiFitKeys.HEALTH_NONE, cbNone?.isChecked == true)
    }

    private fun updateWarnings() {
        val upper = cbUpperBodyInjury?.isChecked == true
        val lower = cbLowerBodyInjury?.isChecked == true
        val joint = cbJointProblems?.isChecked == true
        val breath = cbShortBreath?.isChecked == true
        val none = cbNone?.isChecked == true

        if (none) {
            cardHealthWarning?.visibility = View.GONE
            return
        }

        val messages = mutableListOf<String>()
        if (upper && !lower) messages += "Lower-body friendly programs will be recommended based on your upper body injury."
        if (lower && !upper) messages += "Upper-body friendly programs will be recommended based on your lower body injury."
        if (upper && lower) messages += "Recovery-safe or rehab-focused programs will be recommended."
        if (joint) messages += "Since you have joint problems, we recommend a rehab program."
        if (breath) messages += "Since you have breathing problems, we recommend starting at Beginner level."

        if (messages.isEmpty()) {
            cardHealthWarning?.visibility = View.GONE
        } else {
            cardHealthWarning?.visibility = View.VISIBLE
            tvHealthWarning?.text = messages.joinToString("\n\n")
        }
    }
}