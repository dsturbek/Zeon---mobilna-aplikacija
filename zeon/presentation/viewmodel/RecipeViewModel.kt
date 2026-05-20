package com.example.zeon.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zeon.data.mapper.FoodServerDto
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.model.MealType
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.ws.Recipe
import com.example.zeon.ws.RecipeResponse
import com.example.zeon.ws.WsRecipes
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class RecipeViewModel(
    private val foodRepository: FoodRepository
) : ViewModel(){
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _currentRecipe = MutableLiveData<Recipe?>()
    val currentRecipe: LiveData<Recipe?> = _currentRecipe

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _recipeAddedSuccess = MutableLiveData(false)
    val recipeAddedSuccess: LiveData<Boolean> = _recipeAddedSuccess

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack

    private val api = WsRecipes.recipeService

    fun searchRecipes(query: String) {
        if (query.isEmpty()) {
            _errorMessage.value = "Unesite pojam za pretragu"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        api.searchRecipes(query).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    val fetchedRecipes = response.body()?.meals ?: emptyList()

                    if (fetchedRecipes.isEmpty()) {
                        _errorMessage.value = "Nema rezultata"
                        _recipes.value = emptyList()
                    } else {
                        _recipes.value = fetchedRecipes
                        _errorMessage.value = null
                    }
                } else {
                    _errorMessage.value = "Greška pri dohvaćanju"
                    _recipes.value = emptyList()
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "Nema internetske veze"
                _recipes.value = emptyList()
            }
        })
    }

    fun loadDefaultRecipes() {
        searchRecipes("chicken")
    }

    fun addRecipeToDiary(recipeName: String, amount: Float, mealType: MealType) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val allFoods = foodRepository.getAllFoods().getOrNull() ?: emptyList()

                var foodItem = allFoods.find { it.name == recipeName }

                if (foodItem == null) {
                    val newFoodDto = FoodServerDto(
                        id = 0,
                        name = recipeName,
                        calories = 200,
                        protein = 15.0f,
                        fat = 5.0f,
                        carbs = 25.0f
                    )

                    val createResult = foodRepository.createFood(newFoodDto)

                    if (createResult.isSuccess) {
                        foodItem = createResult.getOrNull()
                        foodRepository.getAllFoods()
                    } else {
                        _errorMessage.value = "Greška pri kreiranju hrane"
                        _isLoading.value = false
                        return@launch
                    }
                }

                val result = foodRepository.addFoodToDiary(
                    foodId = foodItem!!.id,
                    amount = amount,
                    mealType = mealType,
                    date = LocalDate.now()
                )

                result.onSuccess {
                    _recipeAddedSuccess.value = true
                    _isLoading.value = false
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Greška pri dodavanju recepte"
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                _errorMessage.value = "Greška: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun goBack() {
        _navigateBack.value = true
    }

    fun resetNavigation() {
        _navigateBack.value = false
    }

    fun resetRecipeAdded() {
        _recipeAddedSuccess.value = false
    }

}