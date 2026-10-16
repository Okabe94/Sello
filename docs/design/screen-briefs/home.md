# Home / Recibo brief

Designed in [Sello v2](../v2/README.md) as [Recibo](../v2/home.html); labels and authority
are in the [briefs README](README.md). Home is the simplest screen: a short, appealing
answer to “How am I doing this month, and what room do I have left?” It should help
the user feel oriented and in control at a glance.

## Confirmed intent

1. The headline is the remaining budget after reserving known upcoming payments,
   once recurrence exists. It stays useful and exact.
2. Daily guidance is secondary: how much can be used per day without exceeding the
   overall budget, never a prescribed amount per category.
3. Reservations are mentioned only while one remains; there is never a “0 reserved”
   line.
4. One featured goal, when Metas ships, reminds the user why they budget. The user
   chooses it; the default is the goal with the highest completion.
5. A category's own deficit stays visible even when the overall budget is healthy,
   and an isolated category deficit never makes the overall status negative.

Anotar is a direct, prominent action that opens in one tap with the amount ready.
An ordinary confirmed save returns to its caller with brief feedback that needs no
dismissal. Categories keep direct access to their detail and to adding a category;
month selection and category-management/history journeys stay available.

Detailed advice belongs in Resumen. The early-month invitation to review the
previous month is one line, not permission for an accumulating feed. A category
overrun never silently moves money between category budgets.

## Tone

The screen stays calm: a strong main number, clear supporting context and a small
encouraging status when things are healthy. Sign and colour always come with a
text label. No pace-based anxiety, and no extrapolating large rent or car payments
into a warning. Income never increases the Home budget or daily guidance.

Statuses, signed remainders, reservations and daily guidance follow
[ADR 0012](../../decisions/0012-budget-guidance-contract.md) and its
[worked examples](../../decisions/0012-budget-guidance-contract.md#worked-examples).
First use, loading, a failed read and stale last-good data are distinct states; a
failed read never becomes an empty or zero-budget screen.

Large text may reflow or scroll as long as exact amounts and actions stay
understandable and reachable.

## Open

- Review-invitation timing and dismissal.
- Featured-goal ties and selections that become invalid.
- The recurring-payment reservation lifecycle.
- Independent examples for the ADR 0012 rows not yet separately reviewed
  (BG-E01, BG-E04, BG-E12).

Metas and recurrence are future capabilities; current delivery must not simulate
them, and no placeholder tabs or actions appear.
