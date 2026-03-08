## ADDED Requirements

### Requirement: Therapist can view pending connection requests
The system SHALL display all pending connection requests sent to the authenticated therapist. For each request the therapist SHALL see: patient name (or email if name is blank) and the date the request was created. The list SHALL update in real time.

#### Scenario: Pending requests visible on therapist home
- **WHEN** a therapist logs in and has one or more pending connection requests
- **THEN** the therapist sees a section listing the pending requests with patient identity and request date

#### Scenario: No pending requests
- **WHEN** a therapist has no pending connection requests
- **THEN** the pending section is hidden or shows an empty state

---

### Requirement: Therapist can accept a connection request
The system SHALL allow a therapist to accept a pending connection request. Upon acceptance the `connections` document status SHALL be updated to `"active"`. The patient SHALL immediately appear in the therapist's patient list.

#### Scenario: Successful acceptance
- **WHEN** therapist taps "Aceptar" on a pending request
- **THEN** the `connections` document `status` is updated to `"active"` and the patient moves from the pending list to the active patient list

#### Scenario: Accepted patient appears in patient list
- **WHEN** a connection is accepted
- **THEN** the therapist's patient list includes the newly connected patient without requiring a manual refresh

---

### Requirement: Therapist can decline a connection request
The system SHALL allow a therapist to decline a pending connection request. Upon decline the `connections` document SHALL be deleted. The patient SHALL see the request disappear from their pending list.

#### Scenario: Successful decline
- **WHEN** therapist taps "Rechazar" on a pending request
- **THEN** the `connections` document is deleted and the patient no longer appears in pending or active lists for this therapist

#### Scenario: Decline requires confirmation
- **WHEN** therapist taps "Rechazar"
- **THEN** the system shows a confirmation dialog before deleting the document

---

### Requirement: Therapist patient list is derived from active connections
The system SHALL display only patients with whom the therapist has an active (`status: "active"`) connection. The patient list SHALL NOT include patients discovered by scanning registros. The list SHALL update in real time.

#### Scenario: Patient list shows only actively connected patients
- **WHEN** a therapist opens the home screen
- **THEN** only patients with `status: "active"` connections to this therapist are listed

#### Scenario: Empty patient list with explanation
- **WHEN** a therapist has no active connections
- **THEN** an empty state is shown explaining that patients must send a connection request ("Aún no tienes pacientes conectados. Los pacientes deben enviarte una solicitud de acceso.")

#### Scenario: Revoked connection removes patient from list
- **WHEN** a patient revokes an active connection
- **THEN** the patient disappears from the therapist's active patient list in real time

---

### Requirement: Firestore security rules enforce connection-based registro access
The system's Firestore security rules SHALL deny a therapist read access to a patient's registros unless an active `connections` document exists with `patientId == registro.usuario_id` and `therapistId == request.auth.uid`. The per-registro `visibilidad` role filter SHALL continue to apply on top of this connection gate.

#### Scenario: Access granted with active connection and matching visibilidad
- **WHEN** an active connection exists and the registro's visibilidad includes the therapist's role
- **THEN** the therapist can read the registro

#### Scenario: Access denied without active connection
- **WHEN** no active connection exists between therapist and patient, even if the registro's visibilidad includes the therapist's role
- **THEN** Firestore denies the read

#### Scenario: Connections collection access control
- **WHEN** any user attempts to read a connections document
- **THEN** access is granted only if `request.auth.uid == resource.data.patientId` or `request.auth.uid == resource.data.therapistId`