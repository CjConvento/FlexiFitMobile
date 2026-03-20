package com.example.flexifitapp

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var currentWorkoutTime = "08:00"
    private var currentMealTime = "12:00"
    private var currentWaterStartTime = "08:00"
    private var currentWaterEndTime = "20:00"
    private var currentWaterInterval = 60
    private var currentWaterGoal = 8 // glasses
    private var currentGlassSize = 250 // ml

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupToolbar()
        setupNotificationListeners()
        setupGoalsListeners()
        setupAppearanceListeners()
        setupPrivacyListeners()
        setupAboutListeners()
        setupAccountListeners()
        loadSettings()
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    private fun setupNotificationListeners() {
        // Workout reminder
        binding.switchWorkoutReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnWorkoutReminderTime.isEnabled = isChecked
            if (isChecked) {
                saveSettings()
            }
        }

        binding.btnWorkoutReminderTime.setOnClickListener {
            showTimePickerDialog { time ->
                currentWorkoutTime = time
                binding.btnWorkoutReminderTime.text = "Workout: $time"
                saveSettings()
            }
        }

        // Meal reminder
        binding.switchMealReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnMealReminderTime.isEnabled = isChecked
            if (isChecked) {
                saveSettings()
            }
        }

        binding.btnMealReminderTime.setOnClickListener {
            showTimePickerDialog { time ->
                currentMealTime = time
                binding.btnMealReminderTime.text = "Meal: $time"
                saveSettings()
            }
        }

        // Water reminder
        binding.switchWaterReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnWaterStartTime.isEnabled = isChecked
            binding.btnWaterEndTime.isEnabled = isChecked
            binding.btnWaterInterval.isEnabled = isChecked
            if (isChecked) {
                saveSettings()
            }
        }

        binding.btnWaterStartTime.setOnClickListener {
            showTimePickerDialog { time ->
                currentWaterStartTime = time
                binding.btnWaterStartTime.text = "Start: $time"
                saveSettings()
            }
        }

        binding.btnWaterEndTime.setOnClickListener {
            showTimePickerDialog { time ->
                currentWaterEndTime = time
                binding.btnWaterEndTime.text = "End: $time"
                saveSettings()
            }
        }

        binding.btnWaterInterval.setOnClickListener {
            showIntervalDialog()
        }
    }

    private fun setupGoalsListeners() {
        // Water goal adjustment
        binding.btnWaterGoalMinus.setOnClickListener {
            if (currentWaterGoal > 1) {
                currentWaterGoal--
                binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
                saveSettings()
            }
        }

        binding.btnWaterGoalPlus.setOnClickListener {
            if (currentWaterGoal < 20) {
                currentWaterGoal++
                binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
                saveSettings()
            }
        }

        // Glass size selection
        binding.ivDropSmall.setOnClickListener {
            currentGlassSize = 200
            binding.tvSelectedGlassLabel.text = "Small Glass"
            binding.tvSelectedGlassDesc.text = "Using 200ml per glass"
            updateGlassSelectionUI("small")
            saveSettings()
        }

        binding.ivDropMedium.setOnClickListener {
            currentGlassSize = 250
            binding.tvSelectedGlassLabel.text = "Standard Glass"
            binding.tvSelectedGlassDesc.text = "Using 250ml per glass"
            updateGlassSelectionUI("medium")
            saveSettings()
        }

        binding.ivDropLarge.setOnClickListener {
            currentGlassSize = 350
            binding.tvSelectedGlassLabel.text = "Large Glass"
            binding.tvSelectedGlassDesc.text = "Using 350ml per glass"
            updateGlassSelectionUI("large")
            saveSettings()
        }

        // Calorie display mode
        binding.toggleCalorieMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = when (checkedId) {
                    R.id.btn_cal_remaining -> "Remaining"
                    R.id.btn_cal_net -> "Net Calories"
                    else -> "Remaining"
                }
                saveCalorieMode(mode)
                saveSettings()
            }
        }
    }

    private fun setupAppearanceListeners() {
        // Dark mode switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveSettings()
        }

        // Theme mode buttons
        binding.btnThemeAuto.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            Toast.makeText(requireContext(), "Auto theme enabled", Toast.LENGTH_SHORT).show()
            saveSettings()
        }

        binding.btnThemeDark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Toast.makeText(requireContext(), "Dark theme enabled", Toast.LENGTH_SHORT).show()
            saveSettings()
        }

        binding.btnThemeLight.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Toast.makeText(requireContext(), "Light theme enabled", Toast.LENGTH_SHORT).show()
            saveSettings()
        }

        // Read mode switch
        binding.switchReadMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Read mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Read mode disabled", Toast.LENGTH_SHORT).show()
            }
            saveSettings()
        }
    }

    private fun setupPrivacyListeners() {
        binding.btnExportData.setOnClickListener {
            Toast.makeText(requireContext(), "Export feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnResetProgressLogs.setOnClickListener {
            showResetDialog()
        }
    }

    private fun setupAboutListeners() {
        binding.btnAboutFlexifit.setOnClickListener {
            Toast.makeText(requireContext(), "About FlexiFit", Toast.LENGTH_SHORT).show()
        }

        binding.btnContactFeedback.setOnClickListener {
            Toast.makeText(requireContext(), "Contact: support@flexifit.com", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAccountListeners() {
        binding.btnChpass.setOnClickListener {
            Toast.makeText(requireContext(), "Change password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnUpdateEmail.setOnClickListener {
            Toast.makeText(requireContext(), "Update email feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api(requireContext())
                val response = api.getNotificationSettings()

                if (response.isSuccessful) {
                    val settings = response.body()
                    settings?.let {
                        // Load workout settings
                        binding.switchWorkoutReminder.isChecked = it.workoutReminderEnabled
                        it.workoutReminderTime?.let { time ->
                            currentWorkoutTime = time
                            binding.btnWorkoutReminderTime.text = "Workout: $time"
                        }
                        binding.btnWorkoutReminderTime.isEnabled = it.workoutReminderEnabled

                        // Load meal settings
                        binding.switchMealReminder.isChecked = it.mealReminderEnabled
                        it.mealReminderTime?.let { time ->
                            currentMealTime = time
                            binding.btnMealReminderTime.text = "Meal: $time"
                        }
                        binding.btnMealReminderTime.isEnabled = it.mealReminderEnabled

                        // Load water settings
                        binding.switchWaterReminder.isChecked = it.waterReminderEnabled
                        it.waterStartTime?.let { time ->
                            currentWaterStartTime = time
                            binding.btnWaterStartTime.text = "Start: $time"
                        }
                        it.waterEndTime?.let { time ->
                            currentWaterEndTime = time
                            binding.btnWaterEndTime.text = "End: $time"
                        }
                        currentWaterInterval = it.waterIntervalMinutes ?: 60
                        binding.btnWaterInterval.text = "Interval: $currentWaterInterval mins"

                        binding.btnWaterStartTime.isEnabled = it.waterReminderEnabled
                        binding.btnWaterEndTime.isEnabled = it.waterReminderEnabled
                        binding.btnWaterInterval.isEnabled = it.waterReminderEnabled

                        // Load water goal
                        it.dailyWaterGoal?.let { goalMl ->
                            val glassSize = it.glassSizeMl ?: 250
                            if (glassSize > 0) {
                                currentWaterGoal = goalMl / glassSize
                                binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
                            }
                        }

                        // Load glass size
                        it.glassSizeMl?.let { size ->
                            currentGlassSize = size
                            when (size) {
                                200 -> updateGlassSelectionUI("small")
                                250 -> updateGlassSelectionUI("medium")
                                350 -> updateGlassSelectionUI("large")
                            }
                        }

                        // Load calorie mode
                        when (it.calorieDisplayMode) {
                            "Remaining" -> binding.toggleCalorieMode.check(R.id.btn_cal_remaining)
                            "Net Calories" -> binding.toggleCalorieMode.check(R.id.btn_cal_net)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSettings() {
        lifecycleScope.launch {
            try {
                val settings = com.example.flexifitapp.NotificationSettingsDto(
                    workoutReminderEnabled = binding.switchWorkoutReminder.isChecked,
                    workoutReminderTime = currentWorkoutTime,
                    mealReminderEnabled = binding.switchMealReminder.isChecked,
                    mealReminderTime = currentMealTime,
                    waterReminderEnabled = binding.switchWaterReminder.isChecked,
                    waterStartTime = currentWaterStartTime,
                    waterEndTime = currentWaterEndTime,
                    waterIntervalMinutes = currentWaterInterval,
                    dailyWaterGoal = currentWaterGoal * currentGlassSize,
                    glassSizeMl = currentGlassSize,
                    calorieDisplayMode = if (binding.toggleCalorieMode.checkedButtonId == R.id.btn_cal_remaining) "Remaining" else "Net Calories"
                )

                val api = ApiClient.api(requireContext())
                val request = com.example.flexifitapp.UpdateNotificationSettingsRequest(settings)
                val response = api.updateNotificationSettings(request)

                if (response.isSuccessful) {
                    scheduleReminders(settings)
                    Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleReminders(settings: com.example.flexifitapp.NotificationSettingsDto) {
        val scheduler = com.example.flexifitapp.NotificationScheduler(requireContext())

        scheduler.scheduleWorkoutReminder(
            settings.workoutReminderTime ?: "08:00",
            settings.workoutReminderEnabled
        )

        scheduler.scheduleMealReminder(
            settings.mealReminderTime ?: "12:00",
            settings.mealReminderEnabled
        )

        scheduler.scheduleWaterReminders(
            settings.waterReminderEnabled,
            settings.waterStartTime ?: "08:00",
            settings.waterEndTime ?: "20:00",
            settings.waterIntervalMinutes ?: 60
        )
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(time)
        }, hour, minute, true).show()
    }

    private fun showIntervalDialog() {
        val intervals = arrayOf("30 mins", "60 mins", "90 mins", "120 mins")
        val values = mapOf("30 mins" to 30, "60 mins" to 60, "90 mins" to 90, "120 mins" to 120)
        val currentIndex = intervals.indexOfFirst { values[it] == currentWaterInterval }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Water Reminder Interval")
            .setSingleChoiceItems(intervals, currentIndex) { dialog, which ->
                currentWaterInterval = values[intervals[which]] ?: 60
                binding.btnWaterInterval.text = "Interval: $currentWaterInterval mins"
                saveSettings()
                dialog.dismiss()
            }
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateGlassSelectionUI(selected: String) {
        val smallIcon = binding.ivDropSmall
        val mediumIcon = binding.ivDropMedium
        val largeIcon = binding.ivDropLarge

        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val colorSecondary = ContextCompat.getColor(requireContext(), R.color.textSecondary)

        when (selected) {
            "small" -> {
                smallIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorPrimary)
                mediumIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorSecondary)
                largeIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorSecondary)
            }
            "medium" -> {
                smallIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorSecondary)
                mediumIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorPrimary)
                largeIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorSecondary)
            }
            "large" -> {
                smallIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorSecondary)
                mediumIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorSecondary)
                largeIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorPrimary)
            }
        }
    }

    private fun saveCalorieMode(mode: String) {
        val prefs = requireContext().getSharedPreferences("FlexiFitPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("calorie_display_mode", mode).apply()
    }

    private fun showResetDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Progress Logs?")
            .setMessage("This will delete all your workout and nutrition history. This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                Toast.makeText(requireContext(), "Progress logs reset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                // Clear user data
                val prefs = requireContext().getSharedPreferences("FlexiFitPrefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                // Navigate to LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This will permanently delete your account and all data. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}