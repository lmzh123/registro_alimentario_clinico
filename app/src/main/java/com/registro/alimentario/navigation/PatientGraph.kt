package com.registro.alimentario.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.firebase.auth.FirebaseAuth
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.ui.patient.CreateRegistroScreen
import com.registro.alimentario.ui.patient.NotificationSettingsScreen
import com.registro.alimentario.ui.patient.RegistroDetailScreen
import com.registro.alimentario.ui.patient.RegistroHistoryScreen
import com.registro.alimentario.ui.shared.CrisisResourcesScreen
import com.registro.alimentario.ui.shared.PhotoViewerScreen
import com.registro.alimentario.viewmodel.AuthViewModel
import com.registro.alimentario.viewmodel.RegistroViewModel
import com.registro.alimentario.repository.ComentarioRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

fun NavGraphBuilder.patientGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(NavRoutes.REGISTRO_HISTORY) {
        val registroViewModel: RegistroViewModel = hiltViewModel()
        val currentRole by authViewModel.currentRole.collectAsState()

        // Guard: redirect professionals who end up here
        if (currentRole != null && currentRole != UserRole.PACIENTE) {
            navController.navigate(NavRoutes.PROFESSIONAL_HOME) {
                popUpTo(NavRoutes.REGISTRO_HISTORY) { inclusive = true }
            }
            return@composable
        }

        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return@composable
        androidx.compose.runtime.LaunchedEffect(patientId) {
            registroViewModel.loadPatientRegistros(patientId)
        }
        val registros by registroViewModel.registros.collectAsState()

        RegistroHistoryScreen(
            registros = registros,
            onCreateNew = { navController.navigate(NavRoutes.CREATE_REGISTRO) },
            onRegistroTapped = { id -> navController.navigate(NavRoutes.registroDetail(id)) },
            onCrisisResources = { navController.navigate(NavRoutes.CRISIS_RESOURCES) }
        )
    }

    composable(NavRoutes.CREATE_REGISTRO) {
        val registroViewModel: RegistroViewModel = hiltViewModel()
        val formState by registroViewModel.formState.collectAsState()
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return@composable

        CreateRegistroScreen(
            formState = formState,
            patientId = patientId,
            onFieldUpdate = { update -> registroViewModel.updateField(update) },
            onSave = { id -> registroViewModel.save(id) },
            onNavigateBack = { navController.popBackStack() },
            onPhotoTapped = { url -> navController.navigate(NavRoutes.photoViewer(url)) },
            onCrisisResources = { navController.navigate(NavRoutes.CRISIS_RESOURCES) },
            onSaveSuccess = {
                registroViewModel.resetForm()
                navController.popBackStack()
            }
        )
    }

    composable(
        route = NavRoutes.EDIT_REGISTRO,
        arguments = listOf(navArgument("registroId") { type = NavType.StringType })
    ) { backStack ->
        val registroId = backStack.arguments?.getString("registroId") ?: return@composable
        val registroViewModel: RegistroViewModel = hiltViewModel()
        val formState by registroViewModel.formState.collectAsState()
        val registros by registroViewModel.registros.collectAsState()
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return@composable

        androidx.compose.runtime.LaunchedEffect(registroId) {
            val registro = registros.firstOrNull { it.id == registroId }
            if (registro != null) registroViewModel.loadForEdit(registro)
        }

        CreateRegistroScreen(
            formState = formState,
            patientId = patientId,
            onFieldUpdate = { update -> registroViewModel.updateField(update) },
            onSave = { id -> registroViewModel.save(id) },
            onNavigateBack = { navController.popBackStack() },
            onPhotoTapped = { url -> navController.navigate(NavRoutes.photoViewer(url)) },
            onCrisisResources = { navController.navigate(NavRoutes.CRISIS_RESOURCES) },
            onSaveSuccess = {
                registroViewModel.resetForm()
                navController.popBackStack()
            }
        )
    }

    composable(
        route = NavRoutes.REGISTRO_DETAIL,
        arguments = listOf(navArgument("registroId") { type = NavType.StringType }),
        deepLinks = listOf(
            navDeepLink { uriPattern = "${NavRoutes.DEEP_LINK_BASE}/{registroId}" }
        )
    ) { backStack ->
        val registroId = backStack.arguments?.getString("registroId") ?: return@composable
        val registroViewModel: RegistroViewModel = hiltViewModel()
        val registros by registroViewModel.registros.collectAsState()
        val registro = registros.firstOrNull { it.id == registroId } ?: return@composable

        // TODO: collect comments via ComentarioRepository directly or a shared VM
        RegistroDetailScreen(
            registro = registro,
            comments = emptyList(), // wired below via ViewModel in full implementation
            onEdit = { navController.navigate(NavRoutes.editRegistro(registroId)) },
            onDelete = {
                registroViewModel.deleteRegistro(registroId, registro.fotos)
                navController.popBackStack()
            },
            onNavigateBack = { navController.popBackStack() },
            onPhotoTapped = { url -> navController.navigate(NavRoutes.photoViewer(url)) },
            onCrisisResources = { navController.navigate(NavRoutes.CRISIS_RESOURCES) }
        )
    }

    composable(NavRoutes.CRISIS_RESOURCES) {
        CrisisResourcesScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(NavRoutes.NOTIFICATION_SETTINGS) {
        NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = NavRoutes.PHOTO_VIEWER,
        arguments = listOf(navArgument("photoUrl") { type = NavType.StringType })
    ) { backStack ->
        val encodedUrl = backStack.arguments?.getString("photoUrl") ?: return@composable
        val url = java.net.URLDecoder.decode(encodedUrl, "UTF-8")
        PhotoViewerScreen(photoUrl = url, onClose = { navController.popBackStack() })
    }
}
