package com.registro.alimentario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState()
    object Sent : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState.asStateFlow()

    private val _currentRole = MutableStateFlow<UserRole?>(null)
    val currentRole: StateFlow<UserRole?> = _currentRole.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user == null) {
                    _currentRole.value = null
                } else {
                    _currentRole.value = authRepository.getCurrentUserRole()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor completá todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email.trim(), password).fold(
                onSuccess = { user ->
                    _currentRole.value = authRepository.getCurrentUserRole()
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = {
                    _uiState.value = AuthUiState.Error("Credenciales incorrectas. Verificá tu email y contraseña.")
                }
            )
        }
    }

    fun register(email: String, password: String, displayName: String, role: UserRole = UserRole.PACIENTE) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor completá todos los campos")
            return
        }
        if (password.length < 8) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email.trim(), password, displayName.trim(), role).fold(
                onSuccess = { user ->
                    _currentRole.value = role
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = {
                    _uiState.value = AuthUiState.Error("No se pudo crear la cuenta. Intentá con otro email.")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
            _currentRole.value = null
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _passwordResetState.value = PasswordResetState.Error("Ingresá tu correo electrónico")
            return
        }
        viewModelScope.launch {
            _passwordResetState.value = PasswordResetState.Loading
            // Always report success to avoid leaking whether an account exists
            authRepository.sendPasswordResetEmail(email.trim())
            _passwordResetState.value = PasswordResetState.Sent
        }
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = PasswordResetState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
