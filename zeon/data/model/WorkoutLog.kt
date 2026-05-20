package com.example.zeon.data.model
import java.time.LocalDateTime
enum class DifficultyLevel {
    EASY, MEDIUM, HARD
}
data class WorkoutLog(
    val id: String,
    val workoutId: String,
    val completedDate: LocalDateTime,
    val exerciseLogs: List<ExerciseLog>,
    val overallComment: String = "",
    val difficulty: DifficultyLevel,
    val durationSeconds: Long = 0L
)