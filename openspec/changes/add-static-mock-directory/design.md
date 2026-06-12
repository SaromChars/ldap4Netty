## Context

`LDAPSearchHandler` currently emits a fixed `cn=test` entry and a successful search done response. This is enough for smoke tests but not useful for realistic local development.

## Goals / Non-Goals

**Goals:**

- Provide a static in-memory directory store.
- Support common search scope and basic filter matching.
- Keep the implementation small and testable.
- Preserve LDAP response ordering: entries first, done last.

**Non-Goals:**

- Persistent storage.
- Schema validation.
- ACL evaluation.
- Advanced LDAP filter optimization or full filter coverage.

## Decisions

1. Start with typed in-memory entries.

   The core store should accept Java objects first. File loading can convert into the same model without coupling search logic to a file format.

2. Support equality and presence filters first.

   These cover common application health checks and simple lookups while keeping matching behavior tractable.

3. Keep write operations read-only by default.

   Unsupported write operations should return deterministic LDAP results rather than silently succeeding.

## Risks / Trade-offs

- Basic filters may not cover all application queries -> Mitigation: document supported filters and add extension points later.
- DN/scope matching mistakes can be subtle -> Mitigation: add direct unit tests for base, one-level, and subtree behavior.
