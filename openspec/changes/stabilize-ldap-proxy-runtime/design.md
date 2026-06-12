## Context

`ldap-server` now includes `LDAPProxyServer`, `ProxyHandler`, `UpstreamConnector`, `ProxyConfig`, and an interceptor chain. Current tests prove basic bind/search forwarding when local port binding is available, but startup relies on blocking calls and sleep-based readiness.

## Goals / Non-Goals

**Goals:**

- Stabilize the existing 1:1 client-to-upstream proxy model.
- Provide typed config validation and deterministic lifecycle operations.
- Make interceptor behavior explicit and test-covered.
- Keep credential-safe access logging.

**Non-Goals:**

- Add upstream connection pooling.
- Add message ID rewriting beyond preserving existing IDs in the 1:1 model.
- Add mock directory or authentication policy changes.
- Add TLS proxying in this first pass.

## Decisions

1. Keep the 1:1 upstream connection model.

   It is simpler, already exists, and avoids pooling/message ID rewriting complexity. The existing `MessageIdMapper` can remain unused or future-facing.

2. Preserve blocking `start()` and add non-blocking lifecycle APIs.

   Existing demos can continue using `start()`, while tests and embedded users can use start/ready/stop calls.

3. Treat interceptor null return as an intentional drop.

   This behavior already exists and should be documented and tested rather than changed.

## Risks / Trade-offs

- Blocking and non-blocking lifecycle APIs can diverge -> Mitigation: implement blocking `start()` in terms of the same internal channel binding path.
- Interceptors may mutate shared state unsafely -> Mitigation: document per-channel expectations and test ordering only, not thread safety guarantees.
- Tests may depend on fixed ports -> Mitigation: allow port configuration and readiness waiting.

## Open Questions

- Should proxy config support listen host now, or only listen port?
- Should access logging use SLF4J structured arguments or current formatted messages?
