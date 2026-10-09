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

Follow [QUALITY_FLOW.md](QUALITY_FLOW.md) and the [quality runner](#quality-runner)
section below for the mandatory run, snapshot freshness, retention and completion
validation. Do not start dependent work until SELLO-004's acceptance is Done.

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

## Quality runner

Prerequisites: the environment from [setup](../../development/setup.md) (JDK 25 on
`PATH`, `ANDROID_HOME` or `sdk.dir`), Python 3, Git, and for G2 a booted device.

```bash
./scripts/verify-ticket SELLO-017            # run the gate the ticket requires
./scripts/verify-ticket SELLO-017 --gate G2  # run a higher gate; lower is refused
./scripts/verify-ticket SELLO-017 --retain   # passed run -> retained report + field text
python3 docs/planning/mvp/board.py           # regenerate views after ticket edits
python3 docs/planning/mvp/board.py --check   # completion validation
python3 scripts/quality/architecture.py      # architecture rules alone, quick feedback
./gradlew ktlintFormat                       # fix formatting findings
./scripts/update-dependency-locks            # after a deliberate dependency change
python3 -m unittest discover -s scripts/quality -p 'test_*.py'
```

Flow for a ticket:

1. Keep the ticket In Progress while working. Run `verify-ticket` as often as needed;
   each run writes `build/reports/quality/<ticket>/<run id>/report.json` and one log
   per check. That directory is ignored by Git.
2. When the work is final, run with `--retain`. It copies the passed report to
   `docs/planning/mvp/quality-reports/<ticket>.json` and prints a
   `- **Quality run:** ...` line. Paste that line into the ticket's Delivery evidence.
3. Set the status to Review, regenerate the board, run `board.py --check`, commit
   the retained report with the change and open the pull request.
4. CI runs `verify-ticket --merge-candidate <base> --gate G2` on the merge commit
   and then `board.py --check --base <base>`. Its reports are the `quality-reports`
   artifact of the workflow run, kept 90 days:
   `gh run download <run id> -n quality-reports`.

What makes a report stale: any change to a tracked or untracked-but-not-ignored
file, including code, tests, build files, guardrail documents and a ticket's own
text. What does not: ticket status lines, `Execution progress` and `Delivery evidence`
sections, generated board views, retained reports and ignored build outputs. A
ticket already Done in the base revision keeps its original snapshot; rewriting its
acceptance text still invalidates it.

Recovering from failures:

| Message | Meaning and action |
| --- | --- |
| `REFUSED: Unknown ticket` / `prerequisite ... is not Done` | Nothing ran. Fix the ID or finish the prerequisite; do not verify out of order. |
| `REFUSED: ... would be a downgrade` | The ticket's gate is the minimum. Remove `--gate` or pass a higher one. |
| `REFUSED: No working java` / `Android SDK not found` | Export the setup environment in this shell. |
| `REFUSED: ... no connected device` | Boot the isolated emulator from setup; test-APK assembly is not a substitute. |
| `REFUSED: G3 ... not available` | Release checks arrive with SELLO-035. The ticket cannot be verified earlier. |
| `FAILED: <check> exited N` | Read `<check>.log` beside the report, fix the cause, run again. |
| `FAILED: ... produced no test results` | The task ran no tests. Device results must be newer than the run. |
| `FAILED: inputs changed while checks were running` | Stop editing during a run and run again. |
| `Dependency Locking` / `does not have lock state` in a Gradle log | A dependency changed without its lock. If intended, run `./scripts/update-dependency-locks` and review the lockfile diff as the dependency review. |
| `board-check` fails on stale views | Run `python3 docs/planning/mvp/board.py`, then verify again. |
| `report is stale` / `acceptance text changed` | Something changed after the retained run. Verify again with `--retain` and update the field. |
| `does not match the recorded sha256` | The retained file or the field was edited. Regenerate both with `--retain`. |

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
