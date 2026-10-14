package com.example.furnexa.ui.auth

sealed interface AuthState {
    object Loading : AuthState
    object Unauthenticated : AuthState
    data class Authenticated(val email: String) : AuthState
    data class Error(val message: String) : AuthState
}
