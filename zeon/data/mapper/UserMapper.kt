package com.example.zeon.data.mapper

import com.example.zeon.ws.response.UserDTO
import com.example.zeon.data.model.User
import java.text.SimpleDateFormat
import java.util.*

object UserMapper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun UserDTO.toUser(): User {
        return User(
            id = this.id,
            email = this.email,
            username = this.username,
            password = "",
            profileImage = this.profileImage ?: "",
            birthDate = try {
                if (this.birthDate != null) {
                    dateFormat.parse(this.birthDate) ?: Date()
                } else {
                    Date()
                }
            } catch (e: Exception) {
                Date()
            },
            height = this.height ?: 0f,
            weight = this.weight ?: 0f,
            trainerId = this.trainerId,
        )
    }

    fun Date.toApiString(): String {
        return dateFormat.format(this)
    }
}