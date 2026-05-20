package com.example.zeon.data.model

data class WorkoutExercise(
    val id: String,
    val exerciseId: String,
    val exercise: Exercise,
    val plannedSets: Int,
    val plannedReps: Int,
    val plannedWeight: Double = 0.0
)