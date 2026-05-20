package com.example.zeon.data.repository

import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.request.LoginRequest
import com.example.zeon.ws.request.RegisterClientRequest
import com.example.zeon.ws.request.UpdateProfileRequest
import com.example.zeon.ws.response.ErrorResponse
import com.example.zeon.data.mapper.UserMapper.toApiString
import com.example.zeon.data.mapper.UserMapper.toUser
import com.example.zeon.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class UserRepository {

    private val api = NetworkModule.zeonApiService

    suspend fun login(username: String, password: String, role: String = "client"): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(username, password, role))

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Result.success(loginResponse.user.toUser())
                    } else {
                        Result.failure(Exception("Login failed: No response data"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.message ?: "Login failed"
                    } catch (e: Exception) {
                        "Login failed: ${response.code()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun registerClient(
        nameSurname: String,
        email: String,
        username: String,
        password: String,
        trainerId: Int? = null
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterClientRequest(nameSurname, email, username, password, trainerId)
                val response = api.registerClient(request)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null) {
                        Result.success(registerResponse.user.toUser())
                    } else {
                        Result.failure(Exception("Registration failed: No response data"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.message ?: "Registration failed"
                    } catch (e: Exception) {
                        "Registration failed: ${response.code()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getCurrentUser()

                if (response.isSuccessful) {
                    val userDTO = response.body()
                    if (userDTO != null) {
                        Result.success(userDTO.toUser())
                    } else {
                        Result.failure(Exception("Failed to get user data"))
                    }
                } else {
                    Result.failure(Exception("Session expired or invalid"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun getUserById(userId: Int): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUserById(userId)

                if (response.isSuccessful) {
                    val userDTO = response.body()
                    if (userDTO != null) {
                        Result.success(userDTO.toUser())
                    } else {
                        Result.failure(Exception("User not found"))
                    }
                } else {
                    Result.failure(Exception("Failed to fetch user"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun updateUser(
        userId: Int,
        username: String? = null,
        email: String? = null,
        nameSurname: String? = null,
        height: Float? = null,
        weight: Float? = null,
        birthDate: Date? = null,
        gender: String? = null
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequest(
                    name_surname = nameSurname,
                    email = email,
                    username = username,
                    height = height,
                    weight = weight,
                    birthDate = birthDate?.toApiString(),
                    gender = gender
                )

                val response = api.updateUser(userId, request)

                if (response.isSuccessful) {
                    val userDTO = response.body()
                    if (userDTO != null) {
                        Result.success(userDTO.toUser())
                    } else {
                        Result.failure(Exception("Update failed: No response data"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        error.message ?: "Update failed"
                    } catch (e: Exception) {
                        "Update failed: ${response.code()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.logout()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Logout failed"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
}