package com.example.zeon.ws.response

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("details")
    val details: String?
)