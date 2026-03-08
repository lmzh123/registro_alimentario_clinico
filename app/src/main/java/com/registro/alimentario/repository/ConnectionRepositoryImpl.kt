package com.registro.alimentario.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.registro.alimentario.model.Connection
import com.registro.alimentario.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import javax.inject.Inject

class ConnectionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ConnectionRepository {

    private val connections = firestore.collection("connections")
    private val users = firestore.collection("users")

    override suspend fun searchTherapistByEmail(email: String): Result<User?> {
        return try {
            // The security rule only allows patients to read professional profiles,
            // and only when the query is scoped to professional roles.
            // We must include the role filter in the query so Firestore can verify
            // every returned document satisfies the rule.
            val professionalRoles = listOf("nutricionista", "psicologia", "psiquiatria")
            val snapshot = users
                .whereEqualTo("email", email.trim().lowercase())
                .whereIn("role", professionalRoles)
                .get().await()
            val user = snapshot.documents.firstOrNull()?.let { doc ->
                doc.data?.let { User.fromFirestoreMap(doc.id, it) }
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendConnectionRequest(
        patientId: String,
        patientName: String,
        therapistId: String,
        therapistName: String,
        therapistRole: String
    ): Result<Unit> {
        return try {
            val docId = Connection.documentId(patientId, therapistId)
            Log.d("ConnectionRepo", "sendConnectionRequest: docId=$docId patientId=$patientId therapistId=$therapistId")
            val connection = Connection(
                id = docId,
                patientId = patientId,
                patientName = patientName,
                therapistId = therapistId,
                therapistName = therapistName,
                therapistRole = therapistRole,
                status = Connection.STATUS_PENDING
            )
            connections.document(docId).set(connection.toFirestoreMap()).await()
            Log.d("ConnectionRepo", "sendConnectionRequest: success")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ConnectionRepo", "sendConnectionRequest FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getConnectionsForPatient(patientId: String): Flow<List<Connection>> = callbackFlow {
        val listener = connections
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Connection.fromFirestoreMap(doc.id, it) }
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllConnectionsForTherapist(therapistId: String): Flow<List<Connection>> = callbackFlow {
        Log.d("ConnectionRepo", "getAllConnectionsForTherapist: listening for therapistId=$therapistId")
        val listener = connections
            .whereEqualTo("therapistId", therapistId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConnectionRepo", "getAllConnectionsForTherapist error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Connection.fromFirestoreMap(doc.id, it) }
                } ?: emptyList()
                Log.d("ConnectionRepo", "getAllConnectionsForTherapist: got ${list.size} connections")
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateConnectionStatus(connectionId: String, status: String): Result<Unit> {
        return try {
            connections.document(connectionId).update(
                mapOf("status" to status, "updatedAt" to Timestamp.now())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConnection(connectionId: String): Result<Unit> {
        return try {
            connections.document(connectionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(uid: String): Result<User?> {
        return try {
            val doc = users.document(uid).get().await()
            val user = doc.data?.let { User.fromFirestoreMap(doc.id, it) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
