package com.example.zeon.data.repository

import android.content.Context
import com.example.zeon.data.MockFoodList
import com.example.zeon.data.mapper.AddFoodToDiaryRequest
import com.example.zeon.data.mapper.CreateDiaryRequest
import com.example.zeon.data.mapper.FoodDiaryItemDto
import com.example.zeon.data.mapper.FoodMapper
import com.example.zeon.data.mapper.FoodServerDto
import com.example.zeon.data.model.FoodDiary
import com.example.zeon.data.model.FoodFoodDiary
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.model.MealType
import com.example.zeon.helpers.UserSession
import com.example.zeon.ws.NetworkModule
import java.time.LocalDate

class FoodRepository(private val context: Context) {
    private var currentFoodDiaryItems: List<FoodDiaryItemDto> = emptyList()

    suspend fun getAllFoods(): Result<List<FoodItem>> {
        return try {
            val response = NetworkModule.zeonApiService.getAllFoods()
            if (response.isSuccessful) {
                val foods = FoodMapper.toFoodItemList(response.body() ?: emptyList())
                Result.success(foods)
            } else {
                Result.failure(Exception("API Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoodById(foodId: Int): Result<FoodItem> {
        return try {
            val response = NetworkModule.zeonApiService.getFoodById(foodId)
            if (response.isSuccessful) {
                val serverFood = response.body()
                if (serverFood != null) {
                    val food = FoodMapper.toFoodItem(serverFood)
                    Result.success(food)
                } else {
                    Result.failure(Exception("Food not found"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFoods(query: String): Result<List<FoodItem>> {
        return try {
            val response = NetworkModule.zeonApiService.searchFoods(query)
            val foods = FoodMapper.toFoodItemList(response.body() ?: emptyList())
            Result.success(foods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFood(foodDto: FoodServerDto): Result<FoodItem> {
        return try {
            val response = NetworkModule.zeonApiService.createFood(foodDto)
            if (response.isSuccessful) {
                val serverFood = response.body() ?: return Result.failure(Exception("Empty response"))

                val foodItem = FoodItem(
                    id = serverFood.id,
                    name = serverFood.name,
                    calories = serverFood.calories,
                    protein = serverFood.protein,
                    fat = serverFood.fat,
                    carbs = serverFood.carbs,
                    isLiquid = false
                )
                Result.success(foodItem)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFoodByIdLocal(foodId: Int): FoodItem? {
        return MockFoodList.allFood.find { it.id == foodId }
    }

    fun addFood(food: FoodItem): Boolean {
        return try {
            MockFoodList.allFood.add(food)
            true
        } catch (e: Exception) {
            false
        }
    }

    // DNEVNIK - DOHVAĆANJE
    suspend fun getFoodDiaryForDate(date: LocalDate): Result<List<FoodFoodDiary>> {
        return try {
            val finalClientId = UserSession.getUserId(context)

            val diaryListResponse = NetworkModule.zeonApiService.getAllDiariesForClient(finalClientId)

            val diaryId = diaryListResponse.body()?.find { diary ->
                diary.food_diary_date.startsWith(date.toString())
            }?.id_food_diary

            if (diaryId == null) {
                currentFoodDiaryItems = emptyList()
                return Result.success(emptyList())
            }

            val response = NetworkModule.zeonApiService.getFoodDiaryById(diaryId)

            if (response.isSuccessful) {
                val diaryResponse = response.body()
                if (diaryResponse != null) {
                    val diary = MockFoodList.getOrCreateDiary(date)
                    MockFoodList.allEntries.removeAll { it.foodDiaryId == diary.foodDiaryId }

                    currentFoodDiaryItems = diaryResponse.foods ?: emptyList()


                    diaryResponse.foods?.forEach { serverFood ->
                        MockFoodList.allEntries.add(
                            FoodFoodDiary(
                                foodId = serverFood.id_food,
                                foodDiaryId = diary.foodDiaryId,
                                amount = serverFood.amount,
                                mealType = MealType.DORUČAK
                            )
                        )
                    }
                    Result.success(MockFoodList.getEntriesForDate(date))
                } else {
                    Result.failure(Exception("Diary not found"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEntriesForDate(date: LocalDate): List<FoodFoodDiary> {
        return MockFoodList.getEntriesForDate(date)
    }

    fun getEntriesGroupedByMealType(date: LocalDate): Map<MealType, List<FoodFoodDiary>> {
        val entries = getEntriesForDate(date)
        return entries.groupBy { it.mealType }
    }

    suspend fun addFoodToDiary(
        foodId: Int,
        amount: Float,
        mealType: MealType,
        date: LocalDate,
        clientId: Int? = null
    ): Result<Unit> {
        return try {
            val finalClientId = clientId ?: UserSession.getUserId(context)

            val diaryResponse = NetworkModule.zeonApiService.getFoodDiaryForDate(
                clientId = finalClientId,
                date = date.toString()
            )

            val diaryId = if (diaryResponse.isSuccessful) {
                diaryResponse.body()?.id_food_diary
            } else {
                val createRequest = CreateDiaryRequest(
                    ClientId = finalClientId,
                    food_diary_date = date.toString()
                )
                val createResponse = NetworkModule.zeonApiService.createFoodDiary(createRequest)
                if (createResponse.isSuccessful) {
                    createResponse.body()?.id_food_diary
                } else {
                    null
                }
            }

            if (diaryId == null) {
                return Result.failure(Exception("Nije moguće kreirati dnevnik"))
            }

            val request = AddFoodToDiaryRequest(
                ClientId = finalClientId,
                FoodId = foodId,
                amount = amount,
                date = date.toString()
            )

            val response = NetworkModule.zeonApiService.addFoodToExistingDiary(
                diaryId = diaryId,
                request = request
            )

            if (response.isSuccessful) {
                getFoodDiaryForDate(date = date)
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFoodFromDiary(foodId: Int, date: LocalDate): Result<Unit> {
        return try {
            val finalClientId = UserSession.getUserId(context)

            val diaryListResponse = NetworkModule.zeonApiService.getAllDiariesForClient(finalClientId)

            val diaryId = diaryListResponse.body()?.find { diary ->
                diary.food_diary_date.startsWith(date.toString())
            }?.id_food_diary

            if (diaryId == null) {
                return Result.failure(Exception("Diary not found"))
            }

            val response = NetworkModule.zeonApiService.removeFoodFromDiary(
                diaryId = diaryId,
                foodId = foodId
            )

            if (response.isSuccessful) {
                MockFoodList.allEntries.removeAll { it.foodId == foodId }
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setTodayWater(amount: Float) {
        MockFoodList.todayWater = amount
    }

    fun getUserGoals() = MockFoodList.userGoals

    fun getCurrentFoodDiaryItems(): List<FoodDiaryItemDto> {
        return currentFoodDiaryItems
    }
}
