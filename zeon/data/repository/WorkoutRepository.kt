package com.example.zeon.data.repository

import android.os.Handler
import android.os.Looper
import com.example.zeon.data.model.DifficultyLevel
import com.example.zeon.data.model.Exercise
import com.example.zeon.data.model.ExerciseLog
import com.example.zeon.data.model.Workout
import com.example.zeon.data.model.WorkoutExercise
import com.example.zeon.data.model.WorkoutLog
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.WorkoutAssignment
import com.example.zeon.ws.WorkoutDetailsResponse
import com.example.zeon.ws.response.FeedbackTrainingResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkoutRepository private constructor() {
    private val apiService = NetworkModule.zeonApiService
    private val mainHandler = Handler(Looper.getMainLooper())
    private val workouts = mutableListOf<Workout>()
    private val workoutLogs = mutableListOf<WorkoutLog>()
    private val workoutIdMapping = mutableMapOf<String, Pair<Int, Int>>()
    private val dbWorkoutIdToDomainIds = mutableMapOf<Int, MutableList<String>>()
    private val domainIdToDbWorkoutId = mutableMapOf<String, Int>()

    companion object {
        @Volatile
        private var instance: WorkoutRepository? = null
        fun getInstance(): WorkoutRepository {
            return instance ?: synchronized(this) {
                instance ?: WorkoutRepository().also { instance = it }
            }
        }
    }

    private var lastClientId: Int = -1

    fun fetchActiveWorkouts(clientId: Int, callback: (List<Workout>?) -> Unit) {
        lastClientId = clientId
        apiService.getActiveWorkouts(clientId).enqueue(object : Callback<List<WorkoutAssignment>> {
            override fun onResponse(
                call: Call<List<WorkoutAssignment>>,
                response: Response<List<WorkoutAssignment>>
            ) {
                if (response.isSuccessful) {
                    val assignments = response.body()
                    if (assignments.isNullOrEmpty()) {
                        workouts.clear()
                        mainHandler.post { callback(emptyList()) }
                        return
                    }
                    fetchDetailsForAssignments(assignments, callback)
                } else {
                    mainHandler.post { callback(null) }
                }
            }

            override fun onFailure(call: Call<List<WorkoutAssignment>>, t: Throwable) {
                mainHandler.post { callback(null) }
            }
        })
    }

    private fun fetchDetailsForAssignments(
        assignments: List<WorkoutAssignment>,
        callback: (List<Workout>?) -> Unit
    ) {
        val allWorkouts = mutableListOf<Workout>()
        val remaining = AtomicInteger(assignments.size)
        val hasError = AtomicInteger(0)

        for (assignment in assignments) {
            apiService.getAssignmentDetails(assignment.idWorkoutAssigned)
                .enqueue(object : Callback<WorkoutDetailsResponse> {
                    override fun onResponse(
                        call: Call<WorkoutDetailsResponse>,
                        response: Response<WorkoutDetailsResponse>
                    ) {
                        if (response.isSuccessful) {
                            val details = response.body()
                            if (details != null) {
                                val mapped = mapAssignmentToWorkouts(assignment, details)
                                synchronized(allWorkouts) {
                                    allWorkouts.addAll(mapped)
                                }
                            }
                        } else {
                            hasError.incrementAndGet()
                        }
                        if (remaining.decrementAndGet() == 0) {
                            finalizeFetch(allWorkouts, hasError.get() > 0 && allWorkouts.isEmpty(), callback)
                        }
                    }

                    override fun onFailure(call: Call<WorkoutDetailsResponse>, t: Throwable) {
                        hasError.incrementAndGet()
                        if (remaining.decrementAndGet() == 0) {
                            finalizeFetch(allWorkouts, hasError.get() > 0 && allWorkouts.isEmpty(), callback)
                        }
                    }
                })
        }
    }

    private fun finalizeFetch(
        allWorkouts: List<Workout>,
        totalFailure: Boolean,
        callback: (List<Workout>?) -> Unit
    ) {
        if (totalFailure) {
            mainHandler.post { callback(null) }
        } else {
            workouts.clear()
            workouts.addAll(allWorkouts)
            val exerciseRepo = ExerciseRepository.getInstance()
            for (workout in allWorkouts) {
                for (we in workout.exercises) {
                    exerciseRepo.cacheExercise(we.exercise)
                }
            }
            fetchCompletedFeedback(lastClientId) {
                mainHandler.post { callback(allWorkouts) }
            }
        }
    }

    private fun fetchCompletedFeedback(clientId: Int, onDone: () -> Unit) {
        if (clientId == -1) { onDone(); return }

        apiService.getClientFeedback(clientId).enqueue(object : Callback<List<FeedbackTrainingResponse>> {
            override fun onResponse(
                call: Call<List<FeedbackTrainingResponse>>,
                response: Response<List<FeedbackTrainingResponse>>
            ) {
                if (response.isSuccessful) {
                    val feedbackList = response.body() ?: emptyList()
                    buildWorkoutLogsFromFeedback(feedbackList)
                }
                onDone()
            }

            override fun onFailure(call: Call<List<FeedbackTrainingResponse>>, t: Throwable) {
                onDone()
            }
        })
    }

    private fun buildWorkoutLogsFromFeedback(feedbackList: List<FeedbackTrainingResponse>) {
        val today = LocalDate.now()
        val grouped = feedbackList.groupBy { it.workoutId }

        for ((dbWorkoutId, feedbacks) in grouped) {
            val domainIds = dbWorkoutIdToDomainIds[dbWorkoutId] ?: continue

            val candidateWorkouts = domainIds
                .mapNotNull { id -> workouts.find { it.id == id } }
                .filter { it.date <= today }
                .sortedBy { it.date }

            val workout = candidateWorkouts.lastOrNull() ?: continue
            val domainId = workout.id

            if (workoutLogs.any { it.workoutId == domainId }) continue

            val exerciseLogs = feedbacks.map { fb ->
                ExerciseLog(
                    id = fb.id.toString(),
                    workoutId = domainId,
                    exerciseId = fb.exerciseId.toString(),
                    completedSets = fb.completedSets,
                    completedReps = fb.completedReps,
                    weight = fb.completedWeight,
                    comment = fb.message ?: "",
                    timestamp = LocalDateTime.now()
                )
            }

            val workoutLog = WorkoutLog(
                id = UUID.randomUUID().toString(),
                workoutId = domainId,
                completedDate = workout.date.atStartOfDay(),
                exerciseLogs = exerciseLogs,
                difficulty = DifficultyLevel.MEDIUM,
                durationSeconds = 0L
            )
            workoutLogs.add(workoutLog)
        }
    }

    private fun mapAssignmentToWorkouts(
        assignment: WorkoutAssignment,
        details: WorkoutDetailsResponse
    ): List<Workout> {
        val startDate = LocalDate.parse(assignment.dateStart.substringBefore("T"))

        return details.workouts.map { detailedWorkout ->
            val workoutDate = startDate.plusDays((detailedWorkout.workoutPlanDay - 1).toLong())
            val domainId = "${assignment.idWorkoutAssigned}_${detailedWorkout.workoutPlanDay}"

            workoutIdMapping[domainId] = Pair(assignment.idWorkoutAssigned, detailedWorkout.workoutPlanDay)
            detailedWorkout.workoutId?.let { dbId ->
                dbWorkoutIdToDomainIds.getOrPut(dbId) { mutableListOf() }.add(domainId)
                domainIdToDbWorkoutId[domainId] = dbId
            }

            val exercises = detailedWorkout.exercises.map { de ->
                WorkoutExercise(
                    id = "${de.exerciseId}_we",
                    exerciseId = de.exerciseId.toString(),
                    exercise = Exercise(
                        id = de.exerciseId.toString(),
                        name = de.name,
                        description = "",
                        muscleGroup = de.muscle,
                        videoUrl = de.videoUrl
                    ),
                    plannedSets = de.sets,
                    plannedReps = de.reps,
                    plannedWeight = de.weight
                )
            }

            Workout(
                id = domainId,
                title = detailedWorkout.workoutName,
                date = workoutDate,
                time = LocalTime.of(18, 0),
                numberOfExercises = exercises.size,
                muscleGroup = detailedWorkout.muscleGroup,
                exercises = exercises
            )
        }
    }

    fun getWorkoutIdMapping(workoutId: String): Pair<Int, Int>? {
        return workoutIdMapping[workoutId]
    }

    fun getDbWorkoutId(domainWorkoutId: String): Int? {
        return domainIdToDbWorkoutId[domainWorkoutId]
    }

    fun getAllWorkouts(): List<Workout> {
        return workouts
    }

    fun getWorkoutById(id: String): Workout? {
        return workouts.find { it.id == id }
    }

    fun getWorkoutsByDate(date: LocalDate): List<Workout> {
        return workouts.filter { it.date == date }
    }

    fun saveWorkoutLog(workoutLog: WorkoutLog) {
        workoutLogs.add(workoutLog)
    }

    fun getWorkoutLogs(): List<WorkoutLog> {
        return workoutLogs
    }

    fun isWorkoutCompletedOnDate(workoutId: String, date: LocalDate): Boolean {
        return workoutLogs.any {
            it.workoutId == workoutId && it.completedDate.toLocalDate() == date
        }
    }

    fun getWorkoutLogForDate(workoutId: String, date: LocalDate): WorkoutLog? {
        return workoutLogs.firstOrNull {
            it.workoutId == workoutId && it.completedDate.toLocalDate() == date
        }
    }
}
