CREATE TABLE IF NOT EXISTS notification_history (
    id BIGINT PRIMARY KEY,
    channel VARCHAR(16) NOT NULL,
    template_code VARCHAR(64),
    recipient VARCHAR(256) NOT NULL,
    subject VARCHAR(512),
    content TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    error_message VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hist_user ON notification_history(recipient, channel, created_at DESC);

COMMENT ON TABLE notification_history IS '通知发送历史记录表';
COMMENT ON COLUMN notification_history.id IS '主键ID';
COMMENT ON COLUMN notification_history.channel IS '通知渠道：IN_APP | EMAIL | SMS | DINGTALK | WECOM';
COMMENT ON COLUMN notification_history.template_code IS '模板编码';
COMMENT ON COLUMN notification_history.recipient IS '收件人';
COMMENT ON COLUMN notification_history.subject IS '通知主题';
COMMENT ON COLUMN notification_history.content IS '通知内容';
COMMENT ON COLUMN notification_history.status IS '发送状态：PENDING | SUCCESS | FAILED';
COMMENT ON COLUMN notification_history.is_read IS '是否已读';
COMMENT ON COLUMN notification_history.read_at IS '读取时间';
COMMENT ON COLUMN notification_history.error_message IS '错误信息';
COMMENT ON COLUMN notification_history.created_at IS '创建时间';
