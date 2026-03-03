package com.registro.alimentario.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: UserRole = UserRole.PACIENTE,
    val pacienteIds: List<String> = emptyList(),
    val profesionalIds: List<String> = emptyList(),
    val fcmToken: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "displayName" to displayName,
        "email" to email,
        "role" to role.id,
        "pacienteIds" to pacienteIds,
        "profesionalIds" to profesionalIds,
        "fcmToken" to fcmToken
    )

    companion object {
        fun fromFirestoreMap(uid: String, data: Map<String, Any?>): User = User(
            uid = uid,
            displayName = data["displayName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            role = UserRole.fromId(data["role"] as? String ?: "paciente"),
            pacienteIds = (data["pacienteIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            profesionalIds = (data["profesionalIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            fcmToken = data["fcmToken"] as? String ?: ""
        )
    }
}
