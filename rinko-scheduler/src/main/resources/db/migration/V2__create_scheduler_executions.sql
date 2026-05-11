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
