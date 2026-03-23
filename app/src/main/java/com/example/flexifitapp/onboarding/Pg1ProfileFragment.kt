package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class Pg1ProfileFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg1_profile,
    isFirst = true
) {

    private lateinit var rvAge: RecyclerView
    private lateinit var tvAgeSelected: TextView
    private var selectedGender: String = ""
    private var selectedAge: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false

        // Get saved values
        val savedAge = if (isUpdate) {
            UserPrefs.getInt(requireContext(), UserPrefs.KEY_AGE, 25)
        } else {
            OnboardingStore.getInt(requireContext(), FlexiFitKeys.AGE, 25)
        }

        val savedGender = if (isUpdate) {
            UserPrefs.getString(requireContext(), UserPrefs.KEY_GENDER, "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.GENDER)
        }

        rvAge = view.findViewById(R.id.rvAge)
        tvAgeSelected = view.findViewById(R.id.tvAgeSelected)
        val cardMale = view.findViewById<View>(R.id.cardMale)
        val cardFemale = view.findViewById<View>(R.id.cardFemale)

        // Setup age wheel with saved value
        setupAgeWheel(savedAge)

        // Setup gender selection
        fun selectGender(g: String) {
            selectedGender = g
            if (isUpdate) {
                UserPrefs.putString(requireContext(), UserPrefs.KEY_GENDER, g)
            } else {
                OnboardingStore.putString(requireContext(), FlexiFitKeys.GENDER, g)
            }
            applyGenderSelection(g, cardMale, cardFemale)
        }

        if (savedGender.isNotBlank()) {
            selectGender(savedGender)
        }

        cardMale.setOnClickListener { selectGender("Male") }
        cardFemale.setOnClickListener { selectGender("Female") }
    }

    private fun setupAgeWheel(initialAge: Int) {
        val minAge = 10
        val maxAge = 100
        val ageList = (minAge..maxAge).toList()

        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvAge.layoutManager = lm

        val adapter = NumberPickerAdapter(ageList)

        adapter.onBindNumber = { tv, _, isSelected ->
            tv.alpha = if (isSelected) 1f else 0.45f
            tv.textSize = if (isSelected) 22f else 16f
            tv.gravity = Gravity.CENTER
        }

        rvAge.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvAge)

        val startPos = adapter.getVirtualPosForNumber(initialAge.coerceIn(minAge, maxAge))
        rvAge.post {
            lm.scrollToPositionWithOffset(startPos, 0)
            adapter.setSelectedVirtualPos(startPos)
            selectedAge = initialAge
            tvAgeSelected.text = initialAge.toString()
            // Save initial age
            val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
            if (isUpdate) {
                UserPrefs.putInt(requireContext(), UserPrefs.KEY_AGE, initialAge)
            } else {
                OnboardingStore.putInt(requireContext(), FlexiFitKeys.AGE, initialAge)
            }
        }

        rvAge.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                val snapped = snapHelper.findSnapView(lm) ?: return
                val pos = lm.getPosition(snapped)
                val age = adapter.getNumberAtVirtualPos(pos)

                adapter.setSelectedVirtualPos(pos)
                selectedAge = age
                tvAgeSelected.text = age.toString()

                val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
                if (isUpdate) {
                    UserPrefs.putInt(requireContext(), UserPrefs.KEY_AGE, age)
                } else {
                    OnboardingStore.putInt(requireContext(), FlexiFitKeys.AGE, age)
                }
            }
        })
    }

    private fun applyGenderSelection(gender: String, cardMale: View, cardFemale: View) {
        val maleSelected = gender.equals("Male", ignoreCase = true)
        val femaleSelected = gender.equals("Female", ignoreCase = true)
        cardMale.isSelected = maleSelected
        cardFemale.isSelected = femaleSelected
    }

    override fun validateBeforeNext(): String? {
        val isUpdate = arguments?.getBoolean("isUpdate", false) ?: false
        val age = if (isUpdate) {
            UserPrefs.getInt(requireContext(), UserPrefs.KEY_AGE, 0)
        } else {
            OnboardingStore.getInt(requireContext(), FlexiFitKeys.AGE, 0)
        }
        val gender = if (isUpdate) {
            UserPrefs.getString(requireContext(), UserPrefs.KEY_GENDER, "")
        } else {
            OnboardingStore.getString(requireContext(), FlexiFitKeys.GENDER)
        }
        return when {
            age < 10 || age > 100 -> "Please select a valid age (10-100)."
            gender.isBlank() -> "Please select your gender."
            else -> null
        }
    }
}
