# SELLO-094 — Cover goals in the sandbox and catalog

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-092, SELLO-004
- **Gate:** G2

### Outcome
Sandbox scenarios and catalog examples exercise goals through the real command paths.

### Deliverables
Scenarios for stamps, withdrawals, target edits and completion; catalog states for the extensions the earlier tickets add, and device tests for `GoalPill`, `MovementRow`, `StampTicket`, `GoalPinPill` and `IconChoiceStrip`, which have none ([gap list](../../../design/v2/gaps/metas.md)).

### Acceptance criteria
- Scenarios use real commands and storage with synthetic data.
- Catalog shows every extended component in all themes and states.
- None of it ships in the release build.

### Tests
Scenario corpus tests; catalog tests; G2.

### Working checkpoint
The sandbox runs each goal scenario to its check.

### Context and starting points
AGENTS UI and developer-tools rules; ARCHITECTURE §9.

### Implementation plan
1. Scenarios.
2. Catalog.
3. Tests.

### Concrete cases and pitfalls
- Keep sandbox clocks distinct as §9 requires.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-094 --retain`.
