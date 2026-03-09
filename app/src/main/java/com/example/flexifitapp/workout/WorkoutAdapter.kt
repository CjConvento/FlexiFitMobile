package com.example.flexifitapp.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flexifitapp.databinding.ItemWorkoutEntryBinding

class WorkoutAdapter(
    private val items: List<WorkoutItem>,
    private val imageBaseUrl: String,
    private val onClick: (WorkoutItem) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkoutItem) {
            binding.tvWorkoutItemName.text = item.name

            val imageUrl = if (item.imageFileName.isNotBlank()) {
                imageBaseUrl + item.imageFileName
            } else {
                null
            }

            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivWorkoutThumb)

            binding.root.setOnClickListener {
                onClick(item)
            }

            binding.btnOpenWorkoutItem.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}