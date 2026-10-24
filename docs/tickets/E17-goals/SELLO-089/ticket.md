# SELLO-089 — Add the Metas tab and goal detail

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-087, SELLO-004
- **Gate:** G2

### Outcome
Metas is a main tab with the v2 goal cards, totals band and completed goals, and each goal opens its detail.

### Deliverables
The Metas screen, goal detail and the tab, with loading, empty, failed and adaptive states.
Reuses `GoalsBandTotal`, `GoalCard`, `MilestoneRow`, `CompletedGoalRow`, `GoalPinPill`, `MovementRow`, `ColumnsChart`, `DangerSlip` and `ConfirmSlip` ([gap list](../../../design/v2/gaps/metas.md)); extends the shell's `SelloDock` with a tone and icon for the green **Aportar a una meta** / **Crear una meta**, and `CompletedGoalRow` with the LOGRADA landing.

### Acceptance criteria
- Cards ordered by highest completion; completed goals at the bottom with LOGRADA.
- The band totals active goals only, never investments.
- On this tab the dock reads **Aportar a una meta**.

### Tests
Screen and journey tests across windows, text sizes and themes; G2.

### Working checkpoint
Goals appear in Metas and open their detail.

### Context and starting points
v2 Metas and goal pages.

### Implementation plan
1. Tab and navigation.
2. Metas screen.
3. Goal detail and states.

### Concrete cases and pitfalls
- No month bar on Metas: goals are not tied to a month.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-089 --retain`.
