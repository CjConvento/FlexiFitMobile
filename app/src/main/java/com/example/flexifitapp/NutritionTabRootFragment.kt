package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class NutritionTabRootFragment : Fragment(R.layout.fragment_nutri) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOpenCalendar = view.findViewById<ImageButton>(R.id.btnOpenCalendar)

        btnOpenCalendar.setOnClickListener {
            val b = bundleOf(NavKeys.ARG_SOURCE_TAB to "NUTRITION")
            findNavController().navigate(R.id.unifiedCalendarFragment, b)
        }
    }
}