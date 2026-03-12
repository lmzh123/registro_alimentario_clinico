const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onCall } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp();

/**
 * Triggered when a new user document is created at /users/{uid}.
 * Sets the `role` custom claim on the Firebase Auth token.
 * The Android app forces a token refresh after role assignment.
 */
exports.syncRoleClaim = onDocumentCreated("users/{uid}", async (event) => {
  const uid = event.params.uid;
  const data = event.data?.data();
  if (!data) return;

  const role = data.role ?? "paciente";
  await getAuth().setCustomUserClaims(uid, { role });
  console.log(`Set custom claim role=${role} for uid=${uid}`);
});

/**
 * Called by the client after login to force a token refresh and ensure
 * the custom claim is present in the ID token.
 * Returns { role } so the client can navigate to the correct graph.
 */
exports.refreshRoleClaim = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) throw new Error("Unauthenticated");

  const db = getFirestore();
  const userDoc = await db.collection("users").doc(uid).get();
  const role = userDoc.data()?.role ?? "paciente";

  await getAuth().setCustomUserClaims(uid, { role });
  return { role };
});

/**
 * Triggered when a new registro is created.
 * - Sets lastRegistroAt on each active connection for the patient.
 * - Sends FCM push to each connected professional.
 */
exports.notifyProfessionalsOnNewRegistro = onDocumentCreated(
  "registros/{registroId}",
  async (event) => {
    const registro = event.data?.data();
    if (!registro) return;

    const patientId = registro.usuario_id;
    const db = getFirestore();
    const { getMessaging } = require("firebase-admin/messaging");

    // Get the patient's display name for the notification body
    const patientDoc = await db.collection("users").doc(patientId).get();
    const patientName = patientDoc.data()?.displayName || patientDoc.data()?.email || "Tu paciente";

    // Find all active connections for this patient
    const connectionsSnap = await db.collection("connections")
      .where("patientId", "==", patientId)
      .where("status", "==", "active")
      .get();

    if (connectionsSnap.empty) return;

    const batch = db.batch();
    const notifications = [];

    for (const connDoc of connectionsSnap.docs) {
      const conn = connDoc.data();

      // Update lastRegistroAt on the connection document
      batch.update(connDoc.ref, { lastRegistroAt: new Date() });

      // Fetch therapist FCM token
      const therapistDoc = await db.collection("users").doc(conn.therapistId).get();
      const fcmToken = therapistDoc.data()?.fcmToken;
      if (!fcmToken) continue;

      notifications.push(
        getMessaging().send({
          token: fcmToken,
          notification: {
            title: "Nuevo registro de paciente",
            body: `Tu paciente ${patientName} registró una nueva entrada.`,
          },
          data: {
            type: "new_registro",
            patientId,
            patientName,
          },
          android: { notification: { channelId: "professional_notifications" } },
        }).catch((err) => console.error(`FCM error for therapist ${conn.therapistId}:`, err))
      );
    }

    await batch.commit();
    await Promise.all(notifications);
    console.log(`Notified professionals for new registro by patient ${patientId}`);
  }
);

/**
 * Triggered when a new comment is added to a registro.
 * - Sends FCM push to the owning patient.
 * - Updates lastCommentAt on the registro.
 * - Sends FCM push to all connected professionals except the commenter.
 */
exports.notifyPatientOnComment = onDocumentCreated(
  "registros/{registroId}/comentarios/{comentarioId}",
  async (event) => {
    const registroId = event.params.registroId;
    const comment = event.data?.data();
    if (!comment) return;

    const db = getFirestore();
    const { getMessaging } = require("firebase-admin/messaging");

    const registroDoc = await db.collection("registros").doc(registroId).get();
    const registro = registroDoc.data();
    if (!registro) return;

    const patientUid = registro.usuario_id;
    const commenterId = comment.profesional_id;

    // Update lastCommentAt on the registro
    await db.collection("registros").doc(registroId).update({ lastCommentAt: new Date() });

    // Notify the patient
    const patientDoc = await db.collection("users").doc(patientUid).get();
    const patientFcmToken = patientDoc.data()?.fcmToken;
    if (patientFcmToken) {
      try {
        await getMessaging().send({
          token: patientFcmToken,
          notification: {
            title: "Nuevo comentario de tu equipo",
            body: `Tu equipo de ${comment.rol} dejó un comentario en uno de tus registros.`,
          },
          data: { registroId, type: "clinical_comment" },
          android: { notification: { channelId: "clinical_comments" } },
        });
        console.log(`Sent comment notification to patient ${patientUid}`);
      } catch (err) {
        console.error("Failed to send patient notification:", err);
      }
    }

    // Notify other professionals (active connections, excluding the commenter)
    const connectionsSnap = await db.collection("connections")
      .where("patientId", "==", patientUid)
      .where("status", "==", "active")
      .get();

    const professionalNotifications = [];
    for (const connDoc of connectionsSnap.docs) {
      const conn = connDoc.data();
      if (conn.therapistId === commenterId) continue;

      const therapistDoc = await db.collection("users").doc(conn.therapistId).get();
      const fcmToken = therapistDoc.data()?.fcmToken;
      if (!fcmToken) continue;

      professionalNotifications.push(
        getMessaging().send({
          token: fcmToken,
          notification: {
            title: "Nuevo comentario en un registro",
            body: "Un colega dejó un comentario en el registro de tu paciente.",
          },
          data: { registroId, type: "new_comment_for_professional" },
          android: { notification: { channelId: "professional_notifications" } },
        }).catch((err) => console.error(`FCM error for therapist ${conn.therapistId}:`, err))
      );
    }

    await Promise.all(professionalNotifications);
  }
);
