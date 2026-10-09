# 0007 — Pesos are shown without a currency symbol

**Status:** Accepted product direction · **Date:** 2026-10-09
**Owner/approver:** project owner (user) · **Ticket:** SELLO-007

## Decision

The visual reference draws a small raised `$` before hero, total, card and title
amounts and brings it back on a receipt's total line. Reviewing the first working
components, the owner chose to drop it.

- **MUST** show COP amounts with no currency symbol at every size: `937.200`,
  `+850.000`, `−10.000`. The sign rules and the true minus are unchanged.
- **MUST** still say the currency to a screen reader: `937.200 pesos`.
- **MUST** show any other currency as its code, a space, then the figure:
  `USD 2.340,00`. The space must not become a line break. No such currency can be
  entered in the MVP.

This supersedes the reference's "How money is written" rule about the peso sign and
the `$` in its mock-ups; the reference file itself is unchanged. It does not change
grouping, decimals, abbreviations for chart labels or money semantics.

## Alternatives and consequences

Rejected: keeping the raised `$` as drawn, and showing `COP` as a code. Sello is a
single-currency COP app in the MVP, so the mark carries no information. If other
currencies become enterable, whether pesos then need a mark is a new decision.
`MoneyFormatter` owns the rule; `MoneyText` draws whatever code it is given.
