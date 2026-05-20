package com.example.zeon.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zeon.data.model.FoodFoodDiary
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.model.Goals
import com.example.zeon.data.model.MealType
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.helpers.NutritionCalculator
import com.example.zeon.helpers.NutritionSummary
import kotlinx.coroutines.launch
import java.time.LocalDate

class FoodViewModel(
    private val foodRepository: FoodRepository,
    private val nutritionCalculator: NutritionCalculator
) : ViewModel() {
    private val _allFoods = MutableLiveData<List<FoodItem>>()
    private val _waterAmount = MutableLiveData<Float>()
    val waterAmount: LiveData<Float> = _waterAmount
    val allFoods: LiveData<List<FoodItem>> = _allFoods

    private val _breakfastEntries = MutableLiveData<List<FoodFoodDiary>>()
    val breakfastEntries: LiveData<List<FoodFoodDiary>> = _breakfastEntries

    private val _lunchEntries = MutableLiveData<List<FoodFoodDiary>>()
    val lunchEntries: LiveData<List<FoodFoodDiary>> = _lunchEntries

    private val _dinnerEntries = MutableLiveData<List<FoodFoodDiary>>()
    val dinnerEntries: LiveData<List<FoodFoodDiary>> = _dinnerEntries

    private val _snackEntries = MutableLiveData<List<FoodFoodDiary>>()
    val snackEntries: LiveData<List<FoodFoodDiary>> = _snackEntries

    private val _otherMealEntries = MutableLiveData<List<FoodFoodDiary>>()
    val otherMealEntries: LiveData<List<FoodFoodDiary>> = _otherMealEntries

    private val _nutritionSummary = MutableLiveData<NutritionSummary>()
    val nutritionSummary: LiveData<NutritionSummary> = _nutritionSummary

    private val _caloriesPercent = MutableLiveData<Float>()
    val caloriesPercent: LiveData<Float> = _caloriesPercent

    private val _proteinPercent = MutableLiveData<Float>()
    val proteinPercent: LiveData<Float> = _proteinPercent

    private val _carbsPercent = MutableLiveData<Float>()
    val carbsPercent: LiveData<Float> = _carbsPercent

    private val _fatPercent = MutableLiveData<Float>()
    val fatPercent: LiveData<Float> = _fatPercent

    private val _waterPercent = MutableLiveData<Float>()
    val waterPercent: LiveData<Float> = _waterPercent

    private val _userGoals = MutableLiveData<Goals>()
    val userGoals: LiveData<Goals> = _userGoals

    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _foodAddedSuccess = MutableLiveData<Boolean>()
    val foodAddedSuccess: LiveData<Boolean> = _foodAddedSuccess

    private val _foodRemovedSuccess = MutableLiveData<Boolean>()
    val foodRemovedSuccess: LiveData<Boolean> = _foodRemovedSuccess

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllFoods()
        _userGoals.value=foodRepository.getUserGoals()
    }

    fun loadAllFoods() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = foodRepository.getAllFoods()
            result.onSuccess { foods ->
                _allFoods.value = foods
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun loadDiaryData() {
        viewModelScope.launch {
            val currentDate = _selectedDate.value ?: LocalDate.now()

            val result = foodRepository.getFoodDiaryForDate(
                date = currentDate
            )

            result.onSuccess { entries ->
                val grouped = foodRepository.getEntriesGroupedByMealType(currentDate)

                _breakfastEntries.value = grouped[MealType.DORUČAK] ?: emptyList()
                _lunchEntries.value = grouped[MealType.RUČAK] ?: emptyList()
                _dinnerEntries.value = grouped[MealType.VEČERA] ?: emptyList()
                _snackEntries.value = grouped[MealType.UŽINA] ?: emptyList()
                _otherMealEntries.value = grouped[MealType.OSTALO] ?: emptyList()

                updateProgressCard()
            }
            result.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
    private fun updateProgressCard() {
        val currentDate = _selectedDate.value ?: LocalDate.now()
        val goals = foodRepository.getUserGoals()

        val serverItems = foodRepository.getCurrentFoodDiaryItems()

        val nutrition = nutritionCalculator.calculateNutritionFromServerData(serverItems)
        _nutritionSummary.value = nutrition

        val water = nutritionCalculator.calculateWaterFromServerItems(serverItems)
        _waterAmount.value = water
        foodRepository.setTodayWater(water)

        _caloriesPercent.value = nutritionCalculator.getCaloriesPercent(nutrition, goals)
        _proteinPercent.value = nutritionCalculator.getProteinPercent(nutrition, goals)
        _carbsPercent.value = nutritionCalculator.getCarbsPercent(nutrition, goals)
        _fatPercent.value = nutritionCalculator.getFatPercent(nutrition, goals)
        _waterPercent.value = nutritionCalculator.getWaterPercent(water, goals.goalWater.toFloat())
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        loadDiaryData()
    }

    fun previousDay() {
        val currentDate = _selectedDate.value ?: LocalDate.now()
        setSelectedDate(currentDate.minusDays(1))
    }

    fun nextDay() {
        val currentDate = _selectedDate.value ?: LocalDate.now()
        if (currentDate.isBefore(LocalDate.now())) {
            setSelectedDate(currentDate.plusDays(1))
        }
    }

    fun canGoToNextDay(): Boolean {
        val currentDate = _selectedDate.value ?: LocalDate.now()
        return currentDate.isBefore(LocalDate.now())
    }

   fun addFoodToDiary(foodId: Int, amount: Float, mealType: MealType) {
       viewModelScope.launch {
           val currentDate = _selectedDate.value ?: LocalDate.now()
           val result = foodRepository.addFoodToDiary(
               foodId = foodId,
               amount = amount,
               mealType = mealType,
               date = currentDate
           )
           result.onSuccess {
               _foodAddedSuccess.value = true
               loadDiaryData()
               _isLoading.value = false
           }
           result.onFailure { exception ->
               _error.value = exception.message
               _isLoading.value = false
           }
       }
   }

    fun removeFoodFromDiary(foodId: Int, mealType: MealType) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val currentDate = _selectedDate.value ?: LocalDate.now()

            val result = foodRepository.removeFoodFromDiary(
                foodId = foodId,
                date = currentDate
            )

            result.onSuccess {
                _foodRemovedSuccess.value = true
                loadDiaryData()
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun getMealFoods(mealType: MealType): List<FoodItem> {
        val currentDate = _selectedDate.value ?: LocalDate.now()
        val entries = foodRepository.getEntriesForDate(currentDate)

        return entries
            .filter { it.mealType == mealType }
            .mapNotNull { entry ->
                foodRepository.getFoodByIdLocal(entry.foodId)
            }
    }

    fun getMealCalories(mealType: MealType): Float {
        val currentDate = _selectedDate.value ?: LocalDate.now()
        val entries = foodRepository.getEntriesForDate(currentDate)

        var totalCalories = 0f
        entries
            .filter { it.mealType == mealType }
            .forEach { entry ->
                val food = foodRepository.getFoodByIdLocal(entry.foodId)
                if (food != null) {
                    totalCalories += food.calories * (entry.amount / 100f)
                }
            }

        return totalCalories
    }

}