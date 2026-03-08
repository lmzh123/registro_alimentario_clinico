## Why

The app currently has a professional view that discovers patients by scanning registros where the patient shared them with a role category (nutricionista, psicología, psiquiatría). This is not workable in practice: a therapist has no way to know their patients exist until a patient has already shared something, patients cannot choose which specific therapist individuals can see their records, and there is no formal connection or consent model. A patient must be able to explicitly invite named therapists, and therapists must only see patients who have deliberately granted them access.

## What Changes

- Introduce a `connections` Firestore collection to track explicit patient–therapist relationships (pending / active states).
- Add a patient screen to search for registered therapists by email, send connection requests, view active connections, and revoke access.
- Add a therapist screen to view incoming connection requests and accept or decline them.
- Replace the current registro-[tasks.md](tasks.md)scan patient-discovery in `ProfessionalRepositoryImpl` with a relationship-based lookup that queries only patients with an active connection.
- Update Firestore security rules to enforce that a therapist can only read a patient's registros if an active connection exists between them.
- The existing per-registro `visibilidad` array (role-level field visibility) is kept unchanged — it continues to filter *what* a connected therapist sees within a registro.

## Capabilities

### New Capabilities
- `patient-connection-management`: Patient can search for therapist accounts by email, send connection requests, view their active therapist connections, and revoke access at any time.
- `therapist-connection-management`: Therapist can view pending connection requests from patients, accept or decline them, and their patient list is derived exclusively from active connections.

### Modified Capabilities
<!-- None — no existing spec files exist yet. -->

## Impact

- **Firestore**: New `connections` collection (`{id, patientId, therapistId, therapistRole, status, createdAt, updatedAt}`). Security rules updated to gate registro reads on active connection.
- **`ProfessionalRepositoryImpl`**: `getPatientsForProfessional` rewritten to query `connections` collection filtered by `therapistId` + `status == active`, then fetch user docs.
- **`ProfessionalRepository` interface**: May need a new method for accepting/declining requests.
- **`User` model**: `pacienteIds` / `profesionalIds` fields remain but are superseded by the `connections` collection; can be deprecated.
- **New screens**: `ManageTherapistsScreen` (patient), `ConnectionRequestsScreen` (therapist).
- **`ProfessionalViewModel`**: Extended with connection request actions.
- **`AuthViewModel` / `PatientViewModel`**: Extended or a new `ConnectionViewModel` added.
- **Firestore rules** (`firestore.rules`): Add rules for `connections` collection.
