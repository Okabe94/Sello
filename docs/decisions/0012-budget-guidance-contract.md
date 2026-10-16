# 0012 — Budget guidance and income planning definitions

**Date:** 2026-10-15 · **Owner:** project owner (user) · **Ticket:** none
**Status:** partially owner-confirmed step 2 definitions. BG1's presented states
and BG2's presented positive/zero/negative-margin scenarios and payment transition
are confirmed as recorded below. BG3's expected/recorded separation and BG4's
category-only allocation are
confirmed in principle; remaining boundary/lifecycle choices are identified below.
No replacement calculation or screen is implemented.
Status wording follows [ADR 0013](0013-sello-v2-design.md) (Sello v2): Al límite,
Excedido, Sin margen, Revisar and the link Ajustar presupuesto replace the earlier
working words; a closed month reads Cumplido or Excedido. Meanings are unchanged.

## Context and boundary

[ADR 0011](0011-goal-centered-design.md) and the
[design philosophy](../design/philosophy.md) retire pace-based home judgment and
prioritize remaining budget, known commitments and personal goals. The owner asked
to settle the financial definitions before designing the replacement screens.

The actual `MonthlyBudgetPolicy` still calculates the D03 pace forecast and verdict,
and rounds displayed daily allowance down to COP 50 multiples. `ResumenPolicy`
currently subtracts configured limits from recorded income for allocation; it does
not subtract spending a second time. Goals, investments and recurrence are absent.
The [D01–D10 contract](0001-mvp-contract.md) and
[approved example corpus](../testing/mvp-financial-examples.md) remain the delivered
baseline. This decision record does not change those examples, tests, ticket status
or code. Its confirmed definitions guide subsequent designs; the remaining cases
and recurrence mechanisms below still need review before affected implementation.

All figures below are synthetic integer COP examples, with successful reads and
consistent month/as-of data. The decision log distinguishes confirmed choices from
proposed results. Neither is an executed test or an independent technical approval.

## BG1 — Factual budget status

**Owner-confirmed, 2026-10-15:** the four presented states below, with an uplifting
positive status in place of the initially proposed Dentro del presupuesto. Use
**Al día** as working wording, with Todo bien another example the owner gave; final
interface copy is still reviewed with the screen. Recorded-expense classification
does not use pace or turn a category overrun into an overall overrun. BG2 records
confirmed feedback for the presented cases where commitments exhaust/exceed margin;
unpresented combinations and general precedence still need review.

| Recorded position | Direction and approval boundary |
| --- | --- |
| Spending below the finite overall limit | Uplifting positive status; Al día is working wording. Owner revised the initial Dentro del presupuesto proposal. |
| Spending exactly equal to the finite overall limit | Al límite; neutral, not an overspend. Presented equality state confirmed; explicit zero/zero example still awaits review. |
| Spending above the finite overall limit | Excedido, with the exact overrun |
| No finite overall limit | Sin límite; show recorded spending, without inventing a remaining amount or daily allowance |

**Follow-up presentation confirmation:** the owner accepted consistent signed
remainders and restrained semantic color. With an actual overall overrun of 80.000,
show **Restante del presupuesto −80.000** in red plus **Excedido**. Category
remainders follow the same sign/color rule, while a healthy overall remainder stays
in the normal ink with positive status. Color never carries the meaning alone.
This refines presentation without changing arithmetic, classifier precedence or
BG-E06's explicitly labeled positive shortfall magnitude.

Past-month presentation is settled in v2 (Cumplido / Excedido, ADR 0013); explicit zero/zero remains a proposed review case. Past
months use recorded results and omit daily allowance under the delivered contract.
The revised direction introduces
no new percentage warning threshold and makes no promise about unrecorded activity.
Failed reads remain failures, with optional explicitly stale data; no status or zero
is substituted for a failed calculation. The existing day cutoff, zone, historical
limit and unlimited-spending rules remain unless explicitly revised.

Future commitments that exceed remaining budget use the confirmed Revisar
direction for BG-E06 (see BG2); they must not be described as expenses already
recorded. Final copy and status precedence for other combinations await review.

## BG2 — Remaining budget, reserved commitments and daily guidance

Keep distinct concepts even if the screen emphasizes only one:

- `L`: total configured finite limits for the selected month.
- `E`: applicable recorded expenses, including spending in unlimited categories.
- `R = L − E`: remaining budget after recorded expenses; retain its sign exactly.
- `Q`: known recurring expenses reserved for the remainder of that month, not
  already represented by a recorded expense for the same obligation.
- `M = R − Q`: margin after reserving those commitments; retain its sign exactly.
- `D`: days remaining in the current financial month including today.

Home's budget remainder, reservations and daily guidance use these expense-budget
inputs only. Neither recorded income nor expected income is added to the spending
budget or allowance. BG3 discusses the separate income/planning view; it does not
couple salary recording to home capacity.

**Recommended for review:** keep displayed daily allowance as
`50 × floor(max(M, 0) / (50 × D))`. Preserve COP 50 downward rounding and inclusive
today. This is an overall budget guide; it is not a per-category restriction or a
verified cash balance. A displayed zero caused by rounding positive margin below
50 must not imply an overrun. Past months omit it; no-limit months cannot compute it.

**Headline confirmed by the owner, 2026-10-15:** emphasize `M`, after reserving
upcoming payments, and inform the user of `Q` at lower priority. In the presented
example, show **400.000** prominently and the reserved **200.000** as secondary
information. `R = 600.000` retains its underlying recorded-budget meaning; label
these figures distinctly. This refines the philosophy's headline treatment of
commitments. It does not approve new rounding or reservation lifecycle rules.

**Zero free margin confirmed by the owner, 2026-10-15:** if the category budgets
have 100.000 remaining after recorded expenses and 100.000 is reserved for upcoming
expenses, show 0 discretionary margin with neutral **Sin margen** and the
reservation underneath.

**Negative free margin confirmed after clarification, 2026-10-15:** BG-E06 uses
only configured category budgets, recorded expenses and upcoming expenses. With
100.000 left in those budgets and 150.000 reserved, show **Revisar**, the exact
**50.000 budget shortfall** and zero daily allowance. The owner accepted this after
clarifying that no income participates. This is a budget-plan shortfall, not a
claim about the user's income or bank balance.

Reservation must count an obligation exactly once. When that obligation becomes a
recorded expense of the same amount, it leaves `Q` and enters `E`; it is not both.
**Owner-confirmed payment transition, 2026-10-15:** recording BG-E02's 200.000 rent
leaves the headline at **400.000**. If that was the last reserved expense, omit the
reservation context entirely: no residual explanation or “0 reserved” line. The
reservation line is secondary context, not an additional deduction from the hero.
With the same day and no other changes, the daily allowance also stays 40.000.
Algebraically, `L − (E + payment) − (Q − payment) = L − E − Q`; actual spending
and the reserved amount explain the margin without changing it twice. This
invariance assumes the recorded amount equals the released reservation. A changed
amount is a distinct future review case. A failed commitment read is not a known zero.

Do not infer that a manual entry matches a schedule merely from its name or amount.
Occurrence identification, overdue/skipped/paused items, changed amounts and a failed
commitment read need explicit rules before recurrence implementation. Zero reserved
expense is truthful today because recurrence is absent, not a fallback on read failure.

If `R` is positive but `M` is negative, actual spending is still below the limit,
while known commitments do not fit. BG-E06 confirms zero daily allowance, Revisar
and the exact shortfall, with explanation naming the upcoming expenses. Complete
precedence for other combinations, such as an already exceeded recorded budget plus
additional commitments, still needs review.

## BG3 — Expected and recorded income

**Owner-confirmed, 2026-10-15:** future recurring income can inform a clearly labeled
monthly plan. Keep recorded income `I` and expected remaining income `F` distinct;
show `I + F` only as a planned total. Recording a scheduled occurrence replaces its
expected amount, rather than adding that occurrence to both totals. A plan is not
bank verification or a guarantee of arrival. Cadence and occurrence rules remain
future recurrence decisions.

Use total configured finite category budgets as allocation `A`, as clarified in BG4.
Distinguish recorded allocation
capacity `I − A` from planned capacity `I + F − A`, including negative results.
Do not add expected income to recorded history, net flow, goal contributions or
portfolio value. Retain income-independent budget guidance unless separately
approved: receiving salary alone does not change configured spending limits.
Future-month planning also needs a scoped extension to the existing month-selection
contract; this decision does not enable future actuals or new navigation.

## BG4 — Category-only income allocation; independent tracking

**Owner-confirmed, 2026-10-15:** income is allocated only through category budgets.
`A = sum(configured finite category limits for the selected month)`. Recorded
unassigned planning capacity is `I − A`; planned capacity is `I + F − A`. Preserve
negative results. Actual category spending is not subtracted again from allocation.

Goal contributions, investment contributions, withdrawals, portfolio value and
returns do not independently enter this allocation sum. Users may create a category
for investing, saving or another purpose; its configured limit counts through the
same category-budget rule. Do not automatically create such a category or change a
limit when a goal/investment record is entered.

Goals and investments remain independent tracking tools, including when the same
money appears in both. Recording a contribution updates that tool's progress/history
without external proof. It neither allocates income again nor requires overlap
matching, deduplication or an extra allocation-source indication. This replaces both
alternatives previously proposed in this section and the older future sum of category
limits plus goal/investment contributions from architecture/v1.1.

Income suggestions must respect this meaning. Merely recording a goal contribution
cannot be described as reducing unassigned income. To give unassigned income a
budget destination, the relevant action is a user-chosen category-budget change;
the goal contribution is a separate tracking action. The final suggestion flow
still needs design. Unspent category budget and investment gains are not new income.

## Worked examples for owner review

| ID | Inputs | Result and review status |
| --- | --- | --- |
| BG-E01 | Limit 1.000.000; recorded spending 400.000; no reservations; 10 days left | Remaining/margin 600.000; daily 60.000. Positive tone confirmed (Al día as working copy); this numeric case has not been separately owner-reviewed. Earlier pace does not determine it. |
| BG-E02 | Same limit/spending/days; rent 200.000 reserved | Owner confirmed headline 400.000 after reservation, with reserved 200.000 at lower priority. Remainder before reservation is 600.000; presented daily amount is 40.000. Final label, unreviewed lifecycle and general rounding cases remain open. |
| BG-E03 | The rent in BG-E02 is recorded today for 200.000, removing its reservation; still 10 days left | Owner confirmed: spending becomes 600.000; headline remains 400.000; reserved becomes 0 and its context line disappears entirely. Daily remains 40.000 with the same day/no other changes. No second deduction. |
| BG-E04 | Overall limit 300.000; spending 110.000, all in one category limited to 100.000; 10 days left | Category overrun 10.000; overall remainder 190.000; daily 19.000; positive overall status (Al día as working copy). No automatic category reallocation. Original B08 amounts are approved baseline; this revised-label case is not independently reviewed. |
| BG-E05 | Limit 1.000.000; spending 1.000.000, then 1.000.001; no reservations | At equality: Al límite, daily 0. One peso above: Excedido, exact overrun 1, daily 0. |
| BG-E06 | Limit 1.000.000; spending 900.000; reserved 150.000; 10 days left | Owner confirmed after expense/income clarification: remainder 100.000, margin −50.000, daily 0; Revisar and exact 50.000 budget shortfall, identifying upcoming expenses. No income input or bank-balance claim. |
| BG-E07 | Margin 849, then 850; 17 available days | Display daily 0, then 50. The first margin is still positive; no overspend label from rounding. |
| BG-E08 | Recorded income 2.000.000; expected salary 1.000.000; category budgets 2.800.000 | Owner accepted the planning example: planned total 3.000.000, including pending 1.000.000, and planned unassigned 200.000. Recorded capacity is −800.000. The subsequent BG4 answer defines the 2.800.000 assignment as category budgets only. |
| BG-E09 | BG-E08 salary recorded for the expected 1.000.000 | Recorded income 3.000.000; expected remaining 0; recorded/planned allocation capacity both 200.000. No double income; transition rule accepted with BG3. |
| BG-E10 | Income 2.000.000; category budgets 1.000.000; goal contribution 500.000 and investment contribution 500.000, including if they describe the same money | Category-only rule confirmed: allocation 1.000.000 and unassigned 1.000.000. Goal/investment entries do not change either figure; both earlier overlap-counting alternatives are superseded. |
| BG-E11 | Configured category budgets have 100.000 remaining after recorded expenses; upcoming expenses reserve that same 100.000 | Owner confirmed 0 discretionary margin, neutral Sin margen, and reserved 100.000 at lower priority. No income is an input. |
| BG-E12 | BG-E10 plus a user-created investment category with a finite budget of 500.000 | Under BG4, total category allocation becomes 1.500.000 and unassigned becomes 500.000. Goal/investment records still make no separate deduction. Derived illustration of the accepted rule, not a separately reviewed numeric example. |

## Decision log and implementation prerequisites

| Decision | Owner answer | State |
| --- | --- | --- |
| BG1 factual status and equality/zero/no-limit cases | 2026-10-15: owner accepted equality, over-limit and no-limit states; requested uplifting wording such as Al día/Todo bien for below-limit. Explicit zero/zero, past-month copy and commitment precedence are not yet reviewed. | Partially confirmed |
| BG2 headline, reservation, daily formula/rounding and commitment shortfall | 2026-10-15: owner confirmed BG-E02's 400.000 headline/secondary 200.000, BG-E11's Sin margen, BG-E06's Revisar/exact shortfall/zero allowance, then BG-E03's unchanged headline after payment and disappearance of zero-reservation context. Unreviewed lifecycle, other precedence and general rounding cases remain open. | Presented scenarios confirmed; remaining cases open |
| BG3 expected income and recorded/planned distinction | 2026-10-15: owner accepted planning with 2.000.000 recorded, 1.000.000 expected/pending, 2.800.000 assigned and 200.000 planned unassigned; expected becomes recorded once and does not change home capacity. BG4 subsequently limits assignment to categories. Future cadence/month-planning lifecycle remains scoped work. | Confirmed in principle |
| BG4 allocation across independent overlapping records | 2026-10-15: owner rejected allocating through goal/investment entries; only categories allocate income. Users can create an investment category. Independent goal/portfolio tracking does not deduct income, and no overlap identification is needed. | Confirmed |

The first review round presented BG1's four ordinary states and BG-E02's positive
reservation scenario. The owner answered: the below-limit wording should be more
uplifting, such as Al día/Todo bien; the other three states were accepted; show
400.000 and inform about 200.000 at lower priority. No answer to BG3/BG4, no blanket
approval of the entire example table and no implementation approval is inferred.

In the second round, the owner accepted BG-E11's zero free margin, then asked why
income was being combined with spending categories in the negative-margin example.
That example used only configured category limits, recorded expenses and upcoming
expenses, not income. The question itself was not recorded as approval.

After that clarification, the owner answered “now I understand. I like it”, confirming
BG-E06's Revisar and exact 50.000 budget shortfall with zero daily allowance.
At that point BG3/BG4 remained pending. The owner also requested batching gate runs
rather than running the gate after every interview answer; verify the consolidated document at
the next completion checkpoint, without treating an earlier report as fresh evidence
for subsequent edits.

In the following round the owner accepted BG3, then clarified: “we are not going to
allocate anything besides the categories”; users can create an investment category.
That answer supersedes BG4's proposed overlap choices. Goal contributions still
count as recorded goal progress, but are no longer called an income-allocation action.

The owner then confirmed BG-E03: after recording rent, “it should stay 400.000 now
with nothing in a second level giving context”, because the headline already
accounted for the reserved expense. This confirms the stable headline and removal
of the reservation line once nothing remains reserved, not an indefinite constant
across later days, changed amounts or unrelated financial changes.

## Confirmed definitions and remaining implementation inputs

The core design meanings are recorded: factual encouraging status, margin after
known commitments, lower-priority reservation context only while needed, a stable
headline when an equal reservation becomes recorded spending, income-independent
home guidance, expected/recorded income separation, and category-only allocation.
The older future sum of category limits plus goal/investment contributions is
superseded; existing MVP allocation already uses categories only.

The completion checkpoint for this documentation does not certify a replacement
financial engine. Preserve current D03 calculations/tests until their scoped
replacement. Retain existing inclusive-day/COP-50 rules unless separately changed;
new recurrence boundary examples and the final presentation still need review.
Outstanding inputs before affected implementation are the full status precedence,
explicit zero-limit/past-month copy examples, occurrence matching, overdue/skipped/
paused/changed-amount handling, and the future-month income-planning lifecycle.
Investment performance and recommendation eligibility retain their own later scope.

Approve each choice and its relevant worked examples explicitly; do not extend an
answer to unseen cases. Preserve the original approval history. After the decisions,
update the affected architecture transition and philosophy statements, identify the
specific approved-example supersessions and scope implementation verification.
No current formula, test or acceptance is weakened by a proposal or its arithmetic
review. Remaining recurrence/goal/investment details retain their delivery gates.
