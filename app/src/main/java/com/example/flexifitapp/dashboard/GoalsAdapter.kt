package com.example.flexifitapp.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class GoalsAdapter(private val goals: List<String>) :
    RecyclerView.Adapter<GoalsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtGoalName: TextView = view.findViewById(R.id.txtGoalName)
        val imgGoalIcon: ImageView = view.findViewById(R.id.imgGoalIcon)
        val container: View = view.findViewById(R.id.containerGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dshb_goalchip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        holder.txtGoalName.text = goal

        // Dynamic Styling base sa Goal name
        when (goal.lowercase()) {
            "muscle gain" -> {
                holder.imgGoalIcon.setImageResource(R.drawable.ic_muscle)
                holder.container.setBackgroundResource(R.drawable.btn_gradient)
            }
            "cardio" -> {
                holder.imgGoalIcon.setImageResource(R.drawable.ic_cardio)
                holder.container.setBackgroundResource(R.drawable.btn_gradient)
            }
            "rehab" -> {
                holder.imgGoalIcon.setImageResource(R.drawable.ic_rehab)
                holder.container.setBackgroundResource(R.drawable.btn_gradient)
            }
        }
    }

    override fun getItemCount() = goals.size
}