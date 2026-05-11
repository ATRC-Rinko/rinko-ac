CREATE TABLE IF NOT EXISTS video_resolutions (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    version INT NOT NULL,
    resolution VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    file_size BIGINT,
    storage_path VARCHAR(1024),
    error_message VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_res_file ON video_resolutions(file_id, version);
