package com.example.furnexa.ui.room

import androidx.lifecycle.ViewModel
import com.example.furnexa.data.RoomConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomConfigViewModel : ViewModel() {
    private val _config = MutableStateFlow(RoomConfig())
    val config: StateFlow<RoomConfig> = _config.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateRoomName(name: String) {
        _config.value = _config.value.copy(roomName = name)
    }

    fun updateRoomType(type: String) {
        _config.value = _config.value.copy(roomType = type)
    }

    fun updateRoomShape(shape: String) {
        val current = _config.value
        if (shape == "Square") {
            _config.value = current.copy(roomShape = shape, widthCm = current.lengthCm)
        } else {
            _config.value = current.copy(roomShape = shape)
        }
    }

    fun updateLengthCm(length: String) {
        val current = _config.value
        if (current.roomShape == "Square") {
            _config.value = current.copy(lengthCm = length, widthCm = length)
        } else {
            _config.value = current.copy(lengthCm = length)
        }
    }

    fun updateWidthCm(width: String) {
        val current = _config.value
        if (current.roomShape != "Square") {
            _config.value = current.copy(widthCm = width)
        }
    }

    fun updateInteriorStyle(style: String) {
        _config.value = _config.value.copy(interiorStyle = style)
    }

    fun updateBudgetPkr(budget: String) {
        _config.value = _config.value.copy(budgetPkr = budget)
    }

    fun validateAndProceed(): Boolean {
        val current = _config.value
        val length = current.lengthCm.toDoubleOrNull()
        val width = current.widthCm.toDoubleOrNull()
        val budget = current.budgetPkr.toIntOrNull()

        if (length == null || length <= 0) {
            _errorMessage.value = "Room length must be a valid positive number."
            return false
        }
        if (width == null || width <= 0) {
            _errorMessage.value = "Room width must be a valid positive number."
            return false
        }
        if (budget == null || budget < 0) {
            _errorMessage.value = "Budget must be a valid non-negative amount in PKR."
            return false
        }
        if (current.roomShape == "Square" && length != width) {
            _errorMessage.value = "For a square room, length and width must be equal."
            return false
        }

        _errorMessage.value = null
        return true
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
