# Handoff — SELLO-003 onward

**Snapshot:** 2026-10-08, accompanying the SELLO-002 delivery commit.
This is a starting-state guide, not a second architecture or permission to bypass
ticket dependencies. Verify current HEAD, Git status, code and ticket status first.

## Read before editing

1. Root [AGENTS.md](../../AGENTS.md) and [ARCHITECTURE.md](../../ARCHITECTURE.md).
2. [Board README/gates](../planning/mvp/README.md),
   [execution guide](../planning/mvp/EXECUTION_GUIDE.md),
   [current board](../planning/mvp/BOARD.md), and the complete
   [SELLO-003 brief](../planning/mvp/epics/E01-foundation.md#sello-003--bootstrap-enforced-modules-and-variant-identities).
3. [Toolchain ADR](../decisions/0005-toolchain.md), [setup](setup.md),
   [toolchain evidence](../testing/SELLO-002-toolchain-verification.md), and
   predecessor Delivery evidence in the foundation epic.
4. [Approved product contract](../decisions/0001-mvp-contract.md),
   [version-32 examples](../testing/mvp-financial-examples.md), ADRs
   [0002](../decisions/0002-signed-carryover-direction.md),
   [0003](../decisions/0003-mvp-transaction-amount-range.md),
   [0004](../decisions/0004-category-latest-expense-order.md),
   [quality flow](../planning/mvp/QUALITY_FLOW.md) and
   [local visual reference/provenance](../design/README.md).

## What is actually complete

- **SELLO-001: Done.** Owner approved D01–D10 and the entire guided walkthrough.
  A separately authorized agent independently reviewed all 88 case IDs and
  additional examples: Pass. [Review record](../testing/reviews/SELLO-001-independent-review.md).
  Older dated pending-review entries preserve chronology; closure supersedes them.
  Do not reopen product questions just because an earlier progress entry says pending.
- **SELLO-002: Done.** Reproducible matrix, setup, cold-source/cache builds,
  processor/JVM probe, API-30 greeting launch and one instrumented context test
  passed. Its review is explicitly executor self-review, not independent approval.
- Actual production code is still **one `:app` Compose greeting**. There are no
  domain/data/design-system/catalog modules, financial implementation, database,
  sandbox, architecture runner or hosted CI. Temporary Room/JVM probe artifacts
  are evidence, not delivered modules or application tests.
- Last verification: one sample JVM test, one executed API-30 context test,
  32 board tests; lint passes with **18 warnings**. Existing warnings were not
  suppressed. The isolated emulator was shut down; recheck device availability.

## Execute SELLO-003 next

003 remains **Backlog**, but both dependencies are Done, so it is eligible to
start. Follow its entire brief; record actual progress/status in the epic and
regenerate exports. No other ticket is authorized to skip its prerequisites.

- Create Kotlin/JVM `:domain`, Android-library `:data` and `:design-system`, and
  separate Android app `:catalog`; preserve usable existing `:app`.
- Enforce architecture §2's graph. Domain has no Android/UI/storage/DI dependency;
  design-system has no domain/data/app dependency; catalog depends only on the
  design-system/UI and synthetic render fixtures. App data imports are composition-only.
- Move reusable template theme ownership to design-system without copying it;
  keep app and minimal catalog genuinely installable/launchable. No fake save,
  empty feature navigation, financial schema or catalog duplicate components.
- Identities: customer `com.software.sello` / **Sello**; debug
  `com.software.sello.debug` / **Sello Debug**; catalog
  `com.software.sello.catalog` / **Sello Catalog**. They must coexist without data
  collision. Update the current literal package assertion for actual variants.
- Configure test/build source sets, Room/KSP/schema-export capability in data;
  do not introduce a dummy database/table to simulate completion. Actual Room
  version-1 schema/business integrity belongs to SELLO-011.
- Prove a forbidden domain Android import fails for the intended boundary,
  remove the negative fixture, and rerun green. Run **G1**, inspect dependency/
  package identities, assemble affected app/catalog test APKs and install/launch
  both. Compilation and NO-SOURCE tests are not executed behavior tests.

## Toolchain constraints to preserve

AGP **9.3.3**, Gradle **9.5.0**, JDK **25** (tested JetBrains 25.0.2),
Kotlin/JVM/Compose compiler **2.4.20**, Compose BOM **2026.02.01**, KSP2 **2.3.12**,
compile/target API **37**, minimum **30**, Java/Kotlin output **11**, build tools **36.0.0**.
ADR 0005 records exact vendor-range limits and the execution-tested AGP patch difference.

- Existing catalog aliases: Android application, Kotlin Compose/JVM and KSP.
  Root already declares JVM `apply false` **before** AGP/Compose. Reuse them;
  add an Android-library alias using the same AGP pin when needed.
- A child-only versioned JVM plugin request failed on the already loaded unknown
  KGP classpath. Declare versions at root and apply consistently in children.
- Keep AGP built-in Kotlin; do not add kotlin-android or disable source-set
  validation. The older KSP 2.2.10-2.0.2 candidate failed that validation.
- Room **2.8.4** is a verified processor candidate, not a production dependency
  already in the catalog. Pin actual adopted Room artifacts centrally when introduced.
- Preserve pins unless a scoped, primary-source-backed compatibility decision
  requires change; a newer-version lint advisory is not an upgrade instruction.

Host commands and device provisioning are in setup. Relevant environment:

```bash
export ANDROID_HOME=/home/okabe94/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Daemon criteria override JAVA_HOME for daemon selection; inspect actual versions.
At the last check product-native device access was disabled, but a new isolated
CLI emulator worked with KVM. Recheck tools first; never wipe an owner's AVD/device.
Do not depend on another agent's `/tmp` files, cached SDK/JDK locations or live emulator.

## After 003: dependency-driven, not numeric-only

**004 follows 003 and blocks every later ticket.** It must deliver the real
`scripts/verify-ticket` entrypoint, structured reports/fingerprints, shared evidence
validator, architecture/static rules, CI execution and protected-check enforcement.
Read QUALITY_FLOW; the runner is **not available today**. 001–003 use bootstrap
recipes; 004 can verify itself before Done. After 004, every ticket requires its
fresh validated Quality run before Review/Done. Do not downgrade missing checks.

No Git remote/hosted runner/admin configuration exists at this snapshot. 004 will
need user-provided Git host/remote and appropriate access for hosted execution/
protection evidence. Local tooling or workflow YAML alone cannot close its hosted
acceptance. Report the concrete missing input; do not weaken the ticket.

After actual 004 completion, **005**, **006**, **010** become eligible according to
their individual dependencies. Read each canonical brief rather than implementing
the whole epic at once. Meaningful behavior red→green, real Room where required,
isolated sandbox scenarios and real catalog examples must grow with features.

## Product/ownership boundaries

Financial policies are already approved: exact integer COP; unified 12-digit
transaction cap; COP-50 flooring only for daily allowance; overall budget versus
category overspend distinction; stable historical limits; last expense **addition**
ordering; guarded undo; plaintext backup disclosure and atomic recovery. Use the
contract/examples as truth, not design sample numbers or newly guessed formulas.

MVP has Recibo/Resumen only. Widgets are excluded; recurrence/goals/investments/
FX/batch entry and signed surplus/deficit carryover are post-MVP. Carryover is
planned in E09, not dormant MVP tables/jobs. Preserve facts/boundaries, not speculative engines.

The owner handles periodic physical-device testing and release build/signing/key
custody; private APK only. Do not ask for signing secrets. Future independent
financial/recovery/release reviewers require separate assignment; the SELLO-001
agent review did not appoint a permanent reviewer.

Keep epic files canonical and run board generation/checks after edits. Preserve
other work; no automatic commit/push/branch or delegation without current explicit
authorization. Record exact tested revision/dirty files, gates, warnings, device
evidence and designated review before Done. This handoff grants no Git or review waiver.
