package com.example.flexifitapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.databinding.FragmentNotificationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        // 1. I-setup ang Clear All Button
        binding.btnClearAll.setOnClickListener {
            showDeleteAllDialog()
        }

        // 2. I-setup ang Swipe logic
        setupSwipeToDelete()

        viewModel.fetchNotifications()
    }

    private fun setupRecyclerView() {
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Tinatawag ang removeItem sa adapter
                (binding.rvNotifications.adapter as? NotificationAdapter)?.removeItem(viewHolder.adapterPosition)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvNotifications)
    }

    private fun showDeleteAllDialog() {
        val prefs = requireContext().getSharedPreferences("FlexiFitPrefs", Context.MODE_PRIVATE)
        val skipWarning = prefs.getBoolean("skipDeleteWarning", false)

        val adapter = binding.rvNotifications.adapter as? NotificationAdapter

        if (skipWarning) {
            adapter?.clearAll() //
            return
        }

        val checkBox = CheckBox(requireContext()).apply { text = "Don't show this again" }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 0)
            addView(checkBox)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All Notifications?")
            .setMessage("This will remove all your recent alerts.")
            .setView(container)
            .setPositiveButton("Delete All") { _, _ ->
                if (checkBox.isChecked) prefs.edit().putBoolean("skipDeleteWarning", true).apply()
                adapter?.clearAll() //
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.notifications.observe(viewLifecycleOwner) { items ->
            // Ginagawa nating MutableList para gumana ang delete functions
            binding.rvNotifications.adapter = NotificationAdapter(items.toMutableList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}