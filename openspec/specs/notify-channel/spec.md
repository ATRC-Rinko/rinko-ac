# Notify Channel

## ADDED Requirements

### Requirement: Multi-Channel Notification Sending

The system SHALL provide `POST /api/v1/notify/send` to send notifications via specified channels.

Request body:
```json
{
  "channel": "EMAIL",
  "templateCode": "welcome",
  "recipients": ["user@example.com"],
  "variables": {"username": "Alice"}
}
```

Supported channels: `IN_APP`, `EMAIL`, `SMS`.

The request SHALL be published to RabbitMQ queue `notify.queue` for asynchronous processing.

Response SHALL return HTTP 202 Accepted with a `notificationId`.

#### Scenario: Send email notification

- **WHEN** `POST /api/v1/notify/send` with `channel=EMAIL, recipient=user@example.com`
- **THEN** the request SHALL be queued to RabbitMQ
- **AND** HTTP 202 SHALL be returned with a notificationId
- **AND** the email SHALL be delivered asynchronously via SMTP

#### Scenario: Invalid channel

- **WHEN** `POST /api/v1/notify/send` with `channel=UNKNOWN`
- **THEN** HTTP 400 SHALL be returned

---

### Requirement: Notification History

All sent notifications SHALL be recorded in `notification_history` table with columns:
- `id BIGINT PRIMARY KEY`
- `channel VARCHAR(16)` — IN_APP / EMAIL / SMS
- `template_code VARCHAR(64)`
- `recipient VARCHAR(256)`
- `subject VARCHAR(512)`
- `content TEXT`
- `status VARCHAR(16)` — PENDING / SENT / FAILED
- `is_read BOOLEAN DEFAULT FALSE`
- `error_message VARCHAR(512)`
- `created_at TIMESTAMP`

#### Scenario: Notification recorded after sending

- **WHEN** an email is successfully sent
- **THEN** a record SHALL exist in `notification_history` with status SENT
- **AND** the record SHALL include channel, recipient, subject, and content
