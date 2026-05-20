package com.example.zeon.data.model

import java.util.Date

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val password: String,
    val profileImage: String,
    val birthDate: Date,
    val height: Float,
    val weight: Float,
    val gender: String? = null,
    val trainerId: Int? = null
)

