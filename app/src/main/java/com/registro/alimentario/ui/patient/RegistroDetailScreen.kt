package com.registro.alimentario.ui.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.ComentarioClinico
import com.registro.alimentario.ui.shared.components.CommentList
import com.registro.alimentario.ui.shared.components.PhotoRow
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDetailScreen(
    registro: Registro,
    comments: List<ComentarioClinico>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit,
    onPhotoTapped: (String) -> Unit,
    onCrisisResources: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("EEEE dd/MM/yyyy, HH:mm", Locale("es"))

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(registro.tipoComida.displayName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = onCrisisResources) {
                        Icon(Icons.Outlined.FavoriteBorder, stringResource(R.string.crisis_resources_button_cd))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, stringResource(R.string.edit_button))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, stringResource(R.string.delete_button))
                    }
                }
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

            if (registro.descripcion.isNotBlank()) {
                DetailSection(title = stringResource(R.string.description_label)) {
                    Text(registro.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (registro.lugar.isNotBlank()) {
                DetailSection(title = stringResource(R.string.location_label)) {
                    Text(registro.lugar, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (registro.acompanantes.isNotBlank()) {
                DetailSection(title = stringResource(R.string.companions_label)) {
                    Text(registro.acompanantes, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (registro.fotos.isNotEmpty()) {
                PhotoRow(
                    localUris = emptyList(),
                    uploadedUrls = registro.fotos,
                    onTakePhoto = {},
                    onChooseGallery = {},
                    onRemoveLocal = {},
                    onRemoveUploaded = {},
                    onPhotoTapped = onPhotoTapped,
                    editable = false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Behavioral flags
            DetailSection(title = stringResource(R.string.behavioral_section_title)) {
                Text("${stringResource(R.string.atracon_label)}: ${registro.fueAtracon.id}")
                if (registro.desencadenanteAtracon.isNotBlank()) {
                    Text("Desencadenante: ${registro.desencadenanteAtracon}")
                }
                Text("${stringResource(R.string.purga_label)}: ${if (registro.deseosPurgar) "Sí" else "No"}")
                if (registro.deseosPurgar) {
                    Text("${stringResource(R.string.purga_actuo_label)}: ${if (registro.actuoSobrePurga) "Sí" else "No"}")
                }
                Text("${stringResource(R.string.chequeo_label)}: ${if (registro.checqueoCuerpo) "Sí" else "No"}")
            }

            if (registro.emocionesAntes.isNotEmpty()) {
                DetailSection(title = stringResource(R.string.emotions_before_label)) {
                    Text(registro.emocionesAntes.joinToString(", ") {
                        if (it.textoLibre.isNotBlank()) "${it.tipo.displayName} (${it.textoLibre})" else it.tipo.displayName
                    })
                }
            }
            if (registro.emocionesDespues.isNotEmpty()) {
                DetailSection(title = stringResource(R.string.emotions_after_label)) {
                    Text(registro.emocionesDespues.joinToString(", ") {
                        if (it.textoLibre.isNotBlank()) "${it.tipo.displayName} (${it.textoLibre})" else it.tipo.displayName
                    })
                }
            }
            if (registro.pensamientos.isNotBlank()) {
                DetailSection(title = stringResource(R.string.thoughts_label)) {
                    Text(registro.pensamientos, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (registro.comentariosExternos.isNotBlank()) {
                DetailSection(title = stringResource(R.string.external_comments_label)) {
                    Text(registro.comentariosExternos, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (registro.notasAdicionales.isNotBlank()) {
                DetailSection(title = stringResource(R.string.additional_notes_label)) {
                    Text(registro.notasAdicionales, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CommentList(comments = comments)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
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
