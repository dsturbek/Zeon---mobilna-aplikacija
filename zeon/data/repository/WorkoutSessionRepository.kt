package com.example.zeon.data.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.zeon.data.model.ExerciseLog
import com.example.zeon.data.model.Workout
import com.example.zeon.data.model.WorkoutLog
import com.example.zeon.data.model.WorkoutSession
import com.example.zeon.data.model.DifficultyLevel
import com.example.zeon.helpers.UserSession
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.request.FeedbackTrainingRequest
import com.example.zeon.ws.response.FeedbackTrainingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class WorkoutSessionRepository private constructor() {
    private var currentSession: WorkoutSession? = null
    private val workoutRepository = WorkoutRepository.getInstance()
    private val apiService = NetworkModule.zeonApiService
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        @Volatile
        private var instance: WorkoutSessionRepository? = null
        fun getInstance(): WorkoutSessionRepository {
            return instance ?: synchronized(this) {
                instance ?: WorkoutSessionRepository().also { instance = it }
            }
        }
    }

    fun startWorkout(workout: Workout): WorkoutSession {
        val session = WorkoutSession(
            id = UUID.randomUUID().toString(),
            workoutId = workout.id,
            workout = workout,
            startTime = LocalDateTime.now(),
            elapsedTime = 0L,
            exerciseLogs = mutableListOf(),
            isActive = true,
            isPaused = false
        )
        currentSession = session
        return session
    }

    fun getCurrentSession(): WorkoutSession? {
        return currentSession
    }

    fun pauseWorkout() {
        currentSession?.let {
            it.isPaused = true
            it.pauseStartTime = LocalDateTime.now()
        }
    }

    fun resumeWorkout() {
        currentSession?.let {
            it.isPaused = false
            it.pauseStartTime?.let { pauseStart ->
                it.totalPausedSeconds += java.time.Duration.between(pauseStart, LocalDateTime.now()).seconds
            }
            it.pauseStartTime = null
        }
    }

    fun getElapsedSeconds(): Long {
        val session = currentSession ?: return 0
        val totalSeconds = java.time.Duration.between(session.startTime, LocalDateTime.now()).seconds
        val currentPause = if (session.isPaused && session.pauseStartTime != null) {
            java.time.Duration.between(session.pauseStartTime, LocalDateTime.now()).seconds
        } else {
            0L
        }
        return maxOf(0, totalSeconds - session.totalPausedSeconds - currentPause)
    }

    fun updateElapsedTime(seconds: Long) {
        currentSession?.elapsedTime = seconds
    }

    fun addExerciseLog(exerciseLog: ExerciseLog) {
        currentSession?.exerciseLogs?.add(exerciseLog)
    }

    fun removeExerciseLogsForExercise(exerciseId: String) {
        currentSession?.exerciseLogs?.removeAll { it.exerciseId == exerciseId }
    }

    fun completeWorkout(
        context: Context,
        overallComment: String = "",
        difficulty: DifficultyLevel,
        callback: (WorkoutLog?) -> Unit
    ) {
        val session = currentSession ?: run { callback(null); return }

        val workoutLog = WorkoutLog(
            id = UUID.randomUUID().toString(),
            workoutId = session.workoutId,
            completedDate = LocalDateTime.now(),
            exerciseLogs = session.exerciseLogs.toList(),
            overallComment = overallComment,
            difficulty = difficulty,
            durationSeconds = session.elapsedTime
        )

        if (session.exerciseLogs.isEmpty()) {
            workoutRepository.saveWorkoutLog(workoutLog)
            session.isActive = false
            currentSession = null
            callback(workoutLog)
            return
        }

        val logsWithFeedback = session.exerciseLogs.filter { it.comment.isNotBlank() }

        if (logsWithFeedback.isEmpty()) {
            workoutRepository.saveWorkoutLog(workoutLog)
            session.isActive = false
            currentSession = null
            callback(workoutLog)
            return
        }

        val remaining = AtomicInteger(logsWithFeedback.size)
        val hasError = AtomicInteger(0)

        val dbWorkoutId = workoutRepository.getDbWorkoutId(session.workoutId) ?: 0

        for (log in logsWithFeedback) {
            val request = FeedbackTrainingRequest(
                exerciseId = log.exerciseId.toIntOrNull() ?: 0,
                workoutId = dbWorkoutId,
                completedSets = log.completedSets,
                completedReps = log.completedReps,
                completedWeight = log.weight,
                message = log.comment
            )

            apiService.submitFeedback(request).enqueue(object : Callback<FeedbackTrainingResponse> {
                override fun onResponse(
                    call: Call<FeedbackTrainingResponse>,
                    response: Response<FeedbackTrainingResponse>
                ) {
                    if (!response.isSuccessful) {
                        hasError.incrementAndGet()
                    }
                    if (remaining.decrementAndGet() == 0) {
                        finalizeCompletion(workoutLog, session, hasError.get() > 0, callback)
                    }
                }

                override fun onFailure(call: Call<FeedbackTrainingResponse>, t: Throwable) {
                    hasError.incrementAndGet()
                    if (remaining.decrementAndGet() == 0) {
                        finalizeCompletion(workoutLog, session, hasError.get() > 0, callback)
                    }
                }
            })
        }
    }

    private fun finalizeCompletion(
        workoutLog: WorkoutLog,
        session: WorkoutSession,
        hadErrors: Boolean,
        callback: (WorkoutLog?) -> Unit
    ) {
        workoutRepository.saveWorkoutLog(workoutLog)
        session.isActive = false
        currentSession = null
        mainHandler.post { callback(workoutLog) }
    }

    fun fetchExerciseStats(
        clientId: Int,
        exerciseId: Int,
        callback: (lastWeight: Double?, avgReps: Double?, personalBest: Double?) -> Unit
    ) {
        apiService.getClientFeedback(clientId)
            .enqueue(object : Callback<List<FeedbackTrainingResponse>> {
                override fun onResponse(
                    call: Call<List<FeedbackTrainingResponse>>,
                    response: Response<List<FeedbackTrainingResponse>>
                ) {
                    if (response.isSuccessful) {
                        val allFeedback = response.body() ?: emptyList()
                        val history = allFeedback.filter { it.exerciseId == exerciseId }
                        if (history.isEmpty()) {
                            mainHandler.post { callback(null, null, null) }
                            return
                        }
                        val lastWeight = history.firstOrNull()?.completedWeight
                        val avgReps = history.map { it.completedReps }.average()
                        val personalBest = history.maxOfOrNull { it.completedWeight }
                        mainHandler.post { callback(lastWeight, avgReps, personalBest) }
                    } else {
                        mainHandler.post { callback(null, null, null) }
                    }
                }

                override fun onFailure(call: Call<List<FeedbackTrainingResponse>>, t: Throwable) {
                    mainHandler.post { callback(null, null, null) }
                }
            })
    }

    fun getExerciseHistory(exerciseId: String): List<ExerciseLog> {
        return workoutRepository.getWorkoutLogs()
            .flatMap { it.exerciseLogs }
            .filter { it.exerciseId == exerciseId }
            .sortedByDescending { it.timestamp }
    }

    fun getPersonalBest(exerciseId: String): Double? {
        return getExerciseHistory(exerciseId).maxOfOrNull { it.weight }
    }

    fun getAverageReps(exerciseId: String): Double? {
        val history = getExerciseHistory(exerciseId)
        if (history.isEmpty()) return null
        return history.map { it.completedReps }.average()
    }

    fun getLastWeight(exerciseId: String): Double? {
        return getExerciseHistory(exerciseId).firstOrNull()?.weight
    }
}
