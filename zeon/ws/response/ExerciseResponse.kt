package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class ExerciseResponse(
    @SerializedName("id_exercise") val id: Int,
    @SerializedName("exercise_name") val name: String,
    @SerializedName("muscle") val muscle: String,
    @SerializedName("video_url") val videoUrl: String
)
