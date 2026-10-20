package com.example.furnexa.ui.auth

import android.util.Log
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
            _authState.value = AuthState.Loading
            try {
                if (authRepository.hasActiveSession()) {
                    val email = authRepository.getCurrentUserEmail() ?: "User"
                    val role = authRepository.getUserRole()
                    val isAdmin = role == "admin"
                    Log.d("FurnexaAuth", "checkSession: Active session found for $email. role = $role, isAdmin = $isAdmin")
                    _authState.value = AuthState.Authenticated(email, isAdmin)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e("FurnexaAuth", "Session check failed", e)
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _uiState.value = AuthUiState.Loading
            try {
                val result = authRepository.signIn(trimmedEmail, password)
                result.fold(
                    onSuccess = {
                        val userEmail = authRepository.getCurrentUserEmail() ?: trimmedEmail
                        val role = authRepository.getUserRole()
                        val isAdmin = role == "admin"
                        Log.d("FurnexaAuth", "signIn: User $userEmail signed in successfully. role = $role, isAdmin = $isAdmin")
                        _authState.value = AuthState.Authenticated(userEmail, isAdmin)
                        _uiState.value = AuthUiState.Idle
                    },
                    onFailure = { throwable ->
                        val errorMessage = throwable.localizedMessage ?: "Sign in failed. Please check your credentials."
                        Log.e("FurnexaAuth", "Sign in failed: $errorMessage")
                        _authState.value = AuthState.Unauthenticated
                        _uiState.value = AuthUiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "An unexpected error occurred during sign in."
                Log.e("FurnexaAuth", "Sign in exception", e)
                _authState.value = AuthState.Unauthenticated
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _uiState.value = AuthUiState.Loading
            try {
                val result = authRepository.signUp(trimmedEmail, password)
                result.fold(
                    onSuccess = {
                        if (authRepository.hasActiveSession()) {
                            val userEmail = authRepository.getCurrentUserEmail() ?: trimmedEmail
                            val role = authRepository.getUserRole()
                            val isAdmin = role == "admin"
                            Log.d("FurnexaAuth", "signUp: User $userEmail signed up and active. role = $role, isAdmin = $isAdmin")
                            _authState.value = AuthState.Authenticated(userEmail, isAdmin)
                            _uiState.value = AuthUiState.Success("Registration successful!")
                        } else {
                            _authState.value = AuthState.Unauthenticated
                            _uiState.value = AuthUiState.Success("Registration successful! Please check your email to confirm your account, then log in.")
                        }
                    },
                    onFailure = { throwable ->
                        val errorMessage = throwable.localizedMessage ?: "Registration failed. Please try again."
                        Log.e("FurnexaAuth", "Registration failed: $errorMessage")
                        _authState.value = AuthState.Unauthenticated
                        _uiState.value = AuthUiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "An unexpected error occurred during registration."
                Log.e("FurnexaAuth", "Registration exception", e)
                _authState.value = AuthState.Unauthenticated
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _uiState.value = AuthUiState.Loading
            try {
                val result = authRepository.signOut()
                result.fold(
                    onSuccess = {
                        Log.d("FurnexaAuth", "signOut: User signed out successfully.")
                        _authState.value = AuthState.Unauthenticated
                        _uiState.value = AuthUiState.Idle
                    },
                    onFailure = { throwable ->
                        val errorMessage = throwable.localizedMessage ?: "Sign out failed."
                        Log.e("FurnexaAuth", "Sign out failed", throwable)
                        _authState.value = AuthState.Unauthenticated
                        _uiState.value = AuthUiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                Log.e("FurnexaAuth", "Sign out exception", e)
                _authState.value = AuthState.Unauthenticated
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Sign out failed.")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
