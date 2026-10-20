package com.example.furnexa.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFurnitureFormScreen(
    furnitureId: Int?,
    viewModel: AdminFurnitureViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val existingItem = remember(uiState, furnitureId) {
        if (furnitureId != null && uiState is AdminUiState.Success) {
            (uiState as AdminUiState.Success).items.find { it.id == furnitureId }
        } else {
            null
        }
    }

    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var category by remember { mutableStateOf(existingItem?.category ?: "Living Room") }
    var description by remember { mutableStateOf(existingItem?.description ?: "") }
    var pricePkr by remember { mutableStateOf(existingItem?.pricePkr?.toString() ?: "") }
    var widthCm by remember { mutableStateOf(existingItem?.widthCm?.toString() ?: "") }
    var depthCm by remember { mutableStateOf(existingItem?.depthCm?.toString() ?: "") }
    var heightCm by remember { mutableStateOf(existingItem?.heightCm?.toString() ?: "") }
    var style by remember { mutableStateOf(existingItem?.style ?: "Modern") }
    var colour by remember { mutableStateOf(existingItem?.colour ?: "") }
    var roomType by remember { mutableStateOf(existingItem?.roomType ?: "Living Room") }
    var imageUrl by remember { mutableStateOf(existingItem?.imageUrl ?: "") }
    var model3dUrl by remember { mutableStateOf(existingItem?.model3dUrl ?: "") }

    LaunchedEffect(operationMessage) {
        operationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (furnitureId == null) "Add Furniture" else "Edit Furniture") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Furniture Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pricePkr,
                onValueChange = { pricePkr = it },
                label = { Text("Price (PKR) *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = widthCm,
                    onValueChange = { widthCm = it },
                    label = { Text("Width (cm) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = depthCm,
                    onValueChange = { depthCm = it },
                    label = { Text("Depth (cm) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = heightCm,
                    onValueChange = { heightCm = it },
                    label = { Text("Height (cm) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = style,
                onValueChange = { style = it },
                label = { Text("Style (e.g. Modern, Scandinavian)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = colour,
                onValueChange = { colour = it },
                label = { Text("Colour") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = roomType,
                onValueChange = { roomType = it },
                label = { Text("Room Type (e.g. Living Room, Bedroom)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = model3dUrl,
                onValueChange = { model3dUrl = it },
                label = { Text("Optional 3D Model URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (furnitureId == null) {
                        viewModel.addFurniture(
                            name, category, description, pricePkr, widthCm, depthCm, heightCm,
                            style, colour, roomType, imageUrl, model3dUrl,
                            onSuccess = { onBack() }
                        )
                    } else {
                        viewModel.updateFurniture(
                            furnitureId, name, category, description, pricePkr, widthCm, depthCm, heightCm,
                            style, colour, roomType, imageUrl, model3dUrl,
                            onSuccess = { onBack() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (furnitureId == null) "Save Furniture" else "Update Furniture", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
