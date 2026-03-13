package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class Pg1ProfileFragment : BaseOnboardingFragment(
    layoutId = R.layout.obd_fragment_pg1_profile,
    nextActionId = R.id.a1,
    isFirst = true
) {

    private var selectedGender: String = ""
    private var selectedAge: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== AGE WHEEL =====
        val rvAge = view.findViewById<RecyclerView>(R.id.rvAge)
        val tvAgeSelected = view.findViewById<TextView>(R.id.tvAgeSelected)
        setupAgeWheel(rvAge, tvAgeSelected)

        // ===== GENDER (cards) =====
        val cardMale = view.findViewById<View>(R.id.cardMale)
        val cardFemale = view.findViewById<View>(R.id.cardFemale)

        fun applyGenderSelection(g: String) {
            val maleSelected = g.equals("Male", ignoreCase = true)
            val femaleSelected = g.equals("Female", ignoreCase = true)

            cardMale.isSelected = maleSelected
            cardFemale.isSelected = femaleSelected
        }

        fun selectGender(g: String) {
            if (g.isBlank()) return // Huwag mag-save kung empty
            selectedGender = g
            OnboardingStore.putString(requireContext(), FlexiFitKeys.GENDER, g)
            applyGenderSelection(g)
        }

        // Restore saved selection gamit ang FlexiFitKeys
        val savedGender = OnboardingStore.getString(requireContext(), FlexiFitKeys.GENDER)
        if (savedGender.isNotBlank()) {
            selectGender(savedGender)
        }

        // Click listeners
        cardMale.setOnClickListener { selectGender("Male") }
        cardFemale.setOnClickListener { selectGender("Female") }
    }

    private fun setupAgeWheel(rvAge: RecyclerView, tvAgeSelected: TextView) {
        val minAge = 10
        val maxAge = 100
        val ageList = (minAge..maxAge).toList()

        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvAge.layoutManager = lm

        val adapter = NumberPickerAdapter(ageList)

        adapter.onBindNumber = { tv, _, isSelected ->
            tv.alpha = if (isSelected) 1f else 0.45f
            tv.textSize = if (isSelected) 22f else 16f
            tv.gravity = android.view.Gravity.CENTER
        }

        rvAge.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvAge)

        // Restore age gamit ang FlexiFitKeys
        val savedAge = OnboardingStore.getInt(requireContext(), FlexiFitKeys.AGE, 25).coerceIn(minAge, maxAge)
        val startPos = adapter.getVirtualPosForNumber(savedAge)

        rvAge.post {
            lm.scrollToPositionWithOffset(startPos, 0)
            adapter.setSelectedVirtualPos(startPos)
            selectedAge = savedAge
            tvAgeSelected.text = savedAge.toString()
            // I-save ang initial/restored value
            OnboardingStore.putInt(requireContext(), FlexiFitKeys.AGE, savedAge)
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
                OnboardingStore.putInt(requireContext(), FlexiFitKeys.AGE, age)
            }
        })
    }

    override fun validateBeforeNext(): String? {
        val age = OnboardingStore.getInt(requireContext(), FlexiFitKeys.AGE, 0)
        val gender = OnboardingStore.getString(requireContext(), FlexiFitKeys.GENDER)

        return when {
            age < 10 || age > 100 -> "Please select a valid age (10-100)."
            gender.isBlank() -> "Please select your gender."
            else -> null
        }
    }
}