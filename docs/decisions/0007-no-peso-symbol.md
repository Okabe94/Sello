# 0007 — Pesos are shown without a currency symbol

**Owner:** project owner (user) · **Updated:** 2026-10-16
**Status:** approved display rule.

Show COP amounts without `$` or `COP` at every size: `937.200`, `+850.000`,
`−10.000`. Preserve sign rules and the true minus character (`−`).

Screen readers must still announce the currency: “937.200 pesos”.

Other currencies use their code, a nonbreaking space, then the figure:
`USD 2.340,00`. Other-currency entry remains outside MVP.

`MoneyFormatter` owns currency formatting; `MoneyText` renders presentation values.
Grouping, decimal precision, chart abbreviations and financial meaning stay unchanged.

When other currencies become enterable, whether COP needs a visible currency mark
requires a new decision.
