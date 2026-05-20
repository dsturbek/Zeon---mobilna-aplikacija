package com.example.zeon.ws.request

import com.google.gson.annotations.SerializedName

data class FeedbackTrainingRequest(
    @SerializedName("exerciseId") val exerciseId: Int,
    @SerializedName("workoutId") val workoutId: Int,
    @SerializedName("completed_sets") val completedSets: Int,
    @SerializedName("completed_reps") val completedReps: Int,
    @SerializedName("completed_weight") val completedWeight: Double,
    @SerializedName("feedback_message") val message: String
)
