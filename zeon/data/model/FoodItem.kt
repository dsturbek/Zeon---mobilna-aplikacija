package com.example.zeon.data.model

data class FoodItem (
    val id: Int,
    val name: String,
    val calories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val isLiquid: Boolean = false
)