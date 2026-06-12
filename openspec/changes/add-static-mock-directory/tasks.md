## 1. Directory Model

- [x] 1.1 Add in-memory entry model with DN and multi-valued attributes.
- [x] 1.2 Add validation for invalid DNs and invalid attributes.
- [x] 1.3 Add unit tests for entry validation.

## 2. Search Matching

- [x] 2.1 Implement base object, one-level, and subtree scope matching.
- [x] 2.2 Implement equality and presence filter matching.
- [x] 2.3 Add unit tests for scope and filter combinations.

## 3. Handler Integration

- [x] 3.1 Update search handling to query the directory store.
- [x] 3.2 Ensure entries are written before exactly one search done response.
- [x] 3.3 Return deterministic LDAP results for unsupported read-only write operations.
- [x] 3.4 Add integration tests for configured search responses and unsupported modify behavior.

## 4. Documentation And Verification

- [x] 4.1 Add README mock directory examples.
- [x] 4.2 Run `mvn test`.
- [x] 4.3 Run `openspec validate --changes add-static-mock-directory`.
