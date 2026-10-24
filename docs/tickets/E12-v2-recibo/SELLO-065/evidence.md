# SELLO-065 — Use the full-width Anotar dock and the v2 tab bar

- **Revision:** tested snapshot is the `sello-065-v2-dock-and-tabs` working tree on
  `main` at `f3df3ed`, with only the ticket status uncommitted; it is identified
  exactly by the retained input fingerprint below. The commit and pull request follow
  this evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `SelloScaffold` draws `V2Dock` (one full-width brand button
  with a + icon and `SelloDock.actionLabel`, "Anotar") in place of `EntryDock`, keeping
  the dock's tag, margins, keyboard rule and spoken label (`SelloDock.label`, "Anotar
  un gasto"), and `V2NavigationBar` (a `brandSoft` pill behind the selected tab's
  icon, selectable tabs with the `Tab` role) in place of `SelloNavigationBar`. Every
  screen with a dock or tabs goes through the scaffold, so Recibo, Resumen, income
  history, category detail and Configuración all change; entry opens as before,
  including from a past month. The rail and its "+" are unchanged. `V2Dock` has no long
  press; nothing in the app used `SelloDock.onLongClick`, now documented as v1-only.
  `EntryDock` and `SelloNavigationBar` stay for SELLO-073.
- **Red / Green:** `V2BottomControlsTest`'s dock test failed on the field-style dock,
  which read "Anotar un gasto" with no "Anotar" button text; it passed after the swap.
  The same class covers the selected tab and both labels, the dock and 48dp targets at
  font scale 2.0 on 320 × 640dp; `AdaptiveScaffoldTest`, `KeyboardInsetsTest`,
  `V2FrameScaffoldTest`, `CorrectionControlsTest` and every `:app` navigation test
  passed unchanged.
- **Gate results:** `./scripts/verify-ticket SELLO-065 --retain` passed all six checks.
  Host: 525 tests; device: 561 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings.
- **Quality run:** run 20261020T184534Z-2921dff1; SELLO-065 G2 passed; HEAD f3df3ed, inputs sha256 35d0eb7c75bf; report docs/tickets/E12-v2-recibo/SELLO-065/quality-report.json sha256 559ef455041d5e212a119b986920ed124207c6dc68ff914ca9a2d56775c1e898
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  showed the dock and tab bar on Recibo and Resumen in the light and dark themes and at
  font scale 2.0, labels whole. No personal history or physical device was used; no
  manual TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered the spoken label, every scaffold route, the
  rail, the keyboard rule and large text. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

