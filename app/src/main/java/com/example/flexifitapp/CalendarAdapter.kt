package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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


        // 1. Handle blanks
        if (item.dayNumber == null) {
            holder.txtDay.text = ""
            holder.itemView.alpha = 0f
            holder.indicatorDot.visibility = View.GONE
            return
        }

        val day = item.dayNumber
        holder.txtDay.text = day.toString()

        // 2. Highlighting & Indicator Logic
        if (item.isClickable) {
            // ACTIVE DAY (May record sa DB)
            holder.itemView.alpha = 1f
            holder.indicatorDot.visibility = View.VISIBLE

            if (item.isCompleted) {
                // COMPLETED: Green Dot
                holder.indicatorDot.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.GREEN)
            } else {
                // PENDING/NOT COMPLETED: Orange Dot
                holder.indicatorDot.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFA500"))
            }
        } else {
            // NOT STARTED / NO RECORD
            holder.itemView.alpha = 0.5f
            holder.indicatorDot.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (item.isClickable) onDayClick(day)
        }
    }

    override fun getItemCount(): Int = items.size
}