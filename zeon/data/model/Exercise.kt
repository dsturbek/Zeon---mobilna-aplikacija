package com.example.zeon.data.model

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val muscleGroup: String,
    val videoUrl: String,
    val gifUrl: String? = null
)