package com.registro.alimentario.model

import com.google.firebase.Timestamp

enum class FueAtracon(val id: String) {
    SI("si"), NO("no"), NO_SE("no_se");
    companion object { fun fromId(id: String) = entries.firstOrNull { it.id == id } ?: NO }
}

data class Registro(
    val id: String = "",
    val usuarioId: String = "",
    val fechaHora: Timestamp = Timestamp.now(),
    val tipoComida: TipoComida = TipoComida.OTRO,
    val descripcion: String = "",
    val fotos: List<String> = emptyList(),
    val lugar: String = "",
    val acompanantes: String = "",
    // Behavioral flags
    val fueAtracon: FueAtracon = FueAtracon.NO,
    val desencadenanteAtracon: String = "",
    val deseosPurgar: Boolean = false,
    val actuoSobrePurga: Boolean = false,
    val checqueoCuerpo: Boolean = false,
    // Emotions
    val emocionesAntes: List<EmocionEntry> = emptyList(),
    val emocionesDespues: List<EmocionEntry> = emptyList(),
    // Thoughts and comments
    val pensamientos: String = "",
    val comentariosExternos: String = "",
    // Additional notes with their own visibility
    val notasAdicionales: String = "",
    val notasVisibilidad: List<String> = emptyList(),
    // Access control
    val visibilidad: List<String> = emptyList(),
    // Timestamps
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastCommentAt: Timestamp? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "usuario_id" to usuarioId,
        "fecha_hora" to fechaHora,
        "tipo_comida" to tipoComida.id,
        "descripcion" to descripcion,
        "fotos" to fotos,
        "lugar" to lugar,
        "acompanantes" to acompanantes,
        "fue_atracon" to fueAtracon.id,
        "desencadenante_atracon" to desencadenanteAtracon,
        "deseos_purgar" to deseosPurgar,
        "actuo_sobre_purga" to actuoSobrePurga,
        "chequeo_cuerpo" to checqueoCuerpo,
        "emociones_antes" to emocionesAntes.map { mapOf("tipo" to it.tipo.id, "texto" to it.textoLibre) },
        "emociones_despues" to emocionesDespues.map { mapOf("tipo" to it.tipo.id, "texto" to it.textoLibre) },
        "pensamientos" to pensamientos,
        "comentarios_externos" to comentariosExternos,
        "notas_adicionales" to notasAdicionales,
        "notas_visibilidad" to notasVisibilidad,
        "visibilidad" to visibilidad,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, data: Map<String, Any?>): Registro {
            val emocionesAntesList = (data["emociones_antes"] as? List<*>)
                ?.filterIsInstance<Map<String, Any?>>()
                ?.map { EmocionEntry(Emocion.fromId(it["tipo"] as? String ?: ""), it["texto"] as? String ?: "") }
                ?: emptyList()
            val emocionesDespuesList = (data["emociones_despues"] as? List<*>)
                ?.filterIsInstance<Map<String, Any?>>()
                ?.map { EmocionEntry(Emocion.fromId(it["tipo"] as? String ?: ""), it["texto"] as? String ?: "") }
                ?: emptyList()
            return Registro(
                id = id,
                usuarioId = data["usuario_id"] as? String ?: "",
                fechaHora = data["fecha_hora"] as? Timestamp ?: Timestamp.now(),
                tipoComida = TipoComida.fromId(data["tipo_comida"] as? String ?: ""),
                descripcion = data["descripcion"] as? String ?: "",
                fotos = (data["fotos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                lugar = data["lugar"] as? String ?: "",
                acompanantes = data["acompanantes"] as? String ?: "",
                fueAtracon = FueAtracon.fromId(data["fue_atracon"] as? String ?: "no"),
                desencadenanteAtracon = data["desencadenante_atracon"] as? String ?: "",
                deseosPurgar = data["deseos_purgar"] as? Boolean ?: false,
                actuoSobrePurga = data["actuo_sobre_purga"] as? Boolean ?: false,
                checqueoCuerpo = data["chequeo_cuerpo"] as? Boolean ?: false,
                emocionesAntes = emocionesAntesList,
                emocionesDespues = emocionesDespuesList,
                pensamientos = data["pensamientos"] as? String ?: "",
                comentariosExternos = data["comentarios_externos"] as? String ?: "",
                notasAdicionales = data["notas_adicionales"] as? String ?: "",
                notasVisibilidad = (data["notas_visibilidad"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                visibilidad = (data["visibilidad"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now(),
                lastCommentAt = data["lastCommentAt"] as? Timestamp
            )
        }
    }
}
