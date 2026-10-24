# SELLO-025 — Implement seeded histories, replay and live graph-growth scenarios

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
- **Quality run:** run 20261022T060649Z-0bb77358; SELLO-025 G2 passed; HEAD ec9aa8d, inputs sha256 785c9f75d2bd; report docs/tickets/E06-live-testing/SELLO-025/quality-report.json sha256 ad349189a3d050da1be57fc750e178c5c51fe0c1791dac2c3e3240e05192b112
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. On the same AVD the debug build loaded G01
  from the tools, played it to "Terminado: 7 pasos" with each receipt listed, Recibo
  showed 70.000 left and Resumen 200.000/170.000/100.000; Seis meses grew the
  month-by-month columns ([screenshots](artifacts)). The
  sandbox was left afterwards. No physical device or personal data was used; no hosted
  CI run is claimed. Unrelated finding, not fixed: Resumen prints "Movimientos anotados
  hasta 2026-10-03" as an ISO date.
- **Review:** executor self-review covered identity on resume, real write paths, date
  injection, bounded play and backup exclusion. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

