# OSS (Object Storage Service)

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

---

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

---

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

Table SHALL be created during module initialization.

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

---

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

---

### Requirement: Image Thumbnail Auto-Generation

When a file with `contentType` starting with `image/` is uploaded, the system SHALL automatically generate a thumbnail.

Thumbnail size SHALL default to 200x200 pixels, configurable via `rinko.oss.thumbnail.width` and `rinko.oss.thumbnail.height`.

The thumbnail SHALL be stored as a separate S3 object / local file with key: `{baseDir}/{fileId}/thumb.jpg`.

Thumbnail SHALL be generated using Java `BufferedImage` + `javax.imageio.ImageIO`.

#### Scenario: Upload a JPEG image

- **WHEN** a JPEG file is uploaded
- **THEN** a thumbnail SHALL be generated and stored
- **AND** `GET /api/v1/oss/thumbnail/{fileId}` SHALL return the thumbnail

#### Scenario: Non-image file has no thumbnail

- **WHEN** a non-image file is uploaded
- **THEN** no thumbnail SHALL be generated
- **AND** `GET /api/v1/oss/thumbnail/{fileId}` SHALL return HTTP 404

---

### Requirement: Video Multi-Resolution Transcoding

When a file with `contentType` starting with `video/` is uploaded, the system SHALL asynchronously transcode multiple resolution versions.

Configurable resolutions (defaults):

| Label | Width | Height | Bitrate |
|-------|-------|--------|---------|
| 480p  | 854   | 480    | 1M      |
| 720p  | 1280  | 720    | 2.5M    |
| 1080p | 1920  | 1080   | 5M      |

Resolutions where the target height exceeds the source video height SHALL be skipped.

Resolution versions SHALL be stored as: `{baseDir}/{fileId}/v{version}/{resolution}.mp4`

FFmpeg SHALL be invoked via `ProcessBuilder` with args: `ffmpeg -i <input> -vf scale=W:H -b:v R -c:v libx264 -c:a aac <output>.mp4`

Transcoding status SHALL be tracked in `video_resolutions` table with states: PENDING, PROCESSING, COMPLETED, FAILED.

#### Scenario: Upload 4K video triggers all 3 resolutions

- **WHEN** a 3840x2160 MP4 is uploaded
- **THEN** HTTP 201 SHALL return immediately
- **AND** 3 resolution jobs SHALL be queued (480p, 720p, 1080p)
- **AND** `GET /api/v1/oss/video/{fileId}/resolutions` SHALL show progress

#### Scenario: Upload 720p video skips 1080p

- **WHEN** a 1280x720 video is uploaded
- **THEN** only 480p SHALL be generated (720p source ≥ 720p resolution skipped)

#### Scenario: FFmpeg unavailable

- **WHEN** ffmpeg is not installed on the system
- **THEN** video upload SHALL still succeed (original stored)
- **AND** all resolution entries SHALL be FAILED with "ffmpeg not found"

---

### Requirement: Query Video Resolutions

The system SHALL provide `GET /api/v1/oss/video/{fileId}/resolutions` to query transcoding status.

Response SHALL be a JSON array of resolution objects: `{"resolution": "720p", "status": "COMPLETED", "fileSize": 5000000}`.

#### Scenario: Check resolution status during transcoding

- **WHEN** `GET /api/v1/oss/video/123/resolutions` is polled
- **THEN** completed resolutions SHALL show COMPLETED with file size
- **AND** in-progress resolutions SHALL show PROCESSING
- **AND** pending resolutions SHALL show PENDING

---

### Requirement: Stream Specific Video Resolution

The system SHALL provide `GET /api/v1/oss/video/{fileId}/stream/{resolution}` to stream a specific resolution.

#### Scenario: Stream 720p version

- **WHEN** `GET /api/v1/oss/video/123/stream/720p` is called
- **THEN** the 720p MP4 file SHALL be streamed
- **AND** `Content-Type` SHALL be `video/mp4`
