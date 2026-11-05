package com.example.flexifitapp.nutri

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView  // ✅ ADD THIS IMPORT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.R
import com.example.flexifitapp.utils.AppLogger
import kotlinx.coroutines.launch
import com.example.flexifitapp.utils.ImageHelper

class FoodDetailsFragment : Fragment(R.layout.fragment_food_details) {

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var imgHero: ImageView
    private lateinit var tvFoodTitle: TextView
    private lateinit var tvServingLabel: TextView
    private lateinit var etServingWeight: EditText
    private lateinit var btnMinus: ImageButton
    private lateinit var btnPlus: ImageButton
    private lateinit var tvQty: TextView
    private lateinit var tvDescription: TextView
    private lateinit var chipCalories: TextView
    private lateinit var chipProtein: TextView
    private lateinit var chipCarbs: TextView
    private lateinit var chipFats: TextView

    // Data
    private var mealItemId: Int = 0
    private var foodId: Int = 0
    private var currentQty: Double = 1.0
    private var currentWeight: Double = 0.0
    private var baseWeight: Double = 100.0  // Default serving weight in grams
    private var baseCalories: Double = 0.0
    private var baseProtein: Double = 0.0
    private var baseCarbs: Double = 0.0
    private var baseFats: Double = 0.0
    private var unit: String = "g"

    // Para ma-track kung nagbago ang user ng food details
    private var hasChanges = false 

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadArguments()
        setupClickListeners()
        setupWeightListener()
        loadFoodImage()
    }

    private fun initViews(view: View) {
        // ✅ Fixed: Use findViewById (not findViewByIdById)
        btnBack = view.findViewById(R.id.btnBackFood)
        imgHero = view.findViewById(R.id.imgHero)
        tvFoodTitle = view.findViewById(R.id.tvFoodTitle)
        tvServingLabel = view.findViewById(R.id.tvServingLabel)
        etServingWeight = view.findViewById(R.id.etServingWeight)
        btnMinus = view.findViewById(R.id.btnMinus)
        btnPlus = view.findViewById(R.id.btnPlus)
        tvQty = view.findViewById(R.id.tvQty)
        tvDescription = view.findViewById(R.id.tvDescription)
        chipCalories = view.findViewById(R.id.chipCalories)
        chipProtein = view.findViewById(R.id.chipProtein)
        chipCarbs = view.findViewById(R.id.chipCarbs)
        chipFats = view.findViewById(R.id.chipFats)
    }

    private fun loadArguments() {
        arguments?.let {
            mealItemId = it.getInt("mealItemId", 0)
            foodId = it.getInt("foodId", 0)
            currentQty = it.getDouble("qty", 1.0)
            tvFoodTitle.text = it.getString("name") ?: "Food"
            tvDescription.text = it.getString("description") ?: "No description available"

            // Get Total Value From API
            val totalCalories = it.getDouble("calories", 0.0)
            val totalProtein = it.getDouble("protein", 0.0)
            val totalCarbs = it.getDouble("carbs", 0.0)
            val totalFats = it.getDouble("fats", 0.0)

            // NORMALIZE TO PER-SERVING
            // Dahil ang API ay nagse-send ng TOTALS, kailangang i-divide by currentQty
            baseCalories = if (currentQty > 0) totalCalories / currentQty else totalCalories
            baseProtein = if (currentQty > 0) totalProtein / currentQty else totalProtein
            baseCarbs = if (currentQty > 0) totalCarbs / currentQty else totalCarbs
            baseFats = if (currentQty > 0) totalFats / currentQty else totalFats

            // Get serving label
            val servingLabel = it.getString("servingLabel") ?: "1 serving"
            tvServingLabel.text = servingLabel

            // Parse weight if available (e.g., "150g" from serving label)
            parseWeightFromServingLabel(servingLabel)

            // Set current values
            tvQty.text = String.format("%.2f", currentQty) 
            updateMacros()
        }
    }

    private fun parseWeightFromServingLabel(servingLabel: String) {
        // Try to extract weight like "150g" or "100 g"
        val weightRegex = "(\\d+)\\s*g".toRegex()
        weightRegex.find(servingLabel)?.let {
            baseWeight = it.groupValues[1].toDouble()
            etServingWeight.setText(baseWeight.toInt().toString())
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            if (hasChanges) {
                saveAndGoBack()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        btnMinus.setOnClickListener {
            if (currentQty >= 0.25) {
                currentQty -= 0.25 // Decrement by 0.25 
                tvQty.text = String.format("%.2f", currentQty) 

                // Sync: Update weight para magmatch sa new quantity
                currentWeight = currentQty * baseWeight
                etServingWeight.setText(currentWeight.toInt().toString())

                updateMacros()
                hasChanges = true // added to track changes
            }
        }

        btnPlus.setOnClickListener {
            currentQty += 0.25
            tvQty.text = String.format("%.2f", currentQty)

            // Sync: Update weight para magmatch sa new quantity
            currentWeight = currentQty * baseWeight
            etServingWeight.setText(currentWeight.toInt().toString())

            updateMacros()
            hasChanges = true // added to track changes
        }
    }

    private fun setupWeightListener() {
        etServingWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val weightStr = s.toString()
                if (weightStr.isNotEmpty()) {
                    try {
                        currentWeight = weightStr.toDouble()

                        // SYNC: I-update ang quantity para match ang new weight
                        if (baseWeight > 0) {
                            currentQty = currentWeight / baseWeight
                            tvQty.text = String.format("%.2f", currentQty)
                        }

                        updateMacrosFromWeight()
                        hasChanges = true
                    } catch (e: NumberFormatException) {
                        // Ignore invalid input
                    }
                }
            }
        })
    }

    private fun updateMacros() {
        // Update macros based on quantity
        val multiplier = currentQty
        updateChips(
            calories = baseCalories * multiplier,
            protein = baseProtein * multiplier,
            carbs = baseCarbs * multiplier,
            fats = baseFats * multiplier
        )
    }

    // Dedicated save function na safe sa lifecycle
    private fun saveAndGoBack() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)

                // SIMPLIFIED: Dahil synced na ang Qty at Weight, gamitin na lang ang currentQty
                val newQty = currentQty

                AppLogger.d("FOOD_DETAILS", "Saving: mealItemId=$mealItemId, newQty=$newQty")
                val success = repository.updateMealItem(mealItemId, newQty)

                if (success) {
                    Toast.makeText(requireContext(), "Quantity updated", Toast.LENGTH_SHORT).show()

                    // 1. I-calculate ang bagong macros base sa newQty
                    val newCalories = (baseCalories * newQty).toInt()
                    val newProtein = (baseProtein * newQty).toInt()
                    val newCarbs = (baseCarbs * newQty).toInt()
                    val newFats = (baseFats * newQty).toInt()
                    val newServingLabel = "$newQty $unit"

                    // 2. I-pack ang updated data sa Bundle
                    val result = bundleOf(
                        "mealItemId" to mealItemId,
                        "newQty" to newQty,
                        "newCalories" to newCalories,
                        "newProtein" to newProtein,
                        "newCarbs" to newCarbs,
                        "newFats" to newFats,
                        "newServingLabel" to newServingLabel
                    )

                    // 3. I-send ang result sa parent fragment bago mag-pop
                    parentFragmentManager.setFragmentResult("food_item_updated", result)
                    parentFragmentManager.popBackStack()
                } else {
                    // Mas detailed na error message para alam na may API issue
                    Toast.makeText(requireContext(), "Failed to update. Check Logcat for details.", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack() // pop anyway para hindi mastuck
                }
            } catch (e: Exception) {
                AppLogger.e("FOOD_DETAILS", "Save exception: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun updateMacrosFromWeight() {
        if (baseWeight <= 0) return

        val weightRatio = currentWeight / baseWeight
        updateChips(
            calories = baseCalories * weightRatio,
            protein = baseProtein * weightRatio,
            carbs = baseCarbs * weightRatio,
            fats = baseFats * weightRatio
        )
    }

    private fun updateChips(calories: Double, protein: Double, carbs: Double, fats: Double) {
        chipCalories.text = "${calories.toInt()} kcal"
        chipProtein.text = "${protein.toInt()}g protein"
        chipCarbs.text = "${carbs.toInt()}g carbs"
        chipFats.text = "${fats.toInt()}g fats"
    }

    private fun loadFoodImage() {
        arguments?.getString("imageUrl")?.let { imageUrl ->
            Glide.with(this)
                .load(ImageHelper.getImageUrl(imageUrl, "foods"))                   
                .placeholder(R.drawable.ic_food_placeholder)
                .error(R.drawable.ic_food_placeholder)
                .into(imgHero)
        }
    }

    private fun updateQuantityInDatabase() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val repository = NutritionRepository(api)

                // Calculate new quantity based on weight or servings
                val newQty = if (currentWeight > 0 && baseWeight > 0) {
                    (currentQty * currentWeight / baseWeight).toDouble()
                } else {
                    currentQty.toDouble()
                }

                val success = repository.updateMealItem(mealItemId, newQty)

                if (success) {
                    Toast.makeText(requireContext(), "Quantity updated", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}