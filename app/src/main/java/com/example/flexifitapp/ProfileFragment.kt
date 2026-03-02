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
        val view = inflater.inflate(R.layout.fragment_profileff, container, false)

        // TODO: Next steps (workflow and rule based logic)
        // 1. Kunin user profile data (age, gender, height, weight, body composition goal, fitness level, selected diet type, selected programs)
        // 2. I-filter ang recommended programs depende sa fitness goal, body composition goal, workout location and fitness level)
        // 3. Palitan ang "Day's Workout" content based on selected program

        return view
    }
}
