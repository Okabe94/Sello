# V2 component review catalog

The **V2** section of the independently installed Sello Catalog has 27 synthetic,
interactive example families. The original 38 examples remain available. The
visual reference is [Sello v2](../design/v2/README.md), including its locked screen
examples; the HTML component inventory's Exists/Extend/New labels are not a code
completeness check.

## Scope and ownership

New reusable visuals live in `design-system/.../component/v2`; existing primitives
have additive options for compact keys, semantic button tones, warning stamps,
confirmation notices, fitted monetary text and striped current-month columns.
The customer app continues using its existing defaults. Product navigation,
financial calculations, storage, goal/investment lifecycle and ticket statuses are
outside this catalog change.

Catalog fixtures, local interaction state and Compose previews live in
`catalog/.../v2`. No app screens, domain/data dependencies, financial repositories
or write paths are used. Examples of later capabilities demonstrate presentation,
not delivered financial features. An unknown-save example offers checking only;
it does not simulate a second financial write.

## Example map

Every ID has the `v2.` prefix and is stable for targeted UI tests.

| IDs | Coverage |
| --- | --- |
| `entry`, `amount`, `keypad` | Fixed amount/action/pad; 12-digit fitting and label error; compact pad; disabled and unknown-save states; restored synthetic draft; confirmation over the caller's dock |
| `chips`, `note-composer` | One/two scrolling rows and trailing fade; native keyboard composer, overlength retention and cancel/done |
| `buttons`, `dock` | Brand/gain/ink/loss tones, outlines, full-width actions, busy and disabled states |
| `navigation`, `month-bar`, `past-month-ribbon` | Two/three/four tabs, selected icon pill, existing rail, separate month/year, return-to-current-month action |
| `stamps`, `footnotes`, `summary` | V2 status/result inks and words, landing replay, footnotes/fact/review invitation, current/overrun/unlimited/closed answer slips |
| `comparison`, `ranking`, `suggestion` | Same-cutoff bars, exact differences, income slip, category shares and total, dismissible suggestion |
| `charts` | Assigned/unassigned split, striped current month, exact selection, weekday answer, recorded-value points plus net-contribution steps |
| `milestones`, `goal-pill`, `goal-cards` | Earned/kept/empty/ready places, projected captions, Recibo goal and reached row, active/completed cards |
| `movements`, `investments`, `icon-choice` | Signed movements, withdrawal limit appearance, gains/losses, dated/missing valuations, icon strip and Recibo pin |
| `feedback`, `celebration`, `states` | Check confirmation, undo/busy notices, multi-stamp ticket, danger/confirm slips, completion overlay/plate, long content, empty/loading/read failure |
| `foundations` | Actual token/font/icon examples and motion specs respecting reduced motion |

Use the catalog controls for both inks, light/dark, fonts 1.0/1.3/2.0 and
320/360/600/840dp or 760×400 landscape stages. Previews parameterize all four schemes
and include compact, 320dp double-font and expanded sizes. Open composer and
celebration previews show their overlays directly. Large aggregate amounts retain
exact digits; after the accessible minimum size they can scroll horizontally.

The new Material Symbols Rounded vectors come from the official Google Fonts
`fonts.gstatic.com` endpoints recorded in each drawable. The existing Apache 2.0
Material Symbols license covers them; no dependency/toolchain update is needed.

## V2 screens

The **V2 screens** group shows whole screens from
[the evidence](../design/v2/evidence) for comparison: Recibo (a payment reserved), Metas,
Inversiones, the Lograda celebration and Metas after Listo with Deshacer. They are put
together only from existing components with synthetic data. The two pieces the
evidence needed that had no composable, `V2TitleBar` (a tab title with actions on the
band) and `GoalsBandTotal` (Guardado en metas), now exist in `:design-system`.
`PerformanceLine` keeps the arrow, percentage and amount on one line, and
`InvestmentRow` follows the reference: a brand-into-paper `investmentShade` circle, the
value over its signed change on the right, and rules between rows. `components.jpg` is
the component reference itself, so it has no screen here.

## Verification

The registry contract first failed because the V2 section was absent. A device test
also failed on the absent `link:v2.entry`, before implementation. Component tests
cover compact-pad size/touch bounds/clear-all, exact one-line signed money at 320dp
and font 2.0, four-tab selection, the past-month action, overlength note retention
and kept milestones with reduced motion. Long-note checks also keep both composer
actions visible at double font size. A landscape regression test first exposed the
amount scrolling away with the choices; only the choices now scroll in the left pane.
Catalog interaction tests cover unknown outcomes, activity recreation without
auto-submission, and a note counter that first counted emoji as two characters; it
now counts code points like the entry example.
Device testing found three visual faults, each now covered by a test that failed first:
the two chip rows had no gap, because the grid stretched each chip to its 48dp row;
expense chips had no icons; and the note keyboard flipped the entry behind the sheet
into two columns. The entry now keeps its keyboard-free height while a keyboard is open. A second round
matched the reference: chips in a column share the widest chip's width and start at the
leading edge; the note and an income source's name are full-width `EntryLine`s with
Editar/Cambiar; and `NoteComposer` states what the note belongs to, then shows a filled
box, a round check button and an Opcional hint. `V2MonthBar` is now a month, year and chevron
button with trailing icon actions, and `PastMonthRibbon` a translucent pill on the band
with its white return button on the same line. `GoalCard` puts the target and its flagged date
on one line and spreads 60dp places across the card; reached places are a double ring.
`CompletedGoalRow` has an ink icon, a regular detail line and the stamp on the right.

Run focused suites before the unticketed integrated gate:

```bash
./gradlew :catalog:testDebugUnitTest :design-system:testDebugUnitTest
./gradlew :design-system:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.software.sello.designsystem.component.V2ComponentTest
./gradlew :catalog:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.software.sello.catalog.V2CatalogInteractionTest
./scripts/verify-ticket --merge-candidate main --gate G2
```

Activity recreation is not process-death evidence. Product goal completion,
withdrawal rules and income allocation still require their own approved examples,
commands and integration tests when those features ship.
