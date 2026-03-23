package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.R

abstract class BaseOnboardingFragment(
    layoutId: Int,
    private val isFirst: Boolean = false
) : Fragment(layoutId) {

    open fun validateBeforeNext(): String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Next/Confirm button
        view.findViewById<View?>(R.id.btnConfirm)?.setOnClickListener {
            val error = validateBeforeNext()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the activity's nextPage method
            (requireActivity() as? OnboardingActivity)?.nextPage()
        }

        // Back button (if exists)
        view.findViewById<View?>(R.id.btnBack)?.setOnClickListener {
            goBack()
        }
    }

    protected fun goBack() {
        if (!isFirst) {
            (requireActivity() as? OnboardingActivity)?.previousPage()
        }
    }
}