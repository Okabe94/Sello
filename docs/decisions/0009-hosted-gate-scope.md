# 0009 — What the hosted check runs, and when

**Owner:** project owner (user) · **Updated:** 2026-10-16
**Status:** approved hosted gate policy.

The required `quality` check always reports; the workflow has no path filter.
`scripts/verify-ticket` chooses what runs:

| Event | Gate |
| --- | --- |
| Pull request | G2; G1 only when every changed path is a document and no ticket advances |
| Push to `main` | G1 through `--landed` |
| Nightly or manually requested run | G2 on `main` |

G1 covers board/tool tests, architecture rules, formatting, host tests, lint and
assembly. G2 adds API-30 emulator tests. Ticket minimum gates still apply.
Completion evidence is validated with `board.py --check --base` on every run.

## Documentation-only candidates

A document is a path under `docs/` except Python files, or a top-level Markdown
file. Workflow files, scripts, build files and module contents do not qualify.
Count deleted paths, both ends of moves and uncommitted files. Uncertainty means G2.

Candidates advance tickets that are In Progress or Review, or became Done since
the base revision. They must run those tickets' required gates even if only
documents changed. Documentation alone cannot bypass required device verification.

## Conditions and evidence

The lighter push check relies on `main` requiring up-to-date PRs and a passing
`quality` check, enforced for administrators too, with squash merges of the checked
candidate. If those conditions change, restore device checks on pushes to `main`.
Device-only failures may otherwise be detected by the nightly run up to a day later.

Changed tested inputs, including merging `main` into a branch, need fresh completion
evidence. Local ticket gates and retained-report rules follow
[QUALITY_FLOW](../planning/mvp/QUALITY_FLOW.md).
Verification is covered by `scripts/quality/test_candidate_gate.py` and
`scripts/quality/test_workflow.py`: gate selection, workflow coverage, completion
evidence and failures that must not be ignored.
