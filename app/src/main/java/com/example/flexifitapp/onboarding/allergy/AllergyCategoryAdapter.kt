package com.example.flexifitapp.onboarding.allergy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class AllergyCategoryAdapter(
    private val categories: MutableList<AllergyCategory>,
    private val onCategoryChecked: (Int, Boolean) -> Unit,
    private val onSubChecked: (Int, Int, Boolean) -> Unit
) : RecyclerView.Adapter<AllergyCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_allergy_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)

        // Expand/collapse when clicking the header
        holder.categoryHeader.setOnClickListener {
            category.isExpanded = !category.isExpanded
            notifyItemChanged(position)
        }

        // Category checkbox listener
        holder.cbCategory.setOnCheckedChangeListener(null)
        holder.cbCategory.isChecked = category.isCategorySelected
        holder.cbCategory.setOnCheckedChangeListener { _, isChecked ->
            onCategoryChecked(position, isChecked)
        }

        // Sub-adapter setup
        val subAdapter = SubAllergyAdapter(category.subAllergies) { subPos, isChecked ->
            onSubChecked(position, subPos, isChecked)
        }
        holder.rvSubAllergies.adapter = subAdapter
        holder.rvSubAllergies.visibility = if (category.isExpanded) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryHeader: LinearLayout = itemView.findViewById(R.id.categoryHeader)
        val cbCategory: CheckBox = itemView.findViewById(R.id.cbCategory)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val ivExpand: ImageView = itemView.findViewById(R.id.ivExpand)
        val rvSubAllergies: RecyclerView = itemView.findViewById(R.id.rvSubAllergies)

        fun bind(category: AllergyCategory) {
            tvCategoryName.text = category.name
            ivExpand.rotation = if (category.isExpanded) 180f else 0f
        }
    }
}