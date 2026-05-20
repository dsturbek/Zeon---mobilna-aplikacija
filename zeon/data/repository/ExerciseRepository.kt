package com.example.zeon.data.repository

import android.os.Handler
import android.os.Looper
import com.example.zeon.data.model.Exercise
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.response.ExerciseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExerciseRepository private constructor() {
    private val apiService = NetworkModule.zeonApiService
    private val mainHandler = Handler(Looper.getMainLooper())
    private val exerciseCache = mutableMapOf<String, Exercise>()

    companion object {
        @Volatile
        private var instance: ExerciseRepository? = null
        fun getInstance(): ExerciseRepository {
            return instance ?: synchronized(this) {
                instance ?: ExerciseRepository().also { instance = it }
            }
        }
    }
    fun cacheExercise(exercise: Exercise) {
        exerciseCache[exercise.id] = exercise
    }

    fun getExerciseById(id: String): Exercise? {
        return exerciseCache[id]
    }

    fun getExerciseById(id: String, callback: (Exercise?) -> Unit) {
        val cached = exerciseCache[id]
        if (cached != null) {
            callback(cached)
            return
        }

        val numericId = id.toIntOrNull()
        if (numericId == null) {
            callback(null)
            return
        }

        apiService.getExerciseById(numericId).enqueue(object : Callback<ExerciseResponse> {
            override fun onResponse(
                call: Call<ExerciseResponse>,
                response: Response<ExerciseResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val exercise = Exercise(
                            id = body.id.toString(),
                            name = body.name,
                            description = "",
                            muscleGroup = body.muscle,
                            videoUrl = body.videoUrl
                        )
                        exerciseCache[exercise.id] = exercise
                        mainHandler.post { callback(exercise) }
                    } else {
                        mainHandler.post { callback(null) }
                    }
                } else {
                    mainHandler.post { callback(null) }
                }
            }

            override fun onFailure(call: Call<ExerciseResponse>, t: Throwable) {
                mainHandler.post { callback(null) }
            }
        })
    }
}
