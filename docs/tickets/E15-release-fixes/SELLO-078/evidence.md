# SELLO-078 — Say what failed when a backup does not pass its own check

- **Revision:** the gate tested commit `7bc0fa5` on `sello-078-backup-self-check`, on
  `main` at `cdcd1d4`; the only uncommitted file was the retained report. The status
  and this section are added to that commit afterwards.
- **Requirement mapping:** a history over the format's limits (`TooLarge`,
  `TooManyRecords`, `HistoryTooLong`) still reads "Tus datos superan lo que cabe en un
  respaldo"; every other refusal of the app's own file reads "Falla de Sello al armar
  el respaldo; tus datos no cambiaron. No uses el archivo vacío que pudo quedar." Both
  keep the empty-file warning and show nothing of the refused data.
- **Red / Green:** `BackupPreparationFailureTest.onlyAHistoryBeyondTheFormatsLimitsIsTooLarge`
  failed first (`expected:<TooLarge> but was:<Unrestorable>`) and
  `BackupViewModelTest.aFileThatFailsItsOwnCheckForAnotherReasonIsNotCalledTooLarge`
  failed with `expected:<SelfCheck> but was:<TooLarge>`; both pass with the mapping.
- **Tests:** host `BackupPreparationFailureTest` (new, 2) and `BackupViewModelTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-078 --retain` passed all seven G2
  checks, full coverage. Host: 748 tests; device: 754 tests (data 236, design-system
  136, app 361, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`.
- **Quality run:** run 20261024T010135Z-e9161a71; SELLO-078 G2 passed; HEAD 7bc0fa5, inputs sha256 521fe8e93d21; report docs/tickets/E15-release-fixes/SELLO-078/quality-report.json sha256 31f6211075cf167aebaa8ba9d88422d9d542a6328e3ecb37390f266a2a305e50
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging. Not shown on a device: no
  stored history makes the app's own check refuse its file now that SELLO-036 fixed the
  clock, so the new message is covered by the view-model and mapping tests only.
