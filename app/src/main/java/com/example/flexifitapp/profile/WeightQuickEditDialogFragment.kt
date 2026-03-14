package com.example.flexifitapp.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.example.flexifitapp.profile.AchievementEngine
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class WeightQuickEditDialogFragment :
    DialogFragment(R.layout.dialog_weight_quick_edit) {

    private var btnClose: ImageView? = null
    private var tvCurrentWeight: TextView? = null
    private var tvTargetWeight: TextView? = null
    private var etNewWeight: TextInputEditText? = null
    private var btnSave: MaterialButton? = null

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
                (resources.displayMetrics.widthPixels * 0.82f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        loadWeightData()
        setupClicks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        btnClose = null
        tvCurrentWeight = null
        tvTargetWeight = null
        etNewWeight = null
        btnSave = null
    }

    private fun bindViews(view: View) {
        btnClose = view.findViewById(R.id.btnClose)
        tvCurrentWeight = view.findViewById(R.id.tvCurrentWeight)
        tvTargetWeight = view.findViewById(R.id.tvTargetWeight)
        etNewWeight = view.findViewById(R.id.etNewWeight)
        btnSave = view.findViewById(R.id.btnSaveWeight)
    }

    private fun loadWeightData() {
        val ctx = requireContext()

        val currentWeight = UserPrefs.getInt(ctx, UserPrefs.KEY_WEIGHT_KG, 0)
        val targetWeight = UserPrefs.getInt(ctx, UserPrefs.KEY_TARGET_WEIGHT_KG, 0)

        tvCurrentWeight?.text = if (currentWeight > 0) "$currentWeight kg" else "0 kg"
        tvTargetWeight?.text = if (targetWeight > 0) "$targetWeight kg" else "0 kg"

        if (currentWeight > 0) {
            etNewWeight?.setText(currentWeight.toString())
            etNewWeight?.setSelection(etNewWeight?.text?.length ?: 0)
        }
    }

    private fun setupClicks() {
        btnClose?.setOnClickListener {
            dismiss()
        }

        btnSave?.setOnClickListener {
            val input = etNewWeight?.text?.toString()?.trim().orEmpty()
            // ... (keep your existing error checks)

            val newWeight = input.toIntOrNull() ?: return@setOnClickListener
            val ctx = requireContext()

            // 1. Save new weight
            UserPrefs.putInt(ctx, UserPrefs.KEY_WEIGHT_KG, newWeight)
            UserPrefs.putInt(ctx, UserPrefs.KEY_LATEST_WEIGHT_KG, newWeight)
            UserPrefs.putBool(ctx, UserPrefs.KEY_HAS_WEIGHT_LOG, true)

            // 2. AUTOMATIC BMI UPDATE (Eto yung bago babe)
            val heightCm = UserPrefs.getFloat(ctx, UserPrefs.KEY_HEIGHT_CM, 0f)
            if (heightCm > 0f) {
                val heightM = heightCm / 100f
                val newBmi = newWeight / (heightM * heightM)
                UserPrefs.putFloat(ctx, UserPrefs.KEY_BMI, newBmi)

                // I-clear din natin yung server category para magcompute ulit
                // yung local display sa Nutritional Data dialog
                UserPrefs.putString(ctx, "bmi_category", "")
            }

            // 3. Update achievements
            AchievementEngine.updateAchievementsLocally(ctx) // Siguraduhin na match ang function name sa Engine mo

            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putInt(BUNDLE_NEW_WEIGHT, newWeight) }
            )
            dismiss()
        }
    }

    companion object {
        const val REQUEST_KEY = "weight_quick_edit_result"
        const val BUNDLE_NEW_WEIGHT = "bundle_new_weight"
    }
}