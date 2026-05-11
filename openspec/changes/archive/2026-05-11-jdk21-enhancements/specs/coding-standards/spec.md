# Coding Standards — Delta

## ADDED Requirements

### Requirement: JDK 21 Feature Usage

Java modules SHALL leverage JDK 21 finalized features where applicable:

- **Virtual Threads**: Servlet modules (Java, Jetty) SHALL enable Virtual Threads via `spring.threads.virtual.enabled=true`. WebFlux modules (Kotlin, Netty) SHALL continue using their reactive thread model.

- **Record**: Immutable DTOs SHALL use Java `record` (JDK 14+). Records auto-generate canonical constructor, accessors, `equals`, `hashCode`, and `toString`.

- **Pattern Matching**: `instanceof` pattern matching SHALL be used to eliminate redundant casts:
  ```java
  if (obj instanceof String s) { /* use s directly */ }
  ```

- **Switch Expressions**: Multi-value switch SHALL use arrow syntax with `yield` for complex branches.

- **Sequenced Collections**: Methods `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `reversed()` from `SequencedCollection` / `SequencedMap` SHOULD be used where clearer than traditional `get(0)` / `get(size()-1)`.

Virtual Threads SHALL NOT be used for `synchronized`-heavy code paths, as they pin the carrier thread.

#### Scenario: Servlet module request handling

- **WHEN** `spring.threads.virtual.enabled=true` is set in Nacos shared config
- **THEN** Jetty SHALL use virtual threads for request processing
- **AND** each request SHALL consume ~1KB of memory (vs ~1MB for platform threads)
- **AND** the module SHALL handle 10,000+ concurrent connections without thread pool exhaustion

#### Scenario: Using pattern matching for type-safe cast

- **WHEN** a developer writes `if (obj instanceof String s && s.length() > 0)`
- **THEN** they SHALL use `instanceof` pattern matching
- **AND** SHALL NOT write `if (obj instanceof String) { String s = (String) obj; ... }`

#### Scenario: Virtual Threads not used with synchronized blocks

- **WHEN** a developer writes a method with `synchronized` keyword
- **THEN** they SHALL be aware that virtual threads will pin the carrier thread
- **AND** they SHALL use `ReentrantLock` instead of `synchronized` if the method runs on a virtual thread
