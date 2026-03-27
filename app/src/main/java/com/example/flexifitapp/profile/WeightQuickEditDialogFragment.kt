package com.example.flexifitapp.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import com.example.flexifitapp.profile.AchievementEngine
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.ProfileFragment
import com.example.flexifitapp.profile.UpdateWeightRequest
import okhttp3.ResponseBody
import retrofit2.Response


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

        // Use getFloat and convert to Int
        val currentWeight = UserPrefs.getFloat(ctx, UserPrefs.KEY_WEIGHT_KG, 0f).toInt()
        val targetWeight = UserPrefs.getFloat(ctx, UserPrefs.KEY_TARGET_WEIGHT_KG, 0f).toInt()

        tvCurrentWeight?.text = if (currentWeight > 0) "$currentWeight kg" else "0 kg"
        tvTargetWeight?.text = if (targetWeight > 0) "$targetWeight kg" else "0 kg"

        if (currentWeight > 0) {
            etNewWeight?.setText(currentWeight.toString())
            etNewWeight?.setSelection(etNewWeight?.text?.length ?: 0)
        }
    }

    private fun setupClicks() {
        btnClose?.setOnClickListener { dismiss() }

        btnSave?.setOnClickListener {
            val input = etNewWeight?.text?.toString()?.trim().orEmpty()
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newWeight = input.toIntOrNull()
            if (newWeight == null || newWeight <= 0) {
                Toast.makeText(requireContext(), "Invalid weight value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ctx = requireContext()

            // 1. Update local preferences
            UserPrefs.putInt(ctx, UserPrefs.KEY_WEIGHT_KG, newWeight)
            UserPrefs.putInt(ctx, UserPrefs.KEY_LATEST_WEIGHT_KG, newWeight)
            UserPrefs.putBool(ctx, UserPrefs.KEY_HAS_WEIGHT_LOG, true)

            // 2. Update BMI locally
            val heightCm = UserPrefs.getFloat(ctx, UserPrefs.KEY_HEIGHT_CM, 0f)
            if (heightCm > 0f) {
                val heightM = heightCm / 100f
                val newBmi = newWeight / (heightM * heightM)
                UserPrefs.putFloat(ctx, UserPrefs.KEY_BMI, newBmi)
                UserPrefs.putString(ctx, "bmi_category", "") // reset category, will be updated by server
            }

            // 3. Update achievements locally
            AchievementEngine.updateAchievementsLocally(ctx)

            // 4. Disable button to prevent double‑click
            btnSave?.isEnabled = false
            btnSave?.text = "Saving..."

            // 5. Sync with server in background
            lifecycleScope.launch {
                try {
                    val api = ApiClient.api()
                    val request = UpdateWeightRequest(newWeight.toDouble())
                    val response = api.updateWeight(request)

                    if (response.isSuccessful) {
                        // Notify parent fragment to refresh profile data
                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            Bundle().apply { putInt(BUNDLE_NEW_WEIGHT, newWeight) }
                        )
                        // Also refresh the profile data if needed
                        (parentFragment as? ProfileFragment)?.syncProfileFromServer()
                        dismiss()
                    } else {
                        Toast.makeText(ctx, "Failed to sync weight with server", Toast.LENGTH_SHORT).show()
                        btnSave?.isEnabled = true
                        btnSave?.text = "Save"
                    }
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSave?.isEnabled = true
                    btnSave?.text = "Save"
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY = "weight_quick_edit_result"
        const val BUNDLE_NEW_WEIGHT = "bundle_new_weight"
    }
}