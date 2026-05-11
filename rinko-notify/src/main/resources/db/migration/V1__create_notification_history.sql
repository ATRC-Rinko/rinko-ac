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
