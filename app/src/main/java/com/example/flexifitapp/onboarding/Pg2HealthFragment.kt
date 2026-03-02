package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.flexifitapp.R

class Pg2HealthFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg2_health,
    nextActionId = R.id.a3
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Example: RadioGroup selection (Yes/No / Injury / Medical)
        // Palitan IDs based sa XML mo:
        val rgInjury = view.findViewById<RadioGroup?>(R.id.rgInjury)  // example id
        val rgMedical = view.findViewById<RadioGroup?>(R.id.rgMedical) // example id

        // Preload
        preloadRadio(rgInjury, "has_injury")
        preloadRadio(rgMedical, "has_medical_condition")

        // Auto-save on change
        rgInjury?.setOnCheckedChangeListener { group, checkedId ->
            val label = group.findViewById<RadioButton?>(checkedId)?.text?.toString().orEmpty()
            OnboardingStore.putString(requireContext(), "has_injury", label)
        }

        rgMedical?.setOnCheckedChangeListener { group, checkedId ->
            val label = group.findViewById<RadioButton?>(checkedId)?.text?.toString().orEmpty()
            OnboardingStore.putString(requireContext(), "has_medical_condition", label)
        }
    }

    override fun validateBeforeNext(): String? {
        // Required: at least one selection for each (if you want both required)
        // If not required, remove one.
        val injury = OnboardingStore.getString(requireContext(), "has_injury")
        val medical = OnboardingStore.getString(requireContext(), "has_medical_condition")

        return when {
            injury.isBlank() -> "Please answer the injury question."
            medical.isBlank() -> "Please answer the medical condition question."
            else -> null
        }
    }

    private fun preloadRadio(rg: RadioGroup?, key: String) {
        if (rg == null) return
        val saved = OnboardingStore.getString(requireContext(), key)
        if (saved.isBlank()) return

        for (i in 0 until rg.childCount) {
            val child = rg.getChildAt(i)
            if (child is RadioButton && child.text.toString().equals(saved, ignoreCase = true)) {
                child.isChecked = true
                break
            }
        }
    }
}