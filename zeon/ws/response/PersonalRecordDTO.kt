package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class PersonalRecordDTO(
    @SerializedName("id_pr")
    val id: Int,

    @SerializedName("max_weight")
    val maxWeight: Double?,

    @SerializedName("max_reps")
    val maxReps: Int?,

    @SerializedName("max_volument")
    val maxVolume: Double?,

    @SerializedName("date_achievement")
    val dateAchievement: String?,

    @SerializedName("Client_profileId")
    val clientProfileId: Int?,

    @SerializedName("ExerciseId")
    val exerciseId: Int,

    @SerializedName("exercise_name")
    val exerciseName: String? = null,

    @SerializedName("muscle")
    val muscle: String? = null
)
