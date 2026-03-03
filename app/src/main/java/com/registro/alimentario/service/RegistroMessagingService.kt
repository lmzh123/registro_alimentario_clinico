package com.registro.alimentario.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.registro.alimentario.MainActivity
import com.registro.alimentario.R

class RegistroMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Persist the new FCM token to Firestore for the current user
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"]
        val registroId = message.data["registroId"]

        when (type) {
            "clinical_comment" -> showCommentNotification(registroId)
            else -> { /* unhandled message type */ }
        }
    }

    private fun showCommentNotification(registroId: String?) {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            android.net.Uri.parse("registroalimentario://registro/${registroId ?: ""}"),
            this,
            MainActivity::class.java
        )
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "clinical_comments")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notification_comment_title))
            .setContentText(getString(R.string.notification_comment_body, "tu equipo"))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
