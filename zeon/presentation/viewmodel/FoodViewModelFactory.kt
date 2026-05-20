package com.example.zeon.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.helpers.NutritionCalculator

class FoodViewModelFactory(
    private val foodRepository: FoodRepository,
    private val nutritionCalculator: NutritionCalculator
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            FoodViewModel(foodRepository, nutritionCalculator) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}