package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class FeedbackTrainingResponse(
    @SerializedName("id_feedback") val id: Int,
    @SerializedName("feedback_message") val message: String?,
    @SerializedName("completed_sets") val completedSets: Int,
    @SerializedName("completed_reps") val completedReps: Int,
    @SerializedName("completed_weight") val completedWeight: Double,
    @SerializedName("ClientId") val clientId: Int,
    @SerializedName("Exercise_WorkoutExerciseId") val exerciseId: Int,
    @SerializedName("Exercise_WorkoutWorkoutId") val workoutId: Int,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("exercise_name") val exerciseName: String? = null,
    @SerializedName("workout_name") val workoutName: String? = null,
    @SerializedName("muscle") val muscle: String? = null
)
