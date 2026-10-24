# SELLO-031 — Deliver fenced two-confirmation reset and recovery

- **Revision:** the gate tested commit `be535f4` on `sello-031-reset`, on `main` at
  `3645778`; the only uncommitted file was the retained report. The Quality run line
  below is added to that commit afterwards.
- **Decisions:** the owner approved, on 2026-10-22, from a concept mock-up: its own
  screen from Configuración → Tus datos; every stored preference back to first-run,
  including correction hints and "Último respaldo"; changed counts return to the first
  confirmation and need the word again; one pull request; the mock-up's copy. Added
  during delivery: the database file is compacted after the erase so no erased row
  survives in free pages or the write-ahead log, and a failed preference reset names
  both groups because they are cleared in one DataStore edit.
- **Requirement mapping:** counted preview tied to generation/revision, exact trimmed
  `BORRAR`, stale counts refused (`ResetViewModel`, `ResetSession`, `ResetScreen`);
  `RoomResets` erases every financial row, `deleted_expense`/`deleted_income` and all
  receipts in one transaction, keeps one `history.reset` receipt with no subject, sets
  the device zone and a new generation; old drafts, undo and sandbox runners are
  refused by the generation; `pending.properties` holds only the operation, written
  before the transaction, so a restart finishes a committed erase (files, compaction,
  preferences once) and forgets an uncommitted one; the restore folder and
  `sello-unreadable.db` with companions are deleted (ADR 0008); restart into first-run
  Recibo. External backup files are not touched and the screens say so; no permission
  was added.
- **Red / Green:** mutations each turned tests red: no compaction, no staleness check,
  no replay by receipt, receipts kept, the set-aside copy kept
  (`ResetGenerationContractTest`) and no exact-word check (`ResetViewModelTest`).
- **Tests:** device `ResetGenerationContractTest` (10, real Room and files),
  `ResetReuseJourneyTest` (2), `ResetScreenTest` (10), `ScenarioReplayTest` (+1, a
  sandbox runner refused after a reset); host `ResetViewModelTest` (12).
- **Gate results:** `./scripts/verify-ticket SELLO-031 --retain` passed all six checks,
  full coverage. Host: 674 tests; device: 681 tests (data 226, design-system 122, app
  312, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as before, none in changed code.
- **Quality run:** run 20261023T025243Z-f7973252; SELLO-031 G2 passed; HEAD be535f4, inputs sha256 6f33a2bf7319; report docs/tickets/E07-recovery-privacy/SELLO-031/quality-report.json sha256 cf4505d4767463f1bd6be4280dc2c25d670fed0582154b8a77c05a53f3e5de31
- **Device / Artifact:** on the isolated synthetic API-30 AVD `ci30` (`emulator-5582`),
  a synthetic G01 backup was restored, then erased: wrong word left "Borrar todo"
  disabled; afterwards every financial table was empty, one `history.reset` receipt
  remained, the write-ahead log was empty and no category name or note was found in
  the database bytes; first-run Recibo opened; the backup file was still 3,066 bytes and
  restoring it again worked across the new generation
  ([screenshots](artifacts)). No physical device or personal
  data was used.
- **Review:** executor self-review covered no change before the word, one erase per
  operation, what survives, and that backups are not claimed erased. Done is proposed
  by this ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.

