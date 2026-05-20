package com.example.zeon.data.model

import java.time.LocalDate

data class FoodDiary(
    val foodDiaryId: Int,
    val foodDairyDate: LocalDate,
    val clientId: Int,
)
