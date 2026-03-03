package com.registro.alimentario.model

enum class Emocion(val id: String, val displayName: String) {
    ANSIEDAD("ansiedad", "Ansiedad"),
    TRISTEZA("tristeza", "Tristeza"),
    ENOJO("enojo", "Enojo"),
    SOLEDAD("soledad", "Soledad"),
    ABURRIMIENTO("aburrimiento", "Aburrimiento"),
    ALEGRIA("alegria", "Alegría"),
    NEUTRALIDAD("neutralidad", "Neutralidad"),
    OTRO("otro", "Otro");

    companion object {
        fun fromId(id: String): Emocion =
            entries.firstOrNull { it.id == id } ?: OTRO
    }
}

data class EmocionEntry(
    val tipo: Emocion = Emocion.NEUTRALIDAD,
    val textoLibre: String = ""
)
