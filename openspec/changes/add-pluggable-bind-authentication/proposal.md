## Why

The bind handler currently accepts credentials without a real policy, which is acceptable for a demo but unsafe and limiting for a mock LDAP server. A small authenticator abstraction lets tests and local runtimes choose explicit bind behavior.

## What Changes

- Add a pluggable authenticator interface for bind requests.
- Add an authentication context with bind DN, credentials, remote address, and optional client certificate data.
- Provide a static account authenticator for tests and examples.
- Update bind handling to create sessions only after successful authentication.
- Return invalid credentials when authentication fails.
- Add protected-operation checks where needed.

## Capabilities

### New Capabilities

- `pluggable-bind-authentication`: Configurable bind authentication policy and authenticated session creation.

### Modified Capabilities

- None.

## Impact

- Affected code: `LDAPBindHandler`, `LDAPSession`, SSL context/certificate handling, handlers that depend on authenticated sessions.
- Test impact: accepted bind, rejected bind, static account matching, certificate context, operation before bind.
