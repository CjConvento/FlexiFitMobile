package com.example.flexifitapp.nutri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class MealSectionAdapter(
    private var sections: MutableList<MealSection>,
    private val onOpenFood: (mealType: String, food: MealFood) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {

    // --- SMART UPDATE ---
    fun updateData(newSections: List<MealSection>) {
        // I-save muna natin kung ano ang mga "expanded" na sections sa kasalukuyan
        val expandedStates = sections.associate { it.mealType to it.expanded }

        // I-apply ang saved states sa bagong data na galing sa API
        newSections.forEach { newSection ->
            newSection.expanded = expandedStates[newSection.mealType] ?: true // Default to true para kita agad ang food
        }

        this.sections = newSections.toMutableList()
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvMealTitle)
        val expand: ImageView = v.findViewById(R.id.ivExpand)
        val rvFoods: RecyclerView = v.findViewById(R.id.rvMealItems)
        val header: View = v.findViewById(R.id.headerRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_section, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val section = sections[position]
        holder.title.text = section.mealType

        // Para hindi tayo laging nag-re-create ng adapter, i-check kung may adapter na
        val foodAdapter = MealFoodAdapter(section.foods) { food ->
            onOpenFood(section.mealType, food)
        }
        holder.rvFoods.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvFoods.adapter = foodAdapter
        holder.rvFoods.isNestedScrollingEnabled = false

        // --- EXPAND/COLLAPSE LOGIC ---
        holder.rvFoods.visibility = if (section.expanded) View.VISIBLE else View.GONE

        // Smooth rotation (optional tweak)
        holder.expand.animate().rotation(if (section.expanded) 0f else -90f).setDuration(200).start()

        holder.header.setOnClickListener {
            section.expanded = !section.expanded
            // Mas mabilis ito kaysa notifyDataSetChanged()
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = sections.size
}