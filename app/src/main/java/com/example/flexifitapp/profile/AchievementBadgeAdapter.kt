package com.example.flexifitapp.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class AchievementBadgeAdapter(
    private var items: List<AchievementBadge>
) : RecyclerView.Adapter<AchievementBadgeAdapter.BadgeViewHolder>() {

    fun submitList(newItems: List<AchievementBadge>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBadgeIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
        private val tvBadgeTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)
        private val tvBadgeSubtitle: TextView = itemView.findViewById(R.id.tvBadgeSubtitle)
        private val tvBadgeStatus: TextView = itemView.findViewById(R.id.tvBadgeStatus)

        fun bind(item: AchievementBadge) {
            ivBadgeIcon.setImageResource(item.iconRes)
            tvBadgeTitle.text = item.title
            tvBadgeSubtitle.text = item.description

            if (item.unlocked) {
                tvBadgeStatus.text = "Unlocked"
                ivBadgeIcon.alpha = 1f
            } else {
                tvBadgeStatus.text = "Locked"
                ivBadgeIcon.alpha = 0.35f
            }
        }
    }
}