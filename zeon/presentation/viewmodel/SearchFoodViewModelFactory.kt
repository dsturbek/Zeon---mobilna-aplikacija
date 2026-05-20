package com.example.zeon.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zeon.data.repository.FoodRepository

class SearchFoodViewModelFactory(
    private val foodRepository: FoodRepository
) : ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SearchFoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            SearchFoodViewModel(foodRepository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}