CREATE TABLE IF NOT EXISTS scheduler_jobs
(
    id
    BIGINT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    128
) NOT NULL UNIQUE,
    type VARCHAR
(
    16
) NOT NULL,
    cron_expression VARCHAR
(
    64
),
    config TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    max_retries INT NOT NULL DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

COMMENT
ON TABLE scheduler_jobs IS '定时任务配置表';
COMMENT
ON COLUMN scheduler_jobs.id IS '主键ID';
COMMENT
ON COLUMN scheduler_jobs.name IS '任务名称（唯一）';
COMMENT
ON COLUMN scheduler_jobs.type IS '任务类型';
COMMENT
ON COLUMN scheduler_jobs.cron_expression IS 'CRON表达式';
COMMENT
ON COLUMN scheduler_jobs.config IS '任务配置（JSON格式）';
COMMENT
ON COLUMN scheduler_jobs.enabled IS '是否启用';
COMMENT
ON COLUMN scheduler_jobs.max_retries IS '最大重试次数';
COMMENT
ON COLUMN scheduler_jobs.created_at IS '创建时间';
COMMENT
ON COLUMN scheduler_jobs.updated_at IS '更新时间';
