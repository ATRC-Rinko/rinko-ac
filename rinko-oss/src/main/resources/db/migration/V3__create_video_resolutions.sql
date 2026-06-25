CREATE TABLE IF NOT EXISTS video_resolutions
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
    resolution VARCHAR
(
    16
) NOT NULL,
    status VARCHAR
(
    16
) NOT NULL DEFAULT 'PENDING',
    file_size BIGINT,
    storage_path VARCHAR
(
    1024
),
    error_message VARCHAR
(
    512
),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_res_file ON video_resolutions (file_id, version);

COMMENT
ON TABLE video_resolutions IS '视频转码分辨率记录表';
COMMENT
ON COLUMN video_resolutions.id IS '主键ID';
COMMENT
ON COLUMN video_resolutions.file_id IS '文件ID（关联 file_metadata）';
COMMENT
ON COLUMN video_resolutions.version IS '文件版本号';
COMMENT
ON COLUMN video_resolutions.resolution IS '视频分辨率（如 1920x1080）';
COMMENT
ON COLUMN video_resolutions.status IS '转码状态：PENDING | PROCESSING | COMPLETED | FAILED';
COMMENT
ON COLUMN video_resolutions.file_size IS '转码后文件大小（字节）';
COMMENT
ON COLUMN video_resolutions.storage_path IS '转码后存储路径';
COMMENT
ON COLUMN video_resolutions.error_message IS '错误信息';
COMMENT
ON COLUMN video_resolutions.created_at IS '创建时间';
COMMENT
ON COLUMN video_resolutions.updated_at IS '更新时间';
