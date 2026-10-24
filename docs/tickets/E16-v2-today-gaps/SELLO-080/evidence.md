# SELLO-080 — Lead Resumen with the same-days comparison

- **Revision:** tested snapshot is branch `sello-080-082-resumen-v2` with `main`
  (`136a1aa`) merged in, identified exactly by the retained input fingerprint below;
  the commit and pull request follow this evidence.
- **Decisions:** the owner chose, on 2026-10-23, to deliver this before the SELLO-079
  audit (E16 note). The kept daily chart ("Así crecen tus gastos") is removed, as the
  SELLO-072 decision allowed once the hero replaced it; the domain still computes the
  daily series, unused by Resumen.
- **Requirement mapping:** `ResumenPolicy` adds `SpendingComparison`: the month in
  progress compares day 1 to today with the same days of the month before, capped at
  its length; a finished month compares whole months; there is no comparison when the
  month before has nothing recorded. The difference is never negative, with a
  direction; the rate is in tenths of a per cent, half up, or absent against zero.
  `ResumenRenderModel` builds "¿Cómo va este mes?" / "¿Cómo cerró …?" and
  `ComparisonSlip` draws it: the difference, "más/menos/lo mismo que en …", the rate
  with ↑/↓ in the same neutral ink, the range ("Del 1 al 24 de cada mes", both ranges
  when capped, or "Meses completos") and one line per month with its bar on one scale
  and its exact amount. Without a comparison the month's spending stands alone; a first
  month adds "Cuando completes otro mes podrás comparar los mismos días.". Loading,
  failed (never zero) and stale states are unchanged.
- **Red / Green:** with the new snapshot fields still null, `ResumenInsightsTest`'s
  comparison cases (same days, 31 Mar vs 28 Feb, 31 Oct vs 30 Sep, whole months, a
  zero rate base) and `ResumenRenderModelTest`'s hero cases failed; all passed after
  the change. `ResumenScreenTest` covers the hero in progress, capped ranges, a finished
  month, no rate, a first month, the one-line bars and the explanation's 16sp size.
- **Gate results:** `./scripts/verify-ticket SELLO-080 --retain` passed all seven checks (board, board tests, quality tests, performance tests, architecture, host, device) on the merged working tree. Host: 744 tests, results reused from Gradle's cache; device: 753 tests run fresh (app 360, data 236, design-system 136, catalog 21); zero failures, errors or skips. Lint had zero errors and 34 warnings.
- **Quality run:** run 20261023T225328Z-d4199b96; SELLO-080 G2 passed; HEAD 29004f5, inputs sha256 ad2e8c229377; report docs/tickets/E16-v2-today-gaps/SELLO-080/quality-report.json sha256 533eb308afe9149249d0a12350a51ba5581a855a266de59b1c38f43f5d818230
- **Device / Artifact:** the gate ran on an isolated API-30 AVD (320 × 640dp) the
  executor created; screenshots on a second isolated Pixel 6 AVD with the "Seis meses"
  sandbox scenario are in `docs/testing/evidence/resumen-v2/`. No personal data or
  physical device was used for evidence.
- **Review:** executor self-review only. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

