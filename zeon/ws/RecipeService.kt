package com.example.zeon.ws

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeService {
    @GET("search.php")
    fun searchRecipes(@Query("s") query: String): Call<RecipeResponse>

    @GET("lookup.php")
    fun getRecipeById(@Query("i") id: String): Call<RecipeResponse>
}