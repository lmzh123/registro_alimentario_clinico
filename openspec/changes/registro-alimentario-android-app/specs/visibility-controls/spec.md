## ADDED Requirements

### Requirement: Set registro visibility at creation
The system SHALL allow a patient to configure which professional roles can see a Registro before saving it. The `visibilidad` array on each Registro SHALL contain zero or more of: `nutricionista`, `psicologia`, `psiquiatria`. An empty array means the registro is private (only the patient can see it).

#### Scenario: Default visibility
- **WHEN** a patient creates a new registro without changing visibility settings
- **THEN** the registro SHALL be saved with `visibilidad: []` (private by default)

#### Scenario: Sharing with selected roles
- **WHEN** a patient selects one or more roles from the visibility selector
- **THEN** the `visibilidad` array SHALL contain exactly the selected role strings

#### Scenario: Private registro not visible to professionals
- **WHEN** a professional queries registros and a registro has `visibilidad: []`
- **THEN** that registro SHALL NOT appear in the professional's view, enforced by Firestore security rules

---

### Requirement: Update registro visibility after creation
The system SHALL allow a patient to update the `visibilidad` field of any of their own registros at any time after creation.

#### Scenario: Patient adds a role to existing registro
- **WHEN** a patient opens an existing registro and adds `psicologia` to the visibility settings
- **THEN** `psicologia` is added to the `visibilidad` array and the professional with that role can immediately access the registro

#### Scenario: Patient removes a role from registro
- **WHEN** a patient removes `nutricionista` from the visibility settings of an existing registro
- **THEN** `nutricionista` is removed from `visibilidad` and the nutricionista can no longer read that registro

---

### Requirement: Per-note visibility in additional notes
The system SHALL allow per-note visibility configuration on the `notas_adicionales` field — allowing the patient to choose whether additional notes are shared with all roles in `visibilidad` or restricted to a subset (or kept private).

#### Scenario: Notes visibility independent of registro visibility
- **WHEN** a patient marks `notas_adicionales` as private while the registro itself is shared with `nutricionista`
- **THEN** the nutricionista can see the registro but the `notas_adicionales` field SHALL be blank/hidden in their view

---

### Requirement: Visibility UI clarity
The system SHALL present visibility controls using clear, plain language that a non-technical patient can understand. Labels SHALL name the roles by their title, not technical identifiers.

#### Scenario: Visibility selector uses human-readable labels
- **WHEN** a patient views the visibility settings UI
- **THEN** checkboxes/toggles SHALL be labeled "Nutricionista", "Psicología", "Psiquiatría" — not role enum values
