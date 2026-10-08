# Mandatory ticket quality flow

**Contract owner:** SELLO-004. This defines its deliverables, not an installed runner.
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
   Validate evidence and regenerate/check the board before requesting Review.
5. A designated reviewer verifies acceptance, report freshness and actual results.
   Only approved acceptance plus passing required checks permits Done. Self-review
   is not independent approval. Re-run after relevant edits or reopen on regression.

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

## Enforcement today versus after 004

Today `board.py` rejects missing explicit 004 dependencies and missing/placeholder
`Quality run` fields for SELLO-004 onward in Review/Done. It does **not** prove the
referenced tests ran or validate report contents. Agent instructions enforce the
workflow by policy; they are not a security boundary.

004 must replace that evidence-field-only check with the validator above, add
negative tests and wire the runner into CI. No remote/admin access currently exists;
hosted execution and merge protection remain incomplete acceptance until verified.
Do not call 004 Done merely because local checks or workflow YAML exist.
