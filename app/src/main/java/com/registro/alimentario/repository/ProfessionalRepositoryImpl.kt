package com.registro.alimentario.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfessionalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfessionalRepository {

    override fun getSharedRegistrosForPatient(
        patientId: String,
        professionalRole: String
    ): Flow<List<Registro>> = callbackFlow {
        val listener = firestore.collection("registros")
            .whereEqualTo("usuario_id", patientId)
            .whereArrayContains("visibilidad", professionalRole)
            .orderBy("fecha_hora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val registros = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Registro.fromFirestoreMap(doc.id, it) }
                } ?: emptyList()
                trySend(registros)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getPatientsForProfessional(professionalRole: String): Result<List<User>> {
        return try {
            // Find all registros where visibilidad contains this role, then get unique patient UIDs
            val registros = firestore.collection("registros")
                .whereArrayContains("visibilidad", professionalRole)
                .get()
                .await()

            val patientIds = registros.documents
                .mapNotNull { it.getString("usuario_id") }
                .distinct()

            val users = patientIds.mapNotNull { uid ->
                val doc = firestore.collection("users").document(uid).get().await()
                doc.data?.let { User.fromFirestoreMap(uid, it) }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
