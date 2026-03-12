package com.registro.alimentario.repository

import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import kotlinx.coroutines.flow.Flow

class EmailNotVerifiedException : Exception("email_not_verified")

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun register(email: String, password: String, displayName: String, role: UserRole = UserRole.PACIENTE): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUserRole(): UserRole?
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun resendEmailVerification(email: String, password: String): Result<Unit>
}
