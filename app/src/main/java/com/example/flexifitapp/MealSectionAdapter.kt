package com.example.flexifitapp

class MealSectionAdapter(
    private val sections: MutableList<MealSection>,
    private val onOpenFood: (mealType: String, food: MealFood) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title = v.findViewById<TextView>(R.id.tvMealTitle)
        val expand = v.findViewById<ImageView>(R.id.ivExpand)
        val rvFoods = v.findViewById<RecyclerView>(R.id.rvFoods)
        val header = v.findViewById<View>(R.id.headerRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_section_rv, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val section = sections[position]
        h.title.text = section.mealType

        h.rvFoods.layoutManager = LinearLayoutManager(h.itemView.context)
        val foodAdapter = MealFoodAdapter(section.foods) { food ->
            onOpenFood(section.mealType, food)
        }
        h.rvFoods.adapter = foodAdapter
        h.rvFoods.isNestedScrollingEnabled = false

        h.rvFoods.visibility = if (section.expanded) View.VISIBLE else View.GONE
        h.expand.rotation = if (section.expanded) 0f else -90f

        h.header.setOnClickListener {
            section.expanded = !section.expanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = sections.size
}