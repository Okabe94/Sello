# Resumen and income brief

Designed in [Sello v2](../v2/README.md) as [Resumen](../v2/resumen.html) and
[income](../v2/income.html); labels and authority are in the [briefs README](README.md).
Resumen helps the user understand past behavior and make a budget that fits their
real life. Income has its own clearly identified context within that journey.

## Resumen: confirmed intent

Explain how spending compares with previous months, which categories repeatedly
cause difficulty and which weekdays concentrate spending. Support useful
adjustments rather than an undirected collection of totals. Home handles the quick
current-month answer.

- **Compare like periods.** A month in progress is never presented as complete or
  extrapolated from its pace; full-month history is labeled separately.
- **Short months.** Compare through the same day number, capped at the end of the
  shorter month, and label both ranges when they differ (on 31 March: “Del 1 al 31
  de marzo · Del 1 al 28 de febrero”). A finished month compares whole months.
- **Missing history.** A month with nothing recorded has no data; it is not a real
  zero. Compare only when the previous period has data; otherwise show the current
  month alone, with no comparison and no message about what is missing.
- **Charts.** Every selected or spoken value is exact. Spending distribution shows
  shares of actual spending, not consumption of category budgets. Ranking by
  spending does not replace Home's configurable category order.
- **Weekdays.** Shown once one week of data exists; the month in progress counts.
  When no day stands out, the data is shown without comment.
- **Suggestions.** At most one. A category becomes eligible after going over its
  limit two months in a row; when several are eligible, the one with the bigger
  budget is shown. A dismissed suggestion may return the next month if it is still
  eligible. The tone is soft and factual: increased spending alone is not a failure,
  spending less is not the universal goal, and an isolated category overrun is not
  an overall failed budget. No warning feeds or prescribed savings percentages.

Keep an obvious route to income, and existing month selection, chart/detail access,
history and corrections.

## Income: confirmed intent

Help the user understand how much received income has a budget destination, and
encourage its intentional use, including recreation, saving or investing through
user-created categories. This is planning from manual records, not a verified bank
balance.

- Only finite category limits allocate income; expenses are not subtracted again.
  Goals and investments are independent tracking and never allocate income.
  Recording income never raises category budgets, Home's remaining amount or its
  daily guidance. These rules and their examples are
  [ADR 0012](../../decisions/0012-budget-guidance-contract.md) BG3/BG4 (BG-E08 to
  BG-E10).
- A negative unassigned amount keeps its sign and meaning.
- **Asignar a una categoría** creates a new category, with only its limit prefilled
  with the unassigned amount. Recording goal progress alone never claims to reduce
  unassigned income.
- Expected income, when recurring income ships, stays out of recorded totals.

## States to preserve

| Situation | Desired behavior |
| --- | --- |
| Select a monthly/category chart value | The exact value with clear period/context; category selection keeps detail access. |
| A category goes over its limit two months in a row | A gentle, evidence-based finding with a relevant adjust action, when the analysis ships. |
| Loading, failed read, stale last-good data, empty history | Distinct truthful states; a failed read is not zero income or an empty history. |

## Open

- Nothing for expected income: its lifecycle (expected until catch-up records it on its
  date, never matched to a manual entry) is settled in the
  [recurrence examples](../../testing/recurrence-examples.md) (SELLO-096).

Advanced analysis, recurrence, Metas and investments keep their release scope; this
brief does not authorize dormant data models or unfinished destinations.
