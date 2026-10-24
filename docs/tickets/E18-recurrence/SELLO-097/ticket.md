# SELLO-097 — Calculate schedule occurrences

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-096, SELLO-004
- **Gate:** G2

### Outcome
Domain works out every occurrence of a schedule, exactly, for any range.

### Deliverables
Schedule models and an occurrence policy for the three kinds, with pause and end.

### Acceptance criteria
- Matches every approved example, including clamping and anchors.
- Every 15 days and twice a month differ as approved.
- Pauses skip occurrences; an end date stops them.

### Tests
Property and example tests in domain; G1 host tests within G2.

### Working checkpoint
Occurrences match the examples.

### Context and starting points
Approved examples (SELLO-096).

### Implementation plan
1. Models.
2. Policy.
3. Tests.

### Concrete cases and pitfalls
- Use financial dates, never device time.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-097 --retain`.
