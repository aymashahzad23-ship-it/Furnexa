package com.example.furnexa.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furnexa.data.Furniture
import com.example.furnexa.data.FurnitureRepository
import com.example.furnexa.data.RoomConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecommendationUiState {
    object Loading : RecommendationUiState
    data class Success(
        val roomConfig: RoomConfig,
        val recommendedItems: List<Furniture>,
        val selectedItemIds: Set<Int> = emptySet(),
        val totalCost: Int = 0,
        val remainingBudget: Int,
        val isOverBudget: Boolean = false,
        val styleMatchMessage: String = ""
    ) : RecommendationUiState
    data class Error(val message: String) : RecommendationUiState
}

class RecommendationViewModel(
    private val repository: FurnitureRepository = FurnitureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private var allFurniture: List<Furniture> = emptyList()

    fun loadRecommendations(config: RoomConfig) {
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            repository.getFurnitureList().fold(
                onSuccess = { list ->
                    allFurniture = list
                    processRecommendations(config, list)
                },
                onFailure = { err ->
                    _uiState.value = RecommendationUiState.Error(err.localizedMessage ?: "Failed to load furniture.")
                }
            )
        }
    }

    private fun processRecommendations(config: RoomConfig, list: List<Furniture>) {
        val roomTypeMatched = list.filter {
            it.roomType.equals(config.roomType, ignoreCase = true) ||
                    it.category.contains(config.roomType, ignoreCase = true)
        }

        val baseList = if (roomTypeMatched.isNotEmpty()) roomTypeMatched else list

        val styleMatched = baseList.filter {
            it.style.equals(config.interiorStyle, ignoreCase = true)
        }

        val recommendations: List<Furniture>
        val message: String

        if (styleMatched.isNotEmpty()) {
            recommendations = styleMatched
            message = "Showing ${config.interiorStyle} furniture tailored for your ${config.roomType}."
        } else {
            recommendations = baseList
            message = "No exact ${config.interiorStyle} style match found for ${config.roomType}. Showing broader alternatives."
        }

        val budget = config.budgetPkr.toIntOrNull() ?: 0
        _uiState.value = RecommendationUiState.Success(
            roomConfig = config,
            recommendedItems = recommendations,
            selectedItemIds = emptySet(),
            totalCost = 0,
            remainingBudget = budget,
            isOverBudget = false,
            styleMatchMessage = message
        )
    }

    fun toggleItemSelection(item: Furniture) {
        val state = _uiState.value
        if (state is RecommendationUiState.Success) {
            val newSelection = if (state.selectedItemIds.contains(item.id)) {
                state.selectedItemIds - item.id
            } else {
                state.selectedItemIds + item.id
            }

            val totalCost = state.recommendedItems
                .filter { newSelection.contains(it.id) }
                .sumOf { it.pricePkr }

            val budget = state.roomConfig.budgetPkr.toIntOrNull() ?: 0
            val remaining = budget - totalCost
            val isOver = totalCost > budget

            _uiState.value = state.copy(
                selectedItemIds = newSelection,
                totalCost = totalCost,
                remainingBudget = remaining,
                isOverBudget = isOver
            )
        }
    }
}
