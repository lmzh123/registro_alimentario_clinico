## Why

Patients in eating disorder treatment need a structured, private way to document meals with behavioral and emotional context and share that information selectively with their clinical team (nutritionist, psychologist, psychiatrist). No existing app is designed specifically for this clinical workflow with the required role-based visibility, non-stigmatizing language, and eating-disorder-sensitive design constraints.

## What Changes

- Introduce a new Android application built with Kotlin + Jetpack Compose and Firebase backend
- Implement role-based access with four distinct user types: Paciente, Nutricionista, Psicología, Psiquiatría
- Create the core `Registro` data model capturing full meal context: description, photos, location, companions, behavioral flags, emotions before/after, thoughts, and external comments
- Enable per-record and per-note visibility controls so patients choose exactly what each professional sees
- Provide read-only professional dashboards filtered by role (nutritionist sees food notes only; psychology sees emotional/behavioral notes; psychiatry sees everything)
- Add clinical commenting system so professionals can annotate records without editing them
- Include persistent crisis resources access throughout the patient UI
- Support soft, optional meal reminders via Firebase FCM

## Capabilities

### New Capabilities
- `user-auth`: Firebase Auth with role assignment (paciente, nutricionista, psicología, psiquiatría); login, registration, and session management
- `food-registry`: Full `Registro` creation and management — meal type, description, photos, location, companions, behavioral flags (atracón, purga, chequeo), emotions before/after with free text, thoughts, external comments, additional notes
- `photo-capture`: In-app camera capture and gallery selection with upload to Firebase Storage; photos stored as URL references in Registro
- `visibility-controls`: Per-registro and per-note visibility array (`visibilidad`) controlling which roles can access each record; patient configures sharing at creation and can update later
- `professional-dashboard`: Role-filtered read-only view of shared records; each professional role sees only the fields scoped to their access level; filterable by behavioral flags and date range
- `clinical-comments`: Professionals add timestamped comments to specific registros; patients receive in-app notifications when new comments appear; professionals cannot edit patient records
- `crisis-resources`: Persistent quick-access panel to crisis support lines and resources; visible throughout all patient-facing screens; content configurable per country/region
- `notifications`: Soft optional meal reminder notifications via Firebase FCM; fully configurable schedule; patient can disable at any time

### Modified Capabilities
<!-- No existing specs — this is a greenfield application -->

## Impact

- New Android project: Kotlin + Jetpack Compose, targeting API 26+ (Android 8.0)
- Firebase project required: Firestore (database), Firebase Auth, Firebase Storage (photos), Firebase FCM (notifications)
- No existing codebase — all code is new
- Clinical review required before public release (language, UX, crisis resource content)
