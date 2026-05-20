package com.example.zeon.data.model

import java.time.LocalDate

data class PersonalRecord(
    val id: Int,
    val maxWeight: Double,
    val maxReps: Int,
    val maxVolume: Double,
    val dateAchievement: LocalDate,
    val exerciseId: Int,
    val exerciseName: String,
    val muscle: String
)
