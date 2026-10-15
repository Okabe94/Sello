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
| [Expense and income entry](entry.html) | Locked 2026-10-15 |
| Resumen | Next |
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

## Expense and income entry

**Order, top to bottom:** ✕ and the Gasto/Ingreso switch → amount slip (pinked edge,
label and date pill on top, category circle beside the amount) → category or source
chips → note row → **Anotar gasto / Anotar ingreso** → Sello pad. The amount, Anotar
and the pad never move; only the chips and note area scroll. Income shows the amount
in gain green with a `+`.

- **Amount entry:** always the Sello pad, ready the moment the form opens. The system
  keyboard is never used for amounts. Pad: the familiar 3 × 4 layout (1–9, then `000`,
  `0`, delete), 44dp keys with 4dp gaps (48dp touch pitch), 192dp in total.
- **Delete key:** soft brand tint (`brandSoft` with `onBrandSoft` icon) so it stands
  apart from the digits. Long press clears the whole amount and says so to TalkBack.
- **Amount size steps down as digits are added**, so the full amount always stays on
  one line and is never cut or wrapped:

  | Digits | Size (normal phone) | Narrow window (≈320dp) |
  | --- | --- | --- |
  | Up to 6 | 45sp (full size) | 45sp |
  | 7–9 | 35sp | 31sp |
  | 10–12 | 26sp | 21sp |

  Twelve digits is the maximum. A 13th key press shakes the amount, changes nothing and
  shows **Máximo 12 dígitos** in red in place of the "Gasto en pesos" label until the
  next valid key, so no line is reserved and nothing moves. On Android this is
  auto-sized text with these steps as the allowed sizes.
- **Category:** none selected when the form opens, so Anotar waits for one. Opening
  from a category ("Anotar aquí") pre-selects it.
- **Chips:** v1.1 SelloChip shape (36dp, 10dp corners, 1.5dp outline). At most two
  rows on any screen size; more chips scroll sideways with a 16dp fade at the edge.
- **Note:** its own row under the chips ("Agregar nota", or the note with Editar).
  It opens a composer docked to the top of the text keyboard (like a chat box),
  showing what the note belongs to, a ✓ to finish and Cancelar. Over 60 characters:
  the text is kept, the box and counter turn red and ✓ is disabled. The Otro income
  source name uses the same composer.
- **Saving:** no receipt screen. While saving, the button shows a spinner and
  "Guardando…" and everything else is locked. A definite failure says
  **No se guardó. Tu borrador sigue aquí.** and keeps the draft. An unknown outcome
  says **Aún no sabemos si se guardó. Compruébalo antes de volver a anotar.** and its
  only action is **Comprobar si se guardó** (same operation, no second write).
- **Saved:** the form closes and Home shows a short confirmation above Anotar, for
  example **Gasto anotado: 48.700 en Comida**, with the numbers already updated. Ink
  background, paper text, as v1.1's notice. No undo for new entries.

**Rejected during the review:** the system keyboard for amounts (layout jumps after the
note closes, no `000`), the four-column pad, darker-paper and outlined delete keys, the
paper-slip note dialog, and placing the note beside the date or under the amount.

## Changes to record in the decision documents

These Home choices replace working wording in
[ADR 0012](../../decisions/0012-budget-guidance-contract.md) and the
[Home brief](../screen-briefs/home.md). Update them when the v2 spec is consolidated.

- Stamps: Límite alcanzado → **Al límite**; Sobre el límite → **Excedido**;
  Sin margen libre → **Sin margen**; Por ajustar → **Revisar**.
- Over-the-limit and shortfall link: **Ajustar presupuesto** (was Revisar presupuesto).
- Per-day line wording: **por día** (was "Puedes usar … al día", which clashed with
  the Al día stamp).
- Entry: the Sello pad is the confirmed way to type amounts. This replaces the
  system-keyboard default in [ADR 0011](../../decisions/0011-goal-centered-design.md)
  and the conditional pad wording in the philosophy and the
  [entry brief](../screen-briefs/entry.md). The note uses a composer docked to the
  keyboard rather than a centred dialog.
