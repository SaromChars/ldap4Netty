## Context

`LDAPDecoder` currently reads bytes into arrays, scans for BER sequence tags, computes lengths, and calls `MyLDAPDecoder.decode2Message` for each complete frame. It attempts to retain incomplete bytes in `remainData`, but behavior around malformed input and multi-frame reads is under-tested.

## Goals / Non-Goals

**Goals:**

- Lock down frame boundary behavior with automated tests.
- Preserve support for fragmented and coalesced TCP reads.
- Preserve multi-response LDAP streams such as search entries plus search done.
- Improve error handling and logging.

**Non-Goals:**

- Replace Apache Directory API.
- Implement a new LDAP ASN.1 codec.
- Change LDAP message model APIs.

## Decisions

1. Add tests before refactoring decoder internals.

   Codec behavior is cross-cutting, so regression tests should describe the expected behavior before changing implementation.

2. Keep Apache Directory as the message decoder.

   This change focuses on frame boundaries, not ASN.1 semantics.

3. Prefer clear frame boundary code over exception-message-based control flow.

   Unsupported or malformed frames should follow a documented policy rather than relying on decoder exception strings.

## Risks / Trade-offs

- Refactoring codec may break proxy/server tests -> Mitigation: run full Maven suite after focused codec tests.
- Malformed frame behavior may need policy choices -> Mitigation: start with fail channel or emit no message, and document the chosen behavior.
