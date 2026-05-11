# OSS Versioning

## ADDED Requirements

### Requirement: File Version on Re-upload

When a file is uploaded to an existing `fileId` (same path), the system SHALL create a new version.

The latest version SHALL be the active one (served on download). Previous versions SHALL be retained for rollback.

S3 key format: `{baseDir}/{fileId}/v{version}/{originalName}`.

#### Scenario: Upload new version of existing file

- **WHEN** a user uploads a file to an existing fileId
- **THEN** a new version record SHALL be created with incremented version number
- **AND** the old version file SHALL remain in storage
- **AND** the latest version SHALL be served on download

---

### Requirement: List File Versions

The system SHALL provide `GET /api/v1/oss/files/{fileId}/versions` to list all versions.

Response SHALL include version number, file size, sha256, and creation timestamp for each version.

#### Scenario: Query versions of a file

- **WHEN** `GET /api/v1/oss/files/123/versions` is called for a file with 3 versions
- **THEN** response SHALL return all 3 versions ordered by version DESC
- **AND** the latest version SHALL be marked as `isLatest: true`

---

### Requirement: Rollback to Previous Version

The system SHALL provide `POST /api/v1/oss/files/{fileId}/rollback/{version}` to set a previous version as the active one.

This SHALL NOT delete the newer version — it SHALL only change the "current" pointer in `file_metadata`.

#### Scenario: Rollback to version 1

- **WHEN** `POST /api/v1/oss/files/123/rollback/1` is called
- **THEN** version 1 SHALL become the active version
- **AND** the download endpoint SHALL serve version 1's content
