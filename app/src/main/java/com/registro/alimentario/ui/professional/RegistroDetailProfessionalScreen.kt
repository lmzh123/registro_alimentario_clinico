package com.registro.alimentario.ui.professional

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.ComentarioClinico
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.ui.shared.components.CommentInput
import com.registro.alimentario.ui.shared.components.CommentList
import com.registro.alimentario.ui.shared.components.PhotoRow
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDetailProfessionalScreen(
    registro: Registro,
    comments: List<ComentarioClinico>,
    professionalRole: UserRole,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onNavigateBack: () -> Unit,
    onPhotoTapped: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE dd/MM/yyyy, HH:mm", Locale("es"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(registro.tipoComida.displayName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                }
                // No edit actions — professionals cannot modify registros
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = dateFormat.format(registro.fechaHora.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Show fields based on role:
            // Nutricionista: food info only
            // Psicologia: behavioral + emotional info
            // Psiquiatria: everything
            when (professionalRole) {
                UserRole.NUTRICIONISTA -> NutricionistaView(registro, onPhotoTapped)
                UserRole.PSICOLOGIA -> PsicologiaView(registro, onPhotoTapped)
                UserRole.PSIQUIATRIA -> PsiquiatriaView(registro, onPhotoTapped)
                UserRole.PACIENTE -> { /* guard — should never happen */ }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CommentList(comments = comments)

            Spacer(modifier = Modifier.height(12.dp))
            CommentInput(
                text = commentText,
                onTextChange = onCommentTextChange,
                onSend = onSubmitComment
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NutricionistaView(registro: Registro, onPhotoTapped: (String) -> Unit) {
    if (registro.descripcion.isNotBlank()) {
        ProfDetailSection(stringResource(R.string.description_label)) {
            Text(registro.descripcion)
        }
    }
    if (registro.lugar.isNotBlank()) {
        ProfDetailSection(stringResource(R.string.location_label)) {
            Text(registro.lugar)
        }
    }
    if (registro.acompanantes.isNotBlank()) {
        ProfDetailSection(stringResource(R.string.companions_label)) {
            Text(registro.acompanantes)
        }
    }
    if (registro.fotos.isNotEmpty()) {
        PhotoRow(
            localUris = emptyList(),
            uploadedUrls = registro.fotos,
            onTakePhoto = {}, onChooseGallery = {},
            onRemoveLocal = {}, onRemoveUploaded = {},
            onPhotoTapped = onPhotoTapped,
            editable = false
        )
    }
    // notas_adicionales: only if shared
    if (registro.notasAdicionales.isNotBlank() &&
        registro.notasVisibilidad.contains(UserRole.NUTRICIONISTA.id)
    ) {
        ProfDetailSection(stringResource(R.string.additional_notes_label)) {
            Text(registro.notasAdicionales)
        }
    }
}

@Composable
private fun PsicologiaView(registro: Registro, onPhotoTapped: (String) -> Unit) {
    if (registro.fotos.isNotEmpty()) {
        PhotoRow(
            localUris = emptyList(),
            uploadedUrls = registro.fotos,
            onTakePhoto = {}, onChooseGallery = {},
            onRemoveLocal = {}, onRemoveUploaded = {},
            onPhotoTapped = onPhotoTapped,
            editable = false
        )
    }
    ProfDetailSection(stringResource(R.string.behavioral_section_title)) {
        Text("${stringResource(R.string.atracon_label)}: ${registro.fueAtracon.id}")
        if (registro.desencadenanteAtracon.isNotBlank()) {
            Text("Desencadenante: ${registro.desencadenanteAtracon}")
        }
        Text("${stringResource(R.string.purga_label)}: ${if (registro.deseosPurgar) "Sí" else "No"}")
        if (registro.deseosPurgar) {
            Text("Actuó sobre purga: ${if (registro.actuoSobrePurga) "Sí" else "No"}")
        }
        Text("${stringResource(R.string.chequeo_label)}: ${if (registro.checqueoCuerpo) "Sí" else "No"}")
    }
    if (registro.emocionesAntes.isNotEmpty()) {
        ProfDetailSection(stringResource(R.string.emotions_before_label)) {
            Text(registro.emocionesAntes.joinToString(", ") { it.tipo.displayName })
        }
    }
    if (registro.emocionesDespues.isNotEmpty()) {
        ProfDetailSection(stringResource(R.string.emotions_after_label)) {
            Text(registro.emocionesDespues.joinToString(", ") { it.tipo.displayName })
        }
    }
    if (registro.pensamientos.isNotBlank()) {
        ProfDetailSection(stringResource(R.string.thoughts_label)) {
            Text(registro.pensamientos)
        }
    }
    if (registro.comentariosExternos.isNotBlank()) {
        ProfDetailSection(stringResource(R.string.external_comments_label)) {
            Text(registro.comentariosExternos)
        }
    }
    if (registro.notasAdicionales.isNotBlank() &&
        registro.notasVisibilidad.contains(UserRole.PSICOLOGIA.id)
    ) {
        ProfDetailSection(stringResource(R.string.additional_notes_label)) {
            Text(registro.notasAdicionales)
        }
    }
}

@Composable
private fun PsiquiatriaView(registro: Registro, onPhotoTapped: (String) -> Unit) {
    // Full access — show everything
    NutricionistaView(registro, onPhotoTapped)
    PsicologiaView(registro, onPhotoTapped)
    // Notes shown if shared or if psiquiatria always gets them per spec
    if (registro.notasAdicionales.isNotBlank() &&
        (registro.notasVisibilidad.contains(UserRole.PSIQUIATRIA.id) || registro.notasVisibilidad.isEmpty().not())
    ) {
        // Already handled in individual views — avoid duplicate display
    }
}

@Composable
private fun ProfDetailSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
