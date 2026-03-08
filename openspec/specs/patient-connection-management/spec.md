## ADDED Requirements

### Requirement: Patient can search for a therapist by email
The system SHALL allow a patient to search for a registered therapist account by entering an exact email address. The search SHALL only return users whose role is NOT `paciente`. The system SHALL display the therapist's display name, email, and role.

#### Scenario: Therapist found by email
- **WHEN** patient enters a valid email that matches a registered therapist
- **THEN** the system displays the therapist's name, email, and professional role

#### Scenario: Email not found or belongs to a patient
- **WHEN** patient enters an email that does not match any therapist (including emails of patient accounts)
- **THEN** the system displays a message indicating no therapist was found with that email

#### Scenario: Empty email input
- **WHEN** patient attempts to search with an empty or whitespace-only email
- **THEN** the search action is disabled or produces a validation message; no Firestore query is issued

---

### Requirement: Patient can send a connection request to a therapist
The system SHALL allow a patient to send a connection request to a therapist found via email search. The request SHALL be stored as a `connections` document with `status: "pending"`. Only one pending or active connection between the same patient and therapist pair SHALL be allowed at any time.

#### Scenario: Successful connection request
- **WHEN** patient taps "Enviar solicitud" on a therapist result and no existing connection exists
- **THEN** a `connections` document is created with `status: "pending"`, `patientId`, `therapistId`, and `therapistRole` fields, and the UI confirms the request was sent

#### Scenario: Duplicate request prevented
- **WHEN** patient attempts to send a request to a therapist they already have a pending or active connection with
- **THEN** the system does not create a new document and shows an appropriate message ("Ya tienes una conexión con este profesional")

---

### Requirement: Patient can view their active and pending therapist connections
The system SHALL display a list of the patient's connections. For each connection the patient SHALL see: therapist name, therapist role, and connection status (pending or active). The list SHALL update in real time.

#### Scenario: Active connection displayed
- **WHEN** a connection has `status: "active"`
- **THEN** the patient sees the therapist listed with a visual indicator of active status

#### Scenario: Pending connection displayed
- **WHEN** a connection has `status: "pending"`
- **THEN** the patient sees the therapist listed with a visual indicator of pending status (awaiting therapist acceptance)

#### Scenario: No connections
- **WHEN** the patient has no connections in any status
- **THEN** an empty state is shown explaining how to invite a therapist

---

### Requirement: Patient can revoke an active or pending connection
The system SHALL allow a patient to revoke (delete) any of their connections regardless of status. After revocation, the therapist SHALL immediately lose access to the patient's registros (enforced by Firestore rules).

#### Scenario: Revoke active connection
- **WHEN** patient confirms revocation of an active connection
- **THEN** the `connections` document is deleted and the therapist no longer appears in the patient's list

#### Scenario: Cancel pending request
- **WHEN** patient confirms cancellation of a pending request before the therapist has accepted
- **THEN** the `connections` document is deleted and the request no longer appears for either party

#### Scenario: Revocation requires confirmation
- **WHEN** patient taps "Revocar acceso" on a connection
- **THEN** the system shows a confirmation dialog before deleting the document