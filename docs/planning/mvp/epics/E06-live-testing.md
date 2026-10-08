# SELLO-E06 — Debug live-testing sandbox

- **Status:** Backlog
- **Goal:** Explore time-sensitive financial behavior safely through the actual app pipeline.
- **Exit:** Isolated controllable dates, reproducible histories/graphs, inspectable receipts and restart/failure scenarios; no tooling in customer release.

## SELLO-024 — Create isolated sandbox sessions and financial time controls

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-005, SELLO-011, SELLO-015, SELLO-004
- **Gate:** G2

### Outcome
Live testing can freeze/advance dates without touching production data or unrelated clocks.

### Deliverables
Debug-source-set tools entry, explicit sandbox badge, dedicated sandbox Room/
preferences namespace and session lifecycle; freeze/day/month/date controls and
bounded play/pause using the observable financial clock. Persist time/zone/profile
for process-restart testing; keep real audit and monotonic sources independent.

### Acceptance criteria
- Debug application ID differs from production; sandbox switching also preserves
  the normal debug profile. Switching/reset cannot race an active financial write.
- Device time, undo expiry, animation and network timeout never accelerate. Date
  changes trigger normal snapshot/rollover paths, not debug-only calculations.
- Rewind after mutation requires confirmed scenario reset/replay; a displayed old
  date cannot pretend newer persisted history never happened.
- Tools/classes/resources/routes have no release dependency; no “hidden menu” substitute.

### Tests
Clock separation, persistence/restart and switch/session-isolation tests; device
date stepping and badge checks; release class/resource/manifest inspection; G2.

### Working checkpoint
Debug app visibly changes financial date; customer release uses real financial
time. Real fixtures arrive in the next ticket, not fake financial screens here.

### Context and starting points
This is an isolated laboratory, not a release settings menu or altered device clock.
Use the distinct clock ports from SELLO-005, real Room factory from SELLO-011 and
app Root from SELLO-015. Read architecture §9,
[module map](../EXECUTION_GUIDE.md#module-and-file-map) and accepted D02/D09.

### Implementation plan
1. Add debug-only tools UI/bindings with separate sandbox DB/preferences namespace
   and visible session/date/zone badge; normal debug profile and production stay untouched.
2. Define owned session start/stop/switch: stop old observers/runners, wait/cancel work
   safely and invalidate old UI actions before binding the new profile/financial clock.
3. Implement freeze, +day, +month, jump and bounded play/pause through the observable
   financial clock; keep audit/monotonic sources unchanged. Persist sandbox clock/session state.
4. Require reset/replay to rewind mutated scenario history; initial arbitrary date
   selection is safe only before financial mutations. Show explicit confirmation/state.
5. Add startup/restart and release-exclusion checks. Seed/history execution is SELLO-025,
   not unchecked inserts disguised as the time-control implementation.

### Concrete cases and pitfalls
- Advance Oct 31 to Nov 1: normal month/rollover observation runs; undo expiry and
  background inactivity do not accelerate. Device wall clock remains unchanged.
- Switching profile with a pending old form cannot send its action into the new DB
  just because both generations happen to be 1; session origin is also guarded.
- A customer release must contain no tools route/controller/fixtures, even without
  shrinking. BuildConfig-hidden controls are insufficient isolation.

### Verification recipe
Create/run `*SandboxSessionTest`, `*SandboxClockPersistenceTest` and debug TimeControlsJourneyTest;
G2. Compare real audit/monotonic time before/after stepping, reopen sandbox and normal
profiles, then inspect release manifest/resources/classes for developer artifacts.

## SELLO-025 — Implement seeded histories, replay and live graph-growth scenarios

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-017, SELLO-018, SELLO-022, SELLO-024, SELLO-004
- **Gate:** G2

### Outcome
A developer can watch actual graphs/budgets grow by replaying deterministic financial actions.

### Deliverables
Versioned synthetic scenario fixtures with seed/time/zone, validated category/
expense/income commands, bounded date-step runner, pause/resume/checkpoint and
reset-to-baseline. Tools show operation IDs, receipts, revisions and step outcomes.

### Acceptance criteria
- Same baseline/seed/actions reproduces exact totals/history; each step writes through
  production commands and reads actual Room/snapshots. No unchecked DAO seed shortcuts.
- Replaying/restarting a partially completed step uses the same operation identity;
  no duplicate expenses/income. Manual pause has observable progress and no unbounded loop.
- Scenarios demonstrate day/month/year changes, no-budget/over-budget states and
  monthly graph growth. Missing recurrence is stated; no pretend scheduled entries.
- Reset is sandbox-only and restores time/fixtures; no personal data or debug
  simulation metadata leaks into normal user backups.

### Tests
Deterministic fixture/replay and partial-step restart tests; real-Room counts and
graph/source agreement; device step/play/pause/reset journey. Run G2.

### Working checkpoint
Developer can reproduce a historical chart with known exact totals and observe it
update in actual Recibo/Resumen; ordinary manual workflows still work.

### Context and starting points
A seeded graph is useful only when it exercises the same writes/calculations as
manual use. Inputs are real expense/income/summary ports and the isolated session
from SELLO-024. Read [G01](../DECISION_INPUTS.md#exact-baseline-examples), architecture
§9 and recovery-case checklist; no recurrence port exists in MVP.

### Implementation plan
1. Define a small versioned synthetic scenario format with seed, zone, starting
   date and explicit advance/create-category/set-limit/expense/income steps.
2. Implement a bounded runner calling actual domain commands. Persist pending step's
   operation ID before writing; checkpoint after resolving its receipt. Stop on unknown outcome.
3. Add play/pause/single-step/reset/replay controls and inspectable step/receipt/revision
   results. Pause/cancel must leave resumable pending work rather than create a fresh ID.
4. Implement G01 and month-boundary/over-budget scenarios, injecting dates so entry
   validation remains real; observe actual Recibo/Resumen charts, not a separate debug graph.
5. Reset only the sandbox baseline and resume after process death through original
   step IDs. Keep scenario metadata synthetic/debug-only and outside portable backups.

### Concrete cases and pitfalls
- G01 yields three expenses, one income, cumulative 10,000/25,000/30,000 and final
  remaining 70,000. Resume after step 2 cannot add a fourth expense.
- A financial commit followed by checkpoint failure recovers its receipt; it does
  not rerun the amount under a new operation. Random seed alone is insufficient if IDs vary on resume.
- Jumping dates does not advance WorkManager or generate pretend recurring values.
  Future recurrence uses its actual product use case when implemented later.

### Verification recipe
Create/run `*ScenarioReplayTest` and real-Room ScenarioGraphGrowthTest; G2. Compare
fresh replay versus interrupted/resumed rows, receipts and exact chart labels; demonstrate
live pause/step/restart/reset on debug app with recorded scenario/version/seed.

## SELLO-026 — Add truthful failure, receipt and recovery diagnostics

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-019, SELLO-025, SELLO-004
- **Gate:** G2

### Outcome
Debug exploration can reproduce unsafe-looking edges without creating another financial implementation.

### Deliverables
Debug-only scripted adapter faults for read/command boundary and caller interruption,
receipt/status inspector and redacted synthetic diagnostic export. Use narrowly
scoped composition decorators; keep actual Room commit/rollback tests separate.

### Acceptance criteria
- Simulated read failure shows ErrorSlip/last-good data, not zero; interruption after
  commit recovers original receipt and never invites a blind new operation.
- Scenarios include duplicate submit, stale edit, expiry/undo conflict and overflow
  rejection. Each fault has an explicit scope/trigger and can be disabled/reset.
- Fake-port failure is never described as proof of SQL rollback. Neither injection
  classes nor diagnostic menus/data ship in release, even with code shrinking disabled.
- Export is synthetic/redacted and explicit; no raw user fields or secrets in logs.

### Tests
Debug decorator contract/recovery tests, real-Room corroborating transaction tests,
device fault/reset/receipt checks and release artifact exclusion assertions; G2.

### Working checkpoint
Normal sandbox behavior resumes after clearing a fault; real app workflows do not
silently inherit failure injection or modified arithmetic.

### Context and starting points
Investigating “did it save?” requires truthful receipts, not debug wrappers that
invent financial outcomes. Use command/undo ports and runner from SELLO-019/025.
Read architecture §§5/9 and
[failure checklist](../EXECUTION_GUIDE.md#contract-and-failure-checklist).

### Implementation plan
1. Add narrowly scoped debug composition decorators for next-read failure, caller
   interruption and post-commit delivery failure; define trigger scope/count/reset explicitly.
2. Provide read-only operation/receipt/revision inspector showing original status and
   simulated fault source. Never rewrite a committed receipt to make the UI look failed.
3. Add duplicate, stale editor, overflow and undo-conflict scripts using actual
   commands; transaction rollback evidence still comes from real-Room constraint tests.
4. Pause scenario progress on unresolved operations and offer recovery by saved ID;
   clear/reset faults predictably and prevent them leaking across sessions.
5. Export only synthetic/redacted diagnostics through a debug-only narrow file adapter;
   document payload/cleanup and verify classes/resources/dependencies absent in release.

### Concrete cases and pitfalls
- Failure delivered after a real 10,000 commit must still inspect one committed
  expense; recovery returns its receipt instead of creating another row.
- Next-read error with last-good 30,000 shows explicit stale/error, not a zero graph.
- An adapter exception before calling Room does not prove Room rollback; inspector
  must distinguish simulated boundary failures from actual transaction outcomes.

### Verification recipe
Create/run `*DebugFaultDecoratorTest`, ReceiptRecoveryDiagnosticJourneyTest and matching
real-Room rollback/recovery tests; G2. Verify triggering once, clearing, switching
sessions and exporting redacted output; inspect customer artifact for absence again.

## SELLO-027 — Establish the growing temporal regression scenario suite

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-020, SELLO-021, SELLO-023, SELLO-026, SELLO-004
- **Gate:** G2

### Outcome
Turn live explorations into repeatable acceptance evidence and an extension contract for future features.

### Deliverables
Scenario inventory with IDs, seed/actions/expected totals, automation hooks and
documented replay instructions. Cover month-end/leap year, archived category,
past-budget retention, income allocation, graph growth, restart and monotonic
undo under financial time stepping. Extend it later in feature tickets.

### Acceptance criteria
- Automated tests and live runs use equivalent fixtures/contracts; expected values
  are independently reviewed examples, not copied from the implementation's output.
- Scenario resume is deterministic and diagnostic reports identify failures/revisions.
  Live testing supplements unit/device tests rather than replacing them.
- Future recurrence contract explicitly requires production run-due/catch-up use case,
  created/skipped/failed/pending results and separate WorkManager tests. No scheduling
  library/engine enters MVP merely to display a debug preview.

### Tests
Execute the complete MVP scenario corpus, compare outputs after fresh versus
resumed runs, and inspect release exclusion again; G2.

### Working checkpoint
Another contributor can reproduce each recorded temporal issue from its scenario ID
without personal data or undocumented local settings.

### Context and starting points
Live exploration becomes durable value only when another actor can reproduce its
inputs and expected outcomes. Inputs are the actual runner/fault contracts and all
MVP financial workflows. Read the example corpus, architecture §9 and guide's
[delivery record](../EXECUTION_GUIDE.md#delivery-record).

### Implementation plan
1. Create `docs/testing/sandbox-scenarios.md` listing stable ID/version/seed, setup,
   action sequence, expected exact totals/state, replay command/UI route and test ownership.
2. Implement deterministic month/year/leap-day, missing-run-date jumps, archived
   category, historical-limit, income allocation, graph-growth and recovery scenarios.
3. Reuse fixtures/actions in automated tests without deriving expected amounts from
   the production policy; independently verify the numbers and checkpoint semantics.
4. Document supported scenario extension process and future recurrence obligation:
   actual run-due/catch-up port, dedup/checkpoints/results and separate scheduler tests.
5. Execute fresh/resumed corpus, record artifacts and inspect release isolation.
   Do not add unused recurrence/work/rate dependencies to anticipate the next increment.

### Concrete cases and pitfalls
- Same ID/seed after reset produces identical financial contents; resume mid-run
  preserves logical step identities and creates no duplicates.
- Advancing financial time while an undo offer is open cannot change its monotonic
  six-second duration. Replaying across month changes retains historical limits.
- “Missed dates” in MVP is timeline/history testing, not proof recurrence works;
  document the distinction so future actors do not mark scheduling acceptance complete.

### Verification recipe
Create/run the named TemporalScenarioCorpusTest and scenario host tests, then G2.
Follow published replay instructions from a fresh debug sandbox and from saved
checkpoint. Evidence lists every scenario result and expected/actual values, not one green summary.
