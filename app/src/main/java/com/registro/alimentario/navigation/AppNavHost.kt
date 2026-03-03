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

    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                uiState = uiState,
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
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                uiState = uiState,
                onRegister = { email, password, name -> authViewModel.register(email, password, name) },
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.REGISTRO_HISTORY) {
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
