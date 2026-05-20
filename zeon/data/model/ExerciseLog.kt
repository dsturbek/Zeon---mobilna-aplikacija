package com.example.zeon.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ExerciseLog(
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val completedSets: Int,
    val completedReps: Int,
    val weight: Double,
    val comment: String = "",
    val timestamp: LocalDateTime
)