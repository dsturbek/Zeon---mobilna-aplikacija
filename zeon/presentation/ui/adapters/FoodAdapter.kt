package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.FoodItem

class FoodAdapter(
    private val foodList : MutableList<FoodItem>,
    private val layoutId:Int)
    : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    var onItemClick: ((FoodItem)->Unit)?=null
    var onDeleteClick: ((FoodItem)->Unit)?=null
    inner class FoodViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val foodName: TextView=view.findViewById(R.id.foodName)
        private val foodMacros: TextView=view.findViewById(R.id.foodMacros)
        private val btnDelete: ImageButton?=view.findViewById(R.id.btnDeleteFood)

        init{
            btnDelete?.setOnClickListener {
                val itemToDelete=foodList[adapterPosition]
                onDeleteClick?.invoke(itemToDelete)
            }

            view.setOnClickListener {
                val clickedFood=foodList[adapterPosition]
                onItemClick?.invoke(clickedFood)
            }
        }
        fun bind(item: FoodItem){
            foodName.text=item.name
            foodMacros.text="${item.calories} kcal | ${item.protein}g P | ${item.carbs}g U | ${item.fat}g M"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodViewHolder {
        val foodView= LayoutInflater
            .from(parent.context)
            .inflate(layoutId,parent,false)
        return FoodViewHolder(foodView)
    }

    override fun onBindViewHolder(
        holder: FoodViewHolder,
        position: Int
    ) {
        holder.bind(foodList[position])
    }

    override fun getItemCount(): Int= foodList.size

    fun addFood(item: FoodItem){
        foodList.add(item)
        notifyItemInserted(foodList.size-1)
    }

    fun removeFood(item: FoodItem){
        val index=foodList.indexOf(item)
        if(index!=-1){
            foodList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateFood(newList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setData(newList: List<FoodItem>){
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }

}