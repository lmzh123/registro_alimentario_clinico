## 1. Project Setup & Firebase Configuration

- [x] 1.1 Create Android project with Kotlin + Jetpack Compose (API 26+ min SDK)
- [x] 1.2 Add Gradle dependencies: Firebase BOM, Firestore, Auth, Storage, FCM, Hilt, Navigation Compose, Coil, Coroutines
- [x] 1.3 Create Firebase project and connect `google-services.json`
- [ ] 1.4 Enable Firebase Auth (email/password provider) in Firebase console
- [x] 1.5 Enable Firestore in production mode and configure initial security rules (deny all)
- [x] 1.6 Enable Firebase Storage and configure default storage rules
- [ ] 1.7 Enable Firebase FCM and add server key to project settings
- [x] 1.8 Set up Firebase Emulator Suite for local development and testing
- [x] 1.9 Configure Hilt application class and base module setup

## 2. Project Architecture Scaffolding

- [x] 2.1 Define package structure: `ui/`, `viewmodel/`, `repository/`, `model/`, `di/`, `navigation/`
- [x] 2.2 Create `Registro` data class matching the Firestore data model (all fields from design.md)
- [x] 2.3 Create `User` data class with `uid`, `displayName`, `role`, `pacienteIds`, `profesionalIds`
- [x] 2.4 Create `ComentarioClinico` data class
- [x] 2.5 Define `TipoComida` enum (desayuno, media_manana, almuerzo, merienda, cena, snack_nocturno, otro)
- [x] 2.6 Define `Emocion` enum (ansiedad, tristeza, enojo, soledad, aburrimiento, alegria, neutralidad, otro)
- [x] 2.7 Define `UserRole` enum (paciente, nutricionista, psicologia, psiquiatria)
- [x] 2.8 Create base `Repository` interface and Hilt module for Firebase dependencies

## 3. User Authentication (user-auth spec)

- [x] 3.1 Implement `AuthRepository` with `register(email, password)`, `login(email, password)`, `logout()`, `currentUser()` using Firebase Auth
- [x] 3.2 Implement `AuthViewModel` with login, register, and logout state flows
- [x] 3.3 Build `LoginScreen` composable with email/password fields, submit button, and error display
- [x] 3.4 Build `RegisterScreen` composable with validation (min 8-char password, non-duplicate email handling)
- [x] 3.5 Implement role-based navigation: after login, read user role from Firestore `/users/{uid}` and navigate to PatientGraph or ProfessionalGraph
- [x] 3.6 Implement session persistence check in `MainActivity` (skip login if session is valid)
- [x] 3.7 Write Cloud Function (or admin script) to assign `paciente` role as custom claim on new user creation
- [x] 3.8 Add navigation guard: block access to wrong-role screens and redirect to home

## 4. Firestore Security Rules

- [x] 4.1 Write rule: paciente can read/write only their own `/registros` documents (`usuario_id == request.auth.uid`)
- [x] 4.2 Write rule: professional can read a registro only if their role is in the `visibilidad` array
- [x] 4.3 Write rule: professional cannot write to `/registros` documents (only the patient can)
- [x] 4.4 Write rule: professional can create documents in `/registros/{id}/comentarios` but not update/delete
- [x] 4.5 Write rule: patient can read all comentarios on their own registros
- [x] 4.6 Write rule: `/users/{uid}` readable only by the owning user and admin role; writable only by admin
- [ ] 4.7 Test all rules in Firebase Emulator using the Rules Playground or unit tests

## 5. Food Registry — Patient (food-registry spec)

- [x] 5.1 Implement `RegistroRepository` with `createRegistro()`, `updateRegistro()`, `deleteRegistro()`, `getRegistrosForPatient()` using Firestore
- [x] 5.2 Implement `RegistroViewModel` with create/edit/delete state flows and form validation
- [x] 5.3 Build `CreateRegistroScreen` composable: meal type selector, description field, location, companions
- [x] 5.4 Add behavioral flags section: atracón (sí/no/no_se) with conditional desencadenante text field; deseos_purgar toggle with conditional actuo_sobre_purga; chequeo_cuerpo toggle
- [x] 5.5 Add emotion section: multi-select picker for emociones_antes and emociones_despues with free-text on "otro"
- [x] 5.6 Add thoughts and external comments text fields; additional notes field
- [x] 5.7 Validate required fields (meal type + description) before allowing save
- [x] 5.8 Build `RegistroHistoryScreen` composable: chronological list with empty state
- [x] 5.9 Build `RegistroDetailScreen` composable: read-only view of all patient-owned fields
- [x] 5.10 Implement edit flow: pre-fill `CreateRegistroScreen` with existing data, update on save
- [x] 5.11 Implement delete flow: confirmation dialog, delete Registro + subcollections + Storage photos

## 6. Photo Capture & Upload (photo-capture spec)

- [x] 6.1 Add camera permission handling using `ActivityResultContracts.TakePicture`
- [x] 6.2 Add gallery picker using `ActivityResultContracts.PickMultipleVisualMedia` (Photo Picker API)
- [x] 6.3 Enforce max 5 photos and max 5 MB per photo client-side before upload
- [x] 6.4 Implement `PhotoRepository` with `uploadPhoto(uri): String` (returns Storage download URL) and `deletePhoto(url)`
- [x] 6.5 Integrate photo upload into `CreateRegistroScreen`: upload all photos before saving Registro
- [x] 6.6 Display photo thumbnails in a horizontal scrollable row within the form and detail views
- [x] 6.7 Implement full-screen photo viewer composable (tapped thumbnail opens overlay)
- [x] 6.8 Implement photo removal from form (before save) and from existing registro (triggers Storage delete)
- [x] 6.9 Handle upload failure: show error, do not save registro, allow retry

## 7. Visibility Controls (visibility-controls spec)

- [x] 7.1 Add visibility selector composable: three labeled toggles (Nutricionista, Psicología, Psiquiatría)
- [x] 7.2 Default visibility to empty array (private) when creating a new registro
- [x] 7.3 Integrate visibility selector into `CreateRegistroScreen` and `EditRegistroScreen`
- [x] 7.4 Add per-note visibility toggle to the `notas_adicionales` section: "solo para mí" vs "compartir con el equipo"
- [x] 7.5 Store notes visibility as a separate field (e.g., `notas_visibilidad: [roles]`) in the Registro document
- [x] 7.6 When rendering a registro for a professional, hide `notas_adicionales` if the professional's role is not in `notas_visibilidad`

## 8. Crisis Resources (crisis-resources spec)

- [x] 8.1 Create a `crisis_resources` configuration collection in Firestore (or bundled JSON in assets) with name, description, phone/URL per resource
- [x] 8.2 Build `CrisisResourcesScreen` composable with calm, neutral styling
- [x] 8.3 Make each phone number tappable (opens device dialer via `Intent.ACTION_DIAL`)
- [x] 8.4 Add persistent crisis resources access point to the patient app bar (top-level icon or button)
- [x] 8.5 Verify crisis resources button is accessible from `CreateRegistroScreen` and all patient screens
- [ ] 8.6 Populate initial crisis resource content in collaboration with the clinical team

## 9. Professional Dashboard (professional-dashboard spec)

- [x] 9.1 Build `ProfessionalHomeScreen` composable: list of patients sharing with this professional's role
- [x] 9.2 Implement `ProfessionalRepository` with `getPatientsForProfessional()` and `getSharedRegistrosForPatient(patientId)` querying Firestore with `array-contains` on `visibilidad`
- [x] 9.3 Build `PatientRegistroListScreen` composable: chronological list of shared registros for a selected patient
- [x] 9.4 Build `RegistroDetailProfessionalScreen` composable: role-filtered field display per spec (nutricionista vs psicología vs psiquiatría)
- [x] 9.5 Implement behavioral flag filters (fue_atracon, deseos_purgar, actuo_sobre_purga, chequeo_cuerpo) with AND logic
- [x] 9.6 Implement date range filter (start date / end date picker)
- [x] 9.7 Ensure no edit controls are present anywhere in the professional UI
- [x] 9.8 Handle empty state: no patients sharing with this role

## 10. Clinical Comments (clinical-comments spec)

- [x] 10.1 Implement `ComentarioRepository` with `addComment(registroId, text)` writing to `/registros/{id}/comentarios`
- [x] 10.2 Build comment input composable (text field + send button, disabled when empty) for professional views
- [x] 10.3 Build comment list composable showing all comments chronologically with role badge and timestamp
- [x] 10.4 Integrate comment section into `RegistroDetailProfessionalScreen`
- [x] 10.5 Display comment section in `RegistroDetailScreen` (patient view) showing all comments from all professionals
- [x] 10.6 Set up Firestore trigger or Cloud Function to send FCM push notification to patient when a new comment is added
- [ ] 10.7 Add in-app notification display (snackbar or notification banner) for new comments when patient app is open
- [x] 10.8 Deep-link from comment push notification to the specific registro detail screen

## 11. Notifications (notifications spec)

- [x] 11.1 Build notification settings screen for patients: enable/disable reminders, configure time per meal type
- [x] 11.2 Implement local notification scheduling using `AlarmManager` or `WorkManager` for each enabled meal reminder
- [x] 11.3 Cancel all scheduled reminders when patient disables notifications
- [x] 11.4 Write soft reminder notification copy in `strings.xml` (non-imperative, supportive tone, reviewed by clinical team)
- [x] 11.5 Register FCM `FirebaseMessagingService` to handle incoming push notifications (comments + potential future server messages)
- [x] 11.6 Request notification permission on Android 13+ (`POST_NOTIFICATIONS`) with explanatory rationale dialog

## 12. Navigation Graph

- [x] 12.1 Define `PatientGraph` nav graph: Home → CreateRegistro, RegistroHistory → RegistroDetail, CrisisResources, NotificationSettings
- [x] 12.2 Define `ProfessionalGraph` nav graph: Home (patient list) → PatientRegistroList → RegistroDetailProfessional
- [x] 12.3 Add deep-link route for registro detail (used by push notification tap)
- [x] 12.4 Implement root nav host that selects PatientGraph vs ProfessionalGraph after auth

## 13. Clinical Language & Accessibility Audit

- [x] 13.1 Extract all UI strings to `strings.xml` — zero hardcoded copy in Composables
- [ ] 13.2 Review all visible text with clinical team: remove stigmatizing language, adjust tone
- [ ] 13.3 Review crisis resource content and phone numbers with clinical team
- [ ] 13.4 Verify reminder notification copy is approved
- [x] 13.5 Add content descriptions to all icon buttons for screen reader accessibility

## 14. Testing

- [ ] 14.1 Write Firestore security rule unit tests using Firebase Emulator for all rules defined in task 4
- [x] 14.2 Write unit tests for `AuthViewModel` (login success, login failure, register validation)
- [x] 14.3 Write unit tests for `RegistroViewModel` (form validation, create/edit/delete flows)
- [x] 14.4 Write integration tests for `RegistroRepository` against Firestore emulator
- [ ] 14.5 Write Compose UI tests for `CreateRegistroScreen` conditional field visibility (atracón/purga expansions)
- [ ] 14.6 Write Compose UI tests for visibility selector composable
- [ ] 14.7 Manual QA: test role-based field visibility for all three professional roles
