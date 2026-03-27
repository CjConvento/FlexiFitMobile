package com.example.flexifitapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.flexifitapp.custom.WaterGlassView
import com.example.flexifitapp.dashboard.BmiDetailsDialog
import com.example.flexifitapp.dashboard.ProfileStatusResponse // Gamitin yung bagong model babe
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // ===== Views (Null-safe handling) =====
    private var txtLevel: TextView? = null
    private var imgLevelIcon: ImageView? = null
    private var txtCalorieIntake: TextView? = null
    private var txtCaloriesBurned: TextView? = null
    private var tvNetCalories: TextView? = null
    private var tvLeftCalories: TextView? = null
    private var progressRing: com.google.android.material.progressindicator.CircularProgressIndicator? = null
    private var progressIntake: com.google.android.material.progressindicator.LinearProgressIndicator? = null
    private var progressBurned: com.google.android.material.progressindicator.LinearProgressIndicator? = null
    private var tvBMIStatus: TextView? = null
    private var txtBMIScore: TextView? = null
    private var btnBMIViewMore: com.google.android.material.button.MaterialButton? = null

    // Header Views
    private var txtHeaderName: TextView? = null
    private var txtHeaderEmail: TextView? = null

    // Workouts Section
    private var txtWorkoutName1: TextView? = null
    private var txtWorkoutl1: TextView? = null
    private var imgWorkout1: ImageView? = null
    private var txtWorkoutName2: TextView? = null
    private var txtWorkoutl2: TextView? = null
    private var imgWorkout2: ImageView? = null

    // Meals Section (Eto na yung container at modern dropdown babe)
    private var mealItemsContainer: android.widget.LinearLayout? = null
    private var autoCompleteMealType: AutoCompleteTextView? = null // Pinalitan ang Spinner
    private var txtTodayMealsHeader: TextView? = null

    // Water & Controls
    private var btnUpdateWater: TextView? = null
    private var txtWaterCount: TextView? = null
    private var waterGlass: com.example.flexifitapp.custom.WaterGlassView? = null

    // Data Storage
    private var globalProfileData: ProfileStatusResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Bind all views first
        bindViews(view)

        // 2. Setup Modern Meal Dropdown (AutoCompleteTextView)
        setupMealDropdown()

        // 3. Setup Header Click (Navigation to Nutrition Tab)
        txtTodayMealsHeader?.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_nutritionTabRoot)
        }

        // 4. Fetch initial data from API
        fetchDashboardData()
    }

    private fun setupMealDropdown() {
        val mealCategories = arrayOf("Breakfast", "Lunch", "Snacks", "Dinner")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mealCategories)

        autoCompleteMealType?.apply {
            setAdapter(adapter)
            // Default text para hindi empty sa start
            setText(mealCategories[0], false)

            // Listener para sa pagpili (Modern way)
            setOnItemClickListener { _, _, position, _ ->
                val mealType = when (position) {
                    0 -> "B"
                    1 -> "L"
                    2 -> "S"
                    3 -> "D"
                    else -> "B"
                }
                updateMealList(mealType)
            }
        }
    }

    private fun bindViews(view: View) {
        // 1. Containers & Header
        val headerContainer = view.findViewById<View>(R.id.dashboardHeader)
        txtHeaderName = headerContainer?.findViewById(R.id.txtHeaderName)
        txtHeaderEmail = headerContainer?.findViewById(R.id.txtHeaderEmail)

        // 2. Fitness Level & BMI
        txtLevel = view.findViewById(R.id.txtLevel)
        imgLevelIcon = view.findViewById(R.id.imgLevelIcon)
        txtBMIScore = view.findViewById(R.id.txtBMIScore)
        tvBMIStatus = view.findViewById(R.id.tvBMIStatus)
        btnBMIViewMore = view.findViewById(R.id.btnBMIViewMore)

        // 3. Calories Section
        txtCalorieIntake = view.findViewById(R.id.txtCalorieIntake)
        txtCaloriesBurned = view.findViewById(R.id.txtCaloriesBurned)
        progressIntake = view.findViewById(R.id.progressIntake)
        progressBurned = view.findViewById(R.id.progressBurned)
        tvNetCalories = view.findViewById(R.id.tvNetCalories)
        tvLeftCalories = view.findViewById(R.id.tvLeftCalories)
        progressRing = view.findViewById(R.id.progressRing)

        // 4. Workouts Section
        txtWorkoutName1 = view.findViewById(R.id.txtWorkoutName1)
        txtWorkoutl1 = view.findViewById(R.id.txtWorkoutl1)
        imgWorkout1 = view.findViewById(R.id.imgWorkout1)
        txtWorkoutName2 = view.findViewById(R.id.txtWorkoutName2)
        txtWorkoutl2 = view.findViewById(R.id.txtWorkoutl2)
        imgWorkout2 = view.findViewById(R.id.imgWorkout2)

        // 5. MEALS SECTION (🔥 ETO YUNG BINAGO NATIN BABE)
        mealItemsContainer = view.findViewById(R.id.mealItemsContainer)
        autoCompleteMealType = view.findViewById(R.id.autoCompleteMealType) // Match sa modern XML ID
        txtTodayMealsHeader = view.findViewById(R.id.txtTodayMealsHeader)

        // 6. Water Section
        txtWaterCount = view.findViewById(R.id.txtWaterCount)
        waterGlass = view.findViewById(R.id.waterGlass)
        btnUpdateWater = view.findViewById(R.id.btnUpdateWater)

        // Click Listeners sa loob ng binding (Para isang bagsakan lang)
        btnUpdateWater?.setOnClickListener {
            // Lilipat sa Nutrition tab shortcut
            (requireActivity() as? MainActivity)?.navigateToNutritionTab()
        }
    }

    private fun fetchDashboardData() {
        Log.d("Dashboard", "Token before API call: ${UserPrefs.getToken(requireContext()).take(20)}...")
        lifecycleScope.launch {
            try {
                val response = ApiClient.api().getDashboardData()
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Log.d("DEBUG_JSON", "Raw Response: $data")

                        globalProfileData = data
                        updateUI(data)
                    } else {
                        Log.e("DEBUG_JSON", "Babe, empty yung body (null) kahit successful.")
                    }
                } else {
                    Toast.makeText(context, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Importante itong catch babe para pag walang internet or timeout
                Log.e("DASHBOARD_ERROR", "Crash babe: ${e.message}")
                Toast.makeText(context, "Network error. Check connection!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(data: ProfileStatusResponse) {

        // 1. Header (Gagamitin natin yung txtHeaderName na na-bind na natin)
        txtHeaderName?.text = "Welcome, ${data.username ?: "User"}!"
        txtHeaderEmail?.text = data.userEmail ?: ""

        // 2. Fitness Level
        txtLevel?.text = data.fitnessLevel ?: "Beginner"
        updateLevelIcon(data.fitnessLevel ?: "Beginner")

        // 3. BMI Card
        val bmiValue = data.bmiData?.value ?: 0.0
        txtBMIScore?.text = String.format("%.1f", bmiValue)
        tvBMIStatus?.text = data.bmiData?.status ?: "No data"

        // Optional: Magpalit ng kulay base sa BMI (Aesthetic vibes!)
        if (bmiValue in 18.5..24.9) {
            txtBMIScore?.setTextColor(Color.parseColor("#C58BF2")) // Purple variant
        }

        btnBMIViewMore?.setOnClickListener {
            val dialog = BmiDetailsDialog(bmiValue, data.bmiData?.status ?: "Normal")
            dialog.show(parentFragmentManager, "bmi_details")
        }

        // 4. Nutrition Section
        val nutri = data.nutrition ?: return
        val target = if (nutri.targetCalories > 0) nutri.targetCalories else 2000

        // Linear Bars
        progressIntake?.max = target
        progressIntake?.setProgress(nutri.intake, true)
        txtCalorieIntake?.text = "${nutri.intake} / $target kcal"

        progressBurned?.max = 1000 // Sample max burned target
        progressBurned?.setProgress(nutri.burned.toInt(), true)
        txtCaloriesBurned?.text = "${nutri.burned.toInt()} kcal"

        // Circular Ring (Net Calories)
        tvNetCalories?.text = "${nutri.netCalories} kcal"
        tvLeftCalories?.text = "${nutri.remaining} kcal\nleft"

        progressRing?.max = target
        progressRing?.setProgress(nutri.netCalories, true)

        // 5. Workout Section (Handling 2 Thumbnails)
        val workouts = data.upcomingWorkouts

        if (!workouts.isNullOrEmpty()) {
            // --- FIRST THUMBNAIL (Workout 1) ---
            val firstWorkout = workouts[0]
            txtWorkoutName1?.text = firstWorkout.name
            txtWorkoutl1?.text = "${firstWorkout.sets} Sets x ${firstWorkout.reps} Reps"

            imgWorkout1?.let { imageView ->
                Glide.with(this)
                    .load(firstWorkout.imageFileName) // URL ng unang thumbnail
                    .placeholder(R.drawable.ic_workout)
                    .error(R.drawable.ic_workout)
                    .into(imageView)
            }

            // --- SECOND THUMBNAIL (Workout 2) ---
            // Check muna natin kung may pangalawang workout sa listahan para hindi mag-crash
            if (workouts.size > 1) {
                val secondWorkout = workouts[1]
                txtWorkoutName2?.text = secondWorkout.name
                txtWorkoutl2?.text = "${secondWorkout.sets} Sets x ${secondWorkout.reps} Reps"

                imgWorkout2?.let { imageView ->
                    Glide.with(this)
                        .load(secondWorkout.imageFileName) // URL ng pangalawang thumbnail
                        .placeholder(R.drawable.ic_workout)
                        .error(R.drawable.ic_workout)
                        .into(imageView)
                }
            } else {
                // Kung isa lang ang workout, pwede mong i-clear o i-hide yung pangalawang card
                Log.d("DEBUG_DASHBOARD", "Babe, isa lang ang workout sa listahan.")
            }
        }



        // 6. Today Meals Section (MODERN & MAGIC!)
        // Imbes na position, titingnan natin kung ano ang kasalukuyang nakasulat sa AutoComplete
        val selectedText = autoCompleteMealType?.text.toString()
        val initialMealType = when {
            selectedText.contains("Lunch", ignoreCase = true) -> "L"
            selectedText.contains("Snack", ignoreCase = true) -> "S"
            selectedText.contains("Dinner", ignoreCase = true) -> "D"
            else -> "B" // Default is Breakfast
        }

        updateMealList(initialMealType)

        // 7. Water
        txtWaterCount?.text = "${nutri.waterGlasses}/${nutri.waterTarget}"
        waterGlass?.setCurrentGlasses(nutri.waterGlasses)
    }

    private fun updateLevelIcon(level: String) {
        val color = when (level.lowercase()) {
            "beginner" -> "#4CAF50"
            "intermediate" -> "#FF9800"
            "advanced" -> "#F44336"
            else -> "#2196F3"
        }
        imgLevelIcon?.setColorFilter(Color.parseColor(color))
    }

    private fun updateMealList(mealType: String) {
        Log.d("DEBUG_MEALS", "Searching for Group Type: $mealType")

        // Siguraduhin na hindi null ang container bago linisin
        mealItemsContainer?.removeAllViews()

        // 1. Hanapin ang tamang GROUP base sa mealType (B, L, S, D)
        val selectedGroup = globalProfileData?.todayMeals?.find {
            it.mealType?.equals(mealType, ignoreCase = true) == true
        }

        // 2. Kunin ang food items. Gamit tayo ng .take(2) kung gusto mo talagang dashboard style (top 2 only)
        // Pero kung gusto mo lahat, alisin mo lang yung .take(2)
        val mealsToShow = selectedGroup?.foodItems // O kaya selectedGroup?.foodItems?.take(2)

        if (mealsToShow.isNullOrEmpty()) {
            // Pwede kang mag-inflate ng "No meals planned" layout dito babe para hindi lang blank
            Log.w("DEBUG_MEALS", "Walang laman ang $mealType babe.")
            return
        }

        // 3. Loop through food items
        mealsToShow.forEach { meal ->
            val mealView = layoutInflater.inflate(R.layout.item_meal_food, mealItemsContainer, false)

            // I-bind ang data sa views ng item_meal_food.xml
            mealView.findViewById<TextView>(R.id.tvFoodName).text = meal.name ?: "Unknown Food"
            mealView.findViewById<TextView>(R.id.tvFoodSub).text = "${meal.calories.toInt()} kcal • ${meal.qty} ${meal.unit}"

            val foodImg = mealView.findViewById<ImageView>(R.id.imgFood)
            Glide.with(this)
                .load(meal.imageUrl)
                .placeholder(R.drawable.ic_nutri)
                .error(R.drawable.ic_nutri) // Safety para sa broken links
                .into(foodImg)

            // Gawing clickable ang bawat row para lumipat sa Nutrition Tab Root
            mealView.setOnClickListener {
                (requireActivity() as? MainActivity)?.navigateToNutritionTab()
            }

            mealItemsContainer?.addView(mealView)
        }
    }

    override fun onDestroyView() {
        // Cleanup to prevent memory leaks
        txtLevel = null; imgLevelIcon = null; txtCalorieIntake = null
        txtCaloriesBurned = null; tvNetCalories = null; tvLeftCalories = null
        progressRing = null; progressIntake = null; progressBurned = null
        txtBMIScore = null; tvBMIStatus = null; btnBMIViewMore = null
        txtWaterCount = null; btnUpdateWater = null; waterGlass = null
        super.onDestroyView()
    }
}