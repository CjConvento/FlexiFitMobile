package com.example.flexifitapp.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class OptionTileAdapter(
    private val items: List<OptionTile>,
    initiallySelectedId: String? = null,
    private val onClick: (OptionTile) -> Unit
) : RecyclerView.Adapter<OptionTileAdapter.VH>() {

    private var selectedId: String? = initiallySelectedId

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
        val ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option_tile, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvLabel.text = item.label
        holder.ivIcon.setImageResource(item.iconRes)

        val isSelected = item.id == selectedId
        holder.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val prev = selectedId
            if (prev == item.id) {
                // already selected → keep selected (typical single select behavior)
                onClick(item)
                return@setOnClickListener
            }

            selectedId = item.id

            // ✅ refresh only 2 items (previous + current)
            if (prev != null) {
                val prevIndex = items.indexOfFirst { it.id == prev }
                if (prevIndex != -1) notifyItemChanged(prevIndex)
            }
            notifyItemChanged(position)

            onClick(item)
        }
    }

    override fun getItemCount() = items.size
}