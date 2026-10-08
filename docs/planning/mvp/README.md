# Sello MVP delivery board

**Created:** 2026-10-08 · **Scope authority:** [ARCHITECTURE.md](../../../ARCHITECTURE.md)
· **Baseline:** `0a31cc2` · **At creation:** 0/36 implementation tickets Done

This is the repository-owned Jira/Kanban plan, not a claim that a hosted Jira
project exists. Start with [BOARD.md](BOARD.md); open an epic for complete tickets.
Ticket IDs are stable local references, not remotely assigned Jira keys. No due
dates, story points, or assignees are fabricated before the team sizes the work.

## Executing without prior project knowledge

Begin with [EXECUTION_GUIDE.md](EXECUTION_GUIDE.md): checkout inspection, current
versus target files/modules, tools/environment, focused and integrated commands,
device/process-restoration procedures, official sources and delivery templates.
[DECISION_INPUTS.md](DECISION_INPUTS.md) lists decisions SELLO-001 must ratify and
independent numerical/recovery examples. The [local design reference](../../design/README.md)
does not depend on a legacy worktree. Downstream decision/test/runbook paths are
explicit deliverables of their owning tickets, not documents claimed to exist today.

Each ticket includes its reason/starting contracts, ordered implementation plan,
concrete cases/pitfalls and verification recipe in addition to acceptance/tests.
These are required executor-brief sections, not optional commentary. Public API
names/test selectors are proposed targets; reuse compatible names delivered by
predecessors and record actual selectors rather than create duplicate implementations.
If predecessor contracts/approvals or devices/keys needed by acceptance are missing,
record the exact input/owner/action instead of guessing or claiming completion.

## Release contract

The MVP includes manual COP expenses/income; categories and historical monthly
budgets; Recibo/detail; basic Resumen with actual totals, simple charts, projection
and verdict; editing/deletion/guarded undo; preferences; backup/restore/reset;
debug live testing; and a separately installable component catalog. Recibo and
Resumen are the only product tabs. Only working capabilities appear in navigation.

Goals, investments, recurrence, batch entry, analytical exports, detailed
comparisons/records, FX and budget notifications remain post-MVP. Widgets and
notification ingestion are excluded. **The sandbox must support extension to
recurrence testing when recurrence ships, not introduce a hidden recurrence engine
into this MVP.** MVP time scenarios exercise dates, budgets, projections, graph
growth, history, restart and recovery through actual financial commands.
Signed budget carryover is approved for post-MVP planning, not MVP implementation;
[ADR 0002](../../decisions/0002-signed-carryover-direction.md) defines
preparation through retained history/domain boundaries, not extra MVP tables/jobs.
MVP renews configured monthly budgets without carryover and retains historical results.

## Post-MVP follow-up

[ROADMAP.md](ROADMAP.md) tracks SELLO-E09/037–043 separately from the eight MVP
epics/36 tickets. Its seven tickets cover approved policy, exact domain replay,
transactional storage, portable recovery, accessible opt-in UI, live scenarios and
integrated release. SELLO-037 depends on SELLO-036 Done; every successor inherits
SELLO-004 quality enforcement and its own gate. This is not a new MVP prerequisite.
Exact category/global scope and carryover transitions remain decisions of SELLO-037.

Canonical epic files share this directory for stable IDs/cross-links. `Increment`
metadata is `MVP` (default for the existing eight epics) or explicit `Post-MVP`.
The generator validates both increments together, rejects MVP dependencies on
post-MVP tickets, and renders separate boards/issue/dependency exports. Future
epics MUST declare their increment explicitly; do not infer scope from ticket number.

## Epics and accountable outcomes

| Epic | Outcome | Tickets |
| --- | --- | --- |
| [SELLO-E01](epics/E01-foundation.md) | Approved contracts, reproducible build, enforced modules, CI and injection/time seams | SELLO-001–005 |
| [SELLO-E02](epics/E02-design-system.md) | Sello components and independent interactive catalog | SELLO-006–009 |
| [SELLO-E03](epics/E03-financial-core.md) | Exact money, real Room, truthful commits and consistent budget snapshots | SELLO-010–014 |
| [SELLO-E04](epics/E04-first-slice.md) | First usable category → Anotar → receipt → Recibo slice | SELLO-015–018 |
| [SELLO-E05](epics/E05-mvp-workflows.md) | Editing/undo, category management, income, Resumen and preferences | SELLO-019–023 |
| [SELLO-E06](epics/E06-live-testing.md) | Isolated time travel, repeatable histories and recovery exploration | SELLO-024–027 |
| [SELLO-E07](epics/E07-recovery-privacy.md) | Portable backups, resumable restore, safe reset and privacy boundaries | SELLO-028–032 |
| [SELLO-E08](epics/E08-release.md) | Executed device, recovery, performance and production-release evidence | SELLO-033–036 |

An epic is Done only when all child tickets are Done and its integrated outcome
works. Foundation tasks do not masquerade as user features; each still has a
verifiable artifact and leaves the existing app launchable.

## Workflow and readiness

Every ticket after SELLO-004 explicitly depends on it and MUST follow
[QUALITY_FLOW.md](QUALITY_FLOW.md): focused tests → mandatory ticket-derived gate
→ validated evidence → Review → approved Done. The runner/report validator are
004 deliverables, not installed tooling. Current board validation enforces the
dependency and non-placeholder evidence field only; hosted enforcement is not active.

`Backlog → Ready → In Progress → Review → Done`; `Blocked` is an explicit detour.

- **Backlog:** scoped, but dependencies/decisions are not yet satisfied. This is
  not a failed task. **Ready:** dependencies Done, acceptance understood, required
  decision approved, test/device prerequisites known, and write scope agreed.
- **In Progress:** one owned task; red→green evidence starts here. **Review:**
  deliverables and fresh gates available. **Done:** acceptance + tests + runnable
  integration checkpoint + review all passed; code merely written is not Done.
- **Blocked:** record blocker, owner/action, affected IDs and next check; do not
  erase the original status or hide missing device/signing evidence.
- Limit implementation WIP to **one ticket per contributor**, and Review to two
  tickets per reviewer. Read dependency links, not epic numbering, as execution order.
  Do not parallelize overlapping schemas, composition wiring or financial policies.
- Only SELLO-001 and SELLO-002 start Ready. No implementation ticket is complete
  because this plan or the initial Git commit exists. Reopen Done tickets only
  with a stated regression; never silently weaken acceptance.

## Definition of done — every ticket

1. Deliverables are checked in and satisfy every acceptance criterion. Changes
   respect architecture/source-set boundaries and do not activate deferred features.
2. Behavior changes have a meaningful failing test followed by passing evidence;
   pure build/docs tasks instead show negative/configuration/link checks as applicable.
3. Ticket-specific tests and its gate pass on the integrated branch. Record exact
   command/environment, revision, result counts, report paths, and device/API where
   relevant. A skipped gate is an outstanding requirement, not a pass.
   For SELLO-004 onward, use the mandatory runner delivered by 004 and record
   validated `Quality run` evidence before Review/Done; see QUALITY_FLOW.md.
4. Existing completed journeys still work. No successful stubs, inaccessible
   placeholders, destructive migrations, compile failures, or failed read → zero.
5. Applicable catalog examples and sandbox scenarios grow with the change.
   Accessibility, process restoration, financial bounds and error handling are
   checked where the ticket touches them; the final epic does not postpone quality.
6. Reviewer checks code, financial/recovery semantics, release isolation and
   evidence. Update the canonical ticket status/evidence, then regenerate the board.

Add a `### Delivery evidence` section to completed tickets with requirement mapping,
red/green commands, integration/device results, review and unresolved issues.
Do not rewrite acceptance after execution just to make a ticket pass.

Use these required evidence fields (no placeholder values): `Revision`,
`Requirement mapping`, `Red / Green`, `Gate results`, `Device / Artifact`, and
`Review`, each written as `- **Field:** evidence`. State why a red test/device gate
is inapplicable for a pure documentation task; do not waive a required G2/G3 test.
SELLO-004 and later additionally require `Quality run` before Review as well as
Done; bootstrap tickets 001–003 are exempt from that field.

## Gate profiles

Use the agreed JDK/SDK environment from SELLO-002; the inspected local baseline is:

```bash
export ANDROID_HOME=/home/okabe94/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
export PATH="$JAVA_HOME/bin:$PATH"
```

- **G0 — scaffold:** `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease --continue`.
  Used until module bootstrap lands; unsigned release assembly is not publishing.
- **G1 — integrated host:** after SELLO-003, run the commands below plus the
  architecture/format/static checks introduced by SELLO-004. CI must keep one
  documented aggregate gate synchronized with actual Gradle task names.

```bash
./gradlew :domain:test :data:testDebugUnitTest :design-system:testDebugUnitTest \
  :app:testDebugUnitTest :catalog:testDebugUnitTest lintDebug \
  :app:assembleDebug :app:assembleRelease :catalog:assembleDebug --continue
```

- **G2 — affected device boundaries:** G1 plus appropriate real-Room/Compose tests
  on a pinned emulator/device. Use `:data:connectedDebugAndroidTest`,
  `:design-system:connectedDebugAndroidTest`, `:app:connectedDebugAndroidTest`,
  and `:catalog:connectedDebugAndroidTest` as applicable. Assemble test APKs if no
  device is available, but do **not** mark device acceptance complete.
- **G3 — release:** G2 across agreed API/window/font configurations, optimized
  production composition/artifact inspection, performance evidence, and signed
  release installation/backup smoke. Separate ephemeral CI signing from upload keys.

Focused tests run first; the gate is the final integrated checkpoint. Documentation
names expected new test behavior, not pretend pre-existing test classes.

## Milestones and safe integration

1. **Reproducible base:** SELLO-001–005, 010. Existing app still launches.
2. **Component foundation:** SELLO-006–009; catalog independently launches.
3. **Walking financial slice:** SELLO-011–018 and 024; real save/read and time seam.
4. **Complete daily use:** SELLO-019–023 and 025–027; expense/income history and graphs.
5. **Recoverable data:** SELLO-028–032; backup, restore and reset executed end-to-end.
6. **MVP candidate:** SELLO-033–036; release evidence, not just a green compile.

Tickets may interleave by dependencies. New standalone modules/tests can ship
before their product UI, but must not replace real workflows with mock success.
Screens enter navigation only when their capability works. Keep each change small
enough to review; split a ticket explicitly if it ceases to be a cohesive deliverable.

## Board maintenance and Jira handoff

Canonical sources are the eight epic Markdown files. `BOARD.md`, `jira-import.csv`
and `dependency-links.csv` are generated views; do not edit them independently.

```bash
python3 docs/planning/mvp/board.py
python3 docs/planning/mvp/board.py --check
python3 -m unittest discover -s docs/planning/mvp -p test_board.py
```

The generator checks IDs, required ticket/executor-brief sections, dependency existence/cycles,
active/ready status prerequisites, and required completion-evidence fields. CSV includes
scope in each description. The same commands also regenerate/check `ROADMAP.md`,
`roadmap-jira-import.csv` and `roadmap-dependency-links.csv`. MVP exports keep eight
epics/36 tickets; roadmap exports contain only post-MVP issues, while their links
can reference MVP prerequisites. Import MVP issues first and map both sets' local
IDs when applying roadmap dependency links; no hosted import has been performed.
CSV includes
full ticket descriptions/tests/checkpoints and numeric hierarchy IDs. Jira imports
require a configured target project and field/work-type/workflow mapping: map Issue
ID + Parent to the importer hierarchy fields, Summary, Description, Priority,
Status and Labels. Task/Story and Backlog/Ready may require project-specific mapping.
The local ID is also in Summary/Description/Labels so it survives assigned Jira keys.
Descriptions include the canonical repository source path; links inside a ticket
resolve relative to that file, not to Jira's host. Open the checkout source when
following those links; use the repository execution guide for shared tools/resources.
Use a **dry-run/small sample first**; no remote project has been created or imported.
Dependency links are a separate export: map local IDs to assigned keys and create
`blocks` links; issue CSV import alone does not establish those links. If hosting
in Jira, define the synchronization owner/process before maintaining two status sets.

Official import guidance: https://support.atlassian.com/jira-cloud-administration/docs/import-data-from-a-csv-file/

## Baseline evidence

See [BASELINE.md](BASELINE.md) for actual repository/build/device observations.
The initial app is still the generated greeting with sample tests; architecture
modules, financial workflows, catalog, sandbox and CI are planned, not implemented.
