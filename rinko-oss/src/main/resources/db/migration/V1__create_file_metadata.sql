CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGINT PRIMARY KEY,
    original_name VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(128),
    sha256 VARCHAR(64),
    storage_path VARCHAR(1024) NOT NULL,
    storage_type VARCHAR(16) NOT NULL,
    parent_id BIGINT,
    is_directory BOOLEAN NOT NULL DEFAULT FALSE,
    current_version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES file_metadata(id) ON DELETE SET NULL
);

CREATE INDEX idx_file_parent ON file_metadata(parent_id);
