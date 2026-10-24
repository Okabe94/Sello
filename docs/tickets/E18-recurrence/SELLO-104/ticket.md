# SELLO-104 — Cover recurrence in the sandbox

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Sandbox scenarios exercise schedules, catch-up after time travel, pause and edits.

### Deliverables
Scenarios with financial-clock jumps and checks of recorded occurrences and reservations. `Scenario.kt`, the [sandbox guide](../../../development/sandbox.md) and the [scenario corpus](../../../testing/sandbox-scenarios.md) no longer say recurrence does not exist.

### Acceptance criteria
- Scenarios use the real catch-up path.
- None ships in the release build.

### Tests
Scenario corpus tests; G2.

### Working checkpoint
Each recurrence scenario reaches its check.

### Context and starting points
ARCHITECTURE §9.

### Implementation plan
1. Scenarios.
2. Checks.
3. Tests.

### Concrete cases and pitfalls
- Keep clocks distinct.
- The corpus's "Recurrence is not covered" list asks for WorkManager worker tests; ADR 0017 has no background work, so the scenarios drive the catch-up that runs on opening instead.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-104 --retain`.
