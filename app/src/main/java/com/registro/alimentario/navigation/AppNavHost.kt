package com.registro.alimentario.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val isCheckingAuth by authViewModel.isCheckingAuth.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    val currentRole by authViewModel.currentRole.collectAsState()
    val passwordResetState by authViewModel.passwordResetState.collectAsState()
    val resendVerificationState by authViewModel.resendVerificationState.collectAsState()

    if (isCheckingAuth) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = remember {
        when (currentRole) {
            UserRole.PACIENTE -> NavRoutes.REGISTRO_HISTORY
            null -> NavRoutes.LOGIN
            else -> NavRoutes.PROFESSIONAL_HOME
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                uiState = uiState,
                passwordResetState = passwordResetState,
                resendVerificationState = resendVerificationState,
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
                onPasswordResetStateDismissed = { authViewModel.resetPasswordResetState() },
                onResendVerification = { email, password -> authViewModel.resendEmailVerification(email, password) },
                onResendVerificationStateDismissed = { authViewModel.resetResendVerificationState() }
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
