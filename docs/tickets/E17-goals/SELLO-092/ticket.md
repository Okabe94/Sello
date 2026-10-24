# SELLO-092 — Earn stamps and complete goals

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-091, SELLO-004
- **Gate:** G2

### Outcome
Crossing 25/50/75 % earns a stamp with a modest celebration; reaching 100 % offers **Marcar como lograda**.

### Deliverables
Milestone detection, celebrations with reduced-motion equivalents, completion with a 10-second Deshacer.
Reuses `MilestonePlace` landing, `StampTicket`, `ReachedRow` and `CelebrationOverlay` ([gap list](../../../design/v2/gaps/metas.md)); adds the kept stamp's relight and the 10-second Deshacer, longer with TalkBack.

### Acceptance criteria
- Stamps follow the approved examples, including faded stamps after a withdrawal or target change.
- Celebrations follow only a confirmed save.
- After the Deshacer window the goal is final and read-only.

### Tests
Domain and journey tests for each crossing, replay safeguards and reduced motion; G2.

### Working checkpoint
A goal can reach each stamp and be completed with its celebration.

### Context and starting points
v2 celebrations page.

### Implementation plan
1. Milestone detection.
2. Celebrations.
3. Completion and undo.

### Concrete cases and pitfalls
- Reopening a screen is not a new crossing.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-092 --retain`.
