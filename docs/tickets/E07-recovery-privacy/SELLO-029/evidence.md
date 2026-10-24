# SELLO-029 — Deliver real document-picker backup creation

- **Revision:** the gate tested commit `56af035` on `sello-029-backup-save`, on `main`
  at `54b47f0`; the only uncommitted file was the previous run's retained report.
  The Quality run line below is added to that commit afterwards.
- **Decisions:** the owner chose, on 2026-10-22, from a concept mock-up: the disclosure
  always on screen above the button (D08/P01–P02, before the destination is chosen);
  "Último respaldo" keeps date, counts, size and file name; a process that dies
  mid-save leaves an unknown outcome and a new file is offered, with no attempt to
  reopen the old one; a failed save leaves the document alone and warns about a
  possibly incomplete file; the mock-up's Spanish copy. Added during delivery: the
  Respaldo screen has no Anotar dock, so its button is the only action, and the
  disclosure's bullet list is the new design-system `BulletList`.
- **Requirement mapping:** domain ports `BackupExports`, `DocumentWriter` and
  `BackupRecords` (`domain/port/Backups.kt`); data adapters `BackupExport` (SELLO-028
  encoder, read-back check) and DataStore keys `backup_*_v1`, which are not portable
  preferences. `AndroidDocumentWriter` opens the picker's document with `wt`, writes,
  flushes, syncs where possible and closes; any refusal, write or close error is
  `Failed`. `BackupSession` (one per process) records the start before the first byte
  and the outcome after close, so leaving the screen does not cut a file short.
  `BackupViewModel`/`BackupScreen`: Configuración → "Tus datos" → Respaldo; the system
  `CreateDocument` dialog suggests `sello-respaldo-AAAA-MM-DD-HHmm.json`; dismissing it
  changes nothing; "Guardado" lands only after close; failure and unknown-outcome
  slips; the picker's answer reaches a recreated process through the saved name. No
  storage permission was added, and there is no restore action (SELLO-030).
- **Red / Green:** mutations each turned tests red: "Guardado" set before the document
  closed and an unrecorded start (`guardadoComesOnlyAfterTheDocumentIsClosed`), no
  unknown-outcome check (`aStartLeftByAProcessThatDiedIsAnUnknownOutcomeNotASave`), and
  a writer that reported success on an I/O error (four of six
  `DocumentWriteContractTest` cases). The first device run of the test provider
  failed because it ran without the Kotlin library; it is Java.
- **Tests:** host `BackupViewModelTest` (12), `SettingsViewModelTest` (+2) and
  `DataStoreUserPreferencesTest` (+4); device `DocumentWriteContractTest` (6, a real
  test content provider: bytes intact, replace not mix, refused, read-only, cut-off
  pipe, not a document) and `BackupFileJourneyTest` (4: Configuración → Respaldo →
  save, then the file read back by `BackupCodec` as G01's counts and amounts with no
  revision or row change; dismissed picker; a broken document keeps the last backup;
  20,000 synthetic expenses saved while the main thread never stalls 700 ms). Catalog
  `feedback.bullet-list` and `feedback.backup-states`.
- **Gate results:** `./scripts/verify-ticket SELLO-029 --retain` passed all six checks,
  full coverage. Host: 632 tests; device: 632 tests run fresh; zero failures, errors or
  skips. Lint had zero errors and the same 33 warnings as SELLO-028, none in changed
  code (a `toUri` warning in the writer was fixed before this run). Earlier runs of
  this commit's code failed only on the shared emulator: once left in the sandbox by
  the evidence walk, once when another session reinstalled the app mid-run (the AVD
  was then restarted); later runs were redone after HANDOFF and the generated board
  views were updated.
- **Quality run:** run 20261022T234722Z-f2a44ec0; SELLO-029 G2 passed; HEAD 56af035, inputs sha256 96d2bab9b4a9; report docs/tickets/E07-recovery-privacy/SELLO-029/quality-report.json sha256 6470d3f5f5ab337b006fdee2e35e2d9c3373eacc88283a304dee6c294410ce47
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`,
  serial `emulator-5582`. On the same AVD, with the sandbox's G01 scenario, the real
  system picker (DocumentsUI → Downloads) saved two files; the second,
  2,297 bytes, read back through `BackupCodec` as one category, three expenses and one
  income ([screenshots and file check](artifacts)). No
  physical device or personal data was used.
- **Review:** executor self-review covered success only after close, no write on a
  dismissed picker, the unknown outcome, and that nothing financial changes. Done is
  proposed by this ticket's pull request; its merge is the designated owner
  acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is
  claimed.

