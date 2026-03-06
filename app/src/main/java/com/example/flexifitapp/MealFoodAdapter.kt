package com.example.flexifitapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MealFoodAdapter(
    private val items: MutableList<MealFood>,
    private val onOpen: (MealFood) -> Unit
) : RecyclerView.Adapter<MealFoodAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img = v.findViewById<ImageView>(R.id.imgFood)
        val name = v.findViewById<TextView>(R.id.tvFoodName)
        val sub = v.findViewById<TextView>(R.id.tvFoodSub)
        val btn = v.findViewById<ImageButton>(R.id.btnOpenFood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_food, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.name.text = item.name
        h.sub.text = "${item.servingLabel} • qty ${item.qty}"

        // Optional: load image with Glide/Coil
        // Glide.with(h.img).load(item.imageUrl).into(h.img)

        h.btn.setOnClickListener { onOpen(item) }
        h.itemView.setOnClickListener { onOpen(item) }
    }

    override fun getItemCount() = items.size

    fun updateItem(updated: MealFood) {
        val idx = items.indexOfFirst { it.mealItemId == updated.mealItemId }
        if (idx != -1) {
            items[idx] = updated
            notifyItemChanged(idx)
        }
    }
}