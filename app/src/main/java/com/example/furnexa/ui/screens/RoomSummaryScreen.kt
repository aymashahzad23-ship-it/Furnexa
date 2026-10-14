package com.example.furnexa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.furnexa.ui.room.RoomConfigViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSummaryScreen(
    viewModel: RoomConfigViewModel,
    onEdit: () -> Unit,
    onFindFurniture: () -> Unit,
    onReturnToCatalog: () -> Unit
) {
    val config by viewModel.config.collectAsState()

    val formattedBudget = remember(config.budgetPkr) {
        val amount = config.budgetPkr.toIntOrNull() ?: 0
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        "PKR ${formatter.format(amount)}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Configuration Summary") },
                navigationIcon = {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Edit")
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Review your room configuration before layout generation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (config.roomName.isNotBlank()) {
                        SummaryRow(label = "Room Name", value = config.roomName)
                    }
                    SummaryRow(label = "Room Type", value = config.roomType)
                    SummaryRow(label = "Room Shape", value = config.roomShape)
                    SummaryRow(label = "Dimensions", value = "${config.lengthCm} cm × ${config.widthCm} cm")
                    SummaryRow(label = "Interior Style", value = config.interiorStyle)
                    SummaryRow(label = "Budget", value = formattedBudget)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFindFurniture,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Find Furniture", style = MaterialTheme.typography.bodyLarge)
            }

            Button(
                onClick = onReturnToCatalog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save & Return to Catalogue", style = MaterialTheme.typography.bodyLarge)
            }

            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Edit Configuration", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
