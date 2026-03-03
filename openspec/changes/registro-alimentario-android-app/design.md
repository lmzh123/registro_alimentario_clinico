## Context

Greenfield Android application for eating disorder treatment support. Patients log meals with behavioral/emotional context and share records selectively with a clinical team. The app must enforce strict role-based data visibility (4 roles), handle sensitive clinical data, and maintain a non-stigmatizing UX throughout. There is no existing codebase.

Stack chosen per project plan: Kotlin + Jetpack Compose (UI), Firebase Firestore (database), Firebase Auth (identity), Firebase Storage (photos), Firebase FCM (push notifications).

## Goals / Non-Goals

**Goals:**
- Establish the Firebase project structure and Firestore security rules that enforce role-based access at the database layer (not only in the UI)
- Define the MVVM + Jetpack Compose architecture and navigation graph
- Specify the Firestore data model (collections, fields, subcollections) for `Registro` and users
- Document key dependency choices (DI, image loading, navigation) and their rationale
- Define the phased delivery approach matching the three phases in the app plan

**Non-Goals:**
- Line-by-line implementation — specs cover behavior; tasks cover steps
- Web or iOS clients
- Third-party EHR integration
- HIPAA/local health data compliance certification (required before public release, out of scope for development phase)
- Full offline-first sync (Phase 3 item)

## Decisions

### D1 — Firebase as sole backend

**Decision:** Use Firebase Firestore + Auth + Storage + FCM as the complete backend.

**Rationale:** The app plan recommends it as the MVP stack. Firebase provides a unified SDK, real-time listeners (useful for clinical comments), built-in auth, and security rules that can enforce field-level access — eliminating the need for a separate API server for the MVP.

**Alternatives considered:**
- Node.js + PostgreSQL: stronger relational model for complex queries, but requires managing a server, auth layer, and REST API — significant overhead for a solo/small team MVP.

---

### D2 — Firestore security rules enforce role-based access

**Decision:** Role-based visibility is enforced in Firestore security rules, not only in the application layer.

**Rationale:** The `visibilidad` array on each `Registro` must be tamper-proof. If access is enforced only in the UI, a malicious client could read fields it shouldn't. Firestore rules allow `request.auth.token.role` checks so even direct SDK calls are blocked.

**Implementation:**
- User's role stored as a Firebase Auth custom claim (`role`: `paciente` | `nutricionista` | `psicologia` | `psiquiatria`)
- Firestore rules: a professional can read a `Registro` only if their role appears in `visibilidad`
- Professionals can read `comentarios` subcollection freely; they cannot write to the parent `Registro` document
- Paciente can read/write only their own documents (`request.auth.uid == resource.data.usuario_id`)

---

### D3 — Firestore data model

**Top-level collections:**

```
/users/{uid}
  role: string
  displayName: string
  pacienteIds: [uid]      ← for professionals: which patients they follow
  profesionalIds: [uid]   ← for patients: which professionals they share with

/registros/{registroId}
  usuario_id: string
  fecha_hora: Timestamp
  tipo_comida: string (enum)
  descripcion: string
  fotos: [string]         ← Storage download URLs
  lugar: string
  acompanantes: string
  fue_atracon: string     ← "si" | "no" | "no_se"
  desencadenante_atracon: string (nullable)
  deseos_purgar: boolean
  actuo_sobre_purga: boolean (nullable)
  chequeo_cuerpo: boolean
  emociones_antes: [{tipo: string, texto: string}]
  emociones_despues: [{tipo: string, texto: string}]
  pensamientos: string
  comentarios_externos: string
  notas_adicionales: string
  visibilidad: [string]   ← roles that can read this registro
  createdAt: Timestamp
  updatedAt: Timestamp

/registros/{registroId}/comentarios/{comentarioId}
  profesional_id: string
  rol: string
  texto: string
  fecha: Timestamp
```

**Rationale:** Flat top-level `registros` collection (not nested under users) enables professional queries across a patient's records without requiring collection group queries with complex ownership checks. The `visibilidad` field gates access via security rules.

---

### D4 — MVVM with Unidirectional Data Flow

**Decision:** Jetpack Compose UI + ViewModel (Hilt-injected) + StateFlow/SharedFlow. Repository pattern abstracting Firestore calls.

**Rationale:** Standard Android architecture. Compose's reactive model maps naturally to StateFlow. Hilt reduces boilerplate and is the official DI solution for Android.

**Layer structure:**
```
UI (Composables)
  └── ViewModel (StateFlow<UiState>)
        └── Repository (suspend funs)
              └── Firebase SDK (Firestore, Storage, Auth)
```

---

### D5 — Navigation Compose with role-based nav graphs

**Decision:** Single-activity app with Navigation Compose. Two root nav graphs: `PatientGraph` and `ProfessionalGraph`, selected after auth based on user role.

**Rationale:** Keeps role-separated navigation clean. Deep links to specific registros (e.g., from a clinical comment notification) are handled via nested nav arguments.

---

### D6 — Photo handling with Coil + Firebase Storage

**Decision:** Capture/pick photos → upload to Firebase Storage → store download URL in Registro. Display with Coil.

**Rationale:** Coil is the standard Compose-compatible image loader. Firebase Storage handles CDN and access tokens. Photos are not stored on device after upload (reduces privacy risk).

**Limits:** Max 5 photos per Registro, each max 5 MB. Enforced client-side before upload.

---

### D7 — Phased delivery

**Phase 1 (MVP):** `user-auth`, `food-registry`, `photo-capture`, `visibility-controls`, `crisis-resources`
**Phase 2:** `professional-dashboard`, `clinical-comments`, `notifications`
**Phase 3:** Reporting, statistics, offline sync (future — out of current scope)

## Risks / Trade-offs

- **Firestore security rules complexity** → Write comprehensive rule unit tests using the Firebase Emulator Suite before deploying. Review rules with each capability that adds new access patterns.
- **Custom claims latency** — role stored as custom claim requires token refresh after role assignment → Force token refresh after role is set server-side (Cloud Function triggered on user creation).
- **No offline support in MVP** — app requires network → Show clear offline state; do not silently lose data. Address in Phase 3.
- **Photo storage costs** — Firebase Storage pricing scales with bandwidth → Enforce client-side size/count limits; consider compression before upload.
- **Clinical language** — UI copy must be reviewed by the clinical team before any patient use → Strings extracted to a dedicated `strings.xml`; no hardcoded copy in Composables, enabling a full text audit.
- **Data sensitivity** — Firestore contains sensitive clinical data → Enable Firestore audit logging; restrict Firebase console access to technical team only.

## Open Questions

- Which crisis resource content and phone numbers to include? (requires input from clinical team per deployment region)
- Should professionals be able to see all of a patient's registros or only ones explicitly shared? Current design: only explicitly shared (`visibilidad` must include their role).
- Cloud Functions needed for role assignment — who provisions them and how? (likely admin tool or manual Firebase console for MVP)