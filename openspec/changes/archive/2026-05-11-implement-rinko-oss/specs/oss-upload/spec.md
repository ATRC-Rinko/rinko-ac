# OSS Upload

## ADDED Requirements

### Requirement: Simple File Upload

The system SHALL provide `POST /api/v1/oss/upload` for simple file uploads via `multipart/form-data`.

The request SHALL accept a single file in the `file` part.

Response SHALL include:
- `fileId` — unique identifier (Snowflake ID)
- `fileName` — original file name
- `fileSize` — file size in bytes
- `contentType` — MIME type
- `sha256` — SHA-256 checksum of file content

#### Scenario: Upload a small file

- **WHEN** `POST /api/v1/oss/upload` with a 500KB image file
- **THEN** the file SHALL be stored in the configured backend (S3 or local)
- **AND** file metadata SHALL be persisted to PostgreSQL
- **AND** HTTP 201 SHALL be returned with file metadata JSON

#### Scenario: Upload without a file

- **WHEN** `POST /api/v1/oss/upload` without a file part
- **THEN** HTTP 400 SHALL be returned
- **AND** error SHALL be RFC 7807 format

---

### Requirement: Multipart Upload for Large Files

The system SHALL support multipart uploads for files larger than 5MB.

`POST /api/v1/oss/upload/multipart/init` SHALL initiate a multipart upload and return an `uploadId`.

`POST /api/v1/oss/upload/multipart/part` SHALL upload a part (chunk), requiring `uploadId`, `partNumber`, and file chunk.

`POST /api/v1/oss/upload/multipart/complete` SHALL complete the upload by providing the `uploadId` and list of `partNumbers` with their ETags.

#### Scenario: Upload a 50MB file in chunks

- **WHEN** a client initiates → uploads 10 parts → completes a multipart upload
- **THEN** the file SHALL be assembled and stored in the backend
- **AND** metadata SHALL be persisted only after completion
