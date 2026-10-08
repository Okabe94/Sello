# SELLO-E09 — Signed budget carryover after MVP

- **Status:** Backlog
- **Increment:** Post-MVP
- **Goal:** Preserve surplus and overspending across enabled budget periods without confusing plans with account balances.
- **Exit:** Approved signed-carry policy, exact explainable availability, recoverable storage/portable files, accessible opt-in workflows and executed multi-month regression/release evidence; non-carry journeys remain usable.

This is an approved roadmap addition, not MVP scope. Read
[ADR 0002](../../../decisions/0002-signed-carryover-direction.md),
[execution guide](../EXECUTION_GUIDE.md) and [quality flow](../QUALITY_FLOW.md).
SELLO-037 cannot start until MVP release acceptance (SELLO-036) is Done. Proposed
file/test names below are deliverables, not existing APIs. No code/schema is built
while executing SELLO-001. Optional scope and detailed finance semantics are owned
by SELLO-037, not guessed by later executors.

## SELLO-037 — Approve signed carryover policy and independent examples

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-001, SELLO-036, SELLO-004
- **Gate:** G1

### Outcome
Turn the approved honesty principle into an implementable contract before enabling
carryover. Both surplus and deficits must survive; rollout/opt-in semantics are not
implied by the direction ADR.

### Deliverables
An approved `docs/decisions/0003-signed-carryover-policy.md`, versioned
`docs/testing/signed-carryover-examples.md`, acceptance-to-ticket mapping and reviewed
presentation wording for base limit, carry-in, effective capacity and remaining.
Record product/independent technical approvers and exact approved answers.

### Acceptance criteria
- Define per-category versus global scope, default/opt-in, effective start month,
  starting carry, missing/skipped history and continuous multi-month formula.
- Define enable/disable/re-enable, archive/unarchive, finite/unlimited/zero transitions,
  base-limit edits, caps, negative effective capacity and out-of-range handling.
  No deficit can be silently forgiven or clipped; any settlement requires explicit policy.
- Define historical correction cascades, preview/confirmation scope, report provenance,
  forecast/verdict denominator and income-allocation treatment without counting carry as income.
- Independently approve at least three consecutive months, surplus, deficit, deficit
  larger than next base, skipped months and correction cases. No unresolved financial
  choice is left for the domain/data/UI executor to invent.

### Tests
Independently check integer/rational arithmetic and policy transitions; validate
document links and board outputs, then G1. Documentation work needs no invented
behavioral red test. Missing approvals prevent Done.

### Working checkpoint
Existing released MVP remains unchanged/buildable; each successor can use concrete
approved inputs rather than an ambiguous rollover description.

### Context and starting points
Read ADR 0002 and accepted D01/D03/D04 portions in
[MVP decisions](../../../decisions/0001-mvp-contract.md). Inspect the released
monthly snapshot, command and backup contracts from SELLO-013/014/028–031; the
ADR's C01/C02 illustrate intent, not a complete approved replay algorithm.

### Implementation plan
1. Inventory actual released contracts, source ownership and retention guarantees;
   list decisions that change existing financial/report/portable meanings.
2. Present concrete alternatives to the product owner; record selected policies,
   reasons, rejected alternatives and effective-date rules in the new decision record.
3. Work multi-month examples with independently calculated expected values. Separate
   base limit, carry, effective capacity, spending, remaining and income allocation.
4. Obtain product approval and independent financial/technical example review;
   update guardrails through an ADR for any accepted architecture change.
5. Map approved examples/edge cases to SELLO-038–043 and publish the contract;
   run documentation checks and the ticket runner, record Quality run and request review.

### Concrete cases and pitfalls
- Initial base 100,000, spend 120,000 gives −20,000 carry and next capacity 80,000
  under the ADR's conditional assumptions; surplus-only rollover is not this feature.
- In the continuous candidate, spending 70,000 from capacity 80,000 leaves 10,000
  and next capacity 110,000, not 130,000. Review rather than assume this formula.
- A past expense correction may alter later derived availability while configured
  later base limits stay unchanged; users need a clear impact explanation.

### Verification recipe
Use SELLO-004's actual ticket runner with G1, plus link/board checks. Attach signed
decision/example review and requirement mapping; no approval claim from agent self-review.

## SELLO-038 — Implement exact signed carryover domain calculations

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-037, SELLO-014, SELLO-004
- **Gate:** G1

### Outcome
Provide one exact, deterministic financial calculation consumed by all later paths;
UI/storage must not independently reinvent rollover or forget an earlier deficit.

### Deliverables
Domain-owned signed-carry calculation and typed policy/provenance contracts integrated
with the actual monthly snapshot API. Publish boundary documentation and focused
`SignedCarryoverPolicyTest`/`CarryoverCorrectionReplayTest` (target names).

### Acceptance criteria
- Approved multi-month calculations distinguish base/carry/effective capacity/remaining,
  preserve signed values and use checked/widened arithmetic, never floating-point truth.
- Replay follows effective month ordering and approved transitions; corrected history
  has a deterministic affected range. No financial wall-clock reads or storage dependency.
- Missing history, invalid mode inputs and unrepresentable totals produce typed failures,
  not zero carry. No-carry mode preserves all approved MVP snapshot/forecast examples.
- Income/net cash flow remain separate from carry; forecast/verdict/allowance reuse
  the approved policy, including COP 50 allowance rounding without erasing deficits.

### Tests
Meaningful red→green unit tests with SELLO-037's hardcoded reviewed values; cover
multi-month signed chains, corrections, skipped months, transitions, zero/negative
capacity and Long intermediate/result overflow. Run existing monthly-policy tests and G1.

### Working checkpoint
Pure calculations and existing released app/catalog compile and pass; production
customers cannot activate incomplete storage/UI. No successful placeholder ports.

### Context and starting points
Inspect actual `:domain` money/month snapshot and port conventions from SELLO-010/014,
not names guessed from this plan. The SELLO-037 policy and example corpus are required
inputs; read architecture §§2/4 and ADR 0002.

### Implementation plan
1. Map approved outputs and failure states onto existing exact money/snapshot contracts.
2. Add red tests for reviewed chains and invalid inputs before changing calculations.
3. Implement deterministic replay and provenance/affected-range results off UI logic;
   widen arithmetic and validate each representable output.
4. Integrate snapshot/forecast/allowance policy without rewriting unrelated domain APIs;
   preserve original no-carry behavior and cancellation ownership conventions.
5. Publish consumer-shaped storage/read needs and run focused suites then ticket G1;
   retain evidence and request designated review.

### Concrete cases and pitfalls
- Clamping a negative spending allowance is presentation/planning behavior, not
  permission to clamp the underlying deficit or fabricate positive capacity.
- Effective capacity cannot be computed from only previous base minus spending if
  an earlier carry already influenced that month's capacity.
- Test expectations must come from the reviewed corpus, not another invocation of
  the production calculator. Unknown history is not identical to genuine zero spending.

### Verification recipe
Run new exact-domain tests and legacy `MonthlyBudgetPolicyTest`, then the ticket
runner's G1. Record reviewed input IDs, boundary results and original MVP regressions.

## SELLO-039 — Persist carryover configuration and atomic correction replay

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-038, SELLO-011, SELLO-012, SELLO-013, SELLO-004
- **Gate:** G2

### Outcome
Make approved carryover settings and derived availability reliable across restart,
concurrent edits and cancellation without altering historical base-budget facts.

### Deliverables
Reviewed Room migration/exported schemas, validated configuration/transition commands,
real transactional adapters and revision-consistent carryover reads using the domain
policy. Document whether derivation is on demand or cached and how stale caches are fenced.

### Acceptance criteria
- Upgrade retains all MVP history/settings/receipts; existing users stay in approved
  no-carry mode unless explicit opt-in policy says otherwise. No destructive fallback.
- Commands use operation ID, generation and expected version; setting changes,
  affected financial revisions and durable receipts are atomic.
- Correction replay/invalidation cannot publish mixed revisions or partially update
  a chain. If bounded resumable work is needed, old/new visibility is explicitly safe.
- Identical replays return receipts; stale commands, reset generation, invalid transitions
  and overflow reject without partial mutation. Cancellation does not imply rollback.
- No clock-triggered money generation or duplicate month-boundary writes; reads work
  after app absence. Customer activation remains unavailable until recovery/UI tickets ship.

### Tests
Real-Room old-schema migration/retention and command/replay tests: duplicate submits,
rollback, concurrent historical edit/read, invalid versions, reset fencing, cancellation
around commit and DB reopen. Assert rows/revisions/receipts, not mocked DAO calls; G2.

### Working checkpoint
Upgraded app launches with prior no-carry data intact; data/domain integrations work
without exposing an opt-in that has incomplete backup or customer UX.

### Context and starting points
Inspect actual schema/migrations and operation protocol from SELLO-011–013, domain
contracts from SELLO-038 and accepted policy from SELLO-037. Review portable-format
impact with SELLO-040 before changing persistence; do not serialize Room entities.

### Implementation plan
1. Choose the minimal schema/index/caching changes from measured replay/query needs;
   document migration and portable-format impacts before implementation.
2. Add old-schema retention and atomic failure tests; implement non-destructive migration.
3. Implement settings/transition commands with the existing receipt/version/generation
   protocol and real-Room concurrency guarantees.
4. Wire consistent reads and correction replay to the domain calculator; expose typed
   pending/failed states where required, never stale data labeled current.
5. Exercise restart/cancellation/rollback/correction races and existing no-carry commands;
   run G2 and publish storage/read contracts for recovery and UI consumers.

### Concrete cases and pitfalls
- A current default edit cannot overwrite prior base limits; an approved historical
  correction may affect later derived carry only through the reviewed replay policy.
- A follow-up failure after commit cannot remove the settings change or financial receipt.
- Adding every future month as a persisted job is not required for calendar continuity.

### Verification recipe
Run real-Room `CarryoverMigrationTest` and `CarryoverCommandContractTest` (target names),
existing financial/recovery suites, connected affected tests and the complete G2 runner.

## SELLO-040 — Extend portable backup, restore and reset for carryover

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-039, SELLO-028, SELLO-029, SELLO-030, SELLO-031, SELLO-004
- **Gate:** G2

### Outcome
Carryover-enabled financial histories can be recovered portably before customer
activation; older MVP backups remain interpretable without inventing missing carry.

### Deliverables
Versioned portable-format evolution and compatibility documentation, strict codecs,
restoration/reset integration and synthetic old/new backup fixtures. Record whether
derived values are omitted or included with validated provenance; never trust caches.

### Acceptance criteria
- Review old-version compatibility and unknown-version rejection explicitly; old
  backups restore with approved no-carry interpretation, not guessed starting deficits.
- Round trip preserves actual records, base history and the approved carry configuration;
  replay matches reviewed values without counting carry as new transactions/income.
- Validate references, bounds, transitions and signed semantics before confirmation.
  Invalid files cause no financial/settings mutation or partial historical replay.
- Replacement advances generation, fences old commands/undo and recovers its receipt;
  settings completion remains resumable. Reset removes carry settings/history correctly.
- Disclosure/cleanup/privacy guarantees remain intact; no fixtures contain personal data.
- Before SELLO-041's customer presentation is integrated, enabled-policy restore is
  exercised only through isolated test/developer paths. The customer app explicitly
  rejects not-yet-supported enabled-policy files before mutation rather than restoring
  carry-enabled data into a UI that cannot explain it. No false successful restore.

### Tests
Strict codec and real-Room replacement/reopen tests, old/new round trips, corrupt
configuration/overflow/references, unknown versions, generation fencing and preference
failure recovery. Re-run MVP backup/restore/reset journeys and G2.

### Working checkpoint
Both existing and carry-capable backups restore safely; released no-carry recovery
works unchanged, and customer carryover activation still waits for SELLO-041.

### Context and starting points
Read implemented portable spec/codecs and restore/reset protocol from SELLO-028–031,
SELLO-039 migration/commands and SELLO-037 reviewed example corpus. Architecture §7
owns bounds, plaintext disclosure and recovery guarantees.

### Implementation plan
1. Inventory actual wire versions and history/configuration fields needed to replay;
   approve compatibility changes and update format documentation.
2. Add failing synthetic compatibility/validation/round-trip tests; implement strict
   decoding without silently upgrading unknown financial meaning.
3. Integrate staged restore validation, atomic replacement and domain replay; preserve
   receipt recovery and settings follow-up behavior across cancellation/restart.
4. Extend reset/old-command fencing and preview counts/provenance without exposing
   internal caches or operation IDs as portable replay authority.
   Keep customer activation/restore support unavailable until SELLO-041 integration;
   verify rejected unsupported activation makes no financial/settings changes.
5. Execute old/new backup, restore and reset regressions on real storage/devices;
   run G2 and publish recovery evidence before customer activation.

### Concrete cases and pitfalls
- A negative carried deficit is not an invalid positive expense; money validation
  must respect distinct types while rejecting malformed original fields.
- Exporting only today's effective capacity loses the evidence needed for corrections.
- A successful financial replacement with unfinished settings remains incomplete restore.

### Verification recipe
Run targeted carryover codec/restore tests and original restore/reset connected suites,
then G2. Retain synthetic file versions, exact expected replay totals and reopen evidence.

## SELLO-041 — Deliver accessible carryover opt-in and availability explanations

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-040, SELLO-020, SELLO-022, SELLO-023, SELLO-004
- **Gate:** G2

### Outcome
Users can knowingly activate the approved policy and understand why availability
differs from the configured monthly limit, including after historical corrections.

### Deliverables
Working approved-scope enable/disable flows, base/carry/effective/remaining breakdowns
in affected Recibo/detail/Resumen surfaces, contextual guidance and settings/draft
restoration. Reusable primitives and real catalog examples follow Sello's visual language.

### Acceptance criteria
- Activation and transitions use real commands and confirmed receipts; no hidden
  default enablement or success from an unimplemented control.
- Exact accessible positive/negative values and source periods explain signed carry.
  Negative capacity is visible; zero daily allowance does not hide the deficit.
- Forecast/verdict/graphs/order/income copy uses domain policy consistently. No-carry
  mode and existing journeys remain usable; labels never claim a verified bank balance.
- Historical corrections expose the approved downstream impact before confirmation
  and refresh one consistent revision after commit. Draft/version conflicts recover.
- Loading/empty/error/busy/restored states, themes, reduced motion and large-font/adaptive
  layouts work; catalog fixtures/controls stay outside customer artifacts.

### Tests
ViewModel and connected Compose flows for activation/transitions, exact breakdown,
historical impact, duplicate submission, stale draft, read/commit errors and recreation.
TalkBack/large-font/adaptive/theme checks and actual catalog interactions; G2.

### Working checkpoint
Carryover is now a complete customer journey with recovery support, not a toggle
on unfinished mathematics/storage. Existing MVP no-carry scenarios still pass.

### Context and starting points
Use accepted SELLO-037 scope/copy, real SELLO-039 ports and SELLO-040 recovery
guarantees. Inspect existing SELLO-020/022/023 roots/states/components; the visual
reference has no authoritative carryover layout, so obtain approval for the extension.

### Implementation plan
1. Obtain design approval for the minimal Sello component/layout extension and
   map immutable states/actions/events to actual domain render values.
2. Add failing presentation/semantics tests for signed breakdown and non-carry regression.
3. Implement real opt-in/transition command flows with receipt recovery and version
   conflict handling; show start period and approved consequences explicitly.
   Integrate customer carry-enabled restore support from SELLO-040 only once affected
   screens explain the policy; prove the former rejection is replaced by a complete journey.
4. Extend affected snapshots/screens/graphs/order and persistent help, reusing domain
   values and design-system primitives rather than local formulas.
5. Add interactive catalog states, execute device/a11y/restoration tests and G2;
   request design and financial-meaning review before acceptance.

### Concrete cases and pitfalls
- Base 100,000 plus carry −20,000 gives capacity 80,000 under approved example
  assumptions; the UI must not relabel the base as 80,000 or hide the −20,000.
- Moving an old transaction between months can affect more than one period;
  a generic “only this month changes” message would be false with signed carry.
- Plotting approximate coordinates never replaces exact selected/accessible values.

### Verification recipe
Run affected root/ViewModel/Compose tests and catalog examples, then G2. Capture
approved designs, exact values, opt-in/restore/disable journeys and actual device evidence.

## SELLO-042 — Extend live sandbox and regression corpus for signed carryover

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-041, SELLO-024, SELLO-025, SELLO-027, SELLO-004
- **Gate:** G2

### Outcome
Developers can reproduce accumulating surplus/deficit, month progression and late
corrections through real financial commands instead of fabricated chart data.

### Deliverables
Named synthetic carryover scenarios, resumable deterministic command scripts,
reviewed expected snapshot/graph checkpoints and sandbox runner documentation.
Extend the existing developer tooling rather than creating a second simulation engine.

### Acceptance criteria
- Financial stepping crosses consecutive/skipped/short/leap-year months; actual
  storage/domain paths produce carry, graph and receipt values matching reviewed inputs.
- Cover surplus, repeated overspend, deficit beyond next base, correction cascades,
  opt-in transitions and restore/reset interruption using approved policy.
- Repeated/resumed scenarios do not duplicate financial records or reset prior carry.
  Financial stepping does not alter audit timestamps or monotonic undo deadlines.
- Scenario data remains synthetic/isolated, bounded and absent from customer builds;
  customer histories are never manipulated by sandbox commands.

### Tests
Real-Room command/replay/checkpoint suites and connected sandbox journeys before/
after restart, interruption, restoration and reset. Assert independently reviewed
exact snapshot/graph values; no arbitrary sleeps or fabricated financial success; G2.

### Working checkpoint
An independent developer can replay a named multi-month carryover history and inspect
the same availability customers see, while customer release remains developer-tool-free.

### Context and starting points
Inspect the actual sandbox protocol/fixtures from SELLO-024/025/027 and current
carryover ports/UI. Read architecture §9, SELLO-037 examples and ADR 0002; financial,
audit and monotonic clocks remain distinct.

### Implementation plan
1. Define stable scenario IDs and reviewed input/expected-checkpoint records; reuse
   existing synthetic fixtures and operation-ID/resume conventions.
2. Add red tests for representative signed chains and interrupted replay without duplicates.
3. Implement scenario scripts using real settings/expense/correction/restore commands;
   step only the injected financial clock.
4. Expose existing sandbox progress/read failures and exact graph/snapshot provenance;
   document reset/replay and isolate scenario storage.
5. Execute replay/reopen/restore/reset device cases and release artifact isolation;
   run G2 and publish reusable regression/runbook evidence.

### Concrete cases and pitfalls
- A scenario that writes precomputed carry/graph arrays cannot prove production replay.
- A repeated late correction must recover the same receipt, not cascade twice.
- Month stepping alone cannot serve as proof of real audit/undo timing correctness.

### Verification recipe
Run carryover scenario regression tests, connected sandbox journeys and G2. Retain
scenario IDs, reviewed expected values, real receipt/reopen checkpoints and release checks.

## SELLO-043 — Verify and release the complete signed carryover increment

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-042, SELLO-033, SELLO-034, SELLO-035, SELLO-036, SELLO-004
- **Gate:** G3

### Outcome
Ship an independently reviewed, recoverable carryover increment without regressing
released MVP behavior or misrepresenting financial availability.

### Deliverables
Executed integrated acceptance matrix, migration/portable-recovery evidence,
approved performance comparison, production-signed optimized artifact/install tests,
release notes explaining opt-in/deficits/corrections and designated release approval.

### Acceptance criteria
- Execute approved no-carry and carryover journeys across the accepted API/device/
  window/font/theme/input matrix; actual process-death recovery is not just rotation.
- Upgrade representative prior release databases and restore old/new backups; verify
  signed-chain totals and record retention, including negative capacity and corrections.
- Measure large-history/replay responsiveness against agreed budgets; no unbounded
  main-thread work or invisible stale-revision success. Review deviations, not blanket baselines.
- Inspect customer artifact for developer tooling/fixtures/test clocks; signing,
  privacy, dependency and shrinker checks pass with actual required credentials/device.
- Product/financial/design/technical release reviewers accept evidence; no Jira import,
  hosted run or independent approval is claimed without actual execution.

### Tests
G3 including existing MVP regression/recovery suites, signed-chain corpus, device
accessibility/restoration, retained CI reports, optimized signed installation and
large-history replay benchmarks on the approved matrix.

### Working checkpoint
Approved complete carryover release is installable/recoverable; original non-carry
daily-use journeys remain usable. Every epic child has actual Done evidence.

### Context and starting points
Use completed SELLO-037–042 artifacts and MVP SELLO-033–036 release runbooks.
Inspect actual performance targets, signing procedures and support matrix rather
than copying outdated baseline devices/tasks. No host/device/key availability is assumed.

### Implementation plan
1. Map approved requirements/examples to executed release journeys and reviewers;
   validate all predecessor Quality run and decision evidence.
2. Run integrated historical/signed-chain/transition/no-carry and old/new recovery
   cases on the accepted matrix; distinguish assembly from executed device results.
3. Measure replay/correction/large-history costs against accepted budgets; fix scoped
   carryover regressions with meaningful red→green tests and rerun affected gates.
4. Build/install and inspect production-signed optimized artifact; verify developer
   exclusion, privacy and old-release upgrade/recovery with actual credentials.
5. Run final G3, retain snapshot-bound reports and obtain independent/product release
   approvals; update release notes and epic evidence only after actual acceptance.

### Concrete cases and pitfalls
- Correct arithmetic in a unit test does not prove migration, portable restore or UI
  consistency after process death. A release with debug controls hidden still fails isolation.
- Local runner success is not a hosted required check; debug signing is not upload signing.
- A missing reviewer/device/signing key is an incomplete gate, not a reason to call Done.

### Verification recipe
Use the final G3 ticket runner and current release runbook; record exact candidate
revision/dirty inputs, artifact/device IDs, counts, failures/warnings, retained reports
and named approvals. Update canonical statuses and regenerate/check both boards.
