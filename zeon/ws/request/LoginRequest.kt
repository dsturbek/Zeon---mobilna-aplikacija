package com.example.zeon.ws.request

data class LoginRequest(
    val username: String,
    val password: String,
    val role: String = "client"
)