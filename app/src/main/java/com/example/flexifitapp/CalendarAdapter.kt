package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log // Siguraduhin na may import ito babe

class CalendarAdapter(
    private val items: List<CalendarDay>,
    private val onDayClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txtDay: TextView = v.findViewById(R.id.txtDay)
        val indicatorDot: View = v.findViewById(R.id.indicatorDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // 1. Handle blanks (para sa spacing)
        if (item.dayNumber == null) {
            holder.txtDay.text = ""
            holder.itemView.alpha = 0f
            holder.indicatorDot.visibility = View.GONE
            holder.itemView.setOnClickListener(null) // Para hindi clickable ang blank
            return
        }

        holder.txtDay.text = item.dayNumber.toString()

        // CCTV LOG
        Log.d("DEBUG_CALENDAR_UI", "Day ${item.dayNumber} status: ${item.status}")

        if (item.isClickable) {
            holder.itemView.alpha = 1f
            holder.indicatorDot.visibility = View.VISIBLE

            // 🎨 KULAY LOGIC (FIXED BABE!)
            val color = when (item.status.uppercase()) {
                "COMPLETED" -> "#5c8a73" // Green 🟢
                "SKIPPED", "CANCELLED" -> "#621B21" // Red 🔴
                "PENDING" -> "#9EB9D4"   // Blue (Added #) 🔵
                else -> "#9E9E9E"         // Gray
            }

            try {
                holder.indicatorDot.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(color))
            } catch (e: Exception) {
                Log.e("DEBUG_CALENDAR_UI", "Mali ang hex color babe: $color")
            }

            // Click listener para sa mga active days
            holder.itemView.setOnClickListener { onDayClick(item.dayNumber) }
        } else {
            // Future days: Faded at walang dot
            holder.itemView.alpha = 0.4f
            holder.indicatorDot.visibility = View.GONE
            holder.itemView.setOnClickListener(null) // I-disable ang click sa future days
        }
    }

    override fun getItemCount(): Int = items.size
}