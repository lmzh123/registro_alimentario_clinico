package com.registro.alimentario.model

enum class UserRole(val id: String, val displayName: String) {
    PACIENTE("paciente", "Paciente"),
    NUTRICIONISTA("nutricionista", "Nutricionista"),
    PSICOLOGIA("psicologia", "Psicología"),
    PSIQUIATRIA("psiquiatria", "Psiquiatría");

    companion object {
        fun fromId(id: String): UserRole =
            entries.firstOrNull { it.id == id } ?: PACIENTE
    }
}
