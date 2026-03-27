package com.example.flexifitapp.onboarding

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class ProgramCardAdapter(
    private val items: List<String>,
    private val selected: MutableSet<String>,
    private val onToggle: (String, Boolean) -> Unit,
    private val isLocked: Boolean,
    private val maxSelection: Int = 4,
    private val onLimitReached: (() -> Unit)? = null
) : RecyclerView.Adapter<ProgramCardAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_program_card, parent, false)
        return VH(view, selected, isLocked, onToggle, maxSelection, onLimitReached)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        private val selected: MutableSet<String>,
        private val isLocked: Boolean,
        private val onToggle: (String, Boolean) -> Unit,
        private val maxSelection: Int,
        private val onLimitReached: (() -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvProgramTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvProgramDesc)
        private val tvLevel: TextView = itemView.findViewById(R.id.tvLevel)
        private val tvEnv: TextView = itemView.findViewById(R.id.tvEnv)
        private val ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)

        fun bind(programName: String) {
            val info = ProgramNameParser.parse(programName)

            // 1. CLEAN TITLES: Gawing may space ang category (e.g., MuscleGain -> Muscle Gain)
            val cleanCategory = if (info.category.equals("MuscleGain", true)) "Muscle Gain" else info.category
            tvTitle.text = "$cleanCategory Program"

            tvLevel.text = "Level: ${info.level}"
            tvEnv.text = "Environment: ${info.environment}"

            // 2. DYNAMIC DESCRIPTION
            tvDesc.text = buildDescription(info)

            renderSelectedState(programName)

            // 3. UI LOCKING
            itemView.isEnabled = !isLocked
            itemView.alpha = if (isLocked) 0.5f else 1f

            itemView.setOnClickListener {
                if (isLocked) return@setOnClickListener

                val currentlySelected = selected.contains(programName)
                val isAdding = !currentlySelected

                // 4. LIMIT LOGIC with Logs
                if (isAdding) {
                    if (selected.size >= maxSelection) {
                        Log.w("FLEXIFIT_DEBUG", "Selection Limit Reached ($maxSelection). Cannot add: $programName")
                        onLimitReached?.invoke()
                        return@setOnClickListener
                    }
                    selected.add(programName)
                    Log.d("FLEXIFIT_DEBUG", "Program Added: $programName (Total: ${selected.size})")
                } else {
                    selected.remove(programName)
                    Log.d("FLEXIFIT_DEBUG", "Program Removed: $programName (Total: ${selected.size})")
                }

                onToggle(programName, isAdding)
                renderSelectedState(programName)
            }
        }

        private fun renderSelectedState(programName: String) {
            val isSelected = selected.contains(programName)
            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.bg_option_tile_selected else R.drawable.bg_option_tile
            )
        }

        private fun buildDescription(info: ProgramInfo): String {
            // Mas natural na prefix (e.g., "Injury-Safe: Recommended...")
            val prefix = if (info.rawName.isNotBlank()) "${info.rawName}: " else ""

            val categoryLower = info.category.lowercase().replace(" ", "")
            val levelLower = info.level.lowercase()
            val envLower = info.environment.lowercase()

            return when (categoryLower) {
                "cardio" ->
                    "${prefix}Designed to boost endurance and heart health for $levelLower users in a $envLower setting."
                "musclegain", "muscle_gain" ->
                    "${prefix}Hypertrophy-focused training to build muscle for $levelLower users at the $envLower."
                "rehab" ->
                    "${prefix}Focused on safe recovery and joint mobility within a $envLower environment."
                else ->
                    "${prefix}A customized $levelLower program tailored for $envLower workouts."
            }
        }
    }
}