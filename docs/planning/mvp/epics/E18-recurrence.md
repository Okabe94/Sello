# SELLO-E18 — Recurring payments, incomes and reservations

- **Status:** Backlog
- **Increment:** Post-MVP
- **Goal:** Let a person schedule recurring payments and incomes that Sello records on their dates, reserving payments still to come.
- **Exit:** Schedules can be created, paused, ended and changed; due occurrences are recorded on opening with a notice; Recibo's headline is after reservations with Sin margen and Revisar; income history shows Plan del mes; backups carry schedules.

**Owner direction (2026-10-23):** decisions in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md): monthly, twice a month and
every N days or weeks; recorded automatically when Sello opens, catching up every
missed occurrence on its own date; changes affect future occurrences only; pause,
resume and an optional end; no notifications or background work. Ships with Metas and
Inversiones in one release. [ADR 0012](../../../decisions/0012-budget-guidance-contract.md)
and ARCHITECTURE §8 own reservations and recurrence safety.

## SELLO-095 — Verify what exists for recurrence and reserved payments and what is missing

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-004
- **Gate:** G1

### Outcome
An exact, current list of what the app, the design system and the catalog already have for recurrence and reserved payments, against the approved design, so the following tickets build only what is missing.

### Deliverables
A gap list in `docs/design/v2/gaps/` for recurrence and reserved payments: each designed element marked present (with its code and tests), partial (what differs) or missing; the following tickets updated, split or closed to match, with owner approval for any scope change.

### Acceptance criteria
- Every element of the v2 reserved-payment states on Recibo and the Plan del mes on income is checked against the running app and the code, not only the docs.
- Each present element names its code path and test; each partial one says exactly what differs.
- Work done outside these tickets (other branches, merged PRs, catalog components) is found and credited.
- SELLO-096 to SELLO-104 are revised to the list before any starts; changed scope is recorded and owner-approved.

### Tests
The audit itself, with screenshots of the running app where it differs from the design; G1.

### Working checkpoint
The gap list is merged and every later ticket in this epic matches it.

### Context and starting points
The screens have been worked on outside these tickets (owner, 2026-10-23); [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md) records the decisions. Design: [v2 README](../../../design/v2/README.md) and `docs/design/v2/components.html` (Exists / Extend / New).

### Implementation plan
1. List every designed element, state and rule.
2. Check each in the app on the emulator and in the code and catalog.
3. Write the gap list and revise the following tickets; ask the owner about scope changes.

### Concrete cases and pitfalls
- Do not trust a document's claim that something exists: open the screen and the code.
- Do not start building while auditing; findings go into the list and tickets.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-095 --retain`.

## SELLO-096 — Approve recurrence rules and worked examples

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-095, SELLO-004
- **Gate:** G1

### Outcome
Reviewed rules and independent examples for schedules, catch-up, reservations, pause and edits.

### Deliverables
Examples for each schedule kind (month ends, twice a month vs every 15 days), catch-up after weeks away, reservations in Recibo (Sin margen, Revisar), expected income, pause, end and future-only edits.

### Acceptance criteria
- Every ADR 0017 recurrence decision has worked examples with exact dates and amounts.
- Catch-up bounds and the notice's content are defined.
- The owner approves; open points are listed.

### Tests
Owner review; G1.

### Working checkpoint
Approved examples exist and later tickets cite them.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); ADR 0012 rows; ARCHITECTURE §8.

### Implementation plan
1. Draft.
2. Review.
3. Record approval.

### Concrete cases and pitfalls
- Retain the calendar anchor when clamping month ends.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-096 --retain`.

## SELLO-097 — Calculate schedule occurrences

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-096, SELLO-004
- **Gate:** G2

### Outcome
Domain works out every occurrence of a schedule, exactly, for any range.

### Deliverables
Schedule models and an occurrence policy for the three kinds, with pause and end.

### Acceptance criteria
- Matches every approved example, including clamping and anchors.
- Every 15 days and twice a month differ as approved.
- Pauses skip occurrences; an end date stops them.

### Tests
Property and example tests in domain; G1 host tests within G2.

### Working checkpoint
Occurrences match the examples.

### Context and starting points
Approved examples (SELLO-096).

### Implementation plan
1. Models.
2. Policy.
3. Tests.

### Concrete cases and pitfalls
- Use financial dates, never device time.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-097 --retain`.

## SELLO-098 — Store schedules and record due occurrences on opening

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-097, SELLO-004
- **Gate:** G2

### Outcome
Schedules live in Room, and opening Sello records every due occurrence once, atomically.

### Deliverables
Schema migration, schedule commands, the catch-up use case with unique (schedule, date) origins, bounded and resumable, and receipts.

### Acceptance criteria
- An occurrence is recorded exactly once, even after a crash or a second opening.
- Catch-up is bounded and resumes where it stopped.
- Edits apply to future occurrences only; recorded ones stay.

### Tests
Real-Room tests for idempotency, crash between commits, migration and stale versions; G2.

### Working checkpoint
Opening after two weeks records each missed occurrence on its own date.

### Context and starting points
ARCHITECTURE §8 recurrence rules.

### Implementation plan
1. Schema.
2. Commands.
3. Catch-up and tests.

### Concrete cases and pitfalls
- Never write without a unique origin; a retried catch-up must not duplicate.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-098 --retain`.

## SELLO-099 — Carry schedules through backup, restore and erase

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Backups include schedules; restore and erase handle them without replaying occurrences twice.

### Deliverables
Format additions with docs, restore and reset updates.

### Acceptance criteria
- Restored schedules do not re-record occurrences already in the backup.
- Older files still restore.

### Tests
Codec and round-trip tests; G2.

### Working checkpoint
A backup with schedules restores and catches up correctly.

### Context and starting points
`docs/data/backup-format.md`.

### Implementation plan
1. Format.
2. Restore and reset.
3. Tests.

### Concrete cases and pitfalls
- Restore resets catch-up state to the backup's recorded occurrences.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-099 --retain`.

## SELLO-100 — Manage recurring payments and incomes

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
A person can create, change, pause, resume, end and delete recurring payments and incomes.

### Deliverables
Screens to list and edit schedules, reached from Configuración and from an entry's detail.

### Acceptance criteria
- Each schedule kind can be set with the Sello pad for amounts.
- Changes say they apply from now on.
- Deleting keeps recorded occurrences.

### Tests
ViewModel, screen and journey tests; G2.

### Working checkpoint
Schedules can be managed on a phone.

### Context and starting points
v1.1 snapshot for screens v2 does not show.

### Implementation plan
1. List.
2. Editor.
3. Pause, end, delete.

### Concrete cases and pitfalls
- Keep amounts exact; no rounding.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-100 --retain`.

## SELLO-101 — Show reservations on Recibo

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Recibo's headline is after reserving upcoming payments, with Sin margen and Revisar, and the reserved footnote.

### Deliverables
ADR 0012's reserved states in the answer slip, the footnote and the list of upcoming payments.

### Acceptance criteria
- Headline = remaining − reserved, per ADR 0012 and its examples.
- Sin margen and Revisar appear exactly when their rows say.
- No reservation line when nothing is reserved.

### Tests
Domain example tests (ADR 0012 BG rows) and screen tests; G2.

### Working checkpoint
Recibo shows the reserved states correctly.

### Context and starting points
ADR 0012; v2 Recibo.

### Implementation plan
1. Domain changes.
2. Slip states and footnote.
3. Payments list.

### Concrete cases and pitfalls
- Categories never show reservations.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-101 --retain`.

## SELLO-102 — Show expected income in Plan del mes

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

## SELLO-103 — Tell what Sello recorded on opening

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

## SELLO-104 — Cover recurrence in the sandbox

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Sandbox scenarios exercise schedules, catch-up after time travel, pause and edits.

### Deliverables
Scenarios with financial-clock jumps and checks of recorded occurrences and reservations.

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

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-104 --retain`.
