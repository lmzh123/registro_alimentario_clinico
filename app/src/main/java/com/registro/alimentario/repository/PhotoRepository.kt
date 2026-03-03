package com.registro.alimentario.repository

import android.net.Uri

interface PhotoRepository {
    suspend fun uploadPhoto(registroId: String, uri: Uri): Result<String>
    suspend fun deletePhoto(downloadUrl: String): Result<Unit>
}
