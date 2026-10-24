# SELLO-101 — Show reservations on Recibo

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Recibo's headline is after reserving upcoming payments, with Sin margen and Revisar, and the reserved footnote.

### Deliverables
ADR 0012's reserved states in the answer slip, the footnote and the list of upcoming payments.

### Acceptance criteria
- Headline = remaining − reserved, per ADR 0012 and its examples.
- Sin margen and Revisar appear exactly when their rows say.
- No reservation line when nothing is reserved.

### Tests
Domain example tests (ADR 0012 BG rows) and screen tests; G2.

### Working checkpoint
Recibo shows the reserved states correctly.

### Context and starting points
ADR 0012; v2 Recibo.

### Implementation plan
1. Domain changes.
2. Slip states and footnote.
3. Payments list.

### Concrete cases and pitfalls
- Categories never show reservations.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-101 --retain`.
