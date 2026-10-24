# SELLO-102 — Show expected income in Plan del mes

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Income history shows the v2 Plan del mes with expected incomes until they are recorded.

### Deliverables
The dashed plan box with Anotado, Esperado, Planeado and Sin asignar en el plan, and expected-income chips. None of its parts exists ([gap list](../../../design/v2/gaps/recurrence.md)); the design system gains a dashed plan slip with its **PLAN DEL MES** tag, a softer and a bold `LeaderLine`, a single-rule total and an expected-income chip, each in the catalog and in `docs/design/v2/components.html`.

### Acceptance criteria
- Expected money never enters recorded totals.
- An expected income becomes recorded on its date automatically.
- No plan box for a past month or without expected income.
- The catalog shows each new part in all themes and at large text.

### Tests
ViewModel and screen tests; device tests for the new parts; G2.

### Working checkpoint
Plan del mes shows expected salary until its date.

### Context and starting points
v2 Income; `IncomeHistoryScreen` and the `IncomeHistoryScreenTest` assertion that no **Plan del mes** exists yet. `ReviewInvitation` and `AddChip` draw a dashed outline in their own code; reuse it rather than drawing a third.

### Implementation plan
1. Plan box.
2. Chips.
3. Tests.

### Concrete cases and pitfalls
- Only shown when expected income exists.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-102 --retain`.
