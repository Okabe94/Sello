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
