package com.registro.alimentario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.ui.auth.LoginScreen
import com.registro.alimentario.ui.auth.RegisterScreen
import com.registro.alimentario.viewmodel.AuthViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentRole by authViewModel.currentRole.collectAsState()
    val passwordResetState by authViewModel.passwordResetState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                uiState = uiState,
                passwordResetState = passwordResetState,
                onLogin = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onLoginSuccess = {
                    val role = currentRole ?: UserRole.PACIENTE
                    val destination = if (role == UserRole.PACIENTE) NavRoutes.REGISTRO_HISTORY
                    else NavRoutes.PROFESSIONAL_HOME
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                    authViewModel.resetState()
                },
                onSendPasswordReset = { email -> authViewModel.sendPasswordResetEmail(email) },
                onPasswordResetStateDismissed = { authViewModel.resetPasswordResetState() }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                uiState = uiState,
                onRegister = { email, password, name, role -> authViewModel.register(email, password, name, role) },
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    val role = currentRole ?: UserRole.PACIENTE
                    val destination = if (role == UserRole.PACIENTE) NavRoutes.REGISTRO_HISTORY
                    else NavRoutes.PROFESSIONAL_HOME
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                    authViewModel.resetState()
                }
            )
        }

        // ── Patient Graph ─────────────────────────────────────────────────────
        patientGraph(navController, authViewModel)

        // ── Professional Graph ────────────────────────────────────────────────
        professionalGraph(navController, authViewModel)
    }
}
