package com.example.flexifitapp.profile

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs
import java.util.Locale
import kotlin.math.pow

class NutritionalDataDialogFragment : DialogFragment(R.layout.dialog_nutritional_data) {

    private var btnBack: ImageView? = null

    private var rowNutritionalGoalHeader: LinearLayout? = null
    private var rowAgeHeader: LinearLayout? = null
    private var rowHeightHeader: LinearLayout? = null
    private var rowCurrentWeightHeader: LinearLayout? = null
    private var rowTargetWeightHeader: LinearLayout? = null
    private var rowBmiHeader: LinearLayout? = null
    private var rowBmiCategoryHeader: LinearLayout? = null

    private var ivNutritionalGoalAction: ImageView? = null
    private var ivAgeAction: ImageView? = null
    private var ivHeightAction: ImageView? = null
    private var ivCurrentWeightAction: ImageView? = null
    private var ivTargetWeightAction: ImageView? = null
    private var ivBmiAction: ImageView? = null
    private var ivBmiCategoryAction: ImageView? = null

    private var layoutNutritionalGoalContent: LinearLayout? = null
    private var layoutAgeContent: LinearLayout? = null
    private var layoutHeightContent: LinearLayout? = null
    private var layoutCurrentWeightContent: LinearLayout? = null
    private var layoutTargetWeightContent: LinearLayout? = null
    private var layoutBmiContent: LinearLayout? = null
    private var layoutBmiCategoryContent: LinearLayout? = null

    private var tvNutritionalGoalValue: TextView? = null
    private var tvAgeValue: TextView? = null
    private var tvHeightValue: TextView? = null
    private var tvCurrentWeightValue: TextView? = null
    private var tvTargetWeightValue: TextView? = null
    private var tvBmiValue: TextView? = null
    private var tvBmiCategoryValue: TextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.86f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)

        btnBack?.setOnClickListener { dismiss() }

        bindData()
        setupExpanders()
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)

        rowNutritionalGoalHeader = view.findViewById(R.id.rowNutritionalGoalHeader)
        rowAgeHeader = view.findViewById(R.id.rowAgeHeader)
        rowHeightHeader = view.findViewById(R.id.rowHeightHeader)
        rowCurrentWeightHeader = view.findViewById(R.id.rowCurrentWeightHeader)
        rowTargetWeightHeader = view.findViewById(R.id.rowTargetWeightHeader)
        rowBmiHeader = view.findViewById(R.id.rowBmiHeader)
        rowBmiCategoryHeader = view.findViewById(R.id.rowBmiCategoryHeader)

        ivNutritionalGoalAction = view.findViewById(R.id.ivNutritionalGoalAction)
        ivAgeAction = view.findViewById(R.id.ivAgeAction)
        ivHeightAction = view.findViewById(R.id.ivHeightAction)
        ivCurrentWeightAction = view.findViewById(R.id.ivCurrentWeightAction)
        ivTargetWeightAction = view.findViewById(R.id.ivTargetWeightAction)
        ivBmiAction = view.findViewById(R.id.ivBmiAction)
        ivBmiCategoryAction = view.findViewById(R.id.ivBmiCategoryAction)

        layoutNutritionalGoalContent = view.findViewById(R.id.layoutNutritionalGoalContent)
        layoutAgeContent = view.findViewById(R.id.layoutAgeContent)
        layoutHeightContent = view.findViewById(R.id.layoutHeightContent)
        layoutCurrentWeightContent = view.findViewById(R.id.layoutCurrentWeightContent)
        layoutTargetWeightContent = view.findViewById(R.id.layoutTargetWeightContent)
        layoutBmiContent = view.findViewById(R.id.layoutBmiContent)
        layoutBmiCategoryContent = view.findViewById(R.id.layoutBmiCategoryContent)

        tvNutritionalGoalValue = view.findViewById(R.id.tvNutritionalGoalValue)
        tvAgeValue = view.findViewById(R.id.tvAgeValue)
        tvHeightValue = view.findViewById(R.id.tvHeightValue)
        tvCurrentWeightValue = view.findViewById(R.id.tvCurrentWeightValue)
        tvTargetWeightValue = view.findViewById(R.id.tvTargetWeightValue)
        tvBmiValue = view.findViewById(R.id.tvBmiValue)
        tvBmiCategoryValue = view.findViewById(R.id.tvBmiCategoryValue)
    }

    private fun bindData() {

        val ctx = requireContext()

        val nutritionalGoal = readNutritionGoal()
        val age = readAge()
        val heightCm = readHeightCm()
        val currentWeightKg = readCurrentWeightKg()
        val targetWeightKg = readTargetWeightKg()

        // ✅ Add logs here
        Log.d("NutritionalDialog", "heightCm=$heightCm, weightKg=$currentWeightKg, targetKg=$targetWeightKg")

        // BMI Logic
        val bmi = computeBmi(currentWeightKg, heightCm)

        // Eto yung galing sa Server Sync natin
        val serverBmiCategory = UserPrefs.getString(ctx, "bmi_category", "")

        // UI Mapping
        tvNutritionalGoalValue?.text = nutritionalGoal.ifBlank { "-" }
        tvAgeValue?.text = if (age > 0) "$age years old" else "-"
        tvHeightValue?.text = if (heightCm > 0f) "${formatNumber(heightCm)} cm" else "-"
        tvCurrentWeightValue?.text = if (currentWeightKg > 0f) "${formatNumber(currentWeightKg)} kg" else "-"
        tvTargetWeightValue?.text = if (targetWeightKg > 0f) "${formatNumber(targetWeightKg)} kg" else "-"

        tvBmiValue?.text = if (bmi > 0f) String.format(Locale.US, "%.1f", bmi) else "-"

        // Eto yung 'ifBlank' logic para sa Category
        // Kung blank ang galing server, gagamit siya ng local computation
        tvBmiCategoryValue?.text = serverBmiCategory.ifBlank { getBmiCategory(bmi) }
    }

    private fun setupExpanders() {
        setupToggle(
            rowNutritionalGoalHeader,
            layoutNutritionalGoalContent,
            ivNutritionalGoalAction
        )
        setupToggle(rowAgeHeader, layoutAgeContent, ivAgeAction)
        setupToggle(rowHeightHeader, layoutHeightContent, ivHeightAction)
        setupToggle(rowCurrentWeightHeader, layoutCurrentWeightContent, ivCurrentWeightAction)
        setupToggle(rowTargetWeightHeader, layoutTargetWeightContent, ivTargetWeightAction)
        setupToggle(rowBmiHeader, layoutBmiContent, ivBmiAction)
        setupToggle(rowBmiCategoryHeader, layoutBmiCategoryContent, ivBmiCategoryAction)
    }

    private fun setupToggle(
        header: View?,
        content: View?,
        icon: ImageView?
    ) {
        fun syncIcon() {
            val isExpanded = content?.visibility == View.VISIBLE
            icon?.setImageResource(
                if (isExpanded) {
                    R.drawable.baseline_keyboard_arrow_down_24
                } else {
                    R.drawable.baseline_arrow_right_ios_new_24
                }
            )
        }

        syncIcon()

        header?.setOnClickListener {
            val isExpanded = content?.visibility == View.VISIBLE
            content?.visibility = if (isExpanded) View.GONE else View.VISIBLE
            syncIcon()
        }

        icon?.setOnClickListener {
            val isExpanded = content?.visibility == View.VISIBLE
            content?.visibility = if (isExpanded) View.GONE else View.VISIBLE
            syncIcon()
        }
    }

    private fun readNutritionGoal(): String {
        val direct = UserPrefs.getString(requireContext(), "nutritional_goal", "")
        if (direct.isNotBlank()) return direct

        val bodyCompGoal = UserPrefs.getString(requireContext(), "bodycomp_goal", "")
        if (bodyCompGoal.isNotBlank()) return bodyCompGoal

        val fitnessGoal = UserPrefs.getString(requireContext(), "fitness_goal", "")
        if (fitnessGoal.isNotBlank()) return fitnessGoal

        return "-"
    }

    private fun readAge(): Int {
        return UserPrefs.getInt(requireContext(), "age", 0)
    }

    private fun readHeightCm(): Float {
        return UserPrefs.getFloat(requireContext(), "height_cm", 0f)
    }

    private fun readCurrentWeightKg(): Float {
        return UserPrefs.getFloat(requireContext(), UserPrefs.KEY_WEIGHT_KG, 0f)
    }

    private fun readTargetWeightKg(): Float {
        return UserPrefs.getFloat(requireContext(), "target_weight_kg", 0f)
    }

    private fun computeBmi(weightKg: Float, heightCm: Float): Float {
        if (weightKg <= 0f || heightCm <= 0f) return 0f
        val heightM = heightCm / 100f
        return weightKg / heightM.pow(2)
    }

    private fun getBmiCategory(bmi: Float): String {
        if (bmi <= 0f) return "-"
        return when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }
    }

    private fun formatNumber(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        btnBack = null

        rowNutritionalGoalHeader = null
        rowAgeHeader = null
        rowHeightHeader = null
        rowCurrentWeightHeader = null
        rowTargetWeightHeader = null
        rowBmiHeader = null
        rowBmiCategoryHeader = null

        ivNutritionalGoalAction = null
        ivAgeAction = null
        ivHeightAction = null
        ivCurrentWeightAction = null
        ivTargetWeightAction = null
        ivBmiAction = null
        ivBmiCategoryAction = null

        layoutNutritionalGoalContent = null
        layoutAgeContent = null
        layoutHeightContent = null
        layoutCurrentWeightContent = null
        layoutTargetWeightContent = null
        layoutBmiContent = null
        layoutBmiCategoryContent = null

        tvNutritionalGoalValue = null
        tvAgeValue = null
        tvHeightValue = null
        tvCurrentWeightValue = null
        tvTargetWeightValue = null
        tvBmiValue = null
        tvBmiCategoryValue = null
    }
}