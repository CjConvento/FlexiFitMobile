package com.example.flexifitapp.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R

class BmiDetailsDialog(private val bmi: Double) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dshb_bmi_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Transparent background para sa corners ng card
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val txtScore = view.findViewById<TextView>(R.id.diagBmiScore)
        val txtStatus = view.findViewById<TextView>(R.id.diagBmiStatus)
        val txtAdvice = view.findViewById<TextView>(R.id.diagBmiAdvice)
        val btnDone = view.findViewById<Button>(R.id.btnDone)

        txtScore.text = String.format("%.1f", bmi)

        when {
            bmi < 18.5 -> {
                txtStatus.text = "Underweight"
                txtAdvice.text = "You might need to increase your calorie intake with nutrient-dense foods."
            }
            bmi < 25.0 -> {
                txtStatus.text = "Normal Weight"
                txtAdvice.text = "Great job! Keep maintaining your current lifestyle and balanced diet."
            }
            else -> {
                txtStatus.text = "Overweight"
                txtAdvice.text = "Consider a combination of consistent cardio and portion control."
            }
        }

        btnDone.setOnClickListener { dismiss() }
    }
}