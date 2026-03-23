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

        // Add this line
        Log.d("CALENDAR_ADAPTER", "Position $position: dayNumber=${item.dayNumber}, clickable=${item.isClickable}")

        if (item.dayNumber == null) {
            holder.txtDay.text = ""
            holder.itemView.alpha = 0f
            holder.indicatorDot.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
            return
        }

        holder.txtDay.text = item.dayNumber.toString()
        Log.d("CALENDAR_ADAPTER", "Day ${item.dayNumber}, status: ${item.status}")

        if (item.isClickable) {
            holder.itemView.alpha = 1f
            holder.indicatorDot.visibility = View.VISIBLE

            val color = when (item.status.uppercase()) {
                "COMPLETED" -> "#5c8a73"
                "SKIPPED", "CANCELLED" -> "#621B21"
                "PENDING" -> "#9EB9D4"
                else -> "#9E9E9E"
            }
            holder.indicatorDot.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(color)
            )

            // ✅ Safe click: ensure dayNumber is not null
            holder.itemView.setOnClickListener {
                Log.d("CALENDAR_ADAPTER", "Clicked day: ${item.dayNumber}")
                onDayClick(item.dayNumber)   // now it's non-null because we checked above
            }
        } else {
            holder.itemView.alpha = 0.4f
            holder.indicatorDot.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size
}