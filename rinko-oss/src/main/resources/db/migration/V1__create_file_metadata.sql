CREATE TABLE IF NOT EXISTS file_metadata
(
    id
    BIGINT
    PRIMARY
    KEY,
    original_name
    VARCHAR
(
    512
) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR
(
    128
),
    sha256 VARCHAR
(
    64
),
    storage_path VARCHAR
(
    1024
) NOT NULL,
    storage_type VARCHAR
(
    16
) NOT NULL,
    parent_id BIGINT,
    is_directory BOOLEAN NOT NULL DEFAULT FALSE,
    current_version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent FOREIGN KEY
(
    parent_id
) REFERENCES file_metadata
(
    id
) ON DELETE SET NULL
    );

CREATE INDEX idx_file_parent ON file_metadata (parent_id);

COMMENT
ON TABLE file_metadata IS '文件元数据表';
COMMENT
ON COLUMN file_metadata.id IS '主键ID';
COMMENT
ON COLUMN file_metadata.original_name IS '原始文件名';
COMMENT
ON COLUMN file_metadata.file_size IS '文件大小（字节）';
COMMENT
ON COLUMN file_metadata.content_type IS '文件内容类型（MIME）';
COMMENT
ON COLUMN file_metadata.sha256 IS '文件SHA256哈希值';
COMMENT
ON COLUMN file_metadata.storage_path IS '存储路径';
COMMENT
ON COLUMN file_metadata.storage_type IS '存储类型：LOCAL | S3 | OSS';
COMMENT
ON COLUMN file_metadata.parent_id IS '父目录ID';
COMMENT
ON COLUMN file_metadata.is_directory IS '是否为目录';
COMMENT
ON COLUMN file_metadata.current_version IS '当前版本号';
COMMENT
ON COLUMN file_metadata.created_at IS '创建时间';
COMMENT
ON COLUMN file_metadata.updated_at IS '更新时间';
