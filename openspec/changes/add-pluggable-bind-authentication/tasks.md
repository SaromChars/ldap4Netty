## 1. Authentication API

- [x] 1.1 Add authenticator interface and authentication result model.
- [x] 1.2 Add authentication context with DN, credentials, remote address, and optional certificate data.
- [x] 1.3 Add static account authenticator implementation.

## 2. Bind Handler Integration

- [x] 2.1 Update `LDAPBindHandler` to call the authenticator.
- [x] 2.2 Create sessions only after successful authentication.
- [x] 2.3 Return invalid credentials for rejected bind attempts.
- [x] 2.4 Ensure bind handling does not log password strings.

## 3. Protected Operation Checks

- [x] 3.1 Add a reusable session validation helper or handler.
- [x] 3.2 Apply session validation to search/modify handlers where configured.
- [x] 3.3 Add tests for search before bind with anonymous access disabled.

## 4. Verification

- [x] 4.1 Add tests for accepted bind and rejected bind.
- [x] 4.2 Add tests for static account matching and mismatch.
- [x] 4.3 Add certificate context coverage where practical.
- [x] 4.4 Run `mvn test`.
- [x] 4.5 Run `openspec validate --changes add-pluggable-bind-authentication`.
