# 0002 — Preserve surplus and overspending across periods

**Owner:** project owner (user) · **Updated:** 2026-10-16
**Status:** approved post-MVP direction; detailed policy still needs approval.

Carryover preserves both unused budget and overspending across months. Neither
may disappear silently. MVP renews configured monthly budgets without carryover;
do not add unused tables, jobs, settings or UI for it. Renewal changes a spending
plan, not actual money or bank balances.

Keep monthly base, carry-in and resulting capacity distinct and explainable.
Carryover is neither income nor expense; do not count it in allocation or net flow.

## Simple examples

Assume one category, a monthly base of 100,000 COP, no previous carry, no caps or
transfers, and carryover enabled:

| Example | Spending | Carry into next month | Next month's capacity |
| --- | --- | --- | --- |
| C01 | 20,000 | +80,000 | 180,000 |
| C02 | 120,000 | −20,000 | 80,000 |

These examples approve no multi-month formula. Preserve earlier deficits, even
when allowance is zero; never silently clip, expire or forgive them.

## Open policy decisions

SELLO-037 must settle these with reviewed multi-month and correction examples:

- Period, start date, opt-in scope, and enabling/disabling behavior. Carryover for
  every category is not approved; cross-category effects remain undecided.
- Caps, zero/negative capacity, missing history, and archive/unlimited transitions.
- How correcting an older expense changes later carry. Base-limit changes must
  preserve prior configured limits; explain any changes to later derived capacity.
- Which capacity budget verdicts compare against, and income-allocation treatment.

## Implementation and verification

Follow [architecture](../../ARCHITECTURE.md) for module and financial boundaries.
Preserve dated transactions, category identities and past limits. Delivery belongs
to [SELLO-E09](../tickets/E09-signed-carryover/README.md), SELLO-037–043, after
SELLO-036 release acceptance. After policy approval, verify real-Room atomicity,
replay/restart, backup/migrations, sandbox time scenarios and accessible capacity
breakdowns. Cover skipped months, late corrections, mode transitions and overflow.
