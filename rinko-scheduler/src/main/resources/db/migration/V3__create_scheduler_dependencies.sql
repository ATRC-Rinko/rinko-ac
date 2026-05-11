CREATE TABLE IF NOT EXISTS scheduler_dependencies (
    id BIGINT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    depends_on_job_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dep UNIQUE (job_id, depends_on_job_id)
);
