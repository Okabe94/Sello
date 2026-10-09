# SELLO-E02 — Sello design system and component catalog

- **Status:** In Progress
- **Goal:** Build the actual reusable visual language and a separately runnable UI laboratory.
- **Exit:** Shared primitives cover MVP screens, all themes/states are browseable by type, and app release has no catalog dependency.

## SELLO-006 — Implement Sello tokens and the independent catalog shell

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-001, SELLO-003, SELLO-004
- **Gate:** G2

### Outcome
Make typography, colors, spacing, shapes and motion authoritative reusable assets.

### Deliverables
SelloTheme with Cobalto/Violeta × light/dark, Material mappings, semantic gain/loss
tokens, bundled licensed fonts and Rounded symbols. Catalog home grouped by component
type with local theme/font/window controls; stable example IDs and token previews.

### Acceptance criteria
- No dynamic Material palette silently replaces Sello's inks; semantic meanings
  remain unchanged across inks. Stencil type is limited to stamps.
- Catalog uses its own launcher/application ID, installs beside app and accesses no
  financial repository, production DI, storage/network permission or customer data.
- Catalog controls and fixtures are not part of the shared component runtime API.

### Tests
Theme/token mapping and contrast checks, four-scheme previews, catalog launch/theme
interaction UI tests and dependency inspection. Run G2 for catalog/theme boundaries.

### Working checkpoint
App renders through the same theme; catalog is a real independent executable,
not documentation screenshots or duplicated composables.

### Context and starting points
The template theme is not Sello and per-screen styling would fork immediately.
Use modules/IDs from SELLO-003, accepted D01/D02 and
[foundations](../../../design/sello-spec.html#foundations). Read architecture §6,
the guide's module map and Compose testing/accessibility sources.

### Implementation plan
1. Inventory the reference's four color schemes/type/spacing/shape/motion tokens;
   implement typed providers in design-system/theme with Material mappings.
2. Bundle reviewed fonts/icon assets and license notices; remove template palette
   ownership after app points to the new theme. No runtime font/network fetch.
3. Add catalog home/routes grouped by component type, stable example IDs and local
   theme/font/window selection. Initially show implemented foundations only.
4. Add token/type samples, long monetary text samples and contrast checks in all
   schemes. Keep catalog controls/fixtures in catalog, not the reusable theme API.
5. Verify independent launch/package permissions and apply the theme in the existing
   customer screen; preference persistence comes later in SELLO-023.

### Concrete cases and pitfalls
- Switching Cobalto to Violeta changes brand ink, not gain/loss meaning.
- Saira Stencil One is for stamps; minimum readable text is 12sp. System theme
  default must not override selected ink or introduce dynamic Material colors.
- Browser Google Fonts loading is not proof app assets are bundled/licensed;
  test actual APK resources and offline launch.

### Verification recipe
Create `*SelloThemeTest` and catalog launcher/control UI tests. Run catalog host
tests/assembleDebug plus catalog connected tests and G2; record four-scheme previews,
contrast results, bundled assets/licenses and distinct application IDs.

### Execution progress
2026-10-09: started on branch `sello-006-tokens-catalog-shell` after SELLO-005 merged.
The owner chose to ship Material Symbols Rounded as individual vectors, not the
15.2 MB icon font. The two text fonts were already fixed by the architecture.

Delivered in `:design-system`: `SelloColors` for Cobalto/Violeta × light/dark with a
shared `semantic` group, the full Material mapping, `SelloTypography` plus stamp-only
`SelloType`, `SelloShapes`, `SelloSpacing`, `SelloMotion`, `SelloIcon` with a starter
set of ten icons, the two bundled fonts and three licence texts
(`design-system/THIRD_PARTY_NOTICES.md`). `SelloTheme(ink, darkTheme)` no longer has a
dynamic-colour path. Delivered in `:catalog`: a home grouped by component type, six
foundations examples with stable IDs, and local ink, mode, font-scale and window-width
controls. The customer app's placeholder screen renders through the same theme.

Choices made during the work, for owner review. Dark `errorContainer` uses the
documented `lossSoft` value `#5A171B`; the reference's stylesheet also carries an
unused `#6B1A1F`. Material slots the reference does not name (fixed, inverse, tint,
low/highest containers) are fed from the nearest Sello token so none keeps a Material
baseline colour. Roles the type table omits (`headlineLarge`, `headlineMedium`,
`titleSmall`, `bodySmall`) are interpolated in Schibsted Grotesk. Selecting a weight
from the variable font needs Compose's experimental `Font(variationSettings)` API in
BOM 2026.02.01, opted into in one place. The only dependency change is Compose UI test
for the catalog's device tests; no existing locked version moved.

Left for later tickets: reduced-motion handling and the tightened thousands point
belong to the components that animate and print money (SELLO-007); the status bar does
not follow the catalog's own light/dark choice until the scaffold exists (SELLO-009);
saving the chosen ink is SELLO-023. Catalog width presets cover compact, medium and
expanded; short-landscape is checked by rotating the device.

2026-10-09: owner approved pull request 4; moved to Done for a squash merge. The catalog
and debug app were also installed on the owner's phone (Android 16, API 36) over USB.

### Delivery evidence
- **Revision:** branch `sello-006-tokens-catalog-shell`; tested snapshot is commit `0af2b5a`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** no dynamic palette replaces the inks → `SelloTheme` has no dynamic-colour path and `SelloMaterialMappingTest.noMaterialSlotFallsBackToAColourOutsideSello` reads every colour slot of the pinned Material version; device `BundledAssetsTest.themeProvidesTheChosenSchemeToSelloAndMaterialReaders`. Semantic meaning unchanged across inks → `SelloColorsTest.changingInkChangesBrandButNeverMeaning` and device `CatalogShellTest.switchingInkChangesBrandButNotGainOrLoss`. Stencil limited to stamps, 12sp minimum → `SelloTypographyTest`. Contrast → `SelloColorsTest.everyReferencePairMeetsItsContrastNeed` over all four schemes with a helper checked against the reference's published ratios. Own launcher and application ID, no platform permission → `CatalogIdentityTest`, `CatalogIsolationTest`; no repository or production DI → architecture rule `module-edge` allows `:catalog` to depend on `:design-system` only. Controls and fixtures outside the shared API → they live in `catalog/src/main`; `:design-system` exposes none. Stable example IDs → `CatalogExamplesTest`. Bundled, licensed, offline assets → `BundledAssetsTest` loads both fonts, every icon and the three licence texts from the package.
- **Red / Green:** the slot test failed first on real behaviour: twelve Material fixed slots still held baseline purples; mapping them made it pass. Mutations of the finished code each failed the expected tests and were reverted: a Violeta-only green, one wrong brand hex, a low-contrast outline, an unmapped surface slot, 11sp label text, stencil in a title role, tabular figures removed, a changed stamp spring; on the device, ignoring the ink and font controls and adding the INTERNET permission failed 5 of 10 catalog tests.
- **Gate results:** local `./scripts/verify-ticket SELLO-006 --gate G2` passed: 45 host tests (29 app, 13 design-system, 3 catalog), 19 device tests on an isolated API 30 emulator (5 app, 4 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 22 warnings, all in `:app` and of kinds present before this ticket.
- **Quality run:** run 20261009T145645Z-a629f308; SELLO-006 G2 passed; HEAD 0af2b5a, inputs sha256 990bcc176aae; report docs/planning/mvp/quality-reports/SELLO-006.json sha256 ddfa52c5b9269d923bca6bfdae612b6da08462f173c2058442d27a31eb440b82
- **Device / Artifact:** four-scheme captures from the installed catalog on the API 30 emulator are in `docs/testing/evidence/SELLO-006/`. The unsigned release APK of the customer app contains `font/schibsted_grotesk`, `font/saira_stencil_one`, the three `raw/license_*` texts and twelve `ic_sello_*` drawables per `aapt2 dump resources`; the merged catalog manifest requests only AndroidX's own package-scoped receiver permission. Application IDs: `com.software.sello`, `com.software.sello.debug`, `com.software.sello.catalog`. Hosted run 37948149714 on the pull request's merge commit passed the `quality` check with the ticket in Review.
- **Review:** executor self-review of the diff, lock-file changes, reports and captures. Project owner reviewed pull request 4 and approved on 2026-10-09, including the choices listed under Execution progress. This is owner acceptance, not an independent technical review; GitHub does not let the account that opened a pull request approve it.

## SELLO-007 — Deliver paper, exact money and feedback components

- **Type:** Story
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-006, SELLO-010, SELLO-004
- **Gate:** G2

### Outcome
Screens can present financial answers, loading, emptiness and failures consistently.

### Deliverables
Slip/pinked shape, TearLine, LeaderLine, TotalLine, KeyValue, MoneyText, Stamp,
ConfirmSlip, EmptySlip, ErrorSlip and SlipSkeleton; resource-based shared `es-CO`
formatting in app presentation and simple exact render inputs in design-system.
Add grouped previews/catalog fixtures for every applicable state.

### Acceptance criteria
- Amounts retain exact values and currency-aware semantics; abbreviations never
  replace receipt/list values. True minus and tabular digits follow the reference.
- Large Long-boundary amounts wrap without clipping at font scale 2.0; stamps
  announce ordinary words and reduced motion exposes the final answer immediately.
- Error/retry/busy/confirmation interactions have accessible roles and 48dp targets.

### Tests
Money formatting boundaries, semantic-node/currency assertions, reduced-motion
and callback UI tests in four themes; visual review of pinked edges/tear lines.

### Working checkpoint
Catalog demonstrates real working components; the existing app still renders.
No component depends on domain/entity types or performs a financial write.

### Context and starting points
These primitives make financial UI and errors consistent before entry screens exist.
Read [paper/money/stamps](../../../design/sello-spec.html#c-slip), architecture §6,
the Money policy/tests from SELLO-010 and tokens from SELLO-006.
Shared application formatting and design-system rendering have different owners.

### Implementation plan
1. Create paper/tear/leader/total/key-value and stamp APIs using theme tokens and
   modifiers; implement pinked clipping/ripple, not hand-drawn copies in screens.
2. Add exact `es-CO` formatter in app/presentation and stateless formatted-money
   inputs in design-system. Preserve currency/sign/accessibility separately from geometry.
3. Implement empty/skeleton/error/retry/confirm/busy feedback with ordinary semantic
   roles and required callbacks; decorative stamps cannot trigger writes.
4. Register synthetic catalog examples and previews for empty/large/negative amounts,
   long labels, retry/confirmation actions, reduced motion and four color schemes.
5. Validate large-font wrapping, divider spacing, hit areas and reader order; app
   still launches through shared components without a repository dependency in catalog.

### Concrete cases and pitfalls
- Display COP 1,234 as full grouped value; a value near Long maximum must never be
  converted to Float for its label. Negative uses true minus and announces currency.
- Confirm cancellation invokes only cancellation; ErrorSlip retry invokes exactly
  its callback. One visual success stamp cannot imply a fake committed receipt.
- Reduced motion exposes exact final text immediately; no financial value exists
  only midway through a count animation. Clip pinked shapes, not text/content bounds.

### Verification recipe
Create/run `*MoneyFormatterTest`, `*MoneySemanticsTest` and feedback interaction UI
tests, then G2 for design-system/catalog. Review reference shape/spacing screenshots
at scales 1.0/1.3/2.0; list every implemented example ID in delivery evidence.

### Execution progress
2026-10-09: started on branch `sello-007-paper-money-feedback` after SELLO-010 merged.
No new product decision or dependency was needed.

Delivered in `:design-system` (`component`): `Slip` with `PinkedBottomShape`,
`TearLine`, `LeaderLine`, `TotalLine`, `KeyValue`, `MoneyText`, `CountingMoneyText`,
`Stamp`, `ConfirmSlip`, `EmptySlip`, `ErrorSlip`, `SlipSkeleton` and a minimal
`SelloButton`, which the feedback slips need before SELLO-008 extends it. `SelloTheme`
now takes `reducedMotion`, defaulting to the system setting. Delivered in `:app`
(`presentation.money`): `MoneyFormatter` with resource-backed `ResourceMoneyLabels`.
The design system draws a pre-formatted `MoneyTextValue` and knows nothing of `Money`.
The catalog gained a Motion control and these example IDs: `paper.slip`,
`paper.tear-line`, `paper.leader-line`, `paper.total-line`, `paper.key-value`,
`stamps.stamp`, `money.money-text`, `money.counting`, `inputs.button`,
`feedback.confirm-slip`, `feedback.empty-slip`, `feedback.error-slip`,
`feedback.slip-skeleton`.

Choices made during the work, for owner review:
- Tabular figures now apply to the display, headline and title roles and to money and
  receipt values, not to body and label text. In Schibsted Grotesk the feature also
  widens points, commas and colons, which visibly broke sentences; the reference's own
  stylesheet applies it to figures only. This revises a SELLO-006 token.
- `MoneyText` takes a formatted value, where the reference sketch took a `Long`, so
  formatting stays in the application as this ticket requires. Counting takes the
  application's formatter as a parameter.
- A figure too wide for its place wraps after a thousands point. Hero first steps down
  to Total, as the reference says.
- Spoken amounts use `peso`/`pesos`, `menos` and `más`. Non-COP currencies format with
  two decimals and their code but have no MVP use.
- The empty and error slips use an outlined button, keeping one filled button per screen.
- The confirmation has a `busy` state so its action cannot be requested twice.

2026-10-09, owner review of the running catalog: the owner chose to show pesos with no
currency symbol and other currencies as code, space, figure (`USD 2.340,00`), recorded
as [ADR 0007](../../../decisions/0007-no-peso-symbol.md). This supersedes the
reference's raised peso sign. The tests were changed first and six failed before the
formatter and `MoneyText` were updated; the gate was run again on the result.

2026-10-09: owner approved pull request 6, including the choices above; moved to Done
for a squash merge.

Found and fixed during the work: the device test caught `BigInteger.longValueExact`,
which does not exist on API 30. Some escape sequences in Kotlin sources, including two
SELLO-010 test files, had been saved as the literal invisible characters; they are
escapes again, with no behaviour change.

Left for later: the customer app does not call the formatter or components yet (first
slice, E04). `SlipSkeleton` leaves the 150 ms show delay to its caller. The reduced
motion setting is read when the theme is composed, not observed while running. The
snackbar with undo and `SlipHeading` are not in this ticket's deliverables.

### Delivery evidence
- **Revision:** branch `sello-007-paper-money-feedback`; tested snapshot is commit `16c5c62`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** exact values, currency-aware semantics, no abbreviation, true minus → `MoneyFormatterTest` (hand-typed tables from 0 to both Long limits, signs, singular, a scale-2 currency), device `ResourceMoneyLabelsTest` for the real wording, `MoneyTextRenderTest` and device `MoneySemanticsTest.amountsAnnounceTheFullFigureWithCurrencyAndDrawATrueMinus`. Long-boundary amounts wrap without clipping at font scale 2.0 → `theLargestAmountWrapsInsideANarrowSlipAtDoubleFontSize` in a 320dp slip. Stamps announce ordinary words → `PaperAndStampTest`. Reduced motion exposes the final answer at once and no value exists only mid-count → `withReducedMotionANewAmountIsShownAtOnce`, `aCountNeverHidesTheFinalAmountFromAScreenReaderAndEndsExact`, and host `countFrame` tests at the Long limits. Roles, callbacks and 48dp targets → `FeedbackInteractionTest` (retry once per press, cancel only cancels, confirm only confirms, back cancels, busy blocks both) and the action line in `PaperAndStampTest`. No component depends on domain types or writes → `:design-system` has no `:domain` edge under the architecture rules. Scheme-parameterised device tests run their assertions in all four schemes.
- **Red / Green:** against naive versions, 5 of 5 formatter tests failed (comma grouping, no sign, wrong symbol and wording, `Math.abs` of the lowest Long) and 3 design-system tests failed (floating-point count frames wrong at the limits, no break points, partial teeth); all pass with the real code. A fourth, on tightening, first failed on a float comparison in the test itself and was corrected. Five mutations of the finished components each failed the expected device tests and were reverted: cancel also confirming, a single-line hero figure, stamp read in capitals, reduced motion ignored, a loading button left enabled.
- **Gate results:** local `./scripts/verify-ticket SELLO-007 --gate G2` passed: 95 host tests (35 domain, 34 app, 23 design-system, 3 catalog), 35 device tests on an isolated API 30 emulator (6 app, 19 design-system, 10 catalog, which open every example by ID), ktlint, architecture rules, lint with 0 errors and 22 warnings, all in `:app` and present before this ticket. No dependency or lock file changed.
- **Quality run:** run 20261009T212536Z-8540c6b2; SELLO-007 G2 passed; HEAD 16c5c62, inputs sha256 9bf67f3c73f3; report docs/planning/mvp/quality-reports/SELLO-007.json sha256 24a408b53676e5b2364906ffa12828322892cf94b2e46f9f046314e0980228c8
- **Device / Artifact:** twelve captures of the installed catalog on the API 30 emulator are in `docs/testing/evidence/SELLO-007/`, including the pinked edge, tear-line notches, the confirmation over its scrim and money at font scale 2.0 in Violeta dark. Font scale 1.3 was neither captured nor separately tested; scales 1.0 and 2.0 were. Hosted run 37993487360 on the pull request's merge commit passed the `quality` check with the ticket in Review.
- **Review:** executor self-review of the diff, captures against the reference, reports and logs. Project owner tried the catalog on an emulator, asked for the currency-mark change, reviewed pull request 6 and approved on 2026-10-09. This is owner acceptance, not an independent technical review; GitHub does not let the account that opened a pull request approve it.

## SELLO-008 — Build accessible amount-first inputs and entry states

- **Type:** Story
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-006, SELLO-010, SELLO-004
- **Gate:** G2

### Outcome
Provide the shared keypad/chips/fields/buttons needed for expense and income entry.

### Deliverables
Amount keypad, date/category/source chips, bounded text fields, switches and busy
buttons, plus reusable validation-display contracts. Catalog examples demonstrate
editing, invalid paste, missing selections, disabled/busy and large-font states.

### Acceptance criteria
- Controls display explicit validation; they never sanitize `1e3`, a negative
  sign or ambiguous separators into another amount. Commands still validate later.
- Hardware keyboard/Switch Access focus follows reading order. System keyboard
  entry keeps the focused field visible; hit targets meet the minimum.
- Empty amount is a draft, not a valid zero-money command; double taps are preventable
  visually but eventual database uniqueness remains required.

### Tests
Keypad/input boundary tests and Compose tests for callbacks, focus, paste rejection,
busy/disabled semantics and scale 2.0; run G2 on relevant catalog/device states.

### Working checkpoint
Inputs are locally interactive in catalog without a repository or fake saving service.

### Context and starting points
Expense and income must share controls without sharing an unsafe universal form.
Inputs are defined at [keypad](../../../design/sello-spec.html#c-amountkeypad) and
the following input components. Use SELLO-010 validators and approved D05 field
limits, not invented max digits/note lengths.

### Implementation plan
1. Build keypad/backspace/chips/date/note/field/switch/button controls as stateless
   values/actions; actual drafts and business validation stay with consuming features.
2. Route keyboard/paste input through the original-input validator; retain invalid
   text and typed error rather than filtering characters into a different amount.
3. Implement required/busy/disabled/focused states and category-chip wrapping/scrolling;
   request focus/keyboard behavior only at the appropriate Root/control boundary.
4. Add catalog-local interactive drafts and reset controls; enforce hardware keyboard
   and Switch Access focus order with minimum hit boxes.
5. Add previews for absent category, max digits, note error and keyboard-visible layout;
   do not add recurrence or investment controls merely because reference shows them.

### Concrete cases and pitfalls
- Pasting `1e3` must stay invalid, not become 13; deleting all digits returns an
  incomplete draft, not a valid zero expense. D05 resolves keypad versus import range.
- Busy button cannot emit a second confirm action; this UI guard does not replace
  future database operation uniqueness. Chips cannot trap focus below the keyboard.
- At font scale 2.0 a tall hit box is required even for visually short chips.

### Verification recipe
Create/run `*AmountInputTest` and `*EntryControlsTest` for validation/callback/focus
boundaries; run design-system/catalog G2. Capture keyboard/paste and large-font
examples using the guide's device procedure, with no financial repository involved.

### Execution progress
2026-10-09: started on branch `sello-008-entry-inputs` after SELLO-007 merged. No new
product decision or dependency was needed.

Delivered in `:domain`: `AmountDraft` (`Empty`, `Digits`, `Rejected`) with keypad and
paste rules built on `CopAmountInput` and `TransactionAmount`. Delivered in
`:design-system`: `AmountKeypad`, `AmountField`, `SelloChip`, `ChipFlow`, `FieldPill`,
`FormField`, `SwitchRow`, `SegmentedSwitch`, and the shared `FieldMessage` /
`FieldMessageText` validation display; six more icons. `SelloButton` with its loading
and disabled states came with SELLO-007. New catalog example IDs:
`inputs.amount-keypad`, `inputs.chip`, `inputs.field-pill`, `inputs.form-field`,
`inputs.switch`, `inputs.expense-entry`. The catalog now resizes for the system keyboard.

Choices made during the work, for owner review:
- Keypad keys are at least 48dp tall. The reference draws 40dp and names that as an
  exception to its own 48dp rule; this ticket requires the minimum.
- The draft rules live in `:domain` so expense and income entry share them. The catalog
  cannot depend on `:domain`, so its examples use a small stand-in draft marked as a
  fixture, as with money formatting.
- Typing after a refused paste starts a new amount; backspace removes the refused text
  whole. Nothing is ever built from part of it.
- `000` at the limit is refused whole, not partly applied. A leading zero is ignored
  without the limit signal.
- Paste is a button on the amount display that hands over the clipboard text untouched;
  there is no hidden text field. Enter on a hardware keyboard submits only when the
  caller provides a submit action.
- `FormField` neither trims nor cuts at a limit; it shows a counter and the caller's
  error, so over-long text is rejected, not truncated.
- Chips and pills grow with the font size and end long labels with an ellipsis.

Left for later: the date picker and its no-future rule belong to the entry screen
(E04). `SettingRow` is settings work, and recurrence controls are post-MVP. The
"Máximo 12 dígitos" and other messages are the consuming feature's resources; the
catalog's are fixtures. Switch Access was not run on a device; its order follows the
same focus order the hardware-keyboard test checks.

2026-10-09: owner approved pull request 7, including the choices above; moved to Done
for a squash merge.

### Delivery evidence
- **Revision:** branch `sello-008-entry-inputs`; tested snapshot is commit `6b08176`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** explicit validation and no sanitising of `1e3`, a negative sign or ambiguous separators → `KeypadAmountInputTest.anInvalidPasteIsKeptAsWrittenWithItsReason` and `aRefusedPasteIsNeverTurnedIntoAnotherAmountByTyping`; device `EntryControlsTest.aRefusedPasteIsShownExactlyAsReceivedWithItsReason`, `pasteHandsOverTheClipboardTextUntouched`, `aFieldPassesOnExactlyWhatWasTypedAndMarksItsError`. Empty amount is a draft, not zero → `deletingEveryDigitLeavesAnEmptyDraftNotZero`, `onlyATypedPositiveAmountBecomesATransactionAmount`, device `anEmptyDraftIsAnnouncedAsNoAmountNotAsZero`. Twelve-digit limit with feedback (M04) → `theTwelfthDigitIsAcceptedAndTheThirteenthChangesNothing`, `tripleZeroIsAllOrNothingAtTheLimit`, device `theTypedAmountAndAHintAreBothShown`. Keyboard focus in reading order and hardware keys → `aHardwareKeyboardTypesDeletesAndSubmitsAndTabFollowsReadingOrder`. Focused field stays visible → `aFocusedFieldAtTheBottomStaysVisibleWhileTyping`. Hit targets, including at font scale 2.0 → `keysKeepAFullTouchTargetAtEveryFontSize`, `chipsSelectOnceReportTheirStateAndKeepATallHitBoxAtDoubleFont`, `aFieldPillIsAButtonWithAFullTouchTarget`, `theWholeSwitchRowTogglesOncePerPress`. Double taps preventable → SELLO-007's `aLoadingButtonKeepsItsLabelAndIgnoresPresses` and `aDisabledKeypadReportsNothing`. No repository or saving service in the catalog → architecture rule `module-edge`.
- **Red / Green:** the nine draft tests were first run against a naive draft (digits filtered out of pasted text, no limit, zero accepted): 7 failed, then all passed with the real rules. On the device, the first run failed four tests: two measured drawn height where touch area was meant, one hit a sub-pixel rounding of 48dp, and the focus test depended on touch mode left by earlier tests; the tests were corrected, the last by supplying keyboard input mode, and it then passed three consecutive full runs. Eight mutations were tried and reverted: seven failed the expected tests (limit raised to 13, an out-of-range paste silently emptied, paste trimmed, field input trimmed, `000` sending a single zero, refused text shown as digits only, Backspace clearing everything); removing the chip's explicit minimum-size modifier did not fail, because the selectable area already extends to 48dp.
- **Gate results:** local `./scripts/verify-ticket SELLO-008 --gate G2` passed: 104 host tests (44 domain, 34 app, 23 design-system, 3 catalog), 49 device tests on an isolated API 30 emulator (6 app, 33 design-system, 10 catalog, which open every example by ID), ktlint, architecture rules, lint with 0 errors and 22 warnings, all in `:app` and present before this ticket. An earlier run of the gate failed on the focus test described above. No dependency or lock file changed.
- **Quality run:** run 20261009T221332Z-eb37da52; SELLO-008 G2 passed; HEAD 6b08176, inputs sha256 7d172d45a0ba; report docs/planning/mvp/quality-reports/SELLO-008.json sha256 7d2384977f8bc4f249d9a83b2dfd7de97689ec02c888112f5df44c5af6a7aace
- **Device / Artifact:** eight captures of the installed catalog on the API 30 emulator are in `docs/testing/evidence/SELLO-008/`: the keypad empty, typed and with a refused `1e3`, chips, pills, fields, switches, and the combined entry example at font scale 2.0 in Violeta dark. No capture shows the system keyboard open. Hosted run 37998255008 on the pull request's merge commit passed the `quality` check with the ticket in Review.
- **Review:** executor self-review of the diff, captures against the reference, reports and logs. Project owner reviewed pull request 7 and approved on 2026-10-09, including the listed choices. This is owner acceptance, not an independent technical review; GitHub does not let the account that opened a pull request approve it.

## SELLO-009 — Build category, adaptive navigation and exact-chart primitives

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-007, SELLO-004
- **Gate:** G2

### Outcome
Supply shared visual building blocks for Recibo and basic Resumen without owning navigation logic.

### Deliverables
CategoryCircle/Cell, scaffold/dock/tab/rail and month-switcher primitives; minimal
bar/line chart wrappers with exact render fixtures, selection values and accessible
text equivalents. Add all type groups/states to catalog with stable IDs.

### Acceptance criteria
- Category meaning uses names/icons and brand ink, not per-category colors.
- Width/height adaptation follows Sello: compact, short landscape, medium and expanded;
  dock/insets never cover content. Navigation callbacks are stateless.
- Chart geometry may approximate, but selected amounts and textual equivalents
  use original exact values. Charts are ≥130dp with at most one labeled reference.
- Customer app remains free of catalog chrome and synthetic data dependencies.

### Tests
Chart selection at large amounts, accessible text/value agreement, layout/inset
and navigation-callback tests, four-scheme catalog walkthrough; run G2.

### Working checkpoint
App can compose the primitives without importing catalog; independent catalog groups
show every delivered component and do not advertise unfinished product screens.

### Context and starting points
Recibo and Resumen need shared category/navigation/chart rendering, not separate
copies. Read [categories](../../../design/sello-spec.html#c-categorycircle),
[scaffold](../../../design/sello-spec.html#c-selloscaffold), navigation/a11y sections
and exact render-input contracts from SELLO-007. No financial aggregate policy is owned here.

### Implementation plan
1. Implement CategoryCircle/Cell using explicit presentation progress/status/name/icon;
   clamp drawing progress only, retaining exact over-limit sign/text.
2. Add scaffold/dock/tabs/rail/month controls with callbacks and inset slots. Support
   two MVP tabs without hardcoding disabled Patrimonio in a reusable component.
3. Implement bar/line chart render inputs retaining IDs and exact labels/values;
   normalize geometry separately. Selected point maps back to original data, not pixels.
4. Implement compact/short-height/medium/expanded layouts and accessible chart summary
   or equivalent values. Navigation state/business month validation belong to app.
5. Register examples with near-maximum amounts, no limit, negative remaining, empty
   chart and different window sizes. Keep complex future investment charts out of MVP.

### Concrete cases and pitfalls
- Two adjacent amounts above floating-point exact range may draw similarly but
  must select/announce their distinct exact values.
- Under 480dp height use the reference's rail/two independently scrollable panes;
  medium content cap and expanded pane/sheet widths come from the local reference.
- A custom Canvas without text semantics/keyboard selection is not an accessible chart.

### Verification recipe
Create/run `*ChartExactSelectionTest`, `*CategorySemanticsTest` and adaptive scaffold
UI tests; run G2 for design-system/catalog. Inspect insets, tap targets and text
summaries against fixture IDs/values rather than judging screenshots alone.
