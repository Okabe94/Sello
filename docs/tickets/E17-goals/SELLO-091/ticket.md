# SELLO-091 — Record contributions and withdrawals

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-089, SELLO-004
- **Gate:** G2

### Outcome
Aportar and Retirar record movements, with past dates and corrections.

### Deliverables
The Aportar/Retirar form with the Sello pad and date pill, the movement history with edit, delete and undo.
Reuses `EntryLayout`, `SegmentedSwitch`, `EntryAmountSlip` (Gain / Ink), `FactLine` and `FieldPill` ([gap list](../../../design/v2/gaps/metas.md)); extends `SelloChip` with the goal's percentage.

### Acceptance criteria
- A withdrawal above saved is refused with a clear message.
- Past dates allowed, future refused.
- Unknown outcomes are recovered before another write.

### Tests
ViewModel, screen and journey tests; duplicate-submit and unknown-outcome cases; G2.

### Working checkpoint
Movements recorded, edited and undone update the goal exactly.

### Context and starting points
v2 Aportar page.

### Implementation plan
1. Form.
2. History with corrections.
3. Tests.

### Concrete cases and pitfalls
- Contributions never touch category budgets.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-091 --retain`.
