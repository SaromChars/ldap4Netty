## ADDED Requirements

### Requirement: Typed proxy configuration
The system SHALL provide a typed proxy configuration for listen port, upstream host, upstream port, connect timeout, and interceptor registration.

#### Scenario: Valid proxy configuration
- **WHEN** a caller creates a proxy with valid listen and upstream settings
- **THEN** the proxy can be started using those settings

#### Scenario: Invalid proxy configuration
- **WHEN** a caller provides a missing upstream host or invalid port
- **THEN** the system MUST reject the configuration before opening sockets

### Requirement: Deterministic proxy lifecycle
The proxy runtime SHALL support non-blocking startup, readiness waiting, and idempotent shutdown of owned resources.

#### Scenario: Await proxy readiness
- **WHEN** a caller starts the proxy with the non-blocking lifecycle API
- **THEN** the caller can wait until the listen socket is bound before connecting LDAP clients

#### Scenario: Stop proxy
- **WHEN** a caller stops a running proxy
- **THEN** the proxy MUST close the listen channel, active upstream channels, and owned event loop groups

### Requirement: Bind and search forwarding
The proxy SHALL forward decoded LDAP bind and search requests to the configured upstream and relay upstream responses to the original client.

#### Scenario: Forward bind
- **WHEN** a client sends a bind request through the proxy
- **THEN** the client receives the upstream bind response with the expected message ID

#### Scenario: Forward search response stream
- **WHEN** an upstream search returns entries followed by search done
- **THEN** the proxy relays each search entry and the final search done response to the client

### Requirement: Ordered interceptors
The proxy SHALL run request interceptors in ascending order and response interceptors in descending order.

#### Scenario: Request order
- **WHEN** multiple interceptors are registered with order values
- **THEN** request messages pass through them from lowest order to highest order

#### Scenario: Response order
- **WHEN** multiple interceptors are registered with order values
- **THEN** response messages pass through them from highest order to lowest order

#### Scenario: Drop message
- **WHEN** an interceptor returns null
- **THEN** the proxy MUST stop interceptor processing and MUST NOT forward that message

### Requirement: Credential-safe access logging
The proxy SHALL log connection and LDAP operation summaries without logging simple bind credentials.

#### Scenario: Bind logging
- **WHEN** a simple bind request passes through access logging
- **THEN** the log output includes operation metadata but MUST NOT include the password bytes or password string
