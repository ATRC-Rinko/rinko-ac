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
COMMENT ON COLUMN log_level_configs.id IS '主键ID';
COMMENT ON COLUMN log_level_configs.service_name IS '服务名称';
COMMENT ON COLUMN log_level_configs.logger_name IS '日志记录器名称';
COMMENT ON COLUMN log_level_configs.log_level IS '日志级别：TRACE | DEBUG | INFO | WARN | ERROR';
COMMENT ON COLUMN log_level_configs.created_at IS '创建时间';
COMMENT ON COLUMN log_level_configs.updated_at IS '更新时间';
