# SELLO-082 — Offer one budget suggestion in Resumen

- **Revision:** tested snapshot is branch `sello-080-082-resumen-v2` with `main`
  (`136a1aa`) merged in, identified exactly by the retained input fingerprint below;
  the commit and pull request follow this evidence.
- **Decisions:** delivered before the SELLO-079 audit (owner, 2026-10-23). A finished
  month never shows a suggestion, as the v2 past-month state shows.
- **Requirement mapping:** `LimitAdjustment` is offered only for the month in progress:
  a category still taking expenses, with a finite limit now, over its limit in each of
  the two complete months before; the bigger limit wins. `SuggestionNote` says the fact
  ("… superó su límite en agosto y septiembre: gastaste … en promedio y su límite es
  …"), "Podrías ajustar su límite…" and one action, **Ajustar el presupuesto de …**,
  which opens that category's editor and changes nothing itself. × stores the month
  and category in preferences (`resumen_suggestion_dismissed_v1`, `SuggestionPreferences`);
  it stays hidden that month and may return the next; a dismissal that cannot be read
  hides nothing.
- **Red / Green:** `ResumenInsightsTest`'s suggestion cases, the dismissal store tests
  in `DataStoreUserPreferencesTest` and `ResumenViewModelTest`'s dismissal cases failed
  on stubs and passed after; `ResumenScreenTest` covers the note, its action and ×.
- **Gate results:** `./scripts/verify-ticket SELLO-082 --retain` passed all seven checks (board, board tests, quality tests, performance tests, architecture, host, device) on the merged working tree. Host: 744 tests, results reused from Gradle's cache; device: 753 tests run fresh (app 360, data 236, design-system 136, catalog 21); zero failures, errors or skips. Lint had zero errors and 34 warnings.
- **Quality run:** run 20261023T231012Z-79e049ea; SELLO-082 G2 passed; HEAD 29004f5, inputs sha256 ad2e8c229377; report docs/tickets/E16-v2-today-gaps/SELLO-082/quality-report.json sha256 6e3aa62e709c366629adc5c6a9be286e16a7bcd711e84f196524c24a3af12bdc
- **Device / Artifact:** as SELLO-080. The sample scenarios have no category over its
  limit twice in a row, so the note is shown by screen tests, not a screenshot.
- **Review:** executor self-review only. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

