package com.example.zeon.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zeon.data.mapper.FoodServerDto
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.repository.FoodRepository
import kotlinx.coroutines.launch

class SearchFoodViewModel(
    private val foodRepository: FoodRepository
) : ViewModel(){
    private val _allFoods = MutableLiveData<List<FoodItem>>()
    val allFoods: LiveData<List<FoodItem>> = _allFoods

    private val _filteredFoods = MutableLiveData<List<FoodItem>>()
    val filteredFoods: LiveData<List<FoodItem>> = _filteredFoods

    private val _foodAddedSuccess = MutableLiveData<FoodItem?>()
    val foodAddedSuccess: LiveData<FoodItem?> = _foodAddedSuccess

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllFoods()
    }

    fun loadAllFoods() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = foodRepository.getAllFoods()
            result.onSuccess { foods ->
                _allFoods.value = foods
                _filteredFoods.value = foods
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun searchFoods(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            if (query.isEmpty()) {
                _filteredFoods.value = _allFoods.value ?: emptyList()
                _isLoading.value = false
                return@launch
            }

            val result = foodRepository.searchFoods(query)

            result.onSuccess { foods ->
                _filteredFoods.value = foods
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun addNewFood(name: String, calories: Int, protein: Float, fat: Float, carbs: Float, isLiquid: Boolean){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val newFoodDto = FoodServerDto(
                    id = 0,
                    name = name,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carbs = carbs
                )
                val result = foodRepository.createFood(newFoodDto)

                result.onSuccess { createdFood ->
                    loadAllFoods()
                    _foodAddedSuccess.value = createdFood
                    _isLoading.value = false
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Greška pri dodavanju hrane"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}