package com.registro.alimentario.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.registro.alimentario.model.ComentarioClinico
import com.registro.alimentario.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComentarioRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ComentarioRepository {

    override fun getComentarios(registroId: String): Flow<List<ComentarioClinico>> = callbackFlow {
        val listener = firestore.collection("registros").document(registroId)
            .collection("comentarios")
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { ComentarioClinico.fromFirestoreMap(doc.id, it) }
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addComment(registroId: String, texto: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No autenticado"))
            val tokenResult = user.getIdToken(false).await()
            val role = tokenResult.claims["role"] as? String
                ?: firestore.collection("users").document(user.uid).get().await()
                    .getString("role")
                ?: return Result.failure(Exception("Rol no encontrado"))

            val comment = ComentarioClinico(
                profesionalId = user.uid,
                rol = UserRole.fromId(role),
                texto = texto,
                fecha = Timestamp.now()
            )
            firestore.collection("registros").document(registroId)
                .collection("comentarios")
                .add(comment.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateComment(registroId: String, commentId: String, texto: String): Result<Unit> {
        return try {
            firestore.collection("registros").document(registroId)
                .collection("comentarios").document(commentId)
                .update("texto", texto)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(registroId: String, commentId: String): Result<Unit> {
        return try {
            firestore.collection("registros").document(registroId)
                .collection("comentarios").document(commentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
