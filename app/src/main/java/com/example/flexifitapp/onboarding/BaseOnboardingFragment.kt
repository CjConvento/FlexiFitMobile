package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.example.flexifitapp.OnboardingActivity
import com.example.flexifitapp.R

abstract class BaseOnboardingFragment(
    layoutId: Int,
    @IdRes private val nextActionId: Int?,
    private val isFirst: Boolean = false
) : Fragment(layoutId) {

    open fun validateBeforeNext(): String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View?>(R.id.btnConfirm)?.setOnClickListener {
            val error = validateBeforeNext()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use activity's nextPage() instead of NavController
            (requireActivity() as OnboardingActivity).nextPage()
        }
    }

    protected fun goBack() {
        if (!isFirst) {
            (requireActivity() as OnboardingActivity).previousPage()
        }
    }
}