package com.example.flexifitapp.nutri

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Siguraduhing gamit ang activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.flexifitapp.R

class FoodDetailsFragment : Fragment(R.layout.fragment_food_details) {

    private lateinit var etServingWeight: EditText
    private lateinit var tvQty: TextView
    private lateinit var tvFoodName: TextView
    private lateinit var tvDescription: TextView // BAGONG WEAPON!
    private lateinit var tvServingLabel: TextView
    private lateinit var chipCalories: TextView
    private lateinit var chipProtein: TextView
    private lateinit var chipCarbs: TextView
    private lateinit var chipFats: TextView


    // Gamitin ang shared ViewModel para makuha ang data
    private val viewModel: NutritionViewModel by activityViewModels()

    private var foodId: Int = 0
    private var mealItemId: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        displayInitialData()
        setupListeners()
    }

    // para makita sila sa screen
    private fun bindViews(view: View) {
        etServingWeight = view.findViewById(R.id.etServingWeight)
        tvQty = view.findViewById(R.id.tvQty)
        tvFoodName = view.findViewById(R.id.tvFoodTitle)    // Match sa XML: tvFoodTitle
        tvDescription = view.findViewById(R.id.tvDescription) // Match sa XML: tvDescription
        tvServingLabel = view.findViewById(R.id.tvServingLabel) // Para ma-update din yung label

        chipCalories = view.findViewById(R.id.chipCalories)
        chipProtein = view.findViewById(R.id.chipProtein)
        chipCarbs = view.findViewById(R.id.chipCarbs)
        chipFats = view.findViewById(R.id.chipFats)

        // Kunin ang IDs mula sa bundle
        foodId = arguments?.getInt("foodId") ?: 0
        mealItemId = arguments?.getInt("mealItemId") ?: 0
    }

    private fun displayInitialData() {
        // Kunin ang data na ipinasa mula sa NutritionTabRootFragment
        foodId = arguments?.getInt("foodId") ?: 0
        mealItemId = arguments?.getInt("mealItemId") ?: 0

        tvFoodName.text = arguments?.getString("name")
        tvDescription.text = arguments?.getString("description") ?: "No description available"
        tvQty.text = arguments?.getInt("qty")?.toString() ?: "1"

        // Initial display ng macros
        chipCalories.text = "${arguments?.getInt("calories")} kcal"
        chipProtein.text = "${arguments?.getInt("protein")}g Prot"
        chipCarbs.text = "${arguments?.getString("carbs")}g Carbs"
        chipFats.text = "${arguments?.getString("fats")}g Fats"
    }

    private fun setupListeners() {
        etServingWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val weight = s.toString()
                if (weight.isNotEmpty()) {
                    // TATAWAG SA ENGINE: Ang ViewModel na ang bahala sa API
                    viewModel.updateMacrosFromServer(mealItemId, foodId, weight)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Observe the ViewModel for recalculated macros
        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            // Kapag nag-update ang data sa server, hanapin ang item at i-update ang chips
            val currentFood = sections.flatMap { it.foods }.find { it.mealItemId == mealItemId }
            currentFood?.let {
                chipCalories.text = "${it.calories} kcal"
                chipProtein.text = "${it.protein}g"
                chipCarbs.text = "${it.carbs}g"
                chipFats.text = "${it.fats}g"
            }
        }

        view?.findViewById<ImageButton>(R.id.btnBackFood)?.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}