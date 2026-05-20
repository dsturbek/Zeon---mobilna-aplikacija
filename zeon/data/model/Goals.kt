package com.example.zeon.data.model

data class Goals(
    val goalId:Int,
    val goalName: String,
    val goalDescription: String,
    val goalCalories: Int,
    val goalWater: Int,
    val clientId: Int,
    val goalProtein: Float,
    val goalCarbs: Float,
    val goalFat: Float
)
