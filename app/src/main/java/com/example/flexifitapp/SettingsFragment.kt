package com.example.flexifitapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "flexifit_settings"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_WEIGHT_UNIT = "weight_unit"
        private const val KEY_HEIGHT_UNIT = "height_unit"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // ==== GET VIEWS ====
        val rgLanguage = view.findViewById<RadioGroup>(R.id.rg_language)
        val rbEnglish = view.findViewById<RadioButton>(R.id.rb_language_english)
        val rbFilipino = view.findViewById<RadioButton>(R.id.rb_language_filipino)

        val rgWeight = view.findViewById<RadioGroup>(R.id.rg_weight_unit)
        val rbKg = view.findViewById<RadioButton>(R.id.rb_weight_kg)
        val rbLbs = view.findViewById<RadioButton>(R.id.rb_weight_lbs)

        val rgHeight = view.findViewById<RadioGroup>(R.id.rg_height_unit)
        val rbCm = view.findViewById<RadioButton>(R.id.rb_height_cm)
        val rbFtIn = view.findViewById<RadioButton>(R.id.rb_height_ft_in)

        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)

        val btnExport = view.findViewById<MaterialButton>(R.id.btn_export_data)
        val btnArchive = view.findViewById<MaterialButton>(R.id.btn_archive_account)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btn_logout)

        // ==== LOAD SAVED PREFERENCES ====
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedLanguage = prefs.getString(KEY_LANGUAGE, "en")
        val savedWeightUnit = prefs.getString(KEY_WEIGHT_UNIT, "kg")
        val savedHeightUnit = prefs.getString(KEY_HEIGHT_UNIT, "cm")
        val savedDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)

        when (savedLanguage) {
            "en" -> rbEnglish.isChecked = true
            "fil" -> rbFilipino.isChecked = true
        }

        when (savedWeightUnit) {
            "kg" -> rbKg.isChecked = true
            "lbs" -> rbLbs.isChecked = true
        }

        when (savedHeightUnit) {
            "cm" -> rbCm.isChecked = true
            "ft_in" -> rbFtIn.isChecked = true
        }

        switchDarkMode.isChecked = savedDarkMode

        // ==== LISTENERS – SAVE ON CHANGE ====

        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val value = when (checkedId) {
                R.id.rb_language_english -> "en"
                R.id.rb_language_filipino -> "fil"
                else -> "en"
            }
            prefs.edit().putString(KEY_LANGUAGE, value).apply()
            Toast.makeText(requireContext(), "Language saved", Toast.LENGTH_SHORT).show()
        }

        rgWeight.setOnCheckedChangeListener { _, checkedId ->
            val value = when (checkedId) {
                R.id.rb_weight_kg -> "kg"
                R.id.rb_weight_lbs -> "lbs"
                else -> "kg"
            }
            prefs.edit().putString(KEY_WEIGHT_UNIT, value).apply()
        }

        rgHeight.setOnCheckedChangeListener { _, checkedId ->
            val value = when (checkedId) {
                R.id.rb_height_cm -> "cm"
                R.id.rb_height_ft_in -> "ft_in"
                else -> "cm"
            }
            prefs.edit().putString(KEY_HEIGHT_UNIT, value).apply()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        btnExport.setOnClickListener {
            Toast.makeText(requireContext(), "Export my data – to be implemented", Toast.LENGTH_SHORT).show()
        }

        btnArchive.setOnClickListener {
            Toast.makeText(requireContext(), "Archive account – to be implemented", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Log out – to be implemented", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
