# SELLO-073 — Retire the v1 components v2 replaced

- **Revision:** tested snapshot is the `sello-073-retire-v1-components` working tree on
  `main` at `e549afa`, uncommitted; it is identified exactly by the retained input
  fingerprint below. The commit and pull request follow this evidence; no claim is
  made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-21, to remove the catalog's v2 screen
  mockups (the goal and investment components keep their own examples), to move the
  prototype's Anotar, Movimiento and Aportar amounts to `EntryAmountSlip` and delete
  `AmountField`, and to give the freed v1 names to their v2 replacements.
- **Requirement mapping:** deleted, each after a search showing no remaining use:
  `AmountField` (its last users were the catalog and `:prototype`), the v1 `EntryDock`
  and the v1 `SelloNavigationBar` (catalog only), with their examples, plus
  `SelloDock`'s long-press fields, which only the v1 dock read. Renamed without
  behaviour change: `V2Dock` → `EntryDock`, `V2NavigationBar` → `SelloNavigationBar`,
  `V2MonthBar` → `MonthBar`, `V2TitleBar` → `TitleBar`, `V2ChipRows` → `ChipRows`,
  `V2EntryLayout` → `EntryLayout`, Stamp's `v2Motion` → `inkRing`; the
  `component.v2` package is folded into `component` and the `V2*` tests renamed. Kept,
  because no v2 component replaces them: `MonthSwitcher` (the pushed screens' titled
  bar), `FieldPill` (the entry date pill), `ChipFlow` (wrapping chips in Configuración
  and the editor), `IconChoiceGrid` and `CategoryCell`. Category detail, the editor and
  Configuración therefore keep their look. The catalog lists 56 examples in ten groups
  (a new "Goals and investments" group) with no V2 section: duplicate stamp and button
  examples were merged, the v1 entry, amount and v2 states/foundations examples
  removed, and `TitleBar` shown in the month-bar example. The work landed as one
  commit, not the two the brief proposed, because deletions and renames touch the same
  files.
- **Red / Green:** the new `CatalogExamplesTest` (full ID list, ten groups, unique
  titles) failed 2 of 3 against `main`'s registry and passes here. `AmountField`'s
  display tests now exercise `EntryAmountSlip`; its empty-draft test checks that the
  faint placeholder figure is spoken only as "Sin monto", since the v2 slip draws a
  placeholder that `AmountField` did not. `CatalogShellTest` and the entry catalog tests
  open `inputs.entry`.
- **Gate results:** `./scripts/verify-ticket SELLO-073 --retain` passed all six checks.
  Host: 533 tests (the three `V2CatalogContractTest` tests folded into
  `CatalogExamplesTest`); device: 594 tests; zero failures, errors or skips. Lint had
  zero errors and 32 warnings: app dependency-update notices and unused template
  colours, and two in untouched prototype files; none in changed code. The count is one
  above SELLO-072, most likely a newly published dependency version; the reports do
  not itemize warnings, so that is not proven.
- **Quality run:** run 20261022T025906Z-f8f67804; SELLO-073 G2 passed; HEAD e549afa, inputs sha256 a2b6c7378652; report docs/tickets/E13-v2-current-app/SELLO-073/quality-report.json sha256 2b8e1d9b4a2f30a2fcbdb32fbb94921d3fbb65285b40310cd532c28d0a38e4c5
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. No personal history or
  physical device was used; no manual TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered each deletion's search, rename-only diffs,
  the kept v1 components and the catalog's single listing. Done is proposed by this
  ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.
