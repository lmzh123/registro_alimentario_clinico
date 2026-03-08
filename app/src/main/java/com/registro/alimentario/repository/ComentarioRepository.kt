package com.registro.alimentario.repository

import com.registro.alimentario.model.ComentarioClinico
import kotlinx.coroutines.flow.Flow

interface ComentarioRepository {
    fun getComentarios(registroId: String): Flow<List<ComentarioClinico>>
    suspend fun addComment(registroId: String, texto: String): Result<Unit>
    suspend fun updateComment(registroId: String, commentId: String, texto: String): Result<Unit>
    suspend fun deleteComment(registroId: String, commentId: String): Result<Unit>
}
