# Notify Provider

## ADDED Requirements

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
