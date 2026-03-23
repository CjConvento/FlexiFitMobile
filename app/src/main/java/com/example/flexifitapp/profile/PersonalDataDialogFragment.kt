package com.example.flexifitapp.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class PersonalDataDialogFragment : DialogFragment(R.layout.dialog_personal_data) {

    private var btnBack: View? = null
    private var btnSave: MaterialButton? = null
    private var etName: TextInputEditText? = null
    private var etUsername: TextInputEditText? = null
    private var tvGenderValue: TextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupClicks()
        loadData()
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        btnSave = view.findViewById(R.id.btnSave)
        etName = view.findViewById(R.id.etName)
        etUsername = view.findViewById(R.id.etUsername)
        tvGenderValue = view.findViewById(R.id.tvGenderValue)
    }

    private fun setupClicks() {
        btnBack?.setOnClickListener { dismiss() }
        btnSave?.setOnClickListener { saveChanges() }
    }

    private fun loadData() {
        val ctx = requireContext()
        val name = UserPrefs.getString(ctx, UserPrefs.KEY_NAME, "")
        val username = UserPrefs.getString(ctx, UserPrefs.KEY_USERNAME, "") // now exists
        val gender = UserPrefs.getString(ctx, UserPrefs.KEY_GENDER, "")

        etName?.setText(name)
        etUsername?.setText(username)
        tvGenderValue?.text = gender.ifBlank { "Not set" }
    }

    private fun saveChanges() {
        val ctx = requireContext()
        val newName = etName?.text?.toString()?.trim().orEmpty()
        val newUsername = etUsername?.text?.toString()?.trim().orEmpty()

        if (newName.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(ctx, "Name and username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Build the full update request with current data
        val request = UpdateOnboardingRequest(
            age = UserPrefs.getInt(ctx, UserPrefs.KEY_AGE, 0),
            gender = UserPrefs.getString(ctx, UserPrefs.KEY_GENDER, "MALE"),
            heightCm = UserPrefs.getFloat(ctx, UserPrefs.KEY_HEIGHT_CM, 0f).toDouble(),
            weightKg = UserPrefs.getFloat(ctx, UserPrefs.KEY_WEIGHT_KG, 0f).toDouble(),
            targetWeightKg = UserPrefs.getInt(ctx, "target_weight_kg", 0).toDouble(),
            upperBodyInjury = UserPrefs.getBool(ctx, "upper_body_injury", false),
            lowerBodyInjury = UserPrefs.getBool(ctx, "lower_body_injury", false),
            jointProblems = UserPrefs.getBool(ctx, "joint_problems", false),
            shortBreath = UserPrefs.getBool(ctx, "short_breath", false),
            fitnessLifestyle = UserPrefs.getString(ctx, "fitness_lifestyle", "ACTIVE"),
            fitnessLevel = UserPrefs.getString(ctx, "fitness_level", "BEGINNER"),
            environment = UserPrefs.getString(ctx, "environment", "GYM"),
            bodyCompGoal = UserPrefs.getString(ctx, UserPrefs.KEY_BODYCOMP_GOAL, "MAINTAIN_WEIGHT"),
            dietaryType = UserPrefs.getString(ctx, "dietary_type", "STANDARD"),
            fitnessGoals = UserPrefs.getStringSet(ctx, UserPrefs.KEY_FITNESS_GOAL_SET).toList(),
            selectedPrograms = UserPrefs.getStringSet(ctx, UserPrefs.KEY_SELECTED_PROGRAMS).toList(),
            isRehabUser = UserPrefs.getBool(ctx, "is_rehab_user", false),
            name = newName,
            username = newUsername
        )

        lifecycleScope.launch {
            try {
                val api = ApiClient.api() // use the existing method to get API service
                val response = api.updateFullProfile(request)
                if (response.isSuccessful) {
                    parentFragmentManager.setFragmentResult(REQUEST_KEY, Bundle())
                    dismiss()
                } else {
                    Toast.makeText(ctx, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val REQUEST_KEY = "personal_data_edit_result"
    }
}