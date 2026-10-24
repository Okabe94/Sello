# SELLO-034 — Prove integrated data retention and recovery failure safety

- **Revision:** the gate tested commit `204c417` on `sello-034-recovery-matrix`, on
  `main` at `6074176`; the only uncommitted file was the retained report. The Quality
  run line below is added to that commit afterwards.
- **Decisions:** the owner approved, on 2026-10-22: all nine sandbox scenarios through a
  backup and restore; fixing here that Respaldo's top-bar arrow left a restored
  history without restarting; one pull request.
- **Requirement mapping:** [recovery matrix](../../../testing/recovery-matrix.md) lists the
  supported schema (1→2→3, open 3; newer refused to the start-up failure screen) and
  backup-format pairs (1 into schema 3; newer refused), every restore, reset, start-up
  and storage failure point with its outcome and test, and corrupt/unavailable storage
  behaviour. `RecoveryRetentionTest` chains a hand-written version 1 history (archived
  category, budget history, past-month limits, ordered expenses with notes, salary and
  other income, Lima zone, a receipt) through upgrade, backup with preferences,
  restore into a Bogotá installation, reset and restore again, asserting exact rows,
  zone and generation at each step; restored records start at version 1, as the
  backup format defines. `TemporalScenarioCorpusTest` round-trips all nine scenarios:
  identical rows for all, and the seven with a final check after their last saved
  change match all 24 figures again. `ScenarioReplayTest` shows a scenario in a new
  process after a reset finds no receipts, does not advance and writes nothing. No
  pre-v1 migration is invented; the older app's database stays unsupported.
- **Red / Green:** `theTopBarsBackArrowAfterARestoreStartsTheAppAgain` failed first
  (the arrow went back without restarting) and passed once `BackupRoute` sends a
  replaced history's arrow through `RestoreAction.Leave`. Adding it exposed that the
  restore journey tests depended on run order through the process-wide session; each
  test now resets it. The retention test's first expectation of kept record versions
  was wrong against the format and was corrected from the format, not the output.
- **Tests:** device `RecoveryRetentionTest` (1), `TemporalScenarioCorpusTest` (+1),
  `ScenarioReplayTest` (+1), `RestoreJourneyTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-034 --retain` passed all six checks,
  full coverage. Host: 674 tests; device: 691 tests (data 229, design-system 122, app
  319, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as before. An earlier run of this work failed only ktlint on two new test
  files, with every test passing; they were formatted and the gate run again.
- **Quality run:** run 20261023T045232Z-335c50c5; SELLO-034 G2 passed; HEAD 204c417, inputs sha256 e47e3fe8b175; report docs/tickets/E08-release/SELLO-034/quality-report.json sha256 8b02e8ae2b5977ea40b19396fac57584265a0981867feec2efe08f1455879f0b
- **Device / Artifact:** the isolated synthetic API-30 AVD `ci30` (`emulator-5582`);
  the corpus run logs each scenario's round trip under `ScenarioCorpus`. No physical
  device or personal data was used.
- **Review:** executor self-review covered the version pairs, the expected values'
  independence and the reset boundary. Found and left: the sandbox labels steps erased
  by a reset "Sin escritura, como se esperaba" (recovery matrix, known gaps). Done is
  proposed by this pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.

