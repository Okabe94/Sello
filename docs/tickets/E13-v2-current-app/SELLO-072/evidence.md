# SELLO-072 — Bring Resumen to v2 for today's figures

- **Revision:** tested snapshot is the `sello-072-v2-resumen` working tree on `main` at
  `ee3717d`, with only the ticket status uncommitted; it is identified exactly by the
  retained input fingerprint below. The commit and pull request follow this evidence;
  no claim is made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-21, to keep the cumulative daily line chart
  (after "¿Dónde se va?") until a v2 comparison replaces it, and to remove the "Gasto
  real" Cumplido/Excedido stamp, which Recibo already shows. The forecast is removed
  because the acceptance forbids projecting the month in progress; the domain still
  computes it, unused by Resumen.
- **Requirement mapping:** Resumen follows the v2 order for figures the app already
  computes. **Ingresos** is one slip of receipt lines (Ingresos anotados, Ingresos menos
  gastos, Sin asignar a categorías), the "Movimientos anotados hasta …" line and **Ver
  ingresos y su destino ›** (48dp), which with the income line opens income history.
  **Mes a mes** draws `ColumnsChart` with `stripedCurrent`: past months in `pastBar`,
  over-limit months in loss, and the month in progress always `ChartPeriod.Current`
  (even over its limit), marked `*`, read "…, mes en curso, hasta hoy" and explained by
  a note; values print on the highest and current columns, the selected month's limit
  is one dashed reference line (`ResumenLimit`), and tapping a column still shows its
  exact value and limit. **¿Dónde se va?** ranks categories with spending by amount in
  `SpendingRankRow`s (icon, name, exact amount, share bar; the share is geometry only),
  each opening its detail, then a **Total** that is the month's spending. Loading,
  failed (never zero), stale with retry and empty states are unchanged, as are the
  one- and two-column layouts. The v2 hero comparison, "Un mes normal", "¿Qué días
  gastas más?" and the suggestion need new calculations and are not built.
- **Red / Green:** with `current` always false, no limit line and every share 0,
  `ResumenRenderModelTest`'s current-month and ranking tests failed; they passed after
  the change, alongside a closed-month case. `ResumenScreenTest` now checks the income
  lines and the absence of the forecast and stamp in every ink and theme, the v2 slip
  order, the marked and explained current month and the limit line, ranked rows that
  open their category and the total, exact selection, every state, font scale 2.0 and
  the side-by-side layout; the Resumen and income journeys follow the new link.
- **Gate results:** `./scripts/verify-ticket SELLO-072 --retain` passed all six checks.
  Host: 536 tests; device: 594 tests; zero failures, errors or skips. Lint had zero
  errors and 31 warnings: the previews now read resources through `LocalResources`,
  removing one existing warning.
- **Quality run:** run 20261021T054301Z-85162d56; SELLO-072 G2 passed; HEAD ee3717d, inputs sha256 ee622ff66850; report docs/tickets/E13-v2-current-app/SELLO-072/quality-report.json sha256 c98ba0a14b532f0e829b4843d8b460aa843a40c23b63e6be0f9d5c3478384713
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  with one synthetic category (limit 100.000) and one expense showed the income lines
  and link, the October column striped and marked `oct*` with its value, the note and
  the dashed "Límite 100.000" line, Comida ranked with its share bar and the Total, and
  the daily chart below. No personal history or physical device was used; no manual
  TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered nothing projected, the current month over
  its limit, shares as geometry, the total against the rows, and the kept chart. Done is
  proposed by this ticket's pull request; its merge is the designated owner acceptance
  under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.

