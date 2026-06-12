## ADDED Requirements

### Requirement: Fragmented frame decoding
The LDAP decoder SHALL retain incomplete LDAP frame bytes and emit a message only when the full frame has arrived.

#### Scenario: Fragmented bind request
- **WHEN** a bind request arrives split across multiple reads
- **THEN** the decoder emits exactly one bind request after the final fragment arrives

### Requirement: Coalesced frame decoding
The LDAP decoder SHALL emit every complete LDAP message when multiple frames arrive in one read.

#### Scenario: Multiple requests in one buffer
- **WHEN** two complete LDAP request frames arrive in the same buffer
- **THEN** the decoder emits both messages in frame order

### Requirement: Search multi-response preservation
The LDAP codec SHALL support multiple LDAP response messages for one search request message ID.

#### Scenario: Search entries and done
- **WHEN** a search response stream contains entries followed by done
- **THEN** the decoder emits each entry and the done response without dropping repeated message IDs

### Requirement: Malformed frame behavior
The LDAP decoder SHALL avoid emitting corrupted messages for malformed LDAP frame bytes.

#### Scenario: Invalid frame bytes
- **WHEN** invalid bytes cannot form a valid LDAP message
- **THEN** the decoder MUST NOT emit a partial or corrupted message

#### Scenario: Declared length not yet available
- **WHEN** a frame header declares a body length that has not fully arrived
- **THEN** the decoder retains bytes and waits for additional data

### Requirement: Codec error handling
The LDAP codec SHALL use structured logging and deterministic channel behavior for codec exceptions.

#### Scenario: Decoder exception
- **WHEN** a decoder exception occurs
- **THEN** the codec logs the failure through the logger and closes or fails the channel according to documented policy
