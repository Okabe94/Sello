# SELLO-E06 — Debug live-testing sandbox

- **Status:** Done
- **Goal:** Explore time-sensitive financial behavior safely through the actual app pipeline.
- **Exit:** Isolated controllable dates, reproducible histories/graphs, inspectable receipts and restart/failure scenarios; no tooling in customer release.

## SELLO-024 — Create isolated sandbox sessions and financial time controls

- **Type:** Task
- **Priority:** P0
- **Status:** Done
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

From SELLO-019 (2026-10-12): expense edit, delete and undo shipped before the sandbox
existed, so they have no sandbox regression inputs. Add them here: U01 to U05 in
`UndoCompensationContractTest` are written against the distinct clocks and can be
run as scenarios, including "advance the financial day a year, undo still ends at
six real seconds".

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

### Delivery evidence

- **Revision:** tested snapshot is the `sello-024-sandbox-time-controls` working tree on
  `main` at `03bb458`, uncommitted; it is identified exactly by the retained input
  fingerprint below. The commit and pull request follow this evidence; no claim is
  made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-22: a switch restarts the process; one
  sandbox beside the normal debug profile; a separate debug-only launcher activity
  with a badge; play at one day every 2 real seconds, stopping after 31 days or at a
  chosen date. Deviation: the badge sits in the status-bar strip so it covers no
  control, and Android gives touches there to the system, so it cannot be tapped
  with a finger (TalkBack can activate it); the tools open from their own launcher.
- **Requirement mapping:** `VariantComposition` (main) is the only hook; the release
  source set returns `ProductionComposition`, the debug one `DebugComposition`.
  `StorageNames` selects `sello.db`/`sello-preferences` or `sandbox.db`/
  `sandbox-preferences`; the session (active, date, changed, pending wipe) is kept in
  `sandbox-session` preferences. Every financial command runs through a `WriteFence`;
  enter, leave and reset close it (running writes finish, new ones never start),
  save the choice and restart the process, so no screen or action crosses profiles.
  `SandboxTime` drives `ControlledFinancialClock`: +day, +month, jump, bounded play
  and pause; dates persist, and once a change is saved an earlier date is refused
  until a confirmed reset, which erases only sandbox files at the next start
  (`eraseFinancialStorage`, debug-only, refuses the normal names). Audit and
  monotonic clocks stay real. `ToolsActivity` (debug manifest, "Sello Herramientas")
  and `SandboxBadges` show the session. `:app:assembleRelease` is finalized by
  `verifyReleaseHasNoDeveloperTools`, which reads the APK's dex, manifest and
  resources. The U01–U05 undo cases already run against distinct clocks in
  `UndoCompensationContractTest`; the sandbox adds "a year later, undo still has its
  six real seconds" through its own controls. Seeded histories are SELLO-025.
- **Red / Green:** mutations each turned a test red: a fence that does not wait
  (`WriteFenceTest`), no rewind guard (`SandboxTimeTest`), a command run outside the
  fence (`FencedCommandsTest`). The release check failed on the debug APK (devtools
  classes, `ToolsActivity` in the manifest, sandbox strings) and passes on release.
  The badge test first failed because the badge had no click semantics and no height
  without a status bar; both are fixed. `ExportedSchemaTest` refused a
  `deleteDatabase` in `:data`, so the erase moved to the debug source set.
- **Gate results:** `./scripts/verify-ticket SELLO-024 --retain` passed all six checks.
  Host: 551 tests (18 new); device: 606 tests (12 new sandbox tests in
  `SandboxSessionOnDeviceTest` and `TimeControlsJourneyTest`); zero failures, errors or
  skips. Lint had zero errors and 33 warnings: app dependency-update notices and
  unused template colours, two in untouched prototype files; none in changed code.
- **Quality run:** run 20261022T051135Z-f25b9709; SELLO-024 G2 passed; HEAD 03bb458, inputs sha256 01e14a11b4e0; report docs/planning/mvp/quality-reports/SELLO-024.json sha256 f414aa0b8e4f414d04ecdbb330074bdcc05b8320297ac626b6705e071baffeca
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. On the same AVD the installed debug build
  entered the sandbox in a new process with the badge, stepped 22 Oct to 23 Nov while
  the device date stayed 22 Oct and Recibo showed Noviembre, kept the date across a
  force-stop, left to the normal profile on Octubre without a badge, and a confirmed
  reset reopened the sandbox on today's date
  ([screenshots](../../../testing/evidence/SELLO-024)). No physical device or personal
  data was used; no hosted CI run is claimed.
- **Review:** executor self-review covered release exclusion, profile isolation, the
  fence, forward-only dates and real-time undo. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

## SELLO-025 — Implement seeded histories, replay and live graph-growth scenarios

- **Type:** Story
- **Priority:** P0
- **Status:** Done
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
from SELLO-024. Read [G01](../../../testing/mvp-financial-examples.md#history-income-and-graph-reconciliation--d04d06), architecture
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

### Delivery evidence

- **Revision:** tested snapshot is the `sello-025-seeded-scenarios` working tree on `main`
  at `ec9aa8d`, uncommitted; it is identified exactly by the retained input fingerprint
  below. The commit and pull request follow this evidence; no claim is made that the
  gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-22, typed Kotlin scenarios versioned by
  id and version, loading that always resets the sandbox to the scenario's start date
  and zone, and one step every 2 real seconds while playing.
- **Requirement mapping:** `Scenario` (id, version, seed, zone, start, steps) with
  `AdvanceTo`, `Category`, `Expense` and `Income` steps; `Scenarios` offers G01
  (7 steps), Fin de mes y de año (12: over-limit and no-limit categories, 31 Oct →
  1 Nov, 31 Dec → 1 Jan) and Seis meses (67, amounts from seed 20261001).
  `ScenarioRunner` calls the fenced `CategoryCommands`, `ExpenseCommands` and
  `IncomeCommands`; entries are dated on the sandbox's day after each date step, so
  future-date validation stays real. Each step's operation is a name-based UUID of
  id, version, seed and index, so a replay or resume returns the original receipt;
  category IDs are recovered from receipts. The sandbox session keeps the scenario and
  the first unconfirmed step, advanced only once a result is known; a rejection or
  unknown outcome stops and is shown. The tools show progress and each step's action,
  result, revision and operation; play is bounded by the scenario and stops on any
  non-commit. Loading uses a new `VariantComposition.newDataZone` hook (release:
  none). No recurrence exists, so no step pretends to be scheduled. Progress lives in
  debug preferences, never in Room, so financial backups cannot carry it.
- **Red / Green:** random operation IDs turned five tests red, including the
  interrupted-resume and unknown-outcome cases; advancing past a failed step turned two
  red. Two placeholder expectations were corrected against the fixture (the pinned
  six-month total 3.004.000 and the G01 expense dates). On real Room, G01 gives spent
  30.000, cumulative 10.000/25.000/30.000, remaining 70.000, income 200.000, net
  170.000 and unassigned 100.000 (financial examples G01); a run interrupted after a
  commit and resumed matches a fresh run's receipts and rows exactly, with three
  expenses; six months' history matches the scenario's own monthly sums.
- **Gate results:** `./scripts/verify-ticket SELLO-025 --retain` passed all six checks.
  Host: 561 tests (10 new); device: 610 tests (4 new in `ScenarioReplayTest`); zero
  failures, errors or skips. Lint had zero errors and 33 warnings, the same set as
  SELLO-024 (dependency-update notices, unused template colours, two prototype files).
- **Quality run:** run 20261022T060649Z-0bb77358; SELLO-025 G2 passed; HEAD ec9aa8d, inputs sha256 785c9f75d2bd; report docs/planning/mvp/quality-reports/SELLO-025.json sha256 ad349189a3d050da1be57fc750e178c5c51fe0c1791dac2c3e3240e05192b112
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. On the same AVD the debug build loaded G01
  from the tools, played it to "Terminado: 7 pasos" with each receipt listed, Recibo
  showed 70.000 left and Resumen 200.000/170.000/100.000; Seis meses grew the
  month-by-month columns ([screenshots](../../../testing/evidence/SELLO-025)). The
  sandbox was left afterwards. No physical device or personal data was used; no hosted
  CI run is claimed. Unrelated finding, not fixed: Resumen prints "Movimientos anotados
  hasta 2026-10-03" as an ISO date.
- **Review:** executor self-review covered identity on resume, real write paths, date
  injection, bounded play and backup exclusion. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

## SELLO-026 — Add truthful failure, receipt and recovery diagnostics

- **Type:** Story
- **Priority:** P1
- **Status:** Done
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

### Delivery evidence

- **Revision:** tested snapshot is the `sello-026-fault-diagnostics` working tree on
  `main` at `1fcca96`, uncommitted; it is identified exactly by the retained input
  fingerprint below. The commit and pull request follow this evidence; no claim is
  made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-22, export through the Android share sheet
  as plain text, faults only in the sandbox, and including the interruption-after-save
  fault.
- **Requirement mapping:** `DebugFaults` arms, in memory only, a failed read, a lost
  answer after a real commit, an interruption after a real commit and a failure before
  storage, each counted and cleared by "Quitar fallos" or a restart. `Faulty*` command
  wrappers sit inside the write fence and `FaultyMonthlySnapshots`/
  `FaultyResumenSnapshots` report a failed read with the last good snapshot, all bound
  by `DebugComposition` only in the sandbox. None changes a figure or a receipt.
  `OperationLog` records what each caller was told and `OperationInspector` reads what
  storage holds through the commands' `find`. The runner gains `Expect` (saved, already
  saved, a named rejection, invalid input), stops on an interruption with the same
  operation to retry, and the Bordes scenario scripts a double submit, a stale edit
  (`VersionConflict`), an undo replaced by a later deletion (`UndoExpired`) and an
  amount past the maximum (`AboveMaximum`). "Exportar diagnóstico" shares the session,
  armed faults and operations (IDs, kinds, outcomes, revisions) with no name, note or
  amount. The release check also looks for the fault and diagnostic names.
- **Red / Green:** a lost answer that skipped the write, a read failure that dropped
  the last good snapshot, and faults that never counted down each turned
  `DebugFaultDecoratorTest` red. The release check finds the new names in the debug
  APK and none in release. Rollback evidence remains the real-Room command contract
  tests in `:data`; the "before storage" fault is labelled as proving nothing about it.
- **Gate results:** `./scripts/verify-ticket SELLO-026 --retain` passed all six checks.
  Host: 567 tests (6 new); device: 616 tests (6 new in
  `ReceiptRecoveryDiagnosticJourneyTest`); zero failures, errors or skips. Lint had zero
  errors and the same 33 warnings as SELLO-025, none in changed code.
- **Quality run:** run 20261022T134026Z-ded8d85c; SELLO-026 G2 passed; HEAD 1fcca96, inputs sha256 f2c8afa26aa7; report docs/planning/mvp/quality-reports/SELLO-026.json sha256 30d7e1386d720eb4f26250eae244e5f320b31819e0d8b4d496395ad8695189ca
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. On the same AVD, with "Respuesta perdida"
  armed, a 5.000 expense entered through the real entry screen was recovered by its
  receipt and announced once; Recibo went from 42.000 to 37.000; the inspector showed
  "unknown (simulated)" beside "saved, rev 9"; export opened the share sheet
  ([screenshots](../../../testing/evidence/SELLO-026)). The sandbox was left afterwards.
  No physical device or personal data was used; no hosted CI run is claimed.
- **Review:** executor self-review covered no invented outcomes, sandbox-only faults,
  clearing, redaction and release exclusion. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

## SELLO-027 — Establish the growing temporal regression scenario suite

- **Type:** Task
- **Priority:** P1
- **Status:** Done
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

### Delivery evidence

- **Revision:** tested snapshot is the `sello-027-temporal-corpus` working tree on `main`
  at `def067f`, uncommitted; it is identified exactly by the retained input fingerprint
  below. The commit and pull request follow this evidence; no claim is made that the
  gate tested a clean commit. With this ticket the E06 epic is proposed Done.
- **Decisions:** the owner chose, on 2026-10-22, that a finished live run compares its
  expected figures with the real snapshots and shows each one, and that the leap-year
  scenario is included with hand-derived figures labelled as not approved.
- **Requirement mapping:** `docs/testing/sandbox-scenarios.md` lists nine scenarios
  (ID, version, seed, steps, start and zone, expected figures with sources, replay from
  fresh and from a checkpoint, owning test), the extension process and the recurrence
  obligation (production run-due/catch-up use case with created/skipped/failed/pending
  results and separate WorkManager tests; no scheduling dependency added). New steps
  `SetDefaultLimit`, `SetMonthLimit`, `Archive`, backdated `Expense` and `Verify`
  (reads the real `MonthlySnapshots`/`ResumenSnapshots`). New scenarios: leap year
  (by hand), skipped months (H03/H04), past limits (H05, H01, H02), archived category
  (AR01/AR02) and income allocation (I01/I02); G01 and month/year ends gained checks
  (versions 2). Restart and monotonic undo under time stepping stay covered by
  `TimeControlsJourneyTest`.
- **Red / Green:** `TemporalScenarioCorpusTest` passed all 37 figures across nine
  scenarios, fresh and resumed, with identical receipts, expenses, incomes, categories
  and limits; setting I02's expected unassigned value to 0 (a clamped result) turned
  it red with `✗ sin asignar 2026-11: esperado 0, real -50.000 (I02)`. Host tests check
  every scenario is in the corpus document, ends with a sourced check and scripts the
  right category versions.
- **Gate results:** `./scripts/verify-ticket SELLO-027 --retain` passed all six checks.
  Host: 570 tests (3 new); device: 617 tests (1 new); zero failures, errors or skips.
  Lint had zero errors and the same 33 warnings as before, none in changed code; the
  release check still finds no developer tools in the release APK.
- **Quality run:** run 20261022T142158Z-e862852d; SELLO-027 G2 passed; HEAD def067f, inputs sha256 d30317bed5f7; report docs/planning/mvp/quality-reports/SELLO-027.json sha256 71cbce24fa3461093ca295d7c3331ecc26bbdf644e09e9f7d6aceee9be63e3f9
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. Every check line of the corpus run is in
  [corpus-run.txt](../../../testing/evidence/SELLO-027/corpus-run.txt); a live G01 run in
  the tools showed "4 de 4 cifras coinciden"
  ([screenshot](../../../testing/evidence/SELLO-027/g01-live.png)). No physical device or
  personal data was used; no hosted CI run is claimed.
- **Review:** executor self-review covered independent expected values, fresh-versus-
  resumed equality, the recurrence boundary and release exclusion. Done is proposed by
  this ticket's pull request, together with the E06 epic; its merge is the designated
  owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass
  is claimed.
