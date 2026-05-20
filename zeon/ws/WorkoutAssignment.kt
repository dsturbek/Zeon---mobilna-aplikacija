package com.example.zeon.ws

import com.google.gson.annotations.SerializedName

data class WorkoutAssignment(
    @SerializedName("id_workout_assigned")
    val idWorkoutAssigned: Int,

    @SerializedName("workout_assigned_name")
    val workoutAssignedName: String,

    @SerializedName("date_start")
    val dateStart: String,

    @SerializedName("date_end")
    val dateEnd: String,

    @SerializedName("ClientId")
    val clientId: Int,

    @SerializedName("Workout_plan_templateId")
    val workoutPlanTemplateId: Int,

    @SerializedName("workout_template_name")
    val workoutTemplateName: String,

    @SerializedName("TrainerId")
    val trainerId: Int
)

data class WorkoutDetailsResponse(
    @SerializedName("id_workout_assigned") val id: Int,
    @SerializedName("workout_assigned_name") val name: String,
    @SerializedName("workouts") val workouts: List<DetailedWorkout>
)

data class DetailedWorkout(
    @SerializedName("id_workout") val workoutId: Int? = null,
    @SerializedName("workout_plan_day") val workoutPlanDay: Int,
    @SerializedName("workout_name") val workoutName: String,
    @SerializedName("muscle_group") val muscleGroup: String,
    @SerializedName("exercises") val exercises: List<DetailedExercise>
)

data class DetailedExercise(
    @SerializedName("id_exercise") val exerciseId: Int,
    @SerializedName("exercise_name") val name: String,
    @SerializedName("muscle") val muscle: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("sets") val sets: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("weight") val weight: Double
)
