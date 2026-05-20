package com.example.zeon.data.model

enum class MealType {
    DORUČAK,
    RUČAK,
    VEČERA,
    UŽINA,
    TEKUĆINA,
    OSTALO;

    override fun toString(): String {
        return name
    }

}