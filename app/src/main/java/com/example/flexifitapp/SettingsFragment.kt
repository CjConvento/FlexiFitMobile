package com.example.flexifitapp

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.flexifitapp.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var currentWorkoutTime = "08:00"
    private var currentMealTime = "12:00"
    private var currentWaterStartTime = "08:00"
    private var currentWaterEndTime = "20:00"
    private var currentWaterInterval = 60
    private var currentWaterGoal = 8       // glasses
    private var currentGlassSize = 250     // ml
    private var currentDailyWaterGoalMl = 2000   // total ml goal

    // Flags
    private var isLoadingNotifications = false
    private var isSettingSwitchProgrammatically = false

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

        loadAppearanceSettings()
        loadNotificationSettings()
    }

    private fun setupToolbar() {
        // (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    private fun setupNotificationListeners() {
        binding.switchWorkoutReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnWorkoutReminderTime.isEnabled = isChecked
            if (isChecked) saveSettings()
        }

        binding.btnWorkoutReminderTime.setOnClickListener {
            showTimePickerDialog { time ->
                currentWorkoutTime = time
                binding.btnWorkoutReminderTime.text = "Workout: $time"
                saveSettings()
            }
        }

        binding.switchMealReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnMealReminderTime.isEnabled = isChecked
            if (isChecked) saveSettings()
        }

        binding.btnMealReminderTime.setOnClickListener {
            showTimePickerDialog { time ->
                currentMealTime = time
                binding.btnMealReminderTime.text = "Meal: $time"
                saveSettings()
            }
        }

        binding.switchWaterReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnWaterStartTime.isEnabled = isChecked
            binding.btnWaterEndTime.isEnabled = isChecked
            binding.btnWaterInterval.isEnabled = isChecked
            if (isChecked) saveSettings()
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
                currentDailyWaterGoalMl = currentWaterGoal * currentGlassSize
                binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
                saveSettings()
            }
        }

        binding.btnWaterGoalPlus.setOnClickListener {
            if (currentWaterGoal < 20) {
                currentWaterGoal++
                currentDailyWaterGoalMl = currentWaterGoal * currentGlassSize
                binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
                saveSettings()
            }
        }

        // Glass size selection
        binding.ivDropSmall.setOnClickListener {
            currentGlassSize = 200
            // Recalculate glasses based on stored ml goal
            currentWaterGoal = (currentDailyWaterGoalMl.toFloat() / currentGlassSize).roundToInt().coerceAtLeast(1)
            // Update ml goal to match the new glasses count (ensures integer glasses)
            currentDailyWaterGoalMl = currentWaterGoal * currentGlassSize
            binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
            binding.tvSelectedGlassLabel.text = "Small Glass"
            binding.tvSelectedGlassDesc.text = "Using 200ml per glass"
            updateGlassSelectionUI("small")
            saveSettings()
        }

        binding.ivDropMedium.setOnClickListener {
            currentGlassSize = 250
            currentWaterGoal = (currentDailyWaterGoalMl.toFloat() / currentGlassSize).roundToInt().coerceAtLeast(1)
            currentDailyWaterGoalMl = currentWaterGoal * currentGlassSize
            binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
            binding.tvSelectedGlassLabel.text = "Standard Glass"
            binding.tvSelectedGlassDesc.text = "Using 250ml per glass"
            updateGlassSelectionUI("medium")
            saveSettings()
        }

        binding.ivDropLarge.setOnClickListener {
            currentGlassSize = 350
            currentWaterGoal = (currentDailyWaterGoalMl.toFloat() / currentGlassSize).roundToInt().coerceAtLeast(1)
            currentDailyWaterGoalMl = currentWaterGoal * currentGlassSize
            binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
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
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isSettingSwitchProgrammatically) return@setOnCheckedChangeListener
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            context?.let { AppPrefs.setNightMode(it, mode) }
            requireActivity().recreate()
        }

        binding.btnThemeAuto.setOnClickListener {
            isSettingSwitchProgrammatically = true
            binding.switchDarkMode.isChecked = false
            isSettingSwitchProgrammatically = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            context?.let { AppPrefs.setNightMode(it, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
            requireActivity().recreate()
        }

        binding.btnThemeDark.setOnClickListener {
            isSettingSwitchProgrammatically = true
            binding.switchDarkMode.isChecked = true
            isSettingSwitchProgrammatically = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            context?.let { AppPrefs.setNightMode(it, AppCompatDelegate.MODE_NIGHT_YES) }
            requireActivity().recreate()
        }

        binding.btnThemeLight.setOnClickListener {
            isSettingSwitchProgrammatically = true
            binding.switchDarkMode.isChecked = false
            isSettingSwitchProgrammatically = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            context?.let { AppPrefs.setNightMode(it, AppCompatDelegate.MODE_NIGHT_NO) }
            requireActivity().recreate()
        }

        binding.switchReadMode.setOnCheckedChangeListener { _, isChecked ->
            if (isSettingSwitchProgrammatically) return@setOnCheckedChangeListener
            context?.let { AppPrefs.setReadMode(it, isChecked) }
            requireActivity().recreate()
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
            showChangeEmailDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showChangeEmailDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_email, null)
        val emailInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewEmail)
        val emailLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Email")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val newEmail = emailInput.text?.toString()?.trim() ?: ""
                if (newEmail.isEmpty()) {
                    emailLayout.error = "Email cannot be empty"
                    return@setPositiveButton
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    emailLayout.error = "Invalid email format"
                    return@setPositiveButton
                }

                // Disable dialog buttons temporarily to prevent double submission
                val positiveButton = (dialog as androidx.appcompat.app.AlertDialog).getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false

                lifecycleScope.launch {
                    try {
                        val api = ApiClient.api()
                        val response = api.updateEmail(UpdateEmailDto(newEmail))
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Email updated successfully!", Toast.LENGTH_SHORT).show()
                            // Optionally update stored email in UserPrefs
                            UserPrefs.putString(requireContext(), UserPrefs.KEY_USER_EMAIL, newEmail)
                            dialog.dismiss()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMsg = if (errorBody?.contains("already in use") == true) {
                                "This email is already registered."
                            } else {
                                "Failed to update email. Please try again."
                            }
                            emailLayout.error = errorMsg
                            positiveButton.isEnabled = true
                        }
                    } catch (e: Exception) {
                        emailLayout.error = "Network error: ${e.message}"
                        positiveButton.isEnabled = true
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadAppearanceSettings() {
        if (isAdded) {
            val ctx = context ?: return
            isSettingSwitchProgrammatically = true
            val nightMode = AppPrefs.getNightMode(ctx)
            binding.switchDarkMode.isChecked = nightMode == AppCompatDelegate.MODE_NIGHT_YES
            val readMode = AppPrefs.isReadModeEnabled(ctx)
            binding.switchReadMode.isChecked = readMode
            isSettingSwitchProgrammatically = false
        }
    }

    private fun loadNotificationSettings() {
        if (isLoadingNotifications) return
        isLoadingNotifications = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.api()
                val response = api.getNotificationSettings()

                if (response.isSuccessful) {
                    val settings = response.body()
                    settings?.let {
                        // Workout
                        binding.switchWorkoutReminder.isChecked = it.workoutReminderEnabled
                        it.workoutReminderTime?.let { time ->
                            currentWorkoutTime = time
                            binding.btnWorkoutReminderTime.text = "Workout: $time"
                        }
                        binding.btnWorkoutReminderTime.isEnabled = it.workoutReminderEnabled

                        // Meal
                        binding.switchMealReminder.isChecked = it.mealReminderEnabled
                        it.mealReminderTime?.let { time ->
                            currentMealTime = time
                            binding.btnMealReminderTime.text = "Meal: $time"
                        }
                        binding.btnMealReminderTime.isEnabled = it.mealReminderEnabled

                        // Water
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

                        // Water goal (ml)
                        it.dailyWaterGoal?.let { goalMl ->
                            currentDailyWaterGoalMl = goalMl
                            val glassSize = it.glassSizeMl ?: 250
                            currentGlassSize = glassSize
                            currentWaterGoal = (goalMl.toFloat() / glassSize).roundToInt().coerceAtLeast(1)
                            // Update ml goal to match integer glasses (optional, keeps consistency)
                            currentDailyWaterGoalMl = currentWaterGoal * currentGlassSize
                            binding.tvWaterGoalValue.text = "$currentWaterGoal glasses"
                        }

                        // Glass size
                        it.glassSizeMl?.let { size ->
                            currentGlassSize = size
                            when (size) {
                                200 -> updateGlassSelectionUI("small")
                                250 -> updateGlassSelectionUI("medium")
                                350 -> updateGlassSelectionUI("large")
                            }
                        }

                        // Calorie mode
                        when (it.calorieDisplayMode) {
                            "Remaining" -> binding.toggleCalorieMode.check(R.id.btn_cal_remaining)
                            "Net Calories" -> binding.toggleCalorieMode.check(R.id.btn_cal_net)
                        }
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    context?.let {
                        Toast.makeText(it, "Error loading settings", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                isLoadingNotifications = false
            }
        }
    }

    private fun saveSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch
            val ctx = context ?: return@launch
            try {
                val settings = NotificationSettingsDto(
                    workoutReminderEnabled = binding.switchWorkoutReminder.isChecked,
                    workoutReminderTime = currentWorkoutTime,
                    mealReminderEnabled = binding.switchMealReminder.isChecked,
                    mealReminderTime = currentMealTime,
                    waterReminderEnabled = binding.switchWaterReminder.isChecked,
                    waterStartTime = currentWaterStartTime,
                    waterEndTime = currentWaterEndTime,
                    waterIntervalMinutes = currentWaterInterval,
                    dailyWaterGoal = currentDailyWaterGoalMl,   // send ml goal
                    glassSizeMl = currentGlassSize,
                    calorieDisplayMode = if (binding.toggleCalorieMode.checkedButtonId == R.id.btn_cal_remaining) "Remaining" else "Net Calories"
                )

                val api = ApiClient.api()
                val request = UpdateNotificationSettingsRequest(settings)
                val response = api.updateNotificationSettings(request)

                if (response.isSuccessful) {
                    scheduleReminders(ctx, settings)
                    Toast.makeText(ctx, "Settings saved", Toast.LENGTH_SHORT).show()
                } else {
                    // Optionally log error
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleReminders(ctx: Context, settings: NotificationSettingsDto) {
        val scheduler = NotificationScheduler(ctx)
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
                val prefs = requireContext().getSharedPreferences("FlexiFitPrefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
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