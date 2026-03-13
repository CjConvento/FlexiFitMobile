package com.example.flexifitapp.onboarding

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
        val programName = items[position]
        holder.bind(programName)
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

            // Gamitin ang category mula sa parser
            tvTitle.text = "${info.category} Program"
            tvLevel.text = "Level: ${info.level}"
            tvEnv.text = "Environment: ${info.environment}"

            // Dito papasok yung revised description logic gamit ang rawName
            tvDesc.text = buildDescription(info)

            renderSelectedState(programName)

            itemView.isEnabled = !isLocked
            itemView.alpha = if (isLocked) 0.65f else 1f

            itemView.setOnClickListener {
                if (isLocked) return@setOnClickListener
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val currentlySelected = selected.contains(programName)
                val next = !currentlySelected

                if (next) {
                    if (!selected.contains(programName) && selected.size >= maxSelection) {
                        onLimitReached?.invoke()
                        return@setOnClickListener
                    }
                    selected.add(programName)
                } else {
                    selected.remove(programName)
                }

                onToggle(programName, next)
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
            // Pinalitan ang safetyNote ng rawName (ito yung injury-safe status)
            val prefix = if (info.rawName.isNotBlank()) "${info.rawName} " else ""

            val categoryLower = info.category.lowercase().replace(" ", "")
            val levelLower = info.level.lowercase()
            val envLower = info.environment.lowercase()

            return when (categoryLower) {
                "cardio" ->
                    "${prefix}Recommended to improve endurance and burn calories for $levelLower users training at $envLower."
                "musclegain", "muscle_gain" ->
                    "${prefix}Recommended to build muscle for $levelLower users training at $envLower."
                "rehab" ->
                    "${prefix}Recommended for recovery-focused training at $envLower."
                else ->
                    "${prefix}Recommended program for $levelLower users at $envLower."
            }
        }
    }
}