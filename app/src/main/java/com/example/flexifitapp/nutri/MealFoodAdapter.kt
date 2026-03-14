package com.example.flexifitapp.nutri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flexifitapp.ApiConfig
import com.example.flexifitapp.R

class MealFoodAdapter(
    private val items: MutableList<MealFood>,
    private val onOpen: (MealFood) -> Unit
) : RecyclerView.Adapter<MealFoodAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgFood)
        val name: TextView = v.findViewById(R.id.tvFoodName)
        val sub: TextView = v.findViewById(R.id.tvFoodSub)
        val btn: ImageButton = v.findViewById(R.id.btnOpenFood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_food, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]

        h.name.text = item.name
        h.sub.text = "${item.servingLabel} • ${item.calories} kcal"

        // Bubuuin ang full path gamit ang ApiConfig
        val fullUrl = "${ApiConfig.FOOD_IMAGE_URL}${item.imageUrl}"

        // AKTIBONG GLIDE: I-lo-load na nito ang images mula sa API mo
        Glide.with(h.itemView.context)
            .load(fullUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder) // Fallback kung walang file sa server
            .centerCrop()
            .into(h.img)

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