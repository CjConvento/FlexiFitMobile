package com.example.flexifitapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SurveyStep2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_step2)

        // 🔽 Radio Group + buttons
        val rgGoal = findViewById<RadioGroup>(R.id.rgGoal)
        val rbCutting = findViewById<MaterialRadioButton>(R.id.rbCutting)
        val rbBulking = findViewById<MaterialRadioButton>(R.id.rbBulking)
        val rbLeanBulk = findViewById<MaterialRadioButton>(R.id.rbLeanBulk)
        val rbBodyRecomp = findViewById<MaterialRadioButton>(R.id.rbBodyRecomp)

        // 🔽 Dropdown (MaterialAutoCompleteTextView)
        val etHealth = findViewById<MaterialAutoCompleteTextView>(R.id.etHealthConditions)

        val healthAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.health_conditions)
        )
        etHealth.setAdapter(healthAdapter)

        // 🔽 Buttons
        val btnBack = findViewById<MaterialButton>(R.id.btnBackStep2)
        val btnFinish = findViewById<MaterialButton>(R.id.btnFinish)

        btnBack.setOnClickListener {
            finish() // balik Step 1
        }

        // ------------------------------
        // 🔥 FINISH BUTTON LOGIC
        // ------------------------------
        btnFinish.setOnClickListener {

            // ✅ Siguraduhin may napiling goal
            val selectedId = rgGoal.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please choose your fitness goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = when (selectedId) {
                R.id.rbCutting -> "Cutting"
                R.id.rbBulking -> "Bulking"
                R.id.rbLeanBulk -> "Lean Bulk"
                R.id.rbBodyRecomp -> "Body Recomposition"
                else -> ""
            }

            val health = etHealth.text?.toString()?.trim().orEmpty()

            // 🔐 SAVE SURVEY COMPLETION TO FIREBASE FOR CURRENT USER (UID)
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                val uid = currentUser.uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)

                val updateMap = mapOf(
                    "surveyCompleted" to true,
                    "surveyGoal" to goal,
                    "surveyHealth" to health
                )

                ref.updateChildren(updateMap)
            }

            Toast.makeText(
                this,
                "Survey Completed!\nGoal: $goal\nHealth: $health",
                Toast.LENGTH_LONG
            ).show()

            // 🚀 GO TO MAIN ACTIVITY
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
