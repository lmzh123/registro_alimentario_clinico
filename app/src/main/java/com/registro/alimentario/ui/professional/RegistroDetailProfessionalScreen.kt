package com.registro.alimentario.ui.professional

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.ComentarioClinico
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.RestriccionPrevia
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
    commentText: String,
    currentUserId: String,
    editingCommentId: String?,
    editingCommentText: String,
    onCommentTextChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onEditCommentStart: (ComentarioClinico) -> Unit,
    onEditCommentTextChange: (String) -> Unit,
    onEditCommentSubmit: () -> Unit,
    onEditCommentCancel: () -> Unit,
    onDeleteComment: (ComentarioClinico) -> Unit,
    onNavigateBack: () -> Unit,
    onPhotoTapped: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE dd/MM/yyyy, HH:mm", Locale("es"))
    val scrollState = rememberScrollState()
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(registro.tipoComida.displayName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding()
        ) {
            Text(
                text = dateFormat.format(registro.fechaHora.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProfesionalView(registro, onPhotoTapped)

            Spacer(modifier = Modifier.height(16.dp))
            CommentList(
                comments = comments,
                currentUserId = currentUserId,
                editingCommentId = editingCommentId,
                editingCommentText = editingCommentText,
                onEditStart = onEditCommentStart,
                onEditTextChange = onEditCommentTextChange,
                onEditSubmit = onEditCommentSubmit,
                onEditCancel = onEditCommentCancel,
                onDelete = onDeleteComment
            )
            Spacer(modifier = Modifier.height(12.dp))
            CommentInput(
                text = commentText,
                onTextChange = onCommentTextChange,
                onSend = onSubmitComment
            )
        }
    }
}

@Composable
private fun ProfesionalView(registro: Registro, onPhotoTapped: (String) -> Unit) {
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
        registro.restriccionPrevia?.let {
            Text("${stringResource(R.string.restriccion_label)}: ${stringResource(when (it) {
                RestriccionPrevia.SALTE_COMIDA -> R.string.restriccion_salte_comida
                RestriccionPrevia.COMI_MENOS -> R.string.restriccion_comi_menos
                RestriccionPrevia.RETRASE_COMIDA -> R.string.restriccion_retrase_comida
                RestriccionPrevia.NO_HUBO -> R.string.restriccion_no_hubo
            })}")
        }
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
    if (registro.notasAdicionales.isNotBlank()) {
        ProfDetailSection(stringResource(R.string.additional_notes_label)) {
            Text(registro.notasAdicionales)
        }
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
