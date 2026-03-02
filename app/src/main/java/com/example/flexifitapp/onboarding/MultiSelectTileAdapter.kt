package com.example.flexifitapp.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class MultiSelectTileAdapter(
    private val items: List<OptionTile>,
    preselected: Set<String> = emptySet(),
    private val onSelectionChanged: (Set<String>) -> Unit
) : RecyclerView.Adapter<MultiSelectTileAdapter.VH>() {

    private val selectedIds = preselected.toMutableSet()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = items[position].id.hashCode().toLong()

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)
        val tvLabel = itemView.findViewById<TextView>(R.id.tvLabel)
        val ivCheck = itemView.findViewById<ImageView>(R.id.ivCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option_tile, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvLabel.text = item.label
        holder.ivIcon.setImageResource(item.iconRes)

        val isSelected = selectedIds.contains(item.id)
        holder.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val changed = if (selectedIds.contains(item.id)) {
                selectedIds.remove(item.id)
            } else {
                selectedIds.add(item.id)
            }

            if (changed) {
                notifyItemChanged(position)
                onSelectionChanged(selectedIds.toSet())
            }
        }
    }
}