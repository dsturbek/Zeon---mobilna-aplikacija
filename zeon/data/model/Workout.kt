package com.example.zeon.data.model

import java.time.LocalDate
import java.time.LocalTime

data class Workout(
    val id: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val numberOfExercises: Int,
    val muscleGroup: String,
    val exercises : List<WorkoutExercise> = emptyList()
)