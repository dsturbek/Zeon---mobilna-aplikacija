package com.example.zeon.helpers

import com.example.zeon.R
import com.example.zeon.data.MockFoodList
import com.example.zeon.data.mapper.FoodDiaryItemDto
import com.example.zeon.data.mapper.FoodMapper
import com.example.zeon.data.model.FoodFoodDiary
import com.example.zeon.data.model.Goals

data class NutritionSummary(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

class NutritionCalculator {
    fun calculateNutritionFromEntries(items: List<FoodDiaryItemDto>): NutritionSummary {
        var totalCalories = 0f
        var totalProtein = 0f
        var totalCarbs = 0f
        var totalFat = 0f

        items.forEach { item ->
            val multiplier = item.amount / 100f
            totalCalories += item.calories * multiplier
            totalProtein += item.protein * multiplier
            totalCarbs += item.carbs * multiplier
            totalFat += item.fat * multiplier
        }

        return NutritionSummary(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat
        )

    }

    fun calculateWaterFromServerItems(items: List<FoodDiaryItemDto>): Float {
        return items
            .filter { item ->
                FoodMapper.isLiquidFood(item.name)
            }
            .sumOf { item ->
                item.amount.toDouble() / 1000.0
            }
            .toFloat()
    }

    fun calculateNutritionFromServerData(items: List<FoodDiaryItemDto>): NutritionSummary {
        var totalCalories = 0f
        var totalProtein = 0f
        var totalCarbs = 0f
        var totalFat = 0f

        items.forEach { item ->
            val multiplier = item.amount / 100f
            totalCalories += item.calories * multiplier
            totalProtein += item.protein * multiplier
            totalCarbs += item.carbs * multiplier
            totalFat += item.fat * multiplier
        }

        return NutritionSummary(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat
        )
    }

    fun getProgressPercent(value: Float, goal: Float): Float {
        return if (goal > 0) (value / goal) * 100 else 0f
    }

    fun getCaloriesPercent(nutrition: NutritionSummary, goals: Goals): Float {
        return getProgressPercent(nutrition.calories, goals.goalCalories.toFloat())
    }

    fun getProteinPercent(nutrition: NutritionSummary, goals: Goals): Float {
        return getProgressPercent(nutrition.protein, goals.goalProtein)
    }

    fun getCarbsPercent(nutrition: NutritionSummary, goals: Goals): Float {
        return getProgressPercent(nutrition.carbs, goals.goalCarbs)
    }

    fun getFatPercent(nutrition: NutritionSummary, goals: Goals): Float {
        return getProgressPercent(nutrition.fat, goals.goalFat)
    }

    fun getWaterPercent(waterAmount: Float, waterGoal: Float): Float {
        return getProgressPercent(waterAmount, waterGoal)
    }

    fun getColorForProgress(percent: Float, resources: android.content.res.Resources): Int {
        return when {
            percent > 100f -> resources.getColor(R.color.crvena, null)
            percent >= 90f -> resources.getColor(R.color.narancasta, null)
            else -> resources.getColor(R.color.zelena, null)
        }
    }

    fun calculateCaloriesForAmount(caloriesPer100: Float, amount: Float): Float {
        return caloriesPer100 * (amount / 100f)
    }
}