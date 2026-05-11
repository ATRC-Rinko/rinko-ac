# OSS Download

## ADDED Requirements

### Requirement: File Download via Proxy

The system SHALL provide `GET /api/v1/oss/download/{fileId}` to download a file.

The response SHALL stream the file content with appropriate `Content-Type` and `Content-Disposition` headers from the metadata.

If the file does not exist, HTTP 404 SHALL be returned.

#### Scenario: Download an existing file

- **WHEN** `GET /api/v1/oss/download/123` is called for an existing file
- **THEN** the file content SHALL be streamed from the storage backend
- **AND** response headers SHALL include `Content-Type` and `Content-Disposition: attachment; filename="original-name.pdf"`

#### Scenario: Download a non-existent file

- **WHEN** `GET /api/v1/oss/download/999` is called for a non-existent file
- **THEN** HTTP 404 SHALL be returned with RFC 7807 body

---

### Requirement: Pre-signed URL Generation

The system SHALL provide `GET /api/v1/oss/presign/{fileId}` to generate a time-limited download URL.

Query parameter `expires` (seconds, default 600) SHALL control the URL validity duration.

The response SHALL return the pre-signed URL as a JSON string.

Pre-signed URL generation SHALL be supported for S3 backend only. Local storage SHALL return a direct download URL.

#### Scenario: Generate a pre-signed URL for S3

- **WHEN** `GET /api/v1/oss/presign/123?expires=300` is called
- **THEN** a pre-signed S3 URL valid for 5 minutes SHALL be returned
- **AND** the URL SHALL allow direct file access without authentication

#### Scenario: Pre-signed URL for local storage

- **WHEN** `GET /api/v1/oss/presign/123` is called with local storage active
- **THEN** a direct download URL (`/api/v1/oss/download/123`) SHALL be returned
