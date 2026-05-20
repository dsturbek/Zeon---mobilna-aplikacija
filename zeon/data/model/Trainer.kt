package com.example.zeon.data.model

data class Trainer (
    val id: Int,
    val name: String,
    val specialization: String,
    val experience: Int,
    val rating: Double,
    val price: Double,
    val imageRes: Int? = null,
    val imageUrl: String? = null
)