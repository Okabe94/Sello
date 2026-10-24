# SELLO-093 — Show one goal on Recibo

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-092, SELLO-004
- **Gate:** G2

### Outcome
Recibo shows the chosen goal as the v2 pill with Aportar, and a row when another goal reached 100 %.

### Deliverables
The goal pill, the person's choice of goal, the fallback rule and the green 100 % row.
Reuses `GoalPill` and `ReachedRow` ([gap list](../../../design/v2/gaps/metas.md)).

### Acceptance criteria
- The person's choice wins; otherwise the highest completion; nothing with no active goal.
- When the shown goal is completed or deleted, the fallback applies.
- Not shown on a past month.

### Tests
ViewModel and screen tests for each rule; G2.

### Working checkpoint
Recibo shows the right goal in each case.

### Context and starting points
Fallback decided in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); v2 Recibo.

### Implementation plan
1. Choice storage.
2. Pill and row.
3. Tests.

### Concrete cases and pitfalls
- The pill is the progress bar; keep its exact percentage accessible.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-093 --retain`.
