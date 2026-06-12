## 1. Codec Regression Tests

- [x] 1.1 Add test utilities to encode LDAP messages into byte arrays.
- [x] 1.2 Add fragmented frame tests for bind requests.
- [x] 1.3 Add coalesced frame tests for multiple complete requests.
- [x] 1.4 Add search multi-response tests for entries plus done.
- [x] 1.5 Add malformed and incomplete frame tests.

## 2. Decoder Hardening

- [x] 2.1 Review current `LDAPDecoder` frame scanning and incomplete byte retention.
- [x] 2.2 Refactor frame boundary handling if needed to satisfy tests.
- [x] 2.3 Remove exception-message-based frame skipping where possible.
- [x] 2.4 Replace `printStackTrace` with logger-based exception handling.

## 3. Verification

- [x] 3.1 Run focused `ldap-util` tests.
- [x] 3.2 Run full `mvn test`.
- [x] 3.3 Run `openspec validate --changes harden-ldap-codec-framing`.
