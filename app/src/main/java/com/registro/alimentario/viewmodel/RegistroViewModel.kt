package com.registro.alimentario.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.registro.alimentario.model.EmocionEntry
import com.registro.alimentario.model.FueAtracon
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.TipoComida
import com.registro.alimentario.repository.PhotoRepository
import com.registro.alimentario.repository.RegistroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RegistroFormState(
    val id: String = "",
    val tipoComida: TipoComida? = null,
    val descripcion: String = "",
    val lugar: String = "",
    val acompanantes: String = "",
    val fueAtracon: FueAtracon = FueAtracon.NO,
    val desencadenanteAtracon: String = "",
    val deseosPurgar: Boolean = false,
    val actuoSobrePurga: Boolean = false,
    val checqueoCuerpo: Boolean = false,
    val emocionesAntes: List<EmocionEntry> = emptyList(),
    val emocionesDespues: List<EmocionEntry> = emptyList(),
    val pensamientos: String = "",
    val comentariosExternos: String = "",
    val notasAdicionales: String = "",
    val notasEsPrivada: Boolean = true,
    val visibilidad: List<String> = emptyList(),
    val fotosUris: List<Uri> = emptyList(),          // pending local URIs (not yet uploaded)
    val fotosUrls: List<String> = emptyList(),        // already uploaded URLs
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
) {
    fun isValid(): Boolean = tipoComida != null && descripcion.isNotBlank()
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registroRepository: RegistroRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RegistroFormState())
    val formState: StateFlow<RegistroFormState> = _formState.asStateFlow()

    private val _registros = MutableStateFlow<List<Registro>>(emptyList())
    val registros: StateFlow<List<Registro>> = _registros.asStateFlow()

    fun loadPatientRegistros(patientId: String) {
        viewModelScope.launch {
            try {
                registroRepository.getRegistrosForPatient(patientId).collect { list ->
                    _registros.value = list
                }
            } catch (e: Exception) {
                // Flow closed with a Firestore error (e.g. permission denied, missing index).
                // Leave _registros as-is so the UI stays stable instead of crashing.
            }
        }
    }

    fun loadForEdit(registro: Registro) {
        _formState.value = RegistroFormState(
            id = registro.id,
            tipoComida = registro.tipoComida,
            descripcion = registro.descripcion,
            lugar = registro.lugar,
            acompanantes = registro.acompanantes,
            fueAtracon = registro.fueAtracon,
            desencadenanteAtracon = registro.desencadenanteAtracon,
            deseosPurgar = registro.deseosPurgar,
            actuoSobrePurga = registro.actuoSobrePurga,
            checqueoCuerpo = registro.checqueoCuerpo,
            emocionesAntes = registro.emocionesAntes,
            emocionesDespues = registro.emocionesDespues,
            pensamientos = registro.pensamientos,
            comentariosExternos = registro.comentariosExternos,
            notasAdicionales = registro.notasAdicionales,
            notasEsPrivada = registro.notasVisibilidad.isEmpty(),
            visibilidad = registro.visibilidad,
            fotosUrls = registro.fotos
        )
    }

    fun updateField(update: RegistroFormState.() -> RegistroFormState) {
        _formState.value = _formState.value.update()
    }

    fun addPhotoUri(uri: Uri) {
        val current = _formState.value
        val totalPhotos = current.fotosUris.size + current.fotosUrls.size
        if (totalPhotos >= 5) return
        _formState.value = current.copy(fotosUris = current.fotosUris + uri)
    }

    fun removeLocalPhoto(uri: Uri) {
        _formState.value = _formState.value.copy(
            fotosUris = _formState.value.fotosUris - uri
        )
    }

    fun removeUploadedPhoto(url: String) {
        _formState.value = _formState.value.copy(
            fotosUrls = _formState.value.fotosUrls - url
        )
    }

    fun save(patientId: String) {
        val state = _formState.value
        if (!state.isValid()) {
            _formState.value = state.copy(errorMessage = "El tipo de comida y la descripción son obligatorios")
            return
        }
        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, errorMessage = null)

            // Determine registroId for Storage path
            val registroId = state.id.ifBlank { UUID.randomUUID().toString() }

            // Upload pending photos
            val newUrls = mutableListOf<String>()
            for (uri in state.fotosUris) {
                photoRepository.uploadPhoto(registroId, uri).fold(
                    onSuccess = { newUrls.add(it) },
                    onFailure = {
                        _formState.value = _formState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al subir las fotos. Intentá de nuevo."
                        )
                        return@launch
                    }
                )
            }

            val allUrls = state.fotosUrls + newUrls
            val notasVisibilidad = if (state.notasEsPrivada) emptyList() else state.visibilidad

            val registro = Registro(
                id = registroId,
                usuarioId = patientId,
                fechaHora = Timestamp.now(),
                tipoComida = state.tipoComida!!,
                descripcion = state.descripcion,
                fotos = allUrls,
                lugar = state.lugar,
                acompanantes = state.acompanantes,
                fueAtracon = state.fueAtracon,
                desencadenanteAtracon = state.desencadenanteAtracon,
                deseosPurgar = state.deseosPurgar,
                actuoSobrePurga = state.actuoSobrePurga,
                checqueoCuerpo = state.checqueoCuerpo,
                emocionesAntes = state.emocionesAntes,
                emocionesDespues = state.emocionesDespues,
                pensamientos = state.pensamientos,
                comentariosExternos = state.comentariosExternos,
                notasAdicionales = state.notasAdicionales,
                notasVisibilidad = notasVisibilidad,
                visibilidad = state.visibilidad
            )

            val result = if (state.id.isBlank()) {
                registroRepository.createRegistro(registro)
            } else {
                registroRepository.updateRegistro(registro).map { registroId }
            }

            result.fold(
                onSuccess = {
                    _formState.value = RegistroFormState(savedSuccessfully = true)
                },
                onFailure = {
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        errorMessage = "No se pudo guardar el registro. Intentá de nuevo."
                    )
                }
            )
        }
    }

    fun deleteRegistro(registroId: String, photoUrls: List<String>) {
        viewModelScope.launch {
            registroRepository.deleteRegistro(registroId, photoUrls)
        }
    }

    fun resetForm() {
        _formState.value = RegistroFormState()
    }
}
