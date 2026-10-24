# SELLO-100 — Manage recurring payments and incomes

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-114, SELLO-004
- **Gate:** G2

### Outcome
A person can create, change, pause, resume, end and delete recurring payments and incomes.

### Deliverables
Screens to list and edit schedules, built to the design approved in SELLO-114, including where they are reached from and how a schedule is created.

### Acceptance criteria
- The screens match SELLO-114's approved pages.
- Each schedule kind can be set with the Sello pad for amounts.
- Changes say they apply from now on.
- Deleting keeps recorded occurrences.

### Tests
ViewModel, screen and journey tests; G2.

### Working checkpoint
Schedules can be managed on a phone.

### Context and starting points
SELLO-114's approved pages; the [gap list](../../../design/v2/gaps/recurrence.md) names the reusable parts (`SegmentedSwitch`, `LeaderLine`, `ActionLine`, `AmountKeypad`, `ConfirmSlip`, `DangerSlip`, `Stamp`).

### Implementation plan
1. List.
2. Editor.
3. Pause, end, delete.

### Concrete cases and pitfalls
- Keep amounts exact; no rounding.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-100 --retain`.
