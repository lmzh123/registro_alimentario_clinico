## ADDED Requirements

### Requirement: Create food registry entry
The system SHALL allow a patient to create a new `Registro` capturing all fields defined in the data model. Only users with role `paciente` SHALL be able to create registros.

#### Scenario: Successful registro creation
- **WHEN** a patient fills in at minimum the meal type and description and taps "Guardar"
- **THEN** a new Registro document is saved to Firestore with `usuario_id` set to the current user's UID and `createdAt` set to the server timestamp

#### Scenario: Creation with no required fields
- **WHEN** a patient taps "Guardar" without selecting a meal type or entering a description
- **THEN** the system SHALL display inline validation messages and NOT submit the form

#### Scenario: Behavioral flags conditionally expand
- **WHEN** a patient selects "Sí" for `fue_atracon`
- **THEN** the system SHALL reveal a text field for `desencadenante_atracon`

#### Scenario: Purga conditional field
- **WHEN** a patient selects "Sí" for `deseos_purgar`
- **THEN** the system SHALL reveal a Yes/No selector for `actuo_sobre_purga`

---

### Requirement: Meal type selection
The system SHALL provide a fixed enum of meal types for the `tipo_comida` field: desayuno, media mañana, almuerzo, merienda, cena, snack nocturno, otro.

#### Scenario: Meal type is required
- **WHEN** a patient attempts to save without selecting a meal type
- **THEN** the system SHALL show a validation error on the meal type field

---

### Requirement: Emotion selector
The system SHALL provide a multi-select emotion picker for `emociones_antes` and `emociones_despues` with the following options: ansiedad, tristeza, enojo, soledad, aburrimiento, alegría, neutralidad, otro. Each selected emotion MAY have an optional free-text note.

#### Scenario: Multiple emotions selectable
- **WHEN** a patient selects two or more emotions from the picker
- **THEN** all selected emotions SHALL be saved in the array field

#### Scenario: Free text on "otro"
- **WHEN** a patient selects "otro" in the emotion picker
- **THEN** the system SHALL reveal a text input to describe the emotion

---

### Requirement: View own registro history
The system SHALL allow a patient to view a chronological list of their own registros, ordered from most recent to oldest.

#### Scenario: History list loads
- **WHEN** a patient opens the history screen
- **THEN** the system SHALL display all their registros ordered by `fecha_hora` descending

#### Scenario: Empty state
- **WHEN** a patient has no registros yet
- **THEN** the system SHALL display a welcoming empty state message (non-pressuring, no gamification)

---

### Requirement: Edit own registro
The system SHALL allow a patient to edit any of their own registros. Professionals SHALL NOT be able to edit any registro.

#### Scenario: Patient edits registro
- **WHEN** a patient opens a registro they own and taps "Editar"
- **THEN** the form is pre-filled with existing data and changes can be submitted

#### Scenario: Edit updates timestamp
- **WHEN** a patient saves changes to a registro
- **THEN** the `updatedAt` field SHALL be updated to the server timestamp

#### Scenario: Professional cannot edit
- **WHEN** a user with a professional role views a registro
- **THEN** no edit controls SHALL be visible or accessible

---

### Requirement: Delete own registro
The system SHALL allow a patient to delete any of their own registros, including all associated photos from Firebase Storage.

#### Scenario: Registro deleted with confirmation
- **WHEN** a patient taps "Eliminar" on a registro and confirms the deletion dialog
- **THEN** the Registro document, its comentarios subcollection, and all associated photo files in Storage SHALL be deleted

#### Scenario: Deletion cancelled
- **WHEN** a patient taps "Eliminar" but cancels the confirmation dialog
- **THEN** no data is deleted and the registro remains unchanged
