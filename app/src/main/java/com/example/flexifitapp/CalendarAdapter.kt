package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.flexifitapp.utils.AppLogger

class CalendarAdapter(
    private val items: List<CalendarDay>,
    private val onDayClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txtDay: TextView = v.findViewById(R.id.txtDay)
        val layoutIndicators: View = v.findViewById(R.id.layoutIndicators)
        val dot1: View = v.findViewById(R.id.indicatorDot1)
        val dot2: View = v.findViewById(R.id.indicatorDot2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // Add this line
        AppLogger.d(
            "CALENDAR_ADAPTER",
            "Position $position: dayNumber=${item.dayNumber}, clickable=${item.isClickable}"
        )

        if (item.dayNumber == null) {
            holder.txtDay.text = ""
            holder.itemView.alpha = 0f
            holder.layoutIndicators.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
            return
        }

        holder.txtDay.text = item.dayNumber.toString()

        AppLogger.d("CALENDAR_ADAPTER", "Day ${item.dayNumber}, status: ${item.status}")

        if (item.isClickable) {
            holder.itemView.alpha = 1f
            holder.layoutIndicators.visibility = View.VISIBLE

            val hasMultiplePrograms =
                item.summary?.contains("2 programs", ignoreCase = true) == true
                        || item.summary?.contains("multi", ignoreCase = true) == true

            if (hasMultiplePrograms) {
                holder.dot1.visibility = View.VISIBLE
                holder.dot2.visibility = View.VISIBLE
                holder.dot1.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.colorPrimary)
                )
                holder.dot2.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.colorSecondary)
                )
            } else {
                holder.dot1.visibility = View.VISIBLE
                holder.dot2.visibility = View.GONE

                val colorResId: Int = when {
                    // 1. Completed/Done
                    item.status.uppercase() == "COMPLETED" || item.status.uppercase() == "DONE" ->
                        R.color.colorStatusCompleted

                    // 2. Skipped (User Action)
                    item.status.uppercase() == "SKIPPED" || item.status.uppercase() == "CANCELLED" ->
                        R.color.colorStatusSkipped

                    // 3. Missed Days
                    item.isPast && (item.status.uppercase() == "PENDING" || item.status.uppercase() == "NOT_STARTED") ->
                        R.color.colorStatusMissed

                    // 4. Pending (Current)
                    item.status.uppercase() == "PENDING" ->
                        R.color.colorStatusNotStarted

                    // 5. Default / Not Started
                    else ->
                        R.color.colorStatusNotStarted
                }

                val colorInt = ContextCompat.getColor(context, colorResId)
                holder.dot1.backgroundTintList = android.content.res.ColorStateList.valueOf(colorInt)
            }

            holder.itemView.setOnClickListener {
                AppLogger.d("CALENDAR_ADAPTER", "Clicked day: ${item.dayNumber}")
                onDayClick(item.dayNumber)
            }
        } else {
            holder.itemView.alpha = 0.4f
            holder.layoutIndicators.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size
}