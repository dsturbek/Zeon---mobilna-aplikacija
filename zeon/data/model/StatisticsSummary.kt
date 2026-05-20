package com.example.zeon.data.model

data class StatisticsSummary(
    val totalWorkouts: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val personalRecords: List<PersonalRecord>,
    val muscleProgress: List<MuscleProgress>,
    val weeklyFrequency: Double,
    val totalWorkoutDays: Int
)

data class MuscleProgress(
    val muscleGroup: String,
    val progressPercent: Double,
    val currentWeight: Double,
    val initialWeight: Double,
    val exerciseCount: Int
)

data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalWorkoutDays: Int
)
