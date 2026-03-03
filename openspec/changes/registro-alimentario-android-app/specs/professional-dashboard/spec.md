## ADDED Requirements

### Requirement: Professional views shared registros
The system SHALL allow a professional to view only the registros that patients have explicitly shared with their role (i.e., the professional's role string appears in `visibilidad`). Access is enforced at the Firestore security rule level.

#### Scenario: Nutricionista sees food-relevant fields only
- **WHEN** a user with role `nutricionista` opens a registro
- **THEN** the system SHALL display: fecha_hora, tipo_comida, descripcion, fotos, lugar, acompanantes, notas_adicionales (if shared), and clinical comments — behavioral flags and emotion fields SHALL NOT be shown

#### Scenario: Psicología sees emotional and behavioral fields
- **WHEN** a user with role `psicologia` opens a registro
- **THEN** the system SHALL display: fecha_hora, tipo_comida, emociones_antes, emociones_despues, pensamientos, comentarios_externos, fue_atracon, desencadenante_atracon, deseos_purgar, actuo_sobre_purga, chequeo_cuerpo, notas_adicionales (if shared), and clinical comments — the description and photos MAY be shown; food-specific details are secondary

#### Scenario: Psiquiatría sees all fields
- **WHEN** a user with role `psiquiatria` opens a registro
- **THEN** the system SHALL display all fields of the registro without restriction

---

### Requirement: Professional patient list
The system SHALL allow a professional to see a list of patients who have shared at least one registro with their role. The professional navigates to a patient's registro history from this list.

#### Scenario: Patient list shows active sharers
- **WHEN** a professional opens their home screen
- **THEN** only patients with at least one registro sharing with that professional's role SHALL appear

#### Scenario: Empty state for no patients
- **WHEN** a professional has no patients sharing registros with them
- **THEN** the system SHALL display a clear empty state message

---

### Requirement: Filter registros by behavioral flags
The system SHALL allow professionals (any role) to filter a patient's shared registros by behavioral flags: `fue_atracon`, `deseos_purgar`, `actuo_sobre_purga`, `chequeo_cuerpo`.

#### Scenario: Filter by atracón
- **WHEN** a professional applies the filter `fue_atracon = "si"`
- **THEN** only registros where `fue_atracon` equals `"si"` SHALL be shown in the list

#### Scenario: Multiple filters applied
- **WHEN** a professional applies more than one behavioral filter
- **THEN** only registros matching ALL selected filters SHALL be shown (AND logic)

---

### Requirement: Filter registros by date range
The system SHALL allow professionals to filter a patient's shared registros by a custom date range.

#### Scenario: Date range filter
- **WHEN** a professional selects a start and end date
- **THEN** only registros with `fecha_hora` within that range SHALL be shown

---

### Requirement: Read-only access enforcement
The system SHALL ensure that professionals cannot modify any registro data. Edit controls SHALL NOT appear in the professional UI. Write operations from professional accounts to registro documents SHALL be blocked by Firestore security rules.

#### Scenario: No edit affordance in professional UI
- **WHEN** a professional views a registro detail screen
- **THEN** no edit button, swipe-to-edit, or any modification control SHALL be present

#### Scenario: Firestore rule blocks professional write
- **WHEN** a professional client attempts to write to a Registro document directly
- **THEN** Firestore SHALL reject the write with a permission-denied error
