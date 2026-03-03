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
 * Triggered when a new comment is added to a registro.
 * Sends an FCM push notification to the owning patient.
 */
exports.notifyPatientOnComment = onDocumentCreated(
  "registros/{registroId}/comentarios/{comentarioId}",
  async (event) => {
    const registroId = event.params.registroId;
    const comment = event.data?.data();
    if (!comment) return;

    const db = getFirestore();
    const registroDoc = await db.collection("registros").doc(registroId).get();
    const registro = registroDoc.data();
    if (!registro) return;

    const patientUid = registro.usuario_id;
    const patientDoc = await db.collection("users").doc(patientUid).get();
    const fcmToken = patientDoc.data()?.fcmToken;
    if (!fcmToken) return;

    const { getMessaging } = require("firebase-admin/messaging");
    const message = {
      token: fcmToken,
      notification: {
        title: "Nuevo comentario de tu equipo",
        body: `Tu equipo de ${comment.rol} dejó un comentario en uno de tus registros.`,
      },
      data: {
        registroId,
        type: "clinical_comment",
      },
      android: {
        notification: {
          channelId: "clinical_comments",
        },
      },
    };

    try {
      await getMessaging().send(message);
      console.log(`Sent comment notification to patient ${patientUid}`);
    } catch (err) {
      console.error("Failed to send notification:", err);
    }
  }
);
