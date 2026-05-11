# Notify Template

## ADDED Requirements

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

Table: `notification_templates`, created via Flyway `V2__create_notification_templates.sql`.

#### Scenario: Create a new template

- **WHEN** `POST /api/v1/notify/templates` with `{"code":"welcome","name":"Welcome","subject":"Welcome {username}","body":"Hello {username}, welcome to Rinko!","channels":"EMAIL,IN_APP"}`
- **THEN** the template SHALL be saved with HTTP 201

#### Scenario: Send notification with template variables

- **WHEN** `POST /api/v1/notify/send` with `templateCode=welcome, variables={username: Alice}`
- **THEN** the template variables SHALL be replaced
- **AND** the sent message subject SHALL be "Welcome Alice"
- **AND** the body SHALL be "Hello Alice, welcome to Rinko!"
