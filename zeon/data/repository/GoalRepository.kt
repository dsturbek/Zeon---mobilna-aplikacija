package com.example.zeon.data.repository

import com.example.zeon.data.model.Goals
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.response.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoalRepository {
    private val api = NetworkModule.zeonApiService

    suspend fun getGoalsForClient(clientId: Int): Result<List<Goals>> {
        return withContext(Dispatchers.IO) {
            try {

                val response = api.getGoalsForClient(clientId)

                if (response.isSuccessful) {
                    val goalsDTO = response.body()

                    if (goalsDTO != null) {
                        val goals = goalsDTO.map { dto ->
                            Goals(
                                goalId = dto.goalId,
                                goalName = dto.goalName,
                                goalDescription = dto.goalDescription ?: "",
                                goalCalories = dto.goalCalories ?: 0,
                                goalWater = dto.goalWater ?: 0,
                                clientId = dto.clientId,
                                goalProtein = 0f,
                                goalCarbs = 0f,
                                goalFat = 0f
                            )
                        }

                        Result.success(goals)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()

                    val errorMessage = try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.message ?: "Failed to fetch goals"
                    } catch (e: Exception) {
                        "Failed to fetch goals: ${response.code()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
}
