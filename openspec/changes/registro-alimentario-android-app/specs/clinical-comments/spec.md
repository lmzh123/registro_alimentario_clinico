## ADDED Requirements

### Requirement: Professional adds clinical comment
The system SHALL allow any professional to add a timestamped comment to a registro they can access. Comments are stored in the `comentarios` subcollection of the Registro. Professionals SHALL NOT be able to edit or delete their own comments once submitted.

#### Scenario: Successful comment submission
- **WHEN** a professional types a comment and taps "Enviar"
- **THEN** a new document is created in `registros/{registroId}/comentarios` with the professional's UID, role, text, and server timestamp

#### Scenario: Empty comment rejected
- **WHEN** a professional attempts to submit an empty comment
- **THEN** the submit button SHALL be disabled and no document SHALL be created

#### Scenario: Comment from nutricionista on accessible registro
- **WHEN** a nutricionista comments on a registro where `visibilidad` includes `nutricionista`
- **THEN** the comment is saved with `rol: "nutricionista"`

---

### Requirement: Patient views clinical comments
The system SHALL allow a patient to read all clinical comments on their registros. Comments SHALL be displayed in the registro detail view, grouped by professional role, ordered chronologically.

#### Scenario: Patient sees comments on their registro
- **WHEN** a patient opens a registro that has clinical comments
- **THEN** all comments from all roles SHALL be visible to the patient

#### Scenario: No comments empty state
- **WHEN** a patient opens a registro with no comments
- **THEN** the comments section displays "Sin comentarios del equipo aún" or equivalent neutral message

---

### Requirement: Patient notified of new comment
The system SHALL send an in-app notification (and FCM push if enabled) to the patient when a new clinical comment is added to one of their registros.

#### Scenario: In-app notification on new comment
- **WHEN** a professional adds a comment and the patient's app is open
- **THEN** the patient SHALL receive a visible in-app notification indicating a new comment on a specific registro

#### Scenario: Push notification when app is in background
- **WHEN** a professional adds a comment and the patient's app is not in the foreground
- **THEN** a Firebase FCM push notification SHALL be sent to the patient's device; tapping the notification SHALL deep-link to the commented registro

---

### Requirement: Comments visible to other professionals
The system SHALL allow all professionals with access to a registro to read comments left by other professionals on that same registro.

#### Scenario: Cross-role comment visibility
- **WHEN** a psiquiatría user views a registro and a psicología user has already commented
- **THEN** the psicología comment SHALL be visible to psiquiatría (and vice versa)
