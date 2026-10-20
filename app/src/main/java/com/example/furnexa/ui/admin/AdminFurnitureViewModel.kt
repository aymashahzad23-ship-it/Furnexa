package com.example.furnexa.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furnexa.data.Furniture
import com.example.furnexa.data.FurnitureInput
import com.example.furnexa.data.FurnitureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUiState {
    object Loading : AdminUiState
    data class Success(
        val items: List<Furniture>,
        val totalCount: Int = 0
    ) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

class AdminFurnitureViewModel(
    private val repository: FurnitureRepository = FurnitureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadFurniture()
    }

    fun loadFurniture() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            repository.getFurnitureList().fold(
                onSuccess = { list ->
                    _uiState.value = AdminUiState.Success(items = list, totalCount = list.size)
                },
                onFailure = { err ->
                    _uiState.value = AdminUiState.Error(err.localizedMessage ?: "Failed to load furniture records.")
                }
            )
        }
    }

    fun addFurniture(
        name: String,
        category: String,
        description: String,
        priceStr: String,
        widthStr: String,
        depthStr: String,
        heightStr: String,
        style: String,
        colour: String,
        roomType: String,
        imageUrl: String,
        model3dUrl: String?,
        onSuccess: () -> Unit
    ) {
        val price = priceStr.toIntOrNull()
        val width = widthStr.toDoubleOrNull()
        val depth = depthStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()

        if (name.isBlank() || category.isBlank() || description.isBlank()) {
            _operationMessage.value = "Name, category, and description are required."
            return
        }
        if (price == null || price < 0) {
            _operationMessage.value = "Price must be a valid non-negative number."
            return
        }
        if (width == null || width <= 0 || depth == null || depth <= 0 || height == null || height <= 0) {
            _operationMessage.value = "Dimensions must be valid positive numbers."
            return
        }

        viewModelScope.launch {
            val input = FurnitureInput(
                name = name.trim(),
                category = category.trim(),
                description = description.trim(),
                pricePkr = price,
                widthCm = width,
                depthCm = depth,
                heightCm = height,
                style = style.trim(),
                colour = colour.trim(),
                roomType = roomType.trim(),
                imageUrl = imageUrl.trim(),
                model3dUrl = model3dUrl?.trim()?.takeIf { it.isNotBlank() }
            )

            repository.insertFurniture(input).fold(
                onSuccess = {
                    _operationMessage.value = "Furniture added successfully!"
                    loadFurniture()
                    onSuccess()
                },
                onFailure = { err ->
                    _operationMessage.value = err.localizedMessage ?: "Failed to add furniture. Ensure you have admin permissions."
                }
            )
        }
    }

    fun updateFurniture(
        id: Int,
        name: String,
        category: String,
        description: String,
        priceStr: String,
        widthStr: String,
        depthStr: String,
        heightStr: String,
        style: String,
        colour: String,
        roomType: String,
        imageUrl: String,
        model3dUrl: String?,
        onSuccess: () -> Unit
    ) {
        val price = priceStr.toIntOrNull()
        val width = widthStr.toDoubleOrNull()
        val depth = depthStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()

        if (name.isBlank() || category.isBlank() || description.isBlank()) {
            _operationMessage.value = "Name, category, and description are required."
            return
        }
        if (price == null || price < 0) {
            _operationMessage.value = "Price must be a valid non-negative number."
            return
        }
        if (width == null || width <= 0 || depth == null || depth <= 0 || height == null || height <= 0) {
            _operationMessage.value = "Dimensions must be valid positive numbers."
            return
        }

        viewModelScope.launch {
            val input = FurnitureInput(
                name = name.trim(),
                category = category.trim(),
                description = description.trim(),
                pricePkr = price,
                widthCm = width,
                depthCm = depth,
                heightCm = height,
                style = style.trim(),
                colour = colour.trim(),
                roomType = roomType.trim(),
                imageUrl = imageUrl.trim(),
                model3dUrl = model3dUrl?.trim()?.takeIf { it.isNotBlank() }
            )

            repository.updateFurniture(id, input).fold(
                onSuccess = {
                    _operationMessage.value = "Furniture updated successfully!"
                    loadFurniture()
                    onSuccess()
                },
                onFailure = { err ->
                    _operationMessage.value = err.localizedMessage ?: "Failed to update furniture. Ensure you have admin permissions."
                }
            )
        }
    }

    fun deleteFurniture(id: Int) {
        viewModelScope.launch {
            repository.deleteFurniture(id).fold(
                onSuccess = {
                    _operationMessage.value = "Furniture deleted successfully!"
                    loadFurniture()
                },
                onFailure = { err ->
                    _operationMessage.value = err.localizedMessage ?: "Failed to delete furniture. Ensure you have admin permissions."
                }
            )
        }
    }

    fun clearMessage() {
        _operationMessage.value = null
    }
}
