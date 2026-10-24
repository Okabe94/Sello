# SELLO-102 — Show expected income in Plan del mes

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Income history shows the v2 Plan del mes with expected incomes until they are recorded.

### Deliverables
The dashed plan box with Anotado, Esperado, Planeado and Sin asignar en el plan, and expected-income chips.

### Acceptance criteria
- Expected money never enters recorded totals.
- An expected income becomes recorded on its date automatically.

### Tests
ViewModel and screen tests; G2.

### Working checkpoint
Plan del mes shows expected salary until its date.

### Context and starting points
v2 Income.

### Implementation plan
1. Plan box.
2. Chips.
3. Tests.

### Concrete cases and pitfalls
- Only shown when expected income exists.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-102 --retain`.
