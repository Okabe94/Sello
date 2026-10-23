# Component catalog

The independently installed Sello Catalog lists each `:design-system` component once,
in ten groups, with 58 synthetic, interactive examples. The visual reference is
[Sello v2](../design/v2/README.md); the HTML component inventory's
Exists/Extend/New labels are not a code completeness check. SELLO-073 retired the v1
components v2 replaced (`AmountField`, the v1 `EntryDock` and the v1
`SelloNavigationBar`), dropped the `V2` prefix and folded the former V2 section into
the groups below.

## Scope and ownership

Reusable visuals live in `design-system/.../component`. Catalog fixtures, local
interaction state and Compose previews live in `catalog/.../components`. No app
screens, domain/data dependencies, financial repositories or write paths are used.
Examples of later capabilities (goals, investments) demonstrate presentation, not
delivered financial features. An unknown-save example offers checking only; it does
not simulate a second financial write.

## Example map

Each ID is `<group>.<name>` and is stable for targeted UI tests;
`CatalogExamplesTest` pins the full list.

| Group | IDs | Coverage |
| --- | --- | --- |
| `foundations` | `colors`, `typography`, `money-text`, `shape-spacing`, `motion`, `icons` | Tokens, fonts, icons and motion specs respecting reduced motion |
| `paper` | `slip`, `tear-line`, `leader-line`, `action-line`, `total-line`, `key-value`, `answer-slip`, `footnotes`, `suggestion` | Paper lines; current/overrun/unlimited/closed answer slips; footnotes, fact and review invitation; dismissible suggestion |
| `stamps` | `stamp` | Inks and sizes; landing, and the ink-ring landing used for goals |
| `money` | `money-text`, `counting` | Exact accessible amounts |
| `categories` | `category-circle`, `category-cell` | Category fill and cells |
| `inputs` | `button`, `chip`, `chip-rows`, `field-pill`, `form-field`, `switch`, `settings-controls`, `icon-choice`, `icon-strip`, `amount`, `keypad`, `note-composer`, `entry` | Button kinds and tones; wrapping chips and two-row choices; amount fitting, refused paste and limits; compact pad; keyboard note composer; the whole expense and income entry with unknown-save and restored-draft states |
| `navigation` | `scaffold`, `entry-dock`, `navigation-bar`, `month-bar`, `month-switcher`, `past-month-ribbon` | Frame with the ribbon slot; full-width docks in each tone; two/three/four tabs and the rail; month bar and title bar; pushed-screen month title; return-to-current-month |
| `charts` | `columns`, `line`, `split`, `comparison`, `ranking` | Columns and line selection; assigned/unassigned split, striped current month, recorded values; same-cutoff comparison; category ranking and total |
| `feedback` | `confirm-slip`, `empty-slip`, `error-slip`, `notice`, `bullet-list`, `backup-states`, `slip-skeleton`, `danger-and-ticket`, `celebration` | Confirmation, empty, read failure and loading; undo/busy notices; points to read before acting; saving a backup (saving, Guardado, failed, unknown); multi-stamp ticket and danger slip; goal completion overlay |
| `goals` | `milestones`, `goal-pill`, `goal-cards`, `movements`, `investments` | Milestone places, Recibo goal and reached row, active/completed cards, signed movements, returns and valuations |

Use the catalog controls for both inks, light/dark, fonts 1.0/1.3/2.0 and
320/360/600/840dp or 760×400 landscape stages. Previews parameterize all four schemes
and include compact, 320dp double-font and expanded sizes. Open composer and
celebration previews show their overlays directly. Large aggregate amounts retain
exact digits; after the accessible minimum size they can scroll horizontally.

The new Material Symbols Rounded vectors come from the official Google Fonts
`fonts.gstatic.com` endpoints recorded in each drawable. The existing Apache 2.0
Material Symbols license covers them; no dependency/toolchain update is needed.

## Notes

`TitleBar` (a tab title with actions on the band) and `GoalsBandTotal` serve the
future Metas and Inversiones tabs. `PerformanceLine` keeps the arrow, percentage and
amount on one line, and `InvestmentRow` follows the reference: a brand-into-paper
`investmentShade` circle, the value over its signed change on the right, and rules
between rows.

`CelebrationOverlay` runs the reference's timeline from one clock: stamps land at 0.3,
0.65, 1.0 and 1.4 s on the desk, gain floods out of the Meta stamp at 1.75 s, the stamps
rise and turn white at 2.05 s, the title rises at 2.4 s, the outlined plate is stamped at
2.75 s, 64 seeded paper chads fall from 2.0 s and the actions fade in at 3.1 s. Reduced
motion opens on the final screen without chads. The system bars keep dark ink on the pale desk
and turn white once the green floods in. The expense categories end with `AddChip`
(Nueva): a dashed brand chip that is a button, not one of the choices.

## Verification

Component tests
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
box, a round check button and an Opcional hint. `MonthBar` is now a month, year and chevron
button with trailing icon actions, and `PastMonthRibbon` a translucent pill on the band
with its white return button on the same line. `GoalCard` puts the target and its flagged date
on one line and spreads 60dp places across the card; reached places are a double ring.
`CompletedGoalRow` has an ink icon, a regular detail line and the stamp on the right.

Run focused suites before the integrated gate:

```bash
./gradlew :catalog:testDebugUnitTest :design-system:testDebugUnitTest
./gradlew :design-system:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.software.sello.designsystem.component.ComponentContractsTest
./gradlew :catalog:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.software.sello.catalog.EntryCatalogInteractionTest
```

Activity recreation is not process-death evidence. Product goal completion,
withdrawal rules and income allocation still require their own approved examples,
commands and integration tests when those features ship.
