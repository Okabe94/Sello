# SELLO-106 — Approve investment rules and worked examples

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-105, SELLO-004
- **Gate:** G1

### Outcome
Reviewed rules and independent examples for investments before any code.

### Deliverables
Examples for contributions, withdrawals (never above the last value), dividends, values, the simple return, missing values, USD with the app-wide rate and corrections.

### Acceptance criteria
- Every ADR 0017 investment decision has examples with exact amounts.
- Rate validation (positive, bounded, decimal) and its date are defined.
- The owner approves; open points are listed.

### Tests
Owner review; G1.

### Working checkpoint
Approved examples exist.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); Inversiones brief; ARCHITECTURE FX rules.

### Implementation plan
1. Draft.
2. Review.
3. Record approval.

### Concrete cases and pitfalls
- Never a 0 % return without contributions.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-106 --retain`.
