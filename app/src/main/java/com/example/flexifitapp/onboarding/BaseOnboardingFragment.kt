package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flexifitapp.R

abstract class      BaseOnboardingFragment(
    layoutId: Int,
    @IdRes private val nextActionId: Int?,
    private val isFirst: Boolean = false
) : Fragment(layoutId) {

    /**
     * Override this in child fragments if you want to block Next
     * and show a message when required fields are missing.
     *
     * Return null = OK (allow next)
     * Return String = error message (block next)
     */
    open fun validateBeforeNext(): String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



            nextActionId?.let { findNavController().navigate(it) }
        }
    }
