package com.example.furnexa.data

import android.util.Log
import com.example.furnexa.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRoleDto(
    @SerialName("user_id") val userId: String,
    @SerialName("role") val role: String
)

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

    suspend fun getUserRole(): String {
        return runCatching {
            val session = auth.currentSessionOrNull()
            val userId = session?.user?.id
            if (userId.isNullOrBlank()) {
                Log.w("FurnexaAuth", "getUserRole: No authenticated user session found. Defaulting to 'user'.")
                return@runCatching "user"
            }

            Log.d("FurnexaAuth", "getUserRole: Querying role for user UUID: $userId")

            val roleRecords = SupabaseConfig.client.from("user_roles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<UserRoleDto>()

            val userRoleEntry = roleRecords.find { it.userId == userId }
            val roleValue = userRoleEntry?.role?.trim()?.lowercase()

            val resolvedRole = if (roleValue == "admin") "admin" else "user"
            Log.d("FurnexaAuth", "getUserRole: Resolved role for user $userId is '$resolvedRole' (raw db role: '$roleValue')")

            resolvedRole
        }.getOrElse { e ->
            Log.e("FurnexaAuth", "getUserRole: Exception caught during role fetch, strictly defaulting to 'user'.", e)
            "user"
        }
    }

    suspend fun isAdminUser(): Boolean {
        return getUserRole() == "admin"
    }
}
