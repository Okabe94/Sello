# Mandatory ticket quality flow

Commands and failure recovery are in the
[execution guide](EXECUTION_GUIDE.md#quality-runner); choices are in
[ADR 0006](../../decisions/0006-quality-tooling.md). Every ticket follows this flow
except the bootstrap tickets SELLO-001–003, which kept their own verification.
Unticketed work still runs the gates; do not invent a ticket ID to run checks.

## Executor flow

1. Read the canonical ticket, approved inputs and predecessor evidence; verify
   prerequisites before Ready.
2. Implement within owned boundaries; run meaningful focused red→green tests.
3. Update the branch with `main` once, before the final gate, and resolve any conflict
   there. Then run `./scripts/verify-ticket SELLO-017`. It MUST derive the minimum G1/G2/G3 profile
   from the canonical ticket, execute required checks and retain a structured report.
   Default ticket commands execute full coverage. `--affected BASE` opts into
   dependency-aware G2 device scope; `--gate G2` always forces full coverage.
   The ticket minimum remains mandatory. No manual module selector or
   skip-as-success flag is allowed. Higher verification is permitted.
4. Record `Quality run` in the canonical ticket's Delivery evidence: run ID,
   ticket/profile, tested snapshot, outcome and retained artifact reference.
   Validate evidence and regenerate/check the board before opening the pull request.
5. Open the final pull request with the ticket already marked Done
   ([below](#done-is-proposed-merging-approves)). A ticket may span several PRs.
   Partial PRs keep it In Progress, describe the delivered scope and remaining work,
   and run the applicable checks without claiming complete ticket acceptance.
   Self-review is not independent approval. Re-run after relevant edits or reopen on
   regression.

## Done is proposed, merging approves

A pull request describes what `main` will say once it is merged. The ticket's final
pull request therefore carries the ticket as **Done**, with its complete Delivery
evidence, from the moment it is opened. Earlier parts keep the ticket In Progress.
A designated reviewer verifies acceptance, report freshness and actual results, and
nothing reaches `main` without their go-ahead and passing required checks, so `main`
never says Done for work that was not approved.

- **The approval is the merge.** The `Review` evidence field names the pull request
  and says that its merge is the reviewer's acceptance. It must not claim an
  approval, a date or a hosted run that has not happened; the pull request and its
  checks on the hosting service are that record.
- **The last ticket of an epic carries the epic too.** When the ticket is the last
  one open in its epic, mark the epic Done in the same commit as the ticket's Done
  and evidence, before the pull request is opened. Doing it afterwards costs a whole
  hosted run for one status line.
- **On the branch, Done means "proposed".** Until merged it is a request, not a
  fact. Do not start work that depends on the ticket from an unmerged branch.
- **Changes requested** are pushed to the same pull request. If they change the
  ticket's own change set, run the ticket gate again and update `Quality run`; the
  board check rejects a stale report.
- **`main` moving after the run costs no local work.** The retained report is bound
  to the pull request's own change set, not to the whole tree, and the hosted run on
  the merge result verifies the combination. Do not rerun the local gate unless
  GitHub reports a conflict; then merge `main`, resolve, and rerun with
  `--affected origin/main --retain`, because the change set itself changed.
  GitHub offers merge queues only in organization-owned repositories; while Sello is owned by a personal account, GitHub still asks for an up-to-date branch before merging: press "Update branch" (or `gh pr update-branch`) and let
  the hosted check rerun unattended. Once the repository is organization-owned, the
  merge queue replaces that step.
- **Not approved or abandoned:** close the pull request. `main` still shows the
  ticket as it was, so there is nothing to revert.
- **Why:** one pull request needs one hosted run; writing Done in a second commit
  would re-run the whole gate on unchanged code.
- The `Review` status remains for work that is waiting on something other than
  this merge, for example a device or person that is not available yet.
- After merging, the executor may start the next ticket while the hosted run on
  `main` finishes, and must report and fix a failure there before anything else merges.

## Runner and evidence contract

Standing rules for the runner, validator and CI; a change to the tooling must keep them.

- **MUST** expose one local/CI entrypoint; document requirements and actionable
  failures. Reject unknown tickets, unfinished prerequisites, unavailable required
  tasks/SDK/devices/keys, failed checks and missing results with nonzero exit.
- **MUST** report a schema version, unique run ID, ticket ID, gate, HEAD and dirty
  input fingerprint, environment/toolchain, commands/exit codes/results, timestamps,
  warnings and artifact/device references. Never include personal data or secrets.
  A report is generated from execution, not an executor-authored assertion of success.
- **MUST** define and test two fingerprints. The input fingerprint records the whole
  tree: code, tests, build/configuration, guardrails and acceptance inputs (including
  relevant untracked files). The change fingerprint binds completion evidence: the
  normalized hunks between the merge base with `main` and the tree, so the same edit
  hashes the same after unrelated changes land, even in the same file, and any
  difference in the edit itself, including line order, does not. Both exclude
  generated outputs/reports, the informative handoff and status/evidence bookkeeping,
  not whole ticket files. Acceptance edits invalidate evidence; adding the resulting
  evidence does not.
- **MUST** implement a shared evidence validator used by board completion checks
  and CI. Reject missing, malformed, failed, wrong-ticket, inadequate-profile and
  stale reports before Review/Done. A report is stale when its change fingerprint is
  not one the candidate contains: the working tree against `main` locally, or, with a
  base, one of the first-parent steps from HEAD back to that base, so a batched merge
  group or a multi-commit push binds each pull request on its own while the gate
  itself verifies the combined tree. Reports for earlier Done tickets describe their
  historical tested snapshot; unrelated later changes must not retroactively make
  the whole board invalid. Validate new transitions/changed acceptance against the
  current snapshot, and verify historical artifact identity/retention separately.
- **MUST** separate execution from completion validation: running checks while a
  ticket is In Progress cannot require its own final success record. After the run,
  completion validation verifies the record. Generated board views are checked in
  both paths without creating an evidence-generation cycle.
- **MUST** retain locally generated reports under ignored build reports and durable
  CI artifacts with a documented retention/retrieval policy. A fresh checkout must
  not depend on another developer's ignored file. A missing retained artifact is
  missing evidence, not permission to trust a free-text field.
- **MUST** keep the host's required quality check and protected merge. CI re-executes checks for the actual merge
  candidate; a local report alone cannot authorize merge. Cover workflow/path-filter
  bypasses and gate-policy edits in review. Never expose signing secrets to untrusted PRs.
- **MUST** treat distributed CI parts as incomplete execution, not full-gate or
  retained ticket evidence. The final collector requires all checks/modules for
  the selected gate and device scope, matching commit/inputs/base/event/run/attempt, compatible
  toolchains, successful job outcomes and fresh device results without skips.
  Schema-v2 reports must identify full/affected/documents plus base, paths and
  exact device modules. Unselected suites are not executed/passed; selected suites
  require fresh results without skips. Historical full schema-v1 reports remain
  readable. Missing, failed, cancelled, stale or inconsistent parts fail `quality`. Keep
  the underlying part reports, logs and raw results with the full report.
  Validate every selected ticket's prerequisites and gate before executing a
  candidate-wide part; bind the collected report separately to each ticket's
  current acceptance fingerprint. Individual local ticket commands remain full by
  default; local candidate batches execute each ticket at its minimum gate with
  derived scope. CI executes the shared candidate gate once.
- **SHOULD** offer focused task commands for quick feedback; they never substitute
  for the integrated ticket gate. The approved `--affected BASE` mode is an
  integrated host gate plus dependency-aware device scope, not an arbitrary test filter. Additional enforcement plugins/hooks **COULD** improve
  feedback; they cannot replace trusted CI or designated review.

## What is enforced, and by what

- **Runner:** `scripts/verify-ticket` derives the gate from the canonical ticket and
  exits nonzero on any refusal or failed check. It has no skip or downgrade option.
- **Validator:** `board.py --check` opens the retained report of every Review/Done
  ticket from SELLO-004 onward and rejects a missing, malformed, failed,
  wrong-ticket, lower-gate, edited or stale one. Bootstrap tickets are exempt.
  A ticket that becomes Done in a pull request is checked against that pull
  request's own inputs, exactly as one entering Review was.
- **CI:** the `quality` check runs the same entrypoint and then validates completion
  evidence against the base, on every run. What it executes depends on the event
  ([ADR 0009](../../decisions/0009-hosted-gate-scope.md), amended by
  [ADR 0014](../../decisions/0014-change-aware-quality-gates.md)): eligible pure
  documentation advancing no ticket gets G0. Other PRs get G2 with dependency-aware
  device scope; shared/unknown changes get full devices. Code pushes to main keep
  G1 for trusted cache publication; eligible documentation pushes get G0. Nightly
  and manual runs execute full G2. The workflow also verifies merge queue entries
  (`merge_group`) like pull requests, from the group's base. Until the repository is
  organization-owned (GitHub offers merge queues only there), the classic protection
  keeps requiring an up-to-date branch and the hosted run on the merge result is the
  integration check; there is no path filter and the check always reports.
  Host checks execute once. Only selected app/support device groups start; support
  runs only its selected data/design-system/catalog suites. The final quality job
  validates every required part and completion evidence. See the [local and CI
  commands](../../development/setup.md#parallel-hosted-g2).

These tools prove that checks ran on a known snapshot. They do not judge whether
acceptance is met; the designated review still does. Agent instructions are policy,
not a security boundary: a committed report can be hand-edited, which is why CI
re-executes the gate instead of trusting it.

G3 adds release checks to a full G2: the licence list, the owner's production
signature and artifact checks, and installing and opening that APK on the emulator.
CI runs the same G3 with a throwaway key; only a report with the owner's production
signing completes a G3 ticket. See [ADR 0016](../../decisions/0016-release-gate-signing-split.md)
and [release](../../release/README.md).
