## Why

The repository already contains a working LDAP proxy path with upstream forwarding and interceptors, but it is still shaped like a demo. Stabilizing this first creates an immediately useful feature without expanding into full LDAP server semantics.

## What Changes

- Make `LDAPProxyServer` a first-class runtime with typed configuration validation.
- Add deterministic lifecycle APIs for non-blocking start, readiness, and idempotent stop.
- Document and test interceptor ordering, response reverse-ordering, and null-as-drop behavior.
- Ensure bind/search forwarding preserves message IDs and relays multi-message search responses.
- Harden access logging so bind credentials are never logged.
- Update README proxy usage examples.

## Capabilities

### New Capabilities

- `ldap-proxy-runtime`: Configuration-driven LDAP proxy runtime with lifecycle control, interceptors, access logging, and bind/search forwarding.

### Modified Capabilities

- None.

## Impact

- Affected code: `ldap-server/src/main/java/.../ldap/proxy`, `ProxyApp`, proxy integration tests, README.
- Affected APIs: `LDAPProxyServer`, `ProxyConfig`, `ProxyInterceptor`.
- Test impact: proxy lifecycle, bind forwarding, search forwarding, interceptor behavior, and logging safety.
