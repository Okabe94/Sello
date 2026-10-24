# SELLO-088 — Carry goals through backup, restore and erase

- **Revision:** the gate tested commit `53aa777` on `sello-088-goal-backups`, on `main` at
  `44f9e2e`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards.
- **Requirement mapping:** version 2 round-trips goals exactly, byte for byte
  (`valid-goals-v2.json`); version 1 files still restore, with no goals; every new key
  is required, version 1 takes none of them and an unknown version is refused; erase-all
  removes goals and restore replaces them, never merges.
- **Red / Green:** written with the change. Five existing tests failed first because
  they pinned version 1 as the written format and version 2 as unsupported; they now
  say version 2 is written and version 3 is refused. `GoalBackupTest` covers each
  malformed goal (missing key, a withdrawal above saved, order, a target with an id, a
  movement without a date, a zero amount, a name not in stored form, a dangling choice,
  a duplicate id).
- **Working checkpoint:** `GoalBackupRestoreTest` backs up MT07 plus a completed goal
  and Recibo's choice, restores the file on a second, fresh database and reads the same
  saved amounts, percentages, stamps, completion and choice (1.310.000, 43 %).
- **Tests:** host `GoalBackupTest` (new, 8), `BackupValidatorTest` and
  `BackupRoundTripTest` (updated); device `GoalBackupRestoreTest` (new, 3),
  `ResetScreenTest` and `StartupFailureScreenTest` (+1 each), `RestoreCommandContractTest`
  (updated).
- **Gate results:** `./scripts/verify-ticket SELLO-088 --retain` passed all seven G2
  checks, full coverage. Host: 797 tests; device: 789 tests (data 266, design-system
  136, app 366, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`.
- **Quality run:** run 20261024T051745Z-ee47a098; SELLO-088 G2 passed; HEAD 53aa777, inputs sha256 42235c790a76; report docs/tickets/E17-goals/SELLO-088/quality-report.json sha256 8c9587900047e133ad653300f3bf8f4cdf20ea846bb171b64cab0bc58e092f8c
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging. No screen shows goals yet,
  so the Metas line was checked in screen tests, not on a device.

