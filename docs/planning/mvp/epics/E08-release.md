# SELLO-E08 — Integrated MVP release evidence

- **Status:** Done
- **Goal:** Demonstrate a usable recoverable release, not merely assembled debug APKs.
- **Exit:** Passed complete MVP journeys, temporal/recovery corpus, accessible adaptive UI and signed optimized release; outstanding risks are explicitly accepted or fixed.

## SELLO-033 — Execute accessibility, adaptive and process-restoration journeys

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-018, SELLO-019, SELLO-020, SELLO-021, SELLO-022, SELLO-023, SELLO-009, SELLO-063, SELLO-064, SELLO-065, SELLO-066, SELLO-067, SELLO-004
- **Gate:** G2

### Outcome
Verify and fix the assembled daily-use UI across window, text-size and theme configurations.

### Deliverables
Automated/manual journey matrix and evidence for four schemes, scales 1.0/1.3/2.0,
compact/short-landscape/medium/expanded layouts, insets, reduced motion and the
accessibility semantics tests can read (spoken amounts, labels, targets). Include
actual draft/session restoration, not rotation alone.

### Acceptance criteria
- All daily-use flows work without clipping/covered fields, color-only status,
  gesture-only actions, undersized targets or inaccessible chart values.
- Tab/month/detail/panels follow approved back policy; drafts/operation IDs survive
  eligible process recreation. Force-stop/cold start is reported separately from
  OS process-death restoration and must not create a duplicate save.
- Issues discovered are fixed with regression tests; no blanket a11y waiver.
- Manual TalkBack, Switch Access and hardware keyboard walks are not part of this
  ticket; they are post-MVP in SELLO-074.

### Tests
Run affected connected Compose tests on agreed API/window configurations; capture
screenshot/video/checklist evidence of the device walks. Verify exact money
semantics in visual and accessible outputs; G2.

### Working checkpoint
Integrated daily-use MVP is usable on devices; no recovery or catalog workflow is
broken by layout/accessibility fixes.

### Context and starting points
Component-level previews cannot prove complete forms/navigation usable on actual
devices. Inputs are completed daily-use routes, approved D10 matrix and the catalog.
Read [Sello v2](../../../design/v2/README.md) (large text, reduced motion, exact spoken amounts), the v1.1 [a11y](../../../design/sello-spec.html#a11y) section, adaptive/navigation guidance and
[process-restoration procedure](../EXECUTION_GUIDE.md#device-and-process-restoration).

From SELLO-E12 (2026-10-16): the journeys include the v2 Recibo delivered by
SELLO-061–069, so this ticket starts after that epic.

**Owner direction (2026-10-23):** manual assistive-technology walks (TalkBack,
Switch Access, hardware keyboard) are outside the MVP and move to post-MVP
[SELLO-074](E14-assistive-technology.md#sello-074--review-the-app-with-talkback-switch-access-and-a-keyboard).
The automated semantics checks stay here: exact spoken amounts, labels, focus order,
48dp targets and status that is not color alone.

### Implementation plan
1. Create `docs/testing/ui-acceptance-matrix.md` listing build/API/window/font/theme/
   input mode, each daily-use journey and expected accessible exact values.
2. Automate Compose semantic/focus/restoration checks and execute reduced-motion
   walks on agreed configurations.
3. Run compact portrait, <480dp short landscape, medium and expanded panes with
   keyboard/gesture insets and scales 1.0/1.3/2.0. Test both completed tab stacks.
4. Exercise drafts/operation IDs through rotation, eligible process death and cold
   start separately. Verify saved receipt cannot trigger another write on recreation.
5. Fix found clipping/focus/copy/back issues in their owning layer, add regression
   tests and rerun prior completed journeys. Keep a matrix of passes and outstanding blockers.

### Concrete cases and pitfalls
- Semantics carry the currency/full amount and stamp meaning; charts provide exact
  selected/text values. Color-only over-budget state is not accessible.
- A focused note field remains reachable with keyboard open in landscape; 2.0 scale
  cannot truncate hero amounts or put save/delete actions below an unreachable panel.
- Force-stop success proves durable reopen, not saved-task restoration; report it separately.

### Verification recipe
Run affected app/design-system/catalog connected tests and G2 across the approved
matrix. Retain annotated screenshot/video/focus notes with exact configuration and
financial scenario. No single screenshot or emulator configuration closes the whole matrix.

### Execution progress

- **Part 1 of 2, sweep and fixes** (`sello-033-ui-matrix`). The owner chose, on
  2026-10-23: the real app shell for the sweep, WCAG AA as the contrast floor, fixes in
  the same part when small. Delivered: `UiAcceptanceSweepTest` opens 13 daily-use
  screens through the app's own buttons in five windows (compact 2.0, short landscape
  1.3 and 2.0, medium, expanded; all four themes), checking 48dp touch targets, text
  not cut short, amounts spoken in pesos and main actions inside the window;
  `SelloColorsTest` gained the stamp, soft-fill and past-month pairs. Fixed, each red
  first: screen and month titles take a second line, and move under their actions when
  even that does not fit; the rail widens to its longest label; chips wrap; limit
  suggestions and Resumen's limit line are read with "pesos". Matrix:
  [UI acceptance matrix](../../../testing/ui-acceptance-matrix.md).
- **Part 1 gate:** `./scripts/verify-ticket SELLO-033 --retain` passed on `4a156c5`
  (`main` at `87f3a8f`), run 20261023T065031Z-dbb34ab5 on the API-30 AVD `ci30`
  (`emulator-5582`): 674 host and 702 device tests, zero failures; lint 0 errors and
  the 33 existing warnings. Not the ticket's Quality run: the ticket stays In Progress
  until part 2.
- **Part 2 of 2, device walks** (`sello-033-walks`). On the isolated API-30 phone
  emulator `sello022_api30`: process death for a new expense, a saved expense, an
  expense edit, a new income and a new category, each draft back in a new process
  and each confirmed action one receipt, unchanged by a further kill; rotation in the
  same process; force-stop and cold start reported apart; real medium and expanded
  windows; the note composer sideways with the keyboard at 2.0; reduced motion. The
  sideways walk found **Listo** under the navigation bar on the right; the composer now
  keeps its content clear of the side system bars (red first in
  `ComponentContractsTest`). Results and screenshots are in the matrix.

### Delivery evidence

- **Revision:** the final gate tested commit `8a50ce9` on `sello-033-walks`, on `main`
  at `f67ae77` (part 1, pull request #86); the only uncommitted file was the retained
  report. The Quality run line below is added to that commit afterwards.
- **Decisions:** the owner moved manual TalkBack, Switch Access and keyboard walks to
  post-MVP SELLO-074 on 2026-10-23 (pull request #85), and chose the real app shell
  for the sweep, WCAG AA for contrast and small fixes in the same part.
- **Requirement mapping:** [UI acceptance matrix](../../../testing/ui-acceptance-matrix.md):
  four schemes, scales 1.0/1.3/2.0, compact, short-landscape, medium and expanded
  windows in `UiAcceptanceSweepTest` and real-window walks; insets (system bars,
  keyboard, a side navigation bar); reduced motion; exact spoken amounts; process
  death, rotation and cold start reported separately, each confirmed action one
  receipt. Tab, month, detail and panel Back keep their existing tests; no manual
  assistive-technology walk is claimed.
- **Red / Green:** each fix failed first: `LargeTextFitTest` (titles, month title,
  rail, chips, a title moved under its action), `CategoryEditorViewModelTest` and
  `CategoryManagementViewModelTest` (suggestions read "200.000"), `ChartExactSelectionTest`
  (no spoken limit line), `ComponentContractsTest.aNavigationBarAtTheSideCoversNeitherCancelNorDone`
  (Listo under a 48dp side inset). The sweep's first runs had two faulty checks of its
  own, a width-overflow flag true for whole text and a clipped-node size without
  Compose's touch widening; both were corrected and recorded in the matrix.
- **Tests:** device `UiAcceptanceSweepTest` (5 windows), `LargeTextFitTest` (6),
  `ComponentContractsTest` (+1); assertions added to `ChartExactSelectionTest`,
  `CategoryEditorScreenTest`, `ResumenScreenTest`, the two category view model tests
  and `SelloColorsTest`.
- **Gate results:** `./scripts/verify-ticket SELLO-033 --retain` passed all six checks,
  full coverage. Host: 674 tests; device: 703 tests (data 229, design-system 129, app
  324, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as before. Part 1 passed its own gate (run 20261023T065031Z-dbb34ab5).
  Its hosted check failed once in `ScenarioReplayTest.aRunInterruptedAfterACommitAndResumedMatchesAFreshRunExactly`,
  unrelated to this ticket, and passed when the workflow was run again; reported, not
  changed here.
- **Quality run:** run 20261023T074006Z-cfef9451; SELLO-033 G2 passed; HEAD 8a50ce9, inputs sha256 ff410882368c; report docs/planning/mvp/quality-reports/SELLO-033.json sha256 f3c4887e236a3a15243391c3e9378df8ce9f9ca55dd884d674457c53a752024d
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (`emulator-5582`); the walks on the isolated API-30 AVD `sello022_api30`
  (`emulator-5584`), wiped first, with debug builds of the code under test;
  screenshots in [evidence](../../../testing/evidence/SELLO-033). No physical device or
  personal data was used.
- **Review:** executor self-review covered each check's false positives, the test
  windows against real ones and every walk's database result. Done is proposed by this
  ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.

## SELLO-034 — Prove integrated data retention and recovery failure safety

- **Type:** Task
- **Priority:** P0
- **Status:** Done
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

### Delivery evidence

- **Revision:** the gate tested commit `204c417` on `sello-034-recovery-matrix`, on
  `main` at `6074176`; the only uncommitted file was the retained report. The Quality
  run line below is added to that commit afterwards.
- **Decisions:** the owner approved, on 2026-10-22: all nine sandbox scenarios through a
  backup and restore; fixing here that Respaldo's top-bar arrow left a restored
  history without restarting; one pull request.
- **Requirement mapping:** [recovery matrix](../../testing/recovery-matrix.md) lists the
  supported schema (1→2→3, open 3; newer refused to the start-up failure screen) and
  backup-format pairs (1 into schema 3; newer refused), every restore, reset, start-up
  and storage failure point with its outcome and test, and corrupt/unavailable storage
  behaviour. `RecoveryRetentionTest` chains a hand-written version 1 history (archived
  category, budget history, past-month limits, ordered expenses with notes, salary and
  other income, Lima zone, a receipt) through upgrade, backup with preferences,
  restore into a Bogotá installation, reset and restore again, asserting exact rows,
  zone and generation at each step; restored records start at version 1, as the
  backup format defines. `TemporalScenarioCorpusTest` round-trips all nine scenarios:
  identical rows for all, and the seven with a final check after their last saved
  change match all 24 figures again. `ScenarioReplayTest` shows a scenario in a new
  process after a reset finds no receipts, does not advance and writes nothing. No
  pre-v1 migration is invented; the older app's database stays unsupported.
- **Red / Green:** `theTopBarsBackArrowAfterARestoreStartsTheAppAgain` failed first
  (the arrow went back without restarting) and passed once `BackupRoute` sends a
  replaced history's arrow through `RestoreAction.Leave`. Adding it exposed that the
  restore journey tests depended on run order through the process-wide session; each
  test now resets it. The retention test's first expectation of kept record versions
  was wrong against the format and was corrected from the format, not the output.
- **Tests:** device `RecoveryRetentionTest` (1), `TemporalScenarioCorpusTest` (+1),
  `ScenarioReplayTest` (+1), `RestoreJourneyTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-034 --retain` passed all six checks,
  full coverage. Host: 674 tests; device: 691 tests (data 229, design-system 122, app
  319, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as before. An earlier run of this work failed only ktlint on two new test
  files, with every test passing; they were formatted and the gate run again.
- **Quality run:** run 20261023T045232Z-335c50c5; SELLO-034 G2 passed; HEAD 204c417, inputs sha256 e47e3fe8b175; report docs/planning/mvp/quality-reports/SELLO-034.json sha256 8b02e8ae2b5977ea40b19396fac57584265a0981867feec2efe08f1455879f0b
- **Device / Artifact:** the isolated synthetic API-30 AVD `ci30` (`emulator-5582`);
  the corpus run logs each scenario's round trip under `ScenarioCorpus`. No physical
  device or personal data was used.
- **Review:** executor self-review covered the version pairs, the expected values'
  independence and the reset boundary. Found and left: the sandbox labels steps erased
  by a reset "Sin escritura, como se esperaba" (recovery matrix, known gaps). Done is
  proposed by this pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.

## SELLO-035 — Validate performance and optimized release composition

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-004, SELLO-023, SELLO-027, SELLO-032, SELLO-033, SELLO-034
- **Gate:** G3

### Outcome
Measure a realistically populated app and make customer release packaging deliberate.

### Deliverables
Representative synthetic dataset/device profile and approved performance thresholds,
startup/scroll/graph/backup measurements; correct off-main/bounded work; optimized
release configuration/keep rules, dependency/license review and artifact inspection.
Set private-APK production signing through secure local/CI inputs with no committed secrets.

### Acceptance criteria
- Large history remains responsive without full unbounded reads or main-thread file/
  replay work. Chart exactness stays intact; optimization never substitutes rounded money.
- Approved D10: measure representative histories and obtain explicit numerical target
  approval before acceptance. Record named device-testing owner and signing custodian;
  unassigned owners/credentials cannot be substituted with agent self-approval.
- Release DI graphs and shrunk code execute real commands/serialization/recovery;
  no debug clocks, fixtures, catalog code, no-op financial services or tools survive.
- Measurements state device/build/dataset and meet reviewed thresholds; no arbitrary
  claimed benchmark. Signed release installs and launches with correct identity.

### Tests
G3 including actual optimized-release manual/automated smoke, dependency/security
reports, artifact inspection and repeatable performance runs. CI ephemeral signing
and customer-distribution signing are validated separately; lack of credentials is a blocker.

### Working checkpoint
A candidate customer artifact—not the catalog or debug variant—works with persisted
financial history and recoverable files. Reports support the final acceptance decision.

### Context and starting points
Debug responsiveness and unshrunk DI do not establish release behavior. Inputs are
approved D10 targets/device profile, all recovery/privacy/a11y evidence and actual
signing access. Read architecture §10, source-library shrinking/signing docs and
the currently unoptimized release configuration in `app/build.gradle.kts`.

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
- No signing access means G3 is blocked, not “signed by debug key”. Build the
  approved private APK without uploading, tagging or publishing it. No AAB/Play
  deliverable is required for MVP; any channel change needs explicit approval.

### Verification recipe
Run host/device gates and actual `:app:assembleRelease`; execute G3 on the
production-signed optimized private APK candidate, not a debug/ephemeral CI substitute.
Record hash/version/signing certificate, independent release money/recovery smoke,
measurements/thresholds and artifact exclusions. Never write secrets in commands/reports.

### Execution progress

- **Owner decisions, 2026-10-23:** measurements through `adb` with no new libraries;
  the API-30 emulator as the reference device; the targets in the
  [performance profile](../../../testing/performance-profile.md), approved before any
  result; the owner tests on physical devices and keeps the signing key, read in part 3
  from the owner's `~/.gradle/gradle.properties`, never from the repository. An
  independent technical reviewer is not assigned.
- **Part 1 of 3, measurements** (`sello-035-measure`). Delivered: a seeded synthetic
  large history (20 categories, 10,000 expenses, 2,000 incomes, 12 past months)
  restored through Respaldo; `scripts/performance/measure.py` for cold and warm start,
  Recibo scrolling, Resumen, a category's detail, backup and restore; Recibo reports
  itself fully drawn once the month is read (red first); `MainThreadWorkTest` fails on
  main-thread disk work from Sello's code. On the unshrunk release build, on top of
  #88, every target is met; Resumen has the least margin (535 ms of 700). Remaining: R8 shrinking and keep rules with the
  journeys re-measured (part 2); production signing, artifact inspection and G3
  (part 3).
- **Part 2 of 3, shrinking** (`sello-035-shrink`). R8 shrinks and optimizes the
  release build with the libraries' keep rules and none of Sello's: on the optimized
  build, saving, editing, deleting with undo, an income, a new category, a theme kept
  across a restart, a draft across process death, a past month, backup, restore and
  erasing all worked against the expected figures, with no crash. APK 28.9 MB → 4.5 MB;
  every target met with more room (Resumen 402 ms of 700). Remaining: production
  signing, artifact inspection and G3 (part 3).
- **Status stayed Backlog until G3 existed.** The runner verifies an In Progress ticket
  at its own gate, and G3 is what this ticket delivers; a lower gate may not stand in.
  Parts 1 and 2 were verified as merge candidates (G2).
- **Part 3 of 3, signing and G3** (`sello-035-signing`). The owner chose, on
  2026-10-23, to keep the key off CI ([ADR 0016](../../../decisions/0016-release-gate-signing-split.md)).
  The release build signs from the owner's existing `sello.signing.*` Gradle properties
  (`~/.android/sello-release.jks`); its certificate digest is pinned. G3 adds a reviewed
  licence list (143 libraries: 142 Apache-2.0, 1 BSD-3-Clause), the production signature
  and artifact checks with R8's mapping kept beside the APK, and installing and opening
  the signed APK on the emulator; CI runs it with a throwaway key. With the owner's
  approval for this one step, osv.dev and the published POMs found no advisory and no
  licence difference ([advisories](../../../release/advisories.md)). The signed APK
  restored the large history and backed it up by hand. Open for the owner: the app shows
  no third-party licence notice.

### Delivery evidence

- **Revision:** the gate tested commit `bc0767f` on `sello-035-signing`, on `main` at
  `ea65c33`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards. Parts 1 and 2 merged as #89 and #90.
- **Decisions:** the owner approved, on 2026-10-23: `adb` measurements with no new
  library, the API-30 emulator as reference, the targets in the
  [performance profile](../../../testing/performance-profile.md) before any result; the
  owner as device-testing owner and signing custodian; signing from their existing
  `sello.signing.*` properties; ADR 0016 (option 1, key off CI); one network step for
  advisories and published licences. No independent technical reviewer is assigned:
  D10's reviewer remains SELLO-036's blocker, not this ticket's.
- **Requirement mapping:** large history: 20 categories, 10,000 expenses, 2,000 incomes
  and 12 past months, seeded and restored through the app's own validation; every
  target met on the shrunk release (cold start to figures 314 ms of 1.5 s, warm 68 ms,
  Recibo 0% slow frames, Resumen 402 ms of 700, backup 1.25 s of 5, restore 2.22 s of
  10), device, build and dataset stated. Reads stay off the main thread
  (`MainThreadWorkTest`); Resumen reads only the 12 months it charts; money stays exact.
  R8 with no keep rule of Sello's; the shrunk build saved, edited, deleted with undo,
  recorded income, created a category, kept a theme and a draft across restart and
  process death, showed a past month, backed up, restored and erased, with the
  developer-tools check clean. The release APK is signed by the owner's key, its digest
  pinned; G3 checks it, keeps R8's mapping beside it and installs and opens it; 143
  libraries reviewed (142 Apache-2.0, 1 BSD-3-Clause), no advisory. The signed APK
  restored and backed up the large history by hand.
- **Red / Green:** `theAppIsReportedFullyDrawnOnceRecibosFiguresAreReadAndNotWhileLoading`
  failed first (never reported) and passed with `ReportDrawnWhen`. `MainThreadWorkTest`
  failed on a disk read planted on the main thread, then passed without it. The G3 tests
  (`test_gates`, `test_selection`, `test_evidence`, `test_distributed`) failed against
  the old refusal before G3 was implemented.
- **Tests:** device `ReciboScreenTest` (+1), `MainThreadWorkTest` (1); Python
  `test_dataset` (9), `test_measure` (9), `test_release` (13), `test_licenses` (6), and
  G3 cases in `test_gates` (3 new, replacing the refusal test), `test_selection` (+1),
  `test_evidence` (+2), `test_distributed` (+2), `test_workflow` and `test_runner`
  (changed).
- **Gate results:** `./scripts/verify-ticket SELLO-035 --retain` passed all eleven G3
  checks, full coverage. Host: 694 tests; device: 728 tests (data 235, design-system
  133, app 339, catalog 21); zero failures, errors or skips. Lint had zero errors and
  the same 33 warnings as before. Release: `sello-1.0-1.apk`, 4,458,299 bytes, sha256
  `ef2ca71d468b342114aed23904c9f279750e8a9a3888227c78fa8579d1b640ee`, APK Signature
  Scheme v2, certificate sha256
  `abf6ccf5b58de43626daea387d5c18c67981e42c302f8a1c7e16ad5cf0b4d952`, mapping sha256
  `1543e4a0d88c350965803f98ef0c00f0280055b7ac41f9006073d70a45619f58`; installed and
  opened on `emulator-5582`. An earlier run failed only ktlint on `app/build.gradle.kts`,
  with every other check passing; it was formatted and the gate run again. The first
  hosted G3 failed `release-ephemeral`: apksigner 37.0.0 on CI names the signer
  "V3.0 Signer:", a form the parser did not read; it now reads all three forms (tests
  from CI's exact output), and the gate was run again.
- **Quality run:** run 20261023T185147Z-a1f077c8; SELLO-035 G3 passed; HEAD bc0767f, inputs sha256 22a6c9eaeb16; report docs/planning/mvp/quality-reports/SELLO-035.json sha256 7c45b5d0ba4792059c8a424d67d42f358e996e85b2d8adc9e42bd30b5f9a4dd9
- **Device / Artifact:** the gate on the API-30 AVD `ci30` (`emulator-5582`);
  measurements and walks on the isolated API-30 phone emulator `sello022_api30`
  (`emulator-5584`). No physical device or personal data was used; synthetic data only.
- **Review:** executor self-review covered the targets against the results, the money
  path on the shrunk build, the key never leaving the owner's machine and CI never
  completing G3. Found and left: the restore preview and message write "10000" without
  a thousands separator; Resumen shows "2026-10-23" in storage form; the app shows no
  third-party licence notice (an owner decision). Done is proposed by this pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

## SELLO-036 — Accept the complete MVP and prepare release handoff

- **Type:** Task
- **Priority:** P0
- **Status:** Done
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
The approved channel is private APK only. Handoff must identify the exact APK hash,
version/signing identity and installation/recovery instructions; no store listing or
AAB upload is a prerequisite. Signing custody and device/reviewer owners must be resolved.
Approved D10 assigns product/release go/no-go to the project owner; independent
technical review and actual physical-device/signing evidence must be recorded before
acceptance. Approval of the process is not approval of a future candidate artifact.

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

### Execution progress

- **Owner decisions, 2026-10-23:** the owner is the independent technical reviewer and
  gives the product go/no-go; the walkthrough runs on their Samsung SM-S731B
  (`R5GYC4FA50N`) with a fresh install and synthetic data (option b); SELLO-075–077 and
  the missing licence notice are accepted as non-blocking; the version name is
  `0.1.0`; the backup blocker below is fixed in this ticket and its misleading message
  logged as SELLO-078.
- **Blocker found and fixed:** on the phone every backup failed its own check because
  Android 16's clock gave the backup timestamp microseconds; `BackupExport` now rounds
  it to the millisecond (red first in `BackupSnapshotConsistencyTest`). The walkthrough
  was repeated on the rebuilt candidate from a fresh install.
- **Delivered:** [MVP checklist](../../../release/mvp-checklist.md) with the candidate,
  scope, evidence, walkthrough, developer-app check, accepted issues and recovery
  guidance.

### Delivery evidence

- **Revision:** the gate tested commit `bf3d690` on `sello-036-acceptance`, on `main` at
  `6f79598`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards.
- **Decisions:** as recorded under Execution progress (owner as independent reviewer and
  go/no-go, option b on the owner's phone, four accepted issues, version `0.1.0`, the
  backup fix here). The product go/no-go is the owner's merge of this pull request.
- **Requirement mapping:** [MVP checklist](../../../release/mvp-checklist.md): candidate
  `sello-0.1.0-1.apk`, 4,458,295 bytes, sha256
  `1e28bb8c14d093b67846036fc96282bc94c3cbae06a7e60c95125a89fefa16a9`, signed (v2) by the
  owner-confirmed certificate `abf6ccf5…d952`, mapping sha256
  `c63995b6b3baca59ba15737923618672a119f2eab2c34e9f534100c101c424e5`; the G3 build is
  byte-identical to the APK walked on the phone. Full flow on the Samsung SM-S731B
  (Android 16) from a fresh install: category, expense, edit, delete and undo, income,
  Resumen, appearance and order, backup, change, restore with restart, force-stop,
  erase all, reuse, every figure as worked out beforehand. Catalog and sandbox (G01, 4
  of 4 figures) launched on the API-30 emulator; neither is in the candidate. 48 other
  MVP tickets Done with valid evidence; no out-of-scope feature or link present.
- **Red / Green:** `aClockFinerThanMillisecondsStillMakesABackupStampedToTheMillisecond`
  failed first with `Unrestorable(InvalidValue(exportedAt))`, as on the phone, and passed
  once `BackupExport` rounds its timestamp to the millisecond.
- **Tests:** device `BackupSnapshotConsistencyTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-036 --retain` passed all eleven G3
  checks, full coverage. Host: 694 tests; device: 729 tests (data 236, design-system
  133, app 339, catalog 21); zero failures, errors or skips. Lint had zero errors and
  the same 33 warnings as before; `release-production` and `release-launch` passed.
- **Quality run:** run 20261023T202433Z-3d80d646; SELLO-036 G3 passed; HEAD bf3d690, inputs sha256 a704cb74f639; report docs/planning/mvp/quality-reports/SELLO-036.json sha256 e699f95401478220a25c9dd9dff8a121a4a9338a30913874753442c75fa30c9d
- **Device / Artifact:** the owner's Samsung SM-S731B (`R5GYC4FA50N`, Android 16) for
  the walkthrough, synthetic data only; the API-30 AVDs `ci30` (gate) and
  `sello022_api30` (reproduction, catalog and sandbox). Left on the phone: Sello `0.1.0`
  with synthetic data and two backup files in Downloads (14:58, from the failed attempt;
  15:15). Nothing else on the phone was opened or changed.
- **Review:** independent technical review and product go/no-go by the project owner;
  executor self-review covered the expected values, the blocker's cause and the
  candidate's identity. Accepted non-blocking: SELLO-075–078 and the licence notice.
  Distribution, upload or tagging is not part of this ticket.

