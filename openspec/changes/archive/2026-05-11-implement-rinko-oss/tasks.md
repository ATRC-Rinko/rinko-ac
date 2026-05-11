## 1. Maven Dependencies

- [x] 1.1 Add `spring-boot-starter-data-jdbc` to `rinko-oss/pom.xml`
- [x] 1.2 Add `software.amazon.awssdk:s3` (2.29.x) to `rinko-oss/pom.xml`
- [x] 1.3 Add `springdoc-openapi-starter-webmvc-ui` to `rinko-oss/pom.xml`

## 2. Configuration & Infrastructure

- [x] 2.1 Create `OssProperties.java` — storage type, S3 config, local base-path, thumbnail dimensions, video resolutions
- [x] 2.2 Create Flyway `V1__create_file_metadata.sql` — with `parentId` for directory support
- [x] 2.3 Create Flyway `V2__create_file_versions.sql`
- [x] 2.4 Create Flyway `V3__create_video_resolutions.sql`
- [x] 2.5 Update `nacos-config/rinko-oss-dev.yml` — S3 rustfs config, media processing config

## 3. Entity & Repository

- [x] 3.1 Create `FileMetadata.java` — id, originalName, fileSize, contentType, sha256, storagePath, storageType, parentId, isDirectory, currentVersion
- [x] 3.2 Create `FileVersion.java` — fileId, version, fileSize, sha256, storagePath
- [x] 3.3 Create `VideoResolutionEntity.java` — fileId, version, resolution, status, fileSize, storagePath
- [x] 3.4 Create repositories: `FileMetadataRepository`, `FileVersionRepository`, `VideoResolutionRepository`

## 4. Storage Service Layer

- [x] 4.1 Create `StorageService.java` interface — `store(inputStream, key, size, contentType)`, `getInputStream(key)`, `delete(key)`, `presignUrl(key, expiresSeconds)`
- [x] 4.2 Create `S3StorageService.java` — `RequestBody.fromInputStream()` streaming upload, `S3Presigner` presign URL
- [x] 4.3 Create `LocalStorageService.java` — local filesystem implementation
- [x] 4.4 Create `StorageServiceConfig.java` — `@ConditionalOnProperty` to select S3 vs local

## 5. Media Processing

- [x] 5.1 Create `ImageProcessor.java` — `BufferedImage` thumbnail generation
- [x] 5.2 Create `VideoProcessor.java` — FFmpeg `ProcessBuilder` multi-resolution transcoding, async via `@Async` + `ExecutorService`
- [x] 5.3 Create `MediaProcessingService.java` — detect file type, dispatch to ImageProcessor or VideoProcessor

## 6. Business Service Layer

- [x] 6.1 Create `FileService.java` — upload (streaming + SHA-256), download stream, delete, version management, directory CRUD

## 7. Controller Layer

- [x] 7.1 Create `FileController.java` — `POST /upload`, `GET /download/{id}`, `GET /presign/{id}`, `GET /files`, `DELETE /files/{id}`, `POST /directories`
- [x] 7.2 Create `VersionController.java` — `GET /files/{id}/versions`, `POST /files/{id}/rollback/{version}`
- [x] 7.3 Create `MediaController.java` — `GET /thumbnail/{id}`, `GET /video/{id}/resolutions`, `GET /video/{id}/stream/{resolution}`

## 8. Application Entry Point

- [x] 8.1 Create `RinkoOssApplication.java` — `@SpringBootApplication` + `@EnableDiscoveryClient` + `@EnableDruid` + `@EnableAsync` + `@EnableScheduling`
- [x] 8.2 Update `rinko-oss/src/main/resources/application.yml` — add module config import + SpringDoc

## 9. Verification

- [x] 9.1 Run `mvn clean compile` on `rinko-oss` — verify compilation succeeds
- [x] 9.2 Run `mvn clean test` on `rinko-oss` — verify tests pass

## 10. Spec Sync

- [x] 10.1 Sync 5 new specs to `openspec/specs/` — `oss-upload`, `oss-download`, `oss-metadata`, `oss-versioning`, `oss-media-processing`
