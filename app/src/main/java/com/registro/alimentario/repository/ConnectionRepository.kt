package com.registro.alimentario.repository

import com.registro.alimentario.model.Connection
import com.registro.alimentario.model.User
import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    /** Search for a therapist by exact email. Returns null if not found or email belongs to a patient. */
    suspend fun searchTherapistByEmail(email: String): Result<User?>

    /**
     * Send a connection request from [patientId] to [therapistId].
     * Fails silently if a connection already exists for this pair.
     */
    suspend fun sendConnectionRequest(
        patientId: String,
        patientName: String,
        therapistId: String,
        therapistName: String,
        therapistRole: String
    ): Result<Unit>

    /** Real-time stream of all connections where this user is the patient. */
    fun getConnectionsForPatient(patientId: String): Flow<List<Connection>>

    /** Real-time stream of all connections where this user is the therapist. */
    fun getAllConnectionsForTherapist(therapistId: String): Flow<List<Connection>>

    /** Update a connection's status (e.g. "pending" → "active"). */
    suspend fun updateConnectionStatus(connectionId: String, status: String): Result<Unit>

    /** Delete a connection document (used for revoke / decline). */
    suspend fun deleteConnection(connectionId: String): Result<Unit>

    /** Fetch a single user document by UID. */
    suspend fun getUserById(uid: String): Result<User?>
}
