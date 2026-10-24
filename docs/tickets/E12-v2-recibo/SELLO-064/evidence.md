# SELLO-064 — Restyle the month bar and add the past-month ribbon

- **Revision:** tested snapshot is the `sello-064-past-month-ribbon` working tree on
  its merge of `main` at `0ac23eb` (bringing in `28f1660`, CI build caches, with no
  overlap), with only the ticket status uncommitted; it is identified
  exactly by the retained input fingerprint below. The commit and pull request follow
  this evidence; no claim is made that the gate tested a clean commit.
- **Scope decision:** the owner chose, on 2026-10-20, to show the ribbon on every
  screen that shows the selected month (Recibo, Resumen, income history and category
  detail, on a phone or beside its list) and not on Configuración, as this ticket's
  acceptance reads, rather than only on the tabs as the v2 README says.
- **Requirement mapping:** `ShellScreen` fills the scaffold's `ribbon` slot with
  `PastMonthNotice` whenever the selected month is not the current one, unless the
  caller passes `pastMonthRibbon = false` (Configuración). It reads "Estás viendo
  septiembre", with the year when it is not the current one's ("septiembre de 2025"),
  and "Volver a octubre". The button sends the new `ShellAction.ReturnToCurrentMonth`,
  which selects the month the financial clock says is current when it is handled.
  `SelloScaffold` now also draws a ribbon on the band under a titled bar, measuring it
  so the content still starts 50dp on the band. `PastMonthRibbon` puts its return under
  the message from font scale 1.3. The month bar from SELLO-068 already shows the month
  large and the year small, hiding the year visually from 1.3 while the spoken label
  keeps it, and opens the same picker; back and the picker are unchanged.
- **Red / Green:** with `ReturnToCurrentMonth` handled as nothing,
  `NavigationStateTest`'s return, after-midnight and process-death tests failed; they
  passed after the change. On a 320dp screen at font scale 2.0 the first ribbon squeezed
  its message to 0dp beside the button; `V2ComponentTest`'s font scale 1.3 and 2.0
  stacking tests failed on that layout (0dp and 67dp wide messages) and passed after the
  return moved under the message. `PastMonthRibbonShellTest` covers show and hide, one
  tap, a month turning, another year, a pushed screen, category detail, Configuración,
  the live region and font scale 2.0; `ShellOnDeviceTest` picks March of last year in
  the installed app, keeps the ribbon through recreation and on Resumen, and returns
  with no write; `V2FrameScaffoldTest` covers a ribbon under the titled bar at 1.0 and
  2.0.
- **Gate results:** `./scripts/verify-ticket SELLO-064 --retain` passed all six checks.
  Host: 525 tests; device: 558 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. After merging `main` the gate was run again and
  passed with the same counts.
- **Quality run:** run 20261020T150426Z-9aa00f1b; SELLO-064 G2 passed; HEAD 0ac23eb, inputs sha256 a35b4e7315d8; report docs/tickets/E12-v2-recibo/SELLO-064/quality-report.json sha256 86e9ad70dfbdba4e680bf5f8cbf0f7bd407bec788cb7894e7f9b0ea019ab5a8f
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  showed, with September selected, the ribbon on Recibo and Resumen at font scale 1.0,
  and stacked at 1.3 and 2.0. No personal history or physical device was used; no
  manual TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered a tap after midnight, the year in the
  message, the band under a titled bar, Configuración and large text. Done is proposed
  by this ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.

