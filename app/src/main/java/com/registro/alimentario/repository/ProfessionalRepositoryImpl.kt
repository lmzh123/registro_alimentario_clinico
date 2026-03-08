package com.registro.alimentario.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.registro.alimentario.model.Registro
import com.registro.alimentario.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProfessionalRepo", "getSharedRegistrosForPatient error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val registros = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Registro.fromFirestoreMap(doc.id, it) }
                } ?: emptyList()
                trySend(registros)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getPatientsForProfessional(therapistId: String): Result<List<User>> {
        return try {
            // Derive patient list from active connections (replaced registro-scan approach)
            val connectionsSnapshot = firestore.collection("connections")
                .whereEqualTo("therapistId", therapistId)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val patientIds = connectionsSnapshot.documents
                .mapNotNull { it.getString("patientId") }
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
