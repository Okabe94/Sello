# Monthly snapshot

Decisions: [D03, D04, D07](../decisions/0001-mvp-contract.md) ·
Examples: [financial examples](../testing/mvp-financial-examples.md) ·
Rules: [architecture](../../ARCHITECTURE.md) §§4, 5, 6.

Every figure a screen shows about a month comes from one `MonthlySnapshot`, so the
slip, the category detail and the charts cannot disagree. Screens format it; they
never add, subtract or compare amounts themselves.

## Getting one

`MonthlySnapshots` (`:domain`), implemented by `RoomMonthlySnapshots` (`:data`):

- `read(month)` returns one snapshot or a typed failure.
- `observe(month)` emits `Loading` first, then a fresh state after every committed
  change and every change of the financial day, real or simulated.

| State | Meaning | Show |
| --- | --- | --- |
| `Loading` | Nothing read yet | A skeleton, never zeros |
| `Ready(snapshot)` | Read successfully. A month with nothing in it is `Ready` with zero spent | The figures |
| `Failed(failure, lastGood)` | The latest read failed | The error; `lastGood`, if present, clearly as older data with its own date |

A snapshot carries the `generation` and `revision` it was read at and the financial
day `asOf` it was worked out for, including when it is kept as `lastGood`.

## What is in it

- `spent`: everything spent in the month up to `asOf`, in every category, with or
  without a limit. Expenses dated after `asOf` are left out and counted in
  `futureDatedExpenses`.
- `overall`: `NoLimit` when no category has a finite limit that month ("Has gastado /
  Sin límite"; not a zero budget), otherwise `Limited` with the sum of finite limits,
  `spent`, `remaining` (negative when overspent, never clamped), the standing, and
  the daily allowance in the current month.
- `categories`: each with its limit state, its `spent`, how many expenses make that
  up (`expenseCount`) and, when it has a finite limit, its own budget and standing.
  An archived category is listed only in months where it has a limit or spending.

A category over its limit does not reduce the overall allowance by more than what
it actually spent: the overall figures use overall limits and overall spending.

## The rules

All in `MonthlyBudgetPolicy` (`:domain`), with `S` spent, `L` the limit, `d` today's
day of the month and `N` the month's length. Arithmetic is exact; nothing is a
floating-point number and SQL does no sums.

| Figure | Rule |
| --- | --- |
| Forecast | `S × N / d`, kept as an exact fraction; shown rounded half up to a peso. Today counts towards the pace, not towards the days still to come. An estimate, to be labelled as one |
| Verdict, current month | `Over` if spent or the exact forecast is above `L`; `OnTrack` if the forecast is at most 95% of `L`; otherwise `Tight`. Compared before rounding |
| Verdict, past month | `Over` only if more than `L` was spent; otherwise `WithinLimit`. No forecast |
| Available days | `N − d + 1`, today included |
| Daily allowance | `50 × floor(max(remaining, 0) / (50 × available days))`. Rounded down to 50 pesos. Current month only. The only figure rounded this way |

On Recibo, `OnTrack`, `Tight`, `Over` read Al día, Justo, Pasado; a past month
reads Bajo el límite or Sobre el límite. Labels and colours belong to presentation.

These are the rules in the code today. [ADR 0012](../decisions/0012-budget-guidance-contract.md)
approves the statuses and daily guidance that replace them; until a scoped change
implements those, keep these calculations and their tests.

A total that does not fit an exact amount is `SnapshotFailure.Unrepresentable`. A
month after the current one is `FutureMonth`. A damaged row is `Storage(Integrity)`.
None of them is ever shown as zero or as an empty month.

## Consistency

Categories, limits, expenses and the revision are read in one transaction, so a
snapshot never pairs the spending of one revision with the limits of another.
Observation watches the profile row, which every command changes; bursts are
conflated, so a slow screen gets the latest state instead of a backlog.

## Where it is worked out

The rows are read inside Room's transaction, on its own threads. The figures are then
worked out on the computation dispatcher given to `FinancialStorage.monthlySnapshots`
(`DispatcherProvider.default` in the app), never on the caller, which for a view
model is the main thread. The rows are already out of the database by then, so a save
that lands meanwhile cannot mix into the figures; it produces the next snapshot. An
observation cancelled while its figures are being worked out delivers nothing.

Measured on the API 30 emulator, debug build, 20 categories, one month (2026-10-11,
informational, not a target): reading 7 / 20 / 131 ms and working out 1.4 / 2.8 /
19 ms for 100 / 1,000 / 10,000 expenses. Targets and a real device belong to
SELLO-035. The screens still format the result on the main thread.

## Around it

- Income, net flow, "por asignar" and the per-day and monthly series are in
  `ResumenSnapshot`, worked out by `ResumenPolicy` around this snapshot. The series
  come from the same expenses.
- Recurrence is outside the MVP, so nothing is reserved for upcoming payments.
