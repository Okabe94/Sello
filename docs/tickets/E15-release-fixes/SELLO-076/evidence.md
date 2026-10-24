# SELLO-076 — Show Resumen's "recorded until" date in words

- **Revision:** the gate tested commit `834f1e6` on `sello-076-resumen-date`, on `main`
  at `20482f7`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards.
- **Requirement mapping:** the line and the "could not refresh" line use Resumen's long
  date form with "el", as the owner chose; the year only for a day from another year;
  formatted in the render model; the financial day is the snapshot's own. Screen readers
  read the same text node that is shown.
- **Red / Green:** `theRecordedUntilDayShowsItsYearOnlyWhenItIsFromAnotherYear` failed
  first (`expected:<3 de octubre[ de 2026]> but was:<3 de octubre[]>`) and passes; the
  screen tests assert "Movimientos anotados hasta el 3 de octubre" and the stale line's
  "hasta el 3 de octubre.".
- **Working checkpoint:** in the sandbox's month and year-end scenario on 1 January
  2027, January and the past December both read "Movimientos anotados hasta el 1 de
  enero" ([January](artifacts/resumen-enero.png),
  [December](artifacts/resumen-diciembre-pasado.png)).
- **Tests:** host `ResumenRenderModelTest` (+1); device `ResumenScreenTest` (+1, one
  assertion added).
- **Gate results:** `./scripts/verify-ticket SELLO-076 --retain` passed all seven G2
  checks, full coverage. Host: 745 tests; device: 754 tests (data 236, design-system
  136, app 361, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`. Two earlier runs are not evidence: one on the first
  version, before #95 merged, and one that met a sandbox session left active on the
  emulator by the screenshots (72 failures); the app's data was cleared before this one.
- **Quality run:** run 20261023T235512Z-730547cb; SELLO-076 G2 passed; HEAD 834f1e6, inputs sha256 7888ed5c5f90; report docs/tickets/E15-release-fixes/SELLO-076/quality-report.json sha256 9f0cec888ae9d4160a47de3de6f51b04efbec59d3ab53e92cf3455359fa30726
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging.

