package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.ws.Recipe
import com.squareup.picasso.Picasso

class RecipeAdapter(
    private var recipes: List<Recipe> =emptyList()
): RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    var onItemClick: ((Recipe) -> Unit)? = null

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val ivRecipeImage: ImageView = itemView.findViewById(R.id.ivRecipeImage)
        private val tvRecipeName: TextView = itemView.findViewById(R.id.tvRecipeName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)

        fun bind(recipe: Recipe){
            tvRecipeName.text = recipe.strMeal
            tvCategory.text = "${recipe.strCategory ?: ""} • ${recipe.strArea ?: ""}"

            // Učitaj sliku sa Picasso
            Picasso.get()
                .load(recipe.strMealThumb)
                .placeholder(R.color.siva)
                .into(ivRecipeImage)

            itemView.setOnClickListener {
                onItemClick?.invoke(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_list_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>){
        recipes = newRecipes
        notifyDataSetChanged()
    }
}