## ADDED Requirements

### Requirement: Persistent crisis resources access
The system SHALL provide quick access to crisis support resources from any patient-facing screen at all times. The access point SHALL be consistently visible and require no more than two taps to reach the resources.

#### Scenario: Crisis resources reachable from home screen
- **WHEN** a patient is on the home screen
- **THEN** a clearly labeled crisis resources button or persistent element SHALL be visible without scrolling

#### Scenario: Crisis resources reachable from registro form
- **WHEN** a patient is actively filling out a registro form
- **THEN** the crisis resources access point SHALL remain visible (e.g., in the app bar or as a floating element)

---

### Requirement: Crisis resources content
The system SHALL display a list of crisis support resources including at minimum: a local emergency number, a mental health crisis hotline, and a text/chat support option. Content SHALL be reviewed and approved by the clinical team before deployment.

#### Scenario: Resources displayed with name and contact
- **WHEN** a patient opens the crisis resources screen
- **THEN** each resource SHALL show a name, description, and a tappable phone number or URL

#### Scenario: Phone number opens dialer
- **WHEN** a patient taps a crisis hotline phone number
- **THEN** the system SHALL open the device phone dialer pre-filled with that number

---

### Requirement: Non-alarming presentation
The system SHALL present crisis resources in a calm, non-alarming visual style consistent with the app's compassionate tone. The feature SHALL NOT use red alerts, alarms, or urgent styling in the passive/always-visible state.

#### Scenario: Crisis button uses neutral styling
- **WHEN** the crisis resources access point is displayed in its default passive state
- **THEN** it SHALL use the app's standard color palette without red/orange urgency colors

---

### Requirement: Resources configurable per region
The system SHALL support configurable crisis resource content so that the clinical team or deployer can update resource names, phone numbers, and URLs without a code change.

#### Scenario: Resources loaded from configuration
- **WHEN** the crisis resources screen loads
- **THEN** the content SHALL be read from a data source that can be updated independently of the app binary (e.g., Firestore config collection or a bundled JSON updated via assets)
