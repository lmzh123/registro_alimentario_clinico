package com.registro.alimentario.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            if (fbUser == null) {
                trySend(null)
            } else {
                // We emit a minimal user here; full user data is loaded separately
                trySend(User(uid = fbUser.uid, email = fbUser.email ?: "", displayName = fbUser.displayName ?: ""))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun register(email: String, password: String, displayName: String, role: UserRole): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val fbUser = result.user ?: return Result.failure(Exception("Error al crear la cuenta"))

            val user = User(
                uid = fbUser.uid,
                displayName = displayName,
                email = fbUser.email ?: email.trim().lowercase(),
                role = role
            )
            firestore.collection("users").document(fbUser.uid).set(user.toFirestoreMap()).await()

            // Force token refresh to pick up any custom claim set by Cloud Function
            fbUser.getIdToken(true).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val fbUser = result.user ?: return Result.failure(Exception("Error al iniciar sesión"))

            // Force token refresh to ensure custom claims are current
            fbUser.getIdToken(true).await()

            val userDoc = firestore.collection("users").document(fbUser.uid).get().await()
            val user = if (userDoc.exists()) {
                User.fromFirestoreMap(fbUser.uid, userDoc.data ?: emptyMap())
            } else {
                User(uid = fbUser.uid, email = fbUser.email ?: "")
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserRole(): UserRole? {
        val fbUser = auth.currentUser ?: return null
        return try {
            // Prefer the JWT custom claim (set by Cloud Function in production)
            val tokenResult = fbUser.getIdToken(false).await()
            val claimRole = (tokenResult.claims["role"] as? String)?.let { UserRole.fromId(it) }
            if (claimRole != null) return claimRole

            // Fallback: read role from the Firestore user document
            val doc = firestore.collection("users").document(fbUser.uid).get().await()
            val roleString = doc.getString("role")
            roleString?.let { UserRole.fromId(it) }
        } catch (e: Exception) {
            null
        }
    }
}
