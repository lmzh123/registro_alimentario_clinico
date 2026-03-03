package com.registro.alimentario.model

enum class TipoComida(val id: String, val displayName: String) {
    DESAYUNO("desayuno", "Desayuno"),
    MEDIA_MANANA("media_manana", "Media mañana"),
    ALMUERZO("almuerzo", "Almuerzo"),
    MERIENDA("merienda", "Merienda"),
    CENA("cena", "Cena"),
    SNACK_NOCTURNO("snack_nocturno", "Snack nocturno"),
    OTRO("otro", "Otro");

    companion object {
        fun fromId(id: String): TipoComida =
            entries.firstOrNull { it.id == id } ?: OTRO
    }
}
