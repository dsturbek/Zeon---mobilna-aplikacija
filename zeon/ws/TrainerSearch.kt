package com.example.zeon.ws

import com.google.gson.annotations.SerializedName

data class TrainerDto(
    @SerializedName("id_trainer") val idTrainer: Int,
    @SerializedName("name_surname") val nameSurname: String,
    @SerializedName("specialization") val specialization: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("rating") val rating: Double?,
)