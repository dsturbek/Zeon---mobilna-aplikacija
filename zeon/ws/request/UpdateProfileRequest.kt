package com.example.zeon.ws.request

data class UpdateProfileRequest(
    val name_surname: String?,
    val email: String?,
    val username: String?,
    val height: Float?,
    val weight: Float?,
    val birthDate: String?,
    val gender: String?
)