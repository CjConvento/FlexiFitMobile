package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flexifitapp.R

abstract class BaseOnboardingFragment(
    layoutId: Int,
    @IdRes private val nextActionId: Int?,
    private val isFirst: Boolean = false
) : Fragment(layoutId) {

    open fun validateBeforeNext(): String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnConfirm = view.findViewById<TextView?>(R.id.btnConfirm)

        btnConfirm?.setOnClickListener {
            val error = validateBeforeNext()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            nextActionId?.let { actionId ->
                findNavController().navigate(actionId)
            }
        }
    }

    protected fun goBack() {
        if (!isFirst) {
            findNavController().navigateUp()
        }
    }
}