package com.example.flexifitapp.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flexifitapp.R
import com.example.flexifitapp.UserPrefs

class PersonalDataDialogFragment : DialogFragment(R.layout.dialog_personal_data) {

    private var btnBack: ImageView? = null

    private var tvNameValue: TextView? = null
    private var tvUsernameValue: TextView? = null
    private var tvGenderValue: TextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupClicks()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        btnBack = null
        tvNameValue = null
        tvUsernameValue = null
        tvGenderValue = null
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        tvNameValue = view.findViewById(R.id.tvNameValue)
        tvUsernameValue = view.findViewById(R.id.tvUsernameValue)
        tvGenderValue = view.findViewById(R.id.tvGenderValue)
    }

    private fun setupClicks() {
        btnBack?.setOnClickListener { dismiss() }
    }

    private fun loadData() {
        val ctx = requireContext()

        val firstName = UserPrefs.getString(ctx, "first_name", "").trim()
        val lastName = UserPrefs.getString(ctx, "last_name", "").trim()
        val username = UserPrefs.getString(ctx, "username", "").trim()
        val gender = UserPrefs.getString(ctx, UserPrefs.KEY_GENDER, "").trim()

        val fullName = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "User" }

        tvNameValue?.text = fullName
        tvUsernameValue?.text = username.ifBlank { "Username" }
        tvGenderValue?.text = gender.ifBlank { "Not set" }
    }
}