package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MealSectionAdapter(
    private val sections: MutableList<MealSection>,
    private val onOpenFood: (mealType: String, food: MealFood) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvMealTitle)
        val expand: ImageView = v.findViewById(R.id.ivExpand)
        val rvFoods: RecyclerView = v.findViewById(R.id.rvMealItems)
        val header: View = v.findViewById(R.id.headerRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_section, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val section = sections[position]

        holder.title.text = section.mealType

        holder.rvFoods.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvFoods.adapter = MealFoodAdapter(section.foods) { food ->
            onOpenFood(section.mealType, food)
        }

        holder.rvFoods.isNestedScrollingEnabled = false

        holder.rvFoods.visibility = if (section.expanded) View.VISIBLE else View.GONE
        holder.expand.rotation = if (section.expanded) 0f else -90f

        holder.header.setOnClickListener {
            section.expanded = !section.expanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = sections.size
}