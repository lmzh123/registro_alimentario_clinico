## 1. Data Model & Firestore

- [x] 1.1 Define `Connection` data class (`id`, `patientId`, `therapistId`, `therapistRole`, `status`, `createdAt`, `updatedAt`) in `model/Connection.kt` with `toFirestoreMap()` and `fromFirestoreMap()` methods
- [x] 1.2 Update `firestore.rules`: add rules for `connections` collection (patient or therapist can read their own connections; patient can create/delete; therapist can update `status`); add Firestore index on `users` collection for `email` field
- [x] 1.3 Tighten registro read rules: allow professional reads only when an active `connections` document exists with matching `patientId` and `therapistId`

## 2. Connection Repository

- [x] 2.1 Create `ConnectionRepository` interface with methods: `searchTherapistByEmail`, `sendConnectionRequest`, `getConnectionsForPatient` (Flow), `getConnectionsForTherapist` (Flow), `updateConnectionStatus`, `deleteConnection`
- [x] 2.2 Implement `ConnectionRepositoryImpl` using Firestore — `searchTherapistByEmail` queries `users` where `email == input` and `role != "paciente"`; `getConnectionsForPatient` / `getConnectionsForTherapist` use `whereEqualTo` + snapshot listeners; `sendConnectionRequest` checks for existing connection before creating
- [x] 2.3 Wire `ConnectionRepository` / `ConnectionRepositoryImpl` into the Hilt `AppModule` as a singleton binding

## 3. Connection ViewModel

- [x] 3.1 Create `ConnectionViewModel` (HiltViewModel) with states: `searchQuery`, `searchResult` (single User or null), `searchState` (idle/loading/found/notFound/error), `patientConnections` (Flow<List<Connection>>), `therapistPendingRequests` (Flow), `therapistActiveConnections` (Flow)
- [x] 3.2 Implement `searchTherapist(email: String)`, `sendRequest(therapistId, therapistRole)`, `revokeConnection(connectionId)`, `acceptRequest(connectionId)`, `declineRequest(connectionId)` actions in `ConnectionViewModel`
- [x] 3.3 Add duplicate-connection guard in `sendRequest`: check if a connection already exists for the `(currentPatientId, therapistId)` pair before calling the repository

## 4. Patient UI — Manage Therapists Screen

- [x] 4.1 Create `ManageTherapistsScreen` composable in `ui/patient/`: top section lists active and pending connections with revoke/cancel buttons; bottom section contains an email search field and "Buscar" button
- [x] 4.2 Display therapist search results below the search field showing display name, email, and role; include "Enviar solicitud" button; disable or hide the button if a connection already exists with that therapist
- [x] 4.3 Add a confirmation dialog before revoking an active connection or cancelling a pending request
- [x] 4.4 Show appropriate empty state when patient has no connections ("Aún no tienes profesionales conectados. Busca a tu profesional por correo electrónico para invitarle.")
- [x] 4.5 Add `NavRoutes.MANAGE_THERAPISTS` route and wire `ManageTherapistsScreen` into `PatientGraph`, adding a navigation entry point from `RegistroHistoryScreen` (e.g., a settings icon or menu item)

## 5. Therapist UI — Connection Requests & Updated Patient List

- [x] 5.1 Update `ProfessionalHomeScreen` to show a "Solicitudes pendientes" section at the top when `therapistPendingRequests` is non-empty; each item shows patient name/email, date, and "Aceptar" / "Rechazar" buttons
- [x] 5.2 Add confirmation dialog before declining a request
- [x] 5.3 Update `ProfessionalViewModel.loadPatients()` (or replace with `ConnectionViewModel`) to source the patient list from active `connections` documents instead of scanning registros
- [x] 5.4 Show the connection-aware empty state on `ProfessionalHomeScreen` when there are no active connections ("Aún no tienes pacientes conectados. Los pacientes deben enviarte una solicitud de acceso.")
- [x] 5.5 Update `ProfessionalRepositoryImpl.getPatientsForProfessional()` to query `connections` where `therapistId == currentUid` and `status == "active"`, then fetch `User` docs for each `patientId`

## 6. Navigation

- [x] 6.1 Add `NavRoutes.MANAGE_THERAPISTS` constant and pattern to `NavRoutes.kt`
- [x] 6.2 Wire `ManageTherapistsScreen` into `PatientGraph` with `ConnectionViewModel` injected via `hiltViewModel()`
- [x] 6.3 Ensure `ProfessionalGraph` passes `ConnectionViewModel` (or observes the same VM) to `ProfessionalHomeScreen` for connection request handling

## 7. Cleanup

- [x] 7.1 Remove or deprecate the registro-scan patient discovery logic from `ProfessionalRepositoryImpl.getPatientsForProfessional()` (replaced in 5.5)
- [x] 7.2 Add string resources for all new UI labels (search placeholder, empty states, confirmation dialogs, status labels) to `res/values/strings.xml`
- [x] 7.3 Verify Firestore Emulator integration tests (or manual test matrix) cover: send request, accept, decline, revoke, duplicate-request guard, access-denied-without-connection scenario
