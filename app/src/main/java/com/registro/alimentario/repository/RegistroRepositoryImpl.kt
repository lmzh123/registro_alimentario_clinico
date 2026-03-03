package com.registro.alimentario.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.registro.alimentario.model.Registro
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RegistroRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val photoRepository: PhotoRepository
) : RegistroRepository {

    private val registrosCollection get() = firestore.collection("registros")

    override fun getRegistrosForPatient(patientId: String): Flow<List<Registro>> = callbackFlow {
        val listener = registrosCollection
            .whereEqualTo("usuario_id", patientId)
            .orderBy("fecha_hora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val registros = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Registro.fromFirestoreMap(doc.id, it) }
                } ?: emptyList()
                trySend(registros)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createRegistro(registro: Registro): Result<String> {
        return try {
            val doc = registrosCollection.document()
            val withId = registro.copy(id = doc.id, createdAt = Timestamp.now(), updatedAt = Timestamp.now())
            doc.set(withId.toFirestoreMap()).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRegistro(registro: Registro): Result<Unit> {
        return try {
            val updated = registro.copy(updatedAt = Timestamp.now())
            registrosCollection.document(registro.id).set(updated.toFirestoreMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRegistro(registroId: String, photoUrls: List<String>): Result<Unit> {
        return try {
            // Delete all photos from Storage
            photoUrls.forEach { url ->
                photoRepository.deletePhoto(url)
            }
            // Delete comments subcollection documents
            val comments = registrosCollection.document(registroId).collection("comentarios").get().await()
            val batch = firestore.batch()
            comments.documents.forEach { batch.delete(it.reference) }
            // Delete the registro itself
            batch.delete(registrosCollection.document(registroId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRegistroById(registroId: String): Result<Registro> {
        return try {
            val doc = registrosCollection.document(registroId).get().await()
            val data = doc.data ?: return Result.failure(Exception("Registro no encontrado"))
            Result.success(Registro.fromFirestoreMap(doc.id, data))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
