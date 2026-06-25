CREATE TABLE IF NOT EXISTS notification_accounts
(
    id
    BIGINT
    PRIMARY
    KEY,
    provider
    VARCHAR
(
    32
) NOT NULL,
    name VARCHAR
(
    64
) NOT NULL,
    config JSONB NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_notification_accounts_provider ON notification_accounts (provider);

COMMENT
ON TABLE notification_accounts IS '通知账户配置表';
COMMENT
ON COLUMN notification_accounts.id IS '主键ID';
COMMENT
ON COLUMN notification_accounts.provider IS '提供商类型：SMTP / SENDGRID / ALIYUN_SMS / TENCENT_SMS';
COMMENT
ON COLUMN notification_accounts.name IS '账户显示名称';
COMMENT
ON COLUMN notification_accounts.config IS 'JSON 配置内容';
COMMENT
ON COLUMN notification_accounts.enabled IS '是否启用';
COMMENT
ON COLUMN notification_accounts.created_at IS '创建时间';
COMMENT
ON COLUMN notification_accounts.updated_at IS '更新时间';
