# SELLO-E15 — Fixes found during release work

- **Status:** Backlog
- **Increment:** Post-MVP
- **Goal:** Fix the defects found while measuring and accepting the MVP, each with a test.
- **Exit:** Every ticket below is Done: counts and dates read as the rest of the app writes them, a failed backup says why, and the sandbox scenario tests pass reliably.

**Owner direction (2026-10-23):** the defects found during SELLO-035 and SELLO-036 are
logged here rather than fixed in them. None changes money, stored data or the release itself.

## SELLO-075 — Group thousands in every count the app shows

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
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

## SELLO-076 — Show Resumen's "recorded until" date in words

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
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

## SELLO-077 — Make the sandbox scenario replay tests reliable

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
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

## SELLO-078 — Say what failed when a backup does not pass its own check

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
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
