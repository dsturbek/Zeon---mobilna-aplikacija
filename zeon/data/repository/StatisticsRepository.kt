package com.example.zeon.data.repository

import com.example.zeon.data.model.MuscleProgress
import com.example.zeon.data.model.PersonalRecord
import com.example.zeon.data.model.StatisticsSummary
import com.example.zeon.data.model.StreakData
import com.example.zeon.data.model.WorkoutFeedback
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.response.ErrorResponse
import com.example.zeon.ws.response.ExerciseResponse
import com.example.zeon.ws.response.FeedbackTrainingResponse
import com.example.zeon.ws.response.PersonalRecordDTO
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class StatisticsRepository {
    private val api = NetworkModule.zeonApiService

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private var exercisesCache: List<ExerciseResponse>? = null

    suspend fun getPersonalRecords(clientId: Int): Result<List<PersonalRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                if (exercisesCache == null) {
                    loadExercisesCache()
                }

                val response = api.getPersonalRecords(clientId)

                if (response.isSuccessful) {
                    val dtoList = response.body()

                    if (dtoList != null) {
                        val records = dtoList.map { dto -> mapPersonalRecord(dto) }
                        Result.success(records)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun getWorkoutHistory(clientId: Int): Result<List<WorkoutFeedback>> {
        return withContext(Dispatchers.IO) {
            try {
                if (exercisesCache == null) {
                    loadExercisesCache()
                }

                val response = api.getClientFeedbackSuspend(clientId)

                if (response.isSuccessful) {
                    val dtoList = response.body()

                    if (dtoList != null) {
                        val feedback = dtoList.map { dto -> mapWorkoutFeedback(dto) }
                        Result.success(feedback)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun getStatisticsSummary(clientId: Int): Result<StatisticsSummary> {
        return withContext(Dispatchers.IO) {
            try {
                if (exercisesCache == null) {
                    loadExercisesCache()
                }
                val recordsResult = getPersonalRecords(clientId)
                val historyResult = getWorkoutHistory(clientId)

                if (recordsResult.isFailure) {
                    return@withContext Result.failure(
                        recordsResult.exceptionOrNull() ?: Exception("Failed to load personal records")
                    )
                }

                if (historyResult.isFailure) {
                    return@withContext Result.failure(
                        historyResult.exceptionOrNull() ?: Exception("Failed to load workout history")
                    )
                }

                val records = recordsResult.getOrDefault(emptyList())
                val history = historyResult.getOrDefault(emptyList())

                val streakData = calculateStreak(history)
                val muscleProgress = calculateMuscleProgress(records)
                val weeklyFrequency = calculateWeeklyFrequency(history)

                val summary = StatisticsSummary(
                    totalWorkouts = history.distinctBy { it.workoutId }.size,
                    currentStreak = streakData.currentStreak,
                    longestStreak = streakData.longestStreak,
                    personalRecords = records,
                    muscleProgress = muscleProgress,
                    weeklyFrequency = weeklyFrequency,
                    totalWorkoutDays = streakData.totalWorkoutDays
                )

                Result.success(summary)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("Failed to load statistics: ${e.message}"))
            }
        }
    }

    fun calculateStreak(feedbackList: List<WorkoutFeedback>): StreakData {
        if (feedbackList.isEmpty()) {
            return StreakData(0, 0, 0)
        }

        val workoutDates = feedbackList
            .map { it.completedDate }
            .distinct()
            .sortedDescending()

        if (workoutDates.isEmpty()) {
            return StreakData(0, 0, 0)
        }

        var currentStreak = 0
        val today = LocalDate.now()
        var checkDate = today

        val mostRecentWorkout = workoutDates.first()
        val daysSinceLastWorkout = ChronoUnit.DAYS.between(mostRecentWorkout, today)

        if (daysSinceLastWorkout <= 1) {
            checkDate = mostRecentWorkout

            for (date in workoutDates) {
                if (date == checkDate) {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                } else if (date == checkDate.minusDays(1)) {
                    checkDate = date
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
        }

        var longestStreak = 1
        var tempStreak = 1

        val sortedDates = workoutDates.sortedDescending()

        for (i in 0 until sortedDates.size - 1) {
            val daysDiff = ChronoUnit.DAYS.between(sortedDates[i + 1], sortedDates[i])

            if (daysDiff == 1L) {
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                tempStreak = 1
            }
        }

        longestStreak = maxOf(longestStreak, currentStreak)

        return StreakData(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalWorkoutDays = workoutDates.size
        )
    }

    fun calculateMuscleProgress(records: List<PersonalRecord>): List<MuscleProgress> {
        if (records.isEmpty()) {
            return emptyList()
        }

        val recordsByMuscle = records.groupBy { it.muscle }

        return recordsByMuscle.map { (muscle, muscleRecords) ->
            val sortedByDate = muscleRecords.sortedBy { it.dateAchievement }

            val initialRecords = sortedByDate.take(3)
            val currentRecords = sortedByDate.takeLast(3)

            val initialAvgWeight = if (initialRecords.isNotEmpty()) {
                initialRecords.map { it.maxWeight }.average()
            } else 0.0

            val currentAvgWeight = if (currentRecords.isNotEmpty()) {
                currentRecords.map { it.maxWeight }.average()
            } else 0.0

            val progressPercent = if (initialAvgWeight > 0) {
                ((currentAvgWeight - initialAvgWeight) / initialAvgWeight * 100)
            } else 0.0

            MuscleProgress(
                muscleGroup = muscle,
                progressPercent = progressPercent,
                currentWeight = currentAvgWeight,
                initialWeight = initialAvgWeight,
                exerciseCount = muscleRecords.distinctBy { it.exerciseId }.size
            )
        }.sortedByDescending { it.progressPercent }
    }

    private fun calculateWeeklyFrequency(feedbackList: List<WorkoutFeedback>): Double {
        if (feedbackList.isEmpty()) {
            return 0.0
        }

        val workoutDates = feedbackList.map { it.completedDate }.distinct()

        if (workoutDates.isEmpty()) {
            return 0.0
        }

        val sortedDates = workoutDates.sorted()
        val firstDate = sortedDates.first()
        val lastDate = sortedDates.last()

        val totalDays = ChronoUnit.DAYS.between(firstDate, lastDate) + 1
        val totalWeeks = maxOf(1.0, totalDays / 7.0)

        return workoutDates.size / totalWeeks
    }

    private suspend fun loadExercisesCache() {
        try {
            val response = api.getAllExercises()
            if (response.isSuccessful) {
                exercisesCache = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapPersonalRecord(dto: PersonalRecordDTO): PersonalRecord {
        val exercise = exercisesCache?.find { it.id == dto.exerciseId }

        val date = try {
            dto.dateAchievement?.let {
                LocalDate.parse(it.substring(0, 10), dateFormatter)
            } ?: LocalDate.now()
        } catch (e: Exception) {
            LocalDate.now()
        }

        return PersonalRecord(
            id = dto.id,
            maxWeight = dto.maxWeight ?: 0.0,
            maxReps = dto.maxReps ?: 0,
            maxVolume = dto.maxVolume ?: 0.0,
            dateAchievement = date,
            exerciseId = dto.exerciseId,
            exerciseName = dto.exerciseName ?: exercise?.name ?: "Unknown Exercise",
            muscle = dto.muscle ?: exercise?.muscle ?: "Other"
        )
    }

    private fun mapWorkoutFeedback(dto: FeedbackTrainingResponse): WorkoutFeedback {
        val exercise = exercisesCache?.find { it.id == dto.exerciseId }

        val date = try {
            dto.createdAt?.let {
                try {
                    LocalDate.parse(it.substring(0, 10), dateFormatter)
                } catch (e: Exception) {
                    LocalDate.now()
                }
            } ?: LocalDate.now()
        } catch (e: Exception) {
            LocalDate.now()
        }

        return WorkoutFeedback(
            id = dto.id,
            completedSets = dto.completedSets,
            completedReps = dto.completedReps,
            completedWeight = dto.completedWeight,
            exerciseId = dto.exerciseId,
            workoutId = dto.workoutId,
            completedDate = date,
            message = dto.message ?: "",
            exerciseName = dto.exerciseName ?: exercise?.name ?: "Unknown Exercise",
            workoutName = dto.workoutName ?: "Workout",
            muscle = dto.muscle ?: exercise?.muscle ?: "Other"
        )
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
            error.message ?: "Failed to fetch data"
        } catch (e: Exception) {
            "Failed to fetch data"
        }
    }

    companion object {
        @Volatile
        private var instance: StatisticsRepository? = null

        fun getInstance(): StatisticsRepository {
            return instance ?: synchronized(this) {
                instance ?: StatisticsRepository().also { instance = it }
            }
        }
    }
}
