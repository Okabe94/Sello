# Mandatory ticket quality flow

**Contract owner:** SELLO-004, which delivered the runner and validator described
here. Commands and failure recovery are in the
[execution guide](EXECUTION_GUIDE.md#quality-runner); choices are in
[ADR 0006](../../decisions/0006-quality-tooling.md).
Bootstrap tickets SELLO-001–003 use their existing verification recipes. Every
subsequent ticket explicitly depends on SELLO-004; future implementation tickets
must inherit this flow too. Ordinary unticketed work still follows architecture
gates; do not invent a ticket ID to run checks.

## Executor flow

1. Read the canonical ticket, approved inputs and predecessor evidence; verify
   prerequisites before Ready. SELLO-004 must be Done before downstream work starts.
2. Implement within owned boundaries; run meaningful focused red→green tests.
3. Run the entrypoint delivered by 004, targeted as
   `./scripts/verify-ticket SELLO-017`. It MUST derive the minimum G1/G2/G3 profile
   from the canonical ticket, execute required checks and retain a structured report.
   No downgrade/skip-as-success flag is allowed. Higher verification is permitted.
4. Record `Quality run` in the canonical ticket's Delivery evidence: run ID,
   ticket/profile, tested snapshot, outcome and retained artifact reference.
   Validate evidence and regenerate/check the board before opening the pull request.
5. Open the pull request with the ticket already marked Done, as described in
   [Done is proposed, merging approves](#done-is-proposed-merging-approves).
   A designated reviewer verifies acceptance, report freshness and actual results.
   Only approved acceptance plus passing required checks permits the merge, and so
   Done on `main`. Self-review is not independent approval. Re-run after relevant
   edits or reopen on regression.

## Done is proposed, merging approves

Adopted 2026-10-10 by the project owner, from SELLO-013 onward.

A pull request describes what `main` will say once it is merged. The ticket's pull
request therefore carries the ticket as **Done**, with its complete Delivery
evidence, from the moment it is opened. Nothing reaches `main` without the
designated reviewer's go-ahead, so `main` never says Done for work that was not
approved.

- **The approval is the merge.** The `Review` evidence field names the pull request
  and says that its merge is the reviewer's acceptance. It must not claim an
  approval, a date or a hosted run that has not happened; the pull request and its
  checks on the hosting service are that record.
- **On the branch, Done means "proposed".** Until merged it is a request, not a
  fact. Do not start work that depends on the ticket from an unmerged branch.
- **Changes requested** are pushed to the same pull request. If they touch
  fingerprinted inputs, run the ticket gate again and update `Quality run`; the
  board check rejects a stale report.
- **Not approved or abandoned:** close the pull request. `main` still shows the
  ticket as it was, so there is nothing to revert.
- **Why:** writing Done in a second commit after approval re-ran the whole hosted
  gate on unchanged code. One pull request now needs one hosted run.
- The `Review` status remains for work that is waiting on something other than
  this merge, for example a device or person that is not available yet.
- After merging, the executor may start the next ticket while the hosted run on
  `main` finishes, and must report and fix a failure there before anything else merges.

## Runner and evidence contract — implementation owed by 004

- **MUST** expose one local/CI entrypoint; document requirements and actionable
  failures. Reject unknown tickets, unfinished prerequisites, unavailable required
  tasks/SDK/devices/keys, failed checks and missing results with nonzero exit.
  SELLO-004 can run its own G1 before it is Done; 001–003 do not require this runner.
- **MUST** report a schema version, unique run ID, ticket ID, gate, HEAD and dirty
  input fingerprint, environment/toolchain, commands/exit codes/results, timestamps,
  warnings and artifact/device references. Never include personal data or secrets.
  A report is generated from execution, not an executor-authored assertion of success.
- **MUST** define and test fingerprint coverage: code, tests, build/configuration,
  guardrails and acceptance inputs (including relevant untracked files). Exclude
  generated outputs/reports and status/evidence bookkeeping, not whole ticket files.
  Acceptance edits invalidate evidence; adding the resulting evidence does not.
- **MUST** implement a shared evidence validator used by board completion checks
  and CI. Reject missing, malformed, failed, wrong-ticket, inadequate-profile and
  stale reports before Review/Done. Reports for earlier Done tickets describe their
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
- **MUST** configure the chosen host's required quality check and protected merge
  policy when host/admin access exists. CI re-executes checks for the actual merge
  candidate; a local report alone cannot authorize merge. Cover workflow/path-filter
  bypasses and gate-policy edits in review. Never expose signing secrets to untrusted PRs.
- **SHOULD** offer an affected-area focused command, but it never substitutes for
  the integrated ticket gate. Additional enforcement plugins/hooks **COULD** improve
  feedback; they cannot replace trusted CI or designated review.

## What is enforced, and by what

- **Runner:** `scripts/verify-ticket` derives the gate from the canonical ticket and
  exits nonzero on any refusal or failed check. It has no skip or downgrade option.
- **Validator:** `board.py --check` opens the retained report of every Review/Done
  ticket from SELLO-004 onward and rejects a missing, malformed, failed,
  wrong-ticket, lower-gate, edited or stale one. Bootstrap tickets are exempt.
  A ticket that becomes Done in a pull request is checked against that pull
  request's own inputs, exactly as one entering Review was.
- **CI:** the `quality` check runs the same entrypoint on the merge commit of every
  pull request and on `main`, then validates completion evidence against the base.
  `main` requires that check to pass.

These tools prove that checks ran on a known snapshot. They do not judge whether
acceptance is met; the designated review still does. Agent instructions are policy,
not a security boundary: a committed report can be hand-edited, which is why CI
re-executes the gate instead of trusting it.

G3 is not executable until SELLO-035 delivers its checks; the runner refuses it.
