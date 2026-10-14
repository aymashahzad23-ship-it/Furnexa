package com.example.furnexa.ui.layout

import androidx.lifecycle.ViewModel
import com.example.furnexa.data.Furniture
import com.example.furnexa.data.RoomConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlacedItem(
    val furniture: Furniture,
    val xCm: Double,
    val yCm: Double,
    val widthCm: Double,
    val depthCm: Double
)

sealed interface LayoutUiState {
    data class Success(
        val roomConfig: RoomConfig,
        val placedItems: List<PlacedItem>,
        val unplacedItems: List<Furniture>,
        val invalidDimensionItems: List<Furniture>
    ) : LayoutUiState
}

class RoomLayoutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LayoutUiState?>(null)
    val uiState: StateFlow<LayoutUiState?> = _uiState.asStateFlow()

    private var lastConfig: RoomConfig = RoomConfig()
    private var lastSelected: List<Furniture> = emptyList()

    fun generateLayout(roomConfig: RoomConfig, selectedItems: List<Furniture>) {
        lastConfig = roomConfig
        lastSelected = selectedItems

        val roomWidth = roomConfig.lengthCm.toDoubleOrNull() ?: 400.0
        val roomDepth = roomConfig.widthCm.toDoubleOrNull() ?: 300.0

        val validItems = mutableListOf<Furniture>()
        val invalidItems = mutableListOf<Furniture>()

        for (item in selectedItems) {
            if (item.widthCm > 0.0 && item.depthCm > 0.0) {
                validItems.add(item)
            } else {
                invalidItems.add(item)
            }
        }

        val placed = mutableListOf<PlacedItem>()
        val unplaced = mutableListOf<Furniture>()

        val margin = 20.0
        var currentX = margin
        var currentY = margin
        val rowHeight = validItems.maxOfOrNull { it.depthCm } ?: 60.0

        for (item in validItems) {
            val itemW = item.widthCm
            val itemD = item.depthCm

            var placedSuccessfully = false
            if (currentX + itemW <= roomWidth - margin && currentY + itemD <= roomDepth - margin) {
                if (!hasCollision(currentX, currentY, itemW, itemD, placed)) {
                    placed.add(PlacedItem(item, currentX, currentY, itemW, itemD))
                    currentX += itemW + 20.0
                    placedSuccessfully = true
                }
            }

            if (!placedSuccessfully) {
                currentX = margin
                currentY += rowHeight + 20.0
                if (currentX + itemW <= roomWidth - margin && currentY + itemD <= roomDepth - margin) {
                    if (!hasCollision(currentX, currentY, itemW, itemD, placed)) {
                        placed.add(PlacedItem(item, currentX, currentY, itemW, itemD))
                        currentX += itemW + 20.0
                        placedSuccessfully = true
                    }
                }
            }

            if (!placedSuccessfully) {
                unplaced.add(item)
            }
        }

        _uiState.value = LayoutUiState.Success(
            roomConfig = roomConfig,
            placedItems = placed,
            unplacedItems = unplaced,
            invalidDimensionItems = invalidItems
        )
    }

    fun regenerate() {
        generateLayout(lastConfig, lastSelected.shuffled())
    }

    private fun hasCollision(x: Double, y: Double, w: Double, d: Double, placed: List<PlacedItem>): Boolean {
        val padding = 5.0
        for (p in placed) {
            val pX = p.xCm
            val pY = p.yCm
            val pW = p.widthCm
            val pD = p.depthCm

            val overlap = !(x + w + padding <= pX || x >= pX + pW + padding || y + d + padding <= pY || y >= pY + pD + padding)
            if (overlap) return true
        }
        return false
    }
}
