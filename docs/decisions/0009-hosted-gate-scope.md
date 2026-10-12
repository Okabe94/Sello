# 0009 — What the hosted check runs, and when

**Status:** Accepted · **Date:** 2026-10-12
**Owner/approver:** project owner (user) · **Ticket:** none; a change to the quality flow itself

## Context

The hosted `quality` check ran the whole device gate (G2: host checks, an emulator
boot and every device test, about eleven minutes) on every pull request and again on
every push to `main`. Over the sixteen tickets of E11 that meant:

- a pull request that changed only documents booted an emulator twice, once on the
  pull request and once on `main`, and the one failure seen on `main` in that time
  was a timing race in a device test on such a commit;
- every merge was verified twice on identical content: `main` requires a pull
  request to be up to date and checks it as its merge commit, and merges are squashed,
  so what lands is the tree the pull request's run already tested.

The owner wanted the run on `main` kept, lighter, not removed.

## Decision

The workflow still has no path filter, so the required check always reports. What a
run executes is decided inside the job by `scripts/verify-ticket`:

| Event | Runs |
| --- | --- |
| Pull request | G2, as before. G1 when the candidate advances no ticket **and** every changed path is a document. |
| Push to `main` | G1 (`--landed`): board and tool tests, architecture rules, ktlint, host tests, lint, debug and release assembly. |
| Nightly, and on request | G2 on `main`. |

- A **document** is a path under `docs/` that is not a Python file, or a Markdown file
  at the top of the repository. The workflow, `scripts/`, build files and every
  module are not. Deletions, both ends of a move and uncommitted files count.
  Anything uncertain is G2.
- A candidate that advances a ticket (In Progress, Review, or Done since the base)
  runs that ticket's gate whatever it changes, so a ticket cannot be completed by a
  documents-only pull request without its device gate.
- Completion evidence (`board.py --check --base`) is validated on every run, in
  every mode.

## Alternatives rejected

- **A path filter on the workflow.** The required check would not report and the
  pull request could not merge; and it is the bypass QUALITY_FLOW warns about.
- **Dropping the run on `main`.** Rejected by the owner.
- **Treating a retained report as still valid after `main` is merged into the
  branch** when the branch's own changes are the same. It would save a local run per
  update, but it weakens "this exact tree passed" in the validator the flow rests on.
  Not adopted; revisit separately if the cost keeps recurring.

## Consequences

- The run on `main` no longer executes device tests. That rests on two things that
  must stay true: `main` requires pull requests to be up to date with a passing
  `quality` check (verified 2026-10-12: strict status checks, enforced for
  administrators), and merges are squashes of the checked candidate. If either
  changes, `main` needs the device gate back.
- Something only a device shows that reaches `main` anyway (a changed emulator image,
  a slow-runner race) is seen by the nightly run, up to a day later, not at the merge.
- The local ticket gate and its retained report are unchanged.

## Verification

`scripts/quality/test_candidate_gate.py` (which paths are documents, which gate a
candidate gets, what the run on `main` executes) and `test_workflow.py` (no path
filter, one required job, evidence validated on every run, no check allowed to fail
quietly). The host-only paths of the workflow cannot run on the pull request that
introduces them, because that pull request changes the workflow and so runs G2; they
first run on its own merge and on the next documents-only pull request.
