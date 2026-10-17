# 0012 — Budget guidance and income planning definitions

**Owner:** project owner (user) · **Updated:** 2026-10-17
**Status:** approved definitions and examples as labeled below; replacement
calculations and screens are not yet implemented. Recurrence is outside MVP.

[ADR 0011](0011-goal-centered-design.md) sets the product direction;
[ADR 0013](0013-sello-v2-design.md) owns the v2 presentation. This document defines
budget guidance and income allocation, with synthetic whole-peso COP examples.
All examples assume successful reads and a consistent month/as-of snapshot.

## BG1 — Factual budget status

Home's target status describes the budget, without judging spending pace.
One category's overrun does not make a healthy overall budget exceeded or trigger
an automatic change to category limits.

| Current-month position, without upcoming reservations | Status |
| --- | --- |
| Spending below the finite overall limit | Al día |
| Explicit zero limit and zero spending | Al día |
| Spending exactly equals a positive finite limit | Al límite |
| Spending exceeds the finite overall limit | Excedido, with the exact overrun |
| No finite overall limit | Sin límite; show spending, without remainder or daily allowance |

When recurrence ships, reservations that leave zero margin use **Sin margen**;
those that leave a shortfall use **Revisar**. For an overrun with upcoming payments
still reserved, **Revisar** takes priority, even if recorded spending is already
over budget. Once those payments are recorded, use **Excedido** if recorded spending
exceeds the limit. Upcoming payments are not recorded expenses.

Retain signed remainders. An actual overrun of 80.000 shows
**Restante del presupuesto −80.000** in restrained red; category deficits use the
same sign/color rule. Positive remainders use normal ink. Meaning must remain
clear without color. A commitment shortfall is labeled separately as in BG-E06.
Use **Ajustar presupuesto** for the adjustment link.

Past months use recorded results: **Cumplido** within the limit, **Excedido** above
it, with no daily allowance. No new percentage warning threshold is introduced.
Failed reads remain failures; optional last-good data must be labeled stale.
Never substitute zero or a reassuring status for a failed calculation.

## BG2 — Remaining budget, reserved commitments and daily guidance

- `L`: configured finite category limits for the selected month.
- `E`: applicable recorded expenses, including spending in unlimited categories.
- `R = L − E`: remaining budget after recorded expenses, retaining its sign.
- `Q`: known upcoming recurring expenses not already recorded for that obligation.
- `M = R − Q`: margin after reservations, retaining its sign.
- `D`: days remaining in the current financial month, including today.

Home emphasizes `M`; show reserved `Q` at lower priority only while nonzero.
Keep `R` and `M` clearly distinguished wherever both appear. Recorded or expected
income does not increase spending budgets, margin or daily guidance.

The displayed daily guide rounds down to multiples of COP 50:
`50 × floor(max(M, 0) / (50 × D))`.
A positive margin can therefore display zero per day without implying an overrun.
This is overall budget guidance, not a category restriction or verified cash balance.
Past months omit it; no-limit months cannot compute it.

A reservation becomes recorded spending exactly once. For the same amount, it
leaves `Q` and enters `E`, keeping `M` unchanged. With the same day and no unrelated
changes, daily guidance also stays unchanged. Remove the reservation context when
nothing remains reserved; show no “0 reserved” line or extra deduction.

Recurrence is outside MVP, so `Q` is genuinely zero today. A failed read is never
proof of zero commitments. Do not match a manual entry to a schedule by name or
amount alone. Occurrence matching and changed, overdue, skipped or paused payments
belong to later recurrence work.

## BG3 — Expected and recorded income

For later recurrence planning, keep recorded income `I` and expected remaining
income `F` distinct. Show `I + F` only as a clearly labeled planned total, without
promising arrival or bank verification. Recording an expected occurrence replaces
it once; it must not remain in both totals.

With category allocation `A`, recorded planning capacity is `I − A`; planned capacity
is `I + F − A`. Preserve negative results. Expected income does not enter recorded
history, net flow, goal progress or portfolio value. Salary recording alone does
not change configured spending limits or Home's budget guidance.

Expected-income lifecycle and future-month planning are outside MVP. Define them
with recurrence work; this ADR enables no future actuals or new month navigation.

## BG4 — Category-only income allocation; independent tracking

Only category budgets allocate income:
`A = sum(configured finite category limits for the selected month)`.
Recorded unassigned planning capacity is `I − A`, not an account balance.
Actual spending is not subtracted again.

Goal/investment contributions, withdrawals, portfolio value and returns do not
independently allocate income. Users may create saving/investment categories;
their limits count like any other category. Tracking entries never automatically
create categories, change limits or create expense/income entries.

Goals and investments remain independent, even when they describe the same money.
Contributions count as progress without bank proof, overlap matching, deduplication
or an extra allocation-source indication. Unspent budgets and investment gains
are not new income.

Allocation suggestions must offer a user-chosen category-budget change. Recording
a goal contribution alone cannot be described as reducing unassigned income.
The suggestion creates a new category with only its limit prefilled with the
unassigned amount.

## Worked examples

Confirmed means owner-approved, not implemented or independently tested.
Rows explicitly marked otherwise retain their separate review requirement.

| ID | Inputs | Result and review status |
| --- | --- | --- |
| BG-E01 | L 1.000.000; E 400.000; Q 0; D 10 | Confirmed (2026-10-17): R/M 600.000; daily 60.000; Al día. |
| BG-E02 | E01 plus reserved rent 200.000 | Confirmed: headline M 400.000; secondary Q 200.000; R 600.000; daily 40.000. |
| BG-E03 | E02 rent recorded for 200.000 on the same day; no other changes | Confirmed: E 600.000; Q 0; headline 400.000; daily 40.000. Reservation context disappears. |
| BG-E04 | L 300.000; E 110.000 in a category limited to 100.000; Q 0; D 10 | Confirmed (2026-10-17): category overrun 10.000 shown on that category only; overall R 190.000; daily 19.000; Al día. |
| BG-E05 | L 1.000.000; E 1.000.000, then 1.000.001; Q 0 | Confirmed: Al límite/daily 0, then Excedido/overrun 1/daily 0. |
| BG-E06 | L 1.000.000; E 900.000; Q 150.000; D 10 | Confirmed: R 100.000; M −50.000; daily 0; Revisar and exact shortfall 50.000, naming upcoming payments. No income input. |
| BG-E07 | M 849, then 850; D 17 | Confirmed: daily 0, then 50. The first margin remains positive; rounding does not mean overspending. |
| BG-E08 | I 2.000.000; F 1.000.000; A 2.800.000 | Confirmed: planned income 3.000.000, including pending 1.000.000; planned unassigned 200.000; recorded capacity −800.000. |
| BG-E09 | E08 expected salary recorded for 1.000.000 | Confirmed: I 3.000.000; F 0; recorded/planned capacity both 200.000. No double income. |
| BG-E10 | I 2.000.000; A 1.000.000; goal and investment contributions each 500.000, possibly the same money | Confirmed: allocation 1.000.000; unassigned 1.000.000. Tracking entries change neither. |
| BG-E11 | R 100.000; Q 100.000 | Confirmed: M 0; neutral Sin margen; reserved 100.000 secondary. No income input. |
| BG-E12 | E10 plus a user-created investment category limited to 500.000 | A 1.500.000; unassigned 500.000; tracking entries make no separate deduction. Derived illustration, not separately reviewed. |

## Implementation boundary

Current `MonthlyBudgetPolicy` still uses the D03 pace forecast and verdict;
`ResumenPolicy` already allocates recorded income through category limits only.
Goals, investments and recurrence are not implemented.

Preserve current D03 calculations/tests until their scoped replacement uses the
approved rules and independent examples. This ADR changes no code, ticket status
or existing evidence. Day cutoff, financial zone, historical limits, exact-money
and recovery safeguards remain governed by [architecture](../../ARCHITECTURE.md),
the [product contract](0001-mvp-contract.md) and
[approved financial examples](../testing/mvp-financial-examples.md).
