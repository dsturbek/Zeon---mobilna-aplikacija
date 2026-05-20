package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserDTO
)