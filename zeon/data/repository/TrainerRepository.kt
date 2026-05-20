package com.example.zeon.data.repository

import com.example.zeon.R
import com.example.zeon.data.MockDataLoader
import com.example.zeon.data.model.Trainer
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.TrainerDto
import retrofit2.Call

class TrainerRepository {

    private val apiService = NetworkModule.zeonApiService

    fun getMockTrainers(): List<Trainer> {
        return MockDataLoader.loadMockTrainers()
    }

    fun getAllTrainers(): Call<List<TrainerDto>> {
        return apiService.getAllTrainers()
    }

    fun getTrainerById(trainerId: Int): Call<TrainerDto> {
        return apiService.getTrainerById(trainerId)
    }

    fun mapTrainerFromDto(dto: TrainerDto, context: android.content.Context): Trainer {
        val imageName = "trainer_${dto.idTrainer}"
        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        val finalResId = if (resId != 0) resId else R.drawable.muscle_icon

        return Trainer(
            id = dto.idTrainer,
            name = dto.nameSurname,
            specialization = dto.specialization ?: " - ",
            experience = 0,
            rating = dto.rating ?: 0.0,
            price = dto.price ?: 0.0,
            imageRes = finalResId
        )
    }

    fun mapTrainersFromDto(trainersDto: List<TrainerDto>, context: android.content.Context): List<Trainer> {
        return trainersDto.map { dto ->
            val imageName = "trainer_${dto.idTrainer}"
            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

            val finalResId = if (resId != 0) resId else R.drawable.muscle_icon
            Trainer(
                id = dto.idTrainer,
                name = dto.nameSurname,
                specialization = dto.specialization ?: " - ",
                experience = 0,
                rating = dto.rating ?: 0.0,
                price = dto.price ?: 0.0,
                imageRes = finalResId
            )
        }
    }
}