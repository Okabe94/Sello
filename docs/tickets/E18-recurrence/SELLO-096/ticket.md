# SELLO-096 — Approve recurrence rules and worked examples

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-095, SELLO-004
- **Gate:** G1

### Outcome
Reviewed rules and independent examples for schedules, catch-up, reservations, pause and edits.

### Deliverables
Examples for each schedule kind (month ends, twice a month vs every 15 days), catch-up after weeks away, reservations in Recibo (Sin margen, Revisar), expected income, pause, end and future-only edits.

### Acceptance criteria
- Every ADR 0017 recurrence decision has worked examples with exact dates and amounts.
- Catch-up bounds and the notice's content are defined.
- The owner approves; open points are listed.

### Tests
Owner review; G1.

### Working checkpoint
Approved examples exist and later tickets cite them.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); ADR 0012 rows; ARCHITECTURE §8.

### Implementation plan
1. Draft.
2. Review.
3. Record approval.

### Concrete cases and pitfalls
- Retain the calendar anchor when clamping month ends.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-096 --retain`.
