## ADDED Requirements

### Requirement: Static directory entries
The system SHALL allow callers to configure in-memory LDAP entries with distinguished names and attributes.

#### Scenario: Valid entries
- **WHEN** a mock directory is created with valid configured entries
- **THEN** those entries are available to search requests

#### Scenario: Invalid entry
- **WHEN** an entry has an invalid distinguished name or invalid attribute definition
- **THEN** the system MUST reject the configuration with a clear error

### Requirement: Search scope matching
The mock directory SHALL match entries using LDAP base object, one-level, and subtree search scopes.

#### Scenario: Base object scope
- **WHEN** a search uses base object scope for an existing entry DN
- **THEN** only the exact entry is returned

#### Scenario: One-level scope
- **WHEN** a search uses one-level scope under a parent DN
- **THEN** direct child entries are returned and deeper descendants are excluded

#### Scenario: Subtree scope
- **WHEN** a search uses subtree scope under a parent DN
- **THEN** matching entries at or below the base DN are returned

### Requirement: Basic filter matching
The mock directory SHALL support equality and presence filters for configured attributes.

#### Scenario: Equality filter
- **WHEN** a search equality filter matches an entry attribute value
- **THEN** that entry is included in the results

#### Scenario: Presence filter
- **WHEN** a search presence filter matches an entry attribute
- **THEN** that entry is included in the results

#### Scenario: No filter matches
- **WHEN** no configured entries satisfy the filter
- **THEN** the server returns no entries and a successful search done response

### Requirement: Search response ordering
The mock directory SHALL write all matching search result entries before writing exactly one search done response.

#### Scenario: Completed search
- **WHEN** the mock directory handles a search request
- **THEN** it writes zero or more entries followed by one search done response with the request message ID

### Requirement: Read-only unsupported operations
The mock directory SHALL return deterministic LDAP result responses for unsupported write operations in read-only mode.

#### Scenario: Modify request in read-only mode
- **WHEN** a client sends a modify request to the read-only mock directory
- **THEN** the server returns an LDAP result indicating the operation is unsupported or unwilling to perform
