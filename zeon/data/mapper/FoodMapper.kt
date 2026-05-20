package com.example.zeon.data.mapper

import com.example.zeon.data.model.FoodItem
import com.google.gson.annotations.SerializedName

data class FoodServerDto(
    @SerializedName("id_food")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("kCal")
    val calories: Int,

    @SerializedName("proteins")
    val protein: Float,

    @SerializedName("fat")
    val fat: Float,

    @SerializedName("carbohydrates")
    val carbs: Float
)

data class FoodDiaryItemDto(
    @SerializedName("id_food")
    val id_food: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("kCal")
    val calories: Int,

    @SerializedName("proteins")
    val protein: Float,

    @SerializedName("fat")
    val fat: Float,

    @SerializedName("carbohydrates")
    val carbs: Float,

    @SerializedName("amount")
    val amount: Float
)


data class FoodDiaryResponse(
    @SerializedName("id_food_diary")
    val id_food_diary: Int,

    @SerializedName("ClientId")
    val ClientId: Int,

    @SerializedName("food_diary_date")
    val food_diary_date: String,

    @SerializedName("foods")
    val foods: List<FoodDiaryItemDto>,

    @SerializedName("total_nutrition")
    val total_nutrition: NutritionInfo
)

data class NutritionInfo(
    @SerializedName("kCal")
    val kCal: Float,

    @SerializedName("proteins")
    val proteins: Float,

    @SerializedName("fat")
    val fat: Float,

    @SerializedName("carbohydrates")
    val carbohydrates: Float
)

data class DiaryListItem(
    @SerializedName("id_food_diary")
    val id_food_diary: Int,
    @SerializedName("ClientId")
    val ClientId: Int,
    @SerializedName("food_diary_date")
    val food_diary_date: String
)


data class AddFoodToDiaryRequest(
    @SerializedName("ClientId")
    val ClientId: Int,

    @SerializedName("foodId")
    val FoodId: Int,

    @SerializedName("amount")
    val amount: Float,

    @SerializedName("food_diary_date")
    val date: String
)

data class CreateDiaryRequest(
    @SerializedName("ClientId")
    val ClientId: Int,
    @SerializedName("food_diary_date")
    val food_diary_date: String
)


object FoodMapper {
    fun toFoodItem(serverDto: FoodServerDto): FoodItem {
        return FoodItem(
            id = serverDto.id,
            name = serverDto.name,
            calories = serverDto.calories,
            protein = serverDto.protein,
            fat = serverDto.fat,
            carbs = serverDto.carbs,
            isLiquid = isLiquidFood(serverDto.name)
        )
    }


    fun toFoodItemList(serverDtoList: List<FoodServerDto>): List<FoodItem> {
        return serverDtoList.map { toFoodItem(it) }
    }

    fun isLiquidFood(name: String): Boolean {
        val liquidFoods = setOf(
            "voda",
            "vino",
            "pivo",
            "sok",
            "juha",
            "kafa",
            "čaj",
            "caj",
            "coffee",
            "tea",
            "beer",
            "wine",
            "water",
            "juice",
            "soup",
            "broth",
            "milk",
        )

        return liquidFoods.any {
            name.lowercase().contains(it)
        }
    }
}