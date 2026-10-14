package com.example.furnexa.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.furnexa.ui.layout.LayoutUiState
import com.example.furnexa.ui.layout.RoomLayoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomLayoutScreen(
    viewModel: RoomLayoutViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val furnitureColors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFF57C00), // Orange
        Color(0xFF7B1FA2), // Purple
        Color(0xFFC2185B), // Pink
        Color(0xFF00796B)  // Teal
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2D Room Layout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val state = uiState
        if (state !is LayoutUiState.Success) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No layout data available.")
            }
        } else {
            val roomWidth = state.roomConfig.lengthCm.toDoubleOrNull() ?: 400.0
            val roomDepth = state.roomConfig.widthCm.toDoubleOrNull() ?: 300.0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Room: ${state.roomConfig.roomType} (${state.roomConfig.roomShape})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dimensions: ${roomWidth.toInt()} cm × ${roomDepth.toInt()} cm",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Placed items: ${state.placedItems.size} | Unplaced: ${state.unplacedItems.size + state.invalidDimensionItems.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.unplacedItems.isNotEmpty() || state.invalidDimensionItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Placement Warnings:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.unplacedItems.isNotEmpty()) {
                                Text("Could not fit (exceeds room space or collision): ${state.unplacedItems.joinToString { it.name }}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.invalidDimensionItems.isNotEmpty()) {
                                Text("Excluded due to missing dimensions: ${state.invalidDimensionItems.joinToString { it.name }}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Larger Responsive Canvas Floor Plan Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val surfaceColor = MaterialTheme.colorScheme.surface
                        val outlineColor = MaterialTheme.colorScheme.outline

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            val padding = 16f
                            val availableW = canvasWidth - (padding * 2)
                            val availableH = canvasHeight - (padding * 2)

                            val scaleX = availableW / roomWidth.toFloat()
                            val scaleY = availableH / roomDepth.toFloat()
                            val scale = minOf(scaleX, scaleY)

                            val drawW = roomWidth.toFloat() * scale
                            val drawH = roomDepth.toFloat() * scale

                            val startX = (canvasWidth - drawW) / 2f
                            val startY = (canvasHeight - drawH) / 2f

                            // Draw Room Floor
                            drawRect(
                                color = surfaceColor,
                                topLeft = Offset(startX, startY),
                                size = Size(drawW, drawH)
                            )

                            // Draw Room Border
                            drawRect(
                                color = outlineColor,
                                topLeft = Offset(startX, startY),
                                size = Size(drawW, drawH),
                                style = Stroke(width = 5f)
                            )

                            // Draw Placed Furniture Items & Labels
                            state.placedItems.forEachIndexed { index, item ->
                                val fX = startX + (item.xCm.toFloat() * scale)
                                val fY = startY + (item.yCm.toFloat() * scale)
                                val fW = item.widthCm.toFloat() * scale
                                val fD = item.depthCm.toFloat() * scale

                                val itemColor = furnitureColors[index % furnitureColors.size]

                                drawRect(
                                    color = itemColor,
                                    topLeft = Offset(fX, fY),
                                    size = Size(fW, fD)
                                )
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(fX, fY),
                                    size = Size(fW, fD),
                                    style = Stroke(width = 2f)
                                )

                                // Draw label text if box is large enough
                                if (fW > 40f && fD > 25f) {
                                    val nativePaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 26f
                                        isAntiAlias = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    }
                                    val textX = fX + (fW / 2f)
                                    val textY = fY + (fD / 2f) + 8f
                                    drawContext.canvas.nativeCanvas.drawText(
                                        item.furniture.name.take(10),
                                        textX,
                                        textY,
                                        nativePaint
                                    )
                                }
                            }
                        }
                    }
                }

                // Compact Legend / Placed Items List
                if (state.placedItems.isNotEmpty()) {
                    Text(
                        text = "Placed Furniture Legend:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.placedItems.indices.toList()) { index ->
                            val placed = state.placedItems[index]
                            val badgeColor = furnitureColors[index % furnitureColors.size]
                            AssistChip(
                                onClick = {},
                                label = { Text("${placed.furniture.name} (${placed.widthCm.toInt()}x${placed.depthCm.toInt()}cm)") },
                                colors = AssistChipDefaults.assistChipColors(containerColor = badgeColor.copy(alpha = 0.2f))
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.regenerate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Regenerate Layout", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
