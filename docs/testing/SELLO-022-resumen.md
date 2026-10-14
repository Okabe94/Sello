# SELLO-022 Resumen verification

## Part 1: one consistent source of summary figures

`ResumenSnapshots` is the consumer-shaped read port. `FinancialStorage.resumenSnapshots`
requires the financial clock and calculation context; `RoomResumenSnapshots` reads
budgets, expense rows across up to twelve calendar months ending in the selection,
and selected-month income inside one Room transaction. It performs no writes.
`ResumenPolicy` derives the result off the caller's thread using exact domain money
and the existing monthly budget policy. Recibo's port and current UI are unchanged.

The result retains generation, revision, financial date and zone through its budget
snapshot. It contains recorded income minus expenses, income minus finite budget
limits, a verdict based on actual expenses, daily and cumulative spending, and monthly
spending with each month's own limit. Current finite-budget forecast data remains
explicitly an estimate; historical months carry no forecast. Zero finite budgets and
unlimited categories remain distinct. No goals/investment contribution is invented.

Empty histories are successful zero actuals with zero expense counts, not invented
averages. A failed read is typed; a failed refresh keeps its clearly identifiable
last-good snapshot. Cancellation propagates. Future-dated income and expenses are
excluded from actuals under the captured financial day, including time travel.

## Tests and independent examples

- `ResumenSnapshotTest`: I01 (150,000 income, 30,000 expenses, 100,000 limits →
  120,000 net flow, 70,000 budget remainder, 50,000 unassigned allocation), G01/G02
  corrected cumulative spending, actual versus projected verdict, signed values,
  unlimited/zero limits, cutoffs, leap-month history, large exact values and overflow.
- `ResumenSnapshotContractTest`: real command writes and Room reads, independent SQL
  counts/totals, historical limits, concurrent income/expense/budget commits, damaged
  income/history rows, first-read and refresh errors, closed storage, reopen, time
  travel, injected computation placement, cancellation and rows beyond the 500-row
  storage page. Two active observations are measured below.
- The closed-storage regression also exercises the existing income read port. Room
  may throw base Android `SQLException`; the shared read helper now returns typed
  unavailability for it. No command failure/unknown-commit handling changes.
- Room closure cancels its observation scope. That cancellation propagates rather
  than becoming a fake successful zero or being swallowed as a storage retry.

Use synthetic isolated databases/devices only. No financial fixtures from a phone or
owner history belong in test data or evidence.

## Observation decision before screens

The deterministic real-Room test counts calculation-dispatch invocations: two active
observers perform two initial calculations and two more after one income commit.
That is four complete read/calculation cycles, not a latency benchmark. Each Resumen
read has one transaction and one calculation; all slips and charts will consume the
same ViewModel result rather than start their own observations.

Do not introduce a cross-port Recibo/Resumen cache in this PR: these ports have
different shapes, and a shared cache would broaden consistency/lifecycle changes
beyond the measured duplication. SELLO-055 stays separate. In the screens PR use a
lifecycle-bound Resumen subscription and verify that tab switches stop unnecessary
work; revisit broader sharing only with evidence of simultaneously active consumers.

## Next PR and sandbox carry-forward

The second PR owns the full Resumen render model, screen, exact accessible chart
selection, category/income links, restored selection/navigation, four-theme and
large-font/adaptive coverage. No new screen is claimed in part 1.

For SELLO-024/025's future sandbox runner, replay G01 through real commands:
Oct 1 income 200,000; Oct 1 expense 10,000; Oct 2 expense 15,000; Oct 3 expense 5,000;
limit 100,000. Correct Oct 2 to 12,000. Cumulative spending becomes
10,000/22,000/27,000; net flow 173,000; budget remainder 73,000; allocation 100,000
unchanged. Financial time affects cutoff/projection, never audit time or monotonic
undo deadlines. The current repository has no sandbox session/script runner; this
recipe and command-backed contract tests extend the available infrastructure.

## Part 2: screen and navigation acceptance

`ResumenRoot` observes one `ResumenViewModel.state` with the destination lifecycle.
Every slip and chart uses that same rendered snapshot. The upstream subscription
ends five seconds after its last UI subscriber leaves, preserving a rotation grace
period; stopped replay is cleared so a different selected month cannot replay old
figures. No separate chart reads or cross-port cache was added.

Actual income minus expenses is the hero figure. Signed income minus finite limits
is secondary planning capacity, explicitly not an account balance. Actual standing
and current-month estimated projection have different labels. Daily cumulative,
monthly history and ranked category marks preserve original IDs and exact amounts;
geometry is never read back as money. Initial empty months are trimmed from the
history, not turned into invented comparisons or averages. Last-good reads remain
clearly stale and carry their captured date with retry.

Day/month selection is saved by selected month plus source ID, updates from the
latest snapshot after a correction, and expires after three seconds using coroutine
delay (virtual time in tests). A restored temporary selection gets a new bounded display window.
Category and income source routes lead to existing histories; category route IDs
and the shared month survive activity recreation. Category charts have matching
named source rows; horizontally scrollable marks retain at least 48 dp width.

The carried-over income-form preview adds a valid **new** draft to the snapshot's
exact actual income in `IncomePreviewPolicy`, without writing anything. It never
adds an already recorded correction, a rejected amount or future-dated draft.
It observes only while a valid, uncommitted new draft needs it. Overflow and read
failure are unavailable previews, never zero or successful financial writes. The
original income read still owns generation/loading/write eligibility: an optional
expense/summary read failure must not break the existing income journey. Preview
values must match the form month, generation and financial day; failed last-good
snapshots are not used as current previews.

Focused host selectors: `*Resumen*Test`, `*IncomeFormViewModelTest` and
`ResumenSnapshotTest`. Device selectors: `ResumenScreenTest`, `ResumenJourneyTest`,
the existing income journey and `ShellOnDeviceTest`. Screen tests execute all four
themes, 2× font size, signed/large exact amounts, source-ID selection, real links,
empty/error states and the existing chart primitives. Run the screen suite at
640×1280 pixels / 320 dpi, then at 1680×1800 / 320 dpi for the 840 dp two-column
layout. Restore the standard phone dimensions before the full G2 gate.

The actual entry → expense → income → summary → category history → recreation →
back → relaunch journey uses UI commands and independently checks stored SQL totals.
Activity recreation/relaunch does not claim eligible saved-task process death;
record that separately with Home → background `am kill` → Recents and distinct PIDs.
TalkBack manual coverage and screenshots must be stated separately from automated
semantics checks. Preview fixtures are debug-only; the catalog adds G01 cumulative
selection with its own synthetic exact values, not production storage dependencies.

## Verification

Run focused tests, then `./scripts/verify-ticket SELLO-022 --retain` on
an isolated API-30 emulator. Record the actual revision, report and results in the
canonical epic. Part 1 cannot mark SELLO-022 Done; final acceptance and fresh evidence
follow the screens PR.

## Executed final screen check — 2026-10-13

The focused screen selectors first ran 23 tests without failures or skips on isolated
API-31 AVD `test31`. The final required run used isolated synthetic API-30 AVD
`sello022_api30`, serial `emulator-5580`; the physical phone was excluded. Its full
device matrix includes the real UI journey, which wrote 150,000 COP income and
30,000 COP expense, independently queried those Room totals, then selected both the
30,000 COP daily point and the 30,000 COP monthly point before opening category
history, recreation and relaunch.

Saved-task restoration was checked separately on the verified debug APK on that API-30
AVD. Resumen was active in PID 11230; after Home and `am kill`, that PID was absent.
Recents restored the task as PID 11513 on the Resumen tab with `Ingresos menos gastos`.
The first kill was too early to make the process eligible and left PID 11230 alive;
it was discarded and repeated after the activity entered the background. The confirmed
run is actual process-death evidence rather than activity recreation. It used an empty
synthetic history because exact populated history/relaunch is already exercised by the
real journey. Automated screen semantics cover all four themes, 2× type and the
constrained 840 dp layout. No manual TalkBack session or screenshots are claimed.

The retained final G2 run is `20261014T021735Z-93a3887a`: 488 host tests and 486
device tests passed with zero failures, errors or skips; lint reported zero errors
and 30 warnings. Its device is API 30. A passing API-31 run was superseded to match
the runbook exactly. The report is `docs/planning/mvp/quality-reports/SELLO-022.json`.
