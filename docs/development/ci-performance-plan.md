# CI performance plan

Reduce feedback time while preserving the checks required by
[ADR 0009](../decisions/0009-hosted-gate-scope.md) and the
[quality flow](../planning/mvp/QUALITY_FLOW.md). This is unticketed work;
it does not create tickets or change their status.

## Baseline

The reviewed source revision is `7f94c75`. Historical hosted reports show:

- [Full candidate 39057602268](https://github.com/Okabe94/Sello/actions/runs/39057602268):
  13m47s job; 399s Gradle host and 303s Gradle device; 511 host and 528
  device tests. Host JUnit suites total about 5s, included in the host step.
- [Nightly 39271536509](https://github.com/Okabe94/Sello/actions/runs/39271536509):
  15m52s job; 515s Gradle host and 337s Gradle device. This older snapshot
  predates two device tests.
- [Host 39364095314](https://github.com/Okabe94/Sello/actions/runs/39364095314):
  6m19s job; 356s Gradle host.

These runs passed with 32–33 lint warnings. They are measurements of their
own revisions, not a controlled before/after benchmark or a promised saving.

## Options and order

1. **Cache Gradle downloads and build outputs — implemented locally; hosted
   measurement pending.** Restore the
   wrapper, dependencies, compiled build scripts and local task-output cache
   on hosted runners. Enable Gradle build caching, track files read directly
   by tests, and let only trusted `main` runs update shared entries. This targets
   the largest measured cost; quantify the saving after hosted runs exist.
2. **Tune build parallelism and emulator provisioning — pending.** Benchmark
   bounded worker/heap settings, build/emulator contention and clean API-30 AVD
   caching. Preserve all task selections and measure net wall time and cost.
3. **Split device work between two runners — pending.** Candidate suite times
   suggest app alone (~135s) versus data + design-system + catalog (~124s).
   The ideal reduction is about two minutes of serial suite execution, before
   extra provisioning/build overhead. First require per-module reports and
   fail the required `quality` result on any failed, missing or cancelled group.
   Use separate emulators; app methods share database and process state.
4. **Reduce device harness repetition — pending.** Replace the 20–23s catalog
   traversal with all-example content verification and meaningful navigation
   coverage using less repeated scrolling. Replace the fixed two-second
   cancellation wait with deterministic synchronization. Retain real Room and
   the real six-second undo-expiry smoke test. Expected savings are seconds.
5. **Share one gate across multiple tickets — pending.** Prevalidate each
   ticket's prerequisites and required gate, execute the shared gate once on
   an immutable candidate, and bind each ticket's acceptance evidence to it.
   This helps multi-ticket candidates only; the baseline candidate has one.
6. **Remove redundant tests — pending, maintenance priority.** The arithmetic
   template, duplicate catalog-list assertion, standalone floating-point
   fixture check and synthetic missing-binding check total about 0.14s.
   Review those methods individually; deleting them will barely affect CI time.

Do not drop transaction, migration, recovery, accessibility or Android integration
coverage to improve runtime. Keep the existing G1/G2 event policy, exact task lists,
evidence validation, dependency locks and API-30 device execution.

## Option 1 implementation

Owned scope: this plan, the Quality workflow, `gradle.properties`, data-module
test input declarations, hosted-workflow regression tests and the setup guide.
No changes to financial behavior, existing Gradle/JDK or module dependency pins,
gate selection or ticket status are planned. Findings outside this scope remain
follow-up work.

- [x] Add a commit-pinned `gradle/actions/setup-gradle` step before gate execution.
- [x] Restrict cache writes to `main`; pull requests and other refs read only.
- [x] Enable the local Gradle build cache alongside the existing configuration cache.
- [x] Declare the schema directory and scanned Kotlin sources as inputs to data unit
  tests; stale cached schema/source assertions must not pass.
- [x] Keep cache/setup reporting in the job summary and raw Gradle outcomes in
  retained gate logs. `FROM-CACHE`, `UP-TO-DATE` and executed are distinct.
- [x] Verify focused negative/configuration checks, cache reuse and invalidation.
- [x] Document the setup and actual focused verification. Hosted cache restore/save and
  speedup remain unverified until the workflow runs on GitHub.

### Tool choice

Use the MIT-licensed `gradle/actions/setup-gradle` **v5.0.2**, commit
`0723195856401067f7a2779048b490ace7a47d7c`, from the
[official release](https://github.com/gradle/actions/releases/tag/v5.0.2).
Its Node 24 runtime fits the existing GitHub-hosted workflow; keep the project's
Gradle 9.5.0 wrapper and JDK 25. The
[pinned action documentation](https://github.com/gradle/actions/blob/0723195856401067f7a2779048b490ace7a47d7c/docs/setup-gradle.md)
describes commit-scoped cache entries, read-only PR use, summaries and cache contents.
This version provides the required rolling Gradle User Home cache without adopting
v6's separate proprietary enhanced-cache provider or its simpler fixed-key basic
provider. Future action updates need their own compatibility/license review.

No encryption secret is introduced: project configuration-cache entries stay
within a job and are not uploaded. The action caches selected Gradle User Home
build state, not application databases, emulator state, signing keys or reports.
No Build Scan publication, dependency submission or PR comments are enabled.

### Verification and follow-up measurement

Focused verification used HEAD `7f94c75` plus the six files in the owned scope:

- `python3 -m unittest discover -s scripts/quality -p test_workflow.py`: three
  new checks first failed for absent cache setup/write policy/task caching, then
  all 11 workflow tests passed.
- A Gradle task-input probe first failed with `Missing direct test input:
  schemas/com.software.sello.data.local.SelloDatabase/1.json`; after the input
  declarations it passed for both schema and source paths. Probe setup errors
  before that assertion was reached are not the red result.
- `:data:cleanTestDebugUnitTest :data:testDebugUnitTest --info` restored the test
  task `FROM-CACHE`. Adding a synthetic schema JSON and, separately, a comment-only
  Kotlin source reran the test task for changes to `exportedSchemas` and
  `storageSources`. Both temporary fixtures were removed. The probes and logs
  are under ignored `build/reports/ci-performance/`.
- A directory-list probe exposed that tracking only JSON files missed an unexpected
  schema filename: the test task stayed `UP-TO-DATE`. Tracking the entire schema
  directory makes that change invalidate the task and reach the retention assertion.
- All 137 quality-tool tests, 32 board tests, board regeneration/check,
  architecture checks and `ktlintCheck` passed.

The final integrated verification command is
`ANDROID_SERIAL=emulator-5584 ./scripts/verify-ticket --merge-candidate HEAD --gate G2`
with the documented JDK/SDK environment, on a dedicated synthetic API-30 emulator
named `sello_cache_ci`. An initial run on the existing `emulator-5582` was stopped
after another checkout's simultaneous gate interfered with its activities; report
`20261020T041204Z-bd477b30` remains failed. The isolated run verifies the same full
gate without changing assertions or task selection.

The runner writes the tested dirty-file fingerprint,
host/device results and warnings to ignored `build/reports/quality/UNTICKETED/`;
the execution result is reported after the run. No ticket completion report or
hosted result is implied by this implementation checklist.

After an authorized merge populates the `main` cache, compare several equivalent
G1 and G2 hosted runs with the baseline. Record wall time, restore/save overhead,
cache outcomes, test counts, warnings and runner cost. A cold cache must still pass
the full gate. Do not claim a measured hosted speedup from local cache hits.
