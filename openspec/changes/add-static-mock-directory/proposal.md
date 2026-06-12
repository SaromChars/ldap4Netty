## Why

The current search handler returns hard-coded demo data, which limits the project to examples. A static in-memory directory makes ldap4Netty useful as a local LDAP mock server for development and CI.

## What Changes

- Add an in-memory directory entry model with DN and attributes.
- Support static configuration of directory entries.
- Match search requests by base DN, scope, equality filters, and presence filters.
- Return matching search entries followed by one search done response.
- Return deterministic LDAP result responses for unsupported read-only mock write operations.
- Add README examples for mock directory usage.

## Capabilities

### New Capabilities

- `static-mock-directory`: Configurable in-memory LDAP directory for bind/search development and test scenarios.

### Modified Capabilities

- None.

## Impact

- Affected code: `LDAPSearchHandler`, new directory store/model classes, mock server examples, tests, README.
- Possible dependency impact: optional YAML/JSON loading if file-based config is included.
- Test impact: directory validation, scope matching, filter matching, search response completion.
