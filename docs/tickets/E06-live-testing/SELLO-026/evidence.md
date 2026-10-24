# SELLO-026 — Add truthful failure, receipt and recovery diagnostics

- **Revision:** tested snapshot is the `sello-026-fault-diagnostics` working tree on
  `main` at `1fcca96`, uncommitted; it is identified exactly by the retained input
  fingerprint below. The commit and pull request follow this evidence; no claim is
  made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-22, export through the Android share sheet
  as plain text, faults only in the sandbox, and including the interruption-after-save
  fault.
- **Requirement mapping:** `DebugFaults` arms, in memory only, a failed read, a lost
  answer after a real commit, an interruption after a real commit and a failure before
  storage, each counted and cleared by "Quitar fallos" or a restart. `Faulty*` command
  wrappers sit inside the write fence and `FaultyMonthlySnapshots`/
  `FaultyResumenSnapshots` report a failed read with the last good snapshot, all bound
  by `DebugComposition` only in the sandbox. None changes a figure or a receipt.
  `OperationLog` records what each caller was told and `OperationInspector` reads what
  storage holds through the commands' `find`. The runner gains `Expect` (saved, already
  saved, a named rejection, invalid input), stops on an interruption with the same
  operation to retry, and the Bordes scenario scripts a double submit, a stale edit
  (`VersionConflict`), an undo replaced by a later deletion (`UndoExpired`) and an
  amount past the maximum (`AboveMaximum`). "Exportar diagnóstico" shares the session,
  armed faults and operations (IDs, kinds, outcomes, revisions) with no name, note or
  amount. The release check also looks for the fault and diagnostic names.
- **Red / Green:** a lost answer that skipped the write, a read failure that dropped
  the last good snapshot, and faults that never counted down each turned
  `DebugFaultDecoratorTest` red. The release check finds the new names in the debug
  APK and none in release. Rollback evidence remains the real-Room command contract
  tests in `:data`; the "before storage" fault is labelled as proving nothing about it.
- **Gate results:** `./scripts/verify-ticket SELLO-026 --retain` passed all six checks.
  Host: 567 tests (6 new); device: 616 tests (6 new in
  `ReceiptRecoveryDiagnosticJourneyTest`); zero failures, errors or skips. Lint had zero
  errors and the same 33 warnings as SELLO-025, none in changed code.
- **Quality run:** run 20261022T134026Z-ded8d85c; SELLO-026 G2 passed; HEAD 1fcca96, inputs sha256 f2c8afa26aa7; report docs/tickets/E06-live-testing/SELLO-026/quality-report.json sha256 30d7e1386d720eb4f26250eae244e5f320b31819e0d8b4d496395ad8695189ca
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. On the same AVD, with "Respuesta perdida"
  armed, a 5.000 expense entered through the real entry screen was recovered by its
  receipt and announced once; Recibo went from 42.000 to 37.000; the inspector showed
  "unknown (simulated)" beside "saved, rev 9"; export opened the share sheet
  ([screenshots](artifacts)). The sandbox was left afterwards.
  No physical device or personal data was used; no hosted CI run is claimed.
- **Review:** executor self-review covered no invented outcomes, sandbox-only faults,
  clearing, redaction and release exclusion. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.
- **Acceptance migrated:** 4919ae6e7eb14420af55a0aec51123543ddf98f9f9a1bb3a56844b5177428c3f → a4f8aefa44bb61bd6c5c7a3d5e941504a9fd60ad1e89d68964fd31a1f9b75bbc on 2026-10-24; only link paths changed when the ticket moved into its folder.
