# Notify Push

## ADDED Requirements

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
