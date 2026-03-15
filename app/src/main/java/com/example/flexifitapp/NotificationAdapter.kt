package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.databinding.ItemNotificationBinding

// 1. Palitan ang List ng MutableList babe!
class NotificationAdapter(private val items: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Ngayon, gagana na ang removeAt dahil Mutable na ang list natin
    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Gagana na rin ang clear() dito!
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

            val iconRes = when (item.type) {
                NotificationType.WATER -> R.drawable.ic_water_drop
                NotificationType.MEAL -> R.drawable.ic_nutri
                NotificationType.WORKOUT -> R.drawable.ic_workout
                NotificationType.ACHIEVEMENT -> R.drawable.ic_level_award
            }
            imgNotifType.setImageResource(iconRes)
        }
    }

    override fun getItemCount() = items.size
}