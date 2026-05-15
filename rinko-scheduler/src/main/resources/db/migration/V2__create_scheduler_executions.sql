CREATE TABLE IF NOT EXISTS scheduler_executions (
    id BIGINT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'RUNNING',
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    retry_count INT NOT NULL DEFAULT 0,
    result TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exec_job ON scheduler_executions(job_id, start_time DESC);

COMMENT ON TABLE scheduler_executions IS '定时任务执行记录表';
COMMENT ON COLUMN scheduler_executions.id IS '主键ID';
COMMENT ON COLUMN scheduler_executions.job_id IS '任务ID（关联 scheduler_jobs）';
COMMENT ON COLUMN scheduler_executions.status IS '执行状态：RUNNING | SUCCESS | FAILED';
COMMENT ON COLUMN scheduler_executions.start_time IS '开始时间';
COMMENT ON COLUMN scheduler_executions.end_time IS '结束时间';
COMMENT ON COLUMN scheduler_executions.retry_count IS '重试次数';
COMMENT ON COLUMN scheduler_executions.result IS '执行结果';
COMMENT ON COLUMN scheduler_executions.created_at IS '创建时间';
