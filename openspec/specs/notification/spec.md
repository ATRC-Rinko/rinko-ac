# Notification

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

---

### Requirement: In-App Message Query

The system SHALL provide `GET /api/v1/notify/inbox` to query in-app messages for a user.

Query parameters: `userId` (required), `isRead` (optional filter), `page`, `size`.

Response SHALL use `ApiResponse<PageResponse<NotificationHistory>>`.

#### Scenario: Query unread messages

- **WHEN** `GET /api/v1/notify/inbox?userId=100&isRead=false`
- **THEN** only unread IN_APP messages for user 100 SHALL be returned
- **AND** results SHALL be ordered by `created_at DESC`

---

### Requirement: Mark Message as Read

The system SHALL provide `PUT /api/v1/notify/inbox/{notificationId}/read` to mark a message as read.

#### Scenario: Mark a message as read

- **WHEN** `PUT /api/v1/notify/inbox/123/read` is called
- **THEN** the message SHALL have `is_read = true` and `read_at = NOW()`
- **AND** HTTP 200 SHALL be returned

---

### Requirement: Unread Message Count

The system SHALL provide `GET /api/v1/notify/inbox/unread-count?userId={userId}` to get unread count.

#### Scenario: Get unread count

- **WHEN** `GET /api/v1/notify/inbox/unread-count?userId=100`
- **THEN** the response SHALL be `{"count": 5}`

---

### Requirement: Template CRUD

The system SHALL provide CRUD for notification templates:

- `GET /api/v1/notify/templates` — list all templates
- `POST /api/v1/notify/templates` — create template
- `PUT /api/v1/notify/templates/{id}` — update template
- `DELETE /api/v1/notify/templates/{id}` — delete template

Template fields:
- `code VARCHAR(64) UNIQUE` — unique identifier (e.g., `welcome`, `password-reset`)
- `name VARCHAR(128)` — human-readable name
- `subject VARCHAR(512)` — template subject with `{var}` placeholders
- `body TEXT` — template body with `{var}` placeholders
- `channels VARCHAR(256)` — comma-separated supported channels

Table: `notification_templates`.

#### Scenario: Create a new template

- **WHEN** `POST /api/v1/notify/templates` with `{"code":"welcome","name":"Welcome","subject":"Welcome {username}","body":"Hello {username}, welcome to Rinko!","channels":"EMAIL,IN_APP"}`
- **THEN** the template SHALL be saved with HTTP 201

#### Scenario: Send notification with template variables

- **WHEN** `POST /api/v1/notify/send` with `templateCode=welcome, variables={username: Alice}`
- **THEN** the template variables SHALL be replaced
- **AND** the sent message subject SHALL be "Welcome Alice"
- **AND** the body SHALL be "Hello Alice, welcome to Rinko!"

---

### Requirement: WebSocket Real-Time Push

The system SHALL support WebSocket connections at `ws://host/ws/notify?userId={userId}` for real-time notification delivery.

When a new in-app message is created for a user, the message SHALL be pushed to the user's connected WebSocket immediately.

WebSocket connections SHALL be authenticated via query parameter `userId`.

#### Scenario: WebSocket receives new message

- **WHEN** a new IN_APP notification is sent to userId=100
- **THEN** the message SHALL be pushed to all WebSocket connections with `userId=100` within 1 second
- **AND** the message JSON SHALL include `notificationId`, `subject`, `content`, `createdAt`

#### Scenario: WebSocket disconnect and reconnect

- **WHEN** a WebSocket connection drops
- **THEN** the connection SHALL be removed from the session pool
- **AND** reconnection SHALL create a new session

---

### Requirement: SSE (Server-Sent Events) Stream

The system SHALL provide `GET /api/v1/notify/stream?userId={userId}` for SSE-based notification delivery.

The SSE connection SHALL use `SseEmitter` with a 5-minute timeout (renewable on each event).

#### Scenario: SSE receives new message

- **WHEN** a new IN_APP notification is sent to userId=100
- **THEN** the SSE stream SHALL deliver the message JSON within 1 second
- **AND** the event name SHALL be `notification`

#### Scenario: SSE timeout handling

- **WHEN** an SSE connection times out after 5 minutes
- **THEN** the client SHALL reconnect by calling the stream endpoint again

---

### Requirement: Email Provider Switching

The system SHALL support multiple email providers, selectable via `rinko.notify.email.provider`.

Supported providers:
- `smtp` — standard SMTP via `JavaMailSender` (default)
- `sendgrid` — SendGrid HTTP API

Provider SHALL be selected at startup via `@ConditionalOnProperty`.

#### Scenario: Switch from SMTP to SendGrid

- **WHEN** `rinko.notify.email.provider=sendgrid` is configured
- **THEN** `SendGridProvider` SHALL be loaded instead of `SmtpProvider`
- **AND** all email notifications SHALL be sent via SendGrid API

---

### Requirement: SMS Provider Switching

The system SHALL support multiple SMS providers, selectable via `rinko.notify.sms.provider`.

Supported providers:
- `aliyun` — Aliyun SMS SDK (default)
- `tencent` — Tencent Cloud SMS SDK

Provider SHALL be selected at startup via `@ConditionalOnProperty`.

#### Scenario: Switch from Aliyun to Tencent SMS

- **WHEN** `rinko.notify.sms.provider=tencent` is configured
- **THEN** `TencentSmsProvider` SHALL be loaded
- **AND** all SMS notifications SHALL be sent via Tencent Cloud API

---

### Requirement: Batch Send Optimization

The system SHALL provide `POST /api/v1/notify/send-batch` for sending to multiple recipients in one request.

Request body:
```json
{
  "channel": "EMAIL",
  "templateCode": "welcome",
  "recipients": ["a@x.com", "b@x.com", "c@x.com"],
  "variables": {"company": "Rinko"}
}
```

Recipients SHALL be deduplicated. History records SHALL be batch-inserted.

Response SHALL return `{"count": 3, "notificationIds": [1, 2, 3]}`.

#### Scenario: Batch send with duplicates

- **WHEN** `POST /send-batch` with `recipients=["a@x.com","a@x.com","b@x.com"]`
- **THEN** only 2 notification records SHALL be created (deduplicated)
- **AND** response SHALL return `{"count": 2, ...}`
