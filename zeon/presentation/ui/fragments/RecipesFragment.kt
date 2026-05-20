package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.ws.Recipe
import com.example.zeon.ws.RecipeResponse
import com.example.zeon.ws.WsRecipes
import com.example.zeon.presentation.ui.adapters.RecipeAdapter
import com.example.zeon.presentation.viewmodel.RecipeViewModel
import com.example.zeon.presentation.viewmodel.RecipeViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipesFragment : Fragment() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnBack: ImageButton
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        reachingXMLComponents(view)
        setupRecyclerView()
        setupButtonListeners()
        setupObservers()
    }

    private fun initializeViewModel() {
        val repository = FoodRepository(requireContext())
        val factory = RecipeViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(RecipeViewModel::class.java)
    }

    private fun reachingXMLComponents(view: View){
        rvRecipes = view.findViewById(R.id.rvRecipes)
        pbLoading = view.findViewById(R.id.pbLoading)
        etSearch = view.findViewById(R.id.etSearch)
        btnSearch = view.findViewById(R.id.btnSearch)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView(){
        recipeAdapter = RecipeAdapter()
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        rvRecipes.adapter = recipeAdapter

        recipeAdapter.onItemClick = { recipe ->
            val ingredientsText = recipe.getIngredients()
                .joinToString("\n") { (ingredient, measure) ->
                    "• $ingredient - $measure"
                }

            val bundle = Bundle().apply {
                putString("recipeId", recipe.idMeal)
                putString("recipeName", recipe.strMeal)
                putString("recipeCategory", recipe.strCategory)
                putString("recipeArea", recipe.strArea)
                putString("recipeInstructions", recipe.strInstructions)
                putString("recipeThumb", recipe.strMealThumb)
                putString("recipeIngredients", ingredientsText)
            }

            val detailFragment = RecipeDetailFragment()
            detailFragment.arguments = bundle

            (activity as HomeActivity).openFragment(detailFragment)
        }
    }

    private fun setupButtonListeners(){
        btnBack.setOnClickListener {
            (activity as HomeActivity).returnToViewPager()
        }

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            viewModel.searchRecipes(query)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            rvRecipes.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isEmpty()) {
                rvRecipes.visibility = View.GONE
            } else {
                rvRecipes.visibility = View.VISIBLE
                recipeAdapter.updateRecipes(recipes)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
