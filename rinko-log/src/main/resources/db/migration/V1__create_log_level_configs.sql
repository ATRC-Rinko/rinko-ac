-- =====================================================
-- V1: 动态日志级别配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS log_level_configs (
    id BIGINT PRIMARY KEY,
    service_name VARCHAR(64) NOT NULL,
    logger_name VARCHAR(256) NOT NULL,
    log_level VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_log_level UNIQUE (service_name, logger_name)
);

COMMENT ON TABLE log_level_configs IS '动态日志级别配置表';
COMMENT ON COLUMN log_level_configs.log_level IS 'TRACE | DEBUG | INFO | WARN | ERROR';
