# Independent executor guide

Read this with the assigned ticket, [agent rules](../../../AGENTS.md),
[architecture](../../../ARCHITECTURE.md), and [workflow/gates](README.md).
It supplies shared tools and conventions; the ticket supplies the actual work.
Do not execute all tickets at once or copy a proposed API blindly over completed code.

## Starting from a new checkout

1. Run `pwd`, `git status --short`, `git log -1 --oneline`, and inspect applicable
   `AGENTS.md`. Preserve existing work.
2. Read the ticket's dependencies and their Delivery evidence. Locate their
   **actual** produced contracts, tests and decision records. If a supposedly Done
   prerequisite is missing/broken, report the discrepancy; do not recreate a fake
   adapter just to unblock yourself. Ready work still requires its listed approvals.
3. Make an acceptance-to-change/test checklist and identify owned files before
   editing. Prefer the established names and packages below to new ones.
4. For UI work, open [Sello v2](../../design/v2/README.md) and its
   [component catalog](../../design/v2/components.html). A ticket's v1.1 anchors apply
   only to screens v2 does not show (ADR 0013). Never infer money from mockup figures.
5. Use the [setup](../../development/setup.md) and
   [toolchain decision](../../decisions/0005-toolchain.md). Record unavailable
   devices/keys/approvals as blockers where acceptance requires them; never waive
   the gate.

## Module and file map

| Ownership | Where it lives |
| --- | --- |
| Pure financial types/policies/ports | `domain/src/main/kotlin/com/software/sello/domain/{model,policy,port}/`; tests in `domain/src/test/kotlin/` |
| Room/transactions/codecs | `data/src/main/java/com/software/sello/data/{local,mapper,repository}/` and `data/src/main/kotlin/.../data/preferences/`; host tests in `data/src/test/`, real Room in `data/src/androidTest/`; exported schemas in `data/schemas/`. Backup codecs (`backup`) do not exist yet |
| Reusable UI | `design-system/src/main/java/com/software/sello/designsystem/{component,icon,theme}/`; previews and host/device tests alongside that module |
| App wiring/platform | `app/src/main/java/com/software/sello/{composition,navigation,platform,presentation}/`; composition is the only app package importing data implementations |
| Feature UI | `app/src/main/java/com/software/sello/feature/{category,expense,income,recibo,resumen,settings,startup}/`; each owns State/Action/Event/ViewModel/ScreenRoot/Screen as needed. A `backup` feature does not exist yet |
| Sandbox | `app/src/debug/java/com/software/sello/devtools/`, `app/src/debug/res/` and debug-only bindings; release has no reverse dependency |
| Catalog | `catalog/src/main/java/com/software/sello/catalog/{components,foundations}/`; synthetic render examples only, no domain/data/app dependency |
| Decisions/runbooks | `docs/decisions/`, `docs/testing/`; `docs/release/` is created by its owning ticket |

Port/type names in tickets are contract suggestions. Agree signatures at the owning
boundary, document them in its tests, and reuse them downstream. Do not create a
second “almost equivalent” repository or calculation because a name differs.

## Tools and host verification

Tools and the environment are in [setup](../../development/setup.md). Run commands
from the repository root. Use `rg` for source searches. Android Studio is useful for
previews/AVDs; it is not a substitute for command evidence.

Focused **host** examples:

```bash
./gradlew :domain:test --tests '*MoneyPolicyTest'
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

Use the API 30 emulator from [setup](../../development/setup.md#device-smoke) or an
explicitly selected device. If
T3 device tools are available, list/open a suitable device first; otherwise use
Android Studio's AVD manager and `adb devices`. Set `ANDROID_SERIAL` when multiple
devices exist. Device permissions follow [AGENTS.md](../../../AGENTS.md#tests-and-verification).
Release QA uses an isolated synthetic test installation; sandbox scenarios use debug only.

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
  ID restores.
- **Cold start/force-stop:** test persistence/recovery separately. Force-stop or
  clearing app data is not equivalent to saved-task process restoration.
- **Room reopen:** close/reopen the real database in tests, preserving the file;
  in-memory fakes cannot prove durable receipts/migration/rollback.
- **Visual evidence:** record build, API, window size, theme/font scale and scenario.
  A screenshot confirms the shown state, not all financial/device acceptance.

### Device test notes

- Wait for observable UI/data conditions with the existing `Waits.kt` helpers;
  asynchronous updates need condition-based waits, not immediate reads or sleeps.
- After typing, use the `press()` semantics helper and hide the keyboard during
  cleanup so it cannot interfere with the next test.
- Tests in one process share database and month state. Reset both using
  `TestData.reset()` and `TestCommands.showCurrentMonth()` where applicable.
  Seed histories through `TestCommands` so normal observers see the writes.
- For a test window larger than the display, check node existence and bounds;
  off-screen content need not satisfy a visibility assertion.
- Device runs cover Android API availability that host-JDK tests cannot prove.
  System dialogs can steal focus and Back presses; inspect focus before diagnosing
  an app failure.
- For saved-task restoration, return with the same launcher intent or through
  Recents. A different launch can create a second task and test a different journey.

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

See [ADR 0001](../../decisions/0001-mvp-contract.md) for policy and the
[example corpus](../../testing/mvp-financial-examples.md) for exact examples. Table
values are independent assertions, not expected results generated by calling the
production calculator. Never turn a pending example into hidden product policy.
The rules approved in [ADR 0012](../../decisions/0012-budget-guidance-contract.md)
need their own independent examples before the D03 ones they replace are retired.

## Delivery record

Follow [QUALITY_FLOW.md](QUALITY_FLOW.md) and the [quality runner](#quality-runner)
section below for the mandatory run, snapshot freshness, retention and completion
validation.

Add `### Delivery evidence` to the canonical ticket after verification. Required fields:

```text
- **Revision:** actual HEAD plus dirty files tested, or exact integrated commit
- **Requirement mapping:** acceptance item → implementation and independent test
- **Red / Green:** exact commands, behavioral failure, passing rerun; docs-only rationale if applicable
- **Gate results:** actual assigned gate command, counts, reports, warnings/failures
- **Quality run:** run ID, ticket/profile, tested snapshot, outcome and retained artifact
- **Device / Artifact:** device/API/config/build and executed journey, or justified inapplicability
- **Review:** self-review, and the pull request whose merge is the reviewer's approval
```

This is a template, not completed evidence. Do not mark Done while device or signing
acceptance remains missing. Approval is given by merging the ticket's pull request,
which already proposes Done
([quality flow](QUALITY_FLOW.md#done-is-proposed-merging-approves)). Regenerate
board/CSV after ticket/shared-workflow edits, then check the generated output and board unit tests. Do not commit/publish
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
3. For the final PR, propose Done with complete evidence; partial PRs keep the
   ticket In Progress and explain what remains. Regenerate the board, run
   `board.py --check`, commit the retained report with the change and open the PR.
   Review is for work waiting on something outside the merge.
4. CI asks `verify-ticket --candidate-gate <base>` which gate the merge commit needs
   (G2, or G1 for a candidate that advances no ticket and changes only documents),
   runs `verify-ticket --merge-candidate <base> --gate <that>` on the merge commit
   and then `board.py --check --base <base>`. After the merge, the push to `main`
   runs `verify-ticket --landed <previous main>`, the host gate, and the same
   evidence check; `main` gets the device gate nightly. Reports are the
   `quality-reports` artifact of each workflow run, kept 90 days:
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
- Formatting: https://pinterest.github.io/ktlint/latest/
- Shrinking/signing: https://developer.android.com/build/shrink-code
  and https://developer.android.com/studio/publish/app-signing
