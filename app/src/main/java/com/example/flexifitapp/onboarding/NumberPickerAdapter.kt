package com.example.flexifitapp.onboarding

import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class NumberPickerAdapter(
    private val items: List<Int>
) : RecyclerView.Adapter<NumberPickerAdapter.VH>() {

    // "infinite" feel
    private val virtualCount = if (items.isNotEmpty()) Int.MAX_VALUE else 0

    var onBindNumber: ((tv: TextView, number: Int, isSelected: Boolean) -> Unit)? = null

    /** called when user taps a number (optional) */
    var onNumberClick: ((virtualPos: Int, number: Int) -> Unit)? = null

    /** for efficient highlight updates */
    private var _selectedVirtualPos: Int = RecyclerView.NO_POSITION
    val selectedVirtualPos: Int get() = _selectedVirtualPos

    fun setSelectedVirtualPos(newPos: Int) {
        if (newPos == _selectedVirtualPos) return
        val old = _selectedVirtualPos
        _selectedVirtualPos = newPos

        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        notifyItemChanged(newPos)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = (view as? TextView) ?: view.findViewById(R.id.tvNumber)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION || items.isEmpty()) return@setOnClickListener
                val number = getNumberAtVirtualPos(pos)
                onNumberClick?.invoke(pos, number)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // ✅ Try inflate XML if exists
        val v: View = try {
            LayoutInflater.from(parent.context).inflate(R.layout.item_picker_number, parent, false)
        } catch (_: Exception) {
            // ✅ Fallback: create TextView programmatically
            TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(parent, 48)
                )
                gravity = Gravity.CENTER
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
            }
        }
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (items.isEmpty()) return

        val number = items[position % items.size]
        holder.tv.text = number.toString()

        val isSelected = (position == _selectedVirtualPos)

        // default styling if no callback supplied
        if (onBindNumber == null) {
            holder.tv.alpha = if (isSelected) 1f else 0.4f
            holder.tv.textSize = if (isSelected) 22f else 16f
        } else {
            onBindNumber?.invoke(holder.tv, number, isSelected)
        }
    }

    override fun getItemCount() = virtualCount

    fun getNumberAtVirtualPos(virtualPos: Int): Int {
        if (items.isEmpty()) return 0
        return items[virtualPos % items.size]
    }

    fun getVirtualPosForNumber(number: Int): Int {
        if (items.isEmpty()) return 0
        val idx = items.indexOf(number).coerceAtLeast(0)

        // keep it around the middle so it can scroll up/down
        val middle = Int.MAX_VALUE / 2
        val base = middle - (middle % items.size)
        return base + idx
    }

    private fun dp(parent: ViewGroup, dp: Int): Int {
        val density = parent.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}