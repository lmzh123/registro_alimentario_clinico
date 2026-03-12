package com.registro.alimentario.model

import com.google.firebase.Timestamp

/**
 * Represents an explicit patient–therapist connection.
 * Document ID format: "{patientId}_{therapistId}" (deterministic, enables Firestore rules lookup).
 * Status values: "pending" (awaiting therapist acceptance) | "active" (accepted).
 */
data class Connection(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val therapistId: String = "",
    val therapistName: String = "",
    val therapistRole: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastRegistroAt: Timestamp? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "patientId" to patientId,
        "patientName" to patientName,
        "therapistId" to therapistId,
        "therapistName" to therapistName,
        "therapistRole" to therapistRole,
        "status" to status,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACTIVE = "active"

        fun documentId(patientId: String, therapistId: String) = "${patientId}_${therapistId}"

        fun fromFirestoreMap(id: String, data: Map<String, Any?>): Connection = Connection(
            id = id,
            patientId = data["patientId"] as? String ?: "",
            patientName = data["patientName"] as? String ?: "",
            therapistId = data["therapistId"] as? String ?: "",
            therapistName = data["therapistName"] as? String ?: "",
            therapistRole = data["therapistRole"] as? String ?: "",
            status = data["status"] as? String ?: STATUS_PENDING,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now(),
            lastRegistroAt = data["lastRegistroAt"] as? Timestamp
        )
    }
}
