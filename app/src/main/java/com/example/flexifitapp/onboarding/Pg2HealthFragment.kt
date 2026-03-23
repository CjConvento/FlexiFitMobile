package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.google.android.material.card.MaterialCardView

class Pg2HealthFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg2_health
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

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        cbUpperBodyInjury = view.findViewById(R.id.cbHBi)
        cbLowerBodyInjury = view.findViewById(R.id.cbLBi)
        cbJointProblems = view.findViewById(R.id.cbJoint)
        cbShortBreath = view.findViewById(R.id.cbSB)
        cbNone = view.findViewById(R.id.cbNone)
        cardHealthWarning = view.findViewById(R.id.cardHealthWarning)
        tvHealthWarning = view.findViewById(R.id.tvHealthWarning)

        // Load saved values
        val upper = if (isUpdate) {
            UserPrefs.getBool(requireContext(), "upper_body_injury", false)
        } else {
            OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.UPPER_BODY_INJURY)
        }
        val lower = if (isUpdate) {
            UserPrefs.getBool(requireContext(), "lower_body_injury", false)
        } else {
            OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.LOWER_BODY_INJURY)
        }
        val joint = if (isUpdate) {
            UserPrefs.getBool(requireContext(), "joint_problems", false)
        } else {
            OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.JOINT_PROBLEMS)
        }
        val breath = if (isUpdate) {
            UserPrefs.getBool(requireContext(), "short_breath", false)
        } else {
            OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.SHORT_BREATH)
        }
        val none = if (isUpdate) {
            !(upper || lower || joint || breath)
        } else {
            OnboardingStore.getBoolean(requireContext(), FlexiFitKeys.HEALTH_NONE)
        }

        suppressListener = true
        cbUpperBodyInjury?.isChecked = upper
        cbLowerBodyInjury?.isChecked = lower
        cbJointProblems?.isChecked = joint
        cbShortBreath?.isChecked = breath
        cbNone?.isChecked = none
        suppressListener = false

        bindListeners(isUpdate)
        updateWarnings()
    }

    private fun bindListeners(isUpdate: Boolean) {
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
            saveSelections(isUpdate)
            updateWarnings()
        }

        val medicalListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (suppressListener) return@OnCheckedChangeListener
            if (isChecked) {
                suppressListener = true
                cbNone?.isChecked = false
                suppressListener = false
            }
            saveSelections(isUpdate)
            updateWarnings()
        }

        cbUpperBodyInjury?.setOnCheckedChangeListener(medicalListener)
        cbLowerBodyInjury?.setOnCheckedChangeListener(medicalListener)
        cbJointProblems?.setOnCheckedChangeListener(medicalListener)
        cbShortBreath?.setOnCheckedChangeListener(medicalListener)
    }

    private fun saveSelections(isUpdate: Boolean) {
        val ctx = requireContext()
        val upper = cbUpperBodyInjury?.isChecked == true
        val lower = cbLowerBodyInjury?.isChecked == true
        val joint = cbJointProblems?.isChecked == true
        val breath = cbShortBreath?.isChecked == true
        val none = cbNone?.isChecked == true

        if (isUpdate) {
            UserPrefs.putBool(ctx, "upper_body_injury", upper)
            UserPrefs.putBool(ctx, "lower_body_injury", lower)
            UserPrefs.putBool(ctx, "joint_problems", joint)
            UserPrefs.putBool(ctx, "short_breath", breath)
            UserPrefs.putBool(ctx, "is_rehab_user", upper || lower || joint)
        } else {
            OnboardingStore.putBoolean(ctx, FlexiFitKeys.UPPER_BODY_INJURY, upper)
            OnboardingStore.putBoolean(ctx, FlexiFitKeys.LOWER_BODY_INJURY, lower)
            OnboardingStore.putBoolean(ctx, FlexiFitKeys.JOINT_PROBLEMS, joint)
            OnboardingStore.putBoolean(ctx, FlexiFitKeys.SHORT_BREATH, breath)
            OnboardingStore.putBoolean(ctx, FlexiFitKeys.HEALTH_NONE, none)
            OnboardingStore.putBoolean(ctx, FlexiFitKeys.IS_REHAB_USER, upper || lower || joint)
        }
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

    override fun validateBeforeNext(): String? {
        val anyChecked = cbUpperBodyInjury?.isChecked == true ||
                cbLowerBodyInjury?.isChecked == true ||
                cbJointProblems?.isChecked == true ||
                cbShortBreath?.isChecked == true ||
                cbNone?.isChecked == true
        return if (!anyChecked) "Please select at least one option." else null
    }
}