package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class UserDTO(
    @SerializedName("id_client")
    val id: Int,

    @SerializedName("name_surname")
    val nameSurname: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("role")
    val role: String?,

    @SerializedName("height")
    val height: Float?,

    @SerializedName("weight")
    val weight: Float?,

    @SerializedName("birthDate")
    val birthDate: String?,

    @SerializedName("profileImage")
    val profileImage: String?,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("TrainerId")
    val trainerId: Int?
)