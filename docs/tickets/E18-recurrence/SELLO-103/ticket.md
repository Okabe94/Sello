# SELLO-103 — Tell what Sello recorded on opening

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
After catching up, Sello lists what it recorded so each can be checked, edited or deleted.

### Deliverables
A notice and a list of the occurrences recorded in that catch-up, each opening its edit.

### Acceptance criteria
- Shown once per catch-up that recorded something; never empty.
- Each item opens the recorded expense or income.

### Tests
ViewModel and journey tests; G2.

### Working checkpoint
Opening after a week shows what was recorded.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md).

### Implementation plan
1. Notice.
2. List.
3. Tests.

### Concrete cases and pitfalls
- No notifications or background work.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-103 --retain`.
