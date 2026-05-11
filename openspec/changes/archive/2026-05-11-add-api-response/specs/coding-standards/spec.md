# Coding Standards — Delta

## ADDED Requirements

### Requirement: Unified API Response Format

All REST API endpoints SHALL return responses wrapped in `ApiResponse<T>` from `rinko-infra`.

Successful responses SHALL use `ApiResponse.success(data)` with HTTP 2xx status.

Exception responses SHALL be automatically handled by the global exception handler — controllers SHALL NOT catch `RinkoException` manually.

`ApiResponse` structure:
```json
{
  "code": 200,
  "message": "OK",
  "data": <T>,
  "timestamp": "2026-05-11T10:00:00+08:00"
}
```

The response format SHALL be consistent across both Servlet (Java) and WebFlux (Kotlin) modules.

#### Scenario: Successful response from a Servlet controller

- **WHEN** a Servlet controller returns `ApiResponse.success(someData)`
- **THEN** the HTTP response body SHALL be `{"code":200,"message":"OK","data":{...},"timestamp":"..."}`
- **AND** HTTP status SHALL be 200

#### Scenario: ValidationException thrown in a service layer

- **WHEN** a `ValidationException` is thrown from any service method
- **THEN** the global exception handler SHALL catch it
- **AND** the response SHALL be `{"code":400,"message":"<error detail>","data":null,"timestamp":"..."}`
- **AND** HTTP status SHALL be 400

#### Scenario: WebFlux controller returns reactive type

- **WHEN** a WebFlux Kotlin controller returns `Mono.just(ApiResponse.success(data))`
- **THEN** the response SHALL be identical in structure to a Servlet response
- **AND** the client SHALL NOT detect any difference between Servlet and WebFlux responses
