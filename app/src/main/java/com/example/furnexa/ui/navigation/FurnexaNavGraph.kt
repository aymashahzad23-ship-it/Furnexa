package com.example.furnexa.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.furnexa.ui.auth.AuthState
import com.example.furnexa.ui.auth.AuthViewModel
import com.example.furnexa.ui.catalog.FurnitureViewModel
import com.example.furnexa.ui.layout.RoomLayoutViewModel
import com.example.furnexa.ui.recommendation.RecommendationUiState
import com.example.furnexa.ui.recommendation.RecommendationViewModel
import com.example.furnexa.ui.room.RoomConfigViewModel
import com.example.furnexa.ui.screens.FurnitureCatalogueScreen
import com.example.furnexa.ui.screens.FurnitureDetailScreen
import com.example.furnexa.ui.screens.LoginScreen
import com.example.furnexa.ui.screens.RecommendationScreen
import com.example.furnexa.ui.screens.RegisterScreen
import com.example.furnexa.ui.screens.RoomConfigScreen
import com.example.furnexa.ui.screens.RoomLayoutScreen
import com.example.furnexa.ui.screens.RoomSummaryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
}

@Composable
fun FurnexaNavGraph(
    viewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Unauthenticated -> {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = {
                            viewModel.clearUiState()
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = {
                            viewModel.clearUiState()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
        is AuthState.Authenticated -> {
            val email = (authState as AuthState.Authenticated).email
            AuthenticatedNavGraph(
                email = email,
                authViewModel = viewModel
            )
        }
        is AuthState.Error -> {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = {
                            viewModel.clearUiState()
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthenticatedNavGraph(
    email: String,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val furnitureViewModel: FurnitureViewModel = viewModel()
    val roomConfigViewModel: RoomConfigViewModel = viewModel()
    val roomLayoutViewModel: RoomLayoutViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "catalog"
    ) {
        composable("catalog") {
            FurnitureCatalogueScreen(
                email = email,
                viewModel = furnitureViewModel,
                authViewModel = authViewModel,
                onItemClick = { furnitureId ->
                    navController.navigate("detail/$furnitureId")
                },
                onConfigureRoom = {
                    roomConfigViewModel.clearError()
                    navController.navigate("room_config")
                }
            )
        }
        composable("detail/{furnitureId}") { backStackEntry ->
            val furnitureId = backStackEntry.arguments?.getString("furnitureId")?.toIntOrNull() ?: 0
            FurnitureDetailScreen(
                furnitureId = furnitureId,
                viewModel = furnitureViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("room_config") {
            RoomConfigScreen(
                viewModel = roomConfigViewModel,
                onBack = { navController.popBackStack() },
                onProceedToSummary = {
                    navController.navigate("room_summary")
                }
            )
        }
        composable("room_summary") {
            RoomSummaryScreen(
                viewModel = roomConfigViewModel,
                onEdit = { navController.popBackStack() },
                onFindFurniture = {
                    navController.navigate("recommendations")
                },
                onReturnToCatalog = {
                    navController.popBackStack("catalog", inclusive = false)
                }
            )
        }
        composable("recommendations") {
            val config by roomConfigViewModel.config.collectAsState()
            val recommendationViewModel: RecommendationViewModel = viewModel()
            LaunchedEffect(config) {
                recommendationViewModel.loadRecommendations(config)
            }
            RecommendationScreen(
                viewModel = recommendationViewModel,
                onBack = { navController.popBackStack() },
                onItemClick = { furnitureId ->
                    navController.navigate("detail/$furnitureId")
                },
                onViewLayout = {
                    val state = recommendationViewModel.uiState.value
                    if (state is RecommendationUiState.Success) {
                        val selected = state.recommendedItems.filter { state.selectedItemIds.contains(it.id) }
                        roomLayoutViewModel.generateLayout(state.roomConfig, selected)
                        navController.navigate("room_layout")
                    }
                }
            )
        }
        composable("room_layout") {
            RoomLayoutScreen(
                viewModel = roomLayoutViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
