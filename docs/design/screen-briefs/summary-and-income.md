# Resumen and income brief

**Owner-approved Resumen and income review summaries, 2026-10-15 · Final visual
design: Sello v2; detailed policies pending.** Recommendations and listed open decisions
retain their stated review boundaries.
Use the [brief workflow and decision labels](README.md). Resumen helps the user
understand past behavior and make a budget that fits their real life. Income has
its own clearly identified context within that journey.

> **Designed in [Sello v2](../v2/README.md):** [Resumen](../v2/resumen.html) and [income](../v2/income.html). Where this brief and v2 differ,
> v2 and [ADR 0013](../../decisions/0013-sello-v2-design.md) apply; the open policies below stay open.

## Resumen's questions and hierarchy

**Confirmed intent:** explain how spending compares with previous months, which
categories repeatedly cause difficulty and which weekdays concentrate spending.
Support useful adjustments rather than presenting an undirected collection of
financial totals. Home handles the quick current-month answer.

**Recommended reading order:**

1. A concise comparison that answers whether recorded spending increased or
   decreased over comparable periods, with exact amounts and a clear cutoff.
2. Enough monthly/category evidence to understand the change and inspect the
   categories that matter. Keep category details accessible.
3. Weekday patterns and repeated-category findings when sufficient history and
   implemented analysis exist.
4. A small, restrained suggestion section with a concrete action.

This is a logical order, not a mandatory stack of cards. The designer can combine
or arrange related content without changing the questions or hiding main actions.

## Comparisons and suggestions

Compare like periods. An in-progress month must not masquerade as a complete month
or be extrapolated from its spending pace. In the illustrative review, October
days 1–22 have 400.000 spending versus September days 1–22 at 450.000: 50.000 less,
11,1% lower. Full-month history is labeled separately from the partial month.

Show exact selected/spoken chart values even when geometry or labels are compact.
Spending distribution describes shares of actual spending, not consumption of
category budgets. Ranking by spending is a recommendation for this question, not
an implicit replacement for configurable Home category ordering.

Repeated overruns deserve a factual observation and an invitation to adjust the
budget. Use a soft tone with a clear message. Increased spending alone is not a
failure; spending less alone is not the universal goal. Avoid warning feeds,
prescribed savings percentages or presenting an isolated category overrun as an
overall failed budget. The prototype's “3 of 4 months” is an illustrative finding,
not approved recommendation eligibility.

Keep an obvious route to income. Preserve existing month selection, chart/detail
access, history and corrections. The new hierarchy must not silently remove useful
existing information such as recorded net flow; find a subordinate, clearly labeled
place for it rather than promoting it over the main comparative question.

## Income's question and hierarchy

**Confirmed intent:** help the user understand how much received income has a
budget destination. Encourage intentional use of income, including recreation,
saving or investing through user-created categories. This is planning based on
manual records, not a verified bank balance.

- Lead with recorded income and make its history/create action easy to reach.
- Clearly identify configured category budgets and the amount left to assign to
  those budgets. Only finite category limits count as allocation; do not subtract
  expenses again.
- When recurring income ships, separate recorded money from expected/pending
  income. A future planning view may include the latter if clearly labeled.
- Keep investments and goals as independent tracking tools. Their contribution
  records do not allocate income again or require overlap matching.
- Recording income does not automatically increase category budgets, Home's
  remaining amount or its daily guidance.

The exact income-surface composition and position within Resumen belong to the
designer. A separate contextual view is the current exploration. Keep income
discoverable and avoid mixing expected money into recorded totals.

## Concrete review scenarios

| Situation | Desired behavior/meaning |
| --- | --- |
| First month or insufficient comparable history | Say what is missing; show valid available data without invented comparisons or advice. |
| Select a monthly/category chart value | Provide the exact value and clear period/context; category selection retains detail access. |
| A category repeatedly exceeds its budget | Show an evidence-based, gentle finding with a relevant review/adjust action when the analysis ships. |
| Recorded income 2.000.000; category budgets 1.000.000 | Allocation is 1.000.000; recorded unassigned planning capacity is 1.000.000. |
| Same inputs plus a tracked goal/investment contribution | Allocation and unassigned capacity stay unchanged. A user-created saving/investment category would count through its budget limit. |
| Add independent manual income 150.000 | Recorded income 2.150.000; unassigned 1.150.000; Home budget guidance unchanged. |
| An expected income occurrence becomes a recorded income | Replace its expected representation once; never count both. Matching/lifecycle requires later scoped rules. |
| Loading, failed read, stale last-good data, empty history | Distinct truthful states; a failed read is not zero income or an empty history. |

Preserve negative unassigned results and their meaning. Suggestions to give income
a purpose must lead to a user-chosen category-budget action; recording goal progress
alone cannot claim to reduce unassigned income. Exact suggestion placement and
flow remain open.

## Designer discretion and remaining inputs

Choose chart types, section grouping, typography, spacing, disclosure of optional
methodology and income navigation treatment. Preserve exact financial context,
discoverability and accessible non-color equivalents across themes and large text.

Open inputs include comparable-period handling across short months/missing history,
recommendation eligibility and frequency, weekday-analysis coverage and future
income occurrence/planning lifecycle. Do not derive those rules from fixture data.
Advanced analysis, recurrence, Metas and investments retain their release scope;
the brief does not authorize dormant data models or unfinished destinations.

Financial definitions and their approval boundaries remain in
[ADR 0012](../../decisions/0012-budget-guidance-contract.md). The
[Metas](goals.md) and [Inversiones](investments.md) drafts are separate next-review
briefs, not implicit sections to squeeze into Resumen.
