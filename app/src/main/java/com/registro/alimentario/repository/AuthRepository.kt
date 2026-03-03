package com.registro.alimentario.repository

import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUserRole(): UserRole?
}
