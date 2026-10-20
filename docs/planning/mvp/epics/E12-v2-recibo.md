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
epic. Read dependency links for order: SELLO-068 and SELLO-069 wire the built
components into the app, SELLO-061 must precede SELLO-062 and SELLO-067.

**Components already built (owner decision, 2026-10-17):** the v2 components were
built and reviewed in the catalog first (pull request #54, `catalog-v2-components`):
`AnswerSlip`, `V2MonthBar`, `PastMonthRibbon`, `V2Dock`, `V2NavigationBar`, the
confirmation `SelloNotice`, `V2ChipRows`, `AddChip` and the rest of
`designsystem/component/v2`. Tickets here reuse them instead of building their own,
and SELLO-068 and SELLO-069 are reduced to wiring them into the app. Retiring the v1
components they replace is SELLO-073.

These tickets come from a 2026-10-16 comparison of the running app (`98df571`,
debug build on an API 33 emulator, synthetic data) with v2. The early-month review
invitation ("Ver cómo cerraste octubre ›") is not ticketed: its timing and dismissal
are still open in the Home brief.

## SELLO-061 — Replace the pace verdict with the factual budget status

- **Type:** Story
- **Priority:** P0
- **Status:** Done
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

The owner confirmed BG-E01 and BG-E04 on 2026-10-17; ADR 0012 records the values.

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

### Delivery evidence

- **Revision:** tested snapshot is the uncommitted `sello-061-factual-status` working
  tree on `7f94c75e2957d310c99d7fa54997a3663e176640`; it is identified exactly by the
  retained input fingerprint below. The commit and pull request follow this evidence;
  no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `MonthlyBudgetPolicy.verdict(limit, spent)` compares
  recorded spending with the limit only; `Verdict` is `OnTrack`/`AtLimit`/`Over`
  (`Tight` removed). `MonthlyBudgetPolicyTest` proves BG-E01 (Al día, 600.000 left,
  60.000 a day), BG-E04 (category Over, overall Al día, 19.000 a day), BG-E05 (Al
  límite with 0 a day, then Excedido by 1), B05 (zero limit Al día, then Excedido),
  B08, a category exactly at its own limit (Al límite), unlimited spending pushing the
  month over with no category over (B01), the 393.700 of 750.000 day-16 case (Al día,
  356.300 left, 22.250 a day), and the BG1 boundaries. The forecast stays exact in
  the snapshot (V05 still 1.425.004/15) for Resumen, which keeps its own words and
  `PastVerdict` mapping; `ResumenSnapshotTest` pins that. Recibo and category detail
  read Al día, Al límite, Excedido and, for past months, Cumplido or Excedido, with
  gain/neutral/loss inks (`ReciboRenderModelTest.eachStampHasTheInkOfWhatItSays`) and
  the same words in spoken descriptions. No Recibo or category-detail stamp, string
  or test still uses Justo, Pasado, Bajo el límite or Sobre el límite. D03, the
  example corpus (B04/B05/B08 words; V and F verdicts marked retired, amounts
  unchanged), ARCHITECTURE.md §4 and the snapshot/Recibo guides describe the new rule.
- **Red / Green:** against the pace rule, six domain tests failed for the intended
  reason (`Over` or `Tight` where recorded spending was below or at the limit) while
  every amount and daily figure passed; `ReciboScreenTest` and
  `CategoryDetailScreenTest` failed on the old words (Pasado, Bajo el límite, Sobre el
  límite, Justo). All passed after the change. One render-model expectation changed
  by design: Transporte with 145.000 of 200.000 is Al día, not the projected Justo.
- **Gate results:** `./scripts/verify-ticket SELLO-061 --retain` passed all six
  checks: board validation/tests, quality-runner tests, architecture, host and API-30
  device gates. Host: 519 tests; device: 528 tests; zero failures, errors or skips.
  Lint had zero errors and 32 existing warnings. The first gate run failed only on
  ktlint line length in the new tests; `ktlintFormat` fixed it before the passing run.
- **Quality run:** run 20261020T034516Z-0e179666; SELLO-061 G2 passed; HEAD 7f94c75, inputs sha256 218faf16f8aa; report docs/planning/mvp/quality-reports/SELLO-061.json sha256 3a43f8194d39ac88ff9ee7404a5c4ae6576a078619bbc97cb8d8c0309eb0e4d1
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off, matching the hosted runner), serial `emulator-5582`.
  Focused device tests and a visual check ran on API-31 `test31` (`emulator-5580`):
  on 19 October, a 100.000 Mercado limit with 80.000 recorded shows Al día, 20.000
  left and 1.500 a day (the pace rule would have said Pasado); 20.000 more shows Al
  límite with 0 a day. `WalkingSliceJourneyTest`'s saved-expense journey failed once
  there on a keypad lookup unrelated to stamps and passed when rerun alone and in the
  gate. No personal history or physical device was used; the sandbox (SELLO-024) does
  not exist yet, so no sandbox scenario was extended; no manual TalkBack session or
  hosted CI run is claimed.
- **Review:** executor self-review covered the BG1 table and examples, the current
  versus past at-limit distinction, Resumen's untouched words and forecast, and the
  failed/stale states. Done is proposed by this ticket's pull request; its merge is
  the designated owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge
  or hosted CI pass is claimed.

## SELLO-062 — Rebuild the Recibo answer slip to v2

- **Type:** Story
- **Priority:** P0
- **Status:** Done
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

### Delivery evidence

- **Revision:** tested snapshot is the `sello-062-answer-slip` working tree on `main`
  at `2a0a04b`, with only the ticket status uncommitted; it is identified exactly by
  the retained input fingerprint below. The commit and pull request follow this
  evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `ReciboSummary` now carries one label
  (`ReciboAnswerLabel`: Restante del presupuesto, or Has gastado este mes with no finite
  limit), the signed remainder (`MoneyFormatter.format`, so an overrun is −43.700 and
  is read "menos"), the stamp, `ReciboPerDay` in the current month and
  `ReciboClosedMonth` in a finished one; spent, limit and remainder come from the same
  `LimitedBudget`. `ReciboScreen` draws it with the v2 `AnswerSlip`, which gains an
  optional `description` so its label, stamp and amount are read as one sentence; under
  a tear line the per-day figure ("25.350 por día", 0 included, in loss ink when over,
  as financial example B04 requires) and "N días, incluido hoy" (plural, "1 día" on the
  last day), or "Mes cerrado · gastaste 920.000 de 1.000.000." in a past month. The
  categories and their detail keep their sentences. Debug previews
  (`ReciboPreviews.kt`) show today, an overrun on the last day, no limit and a closed
  month. No goal, reserved-payment line or Ajustar presupuesto link is drawn.
- **Red / Green:** with the render model mapped onto the new summary but keeping the
  unsigned amount and no closing figures, the overrun, finished-month and at-limit
  closed-month tests in `ReciboRenderModelTest` failed; they passed after the change.
  `ReciboScreenTest` covers each state, the one-day and zero per-day cases, spoken
  descriptions and a signed 19-digit remainder at font scale 2.0; `V2ComponentTest`
  covers the slip's single sentence at font scale 2.0 and 320dp.
- **Gate results:** `./scripts/verify-ticket SELLO-062 --retain` passed all six checks.
  Host: 522 tests; device: 544 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. The first run failed `ExpenseEntryJourneyTest`,
  which still read the old no-limit sentence; a second passed with one new lint
  warning from the previews reading resources through the context, fixed by using
  `LocalResources`. The final run followed the HANDOFF update with the same counts.
- **Quality run:** run 20261020T140601Z-92492992; SELLO-062 G2 passed; HEAD 2a0a04b, inputs sha256 505e8e51d212; report docs/planning/mvp/quality-reports/SELLO-062.json sha256 b787734d4975b247a4b46251764f6d16a890aab9b3c941d3c819e5f4ec729fa2
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  with one synthetic category (limit 1.000.000) showed, at font scale 1.0, 83.300 por
  día over 12 days; after an expense of 1.043.700, −43.700 Excedido with 0 por día; and,
  with the emulator clock moved to 3 November, October closed with "Mes cerrado ·
  gastaste 1.043.700 de 1.000.000." and November at 35.700 por día over 28 days. At
  font scale 1.3 and 2.0 the label and stamp stacked and nothing was cut off. No
  personal history or physical device was used; no manual TalkBack session or hosted
  CI run is claimed.
- **Review:** executor self-review covered the shared category wording, the snapshot
  source of the closing line, the sign in speech and 12+ digit amounts at large text.
  Done is proposed by this ticket's pull request; its merge is the designated owner
  acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is
  claimed.

## SELLO-063 — Update the Recibo categories card and first-use and failed states

- **Type:** Story
- **Priority:** P0
- **Status:** Done
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

### Delivery evidence

- **Revision:** tested snapshot is the uncommitted `sello-063-categories-card` working
  tree on `26d97c2f8dc55b2c0168b4984d19a18ee33c6e63`; it is identified exactly by the
  retained input fingerprint below. The commit and pull request follow this evidence;
  no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `ReciboScreenTest.theCategoriesCardHeaderCarriesTheOnlyAddButton`
  proves the **+** sits on the **Tus categorías** line above the grid, is named
  "Agregar categoría", has a 48dp target, opens category creation, and that the
  caption and the link under the grid are gone. `theFirstRunOffers…` and
  `aFailureOffersRetryAndShowsNoAmount` prove the v2 first-use and failed-read copy,
  that each never shows as the other, and that a failure shows no amount;
  `aLongPressOrAScreenReaderActionOnACategoryOffersToEditIt…` still passes. The
  categories card keeps `pinked = false` with a comment; only the answer slip is
  pinked. `ReciboListDetailWideTest` proves the header **+** is present and clickable
  beside category detail at 900 × 700dp. `FirstCategoryJourneyTest`,
  `WalkingSliceJourneyTest`, `ExpenseEntryJourneyTest`, `ResumenJourneyTest` and
  `ShellOnDeviceTest` pass through the new first-use action. In `:design-system`,
  `EmptySlip` gains an overload with a required `icon` and `ErrorSlip` an optional
  `icon`; both centre their text under a decorative icon only when given one, and
  `FeedbackInteractionTest.withAnIconTheStateSlipsCentreTheirTextAndKeepTheIconSilent`
  proves the centring, the silent icon and the 48dp retry. The catalog's V2 states
  example uses the new look.
- **Red / Green:** before the change, `theCategoriesCardHeaderCarriesTheOnlyAddButton`
  failed finding no node described "Agregar categoría", and the updated first-use and
  failed-read tests failed on the old copy ("Tu recibo está en blanco", "No pudimos
  leer tus datos de este mes."); all passed after it. Adding `icon` after `onAction`
  first broke existing trailing-lambda and positional `EmptySlip` callers at compile
  time; the original signature was restored and the icon moved to an overload.
- **Gate results:** `./scripts/verify-ticket SELLO-063 --retain` passed all six
  checks: board validation/tests, quality-runner tests, architecture, host and API-30
  device gates. Host: 511 tests; device: 528 tests; zero failures, errors or skips.
  Lint had zero errors and 32 existing warnings.
- **Quality run:** run 20261017T181528Z-48779804; SELLO-063 G2 passed; HEAD 26d97c2, inputs sha256 e7c8bac6467b; report docs/planning/mvp/quality-reports/SELLO-063.json sha256 40841b45e4f8f94f0036fe73df4df2ca69d07b3a1972eaf2e1ae812a8d1b92d7
- **Device / Artifact:** the gate ran on an isolated synthetic API-30 AVD (`ci30`,
  320 × 640dp, mdpi, animations off, matching the hosted runner), serial
  `emulator-5582`. Focused tests and a visual check ran on an API-31 AVD (`test31`,
  `emulator-5580`): a fresh install showed the v2 first-use slip, and creating a
  category from it showed the **Tus categorías** header with its **+**. No personal
  history or physical device was used; no manual TalkBack session or hosted CI run is
  claimed.
- **Review:** executor self-review covered the copy against the v2 Recibo mockup, the
  distinct empty/failed/stale states, the two-column layout and source compatibility
  of the design-system change. Done is proposed by
  [PR #56](https://github.com/Okabe94/Sello/pull/56); its merge is the designated
  owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI
  pass is claimed.

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
The selected-month behaviour behind the SELLO-068 frame: the ribbon shown under the
month bar on every screen that uses the shared month while a past month is selected,
and **Volver a** selecting the current month.

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
SELLO-068; this ticket owns when it shows and what it does.

### Implementation plan
1. Show the SELLO-068 ribbon from the shell when the selected month differs from
   the current one, wired to the existing select-current-month action.
2. Check the SELLO-068 month bar keeps the spoken label and opens the same picker.

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
The shell using `V2Dock` and `V2NavigationBar` (built in pull request #54) on every
tab, with the rail keeping its Anotar action. `EntryDock` and `SelloNavigationBar`
are retired in SELLO-073.

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
1. Replace `EntryDock` with `V2Dock` and `SelloNavigationBar` with `V2NavigationBar`
   in `SelloScaffold`.
2. Check the rail and every shell route.

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
An Excedido month offers **Ajustar presupuesto ›** under the big number, which brings
Recibo's categories card into view, where each category opens its detail and limit
editor.

### Deliverables
The link in the answer slip for an overspent current month, scrolling Recibo to its
categories card and moving accessibility focus to the card's heading.

### Acceptance criteria
- The link appears only with Excedido in the current month, never for a past month.
- Tapping it scrolls Recibo so the **Tus categorías** heading is at the top of the
  list and gives it accessibility focus; nothing navigates away.
- In a two-column layout where the card is already visible, it still moves focus to
  the heading.
- It has a 48dp target and a clear spoken label, such as "Ajustar presupuesto, ir a
  tus categorías".
- Existing limit edits keep their current/past-month rules.

### Tests
Screen test for visibility; scroll-position and focus test; two-column test; G2.

### Working checkpoint
From an Excedido Recibo the link brings the categories card into view, and a
category opens its limit editor as today.

### Context and starting points
**Decision (owner, 2026-10-17): option D**, Recibo's own categories card. It needs no
new screen and works when no single category is over its limit. A dedicated list of
limits was considered and left for later. ADR 0012 names the link; the category
editor is described in [category-editor.md](../../../development/category-editor.md).

### Implementation plan
1. Add the link to the SELLO-062 answer slip for Excedido in the current month.
2. Scroll Recibo's list to the categories card and request focus on its heading.

### Concrete cases and pitfalls
- Overspending with no single overspent category is possible (unlimited categories
  count toward the total); the card shows every category, so the link still helps.
- A failed or loading categories card must still be reachable without a crash.

### Verification recipe
Focused screen tests, then `./scripts/verify-ticket SELLO-067 --retain`.

## SELLO-068 — Wire the v2 month bar and past-month ribbon into the app frame

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-006, SELLO-004
- **Gate:** G2

### Outcome
The app frame draws its month title with the `V2MonthBar` and has a place for the
`PastMonthRibbon` under it, both built in pull request #54.

### Deliverables
`SelloScaffold` using `V2MonthBar` (month, small year, chevron, settings action) in
place of `MonthSwitcher`'s title, an optional ribbon slot that shows
`PastMonthRibbon` on the band, the catalog scaffold example updated and frame tests.

### Acceptance criteria
- Every tab with the shared month shows the v2 month bar; tapping it opens the same
  month picker, and its spoken label still names the month and year.
- When the caller passes a ribbon, it sits on the band under the month bar; without
  one, nothing changes.
- Large text and narrow windows keep the month button and actions reachable.
- `PastMonthRibbon` and `V2MonthBar` hold no month logic or clock; SELLO-064 decides
  when the ribbon shows.

### Tests
Scaffold layout and semantics tests; catalog example test; G2.

### Working checkpoint
Recibo and Resumen show the v2 month bar on a phone; the catalog scaffold shows the
ribbon slot filled.

### Context and starting points
The components and their tests exist in `designsystem/component/v2/V2Controls.kt`
(`V2MonthBar`, `PastMonthRibbon`) and `V2ComponentTest`. The frame is
`SelloScaffold.kt`; `MonthSwitcher` is in `designsystem/component/Navigation.kt`.

### Implementation plan
1. Draw the title with `V2MonthBar` in `SelloScaffold`, keeping the picker action.
2. Add the ribbon slot and its catalog example.

### Concrete cases and pitfalls
- Pushed screens with a title (Configuración, income history) keep their title bar;
  `V2TitleBar` exists for tab titles.
- `MonthSwitcher` itself is retired in SELLO-073 once nothing uses it.

### Verification recipe
`:design-system`, `:catalog` and shell device tests, then
`./scripts/verify-ticket SELLO-068 --retain`.

### Delivery evidence

- **Revision:** tested snapshot is the `sello-068-v2-month-bar` working tree on its
  merge of `main` at `2694b16` (bringing in #59, Gradle caching, with no overlap), with
  only the ticket status uncommitted; it is identified exactly by the retained input
  fingerprint below. The commit and pull request follow this evidence; no claim
  is made that the gate tested a clean commit.
- **Requirement mapping:** `SelloScaffold` gains optional `topBar` and `ribbon` slots:
  a top bar replaces the title row below the status bar, a ribbon sits on the band
  under it, and the band grows by their measured height so content still starts 50dp
  on the band (`SCAFFOLD_BAND_TAG`). Without them nothing changes. `V2MonthBar` gains
  `buttonModifier` for its month button. On Recibo and Resumen (no back arrow or
  screen title) `ShellScreen` draws `V2MonthBar` with `MonthNames.name`, the year,
  the existing "Cambiar de mes. Mes actual/seleccionado: …" label from
  `monthBarLabel`, the same picker action and the settings action; pushed screens
  (Configuración, income history, a category opened on a phone) keep their titled
  `MonthSwitcher` bar. `V2MonthBar` and `PastMonthRibbon` hold no month logic.
  `V2FrameScaffoldTest` proves the replacement, the ribbon order, the band overlap
  with and without a ribbon and at font scale 2.0, and a single picker tap;
  `ShellScreenLayoutTest`, `MonthSwitcherLabelTest`, `ShellOnDeviceTest` and
  `SettingsJourneyTest` read the month from the button's spoken label, because the bar
  speaks as one button. The catalog scaffold example shows the v2 bar with its ribbon
  slot filled and emptied (`V2CatalogInteractionTest`).
- **Red / Green:** with the slots stubbed, the replacement, ribbon and month-button
  tests failed; on the old shell the tab-title test failed finding the one-line
  "Agosto 2026" title. All passed after the change.
- **Gate results:** `./scripts/verify-ticket SELLO-068 --retain` passed all six checks.
  Host: 522 tests; device: 541 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. The first gate run failed two new frame tests that
  measured from the screen top in a window larger than the 320 × 640 screen; they now
  measure from the window, as `AdaptiveScaffoldTest` does. After merging `main` (#59)
  the gate was run again and passed with the same counts.
- **Quality run:** run 20261020T052148Z-7ece84c2; SELLO-068 G2 passed; HEAD 2694b16, inputs sha256 ababa56475c2; report docs/planning/mvp/quality-reports/SELLO-068.json sha256 9d43f499ccd1969be5096e4f0f7c6bd13bf5b723c971398865bdff0b8be5b5e1
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On API-31 `test31`
  (`emulator-5580`) the full `:app` device suite passed (212 tests) and screenshots
  showed the v2 bar on Recibo and Resumen, the slip starting on the band, and
  Configuración keeping its titled bar. There, `V2CatalogInteractionTest`'s
  `expenseCategoriesEndWithANuevaButtonThatIsNotAChoice` fails because the tall
  screen leaves nothing to scroll; it passes on `ci30` and is unrelated to this
  ticket. No personal history or physical device was used; no manual TalkBack session
  or hosted CI run is claimed.
- **Review:** executor self-review covered the band overlap, the unchanged spoken label
  and picker, pushed screens and the rail. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

## SELLO-069 — Wire the confirmation notice into the shell

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-007, SELLO-004
- **Gate:** G2

### Outcome
The shell can show `SelloNotice`'s confirmation look (built in pull request #54)
above the dock, ready for SELLO-066 to use after a save.

### Deliverables
The shell's notice slot accepting a confirmation notice, placed above the dock on
every tab and beside the rail in wide layouts, with a shell test.

### Acceptance criteria
- A confirmation shows the gain check before the message, e.g. "Gasto anotado:
  48.700 en Comida"; the check is decorative and not spoken.
- The message is announced politely; spoken text can carry the full amount.
- Existing undo and plain notices look and behave as before.
- Correct in light and dark themes, at font scale 2.0 and with the keyboard closed.

### Tests
Shell notice placement and semantics tests in both themes; G2.

### Working checkpoint
A test host shows a confirmation above the dock on Recibo and Resumen.

### Context and starting points
`SelloNotice(confirmation = true)` is in `designsystem/component/Corrections.kt` with
its catalog example in the V2 feedback examples. The shell's notice slot is the
`notice` parameter of `SelloScaffold`. Timing and text belong to SELLO-066.

### Implementation plan
1. Pass confirmation notices through the shell's notice slot.
2. Add placement and semantics tests.

### Concrete cases and pitfalls
- Do not add timing or auto-dismiss to the component; the shell owns it.
- The notice must not cover the dock or the tab bar.

### Verification recipe
`:design-system` and shell device tests, then
`./scripts/verify-ticket SELLO-069 --retain`.

### Delivery evidence

- **Revision:** tested snapshot is the uncommitted `sello-069-confirmation-notice`
  working tree on `925deb0b1b865bb3e026860c6e21816513882e8a`; it is identified exactly
  by the retained input fingerprint below. The commit and pull request follow this
  evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `CorrectionNotice.Confirmed(message, spoken)` and
  `ExpenseCorrections.confirmed()` pass a confirmed save through the shell's one notice
  holder, which owns the timing (four seconds, monotonic clock); `SelloNotice` gains
  no timer. `CorrectionNoticeBar` draws it as `SelloNotice(confirmation = true)` in the
  scaffold's existing notice slot. Owner decision (2026-10-19): the newest notice wins,
  so a confirmation takes the place of an offer to undo and that deletion stands.
  `ExpenseCorrectionsTest` proves the four seconds and dismissal, newest-wins over an
  undo offer (undo then restores nothing) and a later notice replacing it.
  `ConfirmationNoticeShellTest` proves, on Recibo and Resumen tabs, in light and dark
  themes and at font scale 2.0, that the notice sits above the dock and tab bar, its
  check is drawn and silent and the message is announced politely with its spoken
  text; beside a rail (1000 × 700dp) it sits over the content. Existing correction
  notices are unchanged: `ExpenseCorrectionJourneyTest`, `IncomeJourneyTest` and the
  shell tests pass untouched. `SelloNotice`'s check gained the `SELLO_NOTICE_CHECK_TAG`
  test tag, nothing else.
- **Red / Green:** with the API stubbed (`confirmed()` doing nothing, the shell drawing
  the plain look), the three new holder tests failed (no notice; an undo offer still
  restored) and every new shell test failed on the missing check while placement
  already passed. All passed after the change.
- **Gate results:** `./scripts/verify-ticket SELLO-069 --retain` passed all six checks.
  Host: 522 tests; device: 533 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. The first gate run failed: 20 tests across
  `:catalog`, `:design-system` and `:app` found no Compose hierarchy (the emulator was
  not in the foreground; it was woken and kept awake), and the rail case asserted the
  check visible in a window larger than the 320 × 640 screen; it now asserts existence
  and position, as the other oversized-window shell tests do.
- **Quality run:** run 20261020T042037Z-14ec9578; SELLO-069 G2 passed; HEAD 925deb0, inputs sha256 c8207ee11893; report docs/planning/mvp/quality-reports/SELLO-069.json sha256 f7a234c3258c15bf648f2aa08d6879fb3058158da3167e7dd3a8c7e42fabd43e
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`; focused device tests ran on
  API-31 `test31` (`emulator-5580`). Nothing in the app sends a confirmation until
  SELLO-066, so there is no in-app visual change to check. No personal history or
  physical device was used; no manual TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered unchanged existing notices, placement on
  every tab and beside the rail, both themes and large text. Done is proposed by this
  ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.
