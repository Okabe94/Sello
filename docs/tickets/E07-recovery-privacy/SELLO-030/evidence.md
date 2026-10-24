# SELLO-030 — Implement staged atomic restore and resumable preference completion

- **Revision:** the final gate tested commit `a7cfc11` on `sello-030-restore-at-start`,
  stacked on part 1 (`8f3c6f7`, PR #80) over `main` at `025f903`; the only
  uncommitted file was the previous run's retained report. The Quality run line below
  is added to that commit afterwards.
- **Decisions:** see Execution progress: the owner's 2026-10-22 choices from the
  concept mock-up (two parts, staged-file pending preferences, `sello-unreadable.db`
  never recovered, restart into Recibo, mock-up and D09 copy). Added during delivery:
  no typed word for a restore at start-up, and new preview/interrupted copy following
  frames 2–4, both offered for owner review in the pull request.
- **Requirement mapping:** whole-file check before preview (`BackupCodec.read`/`check`);
  counted preview tied to generation/revision, refreshed when stale; one replacement
  transaction with a new generation, the backup zone and a `history.restore` receipt,
  so older drafts, undo and commands are refused; replay by operation identifier after
  an unknown outcome or crash; preferences applied after commit from the private
  staged file, D09 slip naming only failed groups, retry without a second replacement,
  resumed at start-up; staging cleared on cancel, completion, expiry and set-aside;
  restart on leaving. Reachable from the start-up failure screen (ADR 0008) without
  the financial graph shown. No permission or URI grant was added; backups carry no
  debug jobs, permissions or internal receipts.
- **Red / Green:** mutations each turned tests red: no staleness check and no replay by
  receipt (part 1); restoring without a checked file, retrying after a restore opened
  the new store, no lock while restoring, the new store published mid-restore, and no
  staging cleanup on set-aside (part 2). The first part-2 gate failed only the
  architecture check (the start-up screen imported the backup feature); the shared
  slips moved to `presentation/restore`.
- **Tests:** host `RestoreViewModelTest` (12), `StartupTest` (16, +8),
  `StartupFailureViewModelTest` (20, +10); device `RestoreCommandContractTest` (13,
  real Room), `RestoreAfterSetAsideTest` (2, real files), `RestoreJourneyTest` (3),
  `StartupFailureScreenTest` (16, +8).
- **Gate results:** `./scripts/verify-ticket SELLO-030 --retain` passed all six checks,
  full coverage. Host: 662 tests; device: 658 tests (data 216, design-system 122, app
  299, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as SELLO-029, none in changed code.
- **Quality run:** run 20261023T020912Z-17275c57; SELLO-030 G2 passed; HEAD a7cfc11, inputs sha256 7ddc6479d7a2; report docs/tickets/E07-recovery-privacy/SELLO-030/quality-report.json sha256 3a4ae5b436ca5ff96a2a2af7db0b73e96a691e086b964f134bf0281670a87cf3
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`,
  serial `emulator-5582`. On the same AVD, with the database overwritten by random
  bytes, the start-up failure screen restored a synthetic G01 backup chosen through the
  real system open dialog (selected with the keyboard; a tap did not return the file
  on this image) and Recibo opened on it; the database then held one category, three
  expenses, one income, generation 2 and one `history.restore` receipt, with
  `sello-unreadable.db` kept and staging empty
  ([screenshots](artifacts)). In-app screenshots come from
  `RestoreJourneyTest`. No physical device or personal data was used.
- **Review:** executor self-review covered no change before confirmation, one
  replacement per operation, the pending-preferences wording, and that nothing at
  start-up opens a second store. Done is proposed by this ticket's final pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

