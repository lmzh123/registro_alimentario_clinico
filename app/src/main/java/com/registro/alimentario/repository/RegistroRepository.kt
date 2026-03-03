package com.registro.alimentario.repository

import com.registro.alimentario.model.Registro
import kotlinx.coroutines.flow.Flow

interface RegistroRepository {
    fun getRegistrosForPatient(patientId: String): Flow<List<Registro>>
    suspend fun createRegistro(registro: Registro): Result<String>
    suspend fun updateRegistro(registro: Registro): Result<Unit>
    suspend fun deleteRegistro(registroId: String, photoUrls: List<String>): Result<Unit>
    suspend fun getRegistroById(registroId: String): Result<Registro>
}
