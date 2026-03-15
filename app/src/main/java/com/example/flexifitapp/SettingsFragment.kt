package com.example.flexifitapp

import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var waterGoal = 8
    private var glassSizeMl: Int = 250
    private var calorieDisplayMode: String = "remaining"

    private lateinit var settingsRepository: SettingsRepository

    private var workoutTime: String? = null
    private var mealTime: String? = null
    private var waterStartTime: String? = null
    private var waterEndTime: String? = null
    private var waterInterval: Int = 60

    private var isLoadingNotificationSettings = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsRepository = SettingsRepository(SettingsApiService.create(requireContext()))

        // =========================
        // VIEW INITIALIZATION
        // =========================

        // Notifications
        val switchWorkoutReminder = view.findViewById<SwitchMaterial>(R.id.switch_workout_reminder)
        val btnWorkoutReminderTime = view.findViewById<MaterialButton>(R.id.btn_workout_reminder_time)
        val switchMealReminder = view.findViewById<SwitchMaterial>(R.id.switch_meal_reminder)
        val btnMealReminderTime = view.findViewById<MaterialButton>(R.id.btn_meal_reminder_time)
        val switchWaterReminder = view.findViewById<SwitchMaterial>(R.id.switch_water_reminder)
        val btnWaterStartTime = view.findViewById<MaterialButton>(R.id.btn_water_start_time)
        val btnWaterEndTime = view.findViewById<MaterialButton>(R.id.btn_water_end_time)
        val btnWaterInterval = view.findViewById<MaterialButton>(R.id.btn_water_interval)

        // Goals & Targets (Water Goal)
        val btnWaterGoalMinus = view.findViewById<MaterialButton>(R.id.btn_water_goal_minus)
        val btnWaterGoalPlus = view.findViewById<MaterialButton>(R.id.btn_water_goal_plus)
        val tvWaterGoalValue = view.findViewById<TextView>(R.id.tv_water_goal_value)

        // Goals & Targets (Glass Size - Drops)
        val ivDropSmall = view.findViewById<ImageView>(R.id.iv_drop_small)
        val ivDropMedium = view.findViewById<ImageView>(R.id.iv_drop_medium)
        val ivDropLarge = view.findViewById<ImageView>(R.id.iv_drop_large)
        val tvSelectedGlassLabel = view.findViewById<TextView>(R.id.tvSelectedGlassLabel)
        val tvSelectedGlassDesc = view.findViewById<TextView>(R.id.tvSelectedGlassDesc)

        // Goals & Targets (Calories)
        val toggleCalorieMode = view.findViewById<MaterialButtonToggleGroup>(R.id.toggle_calorie_mode)

        // Appearance & Others
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val btnThemeAuto = view.findViewById<MaterialButton>(R.id.btn_theme_auto)
        val btnThemeDark = view.findViewById<MaterialButton>(R.id.btn_theme_dark)
        val btnThemeLight = view.findViewById<MaterialButton>(R.id.btn_theme_light)

        // =========================
        // FETCH INITIAL DATA
        // =========================
        viewLifecycleOwner.lifecycleScope.launch {
            isLoadingNotificationSettings = true
            val result = settingsRepository.getNotificationSettings()

            result.onSuccess { dto ->
                workoutTime = dto.workoutReminderTime
                mealTime = dto.mealReminderTime
                waterStartTime = dto.waterStartTime
                waterEndTime = dto.waterEndTime
                waterInterval = dto.waterIntervalMinutes ?: 60
                waterGoal = dto.dailyWaterGoal ?: 8
                glassSizeMl = dto.glassSizeMl ?: 250
                calorieDisplayMode = dto.calorieDisplayMode ?: "remaining"

                // Update UI based on data
                switchWorkoutReminder.isChecked = dto.workoutReminderEnabled
                switchMealReminder.isChecked = dto.mealReminderEnabled
                switchWaterReminder.isChecked = dto.waterReminderEnabled

                btnWorkoutReminderTime.text = "Workout reminder time (${workoutTime ?: "Not set"})"
                btnMealReminderTime.text = "Meal reminder time (${mealTime ?: "Not set"})"
                btnWaterStartTime.text = "Start time (${waterStartTime ?: "Not set"})"
                btnWaterEndTime.text = "End time (${waterEndTime ?: "Not set"})"
                btnWaterInterval.text = "Interval (${waterInterval} mins)"

                tvWaterGoalValue.text = "$waterGoal glasses"

                if (calorieDisplayMode == "remaining") {
                    toggleCalorieMode.check(R.id.btn_cal_remaining)
                } else {
                    toggleCalorieMode.check(R.id.btn_cal_net)
                }

                // Sync the Water Drop UI
                updateGlassSizeUI(glassSizeMl, ivDropSmall, ivDropMedium, ivDropLarge, tvSelectedGlassLabel, tvSelectedGlassDesc)
            }.onFailure {
                toast("Failed to load settings")
            }
            isLoadingNotificationSettings = false
        }

        // =========================
        // STEPPER LOGIC (Water Goal)
        // =========================
        btnWaterGoalPlus.setOnClickListener {
            waterGoal++
            tvWaterGoalValue.text = "$waterGoal glasses"
            saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
        }

        btnWaterGoalMinus.setOnClickListener {
            if (waterGoal > 1) {
                waterGoal--
                tvWaterGoalValue.text = "$waterGoal glasses"
                saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
            }
        }

        // =========================
        // DROP SELECTION LOGIC (Glass Size)
        // =========================
        ivDropSmall.setOnClickListener {
            glassSizeMl = 200
            updateGlassSizeUI(200, ivDropSmall, ivDropMedium, ivDropLarge, tvSelectedGlassLabel, tvSelectedGlassDesc)
            saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
        }

        ivDropMedium.setOnClickListener {
            glassSizeMl = 250
            updateGlassSizeUI(250, ivDropSmall, ivDropMedium, ivDropLarge, tvSelectedGlassLabel, tvSelectedGlassDesc)
            saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
        }

        ivDropLarge.setOnClickListener {
            glassSizeMl = 300
            updateGlassSizeUI(300, ivDropSmall, ivDropMedium, ivDropLarge, tvSelectedGlassLabel, tvSelectedGlassDesc)
            saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
        }

        // =========================
        // CALORIE MODE LOGIC
        // =========================
        toggleCalorieMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                calorieDisplayMode = if (checkedId == R.id.btn_cal_remaining) "remaining" else "net"

                // Tawagin ang save function para ma-sync sa API
                saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)

                // Optional: Toast para sa feedback
                toast("Mode: ${calorieDisplayMode.replaceFirstChar { it.uppercase() }}")
            }
        }

        // =========================
        // NOTIFICATION SETTINGS (Switches & Pickers)
        // =========================
        val notificationSwitches = listOf(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
        notificationSwitches.forEach { switch ->
            switch.setOnCheckedChangeListener { _, _ ->
                if (!isLoadingNotificationSettings) {
                    saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
                }
            }
        }

        btnWorkoutReminderTime.setOnClickListener {
            showTimePicker { apiTime ->
                workoutTime = apiTime
                btnWorkoutReminderTime.text = "Workout reminder time ($apiTime)"
                saveNotificationSettings(switchWorkoutReminder, switchMealReminder, switchWaterReminder)
            }
        }

        // ... Meal and Water time pickers (Parehas lang ang logic) ...
        // (省略 na para hindi masyadong mahaba, pero use the same pattern as Workout time picker)

        // =========================
        // THEME LOGIC
        // =========================
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }


// ... (Theme Logic sa dulo ng onViewCreated)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // =========================
        // ACCOUNT & PRIVACY ACTIONS (DITO MO ILAGAY BABE)
        // =========================

        // 1. Export Data Button
        view.findViewById<MaterialButton>(R.id.btn_export_data).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                settingsRepository.exportData().onSuccess { responseBody ->
                    toast("Data exported successfully!")
                }.onFailure { toast("Export failed") }
            }
        }

        // 2. Reset Progress Button (Danger Zone)
        view.findViewById<MaterialButton>(R.id.btn_reset_progress_logs).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset All Progress?")
                .setMessage("This will permanently delete your workout logs, water history, and achievements.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset Everything") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        settingsRepository.resetProgress().onSuccess {
                            toast("Progress has been cleared.")
                        }.onFailure { toast("Reset failed") }
                    }
                }.show()
        }

        // 3. Delete Account Button
        view.findViewById<MaterialButton>(R.id.btn_delete_account).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account?")
                .setMessage("Are you sure? This action is permanent and cannot be undone.")
                .setNegativeButton("No", null)
                .setPositiveButton("Delete Permanently") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        settingsRepository.deleteAccount().onSuccess {
                            toast("Account deleted. Goodbye!")
                            // Dito mo ilalagay yung intent pabalik sa Login Activity
                        }
                    }
                }.show()
        }
    } // <--- DITO NAGTATAPOS ANG onViewCreated

    // =========================
    // HELPER FUNCTIONS
    // =========================

    private fun updateGlassSizeUI(
        size: Int,
        ivSmall: ImageView, ivMed: ImageView, ivLarge: ImageView,
        tvLabel: TextView, tvDesc: TextView
    ) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)

        // Update Tint colors
        ivSmall.imageTintList = ColorStateList.valueOf(if (size == 200) selectedColor else unselectedColor)
        ivMed.imageTintList = ColorStateList.valueOf(if (size == 250) selectedColor else unselectedColor)
        ivLarge.imageTintList = ColorStateList.valueOf(if (size == 300) selectedColor else unselectedColor)

        // Update Labels based on selection
        when (size) {
            200 -> {
                tvLabel.text = "Small Glass"
                tvDesc.text = "Using 200ml per glass"
            }
            250 -> {
                tvLabel.text = "Standard Glass"
                tvDesc.text = "Using 250ml per glass"
            }
            300 -> {
                tvLabel.text = "Large Glass"
                tvDesc.text = "Using 300ml per glass"
            }
        }
    }

    private fun saveNotificationSettings(
        switchWorkoutReminder: SwitchMaterial,
        switchMealReminder: SwitchMaterial,
        switchWaterReminder: SwitchMaterial
    ) {
        val dto = NotificationSettingsDto(
            workoutReminderEnabled = switchWorkoutReminder.isChecked,
            workoutReminderTime = workoutTime,
            mealReminderEnabled = switchMealReminder.isChecked,
            mealReminderTime = mealTime,
            waterReminderEnabled = switchWaterReminder.isChecked,
            waterStartTime = waterStartTime,
            waterEndTime = waterEndTime,
            waterIntervalMinutes = waterInterval,
            dailyWaterGoal = waterGoal,
            glassSizeMl = glassSizeMl,
            calorieDisplayMode = calorieDisplayMode
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val result = settingsRepository.updateNotificationSettings(dto)
            if (result.isFailure) toast("Failed to save settings")
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, m ->
            onTimeSelected(String.format("%02d:%02d", h, m))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun toast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}