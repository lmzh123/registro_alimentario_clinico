package com.registro.alimentario.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.ui.professional.PatientRegistroListScreen
import com.registro.alimentario.ui.professional.ProfessionalHomeScreen
import com.registro.alimentario.ui.professional.RegistroDetailProfessionalScreen
import com.registro.alimentario.ui.shared.PhotoViewerScreen
import com.registro.alimentario.viewmodel.AuthViewModel
import com.registro.alimentario.viewmodel.ProfessionalViewModel

fun NavGraphBuilder.professionalGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(NavRoutes.PROFESSIONAL_HOME) {
        val professionalViewModel: ProfessionalViewModel = hiltViewModel()
        val currentRole by authViewModel.currentRole.collectAsState()
        val patients by professionalViewModel.patients.collectAsState()

        // Guard: patients who end up here go to patient graph
        if (currentRole == UserRole.PACIENTE) {
            navController.navigate(NavRoutes.REGISTRO_HISTORY) {
                popUpTo(NavRoutes.PROFESSIONAL_HOME) { inclusive = true }
            }
            return@composable
        }

        androidx.compose.runtime.LaunchedEffect(currentRole) {
            currentRole?.let { professionalViewModel.loadPatients(it) }
        }

        ProfessionalHomeScreen(
            role = currentRole ?: UserRole.NUTRICIONISTA,
            patients = patients,
            onPatientSelected = { patient ->
                navController.navigate(
                    NavRoutes.patientRegistroList(patient.uid, patient.displayName.ifBlank { patient.email })
                )
            },
            onLogout = {
                authViewModel.logout()
                navController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = NavRoutes.PATIENT_REGISTRO_LIST,
        arguments = listOf(
            navArgument("patientId") { type = NavType.StringType },
            navArgument("patientName") { type = NavType.StringType }
        )
    ) { backStack ->
        val patientId = backStack.arguments?.getString("patientId") ?: return@composable
        val patientName = java.net.URLDecoder.decode(
            backStack.arguments?.getString("patientName") ?: "", "UTF-8"
        )
        val professionalViewModel: ProfessionalViewModel = hiltViewModel()
        val currentRole by authViewModel.currentRole.collectAsState()
        val registros by professionalViewModel.filteredRegistros.collectAsState()
        val filter by professionalViewModel.filteredRegistros.collectAsState()

        androidx.compose.runtime.LaunchedEffect(patientId, currentRole) {
            currentRole?.let { professionalViewModel.loadRegistrosForPatient(patientId, it.id) }
        }

        PatientRegistroListScreen(
            patientName = patientName,
            registros = registros,
            currentFilter = com.registro.alimentario.viewmodel.RegistroFilter(),
            onFilterChanged = { professionalViewModel.setFilter(it) },
            onRegistroTapped = { id -> navController.navigate(NavRoutes.registroDetailProfessional(id)) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = NavRoutes.REGISTRO_DETAIL_PROFESSIONAL,
        arguments = listOf(navArgument("registroId") { type = NavType.StringType })
    ) { backStack ->
        val registroId = backStack.arguments?.getString("registroId") ?: return@composable
        val professionalViewModel: ProfessionalViewModel = hiltViewModel()
        val currentRole by authViewModel.currentRole.collectAsState()
        val registros by professionalViewModel.filteredRegistros.collectAsState()
        val registro = registros.firstOrNull { it.id == registroId } ?: return@composable
        val commentText by professionalViewModel.commentText.collectAsState()
        // Comments would be collected from ComentarioRepository via a sub-VM in full impl
        val comments = emptyList<com.registro.alimentario.model.ComentarioClinico>()

        RegistroDetailProfessionalScreen(
            registro = registro,
            comments = comments,
            professionalRole = currentRole ?: UserRole.NUTRICIONISTA,
            commentText = commentText,
            onCommentTextChange = { professionalViewModel.updateCommentText(it) },
            onSubmitComment = { professionalViewModel.submitComment(registroId) },
            onNavigateBack = { navController.popBackStack() },
            onPhotoTapped = { url -> navController.navigate(NavRoutes.photoViewer(url)) }
        )
    }
}
