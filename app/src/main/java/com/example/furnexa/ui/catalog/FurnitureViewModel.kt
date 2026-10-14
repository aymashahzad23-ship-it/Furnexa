package com.example.furnexa.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furnexa.data.Furniture
import com.example.furnexa.data.FurnitureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CatalogUiState {
    object Loading : CatalogUiState
    data class Success(
        val items: List<Furniture>,
        val selectedCategory: String = "All",
        val searchQuery: String = ""
    ) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class FurnitureViewModel(
    private val repository: FurnitureRepository = FurnitureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var allFurniture: List<Furniture> = emptyList()

    init {
        loadFurniture()
    }

    fun loadFurniture() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repository.getFurnitureList().fold(
                onSuccess = { list ->
                    allFurniture = list
                    _uiState.value = CatalogUiState.Success(items = list)
                },
                onFailure = { err ->
                    _uiState.value = CatalogUiState.Error(err.localizedMessage ?: "Failed to load catalogue.")
                }
            )
        }
    }

    fun filterByCategory(category: String) {
        val currentState = _uiState.value
        if (currentState is CatalogUiState.Success) {
            val filtered = if (category == "All") {
                allFurniture
            } else {
                allFurniture.filter { it.category.equals(category, ignoreCase = true) }
            }
            val searchFiltered = if (currentState.searchQuery.isBlank()) {
                filtered
            } else {
                filtered.filter {
                    it.name.contains(currentState.searchQuery, ignoreCase = true) ||
                            it.description.contains(currentState.searchQuery, ignoreCase = true)
                }
            }
            _uiState.value = currentState.copy(items = searchFiltered, selectedCategory = category)
        }
    }

    fun search(query: String) {
        val currentState = _uiState.value
        if (currentState is CatalogUiState.Success) {
            val categoryFiltered = if (currentState.selectedCategory == "All") {
                allFurniture
            } else {
                allFurniture.filter { it.category.equals(currentState.selectedCategory, ignoreCase = true) }
            }
            val searchFiltered = if (query.isBlank()) {
                categoryFiltered
            } else {
                categoryFiltered.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = currentState.copy(items = searchFiltered, searchQuery = query)
        }
    }

    fun clearFilters() {
        val currentState = _uiState.value
        if (currentState is CatalogUiState.Success) {
            _uiState.value = currentState.copy(
                items = allFurniture,
                selectedCategory = "All",
                searchQuery = ""
            )
        }
    }
}
