package com.example.zeon.ws.request

data class RegisterClientRequest(
    val name_surname: String,
    val email: String,
    val username: String,
    val password: String,
    val TrainerId: Int? = null
)