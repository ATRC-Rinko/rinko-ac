# OSS Media Processing

## ADDED Requirements

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
