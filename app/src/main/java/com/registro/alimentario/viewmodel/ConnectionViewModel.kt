package com.registro.alimentario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.registro.alimentario.model.Connection
import com.registro.alimentario.model.User
import com.registro.alimentario.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchState { IDLE, LOADING, FOUND, NOT_FOUND, ERROR }

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    // ─── Patient-side state ────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<User?>(null)
    val searchResult: StateFlow<User?> = _searchResult.asStateFlow()

    private val _searchState = MutableStateFlow(SearchState.IDLE)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    /** All connections (any status) for the current patient. */
    private val _patientConnections = MutableStateFlow<List<Connection>>(emptyList())
    val patientConnections: StateFlow<List<Connection>> = _patientConnections.asStateFlow()

    private val _requestSent = MutableStateFlow(false)
    val requestSent: StateFlow<Boolean> = _requestSent.asStateFlow()

    // ─── Therapist-side state ──────────────────────────────────────────────

    /** Pending connection requests (status = "pending") for the current therapist. */
    private val _therapistPendingRequests = MutableStateFlow<List<Connection>>(emptyList())
    val therapistPendingRequests: StateFlow<List<Connection>> = _therapistPendingRequests.asStateFlow()

    /** Active patients (status = "active") for the current therapist, as full User objects. */
    private val _therapistActivePatients = MutableStateFlow<List<User>>(emptyList())
    val therapistActivePatients: StateFlow<List<User>> = _therapistActivePatients.asStateFlow()

    /** Active connections for the current therapist (with lastRegistroAt). */
    private val _therapistActiveConnections = MutableStateFlow<List<Connection>>(emptyList())
    val therapistActiveConnections: StateFlow<List<Connection>> = _therapistActiveConnections.asStateFlow()

    // ─── Patient actions ───────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchState.value = SearchState.IDLE
            _searchResult.value = null
        }
    }

    fun searchTherapist() {
        val email = _searchQuery.value.trim()
        if (email.isBlank()) return
        _searchState.value = SearchState.LOADING
        viewModelScope.launch {
            connectionRepository.searchTherapistByEmail(email).fold(
                onSuccess = { user ->
                    _searchResult.value = user
                    _searchState.value = if (user != null) SearchState.FOUND else SearchState.NOT_FOUND
                },
                onFailure = {
                    _searchResult.value = null
                    _searchState.value = SearchState.ERROR
                }
            )
        }
    }

    fun sendRequest(
        patientId: String,
        patientName: String,
        therapistId: String,
        therapistName: String,
        therapistRole: String
    ) {
        // 3.3 Duplicate-connection guard: check existing connections before calling repository
        val alreadyConnected = _patientConnections.value.any { it.therapistId == therapistId }
        if (alreadyConnected) return

        viewModelScope.launch {
            connectionRepository.sendConnectionRequest(
                patientId = patientId,
                patientName = patientName,
                therapistId = therapistId,
                therapistName = therapistName,
                therapistRole = therapistRole
            ).fold(
                onSuccess = { _requestSent.value = true },
                onFailure = { /* surface error via snackbar in UI */ }
            )
        }
    }

    fun resetRequestSent() {
        _requestSent.value = false
    }

    fun revokeConnection(connectionId: String) {
        viewModelScope.launch {
            connectionRepository.deleteConnection(connectionId)
        }
    }

    fun loadPatientConnections(patientId: String) {
        viewModelScope.launch {
            connectionRepository.getConnectionsForPatient(patientId).collect { list ->
                _patientConnections.value = list
            }
        }
    }

    // ─── Therapist actions ─────────────────────────────────────────────────

    fun loadTherapistConnections(therapistId: String) {
        viewModelScope.launch {
            connectionRepository.getAllConnectionsForTherapist(therapistId).collect { list ->
                _therapistPendingRequests.value = list.filter { it.status == Connection.STATUS_PENDING }
                val activeConnections = list.filter { it.status == Connection.STATUS_ACTIVE }
                _therapistActiveConnections.value = activeConnections
                // Fetch full User objects for active patient IDs
                val patients = activeConnections.mapNotNull { conn ->
                    connectionRepository.getUserById(conn.patientId).getOrNull()
                }
                _therapistActivePatients.value = patients
            }
        }
    }

    fun acceptRequest(connectionId: String) {
        viewModelScope.launch {
            connectionRepository.updateConnectionStatus(connectionId, Connection.STATUS_ACTIVE)
        }
    }

    fun declineRequest(connectionId: String) {
        viewModelScope.launch {
            connectionRepository.deleteConnection(connectionId)
        }
    }
}
