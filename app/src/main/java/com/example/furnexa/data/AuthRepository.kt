package com.example.furnexa.data

import com.example.furnexa.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    private val auth = SupabaseConfig.client.auth

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
        }
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUserOrNull()?.email
    }

    fun hasActiveSession(): Boolean {
        return auth.currentSessionOrNull() != null
    }
}
