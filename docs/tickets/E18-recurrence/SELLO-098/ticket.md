# SELLO-098 — Store schedules and record due occurrences on opening

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-097, SELLO-004
- **Gate:** G2

### Outcome
Schedules live in Room, and opening Sello records every due occurrence once, atomically.

### Deliverables
Schema migration, schedule commands, the catch-up use case with unique (schedule, date) origins, bounded and resumable, and receipts.

### Acceptance criteria
- An occurrence is recorded exactly once, even after a crash or a second opening.
- Catch-up is bounded and resumes where it stopped.
- Edits apply to future occurrences only; recorded ones stay.

### Tests
Real-Room tests for idempotency, crash between commits, migration and stale versions; G2.

### Working checkpoint
Opening after two weeks records each missed occurrence on its own date.

### Context and starting points
ARCHITECTURE §8 recurrence rules.

### Implementation plan
1. Schema.
2. Commands.
3. Catch-up and tests.

### Concrete cases and pitfalls
- Never write without a unique origin; a retried catch-up must not duplicate.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-098 --retain`.
