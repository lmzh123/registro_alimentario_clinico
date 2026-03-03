package com.registro.alimentario.repository

import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.User
import kotlinx.coroutines.flow.Flow

interface ProfessionalRepository {
    fun getSharedRegistrosForPatient(
        patientId: String,
        professionalRole: String
    ): Flow<List<Registro>>
    suspend fun getPatientsForProfessional(professionalRole: String): Result<List<User>>
}
