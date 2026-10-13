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

## Verification

Run focused domain/data tests, then `./scripts/verify-ticket SELLO-022 --retain` on
an isolated API-30 emulator. Record the actual revision, report and results in the
canonical epic. Part 1 cannot mark SELLO-022 Done; final acceptance and fresh evidence
follow the screens PR.
