package com.registro.alimentario.repository

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

data class ProfessionalReads(
    val patientLastSeen: Map<String, Timestamp> = emptyMap(),
    val registroLastSeen: Map<String, Timestamp> = emptyMap()
)

interface ProfessionalReadRepository {
    fun observeReads(professionalId: String): Flow<ProfessionalReads>
    suspend fun markPatientAsSeen(professionalId: String, patientId: String)
    suspend fun markRegistroAsSeen(professionalId: String, registroId: String)
}
