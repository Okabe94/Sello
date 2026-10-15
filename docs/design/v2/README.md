# Sello v2 design

**Status:** in progress. Screens are decided one at a time with the owner and then
locked. When every screen is locked, this folder becomes the design source of truth
and replaces the v1.1 snapshot where they differ.

Open the pages over HTTP from the repository root:

```bash
python3 -m http.server 8765 --bind 127.0.0.1
```

Then visit `http://127.0.0.1:8765/docs/design/v2/home.html`. Pages use the bundled
fonts, the colour values of `SelloColors.kt` and icons generated from
`design-system/src/main/res/drawable`. `flag`, `close` and `trending_up` are
mock-only icons that the design system does not bundle yet. All figures are
synthetic.

| Screen | State |
| --- | --- |
| [Recibo / Home](home.html) | Locked 2026-10-15 |
| Expense and income entry | Next: compare the fixed pad with the system keyboard |
| Resumen | Pending |
| Income | Pending |
| Metas | Pending |
| Inversiones | Pending |

## Recibo / Home

**Order, top to bottom:** month bar → answer slip → review invitation (early month
only) → goal → categories. Anotar is a full-width button on a dock above the tabs.

**Answer slip**

- Label **Restante del presupuesto** and a status stamp on one line, then the big
  number: the budget left after reserving upcoming payments.
- Under the tear line: **per day** as a clear second number (`40.000 por día`) with
  `10 días, incluido hoy` below it. Always shown in the current month, including
  `0 por día`.
- While a payment is reserved, a small footnote at the bottom:
  `Incluye 200.000 separado para 1 pago previsto ›`. It opens the list of payments.
  It disappears when nothing is reserved.
- The bottom edge is the v1.1 pinked shape (sharp teeth, 10dp × 5dp). The tear line
  keeps its punched notches.

**Status stamps**

| Situation | Stamp | Colour | Extra |
| --- | --- | --- | --- |
| Spending below the limit | Al día | gain | — |
| Spending equal to the limit | Al límite | neutral | — |
| Spending above the limit | Excedido | loss | Signed red remainder, e.g. −80.000; link **Ajustar presupuesto** |
| Reserved payments use up what is left | Sin margen | neutral | Big number 0 |
| Reserved payments are more than what is left | Revisar | warn | Label **Falta para tus pagos**, positive shortfall (e.g. 50.000), one sentence `Quedan 100.000 y tus pagos previstos suman 150.000.`, link **Ajustar presupuesto** |
| No finite limit | Sin límite | neutral | Label **Has gastado este mes**; no per-day line |

**Goal:** one pill, 56dp tall. The pill itself is the progress bar: it fills with
`brandSoft` up to the goal's percentage. It shows the goal's icon, name and
percentage, and ends with a round **+** button (Aportar). Tapping the pill opens
the goal. Only shown when Metas ships.

**Categories:** unchanged circle grid. A category shows only its own remainder;
reserved payments are not shown inside categories. Overruns use the signed red
remainder.

**Review invitation:** one dashed-outline line, for example
`Ver cómo cerraste octubre ›`. Never a stack of cards.

**Settled during the review and rejected:** Anotar inside the tab bar (crowded with 4
tabs and large text), reserved amount inside a category, receipt-line, column and
sentence layouts for the lower half, and the other goal looks (ticket stub, ink pill,
punch card, goal stamp, coupon).

## Changes to record in the decision documents

These Home choices replace working wording in
[ADR 0012](../../decisions/0012-budget-guidance-contract.md) and the
[Home brief](../screen-briefs/home.md). Update them when the v2 spec is consolidated.

- Stamps: Límite alcanzado → **Al límite**; Sobre el límite → **Excedido**;
  Sin margen libre → **Sin margen**; Por ajustar → **Revisar**.
- Over-the-limit and shortfall link: **Ajustar presupuesto** (was Revisar presupuesto).
- Per-day line wording: **por día** (was "Puedes usar … al día", which clashed with
  the Al día stamp).
