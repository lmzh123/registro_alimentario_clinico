package com.registro.alimentario.navigation

object NavRoutes {
    // Auth
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Patient
    const val PATIENT_HOME = "patient_home"
    const val CREATE_REGISTRO = "create_registro"
    const val EDIT_REGISTRO = "edit_registro/{registroId}"
    const val REGISTRO_HISTORY = "registro_history"
    const val REGISTRO_DETAIL = "registro_detail/{registroId}"
    const val NOTIFICATION_SETTINGS = "notification_settings"

    // Professional
    const val PROFESSIONAL_HOME = "professional_home"
    const val PATIENT_REGISTRO_LIST = "patient_registro_list/{patientId}/{patientName}"
    const val REGISTRO_DETAIL_PROFESSIONAL = "registro_detail_professional/{registroId}"

    // Shared
    const val CRISIS_RESOURCES = "crisis_resources"
    const val PHOTO_VIEWER = "photo_viewer/{photoUrl}"

    // Deep link base
    const val DEEP_LINK_BASE = "registroalimentario://registro"

    fun editRegistro(registroId: String) = "edit_registro/$registroId"
    fun registroDetail(registroId: String) = "registro_detail/$registroId"
    fun patientRegistroList(patientId: String, patientName: String) =
        "patient_registro_list/$patientId/${patientName.encodeUrl()}"
    fun registroDetailProfessional(registroId: String) = "registro_detail_professional/$registroId"
    fun photoViewer(photoUrl: String) = "photo_viewer/${photoUrl.encodeUrl()}"

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}
