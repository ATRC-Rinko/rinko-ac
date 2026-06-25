CREATE TABLE IF NOT EXISTS file_versions
(
    id
    BIGINT
    PRIMARY
    KEY,
    file_id
    BIGINT
    NOT
    NULL
    REFERENCES
    file_metadata
(
    id
) ON DELETE CASCADE,
    version INT NOT NULL,
    file_size BIGINT NOT NULL,
    sha256 VARCHAR
(
    64
),
    storage_path VARCHAR
(
    1024
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE UNIQUE INDEX idx_version_file ON file_versions (file_id, version);

COMMENT
ON TABLE file_versions IS '文件版本历史表';
COMMENT
ON COLUMN file_versions.id IS '主键ID';
COMMENT
ON COLUMN file_versions.file_id IS '文件ID（关联 file_metadata）';
COMMENT
ON COLUMN file_versions.version IS '版本号';
COMMENT
ON COLUMN file_versions.file_size IS '文件大小（字节）';
COMMENT
ON COLUMN file_versions.sha256 IS '文件SHA256哈希值';
COMMENT
ON COLUMN file_versions.storage_path IS '存储路径';
COMMENT
ON COLUMN file_versions.created_at IS '创建时间';
