CREATE TABLE IF NOT EXISTS scheduler_dependencies
(
    id
    BIGINT
    PRIMARY
    KEY,
    job_id
    BIGINT
    NOT
    NULL,
    depends_on_job_id
    BIGINT
    NOT
    NULL,
    created_at
    TIMESTAMP
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    CONSTRAINT
    uq_dep
    UNIQUE
(
    job_id,
    depends_on_job_id
)
    );

COMMENT
ON TABLE scheduler_dependencies IS '定时任务依赖关系表';
COMMENT
ON COLUMN scheduler_dependencies.id IS '主键ID';
COMMENT
ON COLUMN scheduler_dependencies.job_id IS '当前任务ID';
COMMENT
ON COLUMN scheduler_dependencies.depends_on_job_id IS '依赖的前置任务ID';
COMMENT
ON COLUMN scheduler_dependencies.created_at IS '创建时间';
