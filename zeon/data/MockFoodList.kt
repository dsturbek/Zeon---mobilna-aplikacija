package com.example.zeon.data

import com.example.zeon.data.model.FoodDiary
import com.example.zeon.data.model.FoodFoodDiary
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.model.Goals
import java.time.LocalDate


object MockFoodList {

    val allFood=mutableListOf<FoodItem>(
        FoodItem(1, "Piletina", 165, 31f, 3f, 0f, isLiquid = false),
        FoodItem(2, "Riža", 130, 2.7f, 0.3f, 28f, isLiquid = false),
        FoodItem(3, "Banana", 89, 1.1f, 0.3f, 23f, isLiquid = false),
        FoodItem(4, "Jaja", 155, 13f, 11f, 1f, isLiquid = false),
        FoodItem(5, "Voda", 0, 0f, 0f, 0f, isLiquid = true),
        FoodItem(6, "Narančin sok", 45, 0.7f, 0.2f, 10.4f, isLiquid = true),
        FoodItem(7, "Čaj", 1, 0f, 0f, 0.2f, isLiquid = true),
        FoodItem(8, "Kava", 2, 0.1f, 0.1f, 0.3f, isLiquid = true),
        FoodItem(9, "Mlijeko", 42, 3.4f, 1f, 5f, isLiquid = true)
    )

    val allDiaries=mutableListOf<FoodDiary>()

    val allEntries=mutableListOf<FoodFoodDiary>()

    var todayWater: Float= 0f



    var userGoals = Goals(
        goalId = 1,
        goalName = "Održavanje",
        goalDescription = "Održavanje trenutne težine",
        goalCalories = 2000,
        goalWater = 2,
        clientId = 1,
        goalProtein = 150f,
        goalCarbs = 250f,
        goalFat = 70f
    )

    fun getOrCreateDiary(foodDairyDate: LocalDate, clientId: Int=1): FoodDiary{
        val existing = allDiaries.find { it.foodDairyDate == foodDairyDate && it.clientId == clientId }

        if (existing != null) {
            return existing
        }

        val newDiary= FoodDiary(
            foodDiaryId = allDiaries.size+1,
            foodDairyDate=foodDairyDate,
            clientId = clientId
        )

        allDiaries.add(newDiary)
        return newDiary
    }

    fun getEntriesForDate(foodDairyDate: LocalDate, clientId: Int = 1): List<FoodFoodDiary> {
        val diary = allDiaries.find { it.foodDairyDate == foodDairyDate && it.clientId == clientId }

        if (diary == null) {
            return emptyList()
        }

        return allEntries.filter { it.foodDiaryId == diary.foodDiaryId }
    }

}