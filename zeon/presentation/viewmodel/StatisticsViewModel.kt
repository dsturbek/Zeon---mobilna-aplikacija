package com.example.zeon.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zeon.data.model.MuscleProgress
import com.example.zeon.data.model.PersonalRecord
import com.example.zeon.data.model.StatisticsSummary
import com.example.zeon.data.model.StreakData
import com.example.zeon.data.repository.StatisticsRepository
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {

    private val repository = StatisticsRepository.getInstance()

    private val _statisticsState = MutableLiveData<StatisticsUiState>()
    val statisticsState: LiveData<StatisticsUiState> = _statisticsState

    private val _personalRecords = MutableLiveData<List<PersonalRecord>>()

    private val _streakData = MutableLiveData<StreakData>()

    private val _muscleProgress = MutableLiveData<List<MuscleProgress>>()

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadStatistics(clientId: Int) {
        _statisticsState.value = StatisticsUiState.Loading

        viewModelScope.launch {
            val result = repository.getStatisticsSummary(clientId)

            result.onSuccess { summary ->
                _statisticsState.value = StatisticsUiState.Success(summary)

                _personalRecords.value = summary.personalRecords
                _muscleProgress.value = summary.muscleProgress
                _streakData.value = StreakData(
                    currentStreak = summary.currentStreak,
                    longestStreak = summary.longestStreak,
                    totalWorkoutDays = summary.totalWorkoutDays
                )
            }.onFailure { exception ->
                _statisticsState.value = StatisticsUiState.Error(
                    exception.message ?: "Failed to load statistics"
                )
                _errorMessage.value = exception.message
            }
        }
    }

    fun refreshStatistics(clientId: Int) {
        _isRefreshing.value = true

        viewModelScope.launch {
            val result = repository.getStatisticsSummary(clientId)

            result.onSuccess { summary ->
                _statisticsState.value = StatisticsUiState.Success(summary)
                _personalRecords.value = summary.personalRecords
                _muscleProgress.value = summary.muscleProgress
                _streakData.value = StreakData(
                    currentStreak = summary.currentStreak,
                    longestStreak = summary.longestStreak,
                    totalWorkoutDays = summary.totalWorkoutDays
                )
            }.onFailure { exception ->
                _errorMessage.value = exception.message
            }

            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val summary: StatisticsSummary) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}
