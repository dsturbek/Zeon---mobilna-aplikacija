package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class GoalDTO(
    @SerializedName("id_goal")
    val goalId: Int,

    @SerializedName("goal_name")
    val goalName: String,

    @SerializedName("goal_description")
    val goalDescription: String?,

    @SerializedName("goal_cal")
    val goalCalories: Int?,

    @SerializedName("goal_water")
    val goalWater: Int?,

    @SerializedName("ClientId")
    val clientId: Int
)
