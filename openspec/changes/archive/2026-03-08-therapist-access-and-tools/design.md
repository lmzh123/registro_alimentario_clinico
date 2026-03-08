## Context

The app uses Firebase (Firestore + Auth) with Kotlin/Jetpack Compose on Android. Four roles exist: `paciente`, `nutricionista`, `psicologia`, `psiquiatria`. Each `Registro` has a `visibilidad: List<String>` that contains role IDs, controlling which professional roles can see that registro's fields.

Currently, `ProfessionalRepositoryImpl.getPatientsForProfessional` discovers a therapist's patients by scanning the `registros` collection for documents that include the therapist's role in `visibilidad`. This means:
- A therapist only discovers patients after the patient has shared at least one registro.
- Any therapist with a given role can access any patient who has shared with that role — there is no identity-level consent.
- Firestore rules cannot enforce per-therapist access without a relationship record.

The `User` model already has `pacienteIds` / `profesionalIds` fields, but they are not written or read anywhere in the current implementation.

## Goals / Non-Goals

**Goals:**
- Introduce a `connections` Firestore collection as the source of truth for patient–therapist relationships.
- Allow patients to discover therapists by email and send a connection request.
- Allow therapists to accept or decline incoming requests.
- Restrict therapist access to registros to only patients with an active connection.
- Allow patients to revoke an active connection at any time.
- Keep the per-registro `visibilidad` role filter working on top of the connection layer.

**Non-Goals:**
- No push notification for connection events in this change (FCM can be added later).
- No admin or clinic-wide patient assignment.
- No bulk import of patient lists.
- No changes to the registro visibility model (role-based field filtering is unchanged).

## Decisions

### D1 — Dedicated `connections` collection over updating `User` documents

**Choice**: A top-level `connections/{connectionId}` collection with fields: `patientId`, `therapistId`, `therapistRole`, `status` (`pending` | `active`), `createdAt`, `updatedAt`.

**Alternative**: Write arrays into each `User` document (`pacienteIds` / `profesionalIds`).

**Rationale**: A dedicated collection allows fine-grained Firestore security rules (`allow read if request.auth.uid == resource.data.patientId || request.auth.uid == resource.data.therapistId`), supports querying pending requests efficiently, and avoids document-size growth on `User` docs as patient/therapist lists grow. The existing `pacienteIds` / `profesionalIds` fields on `User` will be deprecated and ignored.

---

### D2 — Patient initiates the connection (not therapist)

**Choice**: Patient searches for a therapist by email, selects them, and sends a request. The therapist then accepts or declines.

**Alternative**: Therapist searches for patients and sends requests.

**Rationale**: Matches the consent model for a clinical tool — the patient is the data owner. A therapist initiating access would require the patient to be aware they are being contacted, adding complexity. Patient-initiated flow also aligns with clinical practice (the patient brings their therapist into the app).

---

### D3 — Firestore security rules gate registro reads on active connection

**Choice**: Update Firestore rules so a professional can read a registro only if: (a) `request.auth.uid == resource.data.usuario_id` (own record), OR (b) a `connections` document exists with `patientId == resource.data.usuario_id`, `therapistId == request.auth.uid`, and `status == "active"`.

**Alternative**: Enforce access only in the repository layer (application-level gate).

**Rationale**: Defense in depth. Application-layer enforcement alone is insufficient for a clinical app handling sensitive behavioral health data. Firestore rules provide a second layer that cannot be bypassed by client-side changes.

---

### D4 — New `ConnectionRepository` / `ConnectionViewModel`

**Choice**: Introduce `ConnectionRepository` interface + `ConnectionRepositoryImpl` and a `ConnectionViewModel` (Hilt-injected) to handle all connection CRUD.

**Alternative**: Extend `ProfessionalRepository` or `AuthRepository` with connection methods.

**Rationale**: Single-responsibility — connections are a distinct domain. A separate VM can be scoped to the connection management screens without polluting `ProfessionalViewModel` or `AuthViewModel`.

---

### D5 — Therapist lookup by email, not by name

**Choice**: Patient searches for a therapist by entering their exact email address. The app queries `users` where `email == <input>` and `role != "paciente"`.

**Alternative**: Free-text search by display name.

**Rationale**: Email is unique and unambiguous. Display-name search would require a full-text index or a prefix scan, adds typo-matching risk, and increases the risk of a patient connecting to the wrong person. For a clinical setting, exact email is safer and matches how professionals share credentials.

## Risks / Trade-offs

- **Risk**: Firestore rule complexity increases; a misconfigured rule could expose or block data → **Mitigation**: Add integration tests for the rules using the Firebase Emulator Suite; review rules with a security checklist before release.
- **Risk**: Email lookup requires scanning the `users` collection by email — this could be slow at scale → **Mitigation**: Add a Firestore index on `users.email`; this is acceptable for a clinical setting (small user base).
- **Risk**: A patient can spam connection requests to a therapist → **Mitigation**: Enforce a uniqueness check: only one `pending` or `active` connection per `(patientId, therapistId)` pair (enforced in Firestore rules and application layer).
- **Risk**: Existing professional screens show patients via registro scan; switching to connection-based lookup means therapists with no accepted connections will see an empty list until patients connect → **Mitigation**: Surface an explanatory empty state ("No tienes pacientes conectados. Los pacientes deben enviarte una solicitud de acceso.").
- **Trade-off**: Keeping `visibilidad` on registros while also requiring a `connection` adds two access gates. This is intentional (defense in depth) but means a patient could have a connection with a therapist yet still share zero registros with them — the therapist sees an empty registro list. This is correct behavior and should be explained in the UI.

## Migration Plan

1. Deploy updated Firestore rules (additive — new `connections` collection rules; existing registro rules tightened to require active connection).
2. Ship app update with new screens and updated `ProfessionalRepositoryImpl`.
3. Existing patients will need to re-connect with their therapists using the new flow. No automated migration of implied relationships (there is no reliable source of "who is whose therapist" in the current data).
4. **Rollback**: Revert Firestore rules to previous version; roll back app release. No data is deleted.

## Open Questions

- Should a therapist be able to see *which* patients have sent them a pending request before accepting, or only a count? (Current design: full list visible to therapist.)
- Should connection revocation by the patient automatically hide previously visible registros from the therapist in real-time? (Assumed yes — Firestore rules enforce this immediately on revocation.)
- Is an in-app notification (badge on therapist home) needed for pending requests in this change, or deferred to the FCM notification change?
