# Sello project instructions

These are execution rules, not another architecture specification. MUST/SHOULD/
COULD have the meanings defined in `ARCHITECTURE.md`.

## Before starting

- **MUST** read `ARCHITECTURE.md` before planning, implementation or review. For
  ticket work, also read `docs/planning/mvp/README.md`, the assigned epic/ticket and
  applicable nested `AGENTS.md` files. Architecture owns technical/safety contracts;
  tickets own deliverables; the approved visual reference owns presentation.
- **MUST** use `docs/planning/mvp/EXECUTION_GUIDE.md` and the ticket's executor brief
  for prerequisites, owned boundaries and verification. Resolve approved decision
  inputs from predecessors; proposed file/test names are targets, not existing APIs.
- **MUST** inspect the actual code, adjacent tests, Git status and available modules
  before editing. Planned modules/APIs/gates are not implemented merely because
  documents describe them. Identify affected boundaries and verification first.
- **MUST** satisfy ticket dependencies and approvals before implementing dependent
  work. Ask about unresolved financial meaning, privacy, destructive behavior or
  conflicting requirements; do not invent a policy or weaken acceptance silently.

## Scope and repository safety

- **MUST** deliver a focused, complete task that leaves existing journeys usable.
  Fix the cause, not a cosmetic symptom; avoid unrelated cleanup, broad renames,
  speculative abstractions and dormant post-MVP capabilities. Record necessary
  scope changes before doing them; document unrelated findings without fixing them.
- **MUST** preserve other work, including uncommitted/untracked files. Do not
  discard, overwrite, stash, reset or clean another contributor's changes. Stop
  and resolve an overlapping edit rather than guessing who owns it.
- **MUST NOT** commit, create/switch branches, merge, push, tag, publish or rewrite
  history unless explicitly authorized for the current task. Inspect the exact
  diff/staged files before an authorized commit; never indiscriminately stage others' work.
- **MUST** treat dependency/toolchain/permission/schema changes as explicit task
  decisions, not incidental fixes. Use pinned catalog versions, official primary
  documentation and compatibility checks; never assume newest means compatible.
- Delegation requires explicit user authorization. If authorized, **MUST** assign
  disjoint write scopes; the coordinator owns integration, ticket status and Git
  operations. Do not delegate overlapping schema/composition/policy changes.

## Implementation boundaries

- **MUST** respect module and composition-only data access rules. Keep financial
  rules in domain, codecs/transactions in data, render models/formatting in
  presentation, and reusable visual primitives in design-system. Follow existing
  conventions; use cases must earn their coordination/rule responsibility.
- **MUST** use required constructor collaborators and injected time/dispatchers/
  scopes. No global service lookup, ad-hoc financial clock reads, successful no-op
  financial services or UI paths that claim an unimplemented operation succeeded.
- **MUST** protect exact money, original-input validation, typed read failures,
  operation IDs, generation/version checks and commit recovery. Never repair
  malformed money silently, use floating-point financial truth, return zero/empty
  on failed reads, swallow cancellation, or blindly retry an uncertain write.
- **MUST** accompany persisted schema changes with exported schemas, retention/
  migration tests and portable-format impact review. No destructive fallback or
  invisible history cascade. Room tests, not mocked DAO calls, prove atomicity.

## Tests and verification

- **MUST** use meaningful red→green tests for behavior changes. The red failure
  must demonstrate the intended behavior defect, not an unrelated compile/import
  error. Assert observable contracts and independently reviewed expected values;
  do not derive expected money with the same calculation being tested.
- **MUST** cover applicable boundaries: overflow, invalid input, duplicate submit,
  stale versions, cancellation/unknown commit, read errors, restart/restoration and
  rollback. Use controlled clocks/virtual time and real Room where required; no
  arbitrary sleeps, live network, global DI or test-order dependencies.
- **MUST** run focused tests first, then the ticket's integrated gate on the final
  changes. Use G0 until module bootstrap exists; G1/G2/G3 are defined in the board
  README. Do not edit tests, disable checks or blanket-baseline warnings to pass.
- **MUST** follow `docs/planning/mvp/QUALITY_FLOW.md`. Run
  `./scripts/verify-ticket <ticket>` and record a fresh `Quality run` from
  `--retain` before Review/Done. No manual gate downgrade, skipped-check success
  or free-text substitute for validated execution evidence. Bootstrap
  SELLO-001–003 used their own recipes and are exempt.
- **MUST** distinguish configured, compiled, executed and passed. Assembly is not
  a device test; rotation is not proof of process-death restoration; CI signing is
  not upload signing. Missing device/SDK/credentials are explicit incomplete gates.
- **SHOULD** use the documented environment, overridden only by the validated
  toolchain decision from SELLO-002:

  ```bash
  export ANDROID_HOME=/home/okabe94/Android/Sdk
  export JAVA_HOME=/opt/android-studio/jbr
  export PATH="$JAVA_HOME/bin:$PATH"
  ```

## UI, developer tools and privacy

- **MUST** reuse Sello tokens/components and immutable State/Action/Event/Root
  presentation patterns. Include relevant loading/empty/error/busy and restored
  draft states, exact accessible amounts, reduced motion and adaptive/large-font
  behavior. New components extend actual catalog examples/previews, not copies.
- **MUST** extend applicable sandbox scenarios with financial behavior changes.
  Follow architecture §9: real command/storage paths, synthetic isolated histories,
  distinct financial/audit/monotonic clocks. Debug controls/fixtures/catalog code
  must be absent from customer artifacts, not simply hidden in the UI.
- **MUST NOT** introduce personal financial fixtures, raw sensitive logs, credentials
  or signing material into source/reports. Minimize permissions; maintain private
  storage, bounded external inputs and explicit plaintext-file disclosure. Legacy
  artifacts are evidence to review, not permission to copy code or user data wholesale.

## Ticket and documentation maintenance

- **MUST** keep epic files as canonical ticket/status sources. Do not edit generated
  `BOARD.md` or CSV exports, invent completed work, or alter acceptance to fit output.
  Record the required delivery evidence before Done; satisfy gates and obtain the
  designated review. Clearly distinguish self-review from independent review/approval.
- **MUST** regenerate/check board views after ticket or shared workflow changes:

  ```bash
  python3 docs/planning/mvp/board.py
  python3 docs/planning/mvp/board.py --check
  ```

  If generator/test code changes, also run
  `python3 -m unittest discover -s docs/planning/mvp -p test_board.py`.
- **MUST** record significant architectural deviations in a short ADR and update
  changed guardrails; do not create contradictory specifications. Ordinary tasks
  without an assigned ticket do not authorize inventing a ticket or changing statuses.

## Handoff

- **MUST** report what changed, relevant file paths, exact verification and results,
  failures/warnings/skipped gates, and remaining decisions. Identify the tested
  revision plus relevant uncommitted changes; never imply a dirty tree was a tested commit.
- **MUST** leave the project buildable after completed code work. If blocked, state
  the concrete blocker, progress and next action; never call incomplete acceptance
  Done. Never claim a hosted CI run, Jira import, device test or review that did not occur.
