# 0002 — Preserve surplus and overspending across periods

**Date:** 2026-10-08 · **Owner:** project owner (user)
**Status:** Accepted direction and post-MVP deferral; detailed policy pending.
**Context:** SELLO-001/D04 discussion. This records a future requirement, not a
completed feature, approved MVP scope change or finalized financial algorithm.

## Intent and alternatives

Sello should help people be honest about their spending. A calendar boundary must
not erase overspending or unused capacity when carryover is enabled. The owner
explicitly wants this capability eventually and approved deferring it beyond MVP
on 2026-10-08, with optional scope still undecided. Surplus-only rollover does not satisfy this direction because
it forgives overspending. MVP renews the configured monthly budget without carryover;
this is a spending-plan renewal, not a claim that available cash/account balance resets.
The approved follow-up is [SELLO-E09](../planning/mvp/epics/E09-signed-carryover.md),
SELLO-037–043, after SELLO-036 release acceptance. No dormant implementation in MVP.

## Foundation guardrails

- **MUST** support both signed surplus and deficit in the future carryover policy.
  Preserve negative underlying availability; a zero spending allowance is not proof
  that a deficit disappeared. No silent clipping, expiry or forgiveness of deficits.
- **MUST** distinguish configured monthly base limit, carry-in and effective capacity.
  Carried capacity is not new income or a newly recorded expense. Keep it separate
  from income allocation and recorded net cash flow; do not count it twice.
  Budget capacity is not a verified bank/account balance in this local manual tracker.
- **MUST** retain dated transactions, stable category identity and historical monthly
  limits already required by MVP. Do not overwrite old limits with today's defaults
  or throw away prior expenses merely because a month ended.
- **MUST** keep calculations in the existing domain policy boundary and storage in
  data. Prepare by preserving facts/ownership, not by adding unused mode enums,
  carry columns/tables, strategy frameworks, jobs or disabled UI to MVP.
- **MUST**, before activation, approve period/start/opt-in scope, cross-category
  effects, missing-history policy, negative-capacity handling, caps, enabling/disabling,
  archive/unlimited transitions, verdict denominator and income-allocation semantics.
  No requirement that every category use carryover is approved.
- **MUST** define the effect of historical corrections on later derived carry.
  Today's base-limit change must not rewrite prior configured limits. A correction
  to an older expense may legitimately alter later derived capacity; explain that
  impact rather than promising complete isolation of future results.
- **SHOULD** show an explainable breakdown such as “Límite del mes”, “Arrastre” and
  “Disponible”, not silently redefine a user's configured limit. Final copy follows
  the approved visual language and accessibility review.

## Conditional worked illustrations

For one finite category, fixed base 100,000 COP, initially zero carry, no caps or
transfers, and signed rollover already enabled:

| Example | Prior expense | Prior unused/overused capacity | Next effective capacity |
| --- | --- | --- | --- |
| C01 | 20,000 | +80,000 | 180,000 |
| C02 | 120,000 | −20,000 | 80,000 |

These illustrate approved intent; they are not authorization to implement an
unreviewed multi-month formula. In a continuous-carry candidate, if the 80,000
capacity in C02 is followed by 70,000 spending, 10,000 remains for the next month
and its capacity becomes 110,000. Using only `base − spent` there would wrongly
produce 130,000 by forgetting the earlier deficit. This candidate needs policy review.

## Activation evidence, not MVP implementation

SELLO-037 must approve reviewed multi-month/correction examples before the epic's
domain/storage/UI successors. SELLO-038–043 then deliver
real-Room replay/restart/atomicity tests, sandbox surplus/deficit/time-step scenarios,
backup/schema migration review, zero/negative-capacity presentation and accessible
provenance. Test skipped months, late corrections, mode transitions and overflow.
Do not create a new MVP ticket or mark SELLO-001 Done from this direction alone.
