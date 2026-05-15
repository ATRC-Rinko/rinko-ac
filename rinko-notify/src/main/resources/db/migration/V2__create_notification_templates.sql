CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    subject VARCHAR(512),
    body TEXT,
    channels VARCHAR(256) NOT NULL DEFAULT 'IN_APP',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE notification_templates IS '通知模板配置表';
COMMENT ON COLUMN notification_templates.id IS '主键ID';
COMMENT ON COLUMN notification_templates.code IS '模板编码（唯一）';
COMMENT ON COLUMN notification_templates.name IS '模板名称';
COMMENT ON COLUMN notification_templates.subject IS '模板主题';
COMMENT ON COLUMN notification_templates.body IS '模板正文';
COMMENT ON COLUMN notification_templates.channels IS '适用通知渠道（多个用逗号分隔）';
COMMENT ON COLUMN notification_templates.created_at IS '创建时间';
COMMENT ON COLUMN notification_templates.updated_at IS '更新时间';
