package com.registro.alimentario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.registro.alimentario.model.ComentarioClinico
import com.registro.alimentario.model.FueAtracon
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.RestriccionPrevia
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.repository.ComentarioRepository
import com.registro.alimentario.repository.ProfessionalReadRepository
import com.registro.alimentario.repository.ProfessionalRepository
import com.registro.alimentario.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

enum class StatsGranularity { WEEKLY, MONTHLY }

data class PeriodStats(
    val periodLabel: String,
    val totalRegistros: Int,
    val deseosPurgar: Int,
    val actuoSobrePurga: Int,
    val atracones: Int,
    val restriccionSalteComida: Int,
    val restriccionComiMenos: Int,
    val restriccionRetrase: Int,
    val restriccionNoHubo: Int
)

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
    private val comentarioRepository: ComentarioRepository,
    private val professionalReadRepository: ProfessionalReadRepository,
    private val connectionRepository: ConnectionRepository
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

    private val _patientBadges = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val patientBadges: StateFlow<Map<String, Boolean>> = _patientBadges.asStateFlow()

    private val _registroBadges = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val registroBadges: StateFlow<Map<String, Boolean>> = _registroBadges.asStateFlow()

    private val _statsGranularity = MutableStateFlow(StatsGranularity.WEEKLY)
    val statsGranularity: StateFlow<StatsGranularity> = _statsGranularity.asStateFlow()

    val periodStats: StateFlow<List<PeriodStats>> = combine(_allRegistros, _statsGranularity) { registros, granularity ->
        computePeriodStats(registros, granularity)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setStatsGranularity(g: StatsGranularity) { _statsGranularity.value = g }

    fun loadPatientBadges(therapistId: String) {
        viewModelScope.launch {
            connectionRepository.getAllConnectionsForTherapist(therapistId)
                .combine(professionalReadRepository.observeReads(therapistId)) { connections, reads ->
                    connections
                        .filter { it.status == com.registro.alimentario.model.Connection.STATUS_ACTIVE }
                        .associate { conn ->
                            val lastRegistroAt = conn.lastRegistroAt
                            val lastSeen = reads.patientLastSeen[conn.patientId]
                            val hasBadge = lastRegistroAt != null &&
                                (lastSeen == null || lastRegistroAt > lastSeen)
                            conn.patientId to hasBadge
                        }
                }.collect { _patientBadges.value = it }
        }
    }

    fun markPatientAsSeen(patientId: String) {
        val therapistId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            professionalReadRepository.markPatientAsSeen(therapistId, patientId)
            _patientBadges.value = _patientBadges.value.toMutableMap().also { it[patientId] = false }
        }
    }

    fun markRegistroAsSeen(registroId: String) {
        val therapistId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            professionalReadRepository.markRegistroAsSeen(therapistId, registroId)
            _registroBadges.value = _registroBadges.value.toMutableMap().also { it[registroId] = false }
        }
    }

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
        val therapistId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            professionalRepository.getSharedRegistrosForPatient(patientId, professionalRole)
                .combine(professionalReadRepository.observeReads(therapistId)) { list, reads ->
                    list to reads
                }.collect { (list, reads) ->
                    _allRegistros.value = list
                    applyFilter()
                    _registroBadges.value = list.associate { registro ->
                        val lastCommentAt = registro.lastCommentAt
                        val lastSeen = reads.registroLastSeen[registro.id]
                        val hasBadge = lastCommentAt != null &&
                            (lastSeen == null || lastCommentAt > lastSeen)
                        registro.id to hasBadge
                    }
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

    private fun computePeriodStats(registros: List<Registro>, granularity: StatsGranularity): List<PeriodStats> {
        if (registros.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val grouped = registros.groupBy { r ->
            calendar.time = r.fechaHora.toDate()
            if (granularity == StatsGranularity.WEEKLY)
                "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR).toString().padStart(2, '0')}"
            else
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH).toString().padStart(2, '0')}"
        }
        return grouped.entries
            .sortedBy { it.key }
            .map { (key, group) ->
                PeriodStats(
                    periodLabel = formatPeriodLabel(key, granularity),
                    totalRegistros = group.size,
                    deseosPurgar = group.count { it.deseosPurgar },
                    actuoSobrePurga = group.count { it.actuoSobrePurga },
                    atracones = group.count { it.fueAtracon == FueAtracon.SI },
                    restriccionSalteComida = group.count { it.restriccionPrevia == RestriccionPrevia.SALTE_COMIDA },
                    restriccionComiMenos = group.count { it.restriccionPrevia == RestriccionPrevia.COMI_MENOS },
                    restriccionRetrase = group.count { it.restriccionPrevia == RestriccionPrevia.RETRASE_COMIDA },
                    restriccionNoHubo = group.count { it.restriccionPrevia == RestriccionPrevia.NO_HUBO }
                )
            }
    }

    private fun formatPeriodLabel(key: String, granularity: StatsGranularity): String {
        return if (granularity == StatsGranularity.WEEKLY) {
            val week = key.substringAfter("-W").trimStart('0').ifEmpty { "0" }
            "Sem $week"
        } else {
            val parts = key.split("-")
            val year = parts.getOrNull(0) ?: ""
            val monthIndex = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            "${monthNames.getOrElse(monthIndex) { monthIndex.toString() }} $year"
        }
    }
}
