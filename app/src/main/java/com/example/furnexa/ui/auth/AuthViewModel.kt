package com.example.furnexa.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furnexa.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            if (authRepository.hasActiveSession()) {
                val email = authRepository.getCurrentUserEmail() ?: "User"
                _authState.value = AuthState.Authenticated(email)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signIn(email.trim(), password)
            result.fold(
                onSuccess = {
                    val userEmail = authRepository.getCurrentUserEmail() ?: email
                    _authState.value = AuthState.Authenticated(userEmail)
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error(throwable.localizedMessage ?: "Sign in failed. Please check your credentials.")
                }
            )
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signUp(email.trim(), password)
            result.fold(
                onSuccess = {
                    if (authRepository.hasActiveSession()) {
                        val userEmail = authRepository.getCurrentUserEmail() ?: email
                        _authState.value = AuthState.Authenticated(userEmail)
                        _uiState.value = AuthUiState.Success("Registration successful!")
                    } else {
                        _uiState.value = AuthUiState.Success("Registration successful! Please check your email to confirm your account.")
                    }
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error(throwable.localizedMessage ?: "Registration failed. Please try again.")
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signOut()
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.Unauthenticated
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { throwable ->
                    _uiState.value = AuthUiState.Error(throwable.localizedMessage ?: "Sign out failed.")
                }
            )
        }
    }

    fun clearUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
