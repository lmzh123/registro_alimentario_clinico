package com.registro.alimentario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.registro.alimentario.model.ComentarioClinico
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.repository.ComentarioRepository
import com.registro.alimentario.repository.ProfessionalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class RegistroFilter(
    val fueAtracon: Boolean = false,
    val deseosPurgar: Boolean = false,
    val actuoSobrePurga: Boolean = false,
    val checqueoCuerpo: Boolean = false,
    val startDate: Date? = null,
    val endDate: Date? = null
)

@HiltViewModel
class ProfessionalViewModel @Inject constructor(
    private val professionalRepository: ProfessionalRepository,
    private val comentarioRepository: ComentarioRepository
) : ViewModel() {

    private val _patients = MutableStateFlow<List<User>>(emptyList())
    val patients: StateFlow<List<User>> = _patients.asStateFlow()

    private val _allRegistros = MutableStateFlow<List<Registro>>(emptyList())
    private val _filter = MutableStateFlow(RegistroFilter())

    private val _filteredRegistros = MutableStateFlow<List<Registro>>(emptyList())
    val filteredRegistros: StateFlow<List<Registro>> = _filteredRegistros.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _commentSent = MutableStateFlow(false)
    val commentSent: StateFlow<Boolean> = _commentSent.asStateFlow()

    private val _comments = MutableStateFlow<List<ComentarioClinico>>(emptyList())
    val comments: StateFlow<List<ComentarioClinico>> = _comments.asStateFlow()

    private val _editingCommentId = MutableStateFlow<String?>(null)
    val editingCommentId: StateFlow<String?> = _editingCommentId.asStateFlow()

    private val _editingCommentText = MutableStateFlow("")
    val editingCommentText: StateFlow<String> = _editingCommentText.asStateFlow()

    fun loadPatients() {
        val therapistId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            professionalRepository.getPatientsForProfessional(therapistId).fold(
                onSuccess = { _patients.value = it },
                onFailure = { /* surface error via snackbar in UI */ }
            )
        }
    }

    fun loadRegistrosForPatient(patientId: String, professionalRole: String) {
        viewModelScope.launch {
            professionalRepository.getSharedRegistrosForPatient(patientId, professionalRole).collect { list ->
                _allRegistros.value = list
                applyFilter()
            }
        }
    }

    fun setFilter(filter: RegistroFilter) {
        _filter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val f = _filter.value
        _filteredRegistros.value = _allRegistros.value.sortedByDescending { it.fechaHora.toDate() }.filter { r ->
            (if (f.fueAtracon) r.fueAtracon.id == "si" else true) &&
            (if (f.deseosPurgar) r.deseosPurgar else true) &&
            (if (f.actuoSobrePurga) r.actuoSobrePurga else true) &&
            (if (f.checqueoCuerpo) r.checqueoCuerpo else true) &&
            (f.startDate?.let { r.fechaHora.toDate() >= it } ?: true) &&
            (f.endDate?.let { r.fechaHora.toDate() <= it } ?: true)
        }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun submitComment(registroId: String) {
        val text = _commentText.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            comentarioRepository.addComment(registroId, text).fold(
                onSuccess = {
                    _commentText.value = ""
                    _commentSent.value = true
                },
                onFailure = { /* surface error */ }
            )
        }
    }

    fun resetCommentSent() {
        _commentSent.value = false
    }

    fun loadComments(registroId: String) {
        viewModelScope.launch {
            comentarioRepository.getComentarios(registroId).collect { _comments.value = it }
        }
    }

    fun startEditComment(comment: ComentarioClinico) {
        _editingCommentId.value = comment.id
        _editingCommentText.value = comment.texto
    }

    fun updateEditingCommentText(text: String) {
        _editingCommentText.value = text
    }

    fun cancelEditComment() {
        _editingCommentId.value = null
        _editingCommentText.value = ""
    }

    fun submitEditComment(registroId: String) {
        val commentId = _editingCommentId.value ?: return
        val text = _editingCommentText.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            comentarioRepository.updateComment(registroId, commentId, text).fold(
                onSuccess = { cancelEditComment() },
                onFailure = { /* surface error */ }
            )
        }
    }

    fun deleteComment(registroId: String, commentId: String) {
        viewModelScope.launch {
            comentarioRepository.deleteComment(registroId, commentId)
        }
    }
}
