## 1. Tests First

- [x] 1.1 Add proxy lifecycle tests for non-blocking start, readiness, and idempotent stop.
- [x] 1.2 Add proxy forwarding tests for bind and search response streams.
- [x] 1.3 Add interceptor ordering and null-as-drop tests.
- [x] 1.4 Add access logging safety coverage for simple bind credentials.

## 2. Runtime Implementation

- [x] 2.1 Add validation to `ProxyConfig` for required host, valid ports, and timeout values.
- [x] 2.2 Add non-blocking start and readiness support to `LDAPProxyServer`.
- [x] 2.3 Track listen and upstream channels so stop closes resources deterministically.
- [x] 2.4 Preserve existing blocking `start()` behavior using the same binding path.

## 3. Interceptors And Logging

- [x] 3.1 Document request and response interceptor ordering in code and README.
- [x] 3.2 Ensure null-as-drop behavior is explicit and covered by tests.
- [x] 3.3 Ensure access logging redacts or omits bind credentials.

## 4. Documentation And Verification

- [x] 4.1 Update README with proxy runtime usage and configuration example.
- [x] 4.2 Run `mvn test`.
- [x] 4.3 Run `openspec validate --changes stabilize-ldap-proxy-runtime`.
