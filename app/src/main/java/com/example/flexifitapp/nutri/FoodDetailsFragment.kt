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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.flexifitapp.ApiClient
import com.example.flexifitapp.R
import kotlinx.coroutines.launch

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
    private var currentQty: Int = 1
    private var currentWeight: Double = 0.0
    private var baseWeight: Double = 100.0  // Default serving weight in grams
    private var baseCalories: Double = 0.0
    private var baseProtein: Double = 0.0
    private var baseCarbs: Double = 0.0
    private var baseFats: Double = 0.0
    private var unit: String = "g"

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
            currentQty = it.getInt("qty", 1)
            tvFoodTitle.text = it.getString("name") ?: "Food"
            tvDescription.text = it.getString("description") ?: "No description available"

            // Base values for 1 serving
            baseCalories = it.getDouble("calories", 0.0)
            baseProtein = it.getDouble("protein", 0.0)
            baseCarbs = it.getDouble("carbs", 0.0)
            baseFats = it.getDouble("fats", 0.0)

            // Get serving label
            val servingLabel = it.getString("servingLabel") ?: "1 serving"
            tvServingLabel.text = servingLabel

            // Parse weight if available (e.g., "150g" from serving label)
            parseWeightFromServingLabel(servingLabel)

            // Set current values
            tvQty.text = currentQty.toString()
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
            parentFragmentManager.popBackStack()
        }

        btnMinus.setOnClickListener {
            if (currentQty > 1) {
                currentQty--
                tvQty.text = currentQty.toString()
                updateMacros()
            }
        }

        btnPlus.setOnClickListener {
            if (currentQty < 10) {
                currentQty++
                tvQty.text = currentQty.toString()
                updateMacros()
            }
        }
    }

    private fun setupWeightListener() {
        etServingWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val weightStr = s.toString()
                if (weightStr.isNotEmpty()) {
                    currentWeight = weightStr.toDouble()
                    updateMacrosFromWeight()
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
                .load(imageUrl)
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

    override fun onDestroyView() {
        // Save changes when fragment is destroyed
        updateQuantityInDatabase()
        super.onDestroyView()
    }
}