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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        if (item.dayNumber == null) {
            holder.txtDay.text = ""
            holder.itemView.isClickable = false
            holder.itemView.alpha = 0f
            holder.itemView.setOnClickListener(null)
            return
        }

        val day = item.dayNumber!!   // safe dahil null-check sa taas
        holder.txtDay.text = day.toString()

        holder.itemView.alpha = if (item.isClickable) 1f else 0.5f
        holder.itemView.isClickable = item.isClickable

        holder.itemView.setOnClickListener {
            if (item.isClickable) onDayClick(day)
        }
    }

    override fun getItemCount(): Int = items.size
}