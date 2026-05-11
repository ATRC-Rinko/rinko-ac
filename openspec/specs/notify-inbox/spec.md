# Notify Inbox

## ADDED Requirements

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
