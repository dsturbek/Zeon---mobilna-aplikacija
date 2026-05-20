package com.example.zeon.data.model

import java.time.LocalDateTime

data class WorkoutSession(
    val id: String,
    val workoutId: String,
    val workout: Workout,
    val startTime: LocalDateTime,
    var elapsedTime: Long = 0L,
    val exerciseLogs: MutableList<ExerciseLog> = mutableListOf(),
    var isActive: Boolean = true,
    var isPaused: Boolean = false,
    var totalPausedSeconds: Long = 0L,
    var pauseStartTime: LocalDateTime? = null
) {
    fun getCompletedExercisesCount(): Int {
        return exerciseLogs.distinctBy { it.exerciseId }.size
    }

    fun getTotalExercisesCount(): Int {
        return workout.exercises.size
    }

    fun getExerciseLogsForExercise(exerciseId: String): List<ExerciseLog> {
        return exerciseLogs.filter { it.exerciseId == exerciseId }
    }
}
