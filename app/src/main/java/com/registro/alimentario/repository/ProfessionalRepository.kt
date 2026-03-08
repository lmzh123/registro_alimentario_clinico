package com.registro.alimentario.repository

import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.User
import kotlinx.coroutines.flow.Flow

interface ProfessionalRepository {
    fun getSharedRegistrosForPatient(
        patientId: String,
        professionalRole: String
    ): Flow<List<Registro>>
    /** Returns patients who have an active connection with the given therapist UID. */
    suspend fun getPatientsForProfessional(therapistId: String): Result<List<User>>
}
