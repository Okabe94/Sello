# SELLO-E12 — Bring Recibo to the approved Sello v2 design

- **Status:** Backlog
- **Increment:** MVP
- **Goal:** Make Home answer "how am I doing this month" with the factual status, wording and frame approved in Sello v2.
- **Exit:** Recibo's status, answer slip, categories, month bar, dock, tabs and save feedback match the v2 Recibo states in today's scope, with existing journeys, recovery and accessibility intact.

Read [ADR 0012](../../../decisions/0012-budget-guidance-contract.md),
[ADR 0013](../../../decisions/0013-sello-v2-design.md), the
[v2 README](../../../design/v2/README.md#recibo--home), the
[v2 Recibo mockup](../../../design/v2/home.html) (its "Today's scope" state shows
the MVP) and the [Home brief](../../../design/screen-briefs/home.md). D01 keeps
v2 as presentation guidance only: goals, recurrence and reservations are not MVP,
so the goal pill, "Llegó al 100 %" row, reserved-payment footnote, **Sin margen**
and **Revisar** are out of this epic and must not appear as placeholders.

**Current focus (owner decision, 2026-10-16):** bring Recibo to a working v2 state
before new features and before MVP release work; SELLO-033 therefore depends on this
epic. Read dependency links for order: SELLO-068 and SELLO-069 build the missing
components, SELLO-061 must precede SELLO-062 and SELLO-067.

These tickets come from a 2026-10-16 comparison of the running app (`98df571`,
debug build on an API 33 emulator, synthetic data) with v2. The early-month review
invitation ("Ver cómo cerraste octubre ›") is not ticketed: its timing and dismissal
are still open in the Home brief.

## SELLO-061 — Replace the pace verdict with the factual budget status

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-014, SELLO-018, SELLO-004
- **Gate:** G2

### Outcome
Recibo and category detail judge the month on what was actually recorded: Al día,
Al límite, Excedido or Sin límite, and Cumplido or Excedido for a past month.

### Deliverables
A replacement for the current-month `Verdict` in `MonthlySnapshot`/
`MonthlyBudgetPolicy` that follows ADR 0012 BG1, past-month results named Cumplido/
Excedido, the Recibo and category-detail stamps, cell and slip spoken descriptions
in the new words, and updated D03 and example-corpus text that retires the pace
verdict for Home.

### Acceptance criteria
- Below a finite overall limit is Al día, including an explicit zero limit with zero
  spending; equal to a positive limit is Al límite; above it is Excedido with the
  exact signed overrun; no finite limit is Sin límite.
- A category uses the same rule against its own limit. A category overrun never
  changes the overall status (BG-E04, B08).
- Past months are Cumplido at or below the limit and Excedido above it.
- The daily allowance keeps the approved COP 50 rounding: 25.350 for 406.300 over 16
  days; 0 at or above the limit.
- No stamp, spoken description or test in Recibo or category detail still uses Justo,
  Pasado, Bajo el límite or Sobre el límite.
- A failed read is never shown as Al día or zero; stale last-good data stays labelled.

### Tests
Domain policy tests built from BG-E01, BG-E04, BG-E05, BG-E07, B05 and B08 with
independently written expected values; render-model tests for each stamp and spoken
description; `ReciboScreenTest` and category-detail screen tests on device; G2.

### Working checkpoint
With 393.700 spent of 750.000 on 16 Oct, Recibo shows Al día and 356.300 left, not
the red Pasado the pace verdict shows today.

### Context and starting points
Evidence from 2026-10-16: with 393.700 spent of 750.000 on day 16, the forecast
(762.794) exceeds the limit, so Recibo shows "Te quedan 356.300 · Pasado"; Hogar at
200.000 of 400.000 shows "Justo". ADR 0012 BG1 and D03 "Approved design" define the
replacement; D03 "Current calculation" says the forecast stays until this ticket.

**Before Ready:** the owner reviews BG-E01 and BG-E04, which ADR 0012 still marks as
"not separately reviewed", and the reviewed values are recorded there.

Start at `domain/.../model/MonthlySnapshot.kt` (`Verdict`, `PastVerdict`,
`BudgetStanding`), `domain/.../policy/MonthlyBudgetPolicy.kt` (`verdict`),
`app/.../feature/recibo/ReciboRenderModel.kt` (stamp mapping) and the `recibo_stamp_*`
and cell strings in `app/src/main/res/values/strings.xml`.

### Implementation plan
1. Write failing domain tests from the reviewed examples, then replace the
   current-month verdict with the factual status. Keep the exact `remaining` sign.
2. Keep `Forecast` in the snapshot for Resumen's estimate line; only Recibo stops
   using it. Do not delete it while Resumen still displays it.
3. Rename the past-month result to Cumplido/Excedido and map both in the render model.
4. Update stamp inks (gain for Al día and Cumplido, neutral for Al límite and Sin
   límite, loss for Excedido), strings, spoken descriptions and screen tests.
5. Update D03 "Current calculation" and the B04/B05/B08 wording in the example
   corpus to the new names, without changing any approved amount.

### Concrete cases and pitfalls
- Spending exactly the limit is Al límite in the current month but Cumplido in a
  past month; do not reuse one enum for both without checking.
- Resumen's actual-result stamp also reads `PastVerdict`; check its words change
  consistently or leave its own strings untouched on purpose and say so.
- Unlimited categories still count toward overall spending (B01).
- Do not derive expected amounts with the policy under test.

### Verification recipe
Focused `:domain:test` and `:app:testDebugUnitTest` for the policy and render model,
then `ReciboScreenTest` on a device, then `./scripts/verify-ticket SELLO-061 --retain`.

## SELLO-062 — Rebuild the Recibo answer slip to v2

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-061, SELLO-004
- **Gate:** G2

### Outcome
The answer slip reads like the v2 Recibo: the remaining budget under one label, a
signed overrun, and the per-day figure as a clear second number.

### Deliverables
The v2 answer slip in `ReciboScreen.kt` for the current month, an overrun, no limit
and a past month, with strings, spoken descriptions, previews and screen tests.

### Acceptance criteria
- The label is **Restante del presupuesto** with the status stamp on the same line,
  then the big number. An overrun shows the signed remainder in loss ink, e.g.
  **−43.700**, not "Te pasaste por 43.700".
- With no finite limit the label is **Has gastado este mes**, with no per-day line.
- In the current month a tear line follows, then **25.350 por día** as a second
  large figure and **16 días, incluido hoy** below it; with one day left it reads
  **1 día, incluido hoy**. The per-day line is always shown in the current month,
  including **0 por día**.
- A past month shows what was left at close and `Mes cerrado · gastaste 920.000 de
  1.000.000.`, with no per-day line.
- The sign is part of the spoken amount; meaning never depends on colour alone.
- The slip stays readable at font scale 2.0 and with reduced motion.

### Tests
Screen tests for each state above, including the one-day and zero per-day cases;
updated previews; spoken-description assertions; G2.

### Working checkpoint
Recibo on a device matches the "Today's scope" and "Excedido" states of the v2
Recibo mockup, without a goal or reserved-payment line.

### Context and starting points
Today the slip says "Te quedan", shows the per-day figure as a dotted line
("Puedes gastar al día … 25.350 / Para 16 días") and shows an overrun unsigned.
Current strings: `recibo_headline_*`, `recibo_days`, `recibo_per_day`. Components:
`Slip`, `TearLine`, `MoneyText`, `Stamp` in `:design-system`. The **Ajustar
presupuesto** link is SELLO-067, not this ticket.

### Implementation plan
1. Change the render model to provide one label, the signed remainder and the
   per-day values; keep amounts formatted in presentation only.
2. Lay out the slip: label and stamp row, big number, tear line, per-day figure and
   caption, or the closed-month sentence.
3. Update strings with correct plurals, previews and screen tests.

### Concrete cases and pitfalls
- The past-month sentence needs both the spent amount and the limit from the same
  snapshot; never mix revisions.
- A long remainder must not wrap; check 12-digit amounts at large text.
- Do not show a reserved-payment line: recurrence does not exist in the MVP.

### Verification recipe
`ReciboScreenTest` and preview checks, a manual pass at font scale 1.0, 1.3 and
2.0, then `./scripts/verify-ticket SELLO-062 --retain`.

## SELLO-063 — Update the Recibo categories card and first-use and failed states

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-018, SELLO-004
- **Gate:** G2

### Outcome
The categories card, first use and a failed read look and read like v2.

### Deliverables
Categories card header with a **+** action, plain card edge, and v2 first-use and
failed-read slips, with strings, previews and screen tests.

### Acceptance criteria
- The card header is **Tus categorías** with a **+** button named "Agregar
  categoría" (48dp target); the "queda en cada una" caption and the link under the
  grid are removed. The button opens the same category creation as today.
- Only the answer slip keeps the pinked bottom; the categories card uses the plain
  slip edge.
- First use shows **Dale forma a tu mes**, "Crea una categoría para lo que importa:
  la casa, la comida, el ocio o tu ahorro." and **Crear categoría**.
- A failed read shows **No pudimos leer tu mes**, "Tus registros siguen guardados.
  Vuelve a intentarlo." and **Reintentar**; it is never shown as first use or zero.
- Long press and accessibility actions on cells keep offering edit.

### Tests
`ReciboScreenTest` for header action, first use and failed read; first-category
journey still passes; G2.

### Working checkpoint
A fresh install shows the v2 first-use slip and creating a category from the header
**+** works.

### Context and starting points
Current strings: `recibo_first_run_title` ("Tu recibo está en blanco"),
`recibo_first_run_body`, `recibo_failed`, `recibo_categories_caption`. `Slip` has a
`pinked` parameter (default true). The v2 component catalog marks the pinked slip as
the slip "that carries the screen's answer".

### Implementation plan
1. Move the add action into the card header and remove the caption and bottom link.
2. Pass `pinked = false` for the categories card.
3. Replace first-use and failed copy; update tests that assert the old text.

### Concrete cases and pitfalls
- The two-pane layout (≥ 600dp) shows the grid beside detail; check the header
  action there too.
- Keep the empty, failed and stale states distinct.

### Verification recipe
Focused Recibo screen tests and `FirstCategoryJourneyTest`, then
`./scripts/verify-ticket SELLO-063 --retain`.

## SELLO-064 — Restyle the month bar and add the past-month ribbon

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-015, SELLO-068, SELLO-004
- **Gate:** G2

### Outcome
The month bar reads **Octubre** with a smaller **2026**, and any past month is
clearly signalled with a one-tap way back.

### Deliverables
Month button styling in `MonthSwitcher` with its catalog example updated, and the
`PastMonthRibbon` from SELLO-068 placed under the month bar on every screen that
uses the shared month.

### Acceptance criteria
- The month button shows the month name large and the year small; at large font the
  year may hide visually but stays in the spoken label.
- While the selected month is not the current one, the ribbon shows **Estás viendo
  septiembre** and **Volver a octubre**; one tap selects the current month.
- The ribbon survives rotation and process death with the selected month, and
  disappears in the current month.
- Back behaviour and the month picker are unchanged.

### Tests
Updated `MonthSwitcherExample`; shell tests for ribbon show/hide and "Volver a";
restoration test; G2.

### Working checkpoint
Choosing September in the month picker shows the ribbon on Recibo; tapping
**Volver a octubre** returns to October.

### Context and starting points
Shared month: `navigation/MonthSession.kt`, `ShellViewModel.kt`; frame:
`SelloScaffold.kt`, `MonthSwitcher` in `designsystem/component/Navigation.kt`;
[app shell guide](../../../development/app-shell.md). The ribbon component itself is
SELLO-068; this ticket owns its placement and behaviour.

### Implementation plan
1. Show the SELLO-068 ribbon from the shell when the selected month differs from
   the current one, wired to the existing select-current-month action.
2. Restyle the month button; keep its spoken label.

### Concrete cases and pitfalls
- The current month comes from the financial clock; a month change at midnight
  must update the ribbon without a restart.
- Resumen and income history share the shell, so they get the ribbon too; that is
  intended by v2.

### Verification recipe
Catalog and shell tests, a manual check of month change and restart, then
`./scripts/verify-ticket SELLO-064 --retain`.

## SELLO-065 — Use the full-width Anotar dock and the v2 tab bar

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-015, SELLO-004
- **Gate:** G2

### Outcome
The dock is one full-width **+ Anotar** button and the selected tab is marked by a
pill behind its icon.

### Deliverables
`EntryDock` and `SelloNavigationBar` changes in `:design-system`, catalog examples,
and the shell updated on every tab.

### Acceptance criteria
- The dock is a single brand button with a + icon and **Anotar**; it opens expense
  entry as today, including from a past month.
- The selected tab shows a `brandSoft` pill behind its icon; selection is exposed to
  accessibility as today.
- Large text keeps both labels readable; targets stay at least 48dp.
- Wide layouts keep a clear Anotar action in the rail.

### Tests
Catalog examples; shell layout and keyboard-inset tests; G2.

### Working checkpoint
Recibo and Resumen show the v2 dock and tab bar on a phone, and the rail layout
still offers Anotar.

### Context and starting points
Today the dock looks like a field ("Anotar un gasto") with a separate Anotar button,
and the selected tab has a line above it. Code: `EntryDock`, `SelloNavigationBar`,
`SelloNavigationRail` in `designsystem/component/Navigation.kt`; tests
`ShellScreenLayoutTest`, `KeyboardInsetsTest`, `AdaptiveScaffoldTest`.

### Implementation plan
1. Change `EntryDock` to a full-width `SelloButton` and update its catalog example.
2. Add the pill indicator to the navigation bar.
3. Check the rail and every shell route.

### Concrete cases and pitfalls
- The dock is reused on Configuración, Ingresos and category detail; check each.
- The keyboard must not push the dock over content.

### Verification recipe
Design-system and shell device tests, then `./scripts/verify-ticket SELLO-065 --retain`.

## SELLO-066 — Replace the saved receipt with brief feedback after a save

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-017, SELLO-021, SELLO-069, SELLO-004
- **Gate:** G2

### Outcome
After a confirmed save the form closes and the caller shows a short confirmation,
e.g. **Gasto anotado: 48.700 en Comida**, with its figures already updated.

### Deliverables
Expense and income creation return to their caller after a confirmed save; the
SELLO-069 confirmation notice above the dock; removal of the full-screen "Gasto anotado /
RECIBIDO" receipt and its **Listo** step.

### Acceptance criteria
- The notice text comes from the saved record read back from storage; nothing is
  announced before the save is confirmed.
- An unknown outcome keeps the form with **Comprobar si se guardó** and shows no
  notice; a definite failure keeps the draft.
- The notice uses ink background and paper text, is announced to TalkBack, needs no
  dismissal and has no undo.
- Income saves show the equivalent notice where income entry was opened.
- Haptics still run only after commit; process death after commit does not repeat
  the save or lose the notice silently.

### Tests
ViewModel tests for the closed-after-commit state and notice text; device journey
tests for expense and income save, unknown outcome and process death; G2.

### Working checkpoint
Saving an expense from Recibo returns straight to Recibo with the notice and the
updated remainder.

### Context and starting points
D01: "Expense and income creation use brief feedback after a confirmed save." Today
saving opens a receipt screen (`ExpenseReceiptView` in `ExpenseEntryState.kt`) that
needs **Listo**. The shell already shows `SelloNotice` for corrections
(`CorrectionNoticeBar` in `navigation/SelloApp.kt`). Tests: `ExpenseEntryJourneyTest`,
`IncomeJourneyTest`, `WalkingSliceJourneyTest`.

### Implementation plan
1. Keep the storage read-back, but turn its result into a navigation result plus
   notice instead of a receipt state.
2. Host the confirmation in the shell beside the correction notice; decide which
   one wins if both are pending and test it.
3. Remove the receipt screen and update journeys.

### Concrete cases and pitfalls
- Do not show a notice for an edit; edits already close after a confirmed update.
- The read-back may fail after commit: the save is still done, so the form must not
  offer a second write.

### Verification recipe
Focused ViewModel tests, then the entry and income journeys on a device, then
`./scripts/verify-ticket SELLO-066 --retain`.

## SELLO-067 — Add the Ajustar presupuesto action to an overspent Recibo

- **Type:** Story
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-062, SELLO-004
- **Gate:** G2

### Outcome
An Excedido month offers **Ajustar presupuesto ›** under the big number, leading to
where the limits can be changed.

### Deliverables
The link in the answer slip for an overspent current month and its navigation.

### Acceptance criteria
- The link appears only with Excedido in the current month, never for a past month.
- It opens the owner-approved destination; existing limit edits keep their
  current/past-month rules.
- It has a 48dp target and a clear spoken label.

### Tests
Screen test for visibility; navigation test for the destination and Back; G2.

### Working checkpoint
From an Excedido Recibo the link reaches the approved limit-editing destination.

### Context and starting points
**Decision input before Ready:** where the link goes. Options: the category editor
for the most overspent category, a list of categories with their limits, or category
detail. The owner chooses; the executor does not guess. ADR 0012 names the link;
the category editor is described in
[category-editor.md](../../../development/category-editor.md).

### Implementation plan
1. Record the decision in this ticket.
2. Add the link and its route; reuse existing editors.

### Concrete cases and pitfalls
- Overspending with no single overspent category is possible (unlimited categories
  count toward the total); the destination must handle it.

### Verification recipe
Focused screen and navigation tests, then `./scripts/verify-ticket SELLO-067 --retain`.

## SELLO-068 — Add the PastMonthRibbon design-system component

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-006, SELLO-004
- **Gate:** G2

### Outcome
`:design-system` has the v2 past-month ribbon, shown in the catalog, ready for the
shell to place under the month bar.

### Deliverables
`PastMonthRibbon` in `designsystem/component`, a `:catalog` example with light/dark,
both inks, font scales 1.0/1.3/2.0 and the compact window, and a component test.

### Acceptance criteria
- It shows a calendar icon, **Estás viendo** with the month name in bold, and a
  **Volver a {month}** button; the app supplies both month names and the action.
- It sits on the band colour with paper text, matches the v2 catalog entry, and
  reflows to two lines at large text without clipping the button.
- The button has a 48dp target; the message is announced politely as status and the
  button has a clear spoken label.
- The component holds no month logic, clock or navigation; it takes plain strings
  and a callback.

### Tests
Catalog example test, semantics and target-size test, large-font layout test; G2.

### Working checkpoint
The catalog app shows the ribbon in all four theme combinations and at font scale
2.0, and its button reports a tap.

### Context and starting points
v2 component catalog: "Past-month ribbon · New · PastMonthRibbon (proposal) ·
Recibo, Resumen, income while a past month is selected". Styling: `.past-ribbon`
in `docs/design/v2/sello.css`; markup: `pastRibbon` in `docs/design/v2/kit.js`.
Reuse `SelloTheme` tokens, `SelloIcon` (the `event` icon) and `SelloButton` styles;
follow existing examples in `catalog/.../StructureExamples.kt`. Placement and
behaviour are SELLO-064.

### Implementation plan
1. Add the composable with a minimal API (`message month`, `return label`,
   `onReturn`, `modifier`).
2. Add the catalog example beside `MonthSwitcherExample`.
3. Add semantics, target-size and large-font tests.

### Concrete cases and pitfalls
- Long month names ("septiembre") plus "Volver a noviembre" must fit at 320dp.
- Do not add a `YearMonth` or clock dependency to `:design-system`.

### Verification recipe
`:design-system` and `:catalog` device tests, then
`./scripts/verify-ticket SELLO-068 --retain`.

## SELLO-069 — Add a confirmation variant to SelloNotice

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-007, SELLO-004
- **Gate:** G2

### Outcome
`SelloNotice` can show the v2 save confirmation: ink background, paper text and a
leading check icon in the gain colour, with no action.

### Deliverables
An optional leading check (or a confirmation kind) on `SelloNotice`, its catalog
example in `NoticeExample`, and component tests; existing notice uses unchanged.

### Acceptance criteria
- The confirmation shows a gain-coloured check icon before the message, e.g.
  "Gasto anotado: 48.700 en Comida"; the icon is decorative and not spoken.
- The message is announced politely; spoken text can carry the full amount.
- Existing undo and plain notices look and behave as before.
- Correct in light and dark themes and at font scale 2.0.

### Tests
Catalog notice example test covering the new variant; semantics test; screenshot or
layout check in dark theme; G2.

### Working checkpoint
The catalog notice example shows the confirmation alongside the existing undo and
plain notices.

### Context and starting points
v2 component catalog: "Toast · Extend · SelloNotice · After a save". Styling:
`.toast` in `docs/design/v2/sello.css` (ink background, paper text, check icon in
gain, darker green in dark theme). Current `SelloNotice` is in
`designsystem/component/Corrections.kt`; its example is `NoticeExample` in
`catalog/.../FeedbackExamples.kt`. Timing and placement belong to SELLO-066.

### Implementation plan
1. Add the optional leading icon to `SelloNotice` without changing its defaults.
2. Extend `NoticeExample` with the confirmation.
3. Add semantics and theme tests.

### Concrete cases and pitfalls
- The check must keep enough contrast on ink in dark theme (v2 uses a darker green).
- Do not add timing or auto-dismiss to the component; the shell owns it.

### Verification recipe
`:design-system` and `:catalog` device tests, then
`./scripts/verify-ticket SELLO-069 --retain`.
