package com.example.flexifitapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
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

        settingsRepository = SettingsRepository(SettingsApi.create(requireContext()))

        // =========================
        // NOTIFICATIONS
        // =========================
        val switchWorkoutReminder = view.findViewById<SwitchMaterial>(R.id.switch_workout_reminder)
        val btnWorkoutReminderTime = view.findViewById<MaterialButton>(R.id.btn_workout_reminder_time)

        val switchMealReminder = view.findViewById<SwitchMaterial>(R.id.switch_meal_reminder)
        val btnMealReminderTime = view.findViewById<MaterialButton>(R.id.btn_meal_reminder_time)

        val switchWaterReminder = view.findViewById<SwitchMaterial>(R.id.switch_water_reminder)
        val btnWaterStartTime = view.findViewById<MaterialButton>(R.id.btn_water_start_time)
        val btnWaterEndTime = view.findViewById<MaterialButton>(R.id.btn_water_end_time)
        val btnWaterInterval = view.findViewById<MaterialButton>(R.id.btn_water_interval)

        // =========================
        // GOALS & TARGETS
        // =========================
        val btnWaterGoalMinus = view.findViewById<MaterialButton>(R.id.btn_water_goal_minus)
        val btnWaterGoalPlus = view.findViewById<MaterialButton>(R.id.btn_water_goal_plus)
        val tvWaterGoalValue = view.findViewById<TextView>(R.id.tv_water_goal_value)
        val btnGlassSize = view.findViewById<MaterialButton>(R.id.btn_glass_size)

        val rbCalRemaining = view.findViewById<RadioButton>(R.id.rb_cal_remaining)
        val rbCalNet = view.findViewById<RadioButton>(R.id.rb_cal_net)

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

                switchWorkoutReminder.isChecked = dto.workoutReminderEnabled
                switchMealReminder.isChecked = dto.mealReminderEnabled
                switchWaterReminder.isChecked = dto.waterReminderEnabled

                btnWorkoutReminderTime.text = "Workout reminder time (${workoutTime ?: "Not set"})"
                btnMealReminderTime.text = "Meal reminder time (${mealTime ?: "Not set"})"
                btnWaterStartTime.text = "Start time (${waterStartTime ?: "Not set"})"
                btnWaterEndTime.text = "End time (${waterEndTime ?: "Not set"})"
                btnWaterInterval.text = "Interval (${waterInterval} mins)"

                tvWaterGoalValue.text = "$waterGoal glasses"
                btnGlassSize.text = "Glass size (${glassSizeMl}ml)"
                rbCalRemaining.isChecked = calorieDisplayMode == "remaining"
                rbCalNet.isChecked = calorieDisplayMode == "net"
            }.onFailure {
                toast("Failed to load notification settings")
            }

            isLoadingNotificationSettings = false
        }

        switchWorkoutReminder.setOnCheckedChangeListener { _, _ ->
            if (isLoadingNotificationSettings) return@setOnCheckedChangeListener
            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        switchMealReminder.setOnCheckedChangeListener { _, _ ->
            if (isLoadingNotificationSettings) return@setOnCheckedChangeListener
            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        switchWaterReminder.setOnCheckedChangeListener { _, _ ->
            if (isLoadingNotificationSettings) return@setOnCheckedChangeListener
            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        btnWorkoutReminderTime.setOnClickListener {
            showTimePicker { apiTime ->
                workoutTime = apiTime
                btnWorkoutReminderTime.text = "Workout reminder time ($apiTime)"
                saveNotificationSettings(
                    switchWorkoutReminder,
                    switchMealReminder,
                    switchWaterReminder
                )
            }
        }

        btnMealReminderTime.setOnClickListener {
            showTimePicker { apiTime ->
                mealTime = apiTime
                btnMealReminderTime.text = "Meal reminder time ($apiTime)"
                saveNotificationSettings(
                    switchWorkoutReminder,
                    switchMealReminder,
                    switchWaterReminder
                )
            }
        }

        btnWaterStartTime.setOnClickListener {
            showTimePicker { apiTime ->
                waterStartTime = apiTime
                btnWaterStartTime.text = "Start time ($apiTime)"
                saveNotificationSettings(
                    switchWorkoutReminder,
                    switchMealReminder,
                    switchWaterReminder
                )
            }
        }

        btnWaterEndTime.setOnClickListener {
            showTimePicker { apiTime ->
                waterEndTime = apiTime
                btnWaterEndTime.text = "End time ($apiTime)"
                saveNotificationSettings(
                    switchWorkoutReminder,
                    switchMealReminder,
                    switchWaterReminder
                )
            }
        }

        btnWaterInterval.setOnClickListener {
            val items = arrayOf(60, 90, 120)
            val labels = arrayOf("60 mins", "90 mins", "120 mins")
            val checked = items.indexOf(waterInterval).let { if (it == -1) 0 else it }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Water Interval")
                .setSingleChoiceItems(labels, checked) { dialog, which ->
                    waterInterval = items[which]
                    btnWaterInterval.text = "Interval (${labels[which]})"
                    dialog.dismiss()

                    saveNotificationSettings(
                        switchWorkoutReminder,
                        switchMealReminder,
                        switchWaterReminder
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        tvWaterGoalValue.text = "$waterGoal glasses"
        btnGlassSize.text = "Glass size (${glassSizeMl}ml)"
        rbCalRemaining.isChecked = calorieDisplayMode == "remaining"
        rbCalNet.isChecked = calorieDisplayMode == "net"

        btnWaterGoalPlus.setOnClickListener {
            waterGoal++
            tvWaterGoalValue.text = "$waterGoal glasses"
            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        btnWaterGoalMinus.setOnClickListener {
            if (waterGoal > 1) waterGoal--
            tvWaterGoalValue.text = "$waterGoal glasses"
            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        btnGlassSize.setOnClickListener {
            val items = arrayOf(200, 250, 300)
            val labels = arrayOf("200ml", "250ml", "300ml")
            val checked = items.indexOf(glassSizeMl).let { if (it == -1) 1 else it }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Glass Size")
                .setSingleChoiceItems(labels, checked) { dialog, which ->
                    glassSizeMl = items[which]
                    btnGlassSize.text = "Glass size (${labels[which]})"
                    dialog.dismiss()

                    saveNotificationSettings(
                        switchWorkoutReminder,
                        switchMealReminder,
                        switchWaterReminder
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        rbCalRemaining.setOnClickListener {
            rbCalRemaining.isChecked = true
            rbCalNet.isChecked = false
            calorieDisplayMode = "remaining"

            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        rbCalNet.setOnClickListener {
            rbCalNet.isChecked = true
            rbCalRemaining.isChecked = false
            calorieDisplayMode = "net"

            saveNotificationSettings(
                switchWorkoutReminder,
                switchMealReminder,
                switchWaterReminder
            )
        }

        // =========================
        // APPEARANCE
        // =========================
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val btnThemeAuto = view.findViewById<MaterialButton>(R.id.btn_theme_auto)
        val btnThemeDark = view.findViewById<MaterialButton>(R.id.btn_theme_dark)
        val btnThemeLight = view.findViewById<MaterialButton>(R.id.btn_theme_light)
        val switchReadMode = view.findViewById<SwitchMaterial>(R.id.switch_read_mode)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnThemeAuto.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            toast("Theme: Auto")
        }

        btnThemeDark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            toast("Theme: Dark")
        }

        btnThemeLight.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            toast("Theme: Light")
        }

        switchReadMode.setOnCheckedChangeListener { _, isChecked ->
            toast("Read Mode ${if (isChecked) "enabled" else "disabled"}")
        }

        // =========================
        // PRIVACY
        // =========================
        val btnExportData = view.findViewById<MaterialButton>(R.id.btn_export_data)
        val btnResetProgressLogs = view.findViewById<MaterialButton>(R.id.btn_reset_progress_logs)

        btnExportData.setOnClickListener {
            toast("Export My Data clicked")
        }

        btnResetProgressLogs.setOnClickListener {
            toast("Reset Progress Logs clicked")
        }

        // =========================
        // ABOUT
        // =========================
        val btnAboutFlexiFit = view.findViewById<MaterialButton>(R.id.btn_about_flexifit)
        val btnContactFeedback = view.findViewById<MaterialButton>(R.id.btn_contact_feedback)

        btnAboutFlexiFit.setOnClickListener {
            toast("About FlexiFit clicked")
        }

        btnContactFeedback.setOnClickListener {
            toast("Contact / Feedback clicked")
        }

        // =========================
        // ACCOUNT
        // =========================
        val btnChPass = view.findViewById<MaterialButton>(R.id.btn_chpass)
        val btnUpdateEmail = view.findViewById<MaterialButton>(R.id.btn_update_email)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btn_logout)
        val btnDeleteAccount = view.findViewById<MaterialButton>(R.id.btn_delete_account)

        btnChPass.setOnClickListener {
            toast("Change Password clicked")
        }

        btnUpdateEmail.setOnClickListener {
            toast("Update Email clicked")
        }

        btnLogout.setOnClickListener {
            toast("Log Out clicked")
        }

        btnDeleteAccount.setOnClickListener {
            toast("Delete Account clicked")
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
            result.onFailure {
                toast("Failed to save notification settings")
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formatted = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(formatted)
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}