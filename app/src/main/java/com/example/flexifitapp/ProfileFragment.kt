package com.example.flexifitapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // TODO: Next steps (AI + logic)
        // 1. Kunin user data (fitness level, BMI, etc.)
        // 2. I-filter ang recommended programs depende sa level
        // 3. Palitan ang "Today’s Workout" content based on selected program

        return view
    }
}
