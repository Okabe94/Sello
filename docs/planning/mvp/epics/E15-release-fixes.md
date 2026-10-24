# SELLO-E15 — Fixes found during release work

- **Status:** In Progress
- **Increment:** Post-MVP
- **Goal:** Fix the defects found while measuring and accepting the MVP, each with a test.
- **Exit:** Every ticket below is Done: counts and dates read as the rest of the app writes them, a failed backup says why, and the sandbox scenario tests pass reliably.

**Owner direction (2026-10-23):** the defects found during SELLO-035 and SELLO-036 are
logged here rather than fixed in them. None changes money, stored data or the release itself.

## SELLO-075 — Group thousands in every count the app shows

- **Type:** Task
- **Priority:** P2
- **Status:** In Progress
- **Depends on:** SELLO-004
- **Gate:** G2

### Outcome
A count of 1,000 or more reads with a thousands separator, as amounts already do:
"10.000 gastos", not "10000 gastos".

### Deliverables
Counts formatted for es-CO wherever the app shows one, with tests at and above 1,000.

### Acceptance criteria
- The restore preview's counts, the restore result ("Se restauró el respaldo: 20
  categorías, 10.000 gastos y 2.000 ingresos"), the erase confirmation, the start-up
  failure restore and the category counts ("Ver los 1.000", "1.000 en 23 días") group
  thousands.
- What a screen reader says matches what is shown.
- Counts under 1,000 are unchanged.

### Tests
Screen or render-model tests with counts of 999, 1,000 and 10,000; G2.

### Working checkpoint
Restoring the SELLO-035 large history shows "10.000 gastos" in the preview and the result.

### Context and starting points
Found in SELLO-035 ([performance profile](../../../testing/performance-profile.md)). Counts
reach the strings through `%d` plurals and preformatted `%1$s` arguments such as
`restore_done_body`, `reset_counts_body` and `startup_restore_counts`; amounts already
group thousands through the presentation money formatter.

### Implementation plan
1. List every count the app shows, then format each through one es-CO count formatter
   in `presentation`, keeping plural selection on the number.
2. Add the tests above.

### Concrete cases and pitfalls
- Choose the plural by the number, not by the formatted text.
- Do not change backup files or stored values; only what is shown.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-075 --retain`.

### Execution progress

- **Counts listed, 2026-10-23:** expenses in category detail ("Ver los …", "… en N
  días", "N gastos", "Se muestran N de M"), the archive notice, the restore preview
  table and its spoken cells, the restore result, the erase confirmation, the start-up
  failure restore and the last backup's counts. Days, weeks, months, years and the
  name and note counters stay as they are: none reaches 1,000, and a year must not be
  grouped.
- `presentation/count/countText` groups thousands with a point whatever the phone's
  language; the plurals take it as text (`%s`) and are still chosen by the number.

## SELLO-076 — Show Resumen's "recorded until" date in words

- **Type:** Task
- **Priority:** P2
- **Status:** Done
- **Depends on:** SELLO-004
- **Gate:** G2

### Outcome
Resumen's income slip says "Movimientos anotados hasta el 23 oct 2026" (or the app's
date form), not "Movimientos anotados hasta 2026-10-23".

### Deliverables
The date formatted like the app's other dates, in the render model, with a test.

### Acceptance criteria
- The line uses the app's es-CO date form, as the entry and category detail do.
- The spoken text matches what is shown.
- The date itself, the financial day it comes from, is unchanged.

### Tests
`ResumenRenderModelTest` or `ResumenScreenTest` asserting the formatted date; G2.

### Working checkpoint
Resumen shows the date in words for the current month and a past month.

### Context and starting points
Found in SELLO-035. `ResumenRenderModel` sets `asOf = budget.asOf.toString()`, and
`ResumenScreen` places it in `resumen_actual_through`.

### Implementation plan
1. Format `asOf` with the presentation date formatter in the render model.
2. Add the test.

### Concrete cases and pitfalls
- Format in presentation, not in the screen; keep the financial day from the snapshot.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-076 --retain`.

### Execution progress

- **Overlap, 2026-10-23:** SELLO-080–082 (#95) already changed the line to "hasta 3 de
  octubre", the form of Resumen's new weekday line. The owner chose to keep that long
  form, add "el" and show the year only for a day from another year: "Movimientos
  anotados hasta el 3 de octubre", "… hasta el 31 de diciembre de 2026" seen in 2027.
  The "could not refresh" line uses the same form.
- `ResumenRenderModel` formats `asOf` against the current financial month, which
  `ResumenViewModel` takes from the month session. The financial day itself is
  unchanged: a past month still says up to which day it was read.

### Delivery evidence

- **Revision:** the gate tested commit `834f1e6` on `sello-076-resumen-date`, on `main`
  at `20482f7`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards.
- **Requirement mapping:** the line and the "could not refresh" line use Resumen's long
  date form with "el", as the owner chose; the year only for a day from another year;
  formatted in the render model; the financial day is the snapshot's own. Screen readers
  read the same text node that is shown.
- **Red / Green:** `theRecordedUntilDayShowsItsYearOnlyWhenItIsFromAnotherYear` failed
  first (`expected:<3 de octubre[ de 2026]> but was:<3 de octubre[]>`) and passes; the
  screen tests assert "Movimientos anotados hasta el 3 de octubre" and the stale line's
  "hasta el 3 de octubre.".
- **Working checkpoint:** in the sandbox's month and year-end scenario on 1 January
  2027, January and the past December both read "Movimientos anotados hasta el 1 de
  enero" ([January](../../../testing/evidence/SELLO-076/resumen-enero.png),
  [December](../../../testing/evidence/SELLO-076/resumen-diciembre-pasado.png)).
- **Tests:** host `ResumenRenderModelTest` (+1); device `ResumenScreenTest` (+1, one
  assertion added).
- **Gate results:** `./scripts/verify-ticket SELLO-076 --retain` passed all seven G2
  checks, full coverage. Host: 745 tests; device: 754 tests (data 236, design-system
  136, app 361, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`. Two earlier runs are not evidence: one on the first
  version, before #95 merged, and one that met a sandbox session left active on the
  emulator by the screenshots (72 failures); the app's data was cleared before this one.
- **Quality run:** run 20261023T235512Z-730547cb; SELLO-076 G2 passed; HEAD 834f1e6, inputs sha256 7888ed5c5f90; report docs/planning/mvp/quality-reports/SELLO-076.json sha256 9f0cec888ae9d4160a47de3de6f51b04efbec59d3ab53e92cf3455359fa30726
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging.

## SELLO-077 — Make the sandbox scenario replay tests reliable

- **Type:** Task
- **Priority:** P1
- **Status:** Done
- **Depends on:** SELLO-004
- **Gate:** G2

### Outcome
`TemporalScenarioCorpusTest` and `ScenarioReplayTest` pass on every run, so a hosted
check fails only for a real defect.

### Deliverables
The cause of the intermittent failures found and fixed, in the test or in the sandbox
runner, with evidence that it no longer recurs.

### Acceptance criteria
- The cause is identified and stated; the fix addresses it rather than retrying.
- Each test passes 20 runs in a row on the API-30 emulator.
- No financial behaviour changes; the sandbox stays absent from release builds.

### Tests
The two tests repeated; a regression test for the cause if it lies in the runner; G2.

### Working checkpoint
Twenty consecutive passes of both tests, recorded.

### Context and starting points
Seen twice in hosted CI on 2026-10-23, each passing on a full rerun:
`ScenarioReplayTest.aRunInterruptedAfterACommitAndResumedMatchesAFreshRunExactly`
(PR #86) expected "Ya estaba guardado" and got "Guardado";
`TemporalScenarioCorpusTest.everyScenarioMeetsItsFiguresAndResumesToTheSameHistory`
(PR #91) expected the first check's figures and got "Comprobado antes". In both, a
resumed scenario reads as if it had or had not already run. A likely cause is the
runner from before the simulated restart still answering; see `ScenarioRunner`.

### Implementation plan
1. Reproduce by repeating each test; capture the runner's state at the failure.
2. Fix the cause; repeat 20 times.

### Concrete cases and pitfalls
- Do not add sleeps or retries; the rules forbid arbitrary waits.
- A full workflow rerun, not a failed-job rerun, is what currently passes CI.

### Verification recipe
Repeated focused runs, then `./scripts/verify-ticket SELLO-077 --retain`.

### Execution progress

- **Cause, 2026-10-23:** not the runner from before the restart. A new process's
  `ScenarioRunner.recover()` runs in the background and, once it held the step lock,
  replaced the whole log with the steps confirmed so far. A step taken before it ran
  was then relisted from its receipt: "Guardado" instead of "Ya estaba guardado", and
  a check as "Comprobado antes" instead of its figures, exactly the two CI failures.
  Which came first depended on thread timing. The same could happen in the debug tools
  when Paso is tapped right after a restart; no record or figure was ever affected.
- **Fix:** `recover()` lists only the steps confirmed by earlier processes (the stored
  progress the runner was built on) and puts them ahead of the steps this process has
  already logged, whose results it keeps; running it twice lists them once. No test
  waits, retries or sleeps were added.

### Delivery evidence

- **Revision:** the gate tested commit `a957cd7` on `sello-077-flaky-scenarios`, on
  `main` at `f5864bf`; the only uncommitted file was the retained report. The status
  and this section are added to that commit afterwards.
- **Requirement mapping:** cause stated under Execution progress; the fix is in
  `ScenarioRunner.recover()`, with no retry, wait or sleep; the sandbox and runner stay
  debug-only and no command, record or figure changed.
- **Red / Green:** host `aStepTakenBeforeTheNewProcessHasListedItsHistoryKeepsItsOwnResult`
  fixes the order (step first, then the relisting) and failed first with CI's message,
  `expected:<[Ya estaba g]uardado> but was:<[G]uardado>`; it passes with the fix.
- **Working checkpoint:** `ScenarioReplayTest` and `TemporalScenarioCorpusTest` (8 tests)
  ran 20 times in a row with no failure on the standard 320 × 640 API-30 AVD, and 20
  more on an API-30 AVD with a Pixel 6a profile.
- **Tests:** host `ScenarioRunnerTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-077 --retain` passed all seven G2
  checks, full coverage. Host: 695 tests; device: 729 tests (data 236, design-system
  133, app 339, catalog 21); zero failures, errors or skips. Lint had zero errors and
  the same 33 warnings as before. A first run on the Pixel 6a AVD failed five layout
  tests that expect a scrolling or shorter screen (see Review); it was not the
  required device and is not evidence.
- **Quality run:** run 20261023T220913Z-55181658; SELLO-077 G2 passed; HEAD a957cd7, inputs sha256 7286a313035e; report docs/planning/mvp/quality-reports/SELLO-077.json sha256 7c05f53991cbbe69d5d6a6ac3c2c6b8ca6d3db07d8bf34811d060fd20d644008
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30), first
  with a Pixel 6a profile, then recreated at the setup guide's default 320 × 640.
- **Review:** executor self-review; owner review by merging. Not fixed here: on a tall
  screen, `ExpenseEntryScreenTest` (two tests), `IncomeJourneyTest`,
  `EntryCatalogInteractionTest` and `ReciboScreenTest.ajustarPresupuesto…` fail because
  they assume the content scrolls; they pass on the gate's AVD.

## SELLO-078 — Say what failed when a backup does not pass its own check

- **Type:** Task
- **Priority:** P2
- **Status:** Done
- **Depends on:** SELLO-004
- **Gate:** G2

### Outcome
A backup the app refuses to hand out because it fails its own reading check is reported
as that, not as "Tus datos superan lo que cabe en un respaldo".

### Deliverables
A distinct message for a self-check failure, separate from a history that is really too
large, with a test.

### Acceptance criteria
- A history over the backup limits still reports it is too large.
- Any other self-check rejection reports that the backup could not be made correctly,
  that nothing in Sello changed and that it is a fault to report.
- Nothing about the rejected data is shown beyond what the current messages show.

### Tests
`BackupViewModelTest` or a screen test for both failures; G2.

### Working checkpoint
Both failures show their own message.

### Context and starting points
Found in SELLO-036: on Android 16 every backup failed its own check (fixed there) and
was reported as too large, which hid the cause. `BackupSession` maps
`BackupPreparationFailure.Unrestorable` to `BackupFailure.TooLarge`.

### Implementation plan
1. Tell a too-large rejection apart from other self-check rejections.
2. Add the message and tests.

### Concrete cases and pitfalls
- Keep the "an empty file may be left" warning for both.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-078 --retain`.

### Execution progress

- **Owner decision, 2026-10-23:** a short message, at most two lines: "Falla de Sello al
  armar el respaldo; tus datos no cambiaron. No uses el archivo vacío que pudo quedar."
- `BackupPreparationFailure` gains `TooLarge` (too many bytes, records or default-limit
  entries); `Unrestorable` is now every other refusal of the app's own file, shown as
  `BackupFailure.SelfCheck` with the message above. A real size refusal keeps its
  message.

### Delivery evidence

- **Revision:** the gate tested commit `7bc0fa5` on `sello-078-backup-self-check`, on
  `main` at `cdcd1d4`; the only uncommitted file was the retained report. The status
  and this section are added to that commit afterwards.
- **Requirement mapping:** a history over the format's limits (`TooLarge`,
  `TooManyRecords`, `HistoryTooLong`) still reads "Tus datos superan lo que cabe en un
  respaldo"; every other refusal of the app's own file reads "Falla de Sello al armar
  el respaldo; tus datos no cambiaron. No uses el archivo vacío que pudo quedar." Both
  keep the empty-file warning and show nothing of the refused data.
- **Red / Green:** `BackupPreparationFailureTest.onlyAHistoryBeyondTheFormatsLimitsIsTooLarge`
  failed first (`expected:<TooLarge> but was:<Unrestorable>`) and
  `BackupViewModelTest.aFileThatFailsItsOwnCheckForAnotherReasonIsNotCalledTooLarge`
  failed with `expected:<SelfCheck> but was:<TooLarge>`; both pass with the mapping.
- **Tests:** host `BackupPreparationFailureTest` (new, 2) and `BackupViewModelTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-078 --retain` passed all seven G2
  checks, full coverage. Host: 748 tests; device: 754 tests (data 236, design-system
  136, app 361, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`.
- **Quality run:** run 20261024T010135Z-e9161a71; SELLO-078 G2 passed; HEAD 7bc0fa5, inputs sha256 521fe8e93d21; report docs/planning/mvp/quality-reports/SELLO-078.json sha256 31f6211075cf167aebaa8ba9d88422d9d542a6328e3ecb37390f266a2a305e50
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging. Not shown on a device: no
  stored history makes the app's own check refuse its file now that SELLO-036 fixed the
  clock, so the new message is covered by the view-model and mapping tests only.
