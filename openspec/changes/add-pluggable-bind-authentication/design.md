## Context

`LDAPBindHandler` currently extracts DN/password and stores a session, then returns success. Comments already indicate that real account matching should happen, but no extension point exists.

## Goals / Non-Goals

**Goals:**

- Move bind decisions behind a small authenticator interface.
- Keep static account authentication easy for tests and demos.
- Preserve SSL client certificate context when available.
- Prevent protected operations from being processed without an authenticated session when anonymous access is disabled.

**Non-Goals:**

- Implement password hashing or external identity providers.
- Implement LDAP SASL mechanisms.
- Implement full authorization/ACL behavior.

## Decisions

1. Use an interface rather than hard-coded account maps in the handler.

   This keeps bind policy separate from Netty channel handling and makes tests straightforward.

2. Return LDAP invalid credentials on authenticator rejection.

   This matches client expectations better than closing the channel or returning generic errors.

3. Pass certificate data through context only when available.

   TLS certificate validation remains the job of SSL setup; authentication policy can choose whether to inspect certificate data.

## Risks / Trade-offs

- Changing default bind behavior could break existing demos -> Mitigation: provide an accept-all or static default for demos where appropriate and document production-safe choices.
- Credential handling can leak in logs -> Mitigation: avoid logging password strings and cover with tests where practical.
