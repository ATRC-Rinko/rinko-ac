# OSS Metadata

## ADDED Requirements

### Requirement: File Metadata Storage

File metadata SHALL be stored in PostgreSQL table `file_metadata` with columns:
- `id BIGINT PRIMARY KEY` — Snowflake-generated file ID
- `original_name VARCHAR(512) NOT NULL` — original uploaded file name
- `file_size BIGINT NOT NULL` — size in bytes
- `content_type VARCHAR(128)` — MIME type
- `sha256 VARCHAR(64)` — SHA-256 checksum
- `storage_path VARCHAR(1024) NOT NULL` — S3 key or local file path
- `storage_type VARCHAR(16) NOT NULL` — `s3` or `local`
- `created_at TIMESTAMP NOT NULL DEFAULT NOW()`

Table SHALL be created via Flyway migration `V1__create_file_metadata.sql`.

#### Scenario: File uploaded successfully

- **WHEN** a file is uploaded
- **THEN** metadata SHALL be saved to `file_metadata` table
- **AND** `fileId` SHALL be a unique Snowflake ID

---

### Requirement: List Files with Pagination

The system SHALL provide `GET /api/v1/oss/files` for listing files with pagination.

Query parameters: `page` (default 1), `size` (default 20, max 100).

Response SHALL use `PageResponse<FileMetadata>` from `rinko-infra`.

#### Scenario: List first page of files

- **WHEN** `GET /api/v1/oss/files?page=1&size=20` is called
- **THEN** response SHALL return first 20 files ordered by `created_at DESC`
- **AND** response SHALL include `totalElements` and `totalPages`

---

### Requirement: Delete File

The system SHALL provide `DELETE /api/v1/oss/files/{fileId}` to delete a file.

Deletion SHALL remove both the file content from storage AND the metadata record.

#### Scenario: Delete an existing file

- **WHEN** `DELETE /api/v1/oss/files/123` is called
- **THEN** the file SHALL be removed from the storage backend
- **AND** the metadata record SHALL be deleted from PostgreSQL
- **AND** HTTP 204 SHALL be returned

#### Scenario: Delete a non-existent file

- **WHEN** `DELETE /api/v1/oss/files/999` is called
- **THEN** HTTP 404 SHALL be returned
