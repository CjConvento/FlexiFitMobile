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
    private val isLocked: Boolean
) : RecyclerView.Adapter<ProgramCardAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_program_card, parent, false)
        return VH(view, selected, isLocked, onToggle)
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
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvProgramTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvProgramDesc)
        private val tvLevel: TextView = itemView.findViewById(R.id.tvLevel)
        private val tvEnv: TextView = itemView.findViewById(R.id.tvEnv)
        private val ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)

        fun bind(programName: String) {
            val info = ProgramNameParser.parse(programName)

            tvTitle.text = "${info.program} Program"
            tvLevel.text = "Level: ${info.level}"
            tvEnv.text = "Environment: ${info.environment}"
            tvDesc.text = buildDescription(info)

            renderSelectedState(programName)

            itemView.isEnabled = !isLocked
            itemView.alpha = if (isLocked) 0.65f else 1f

            itemView.setOnClickListener {
                if (isLocked) return@setOnClickListener
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val currentlySelected = selected.contains(programName)
                val next = !currentlySelected

                // ✅ update local set so UI is always consistent
                if (next) selected.add(programName) else selected.remove(programName)

                // ✅ notify parent for autosave/store/vm
                onToggle(programName, next)

                // ✅ update UI immediately
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
            val safetyNote = if (info.safetyNote.isNotBlank()) "${info.safetyNote} " else ""
            return when (info.program.lowercase()) {
                "cardio" ->
                    "${safetyNote}Recommended to improve endurance and burn calories for ${info.level.lowercase()} users training at ${info.environment.lowercase()}."
                "musclegain", "muscle gain", "muscle_gain" ->
                    "${safetyNote}Recommended to build muscle for ${info.level.lowercase()} users training at ${info.environment.lowercase()}."
                "rehab" ->
                    "${safetyNote}Recommended for recovery-focused training at ${info.environment.lowercase()}."
                else ->
                    "${safetyNote}Recommended program for ${info.level.lowercase()} users at ${info.environment.lowercase()}."
            }
        }
    }
}