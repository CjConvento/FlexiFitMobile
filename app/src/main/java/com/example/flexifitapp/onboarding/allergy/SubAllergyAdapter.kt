package com.example.flexifitapp.onboarding.allergy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexifitapp.R

class SubAllergyAdapter(
    private val subAllergies: List<SubAllergy>,
    private val onChecked: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<SubAllergyAdapter.SubViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_allergy_sub, parent, false)
        return SubViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubViewHolder, position: Int) {
        val sub = subAllergies[position]
        holder.cbSub.isChecked = sub.isSelected
        holder.tvSubName.text = sub.name
        holder.cbSub.setOnCheckedChangeListener(null)
        holder.cbSub.setOnCheckedChangeListener { _, isChecked ->
            onChecked(position, isChecked)
        }
    }

    override fun getItemCount() = subAllergies.size

    class SubViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbSub: CheckBox = itemView.findViewById(R.id.cbSub)
        val tvSubName: TextView = itemView.findViewById(R.id.tvSubName)
    }
}