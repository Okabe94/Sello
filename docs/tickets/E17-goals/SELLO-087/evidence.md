# SELLO-087 — Store goals and their movements

- **Revision:** the gate tested commit `05eff5a` on `sello-087-goal-storage-3`, on `main`
  at `0e9751e` after parts 1 (#102) and 2 (#103); the only uncommitted file
  was the retained report. The status and this section are added to that commit
  afterwards.
- **Requirement mapping:** domain models and `GoalPolicy` (part 1); schema 4 with its
  exported schema, migration and retention tests (part 2); commands with operation
  IDs, receipts, generation and version checks, undo, and typed reads (part 3).
  Commands are atomic and idempotent by operation ID and refuse a withdrawal above
  saved; corrections and undo recompute progress and stamps as the approved examples
  say. Stamps are history: each keeps the day it was earned.
- **Red / Green:** written with the code and proven by deliberate breaks: ten of
  `GoalPolicy` (part 1), two of the migration and mapper (part 2) and four of the
  commands (part 3), each failing at least one test.
- **Working checkpoint:** a goal history written through the commands reads back
  through `GoalReads` with the approved figures (MT01, MT07, MT13, MT17, MT18, MT23).
- **Tests:** host `GoalPolicyTest` (38); device `GoalStorageTest` (5),
  `GoalCommandContractTest` (21), `ExportedSchemaOnDeviceTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-087 --retain` passed all seven G2
  checks, full coverage. Host: 789 tests; device: 784 tests (data 263, design-system
  136, app 364, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`.
- **Quality run:** run 20261024T045755Z-65b945af; SELLO-087 G2 passed; HEAD 05eff5a, inputs sha256 16a7d2cc5097; report docs/tickets/E17-goals/SELLO-087/quality-report.json sha256 4cf8887027f48c4f19f17426d0a7d4b7671179fc9446722e4f20e4a5893be86c
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging. Not here: backups,
  restore and erase-all of goals (SELLO-088); screens (SELLO-089 on). The board read
  works out every goal's whole history; a bound waits for real growth.

