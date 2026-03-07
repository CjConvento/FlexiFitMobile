package com.example.flexifitapp.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.databinding.ItemAchievementBinding

class ProgressAchievementAdapter(
    private val items: List<ProgressAchievement>
) : RecyclerView.Adapter<ProgressAchievementAdapter.AchievementViewHolder>() {

    inner class AchievementViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProgressAchievement) {
            binding.tvAchievementIcon.text = item.icon
            binding.tvAchievementTitle.text = item.title
            binding.tvAchievementSubtitle.text = item.subtitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}