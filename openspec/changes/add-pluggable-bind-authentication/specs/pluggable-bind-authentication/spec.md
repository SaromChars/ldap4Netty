## ADDED Requirements

### Requirement: Authenticator interface
The bind handler SHALL delegate credential decisions to a pluggable authenticator interface.

#### Scenario: Authenticator accepts bind
- **WHEN** the authenticator accepts a bind request
- **THEN** the bind handler creates an authenticated session and returns a successful bind response

#### Scenario: Authenticator rejects bind
- **WHEN** the authenticator rejects a bind request
- **THEN** the bind handler MUST NOT create an authenticated session and MUST return invalid credentials

### Requirement: Static account authenticator
The system SHALL provide a static account authenticator for configured DN/password pairs.

#### Scenario: Static account match
- **WHEN** the bind DN and password match a configured account
- **THEN** the authenticator accepts the bind

#### Scenario: Static account mismatch
- **WHEN** the bind DN is unknown or the password does not match
- **THEN** the authenticator rejects the bind

### Requirement: Authentication context
The authenticator SHALL receive bind DN, credentials, remote address, and available client certificate data.

#### Scenario: Remote address present
- **WHEN** a client sends a bind request
- **THEN** the authenticator receives the client's remote address in the authentication context

#### Scenario: Client certificate present
- **WHEN** TLS client certificate data is available for the channel
- **THEN** the authenticator receives certificate data in the authentication context

### Requirement: Protected operation session check
Handlers for protected LDAP operations SHALL verify authenticated session state before processing when anonymous access is disabled.

#### Scenario: Search before bind
- **WHEN** a client sends a search request before successful bind and anonymous access is disabled
- **THEN** the server returns an LDAP result indicating authentication is required
