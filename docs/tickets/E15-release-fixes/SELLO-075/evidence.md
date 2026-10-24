# SELLO-075 — Group thousands in every count the app shows

- **Revision:** the gate tested commit `a5b82f9` on `sello-075-count-separators`, on
  `main` at `d16a7c5`; the only uncommitted file was the retained report. The status
  and this section are added to that commit afterwards.
- **Requirement mapping:** every count listed under Execution progress goes through
  `countText`; the restore preview's spoken cells use the same text; counts under
  1,000 are unchanged; plurals are chosen by the number; backup files and stored values
  are untouched.
- **Red / Green:** `CountTextTest.thousandsAreGroupedWithAPointAsAmountsAre` failed first
  (`expected:<1[.]000> but was:<1[]000>`) and passes. The screen tests below were written
  with the change.
- **Working checkpoint:** the SELLO-035 large history (`dataset.py large`, 20
  categories, 10,000 expenses, 2,000 incomes) restored on the emulator: the preview
  shows 20 / 10.000 / 2.000 and is read "Respaldo 10.000"; the result says "Se restauró
  el respaldo: 20 categorías, 10.000 gastos y 2.000 ingresos."; the erase confirmation
  says "Se borran 20 categorías, 10.000 gastos y 2.000 ingresos"
  ([preview](artifacts/restaurar-vista-previa.png),
  [result](artifacts/restaurado.png),
  [erase](artifacts/borrar-conteos.png)). The app's data and
  the pushed file were removed afterwards.
- **Tests:** host `CountTextTest` (new, 2); device `ResetScreenTest`,
  `StartupFailureScreenTest` and `CategoryDetailScreenTest` (+1 each).
- **Gate results:** `./scripts/verify-ticket SELLO-075 --retain` passed all seven G2
  checks, full coverage. Host: 750 tests; device: 757 tests (data 236, design-system
  136, app 364, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`.
- **Quality run:** run 20261024T012059Z-2024b0bc; SELLO-075 G2 passed; HEAD a5b82f9, inputs sha256 5cf6d35afa6a; report docs/tickets/E15-release-fixes/SELLO-075/quality-report.json sha256 3ed82a63a9f7be56308459cefcb6d6d12652497161d58305e7a888ebcca09111
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging.

