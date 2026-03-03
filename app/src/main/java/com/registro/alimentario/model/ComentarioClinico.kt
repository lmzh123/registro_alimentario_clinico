package com.registro.alimentario.model

import com.google.firebase.Timestamp

data class ComentarioClinico(
    val id: String = "",
    val profesionalId: String = "",
    val rol: UserRole = UserRole.NUTRICIONISTA,
    val texto: String = "",
    val fecha: Timestamp = Timestamp.now()
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "profesional_id" to profesionalId,
        "rol" to rol.id,
        "texto" to texto,
        "fecha" to fecha
    )

    companion object {
        fun fromFirestoreMap(id: String, data: Map<String, Any?>): ComentarioClinico = ComentarioClinico(
            id = id,
            profesionalId = data["profesional_id"] as? String ?: "",
            rol = UserRole.fromId(data["rol"] as? String ?: "nutricionista"),
            texto = data["texto"] as? String ?: "",
            fecha = data["fecha"] as? Timestamp ?: Timestamp.now()
        )
    }
}
