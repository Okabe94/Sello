# Independent executor guide

Read this with the assigned ticket, [agent rules](../../../AGENTS.md),
[architecture](../../../ARCHITECTURE.md), and [workflow/gates](README.md).
It supplies shared tools and conventions; the ticket supplies the actual work.
Do not execute all tickets at once or copy a proposed API blindly over completed code.

## Starting from a new checkout

1. Run `pwd`, `git status --short`, `git log -1 --oneline`, and inspect applicable
   `AGENTS.md`. Preserve existing work. The initial app is a generated greeting,
   not the target architecture; see [baseline](BASELINE.md).
2. Read the ticket's dependencies and their Delivery evidence. Locate their
   **actual** produced contracts, tests and decision records. If a supposedly Done
   prerequisite is missing/broken, report the discrepancy; do not recreate a fake
   adapter just to unblock yourself. Ready work still requires its listed approvals.
3. Make an acceptance-to-change/test checklist and identify owned files before
   editing. For new modules/symbols, paths below are target locations, not claims
   that the files already exist. Prefer established compatible names after bootstrap.
4. For UI work, open the [local Sello reference](../../design/sello-spec.html) and
   the exact anchors named in the ticket. Never infer money from its mockup figures.
5. Use SELLO-002's [setup](../../development/setup.md) and
   [toolchain decision](../../decisions/0005-toolchain.md). The baseline commands
   below use this host's inspected paths. Record unavailable devices/keys/
   approvals as blockers where acceptance requires them; never waive the gate.

## Module and file map

The baseline has `app/src/main/java/com/software/sello/MainActivity.kt`,
`app/src/main/java/com/software/sello/ui/theme/`, manifest/resources, sample tests,
`settings.gradle.kts`, `gradle/libs.versions.toml` and wrapper configuration.
SELLO-003 creates the rest; SELLO-001/002 records confirm identities/compatibility.

| Target ownership | Proposed implementation/test locations |
| --- | --- |
| Pure financial types/policies/ports | `domain/src/main/kotlin/com/software/sello/domain/{model,policy,port,usecase}/`; tests in `domain/src/test/kotlin/` |
| Room/transactions/codecs | `data/src/main/java/com/software/sello/data/{local,mapper,repository,backup,preferences}/`; host tests in `data/src/test/`, real Room in `data/src/androidTest/`; exported schemas in `data/schemas/` |
| Reusable UI | `design-system/src/main/java/com/software/sello/designsystem/{theme,paper,money,input,category,navigation,chart,feedback}/`; previews and host/device tests alongside that module |
| App wiring/platform | `app/src/main/java/com/software/sello/{composition,navigation,platform,presentation}/`; composition is the only app package importing data implementations |
| Feature UI | `app/src/main/java/com/software/sello/feature/{category,expense,receipt,income,recibo,resumen,settings,backup}/`; each owns State/Action/Event/ViewModel/ScreenRoot/Screen as needed |
| Sandbox | `app/src/debug/java/com/software/sello/devtools/`, `app/src/debug/res/` and debug-only bindings; release has no reverse dependency |
| Catalog | `catalog/src/main/java/com/software/sello/catalog/{navigation,examples,fixtures}/`; synthetic render examples only, no domain/data/app dependency |
| Decisions/runbooks | `docs/decisions/`, `docs/testing/`, `docs/release/`; these are created by their owning tickets, not pre-approved today |

Port/type names in tickets are contract suggestions. Agree signatures at the owning
boundary, document them in its tests, and reuse them downstream. Do not create a
second “almost equivalent” repository or calculation because a name differs.

## Tools and host verification

Required tools: Git, Gradle wrapper, agreed JDK, installed Android SDK/platform-tools,
Python 3 (standard library only) for board tooling. Use `rg` for source searches.
Android Studio is useful for previews/AVDs; it is not a substitute for command evidence.

```bash
export ANDROID_HOME=/home/okabe94/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew --version
./gradlew projects
python3 docs/planning/mvp/board.py --check
```

Run commands from the repository root. `JAVA_HOME` and daemon criteria can select
different JDKs; inspect `gradle/gradle-daemon-jvm.properties` and `./gradlew --version`.
The paths above are this machine's setup; another executor must configure its own
SDK/JDK and untracked `local.properties`, not commit machine paths as build logic.

Focused **host** examples after module creation:

```bash
./gradlew :domain:test --tests '*MoneyPolicyTest'
./gradlew :data:testDebugUnitTest --tests '*BackupValidatorTest'
./gradlew :app:testDebugUnitTest --tests '*ExpenseFormViewModelTest'
./gradlew :catalog:assembleDebug
```

The test names specified by tickets are **tests to create**; if compatible predecessor
tests already exist, document the actual selector. A no-matching-tests result is
not a pass. For a no-test configuration/docs change, verify the actual config/links
and run its gate instead of fabricating behavior red→green.
G0/G1/G2/G3 commands are in [README.md](README.md#gate-profiles); run the whole
assigned gate after focused tests, not only the example command shown in a ticket.

## Device and process restoration

Use an Android emulator/device matching SELLO-001/002's approved API matrix. If
T3 device tools are available, list/open a suitable device first; otherwise use
Android Studio's AVD manager and `adb devices`. Set `ANDROID_SERIAL` when multiple
devices exist. Never wipe or seed personal production data. Release QA uses an
explicitly isolated synthetic test installation; sandbox scenarios use debug only.

```bash
adb devices
./gradlew :app:assembleDebugAndroidTest :data:assembleDebugAndroidTest
./gradlew :data:connectedDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :catalog:connectedDebugAndroidTest
```

For an instrumented class selector use the actual configured test package:

```bash
./gradlew :data:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.software.sello.data.ExpenseCommandContractTest
```

- **Rotation/activity recreation:** exercise layout and SavedStateHandle/ViewModel
  restoration, with exactly one financial write after repeat composition.
- **Eligible process death:** enter a draft, background via Home, kill the **debug**
  background process using `adb shell am kill com.software.sello.debug`, then return
  through Recents. Verify the process really died, task remained, and draft/operation
  ID restores. Confirm the actual ID from SELLO-003 before using this example.
- **Cold start/force-stop:** test persistence/recovery separately. Force-stop or
  clearing app data is not equivalent to saved-task process restoration.
- **Room reopen:** close/reopen the real database in tests, preserving the file;
  in-memory fakes cannot prove durable receipts/migration/rollback.
- **Visual evidence:** record build, API, window size, theme/font scale and scenario.
  A screenshot confirms the shown state, not all financial/device acceptance.

## Contract and failure checklist

Commands carry operation ID, current generation and expected version for existing
records. Tests must independently inspect committed rows/receipt/revision, not
assert only a returned boolean. Same ID/same canonical input replays; differing
input conflicts. Rollback checks row **and** receipt absence. After-commit failures
must preserve committed data and recover by ID. Rethrow cancellation.

Read a consistent revision/as-of snapshot, not independently combined stale flows.
Do financial arithmetic with checked exact values; adapt to approximate coordinates
only in chart drawing. Storage/input/backup decoding failures remain typed failures.
No transient follow-up error may invalidate an already committed financial write.

Write real-Room rollback tests by provoking a real transaction constraint/failure;
use debug boundary decorators only for explicitly simulated read/caller effects.
No timing sleeps or swallowed assertions. Ports for file/provider failures make
write, close, canceled picker and preference-pending outcomes testable.

## Independent example corpus

See [DECISION_INPUTS.md](DECISION_INPUTS.md) for policy approvals and exact baseline
examples. Table values are independent assertions, not expected results generated
by calling the production calculator. Mark proposal-only conventions unapproved
until SELLO-001 is reviewed; never turn a pending example into hidden product policy.

## Delivery record

Follow [QUALITY_FLOW.md](QUALITY_FLOW.md) for mandatory runner, snapshot freshness,
artifact retention and completion validation. The runner is a SELLO-004 deliverable,
not an existing command; do not start dependent work until its acceptance is Done.

Add `### Delivery evidence` to the canonical ticket after verification. Required fields:

```text
- **Revision:** actual HEAD plus dirty files tested, or exact integrated commit
- **Requirement mapping:** acceptance item → implementation and independent test
- **Red / Green:** exact commands, behavioral failure, passing rerun; docs-only rationale if applicable
- **Gate results:** actual assigned gate command, counts, reports, warnings/failures
- **Quality run:** for SELLO-004 onward, run ID, ticket/profile, tested snapshot, outcome and retained artifact
- **Device / Artifact:** device/API/config/build and executed journey, or justified inapplicability
- **Review:** reviewer and result; distinguish self-review and outstanding approval
```

This is a template, not completed evidence. Do not mark Done while device/approval/
signing acceptance remains missing. Regenerate board/CSV after ticket/shared-workflow
edits, then check the generated output and board unit tests. Do not commit/publish
unless the current user request authorizes it.

## Source library

These are official integration guides, not pinned API/version guarantees. Read the
relevant guide against the selected versions; consult predecessor contracts first.

- Android architecture/state: https://developer.android.com/topic/architecture/recommendations
- Build/toolchains: https://developer.android.com/build/migrate-to-built-in-kotlin
  and https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria
- Build variants: https://developer.android.com/build/build-variants
- Typed navigation: https://developer.android.com/guide/navigation/design/type-safety
- Compose testing/restoration: https://developer.android.com/develop/ui/compose/testing
  and https://developer.android.com/develop/ui/compose/state-saving
- Adaptive UI/accessibility: https://developer.android.com/develop/ui/compose/layouts/adaptive
  and https://developer.android.com/develop/ui/compose/accessibility
- Room/migrations: https://developer.android.com/training/data-storage/room
  and https://developer.android.com/training/data-storage/room/migrating-db
- DataStore: https://developer.android.com/topic/libraries/architecture/datastore
- Documents/privacy: https://developer.android.com/training/data-storage/shared/documents
  and https://developer.android.com/identity/data/autobackup
- Koin constructors/graphs: https://insert-koin.io/docs/reference/koin-core/definitions/
- Coroutine tests: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/
- Serialization: https://kotlinlang.org/docs/serialization.html
- Static/format tools: https://detekt.dev/docs/intro/
  and https://pinterest.github.io/ktlint/latest/
- Shrinking/signing: https://developer.android.com/build/shrink-code
  and https://developer.android.com/studio/publish/app-signing
