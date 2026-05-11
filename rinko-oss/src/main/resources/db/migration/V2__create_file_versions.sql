CREATE TABLE IF NOT EXISTS file_versions (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    version INT NOT NULL,
    file_size BIGINT NOT NULL,
    sha256 VARCHAR(64),
    storage_path VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_version_file ON file_versions(file_id, version);
