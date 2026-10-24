# SELLO-068 — Wire the v2 month bar and past-month ribbon into the app frame

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
- **Quality run:** run 20261020T052148Z-7ece84c2; SELLO-068 G2 passed; HEAD 2694b16, inputs sha256 ababa56475c2; report docs/tickets/E12-v2-recibo/SELLO-068/quality-report.json sha256 9d43f499ccd1969be5096e4f0f7c6bd13bf5b723c971398865bdff0b8be5b5e1
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

