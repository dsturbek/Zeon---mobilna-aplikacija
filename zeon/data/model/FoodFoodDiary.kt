package com.example.zeon.data.model

data class FoodFoodDiary(
    val foodId: Int,
    val foodDiaryId: Int,
    val amount: Float,
    val mealType: MealType= MealType.OSTALO
)
