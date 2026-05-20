package com.example.zeon.ws

import com.example.zeon.data.mapper.AddFoodToDiaryRequest
import com.example.zeon.data.mapper.CreateDiaryRequest
import com.example.zeon.data.mapper.DiaryListItem
import com.example.zeon.data.mapper.FoodDiaryResponse
import com.example.zeon.data.mapper.FoodServerDto
import com.example.zeon.ws.request.FeedbackTrainingRequest
import com.example.zeon.ws.request.LoginRequest
import com.example.zeon.ws.request.RegisterClientRequest
import com.example.zeon.ws.request.UpdateProfileRequest
import com.example.zeon.ws.response.ExerciseResponse
import com.example.zeon.ws.response.FeedbackTrainingResponse
import com.example.zeon.ws.response.GoalDTO
import com.example.zeon.ws.response.LoginResponse
import com.example.zeon.ws.response.PersonalRecordDTO
import com.example.zeon.ws.response.RegisterResponse
import com.example.zeon.ws.response.UserDTO
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.*

interface ZeonApiService {
    @GET("api/workout-assignments/client/{clientId}/active")
    fun getActiveWorkouts(@Path("clientId") clientId: Int): Call<List<WorkoutAssignment>>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register/client")
    suspend fun registerClient(
        @Body request: RegisterClientRequest
    ): Response<RegisterResponse>

    @GET("api/workout-assignments/{assignedId}")
    fun getAssignmentDetails(@Path("assignedId") assignedId: Int): Call<WorkoutDetailsResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserDTO>

    @GET("api/conversations/client/{clientId}")
    fun getConversations(@Path("clientId") clientId: Int): Call<List<Conversation>>

    @GET("api/conversations/{conversationId}")
    fun getConversationById(@Path("conversationId") conversationId: Int): Call<Conversation>

    @POST("api/conversations")
    fun createConversation(@Body request: CreateConversationRequest): Call<CreateConversationResponse>

    @GET("api/messages/conversation/{conversationId}")
    fun getMessages(@Path("conversationId") conversationId: Int): Call<List<Message>>

    @POST("api/messages")
    fun sendMessage(@Body request: SendMessageBodyRequest): Call<SendMessageResponseNew>

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): Response<UserDTO>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body request: UpdateProfileRequest
    ): Response<UserDTO>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("trainers")
    fun getAllTrainers(): Call<List<TrainerDto>>

    @GET("trainers/{id}")
    fun getTrainerById(@Path("id") trainerId: Int): Call<TrainerDto>

    @GET("api/goals/client/{clientId}")
    suspend fun getGoalsForClient(
        @Path("clientId") clientId: Int
    ): Response<List<GoalDTO>>

    @GET("api/exercises/{exerciseId}")
    fun getExerciseById(@Path("exerciseId") exerciseId: Int): Call<ExerciseResponse>

    @POST("api/feedback")
    fun submitFeedback(@Body request: FeedbackTrainingRequest): Call<FeedbackTrainingResponse>

    @GET("api/feedback/client/{clientId}")
    fun getClientFeedback(
        @Path("clientId") clientId: Int
    ): Call<List<FeedbackTrainingResponse>>

    @GET("api/feedback/client/{clientId}/workout/{workoutId}")
    fun getWorkoutFeedback(
        @Path("clientId") clientId: Int,
        @Path("workoutId") workoutId: Int
    ): Call<List<FeedbackTrainingResponse>>

    @GET("api/personal-records/client/{clientId}")
    suspend fun getPersonalRecords(
        @Path("clientId") clientId: Int
    ): Response<List<PersonalRecordDTO>>

    @GET("api/personal-records/client/{clientId}/exercise/{exerciseId}")
    suspend fun getPersonalRecordForExercise(
        @Path("clientId") clientId: Int,
        @Path("exerciseId") exerciseId: Int
    ): Response<List<PersonalRecordDTO>>

    @GET("api/exercises")
    suspend fun getAllExercises(): Response<List<ExerciseResponse>>

    @GET("api/feedback/client/{clientId}")
    suspend fun getClientFeedbackSuspend(
        @Path("clientId") clientId: Int
    ): Response<List<FeedbackTrainingResponse>>

    @GET("api/foods")
    suspend fun getAllFoods(): Response<List<FoodServerDto>>

    @GET("api/foods/search/{query}")
    suspend fun searchFoods(@Path("query") query: String): Response<List<FoodServerDto>>

    @GET("api/foods/{id}")
    suspend fun getFoodById(@Path("id") foodId: Int): Response<FoodServerDto>

    @GET("api/food-diary/client/{clientId}/date/{date}")
    suspend fun getFoodDiaryForDate(
        @Path("clientId") clientId: Int,
        @Path("date") date: String
    ): Response<FoodDiaryResponse>

    @POST("api/food-diary/{diaryId}/foods")
    suspend fun addFoodToExistingDiary(@Path("diaryId") diaryId: Int, @Body request: AddFoodToDiaryRequest): Response<Unit>

    @POST("api/food-diary")
    suspend fun createFoodDiary(
        @Body request: CreateDiaryRequest
    ): Response<FoodDiaryResponse>

    @POST("api/foods")
    suspend fun createFood(@Body foodDto: FoodServerDto): Response<FoodServerDto>
    @DELETE("api/food-diary/{diaryId}/foods/{foodId}")
    suspend fun removeFoodFromDiary(
        @Path("diaryId") diaryId: Int,
        @Path("foodId") foodId: Int
    ): Response<Unit>
    @GET("api/food-diary/{diaryId}")
    suspend fun getFoodDiaryById(@Path("diaryId") diaryId: Int): Response<FoodDiaryResponse>

    @GET("api/food-diary/client/{clientId}")
    suspend fun getAllDiariesForClient(@Path("clientId") clientId: Int): Response<List<DiaryListItem>>


}