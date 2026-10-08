# SELLO-E08 — Integrated MVP release evidence

- **Status:** Backlog
- **Goal:** Demonstrate a usable recoverable release, not merely assembled debug APKs.
- **Exit:** Passed complete MVP journeys, temporal/recovery corpus, accessible adaptive UI and signed optimized release; outstanding risks are explicitly accepted or fixed.

## SELLO-033 — Execute accessibility, adaptive and process-restoration journeys

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-018, SELLO-019, SELLO-020, SELLO-021, SELLO-022, SELLO-023, SELLO-009, SELLO-004
- **Gate:** G2

### Outcome
Verify and fix the assembled daily-use UI across input methods and window configurations.

### Deliverables
Automated/manual journey matrix and evidence for four schemes, scales 1.0/1.3/2.0,
compact/short-landscape/medium/expanded layouts, TalkBack, keyboard/Switch Access,
insets and reduced motion. Include actual draft/session restoration, not rotation alone.

### Acceptance criteria
- All daily-use flows work without clipping/covered fields, color-only status,
  gesture-only actions, undersized targets or inaccessible chart values.
- Tab/month/detail/panels follow approved back policy; drafts/operation IDs survive
  eligible process recreation. Force-stop/cold start is reported separately from
  OS process-death restoration and must not create a duplicate save.
- Issues discovered are fixed with regression tests; no blanket a11y waiver.

### Tests
Run affected connected Compose tests on agreed API/window configurations; execute
screen-reader/focus walkthrough and capture screenshot/video/checklist evidence.
Verify exact money semantics in visual and accessible outputs; G2.

### Working checkpoint
Integrated daily-use MVP is usable on devices; no recovery or catalog workflow is
broken by layout/accessibility fixes.

### Context and starting points
Component-level previews cannot prove complete forms/navigation usable on actual
devices. Inputs are completed daily-use routes, approved D10 matrix and the catalog.
Read [a11y](../../../design/sello-spec.html#a11y), adaptive/navigation guidance and
[process-restoration procedure](../EXECUTION_GUIDE.md#device-and-process-restoration).

### Implementation plan
1. Create `docs/testing/ui-acceptance-matrix.md` listing build/API/window/font/theme/
   input mode, each daily-use journey and expected accessible exact values.
2. Automate Compose semantic/focus/restoration checks and execute screen-reader,
   hardware keyboard/Switch Access and reduced-motion walks on agreed configurations.
3. Run compact portrait, <480dp short landscape, medium and expanded panes with
   keyboard/gesture insets and scales 1.0/1.3/2.0. Test both completed tab stacks.
4. Exercise drafts/operation IDs through rotation, eligible process death and cold
   start separately. Verify saved receipt cannot trigger another write on recreation.
5. Fix found clipping/focus/copy/back issues in their owning layer, add regression
   tests and rerun prior completed journeys. Keep a matrix of passes and outstanding blockers.

### Concrete cases and pitfalls
- TalkBack announces currency/full amount and stamp meaning; charts provide exact
  selected/text values. Color-only over-budget state is not accessible.
- A focused note field remains reachable with keyboard open in landscape; 2.0 scale
  cannot truncate hero amounts or put save/delete actions below an unreachable panel.
- Force-stop success proves durable reopen, not saved-task restoration; report it separately.

### Verification recipe
Run affected app/design-system/catalog connected tests and G2 across the approved
matrix. Retain annotated screenshot/video/focus notes with exact configuration and
financial scenario. No single screenshot or emulator configuration closes the whole matrix.

## SELLO-034 — Prove integrated data retention and recovery failure safety

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-030, SELLO-031, SELLO-032, SELLO-027, SELLO-004
- **Gate:** G2

### Outcome
Test the complete release data lifecycle, including interrupted operations, against real persistence.

### Deliverables
Cross-version schema migration retention corpus for every implemented version,
backup fixtures, restore/reset cancellation matrix and executable synthetic recovery
scenarios. Include expenses, income, archived categories, period limits, ordering
and preferences. Document corruption/unavailable-storage behavior.

### Acceptance criteria
- Migration upgrades preserve exact financial identities/history; no destructive
  fallback. With only v1, verify fresh/reopen plus migration harness, not invent a v0 upgrade.
- Invalid backup, overflow, interrupted replacement and failed preferences have
  truthful outcomes; receipts/generation prevent duplicate or stale recovery.
- Temporal replay and backup/restored charts agree with independent expected totals.
  Re-running reset/import does not resurrect discarded payloads or sandbox state.

### Tests
Run real-Room migration/transaction/device corpus with controlled failure points,
process interruption and reopen; execute full sandbox regression inventory; G2.

### Working checkpoint
Every persisted MVP capability survives supported upgrade/backup/restore paths,
or fails safely with a recoverable documented state. Retain command/device evidence.

### Context and starting points
Schema, backup and cancellation tests can each pass while their integrated recovery
path fails. Inputs are actual migration/schema exports, backup formats, reset/restore
records and scenario corpus. Read architecture §§5/7/10 and the guide's Room migration/
failure references; do not invent compatibility with the old app database.

### Implementation plan
1. Inventory every schema and portable-format version actually shipped/implemented;
   define supported upgrade/restore pairs in `docs/testing/recovery-matrix.md`.
2. Prepare independently asserted synthetic prior-state fixtures with archived
   categories, month limits, expense/income order, profile zone and preferences.
3. Execute real-file migrations/reopen and backup/restore under failures before/inside/
   after financial commit, then before/after preferences; verify rows and recovery receipts.
4. Test stale submit/undo/import/reset races across generation change and caller death,
   ensuring pending settings resume without repeating financial replacement.
5. Run fresh/resumed temporal corpus and cross-check exact chart/list/backup values;
   fix gaps with regression tests in owning modules, not unchecked recovery shortcuts.

### Concrete cases and pitfalls
- With only schema v1, fresh/reopen is legitimate; a fake v0 migration is not evidence.
- A valid restore with preferences failure is committed+pending, not rollback or
  complete success. Backup-corruption rejection leaves every store unchanged.
- Old record IDs after restore cannot make stale undo eligible. Retained reset
  metadata cannot contain erased payloads; real corrupt data never becomes an empty ledger.

### Verification recipe
Run data/app connected migration/recovery suites and TemporalScenarioCorpusTest,
then G2. Record each failure point, before/after row/receipt/generation state and
supported version pair. Archive fixtures/checksums and actual device/API evidence.

## SELLO-035 — Validate performance and optimized release composition

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-004, SELLO-023, SELLO-027, SELLO-032, SELLO-033, SELLO-034
- **Gate:** G3

### Outcome
Measure a realistically populated app and make customer release packaging deliberate.

### Deliverables
Representative synthetic dataset/device profile and approved performance thresholds,
startup/scroll/graph/backup measurements; correct off-main/bounded work; optimized
release configuration/keep rules, dependency/license review and artifact inspection.
Set signing through secure local/CI inputs with no committed upload secrets.

### Acceptance criteria
- Large history remains responsive without full unbounded reads or main-thread file/
  replay work. Chart exactness stays intact; optimization never substitutes rounded money.
- Release DI graphs and shrunk code execute real commands/serialization/recovery;
  no debug clocks, fixtures, catalog code, no-op financial services or tools survive.
- Measurements state device/build/dataset and meet reviewed thresholds; no arbitrary
  claimed benchmark. Signed release installs and launches with correct identity.

### Tests
G3 including actual optimized-release manual/automated smoke, dependency/security
reports, artifact inspection and repeatable performance runs. CI ephemeral signing
and upload signing are validated separately; lack of credentials is recorded as a blocker.

### Working checkpoint
A candidate customer artifact—not the catalog or debug variant—works with persisted
financial history and recoverable files. Reports support the final acceptance decision.

### Context and starting points
Debug responsiveness and unshrunk DI do not establish release behavior. Inputs are
approved D10 targets/device profile, all recovery/privacy/a11y evidence and actual
signing access. Read architecture §10, source-library shrinking/signing docs and
[baseline](../BASELINE.md)'s currently unoptimized release configuration.

### Implementation plan
1. Create `docs/testing/performance-profile.md` with reviewed targets, device/API,
   measurement method, repetitions and datasets before accepting results. Use 20
   categories, 10,000 expenses, 2,000 incomes and 12 past months as a proposed large profile.
2. Produce synthetic data through validated commands/backup, install candidate only
   on an isolated test device/profile, and measure startup/scroll/graph/backup/restore
   responsiveness, heap/main-thread work and SQL/query behavior.
3. Fix measured bottlenecks at owned boundaries: bounded/paged queries, exact summaries,
   off-main work. Add benchmarks only when their tooling scope/versions are approved.
4. Enable production optimization/shrinking and minimum tested keep rules; verify
   Koin/Room/serialization/resources/recovery actually run in the optimized artifact.
5. Build/sign using approved external inputs, inspect source/variant/runtime graphs
   and manifests/resources/dependencies; attach dependency/license/security and signing reports.

### Concrete cases and pitfalls
- Do not lower targets after seeing a slow run or replace exact money with Float
  to improve graph throughput. State warm/cold/cache/dataset conditions for each result.
- Obfuscated class-name absence alone is not proof debug tools were excluded; source
  set/dependency inspection and unshrunk customer-variant checks corroborate the artifact.
- No signing access means G3 is blocked, not “signed by debug key”. Build the chosen
  channel's APK/AAB without uploading, tagging or publishing it.

### Verification recipe
Run host/device gates and actual `:app:assembleRelease` (plus `:app:bundleRelease`
if accepted distribution requires AAB); execute G3 on signed optimized candidate.
Record hash/version/signing certificate, independent release money/recovery smoke,
measurements/thresholds and artifact exclusions. Never write secrets in commands/reports.

## SELLO-036 — Accept the complete MVP and prepare release handoff

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-033, SELLO-034, SELLO-035, SELLO-004
- **Gate:** G3

### Outcome
Make a recorded go/no-go decision from executed evidence, not epic/ticket counts alone.

### Deliverables
Release checklist with artifact hash/version/signing identity, accepted MVP scope,
full test/device/performance/privacy evidence, user recovery guidance, known issues
and next-increment backlog. Final board statuses reflect reviewed delivery evidence.

### Acceptance criteria
- Execute fresh install → category/budget → expense → receipt/Recibo → edit/undo →
  income → Resumen → appearance/order → backup → mutate → restore → restart → reset →
  reuse, on the signed production candidate. No skipped step is an implicit pass.
- Independently launch catalog groups/themes/states and debug sandbox time/history/
  recovery scenarios; verify neither ships inside the customer artifact.
- All other 35 tickets' acceptance/gates are satisfied; any genuine blocker prevents
  release. Remaining nonblocking issues are specifically owner-approved, not hidden.
- No Patrimonio, recurrence/batch/rates/alerts/widgets/capture or unsupported links
  are present. Post-MVP scope is documented separately, without prebuilt dormant modules.

### Tests
Full G3 plus documented product-owner walkthrough and fresh board/link/export
validation. Record exact release revision and distinguish manual/device tests from assembly.

### Working checkpoint
MVP is demonstrably installable, useful, recoverable and ready for the agreed
distribution channel; do not publish/commit/tag automatically without explicit approval.

### Context and starting points
This is the final go/no-go, not a last-minute implementation bundle. Inputs are
reviewed evidence from all prior tickets, signed artifact from SELLO-035, accepted
MVP contract and product approval owner. Read architecture §1/10,
[workflow](../README.md#definition-of-done--every-ticket) and delivery-record guide.

### Implementation plan
1. Create `docs/release/mvp-checklist.md` listing exact candidate revision/hash/version/
   identity, supported device/API/window matrix, data/recovery assumptions and report links.
2. Verify the other 35 tickets are Done with actual accepted evidence; inspect
   deferred-feature absence and every visible action/route before the owner walkthrough.
3. Execute the full signed-customer flow from empty through save/edit/undo/income/
   summary/preferences/backup/restore/restart/reset/reuse, comparing known exact values.
4. Separately launch independent catalog and debug sandbox; verify scenarios/types
   are useful and not packaged into customer release. Record any actual blockers.
5. Obtain explicit product go/no-go, document nonblocking issue ownership and
   user recovery/support instructions. Mark this ticket Done only after acceptance;
   leave distribution/upload/tag/commit to a separately authorized action.

### Concrete cases and pitfalls
- “36/36 Done” is the resulting state after this task, not its circular precondition.
- Missing device/signing/product review cannot be replaced by a unit-test count.
  An unresolved recovery or unsafe money defect blocks release, even if build is green.
- No inactive Patrimonio/recurrence/export/widget/capture link remains; catalog/debug
  apps are explicitly separate developer artifacts, not installed customer features.

### Verification recipe
Run complete G3, `python3 docs/planning/mvp/board.py --check` and board unit tests.
Use the final checklist for actual owner/device walkthrough and record approval,
candidate hash, exact scenario results and retained warnings. Generate board/CSV only
after evidence/status edits; do not publish automatically.
