package com.registro.alimentario.ui.patient

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.registro.alimentario.R
import com.registro.alimentario.model.FueAtracon
import com.registro.alimentario.model.TipoComida
import com.registro.alimentario.ui.shared.components.EmotionPicker
import com.registro.alimentario.ui.shared.components.PhotoRow
import com.registro.alimentario.ui.shared.components.VisibilitySelector
import com.registro.alimentario.viewmodel.RegistroFormState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateRegistroScreen(
    formState: RegistroFormState,
    patientId: String,
    onFieldUpdate: (RegistroFormState.() -> RegistroFormState) -> Unit,
    onSave: (patientId: String) -> Unit,
    onNavigateBack: () -> Unit,
    onPhotoTapped: (String) -> Unit,
    onCrisisResources: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = formState.id.isNotBlank()

    LaunchedEffect(formState.savedSuccessfully) {
        if (formState.savedSuccessfully) onSaveSuccess()
    }

    // Camera permission and capture
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { onFieldUpdate { copy(fotosUris = fotosUris + it) } }
    }
    fun launchCamera() {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
            return
        }
        val file = File(context.cacheDir, "camera/photo_${System.currentTimeMillis()}.jpg")
            .also { it.parentFile?.mkdirs() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        takePictureLauncher.launch(uri)
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = maxOf(2, 5 - formState.fotosUris.size - formState.fotosUrls.size)
        )
    ) { uris ->
        uris.forEach { uri ->
            val totalCount = formState.fotosUris.size + formState.fotosUrls.size
            if (totalCount < 5) onFieldUpdate { copy(fotosUris = fotosUris + uri) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) stringResource(R.string.registro_form_title_edit)
                        else stringResource(R.string.registro_form_title_new)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = onCrisisResources) {
                        Icon(Icons.Outlined.FavoriteBorder, stringResource(R.string.crisis_resources_button_cd))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ── Meal type ──
            MealTypeDropdown(
                selected = formState.tipoComida,
                onSelected = { onFieldUpdate { copy(tipoComida = it) } }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Description ──
            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { onFieldUpdate { copy(descripcion = it) } },
                label = { Text(stringResource(R.string.description_label)) },
                placeholder = { Text(stringResource(R.string.description_hint)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 2,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.lugar,
                onValueChange = { onFieldUpdate { copy(lugar = it) } },
                label = { Text(stringResource(R.string.location_label)) },
                placeholder = { Text(stringResource(R.string.location_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.acompanantes,
                onValueChange = { onFieldUpdate { copy(acompanantes = it) } },
                label = { Text(stringResource(R.string.companions_label)) },
                placeholder = { Text(stringResource(R.string.companions_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Photos ──
            PhotoRow(
                localUris = formState.fotosUris,
                uploadedUrls = formState.fotosUrls,
                onTakePhoto = { launchCamera() },
                onChooseGallery = {
                    galleryLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemoveLocal = { uri -> onFieldUpdate { copy(fotosUris = fotosUris - uri) } },
                onRemoveUploaded = { url -> onFieldUpdate { copy(fotosUrls = fotosUrls - url) } },
                onPhotoTapped = onPhotoTapped
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Behavioral flags ──
            SectionTitle(stringResource(R.string.behavioral_section_title))
            AtraconSelector(
                selected = formState.fueAtracon,
                onSelected = { onFieldUpdate { copy(fueAtracon = it) } }
            )
            if (formState.fueAtracon == FueAtracon.SI) {
                OutlinedTextField(
                    value = formState.desencadenanteAtracon,
                    onValueChange = { onFieldUpdate { copy(desencadenanteAtracon = it) } },
                    label = { Text(stringResource(R.string.atracon_trigger_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            SwitchRow(
                label = stringResource(R.string.purga_label),
                checked = formState.deseosPurgar,
                onCheckedChange = { onFieldUpdate { copy(deseosPurgar = it) } }
            )
            if (formState.deseosPurgar) {
                SwitchRow(
                    label = stringResource(R.string.purga_actuo_label),
                    checked = formState.actuoSobrePurga,
                    onCheckedChange = { onFieldUpdate { copy(actuoSobrePurga = it) } }
                )
            }
            SwitchRow(
                label = stringResource(R.string.chequeo_label),
                checked = formState.checqueoCuerpo,
                onCheckedChange = { onFieldUpdate { copy(checqueoCuerpo = it) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Emotions ──
            EmotionPicker(
                label = stringResource(R.string.emotions_before_label),
                selected = formState.emocionesAntes,
                onSelectionChanged = { onFieldUpdate { copy(emocionesAntes = it) } }
            )
            Spacer(modifier = Modifier.height(12.dp))
            EmotionPicker(
                label = stringResource(R.string.emotions_after_label),
                selected = formState.emocionesDespues,
                onSelectionChanged = { onFieldUpdate { copy(emocionesDespues = it) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Thoughts ──
            OutlinedTextField(
                value = formState.pensamientos,
                onValueChange = { onFieldUpdate { copy(pensamientos = it) } },
                label = { Text(stringResource(R.string.thoughts_label)) },
                placeholder = { Text(stringResource(R.string.thoughts_hint)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.comentariosExternos,
                onValueChange = { onFieldUpdate { copy(comentariosExternos = it) } },
                label = { Text(stringResource(R.string.external_comments_label)) },
                placeholder = { Text(stringResource(R.string.external_comments_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Additional notes ──
            OutlinedTextField(
                value = formState.notasAdicionales,
                onValueChange = { onFieldUpdate { copy(notasAdicionales = it) } },
                label = { Text(stringResource(R.string.additional_notes_label)) },
                placeholder = { Text(stringResource(R.string.additional_notes_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
            SwitchRow(
                label = stringResource(R.string.notes_private_label),
                checked = formState.notasEsPrivada,
                onCheckedChange = { onFieldUpdate { copy(notasEsPrivada = it) } }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Visibility ──
            VisibilitySelector(
                selected = formState.visibilidad,
                onSelectionChanged = { onFieldUpdate { copy(visibilidad = it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ── Error message ──
            if (formState.errorMessage != null) {
                Text(
                    text = formState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Save button ──
            Button(
                onClick = { onSave(patientId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formState.isLoading
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.save_button))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeDropdown(
    selected: TipoComida?,
    onSelected: (TipoComida) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.meal_type_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TipoComida.entries.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo.displayName) },
                    onClick = { onSelected(tipo); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun AtraconSelector(selected: FueAtracon, onSelected: (FueAtracon) -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.atracon_label),
            style = MaterialTheme.typography.bodyMedium
        )
        FueAtracon.entries.forEach { option ->
            val label = when (option) {
                FueAtracon.SI -> stringResource(R.string.atracon_si)
                FueAtracon.NO -> stringResource(R.string.atracon_no)
                FueAtracon.NO_SE -> stringResource(R.string.atracon_no_se)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected == option, onClick = { onSelected(option) })
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
