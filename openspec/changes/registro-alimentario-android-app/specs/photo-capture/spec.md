## ADDED Requirements

### Requirement: Attach photos to registro
The system SHALL allow a patient to attach up to 5 photos per Registro. Photos may be captured with the device camera or selected from the device gallery.

#### Scenario: Camera capture
- **WHEN** a patient taps "Tomar foto" within the registro form
- **THEN** the system SHALL request camera permission (if not already granted) and open the device camera; the captured image SHALL be added to the photo attachment list

#### Scenario: Gallery selection
- **WHEN** a patient taps "Elegir de galería" within the registro form
- **THEN** the system SHALL open the system photo picker and allow selection of up to (5 minus current count) photos

#### Scenario: Maximum photo limit enforced
- **WHEN** a patient already has 5 photos attached
- **THEN** the "Tomar foto" and "Elegir de galería" options SHALL be disabled with a visible explanation

---

### Requirement: Photo upload to Firebase Storage
The system SHALL upload all attached photos to Firebase Storage before saving the Registro. The Registro document SHALL store download URLs, not local file paths.

#### Scenario: Photos uploaded before save
- **WHEN** a patient taps "Guardar" with photos attached
- **THEN** all photos SHALL be uploaded to Storage first; the registro is saved only after all uploads succeed

#### Scenario: Upload failure handling
- **WHEN** a photo upload fails due to a network error
- **THEN** the system SHALL display an error message and NOT save the registro, allowing the patient to retry

#### Scenario: File size enforcement
- **WHEN** a patient attempts to attach a photo larger than 5 MB
- **THEN** the system SHALL display an error message and reject the photo before upload

---

### Requirement: Remove attached photo
The system SHALL allow a patient to remove an attached photo before saving, or remove a photo from an existing registro during editing.

#### Scenario: Photo removed from form
- **WHEN** a patient taps the remove control on an attached photo thumbnail
- **THEN** the photo is removed from the attachment list

#### Scenario: Photo deleted from Storage on registro deletion
- **WHEN** a registro is deleted (see food-registry spec)
- **THEN** all Storage files referenced by `fotos` URLs SHALL also be deleted

---

### Requirement: Photo display
The system SHALL display photo thumbnails within the registro form (during creation/editing) and in the registro detail view. Photos SHALL be loaded asynchronously with a placeholder while loading.

#### Scenario: Thumbnails shown in detail view
- **WHEN** a patient or authorized professional views a registro with photos
- **THEN** photo thumbnails SHALL be displayed in a scrollable row; tapping a thumbnail SHALL open a full-screen viewer

#### Scenario: Full-screen photo viewer
- **WHEN** a user taps on a photo thumbnail
- **THEN** the system SHALL present the full-resolution image in a full-screen overlay with a close/back control
