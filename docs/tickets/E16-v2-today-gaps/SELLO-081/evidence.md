# SELLO-081 — Show the average month and the weekday answer in Resumen

- **Revision:** tested snapshot is branch `sello-080-082-resumen-v2` with `main`
  (`136a1aa`) merged in, identified exactly by the retained input fingerprint below;
  the commit and pull request follow this evidence.
- **Decisions:** delivered before the SELLO-079 audit (owner, 2026-10-23). The owner
  chose a six-month weekday window after seeing the selected-month and four-month
  versions on a phone; ADR 0017 and this ticket's text are amended to match.
- **Requirement mapping:** `NormalMonth` averages the complete months with something
  recorded, half up to whole pesos; a month with nothing recorded is left out, not
  counted as zero, and it is hidden while history is insufficient. "Mes a mes" shows
  "Un mes normal" with "Promedio de N meses completos" above the columns, whose short
  labels ("561 mil", "Límite 1 M") are exact when selected or spoken.
  `WeekdayPattern` averages each weekday over how many times it came round, from the
  first recorded day but no further back than six months including this one; it shows
  after a week and names the busiest day only when one stands out. "¿Qué días gastas
  más?" answers "Los sábados" with "36.353 en promedio · últimos 6 meses" (or "desde el
  3 de mayo"), then seven columns, the busiest with its short value.
- **Red / Green:** `ResumenInsightsTest`'s normal-month and weekday cases (half-up
  averages, gaps, finished months, ties, the six-month cap) and `ResumenRenderModelTest`
  failed before the calculations and passed after; `ChartLabelTest` failed on a stub
  and passed. `ResumenScreenTest` covers both slips, exact selection and both periods.
- **Gate results:** `./scripts/verify-ticket SELLO-081 --retain` passed all seven checks (board, board tests, quality tests, performance tests, architecture, host, device) on the merged working tree. Host: 744 tests, results reused from Gradle's cache; device: 753 tests run fresh (app 360, data 236, design-system 136, catalog 21); zero failures, errors or skips. Lint had zero errors and 34 warnings.
- **Quality run:** run 20261023T230202Z-27a8f6fb; SELLO-081 G2 passed; HEAD 29004f5, inputs sha256 ad2e8c229377; report docs/tickets/E16-v2-today-gaps/SELLO-081/quality-report.json sha256 ad994ac581071c4f23de8052c06678a7bb63418c7e23b1e6205bebbedf105140
- **Device / Artifact:** as SELLO-080; `2-un-mes-normal.png` and
  `3-que-dias-gastas-mas.png` in `docs/testing/evidence/resumen-v2/`.
- **Review:** executor self-review only. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

