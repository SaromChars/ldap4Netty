## Why

LDAP traffic rides over TCP, so requests and responses can arrive fragmented or coalesced. The current decoder has custom frame scanning logic, making codec behavior the highest-risk shared foundation for server, client, and proxy correctness.

## What Changes

- Add regression tests for fragmented LDAP frames, coalesced frames, malformed frames, and search multi-response streams.
- Refactor or harden `LDAPDecoder` frame boundary handling while preserving Apache Directory message decoding.
- Define deterministic behavior for malformed or incomplete frames.
- Replace codec `printStackTrace` handling with structured logging and predictable channel behavior.

## Capabilities

### New Capabilities

- `ldap-codec-framing`: Reliable LDAP frame handling for fragmented, coalesced, malformed, and multi-message traffic.

### Modified Capabilities

- None.

## Impact

- Affected code: `ldap-util` codec classes and codec tests.
- Affected runtime: all server, client, and proxy pipelines that use `LDAPDecoder` and `LDAPEncoder`.
- Test impact: codec tests should not require external LDAP services.
