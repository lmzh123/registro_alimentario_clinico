package com.registro.alimentario.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : PhotoRepository {

    override suspend fun uploadPhoto(registroId: String, uri: Uri): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("registros/$registroId/$fileName")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhoto(downloadUrl: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(downloadUrl).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Non-fatal: log and continue even if deletion fails
            Result.failure(e)
        }
    }
}
