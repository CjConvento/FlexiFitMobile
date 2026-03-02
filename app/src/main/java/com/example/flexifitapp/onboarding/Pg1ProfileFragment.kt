package com.example.flexifitapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
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
            selectedGender = g
            OnboardingStore.putString(requireContext(), "gender", g)
            applyGenderSelection(g)
        }

// restore saved selection
        selectGender(OnboardingStore.getString(requireContext(), "gender"))

// click listeners
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
            tv.gravity = android.view.Gravity.CENTER // extra safety
        }

        rvAge.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvAge)

        val savedAge = OnboardingStore.getInt(requireContext(), "age", 18).coerceIn(minAge, maxAge)
        val startPos = adapter.getVirtualPosForNumber(savedAge)

        rvAge.post {
            lm.scrollToPositionWithOffset(startPos, 0)
            adapter.setSelectedVirtualPos(startPos)
            selectedAge = savedAge
            tvAgeSelected.text = savedAge.toString()
            OnboardingStore.putInt(requireContext(), "age", savedAge)
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
                OnboardingStore.putInt(requireContext(), "age", age)
            }
        })
    }

    override fun validateBeforeNext(): String? {
        val age = OnboardingStore.getInt(requireContext(), "age", 0)
        val gender = OnboardingStore.getString(requireContext(), "gender")

        return when {
            age <= 0 -> "Please select your age."
            age < 10 || age > 100 -> "Please enter an age between 10 and 100."
            gender.isBlank() -> "Please select your gender."
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