package com.example.zeon.ws

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WsRecipes {
    const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val recipeService: RecipeService = instance.create(RecipeService::class.java)
}