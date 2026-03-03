package com.registro.alimentario.repository

import com.registro.alimentario.model.ComentarioClinico
import kotlinx.coroutines.flow.Flow

interface ComentarioRepository {
    fun getComentarios(registroId: String): Flow<List<ComentarioClinico>>
    suspend fun addComment(registroId: String, texto: String): Result<Unit>
}
