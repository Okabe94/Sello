# Home / Recibo brief

**Owner-approved review summary, 2026-10-15 · Final visual design: Sello v2.**
Recommendations and listed open decisions retain their stated review boundaries.
Use the [brief workflow and decision labels](README.md). This is the simplest
screen: a short, appealing answer to “How am I doing this month, and what room do I
have left?” It should help the user feel oriented and in control at a glance.

> **Designed in [Sello v2](../v2/README.md):** [Recibo](../v2/home.html). Where this brief and v2 differ,
> v2 and [ADR 0013](../../decisions/0013-sello-v2-design.md) apply; the open policies below stay open.

## Information hierarchy

**Confirmed intent:**

1. The most prominent amount is remaining budget after reserving upcoming known
   payments, once recurrence exists. Keep the headline useful and exact.
2. Overall daily guidance is secondary. It answers how much can be used per day
   without exceeding the overall budget, not a prescribed amount per category.
3. Reservation context has lower priority and appears only while a reservation
   remains. A line saying zero is reserved must disappear.
4. A compact featured goal reminds users why they budget, below the monthly answer
   and before categories in the intended reading order. Include it when Metas
   ships. The user chooses; default to highest completion percentage.
5. Category remaining amounts remain easy to inspect. Preserve a category's own
   deficit even when the overall budget is healthy.

Anotar is a direct, prominent action. Detailed advice belongs in Resumen. A small
early-month invitation may lead there to review the previous month; this is not
permission for an accumulating Home feed.

## Interaction and continuity

Open expense entry from Home in one tap, with amount entry immediately available.
Categories retain direct detail access and an obvious way to add a category.
Keep month selection and existing category-management/history journeys available.
An ordinary confirmed save returns to its caller with brief feedback. The feedback
must not require another screen or dismissal before using Home.

The featured goal has visible access to its detail and contribution flow when
those capabilities ship. Do not expose placeholder tabs or actions. A category
overrun never silently moves money between category budgets.

## Visual and emotional direction

Use the receipt identity and a strong main number with clear supporting context.
Healthy remaining money uses the normal ink with a small encouraging status such
as **Al día**. Actual overall/category deficits use signed negative remainders and
semantic red. Pair the sign/color with a text label; keep the surrounding screen
calm. An isolated category deficit must not make the overall status negative.

For an actual overall deficit, the agreed example is **Restante del presupuesto
−80.000**, with **Excedido**. Keep daily guidance at zero for that example.
Do not use pace-based anxiety or extrapolate large rent/car payments into a warning.
Income never independently increases the Home budget or daily allowance.

## Scenarios to carry into design review

| Situation | Desired reading |
| --- | --- |
| Limits 1.000.000, spending 400.000, reserved payments 200.000, ten inclusive days | Main 400.000; secondary 40.000/day; reserved 200.000 at lower priority. |
| The same 200.000 payment is recorded on the same day, with nothing left reserved | Main stays 400.000; daily stays 40.000; reservation context disappears. |
| Overall remaining is positive while one category is 10.000 over | Overall positive state; category −10.000 in red. |
| Remaining before commitments is 100.000 and commitments are 100.000 | Zero free margin, neutral Sin margen, reservation context. |
| Remaining before commitments is 100.000 and commitments are 150.000 | Revisar, explicitly labeled shortfall of 50.000, zero daily guidance; do not describe it as already recorded overspending. |
| No finite overall limit | Recorded spending and Sin límite; no invented remaining amount/daily allowance. |
| First use, loading, failed read, stale last-good data | Distinct states. A failed read never becomes an empty or zero-budget screen. |

These summarize [ADR 0012](../../decisions/0012-budget-guidance-contract.md).
Its approval boundaries remain in force; the table does not define a complete
classifier or recurring-payment lifecycle.

## Designer discretion and remaining inputs

The designer chooses card count, geometry, spacing, typography scale, goal-preview
composition and the exact dock/navigation treatment. Preserve the hierarchy and
direct actions rather than reproducing the HTML coordinates. Large text can reflow
or scroll while exact amounts and actions remain understandable and reachable.

Open product inputs include review-invitation timing/dismissal and goal-selection
ties or invalidated choices. Financial inputs include full status precedence,
explicit-zero/past-month examples and recurring-payment lifecycle. Metas and
recurrence remain future capabilities; current delivery must not simulate them.
