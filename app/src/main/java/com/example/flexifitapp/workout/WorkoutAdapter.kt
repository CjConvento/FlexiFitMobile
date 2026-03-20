package com.example.flexifitapp.workout

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flexifitapp.R
import com.example.flexifitapp.databinding.ItemWorkoutEntryBinding

class WorkoutAdapter(
    private val items: List<WorkoutItem>,
    private val onClick: (WorkoutItem) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkoutItem) {
            val context = binding.root.context

            // 1. Basic Data
            binding.tvWorkoutName.text = item.name

            // 2. BY ITEM UI LOGIC (Done/Skip State)
            // Gamitin ang isCompleted property mula sa WorkoutItem model
            if (item.isCompleted == true) {
                // Style para sa TAPOS NA: May guhit ang text at medyo transparent ang card
                binding.tvWorkoutName.paintFlags = binding.tvWorkoutName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.6f

                // Siguraduhin na ang btnStartExercise ay TextView/Button sa XML
                binding.btnStartExercise.text = "Finished ✅"
                binding.btnStartExercise.isEnabled = false
            } else {
                // Style para sa HINDI PA TAPOS: Normal look
                binding.tvWorkoutName.paintFlags = binding.tvWorkoutName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f

                binding.btnStartExercise.text = "Start"
                binding.btnStartExercise.isEnabled = true
            }

            // 3. Image Loading (Direct load dahil full URL na)
            Glide.with(context)
                .load(item.imageFileName)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.ivWorkoutIcon)

            // 4. Click Listeners
            binding.root.setOnClickListener { onClick(item) }
            binding.btnStartExercise.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}