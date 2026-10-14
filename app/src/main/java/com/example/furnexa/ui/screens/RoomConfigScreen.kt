package com.example.furnexa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.furnexa.ui.room.RoomConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomConfigScreen(
    viewModel: RoomConfigViewModel,
    onBack: () -> Unit,
    onProceedToSummary: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val roomTypes = listOf("Living Room", "Bedroom", "Study Room")
    val roomShapes = listOf("Rectangle", "Square")
    val interiorStyles = listOf("Modern", "Minimalist", "Traditional")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Room") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Set your room dimensions and preferences for furniture layout.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Room Name
            OutlinedTextField(
                value = config.roomName,
                onValueChange = { viewModel.updateRoomName(it) },
                label = { Text("Room Name (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Room Type
            Text("Room Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                roomTypes.forEach { type ->
                    FilterChip(
                        selected = config.roomType == type,
                        onClick = { viewModel.updateRoomType(type) },
                        label = { Text(type) }
                    )
                }
            }

            // Room Shape
            Text("Room Shape", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                roomShapes.forEach { shape ->
                    FilterChip(
                        selected = config.roomShape == shape,
                        onClick = { viewModel.updateRoomShape(shape) },
                        label = { Text(shape) }
                    )
                }
            }

            // Dimensions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = config.lengthCm,
                    onValueChange = { viewModel.updateLengthCm(it) },
                    label = { Text("Length (cm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = config.widthCm,
                    onValueChange = { viewModel.updateWidthCm(it) },
                    label = { Text("Width (cm)") },
                    singleLine = true,
                    enabled = config.roomShape != "Square",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Interior Style
            Text("Preferred Interior Style", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                interiorStyles.forEach { style ->
                    FilterChip(
                        selected = config.interiorStyle == style,
                        onClick = { viewModel.updateInteriorStyle(style) },
                        label = { Text(style) }
                    )
                }
            }

            // Budget
            OutlinedTextField(
                value = config.budgetPkr,
                onValueChange = { viewModel.updateBudgetPkr(it) },
                label = { Text("Budget (PKR)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (viewModel.validateAndProceed()) {
                        onProceedToSummary()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue to Summary", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
