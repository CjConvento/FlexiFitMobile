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

        preloadSelections()
        bindListeners()
        updateWarnings()
    }

    override fun validateBeforeNext(): String? {
        val anyChecked =
            cbUpperBodyInjury?.isChecked == true ||
                    cbLowerBodyInjury?.isChecked == true ||
                    cbJointProblems?.isChecked == true ||
                    cbShortBreath?.isChecked == true ||
                    cbNone?.isChecked == true

        return if (!anyChecked) "Please select at least one option." else null
    }

    private fun bindListeners() {
        cbNone?.setOnCheckedChangeListener { _, isChecked ->
            if (suppressListener) return@setOnCheckedChangeListener

            suppressListener = true
            if (isChecked) {
                cbUpperBodyInjury?.isChecked = false
                cbLowerBodyInjury?.isChecked = false
                cbJointProblems?.isChecked = false
                cbShortBreath?.isChecked = false
            }
            suppressListener = false

            saveSelections()
            updateWarnings()
        }

        val medicalListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (suppressListener) return@OnCheckedChangeListener

            suppressListener = true
            if (isChecked) {
                cbNone?.isChecked = false
            }
            suppressListener = false

            saveSelections()
            updateWarnings()
        }

        cbUpperBodyInjury?.setOnCheckedChangeListener(medicalListener)
        cbLowerBodyInjury?.setOnCheckedChangeListener(medicalListener)
        cbJointProblems?.setOnCheckedChangeListener(medicalListener)
        cbShortBreath?.setOnCheckedChangeListener(medicalListener)
    }

    private fun preloadSelections() {
        suppressListener = true

        cbUpperBodyInjury?.isChecked =
            OnboardingStore.getBoolean(requireContext(), "upper_body_injury", false)

        cbLowerBodyInjury?.isChecked =
            OnboardingStore.getBoolean(requireContext(), "lower_body_injury", false)

        cbJointProblems?.isChecked =
            OnboardingStore.getBoolean(requireContext(), "joint_problems", false)

        cbShortBreath?.isChecked =
            OnboardingStore.getBoolean(requireContext(), "short_breath", false)

        cbNone?.isChecked =
            OnboardingStore.getBoolean(requireContext(), "health_none", false)

        suppressListener = false
    }

    private fun saveSelections() {
        OnboardingStore.putBoolean(
            requireContext(),
            "upper_body_injury",
            cbUpperBodyInjury?.isChecked == true
        )
        OnboardingStore.putBoolean(
            requireContext(),
            "lower_body_injury",
            cbLowerBodyInjury?.isChecked == true
        )
        OnboardingStore.putBoolean(
            requireContext(),
            "joint_problems",
            cbJointProblems?.isChecked == true
        )
        OnboardingStore.putBoolean(
            requireContext(),
            "short_breath",
            cbShortBreath?.isChecked == true
        )
        OnboardingStore.putBoolean(
            requireContext(),
            "health_none",
            cbNone?.isChecked == true
        )
    }

    private fun updateWarnings() {
        val upper = cbUpperBodyInjury?.isChecked == true
        val lower = cbLowerBodyInjury?.isChecked == true
        val joint = cbJointProblems?.isChecked == true
        val breath = cbShortBreath?.isChecked == true
        val none = cbNone?.isChecked == true

        if (none) {
            cardHealthWarning?.visibility = View.GONE
            tvHealthWarning?.text = ""
            return
        }

        val messages = mutableListOf<String>()

        if (upper && !lower) {
            messages += "Lower-body friendly programs will be recommended based on your upper body injury."
        }

        if (lower && !upper) {
            messages += "Upper-body friendly programs will be recommended based on your lower body injury."
        }

        if (upper && lower) {
            messages += "Recovery-safe or rehab-focused programs will be recommended based on your selected injuries."
        }

        if (joint) {
            messages += "Since you have joint problems, we recommend choosing a rehab program for your recovery."
        }

        if (breath) {
            messages += "Since you have breathing problems, we recommend that you choose the beginner level."
        }

        if (messages.isEmpty()) {
            cardHealthWarning?.visibility = View.GONE
            tvHealthWarning?.text = ""
        } else {
            cardHealthWarning?.visibility = View.VISIBLE
            tvHealthWarning?.text = messages.joinToString("\n\n")
        }
    }
}