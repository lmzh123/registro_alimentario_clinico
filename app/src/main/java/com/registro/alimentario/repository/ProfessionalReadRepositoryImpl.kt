package com.registro.alimentario.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfessionalReadRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfessionalReadRepository {

    override fun observeReads(professionalId: String): Flow<ProfessionalReads> = callbackFlow {
        val docRef = firestore.collection("professional_reads").document(professionalId)
        val listener = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot == null || !snapshot.exists()) {
                trySend(ProfessionalReads())
                return@addSnapshotListener
            }
            @Suppress("UNCHECKED_CAST")
            val patientMap = (snapshot.get("patientLastSeen") as? Map<String, Timestamp>) ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val registroMap = (snapshot.get("registroLastSeen") as? Map<String, Timestamp>) ?: emptyMap()
            trySend(ProfessionalReads(patientLastSeen = patientMap, registroLastSeen = registroMap))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun markPatientAsSeen(professionalId: String, patientId: String) {
        firestore.collection("professional_reads").document(professionalId)
            .set(
                mapOf("patientLastSeen" to mapOf(patientId to FieldValue.serverTimestamp())),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
    }

    override suspend fun markRegistroAsSeen(professionalId: String, registroId: String) {
        firestore.collection("professional_reads").document(professionalId)
            .set(
                mapOf("registroLastSeen" to mapOf(registroId to FieldValue.serverTimestamp())),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
    }
}
