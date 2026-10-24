# SELLO-E17 — Metas: personal goals

- **Status:** In Progress
- **Increment:** Post-MVP
- **Goal:** Let a person create goals, record contributions and withdrawals, earn milestone stamps and complete goals, as designed in Sello v2.
- **Exit:** Metas is a tab with goals, detail, Aportar/Retirar, creation and celebrations; Recibo shows one goal; backups carry goals; the sandbox and catalog cover them.

**Owner direction (2026-10-23):** Metas comes first among the new features and ships with
recurrence and Inversiones in one release ([E19](E19-investments.md)). Decisions:
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md), [ADR 0013](../../../decisions/0013-sello-v2-design.md) and the
[Metas brief](../../../design/screen-briefs/goals.md). Goals never allocate income or
change category budgets.

**Owner decision (2026-10-23):** after the SELLO-085 audit ([gap list](../../../design/v2/gaps/metas.md)),
SELLO-089–094 reuse the goal components the design system and catalog already have and
add only the listed extensions; SELLO-094's catalog part shrinks to those extensions and
the missing device tests. No ticket was removed.

## SELLO-085 — Verify what exists for Metas and what is missing

- **Type:** Task
- **Priority:** P1
- **Status:** Done
- **Depends on:** SELLO-004
- **Gate:** G1

### Outcome
An exact, current list of what the app, the design system and the catalog already have for Metas, against the approved design, so the following tickets build only what is missing.

### Deliverables
A gap list in `docs/design/v2/gaps/` for Metas: each designed element marked present (with its code and tests), partial (what differs) or missing; the following tickets updated, split or closed to match, with owner approval for any scope change.

### Acceptance criteria
- Every element of the v2 Metas, goal detail, Aportar/Retirar, create and celebration pages is checked against the running app and the code, not only the docs.
- Each present element names its code path and test; each partial one says exactly what differs.
- Work done outside these tickets (other branches, merged PRs, catalog components) is found and credited.
- SELLO-086 to SELLO-094 are revised to the list before any starts; changed scope is recorded and owner-approved.

### Tests
The audit itself, with screenshots of the running app where it differs from the design; G1.

### Working checkpoint
The gap list is merged and every later ticket in this epic matches it.

### Context and starting points
The screens have been worked on outside these tickets (owner, 2026-10-23); [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md) records the decisions. Design: [v2 README](../../../design/v2/README.md) and `docs/design/v2/components.html` (Exists / Extend / New).

### Implementation plan
1. List every designed element, state and rule.
2. Check each in the app on the emulator and in the code and catalog.
3. Write the gap list and revise the following tickets; ask the owner about scope changes.

### Concrete cases and pitfalls
- Do not trust a document's claim that something exists: open the screen and the code.
- Do not start building while auditing; findings go into the list and tickets.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-085 --retain`.

### Execution progress

- **Audit, 2026-10-23:** [gap list](../../../design/v2/gaps/metas.md) at `main` `d493c55`,
  each element checked in the code and, where drawn, in the catalog on the API-30
  emulator ([screenshots](../../../testing/evidence/SELLO-085)). The goal components exist
  in the design system and catalog (SELLO-073, #72); nothing else does, and no branch or
  open pull request holds unmerged goal work.
- **Owner decision, 2026-10-23:** SELLO-089–094 revised to reuse those components and
  add only the listed extensions; no ticket removed.

### Delivery evidence

- **Revision:** the gate tested commit `c820cc6` on `sello-085-metas-audit`, on `main` at
  `d493c55`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards.
- **Requirement mapping:** every element of the v2 Metas, goal detail, Aportar/Retirar,
  create, celebration and Recibo-goal pages is listed as present (code path and test),
  partial (what differs) or missing; the goal components from SELLO-073 are credited;
  SELLO-089–094 are revised to the list with the owner's approval.
- **Audit evidence:** six catalog screenshots on the API-30 emulator (goal cards, Recibo
  pill and reached row, movements and the Aportar/Retirar slips, milestone states, icon
  strip and pin, the completion celebration's final frame); no app screenshot, since the
  app has no Metas screen to compare.
- **Red / Green:** not applicable: no behaviour changed and no test was added; the audit is
  documentation (G1).
- **Tests:** none changed; the audit is documentation (G1).
- **Gate results:** `./scripts/verify-ticket SELLO-085 --retain` passed all six G1
  checks. Host: 750 tests, zero failures, errors or skips; lint zero errors and 34
  warnings, as on `main`.
- **Quality run:** run 20261024T022325Z-b286edb4; SELLO-085 G1 passed; HEAD c820cc6, inputs sha256 dbf8dd993689; report docs/planning/mvp/quality-reports/SELLO-085.json sha256 0e5e4ff347cec431b4773c648a99ebf6dd4883e66765a678398c6bfad500613c
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30), catalog
  debug build only.
- **Review:** executor self-review; owner approved the ticket revisions; owner review by
  merging.

## SELLO-086 — Approve goal rules and worked examples

- **Type:** Task
- **Priority:** P1
- **Status:** Done
- **Depends on:** SELLO-085, SELLO-004
- **Gate:** G1

### Outcome
Reviewed rules and independent worked examples for every goal operation, before any code.

### Deliverables
An ADR or rules document with examples: contribution, withdrawal (never above saved), stamps at 25/50/75 % and the goal, target edits, corrections with undo, past-dated movements, completion with Deshacer, deletion and Recibo's goal choice.

### Acceptance criteria
- Every decision in ADR 0017 and ADR 0013's Metas rules has at least one worked example with exact amounts.
- Milestone replay is defined: redrawing or reopening never earns a stamp again.
- The owner approves the examples; open points are listed, not assumed.

### Tests
Review of the examples by the owner; G1.

### Working checkpoint
The approved examples exist and later tickets cite them.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); Metas brief; v2 "Metas".

### Implementation plan
1. Draft rules and examples.
2. Owner review.
3. Record approval.

### Concrete cases and pitfalls
- Examples are worked out independently, not from code.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-086 --retain`.

### Execution progress

- **Owner decisions, 2026-10-23:** a percentage may pass 100 %; a correction that would
  leave less than zero saved is refused and names the withdrawal that depends on it; a
  goal marked achieved below 100 % gets all four stamps; a stamp newly reached by
  lowering the target is earned with the modest celebration.
- [Goal rules and worked examples](../../../testing/goal-examples.md): 14 rules and 38
  examples (MT01–MT38), each figure checked by hand; four further points are proposed
  there and approved by merging.

### Delivery evidence

- **Revision:** the gate tested commit `bc9d45f` on `sello-086-goal-rules`, on `main` at
  `4b2bbc5`; the only uncommitted file was the
  retained report. The status and this section are added to that commit afterwards.
- **Requirement mapping:** every ADR 0017 and ADR 0013 Metas decision has an example:
  contribution (MT01–MT06), withdrawal never above saved (MT07–MT10), stamps at 25, 50,
  75 % and Meta (MT01, MT04, MT05, MT19), target edits (MT17–MT19), corrections with
  undo (MT13–MT16), past dates (MT20–MT22), completion with Deshacer (MT23–MT26),
  deletion (MT27) and Recibo's goal (MT28–MT33). Milestone replay is rule 4 with MT11 and
  MT12. Open points were put to the owner; four smaller ones are listed as proposals.
- **Red / Green:** not applicable: no behaviour changed and no test was added; the
  examples are rules for SELLO-087–094 (G1).
- **Tests:** none changed. Every figure was worked out by hand and rechecked with a
  throwaway calculation outside the repository.
- **Gate results:** `./scripts/verify-ticket SELLO-086 --retain` passed all six G1
  checks. Host: 750 tests, zero failures, errors or skips; lint zero errors and 34
  warnings, as on `main`.
- **Quality run:** run 20261024T030500Z-52414ebf; SELLO-086 G1 passed; HEAD bc9d45f, inputs sha256 5bb9ff012656; report docs/planning/mvp/quality-reports/SELLO-086.json sha256 e57f4c7079466c7def4481c1bbeea4394618c353c06832d3cdab55c1eedc68b7
- **Device / Artifact:** none.
- **Review:** the owner answered the open points on 2026-10-23 and approves the examples
  by merging; executor self-review of every figure.

## SELLO-087 — Store goals and their movements

- **Type:** Task
- **Priority:** P1
- **Status:** Done
- **Depends on:** SELLO-086, SELLO-004
- **Gate:** G2

### Outcome
Goals and their contributions and withdrawals live in Room with exact money, operation IDs and corrections.

### Deliverables
Domain models and policies, a schema migration with exported schema, commands with receipts and undo, reads with typed failures.

### Acceptance criteria
- Migration from the current schema retains every existing row; migration tests prove it.
- Commands are atomic, idempotent by operation ID and reject a withdrawal above saved.
- Corrections and undo recompute progress and stamp state exactly as the approved examples.

### Tests
Real-Room tests for commands, migration, duplicates, stale versions and cancellation; domain example tests; G2.

### Working checkpoint
A goal history can be written and read back with the approved figures.

### Context and starting points
ARCHITECTURE §4–5; approved goal examples (SELLO-086).

### Implementation plan
1. Domain models and policies.
2. Schema, migration and DAOs.
3. Commands, reads and tests.

### Concrete cases and pitfalls
- Stamps earned are history, not a recomputed boolean: keep when they were earned.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-087 --retain`.

### Execution progress

- **Plan approved by the owner, 2026-10-23:** three pull requests: the domain policy,
  schema 4, then commands and reads. Stamps are worked out from the recorded history,
  not stored; movements take an optional 60-character note; goal names need not be
  unique.
- **Part 1, domain:** `Goal`, `GoalMovement`, `GoalTargetChange`, `GoalName` (1–40),
  `GoalId`, `GoalMovementId` and `GoalPolicy` (progress, stamp changes, dates, Recibo's
  goal, pace). `GoalPolicyTest` follows the approved examples by ID; ten deliberate
  breaks of the policy each failed at least one of them. MT39 was added to the examples
  (a later aporte does not cover an earlier withdrawal once its aporte is deleted).
- **Part 2, schema 4:** `goal`, `goal_event` (targets and movements in one recording
  order, unique per goal), `deleted_goal_event` and `featured_goal`, with no cascade;
  migration 3→4 adds them empty and keeps every row; schema exported; strict decoding
  in `GoalMapper`. `GoalStorageTest` reads a stored history back to MT07 and MT17.
  Breaking the migration or the mapper on purpose failed the tests. No command writes
  goals yet, and backup, restore and erase-all do not know them (SELLO-088); no screen
  can create one.
- **Part 3, commands and reads:** `GoalCommands` (`RoomGoalCommands`) with the
  `goal.*` receipts and `GoalReads` (`RoomGoalReads`); `FinancialStorage` offers both,
  and nothing composes them yet. `GoalCommandContractTest` runs MT01, MT07, MT08, MT13–
  MT15, MT17, MT18, MT21, MT23–MT28 and MT30 through the real commands, with replay,
  conflict, stale generation and version, the shared six-second undo and a cancelled
  caller recovering its outcome. Four deliberate breaks of the commands were each
  caught.

### Delivery evidence

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
- **Quality run:** run 20261024T045755Z-65b945af; SELLO-087 G2 passed; HEAD 05eff5a, inputs sha256 16a7d2cc5097; report docs/planning/mvp/quality-reports/SELLO-087.json sha256 4cf8887027f48c4f19f17426d0a7d4b7671179fc9446722e4f20e4a5893be86c
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging. Not here: backups,
  restore and erase-all of goals (SELLO-088); screens (SELLO-089 on). The board read
  works out every goal's whole history; a bound waits for real growth.

## SELLO-088 — Carry goals through backup, restore and erase

- **Type:** Task
- **Priority:** P1
- **Status:** Done
- **Depends on:** SELLO-087, SELLO-004
- **Gate:** G2

### Outcome
Backups include goals and movements; restore and erase-all handle them.

### Deliverables
Backup format version 2 with goals, reading version 1 unchanged; restore and reset updated; format doc.

### Acceptance criteria
- Version 1 files still restore; version 2 round-trips goals exactly.
- Erase-all removes goals; restore replaces them, never merges.

### Tests
Codec and real-Room round-trip tests, malformed-file tests; G2.

### Working checkpoint
A backup with goals restores on a fresh install with the same figures.

### Context and starting points
`docs/data/backup-format.md`; SELLO-028–031.

### Implementation plan
1. Format v2 and docs.
2. Export, validation, restore, reset.
3. Tests.

### Concrete cases and pitfalls
- Every new key is required; unknown versions are refused.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-088 --retain`.

### Execution progress

- **Owner decisions, 2026-10-24:** every backup is version 2, even with no goals (0.1.0
  cannot read it); the counts before restoring and erasing, after restoring and under
  the last backup add Metas only when either side has goals.
- Format version 2 (`goals`, `featuredGoal`) written, versions 1 and 2 read; a goal's
  history is validated with `GoalPolicy`, and a target carries no identifier in the
  file. Export, restore (replacing, never merging) and erase-all include the goal
  tables; `BackupSummary` counts goals, and the stored last backup keeps the count
  (absent in a 0.1.0 record, where it was 0).

### Delivery evidence

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
- **Quality run:** run 20261024T051745Z-ee47a098; SELLO-088 G2 passed; HEAD 53aa777, inputs sha256 42235c790a76; report docs/planning/mvp/quality-reports/SELLO-088.json sha256 8c9587900047e133ad653300f3bf8f4cdf20ea846bb171b64cab0bc58e092f8c
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30, 320 × 640).
- **Review:** executor self-review; owner review by merging. No screen shows goals yet,
  so the Metas line was checked in screen tests, not on a device.

## SELLO-089 — Add the Metas tab and goal detail

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-087, SELLO-004
- **Gate:** G2

### Outcome
Metas is a main tab with the v2 goal cards, totals band and completed goals, and each goal opens its detail.

### Deliverables
The Metas screen, goal detail and the tab, with loading, empty, failed and adaptive states.
Reuses `GoalsBandTotal`, `GoalCard`, `MilestoneRow`, `CompletedGoalRow`, `GoalPinPill`, `MovementRow`, `ColumnsChart`, `DangerSlip` and `ConfirmSlip` ([gap list](../../../design/v2/gaps/metas.md)); extends the shell's `SelloDock` with a tone and icon for the green **Aportar a una meta** / **Crear una meta**, and `CompletedGoalRow` with the LOGRADA landing.

### Acceptance criteria
- Cards ordered by highest completion; completed goals at the bottom with LOGRADA.
- The band totals active goals only, never investments.
- On this tab the dock reads **Aportar a una meta**.

### Tests
Screen and journey tests across windows, text sizes and themes; G2.

### Working checkpoint
Goals appear in Metas and open their detail.

### Context and starting points
v2 Metas and goal pages.

### Implementation plan
1. Tab and navigation.
2. Metas screen.
3. Goal detail and states.

### Concrete cases and pitfalls
- No month bar on Metas: goals are not tied to a month.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-089 --retain`.

## SELLO-090 — Create, edit and delete goals

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-089, SELLO-004
- **Gate:** G2

### Outcome
A person can create a goal with the Sello pad, edit it and delete it with confirmation.

### Deliverables
The create/edit screen with projected months only while editing, and deletion with a confirm slip.
Reuses `IconChoiceStrip`, `FormField`, `AmountKeypad`, `MilestoneSpec` captions, `DangerSlip` and `ConfirmSlip` ([gap list](../../../design/v2/gaps/metas.md)); adds the large icon circle beside the name and the pad that slides up over the form.

### Acceptance criteria
- Target amount uses the Sello pad; target date optional.
- Projected months state their assumed pace and use whole pesos.
- A completed goal can only be deleted.

### Tests
ViewModel and screen tests including process-death restoration; G2.

### Working checkpoint
A goal can be created, edited and deleted on a phone.

### Context and starting points
v2 create page; approved examples.

### Implementation plan
1. Create and edit.
2. Projection display.
3. Deletion.

### Concrete cases and pitfalls
- Never promise a completion date.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-090 --retain`.

## SELLO-091 — Record contributions and withdrawals

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-089, SELLO-004
- **Gate:** G2

### Outcome
Aportar and Retirar record movements, with past dates and corrections.

### Deliverables
The Aportar/Retirar form with the Sello pad and date pill, the movement history with edit, delete and undo.
Reuses `EntryLayout`, `SegmentedSwitch`, `EntryAmountSlip` (Gain / Ink), `FactLine` and `FieldPill` ([gap list](../../../design/v2/gaps/metas.md)); extends `SelloChip` with the goal's percentage.

### Acceptance criteria
- A withdrawal above saved is refused with a clear message.
- Past dates allowed, future refused.
- Unknown outcomes are recovered before another write.

### Tests
ViewModel, screen and journey tests; duplicate-submit and unknown-outcome cases; G2.

### Working checkpoint
Movements recorded, edited and undone update the goal exactly.

### Context and starting points
v2 Aportar page.

### Implementation plan
1. Form.
2. History with corrections.
3. Tests.

### Concrete cases and pitfalls
- Contributions never touch category budgets.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-091 --retain`.

## SELLO-092 — Earn stamps and complete goals

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-091, SELLO-004
- **Gate:** G2

### Outcome
Crossing 25/50/75 % earns a stamp with a modest celebration; reaching 100 % offers **Marcar como lograda**.

### Deliverables
Milestone detection, celebrations with reduced-motion equivalents, completion with a 10-second Deshacer.
Reuses `MilestonePlace` landing, `StampTicket`, `ReachedRow` and `CelebrationOverlay` ([gap list](../../../design/v2/gaps/metas.md)); adds the kept stamp's relight and the 10-second Deshacer, longer with TalkBack.

### Acceptance criteria
- Stamps follow the approved examples, including faded stamps after a withdrawal or target change.
- Celebrations follow only a confirmed save.
- After the Deshacer window the goal is final and read-only.

### Tests
Domain and journey tests for each crossing, replay safeguards and reduced motion; G2.

### Working checkpoint
A goal can reach each stamp and be completed with its celebration.

### Context and starting points
v2 celebrations page.

### Implementation plan
1. Milestone detection.
2. Celebrations.
3. Completion and undo.

### Concrete cases and pitfalls
- Reopening a screen is not a new crossing.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-092 --retain`.

## SELLO-093 — Show one goal on Recibo

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-092, SELLO-004
- **Gate:** G2

### Outcome
Recibo shows the chosen goal as the v2 pill with Aportar, and a row when another goal reached 100 %.

### Deliverables
The goal pill, the person's choice of goal, the fallback rule and the green 100 % row.
Reuses `GoalPill` and `ReachedRow` ([gap list](../../../design/v2/gaps/metas.md)).

### Acceptance criteria
- The person's choice wins; otherwise the highest completion; nothing with no active goal.
- When the shown goal is completed or deleted, the fallback applies.
- Not shown on a past month.

### Tests
ViewModel and screen tests for each rule; G2.

### Working checkpoint
Recibo shows the right goal in each case.

### Context and starting points
Fallback decided in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); v2 Recibo.

### Implementation plan
1. Choice storage.
2. Pill and row.
3. Tests.

### Concrete cases and pitfalls
- The pill is the progress bar; keep its exact percentage accessible.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-093 --retain`.

## SELLO-094 — Cover goals in the sandbox and catalog

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-092, SELLO-004
- **Gate:** G2

### Outcome
Sandbox scenarios and catalog examples exercise goals through the real command paths.

### Deliverables
Scenarios for stamps, withdrawals, target edits and completion; catalog states for the extensions the earlier tickets add, and device tests for `GoalPill`, `MovementRow`, `StampTicket`, `GoalPinPill` and `IconChoiceStrip`, which have none ([gap list](../../../design/v2/gaps/metas.md)).

### Acceptance criteria
- Scenarios use real commands and storage with synthetic data.
- Catalog shows every extended component in all themes and states.
- None of it ships in the release build.

### Tests
Scenario corpus tests; catalog tests; G2.

### Working checkpoint
The sandbox runs each goal scenario to its check.

### Context and starting points
AGENTS UI and developer-tools rules; ARCHITECTURE §9.

### Implementation plan
1. Scenarios.
2. Catalog.
3. Tests.

### Concrete cases and pitfalls
- Keep sandbox clocks distinct as §9 requires.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-094 --retain`.
