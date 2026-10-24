# 0014 — Change-aware quality gates

**Owner:** project owner (user) · **Date:** 2026-10-22
**Status:** user-authorized implementation; supersedes ADR 0009's documentation and device selection rules.

## Scope and decision

Owned scope: quality selection/runner/evidence and tests, the Quality workflow,
architecture/agent verification guidance and active developer/planning Markdown
that describes local or hosted verification. No app behavior, ticket acceptance,
ticket status, dependency/toolchain upgrade or permission change is included.

G0 becomes a Python/Git-only documentation gate for candidates advancing no ticket.
It runs board structure/tests, quality-tool tests and architecture checks. G1 keeps
all existing host tasks, including release assembly. G2 keeps that full host gate
and selects device modules by reverse dependency closure: app → app; catalog →
catalog; data → data/app; domain → data/app; design-system → design-system/catalog/app.
Only recognized module source and data schema paths qualify. Build/lock/workflow/
quality tooling changes, unknown paths, or an uncertain comparison select all four
device modules. Documentation-only ticket advancement still requires its gate and
full device coverage. Deleted files and both ends of moves count.

Nightly/manual hosted runs and explicit `--gate G2` remain full G2. Default local
ticket commands remain full; `--affected BASE` opts into the same selection used
for PRs. Reports explicitly distinguish full, affected and documentation coverage,
record base/paths/modules and bind this selection to every part. Missing selected
results fail; unselected modules are not represented as executed. Ticket minimum
gates and G3 refusal remain. Historical schema-v1 reports stay readable; new
schema-v2 reports require coverage metadata.

Documentation-only pushes can use G0 too, avoiding redundant Android work after a
documentation merge. Code pushes retain G1: trusted main builds populate task-output
caches consumed read-only by PRs. Skipping those builds or promoting untrusted PR
caches is outside this decision; do not claim code-merge G1 has been eliminated.
One required `quality` check always reports; no event/path filter or blanket success.

## Consequences and verification

Narrow PRs use fewer runners; app-dominated PR wall time may change little.
Documentation checks need no Java/SDK/emulator. New module/dependency changes fall
back to full until the explicit graph is reviewed. Tests must cover reverse
closure, moves/deletions, unknown paths, ticket advancement/minima, local/CI parity,
missing/wrong parts, malformed scope and old report compatibility. Final tooling
changes require a full integrated G2 run. No runtime saving is established by
configuration or mocked tests alone.

## Amendment 2026-10-24 — evidence bound to the change set

**Owner:** project owner (user) · **Status:** user-authorized implementation.

Completion evidence was bound to a fingerprint of the whole tree, so any merge to
`main` made every open pull request's retained report stale, whether or not a file
overlapped. The hosted run checks out the merge result, so the report could only
match after the branch took `main` again and the executor reran the full gate.

From this amendment the report records two fingerprints. The whole-tree input
fingerprint stays for the record and still guards a run against edits while checks
execute. Completion evidence is bound to the new change fingerprint: the normalized
hunks between the merge base with `main` and the tree, without positions, so the same
edit hashes the same after unrelated changes land, even elsewhere in the same file,
and any difference in the edit itself, including line order, changes it. The
validator accepts a Review/Done report when its change fingerprint is one the
candidate contains: locally the working tree against `main`; with a base, any
first-parent step from HEAD back to that base, so a batched merge group or a
multi-commit push binds each pull request on its own. The gate itself still runs on
the combined tree, for pull requests and for merge queue entries, which are planned
like pull requests from the group's base. The informative handoff joins the
exclusions, since no check reads it. Schema-v2 reports gain an optional
`snapshot.changes`; a ticket entering Review or Done needs it, earlier Done tickets
keep their historical reports.

Consequences: a branch behind `main` needs no update and no rerun unless GitHub
reports a conflict, in which case the change set really changed. Executors update
with `main` once before the final gate; the merge queue (ADR 0009) does the rest. A combination that only fails when two
tickets meet is caught by the hosted run on the merge result instead of by a local
rerun; that run already existed and is the one that protects `main`. Tests cover
the same edit on a moved `main`, the same file edited elsewhere, a different edit,
reordered lines, bookkeeping-only edits, exclusions, binary and untracked files,
committed and working trees, the first-parent chain, and a merge group event.
