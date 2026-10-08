# SELLO-E01 — Contracts and engineering foundation

- **Status:** Backlog
- **Goal:** Make decisions explicit and build/test the app reproducibly before financial work.
- **Exit:** Approved MVP conventions, enforced module graph, usable CI and injected clocks/composition; existing app remains launchable.

## SELLO-001 — Approve the MVP contract and financial examples

- **Type:** Task
- **Priority:** P0
- **Status:** In Progress
- **Depends on:** None
- **Gate:** G0

### Outcome
Remove ambiguity before implementing figures or recovery. Ratify the architecture's
MVP, two-tab exception and developer tools; do not expand scope to recurrence/goals.

### Deliverables
An approved scope/decision record and versioned worked examples for day inclusion,
variable pace, rounding, projection, 95/100% verdict boundaries, no-budget behavior,
historical limits and income allocation. Record privacy/plaintext backup policy,
supported API range and app/debug/catalog identities. Verify the existing local
Sello snapshot/provenance; do not fetch another worktree as a build prerequisite.

### Acceptance criteria
- Product owner explicitly approves the open decisions; unresolved policy cannot
  be replaced by a developer guess. Examples include day 1, last day, February,
  zero spending, unlimited categories, over-budget and insufficient-history states.
- Category rename/archive and current versus prior-month budget edits have defined
  outcomes. Forecast labels never imply saved actuals; savings is not account balance.
- Design snapshot has no build-time external-path dependency or unrelated legacy files.

### Tests
Review example arithmetic independently, validate Markdown links and the board,
and verify baseline G0 still passes. Documentation-only changes need no invented red test.

### Working checkpoint
Approved decisions are usable inputs to domain/UI tickets; generated app still
builds/launches. Missing approvals keep this ticket out of Done.

### Context and starting points
The design contains sample amounts and legacy migration hints, not approved new
financial policies. Without this task, separate actors will choose different pace,
budget history and reset semantics. Read [decision inputs](../DECISION_INPUTS.md),
[baseline](../BASELINE.md), architecture §§1/4/7/9 and the
[local design](../../../design/sello-spec.html#build). No predecessor implementation exists.

### Implementation plan
1. Create `docs/decisions/0001-mvp-contract.md` using D01–D10 as individual decisions;
   record owner, chosen answer, rationale, alternatives, approval/date and unresolved items.
2. Resolve each budget/forecast/order/field/reset question explicitly. Define formulas,
   included dates, rounding and labels; distinguish fixed rules from reviewed proposals.
3. Create `docs/testing/mvp-financial-examples.md` with M/B/V/H/I/G examples, at least
   three approved forecast calculations, February/leap-year and first/last-day cases.
4. Review examples independently with the product owner. If answers are unavailable,
   deliver the proposal and blocker; never invent “approved” finance/privacy semantics.
5. Verify design provenance; update architecture only for accepted guardrail changes.
   Leave implementation, toolchain changes and downstream ticket statuses untouched.

### Concrete cases and pitfalls
- Spending 50,000 across limited/unlimited categories must reconcile to B01, not
  discard unlimited expenses. Past limits cannot be read from today's category defaults.
- Decide whether forecast comparison uses exact or rounded projection at 95%/100%;
  the allowance's inclusive today is not automatically the forecast's day convention.
- UI keypad limits and Long import/display range need separate explicit answers.
  A theme preference reset does not imply permission/device state can be restored.

### Verification recipe
Run `python3 docs/planning/mvp/board.py --check` and G0 from
[gate profiles](../README.md#gate-profiles); check decision/example links and snapshot
hash. Evidence must include approver/date and independently checked inputs/outputs.
No behavior red test or device run is required for this documentation-only task.

### Execution progress
2026-10-08: drafted [D01–D10 proposals](../../../decisions/0001-mvp-contract.md)
and [versioned worked examples](../../../testing/mvp-financial-examples.md).
2026-10-08: project owner approved D01 scope and D03 forecast/day-inclusion/verdict
rules, with daily allowance rounded down to COP 50 multiples. Recorded per-decision
approval and updated example/architecture contracts; no other choices were approved.
Next: owner reviews D04–D07, followed by D08/D09/D02/D10. Independent full-corpus
review remains outstanding. Ticket stays In Progress; downstream prerequisites
are not satisfied by this partial approval.
2026-10-08 continuation: refined the still-unapproved D04 skipped-month proposal to
resolve known historical defaults from effective-month evidence, independent of app
opening, and explicitly exclude leftover/overspend rollover. Updated H03–H05;
request owner approval of zero/unlimited, monthly carry-forward and historical edits.
2026-10-08 follow-up: owner approved zero/unlimited distinction, prior-month stability
and historical corrections with discoverable user information. D04 is partially
approved; carryover is explicitly still under discussion. Added option comparison
and H06 guidance case; SELLO-019/020/021 must deliver applicable correction messaging.
2026-10-08 direction: owner confirmed eventual signed surplus/deficit carryover,
possibly optional and outside MVP. Added ADR 0002 and retention/ownership guardrails;
no carryover code/schema or new tickets. MVP activation/default and detailed policy
still need approval; next clarify deferral before moving to the remaining D04/D05 inputs.
2026-10-08 plan approval: owner approved MVP budget renewal without carryover and
post-MVP deferral. Added SELLO-E09/037–043 with release/policy/quality dependencies
and separate generated roadmap views; MVP remains eight epics/36 tickets. Detailed
carryover decisions are owned by SELLO-037, not blockers for SELLO-001.
2026-10-08 skipped-month approval: owner agreed that unopened months resolve budget
configuration effective then, not today's default. Recorded D04 approval, aligned
H03/H04 and added real-Room history/materialization acceptance to SELLO-013.
Next: D05 field/input/source rules, then D06/D07 (including archive budget interactions).
2026-10-08 D05 approval: owner approved all presented name/note/uniqueness, amount
input/range-preservation and income-source rules. Recorded approval, added T01–T06
policy examples and aligned SELLO-010 validation acceptance. Next: D06 ordering,
then D07 archive/undo and the remaining privacy/support/release decisions.
2026-10-08 D06 approval: owner approved all presented metric/direction/tie and
unlimited-last ordering rules. Recorded approval, added O01–O08 fixture expectations
and aligned SELLO-023. Archive visibility/budget interactions remain proposed D07;
prepared explicit current/future-month archive consequences for the next discussion.
2026-10-08 D07 approval: owner approved archive/restore/history labels, explained
current/future-month budget effects and six-second real-time undo with process-death
offer expiry/outcome recovery. Closes D04/D06 archive decisions; added AR01–AR06/
U01–U05 and aligned SELLO-013/019/020/021. Next: D08 privacy/portable backup policy.
2026-10-08 D08 approval: owner approved all presented manual plaintext/privacy/
contents/retention choices and requested committing accumulated work. Recorded D08,
added P01–P04 and aligned SELLO-029/032; implementation is still outstanding.
Next: D09 restore/reset, then D02 identities/support and D10 release/review ownership.
Executor checks: 32 board unit tests passed, including meaningful red→green scope
checks (five intended assertion failures before enforcement). Both board/export
sets passed consistency checks; 325 local
documentation file/anchor links, design snapshot SHA-256 and exact arithmetic checks
passed, including 15 COP 50 allowance cases and pre-rounding verdict precision.
G0 (`./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease --continue`)
passed with cached JVM test/lint results (one sample JVM test, 17 existing lint
warnings). Latest continuation tested base: `491eb26` plus uncommitted revised
D04 decision/example documents and carryover-direction ADR, architecture/root README/
decision-input/workflow changes, foundation/financial-core/correction-messaging/
recovery-privacy edits,
new post-MVP epic, board generator/tests and generated MVP/roadmap export views.
Carryover/archive arithmetic, field/money-range/deadline boundaries and O01–O08 ordering
illustrations were executor-checked, not executed feature tests or independent review. No device run
required for this documentation task;
D01/D03–D08 product approvals and future signed-carryover direction are recorded
above; no independent review is claimed.

## SELLO-002 — Establish and verify the supported Android toolchain

- **Type:** Task
- **Priority:** P0
- **Status:** Ready
- **Depends on:** None
- **Gate:** G0

### Outcome
Replace accidental template compatibility with a reproducible supported matrix,
without unnecessary upgrades or changing product behavior.

### Deliverables
Validated AGP/Gradle/Kotlin/Compose/KSP/JDK/SDK matrix, pinned catalog/wrapper and
setup instructions. Explain Gradle daemon toolchain versus Java bytecode target;
audit the existing JDK-25 daemon configuration and Java-11 target rather than assuming
JAVA_HOME alone controls the daemon. Establish debug versus production signing paths.

### Acceptance criteria
- A clean checkout builds using documented installed/downloaded prerequisites;
  wrapper checksum and AGP built-in Kotlin integration are valid.
- Dependency versions are pinned, source/build artifacts contain no credentials,
  and ignored machine-local configuration stays untracked.
- Release compilation succeeds without upload secrets. Production signing remains
  an explicit later release gate; no keys/passwords enter source control.

### Tests
Run G0 on a clean build/cache-independent verification where practical; compile
the instrumented APK. Exercise missing-SDK/JDK/signing setup instructions and
record actual daemon/JDK versions plus dependency/lint warnings.

### Working checkpoint
App's current greeting works on an emulator/device; if no device exists, record
assembly separately and arrange smoke evidence before concluding launch acceptance.

### Context and starting points
The scaffold currently builds, but its mixed versions, JDK daemon criteria and SDK
targets have not been declared supported. Read [baseline](../BASELINE.md),
[tool commands](../EXECUTION_GUIDE.md#tools-and-host-verification), the catalog,
wrapper, `gradle.properties`, daemon criteria and `app/build.gradle.kts`.

### Implementation plan
1. Record actual `./gradlew --version`, installed SDK/platform/build tools and resolved
   catalog versions; distinguish launcher JVM, daemon JDK and Java/Kotlin bytecode targets.
2. Consult the guide's official AGP/Gradle/Kotlin sources and select the smallest
   compatible stable matrix; document why existing pins stay or change, including KSP.
3. Create `docs/decisions/0002-toolchain.md` and `docs/development/setup.md` with exact
   prerequisites/install locations, untracked SDK configuration and commands for a new actor.
4. Update only necessary pins/targets/wrapper checksum. Do not independently raise
   minSdk or change product identity before SELLO-001 approval.
5. Test without relying on cached outputs; document signing setup without credentials.
   Prepare/install the scaffold on the agreed emulator/device and record launcher smoke.

### Concrete cases and pitfalls
- `JAVA_HOME` pointing at Android Studio does not override daemon criteria by itself.
- A supported matrix must include the Compose plugin/BOM and selected KSP processor
  compatibility, not just successful dependency resolution with an old cache.
- A missing device is an unmet launch check. Unsigned release assembly is not an
  upload-signed application; do not create an upload key just to silence a build failure.

### Verification recipe
Run `./gradlew --version`, `./gradlew clean`, G0 and
`./gradlew :app:assembleDebugAndroidTest`; inspect dependency/lint reports. Follow
[device setup](../EXECUTION_GUIDE.md#device-and-process-restoration) for launch smoke.
Record the successful matrix, warnings and any missing prerequisites.

## SELLO-003 — Bootstrap enforced modules and variant identities

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-001, SELLO-002
- **Gate:** G1

### Outcome
Create the architecture's compilation boundaries without introducing unfinished features.

### Deliverables
`:domain` Kotlin/JVM, `:data` and `:design-system` Android libraries, existing
`:app`, and developer `:catalog` application. Configure test source sets, Room schema
export location/KSP capability, catalog launcher, separate debug/catalog application
IDs and the allowed dependency graph. Move template theme ownership without duplication.

### Acceptance criteria
- App and minimal catalog independently install/launch; customer app never depends
  on catalog. Domain has no Android/UI/storage/DI dependency.
- Data import access is reserved for app composition; no reverse module edge or
  screen-to-screen implementation import is introduced.
- Existing instrumentation package assertions are variant-aware; sample tests do
  not produce a false failure after adding the debug ID suffix.

### Tests
Run G1 and compile affected test APKs. Inspect Gradle dependency graphs and package
IDs. Intentionally attempt a prohibited domain Android import/module edge to prove
the compilation boundary rejects it, then remove the negative fixture.

### Working checkpoint
Both launchers show honest minimal UI; no new product destination or fake save exists.

### Context and starting points
Today all code is in `:app`, so source packages do not enforce ownership. Inputs
are accepted identity/toolchain records from SELLO-001/002 and the
[target map](../EXECUTION_GUIDE.md#module-and-file-map). Read architecture §2 and
official build-variant guidance in the source library before altering Gradle.

### Implementation plan
1. Add the four target siblings to `settings.gradle.kts`; configure JVM domain and
   Android library/application plugins using the accepted matrix/catalog, not copy-pasted versions.
2. Create build/test/resource/manifest skeletons for each module; set namespaces,
   SDK/bytecode/test-runner settings and allowed dependency edges explicitly.
3. Give app debug and catalog independent accepted IDs; move reusable theme ownership
   to design-system and keep MainActivity functional. Catalog gets its own real launcher.
4. Add approved Room/KSP/schema export configuration to data, but no speculative
   database tables. Compile test source sets and correct variant-specific package assertions.
5. Document module task/dependency inventory in setup instructions; verify no app→catalog,
   design-system→domain or reverse data edge. Do not add feature navigation yet.

### Concrete cases and pitfalls
- Installing debug app and catalog must not replace production or one another.
- Domain's Android imports must fail; app's unavoidable data Gradle edge is composition-only
  in source, which SELLO-004 enforces separately. Gradle alone cannot enforce that package rule.
- Empty modules still need usable documented test/build tasks; no-matching-tests
  cannot be described as executed business tests.

### Verification recipe
Run `./gradlew projects`, module `dependencies` reports and G1. Compile app/catalog
test APKs and install both launchers. Demonstrate a temporary forbidden domain import
fails compilation, remove it, rerun green, and attach the dependency/package-ID evidence.

## SELLO-004 — Install CI, quality gates and architecture enforcement

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-003
- **Gate:** G1

### Outcome
Prevent boundary/build/test regressions from becoming normal before feature growth.
Make quality verification a required completion workflow for every subsequent task,
not a collection of optional tests. This ticket owns the enforcement tools.

### Deliverables
Host/device CI definitions, documented aggregate gate, formatting/static analysis,
composition-only import rules, dependency review and retained test/lint artifacts.
Validate the board with `board.py --check`. Keep configuration compatible with the
actual Git host; do not claim a hosted run without a remote/runner.
Deliver `scripts/verify-ticket` (target path), a versioned structured quality-report
schema, shared completion-evidence validator and tested executor/CI integration
implementing [QUALITY_FLOW.md](../QUALITY_FLOW.md). Extend the existing Python
board validator to verify reports, not merely the presence of an evidence field.
Document invocation, prerequisites, report retention, snapshot coverage and recovery
from failures in the execution guide. Require the host quality check/protected merge
policy with actual host/admin evidence; local hooks alone are not enforcement.

### Acceptance criteria
- Rules reject a feature importing data, domain importing Android, cross-feature
  internals, customer modules depending on catalog and debug tools in release.
- CI compiles customer release and catalog debug; tests fail the job instead of
  being swallowed. No blanket lint baseline hides new errors.
- Secrets are external; external contributions cannot access upload credentials.
- The runner derives the canonical ticket's gate without downgrade; failures,
  missing checks/devices/keys and skipped requirements return nonzero, never success.
- Review/Done transitions require a successful matching report with current input
  fingerprint. Wrong-ticket/profile, malformed, stale or unavailable evidence fails.
  Historical Done evidence remains verifiable without rerunning every predecessor.
- CI runs the same entrypoint against the merge candidate; required-check/merge
  protection is verified with the chosen host. Missing remote/admin access blocks
  hosted acceptance, not permission to mark configured YAML as executed CI.

### Tests
Demonstrate failing boundary fixtures and a failing test are caught, followed by
clean G1. Execute local gate and hosted CI when infrastructure is available; retain
reports and explicitly distinguish configured CI from executed CI.
Add runner/validator tests for absent reports, wrong IDs, insufficient gate,
nonzero checks, missing device, malformed schema and edited acceptance/code inputs.
Prove status/evidence-only edits do not stale a report, older completed tickets
remain valid, and checks can run before their own completion evidence exists.
Exercise a sample downstream ticket through run → evidence → Review; remove test
fixtures afterward without changing real ticket statuses. Verify host protection
with an authorized failing-check example; record actual results, never fabricate it.

### Working checkpoint
Document one reproducible gate used by every later ticket; app/catalog remain buildable.
An independent executor can follow the shared quality flow, and incomplete evidence
cannot pass the completion validator. Passing checks still require designated review.

### Context and starting points
Module files from SELLO-003 are the inputs; policy violations must fail before they
spread into features. Read architecture §§2/10, current Python board tests and
[verification guidance](../EXECUTION_GUIDE.md#tools-and-host-verification). No Git remote
or hosted CI is assumed available.

### Implementation plan
1. Inventory actual host/device/assembly tasks; add a documented aggregate Gradle or
   script entry point running the agreed G1 checks with retained machine-readable reports.
2. Configure formatting/Detekt/lint and architecture checks for production/debug source
   sets and module edges. Cover direct imports and qualified data references in features.
3. Add negative fixtures for prohibited edges and a deliberately failing test to
   prove checks return nonzero; fixtures must not remain compiled into customer code.
4. Configure host CI and pinned-device jobs for the chosen host; collect reports
   without exposing secrets. Device jobs may be scheduled separately but cannot vanish.
5. Add board check/tests to CI. Record which workflow actually executed; if no remote
   exists, retain local gate evidence and request runner/host access for hosted acceptance.
6. Implement the ticket-derived runner and report schema from QUALITY_FLOW.md.
   Inventory required tasks per gate; record actual execution, environment and
   input fingerprints. Avoid requiring final evidence before running the gate itself.
7. Upgrade board completion validation to consume retained reports, including
   current transition freshness versus historical Done evidence. Define artifact
   retention/retrieval and fail explicitly when reports cannot be retrieved.
8. Add negative bypass/freshness tests; update agent/runbook instructions with the
   working command. Configure and verify the chosen host's required quality check
   and merge protection with its administrator; leave acceptance incomplete until
   hosted results and protection evidence are available.

### Concrete cases and pitfalls
- A feature importing a concrete DAO through an alias/qualified name must not bypass
  the source boundary. Explain the checker coverage/limits rather than claiming complete compiler proof.
- New warnings cannot disappear into a blanket baseline; dependency review is separate
  from a lint version suggestion. PR code must never receive upload credentials.
- Static tooling belongs in the build/test path, not domain or release dependencies.
- A report for SELLO-017 cannot complete SELLO-018; G1 cannot satisfy G2. A copied
  success field is not an executed report. Changed code/acceptance requires rerun;
  adding evidence or regenerating board exports must not create a freshness loop.

### Verification recipe
Execute each negative fixture, remove it, then run the aggregate gate and board unit
tests. Archive output plus actual CI run IDs if available. Device tests require the
[device procedure](../EXECUTION_GUIDE.md#device-and-process-restoration), not test-APK assembly alone.
Run the new entrypoint for SELLO-004 itself after completed predecessor inputs,
validate its retained report, and demonstrate rejected invalid completion fixtures.
Attach local report and hosted check/protection evidence to `Quality run` and delivery
fields; request review only after required checks pass. Do not mark Done without
hosted acceptance or silently replace it with local-only verification.

## SELLO-005 — Wire production composition and distinct time sources

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-001, SELLO-003, SELLO-004
- **Gate:** G1

### Outcome
Make financial dates, audit time and lifetime ownership injectable without shipping debug controls.

### Deliverables
Required constructor-based Koin composition, platform-neutral observable financial
clock and zone contract, separately injected audit/monotonic timing ports, dispatchers
and owned scopes. Production clock reacts to midnight/resume/zone/time changes;
debug composition can replace only financial time. Preference contracts are neutral.

### Acceptance criteria
- Logic never calls global DI or reads wall time ad hoc; cancellation propagates.
- Financial zone is initialized once and later travel does not rewrite effective
  history. Until Room setup exists, use an explicit initialization path, not fake persistence.
- Missing required bindings fail graph construction; no successful no-op financial services.
- Financial stepping cannot change undo timers or real audit timestamps.

### Tests
Controlled-clock rollover/resume/time-zone tests, virtual-time lifecycle/cancellation
tests, and production/debug graph construction tests; run G1.

### Working checkpoint
Existing app/catalog launch under production time; pure tests exercise the future
sandbox seam without introducing its controls into release.

### Context and starting points
Legacy date-sensitive code diverged through wall-clock reads and global services.
Use the approved financial-zone decision and SELLO-003 module graph. Read architecture
§§2/4/9 and [contract checklist](../EXECUTION_GUIDE.md#contract-and-failure-checklist).
Domain clock ports are neutral; Android system observation belongs in app/platform.

### Implementation plan
1. Define financial date/zone/as-of observation, audit Instant and monotonic elapsed
   ports separately; add coroutine dispatcher/scope ownership without global lifetime lookup.
2. Implement real system adapters with explicit startup/resume/midnight/time-change
   recomputation and cancellation. Persisting financial zone is SELLO-011, not a fake default save.
3. Build app composition modules using required constructors; future workflows bind
   only as they exist. Do not require a successful dummy repository for graph creation.
4. Provide controlled ports in test/debug source sets and production graph checks;
   centralize mapping from adapters to consumer contracts.
5. Document lifecycle ownership and disposal so later sandbox session switches can
   cancel old observers and replace financial time without changing the other clocks.

### Concrete cases and pitfalls
- Month rollover while backgrounded must be observed on resume; travel must not
  change stored effective dates. Audit time and monotonic undo/inactivity remain real.
- Cancellation cannot be converted into Failed/empty financial data. A required
  missing binding is a wiring error, not a successful no-op.
- A mutable test clock must publish changes so snapshots update; changing a value
  in a field without an observable signal is not a working simulation seam.

### Verification recipe
Create/run `*FinancialClockTest`, `*ClockOwnershipTest` and composition graph tests
with controlled coroutine time. Run G1; retain midnight/resume/zone/cancellation
assertions and confirm app/catalog launch with production adapters.
