package com.example.flexifitapp.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R

// ... (mga imports mo sa taas)

// Dito mo siya babaguhin babe, sa loob ng parenthesis ()
class BmiDetailsDialog(
    private val bmi: Double,
    private val status: String // <-- Dagdag natin 'to para galing na sa API
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dshb_bmi_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val txtScore = view.findViewById<TextView>(R.id.diagBmiScore)
        val txtStatus = view.findViewById<TextView>(R.id.diagBmiStatus)
        val txtAdvice = view.findViewById<TextView>(R.id.diagBmiAdvice)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        // 1. I-set ang Score
        txtScore.text = String.format("%.1f", bmi)

        // 2. Gamitin ang Status galing API (Wala na yung mahabang 'when' block!)
        txtStatus.text = status

        // 3. Simple logic na lang para sa Advice text
        txtAdvice.text = when {
            bmi < 18.5 -> "You might need to increase your calorie intake with nutrient-dense foods."
            bmi < 25.0 -> "Great job! Keep maintaining your current lifestyle and balanced diet."
            else -> "Consider a combination of consistent cardio and portion control."
        }

        btnDone.setOnClickListener { dismiss() }
    }
}