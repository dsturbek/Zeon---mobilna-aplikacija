package com.example.zeon.data.model

import java.time.LocalDate

data class WorkoutFeedback(
    val id: Int,
    val completedSets: Int,
    val completedReps: Int,
    val completedWeight: Double,
    val exerciseId: Int,
    val workoutId: Int,
    val completedDate: LocalDate,
    val message: String,
    val exerciseName: String,
    val workoutName: String,
    val muscle: String
)
