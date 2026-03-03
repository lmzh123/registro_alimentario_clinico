## ADDED Requirements

### Requirement: Optional meal reminder notifications
The system SHALL allow a patient to enable soft, optional reminder notifications to log a meal. Reminders SHALL be disabled by default and the patient SHALL be able to configure or disable them at any time with no friction.

#### Scenario: Reminders disabled by default
- **WHEN** a new patient account is created
- **THEN** meal reminders SHALL be off unless the patient explicitly enables them

#### Scenario: Patient enables reminders
- **WHEN** a patient navigates to notification settings and enables reminders
- **THEN** the system SHALL schedule FCM or local notifications at the configured times

#### Scenario: Patient disables reminders
- **WHEN** a patient turns off reminders in settings
- **THEN** all scheduled reminder notifications SHALL be cancelled immediately

---

### Requirement: Configurable reminder schedule
The system SHALL allow a patient to configure which times of day to receive meal reminders. Configuration SHALL map to the app's meal type enum (desayuno, almuerzo, cena, etc.) so the reminder is contextually relevant.

#### Scenario: Set reminder for specific meal time
- **WHEN** a patient enables a reminder for "Almuerzo" and sets it to 13:00
- **THEN** a daily notification SHALL be sent at 13:00 with a message referencing lunchtime

#### Scenario: Remove one meal reminder
- **WHEN** a patient disables the reminder for one meal type while keeping others active
- **THEN** only the disabled meal's notification is cancelled; other reminders remain active

---

### Requirement: Soft notification tone and language
The system SHALL use gentle, non-pressuring language in all reminder notifications. Notifications SHALL NOT imply obligation, shame, or urgency.

#### Scenario: Reminder message is supportive
- **WHEN** a meal reminder notification is delivered
- **THEN** the notification body SHALL use calm, optional language (e.g., "¿Cómo fue tu almuerzo hoy? Cuando quieras, puedes registrarlo.") and SHALL NOT use imperative commands or achievement framing

---

### Requirement: Clinical comment push notifications
The system SHALL send a push notification to the patient when a professional adds a new clinical comment to one of their registros (see clinical-comments spec). This notification is always enabled and not configurable.

#### Scenario: Comment notification delivered
- **WHEN** a professional submits a new comment and the patient's device is reachable
- **THEN** a push notification SHALL arrive with the professional's role and a generic message (not the comment text, for privacy)

#### Scenario: Tapping comment notification deep-links
- **WHEN** a patient taps the comment notification
- **THEN** the app SHALL open and navigate directly to the relevant registro detail screen
