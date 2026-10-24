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
- **Status:** Backlog
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

## SELLO-087 — Store goals and their movements

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
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

## SELLO-088 — Carry goals through backup, restore and erase

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
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
