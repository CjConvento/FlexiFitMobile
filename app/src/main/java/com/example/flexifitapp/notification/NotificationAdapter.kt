package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.databinding.ItemNotificationBinding
import com.example.flexifitapp.notification.NotificationItem
import com.example.flexifitapp.notification.NotificationType

class NotificationAdapter(private val items: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clearAll() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            txtNotifTitle.text = item.title
            txtNotifBody.text = item.message
            txtNotifTime.text = item.time

            // ✅ Fixed: Added else branch to make when exhaustive
            val iconRes = when (item.type) {
                NotificationType.WORKOUT -> R.drawable.ic_workout
                NotificationType.MEAL -> R.drawable.ic_nutri
                NotificationType.WATER -> R.drawable.ic_water_drop
                NotificationType.ACHIEVEMENT -> R.drawable.ic_level_award
                else -> R.drawable.ic_workout  // ✅ Added else branch
            }
            imgNotifType.setImageResource(iconRes)
        }
    }

    override fun getItemCount() = items.size
}