package com.example.zeon.ws

data class Recipe(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String,
    val strMealThumb: String,
    val strYoutube: String?,

    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,

    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?,
    val strMeasure16: String?,
    val strMeasure17: String?,
    val strMeasure18: String?,
    val strMeasure19: String?,
    val strMeasure20: String?,

) {
    fun getIngredients(): List<Pair<String, String>> {
        val ingredients = mutableListOf<Pair<String, String>>()

        listOf(
            Pair(strIngredient1, strMeasure1),
            Pair(strIngredient2, strMeasure2),
            Pair(strIngredient3, strMeasure3),
            Pair(strIngredient4, strMeasure4),
            Pair(strIngredient5, strMeasure5),
            Pair(strIngredient6, strMeasure6),
            Pair(strIngredient7, strMeasure7),
            Pair(strIngredient8, strMeasure8),
            Pair(strIngredient9, strMeasure9),
            Pair(strIngredient10, strMeasure10),
            Pair(strIngredient11, strMeasure11),
            Pair(strIngredient12, strMeasure12),
            Pair(strIngredient13, strMeasure13),
            Pair(strIngredient14, strMeasure14),
            Pair(strIngredient15, strMeasure15),
            Pair(strIngredient16, strMeasure16),
            Pair(strIngredient17, strMeasure17),
            Pair(strIngredient18, strMeasure18),
            Pair(strIngredient19, strMeasure19),
            Pair(strIngredient20, strMeasure20),
        ).forEach { (ingredient, measure) ->
            if (!ingredient.isNullOrBlank()) {
                ingredients.add(Pair(ingredient, measure ?: ""))
            }
        }
        return ingredients
    }
}