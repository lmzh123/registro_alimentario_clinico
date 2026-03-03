## ADDED Requirements

### Requirement: User registration
The system SHALL allow new users to register with email and password via Firebase Auth. Upon registration, the system SHALL assign the `paciente` role by default until a clinical admin assigns a professional role.

#### Scenario: Successful patient registration
- **WHEN** a user submits a valid email and password on the registration screen
- **THEN** a Firebase Auth account is created, the user is assigned role `paciente`, and they are navigated to the patient home screen

#### Scenario: Registration with duplicate email
- **WHEN** a user attempts to register with an email already in use
- **THEN** the system SHALL display a non-technical error message without revealing whether the email exists in the system

#### Scenario: Registration with invalid password
- **WHEN** a user submits a password shorter than 8 characters
- **THEN** the system SHALL display an inline validation message before submitting to Firebase

---

### Requirement: User login
The system SHALL allow registered users to log in with email and password. After successful login, the system SHALL route the user to the appropriate nav graph based on their role.

#### Scenario: Paciente login routes to patient home
- **WHEN** a user with role `paciente` logs in successfully
- **THEN** the system SHALL navigate to the PatientGraph home screen

#### Scenario: Professional login routes to professional dashboard
- **WHEN** a user with role `nutricionista`, `psicologia`, or `psiquiatria` logs in successfully
- **THEN** the system SHALL navigate to the ProfessionalGraph home screen

#### Scenario: Login with wrong credentials
- **WHEN** a user submits an incorrect password or unregistered email
- **THEN** the system SHALL display a generic "credenciales incorrectas" message (no distinction between wrong email vs wrong password)

---

### Requirement: Session persistence
The system SHALL persist the authenticated session across app restarts so users do not need to log in on every launch.

#### Scenario: Returning authenticated user
- **WHEN** a user reopens the app and their Firebase Auth session is still valid
- **THEN** the system SHALL skip the login screen and navigate directly to their home screen

---

### Requirement: Logout
The system SHALL allow any user to log out from the app settings. Logout SHALL clear all local cached data.

#### Scenario: Successful logout
- **WHEN** a user taps "Cerrar sesión"
- **THEN** the Firebase Auth session is revoked, local cache is cleared, and the user is navigated to the login screen

---

### Requirement: Role-based navigation guard
The system SHALL prevent a user from accessing screens belonging to a different role's nav graph, even via deep link or back stack manipulation.

#### Scenario: Patient attempts professional screen
- **WHEN** a user with role `paciente` navigates to a professional-only route
- **THEN** the system SHALL redirect them to their home screen and NOT display the professional content