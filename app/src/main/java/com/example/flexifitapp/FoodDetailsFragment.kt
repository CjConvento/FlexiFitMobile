package com.example.flexifitapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController

class FoodDetailsFragment : Fragment(R.layout.fragment_food_details) {

    companion object {
        private const val REQ_FOOD_EDIT = "req_food_edit"
        private const val KEY_MEAL_ITEM_ID = "mealItemId"
        private const val KEY_QTY = "qty"
        private const val KEY_SERVING_LABEL = "servingLabel"
        private const val KEY_CALORIES = "calories"
        private const val KEY_PROTEIN = "protein"
        private const val KEY_CARBS = "carbs"
        private const val KEY_FATS = "fats"
    }

    private var mealItemId: Int = 0
    private var foodId: Int = 0
    private var mealType: String = ""

    private var qty: Int = 1
    private var servingLabel: String = "1 serving"

    // base values per 1 qty
    private var baseCalories: Int = 0
    private var baseProtein: Int = 0
    private var baseCarbs: Int = 0
    private var baseFats: Int = 0

    private lateinit var btnBackFood: ImageButton
    private lateinit var btnMinus: ImageButton
    private lateinit var btnPlus: ImageButton

    private lateinit var tvFoodTitle: TextView
    private lateinit var tvFoodAuthor: TextView
    private lateinit var tvServingLabel: TextView
    private lateinit var tvQty: TextView
    private lateinit var tvDescription: TextView

    private lateinit var chipCalories: TextView

    private lateinit var chipCarbs: TextView
    private lateinit var chipProtein: TextView
    private lateinit var chipFats: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        readArgs()
        setupStaticUi()
        setupListeners()
        refreshUi()
    }

    private fun bindViews(view: View) {
        btnBackFood = view.findViewById(R.id.btnBackFood)
        btnMinus = view.findViewById(R.id.btnMinus)
        btnPlus = view.findViewById(R.id.btnPlus)

        tvFoodTitle = view.findViewById(R.id.tvFoodTitle)
        tvFoodAuthor = view.findViewById(R.id.tvFoodAuthor)
        tvServingLabel = view.findViewById(R.id.tvServingLabel)
        tvQty = view.findViewById(R.id.tvQty)
        tvDescription = view.findViewById(R.id.tvDescription)

        chipCalories = view.findViewById(R.id.chipCalories)
        chipProtein = view.findViewById(R.id.chipProtein)
        chipCarbs = view.findViewById(R.id.chipCarbs)
        chipFats = view.findViewById(R.id.chipFats)

    }

    private fun readArgs() {
        arguments?.let { args ->
            mealItemId = args.getInt(KEY_MEAL_ITEM_ID, 0)
            foodId = args.getInt("foodId", 0)
            mealType = args.getString("mealType").orEmpty()

            qty = args.getInt(KEY_QTY, 1)
            servingLabel = args.getString(KEY_SERVING_LABEL).orEmpty().ifBlank { "1 serving" }

            baseCalories = args.getInt(KEY_CALORIES, 0)
            baseProtein = args.getInt(KEY_PROTEIN, 0)
            baseCarbs = args.getInt(KEY_CARBS, 0)
            baseFats = args.getInt(KEY_FATS, 0)
        }
    }

    private fun setupStaticUi() {
        // Temporary values muna habang wala pang API
        // Later papalitan natin ito ng real API data based on foodId
        when (foodId) {
            1 -> {
                tvFoodTitle.text = "Oatmeal Pancakes"
                tvFoodAuthor.text = "Breakfast Item"
                tvDescription.text = "A healthy oatmeal-based pancake meal that can support energy and satiety for breakfast."
            }
            2 -> {
                tvFoodTitle.text = "Chicken Rice"
                tvFoodAuthor.text = "Lunch Item"
                tvDescription.text = "A balanced chicken and rice meal that provides protein and carbohydrates for daily nutrition goals."
            }
            else -> {
                tvFoodTitle.text = "Food Item"
                tvFoodAuthor.text = mealType.ifBlank { "Meal Item" }
                tvDescription.text = "Food description will appear here."
            }
        }
    }

    private fun setupListeners() {
        btnMinus.setOnClickListener {
            if (qty > 1) {
                qty--
                refreshUi()
            }
        }

        btnPlus.setOnClickListener {
            qty++
            refreshUi()
        }

        btnBackFood.setOnClickListener {
            sendResultAndGoBack()
        }
    }

    private fun refreshUi() {
        val totalCalories = baseCalories * qty
        val totalProtein = baseProtein * qty
        val totalCarbs = baseCarbs * qty
        val totalFats = baseFats * qty

        tvServingLabel.text = servingLabel
        tvQty.text = qty.toString()

        chipCalories.text = "${totalCalories} kcal"
        chipProtein.text = "${totalProtein}g protein"
        chipCarbs.text = "${totalCarbs}g carbs"
        chipFats.text = "${totalFats}g fats"

        // Optional:
        // if you add chipCarbs in XML later, set it here too
    }

    private fun sendResultAndGoBack() {
        setFragmentResult(
            REQ_FOOD_EDIT,
            bundleOf(
                KEY_MEAL_ITEM_ID to mealItemId,
                KEY_QTY to qty,
                KEY_SERVING_LABEL to servingLabel,
                KEY_CALORIES to baseCalories,
                KEY_PROTEIN to baseProtein,
                KEY_CARBS to baseCarbs,
                KEY_FATS to baseFats
            )
        )

        findNavController().popBackStack()
    }
}