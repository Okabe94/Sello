# SELLO-E11 — First-slice bug stabilization

- **Status:** In Progress
- **Increment:** MVP
- **Goal:** Address the collected first-slice correctness, availability, accessibility and measured scaling findings without weakening financial contracts.
- **Exit:** All adopted children have meaningful red→green coverage, their own fresh retained G2 evidence and designated review; deferred or rejected proposals require an explicit owner decision rather than silent acceptance changes.

Registered 2026-10-11 at the owner's request to consolidate the bug tickets into
their own epic. This is planning only: no fix, product decision, device acceptance
or independent approval is claimed. Existing epics, tickets and release obligations
are unchanged. Sources are the [revised UX inventory](../../bugs/reports/bugs_revision.md),
[technical assessment](../../technical/ASSESSMENT-2026-10-11.md) and
[development handoff](../../../development/HANDOFF.md). See the
[archive and evidence guide](../../bugs/README.md) for provenance and privacy.

## Severity order and source mapping

P1 precedes P2. Within each priority, integrity/availability and demonstrated user
exposure precede conditional scaling risks and polish; ordering is a qualitative
triage judgment, not a numeric security score. Technical confidence describes an
issue given its trigger, not user exposure or severity. No P0 incident is established.
Implement sequentially; required approvals and prerequisites can change the next
eligible ticket without changing the severity inventory. Stable canonical IDs use
the existing SELLO-### runner grammar; original draft IDs remain aliases below.

| Order | Canonical ticket | Source alias | Priority | Description / severity rationale |
| --- | --- | --- | --- | --- |
| 1 | SELLO-045 | SELLO-TECH-BUG-001 | P1 | High availability: persistent startup failure blocks every financial journey. |
| 2 | SELLO-046 | SELLO-BUG-005 | P1 | High input correctness: reproduced movement changes intended digits; accidental submission remains a risk, not a demonstrated commit. |
| 3 | SELLO-047 | SELLO-TECH-BUG-002 | P1 | High integrity at the command boundary: accepted dates cannot round-trip; no ordinary UI entry path was identified. |
| 4 | SELLO-048 | SELLO-TECH-BUG-005 | P1 | High responsiveness risk: full-month computation can reach the UI thread; no jank or ANR has been measured. |
| 5 | SELLO-049 | SELLO-TECH-BUG-007 | P2 | Medium presentation consistency: retained old rows can accompany a new header without refreshing identity; race remains unmeasured. |
| 6 | SELLO-050 | SELLO-BUG-007 | P2 | Medium accessibility correctness: a historical selected month is announced as current. |
| 7 | SELLO-051 | SELLO-BUG-004 | P2 | Medium financial-entry readability: ungrouped limits make magnitude harder to verify; stored money was correct. |
| 8 | SELLO-052 | HANDOFF-LAYOUT | P2 | Medium adaptive usability: short narrow windows split the expense form despite insufficient horizontal space. |
| 9 | SELLO-053 | HANDOFF-MOTION | P2 | Medium accessibility: a running theme retains the old system reduced-motion preference. |
| 10 | SELLO-054 | SELLO-TECH-BUG-003 | P2 | Medium boundary robustness: exhausted counters escape typed rejection; natural exhaustion is extraordinarily unlikely. |
| 11 | SELLO-055 | SELLO-TECH-BUG-004 | P2 | Medium efficiency: simultaneous monthly observers duplicate work; measured user impact is not established. |
| 12 | SELLO-056 | SELLO-TECH-BUG-006 | P2 | Medium scaling: every loaded history row is eagerly composed; the device threshold is unmeasured. |
| 13 | SELLO-057 | SELLO-BUG-001 | P2 | Lower user impact within P2: the detail header briefly loses the selected identity while loading. |
| 14 | SELLO-058 | SELLO-BUG-003 | P2 | Lower user impact within P2: owner-requested opt-in default, not a violation of the previously delivered unlimited default. |
| 15 | SELLO-059 | SELLO-BUG-006 | P2 | Lower user impact within P2: requested quick-return affordance; existing manual month selection works. |
| 16 | SELLO-060 | SELLO-BUG-002 | P2 | Lower user impact within P2: inconsistent navigation communication; broad flashing claim only partly confirmed. |

## Shared execution boundaries

- All children remain Backlog. Every child depends on SELLO-004 and requires G2;
  registration does not constitute readiness or completion. Follow the execution
  guide and QUALITY_FLOW.md, using the canonical ID with --retain after a fix.
- SELLO-018 is now recorded Done; archived UX text describing it as unfinished
  reflects the earlier dirty-tree baseline, not this registration's status.
- Owner decisions still required: finite-empty default and unsaved-limit draft
  preservation (SELLO-058), financial-submit separation (SELLO-046), current-month
  shortcut placement (SELLO-059), and agreed performance workloads/targets.
- SELLO-052 owns the shared short/narrow width policy; SELLO-053 owns live motion
  preference observation. Coordinate them before navigation-motion acceptance in
  SELLO-060. Category-editor and snapshot/history siblings have overlapping write
  scopes: deliver sequentially rather than authorizing parallel edits.
- Existing recovery/privacy/release work remains in SELLO-028–036. Do not duplicate
  those tickets, turn unmeasured watchlist concerns into fixes, or implement future
  edit/delete/undo solely to test a stabilization ticket.
- No schema, dependency, permission, financial meaning or toolchain change is
  authorized here. Reusable UI changes extend actual catalog scenarios; financial
  behavior changes extend applicable sandbox paths once delivered.

## SELLO-045 — Reach a safe recovery screen when storage startup fails

- **Type:** Task
- **Source classification:** Bug
- **Priority:** P1
- **Status:** Done
- **Confidence:** 99%, conditional on profile establishment returning a failure.
- **Depends on:** SELLO-004, SELLO-011, SELLO-015, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-001
- **Source report:** [SELLO-TECH-BUG-001](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** High availability: persistent startup failure blocks every financial journey.

### Outcome
A damaged or unavailable financial store preserves its files and produces an
actionable startup failure, rather than preventing the person from reaching any UI.

### What is happening and how
`SelloApplication.onCreate` calls `openStorage` before creating the application graph.
`openStorage` waits in `runBlocking` for profile establishment, closes storage on a
typed failure and throws `FinancialStorageUnavailable`. No application-level failure
state catches that exception before startup exits. Database work uses an I/O context,
but the main startup thread still waits for its result.

Evidence: `app/src/main/java/com/software/sello/composition/SelloApplication.kt:16`;
`app/src/main/java/com/software/sello/composition/StorageModule.kt:38`.

### Future consequences
An unreadable profile, damaged database or persistent open error leaves the user in
a repeatable launch failure without explanation or retry/recovery access. Slow disk
work also extends main-thread startup blocking; no ANR was measured here. Failing
closed protects truth, but it is not a complete recovery experience.

### Deliverables
Introduce explicit initializing/ready/failed startup presentation. Establish storage
asynchronously under an owned scope; initialize the financial graph only after the
persisted profile is available. Show safe retry and the approved recovery guidance
on failure. Keep required collaborators and composition ownership; do not introduce
global service lookup or default financial services to make the error screen work.

### Acceptance criteria
- Failure reaches a visible, accessible explanation without terminating startup.
- Retry can recover a transient fault; repeated retries do not open competing stores.
- No empty database, device-zone substitute, destructive fallback or mutation occurs.
- Financial destinations remain unavailable until the actual profile is ready.
- Rotation/backgrounding do not start duplicate initialization; failed files survive.

### Tests
First reproduce failed initialization and assert a visible failure instead of an
uncaught startup exception. Use controlled initialization fakes for lifecycle/retry
tests and real database files for corrupt/newer-schema preservation. Execute launch,
retry, normal save/read and restoration journeys on API 30 and the affected supported
API. Run the approved G2 mapping; assembly alone does not prove startup handling.

### Working checkpoint
The current create→receipt→Recibo journey remains usable. Own app composition/startup
presentation; reuse storage ports and coordinate recovery actions with their existing
owner. No schema, permission, backup policy or signing change is authorized by this fix.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Reproduce failed startup with a visible-error regression.
2. Define initializing/ready/failed lifecycle and safe retry with the recovery owner.
3. Initialize storage asynchronously under an owned scope, then build the ready financial graph.
4. Prove file preservation, retry serialization and ordinary create/read restoration on device.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-045 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

### Execution progress
2026-10-11: started on branch `sello-045-startup-failure-recovery`. The claim was
verified first on the API 30 emulator: with `sello.db` replaced by text the debug
app died in `SelloApplication.onCreate` with `FinancialStorageUnavailable`, on
every launch, and the file was left byte for byte as it was.

Owner decisions taken before the fix, recorded in
[ADR 0008](../../../decisions/0008-unreadable-storage-start-over.md):
- **Startup stays synchronous.** The Deliverables ask for asynchronous
  initialization with an initializing state. The owner chose the smaller fix: the
  failure becomes a state and a screen, and the open is not moved off the main
  thread, because that wait is unmeasured. The acceptance criteria are unchanged and
  are met without it; the "initializing" presentation is not delivered.
- **Empezar de cero was added.** The owner did not want a person left on a screen
  whose only button may never work. It sets the unreadable file aside (kept as
  `sello-unreadable.db`, not deleted) after a warning and the typed word BORRAR.
- **Restore from a backup on this screen** is left to SELLO-030, which now says so;
  SELLO-031 and SELLO-032 carry the matching notes about the kept copy.

Delivered. `:data`: `setAsideFinancialStorage`. `:app`: `Startup` and
`StartupState` in composition, `openStorage` returning a typed result instead of
throwing, `feature/startup` (state, view model, screen), and `MainActivity` showing
the app only on data that opened. No dependency, permission or schema changed, and
no design-system component was added or changed, so the catalog is unchanged.

Choices made during the work, for owner review:
- The Spanish wording is mine: "No pudimos abrir tus datos", "Sello no pudo leer lo
  que tienes guardado. No se borró nada.", "Intentar de nuevo", "Empezar de cero",
  and a warning that says the data is kept aside on the device but cannot be
  recovered by Sello yet, with a hint to install the newest version first.
- One message for every cause; the screen does not say "damaged".
- The word is accepted with spaces around it and only in capitals.
- Back closes the warning; with no warning open it leaves the app.
- A link that opens the app while the data cannot be opened is dropped.
- After starting from zero the app opens in a new task, so no old selection or
  draft is restored onto the empty history.

Found on the way: with the keyboard open the word field and its buttons were hidden
behind it. The scaffold's padding already includes the keyboard, so it has to be
applied outside the scroll; adding `imePadding` as well doubled it. Fixed on this
screen and checked by hand. The category editor and the wide expense form apply the
same two paddings inside their scroll and were not touched.

Left for later: restore from this screen and recovery of the kept copy (SELLO-030),
erasing it on reset (SELLO-031), a typed cause so the message can differ, and
opening storage off the main thread once a slow migration can be measured.

### Delivery evidence
- **Revision:** branch `sello-045-startup-failure-recovery`; tested snapshot is commit `c2a92d0`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** failure reaches a visible, accessible explanation without terminating startup → `StartupTest.dataThatCannotBeOpenedIsAFailedStartNotACrash`, `StartupFailureScreenTest.itSaysTheDataCouldNotBeOpenedAndThatNothingWasDeleted` and `atTwiceTheFontSizeEveryChoiceCanStillBeReached`, and the device journey below. Retry recovers and never opens competing stores → `StartupTest.tryingAgainOpensOnceMoreAndBecomesReadyWhenItWorks`, `tryingAgainThatFailsStaysFailedWithTheLatestReason`, `onceReadyTryingAgainNeverOpensASecondStore`; everything runs on the main thread, so two attempts cannot overlap. No empty database, device-zone substitute, destructive fallback or mutation → `openStorage` closes storage and returns the failure; `ProfileInitializationTest` (existing) proves a damaged and a newer-schema file are refused and left as they were; starting over happens only after the typed word (`StartupFailureViewModelTest.confirmingWithoutTheWordMovesNothing`, `cancellingClosesTheWarningAndForgetsWhatWasTyped`) and keeps the file (`SetAsideTest`, `UnreadableFileSetAsideTest`); readable data is never set aside (`StartupTest.dataThatOpenedIsNeverSetAside`). Financial destinations unavailable until the profile is ready → `MainActivity` composes the app only for `StartupState.Ready`, and the graph is started only inside a successful open. Rotation/backgrounding do not start duplicate initialization; failed files survive → the `Startup` is held by the application, not the activity, and opens again only on request; `anOpenWarningAndWhatWasTypedSurviveRecreationWithoutConfirmingAnything`; the file's checksum was the same after two launches, a reinstall and a failed retry.
- **Red / Green:** red first. `./gradlew :data:testDebugUnitTest --tests '*SetAsideTest'` against a function that did nothing: 3 of 4 failed. `./gradlew :app:testDebugUnitTest --tests '*StartupTest' --tests '*StartupFailureViewModelTest'` against a `Startup` that still threw on a failed open, as the application did, and a view model that ignored actions: 14 of 18 failed, the first with the same `IllegalStateException` at construction that crashed the app. `StartupFailureScreenTest` against an empty screen on the emulator: 8 of 8 failed. All pass after the implementation. `UnreadableFileSetAsideTest` was written after `setAside` existed and passed on its first run; it was not checked by mutation. The keyboard fix has no automated test; it was found and checked by hand.
- **Gate results:** local `./scripts/verify-ticket SELLO-045 --gate G2 --retain` passed on the tested commit: 331 host tests (196 app, 93 domain, 31 design-system, 8 data, 3 catalog), 270 device tests on the API 30 emulator (107 app, 97 data, 56 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 29 warnings, the same number as before this ticket. No dependency, lock file, permission or schema changed.
- **Quality run:** run 20261011T194501Z-beab9d55; SELLO-045 G2 passed; HEAD c2a92d0, inputs sha256 0ad48cd58130; report docs/planning/mvp/quality-reports/SELLO-045.json sha256 0a1c56104fef98f09240eb95a759fd54d42540e98b56182d0df1d13fca807999
- **Device / Artifact:** the debug app on the API 30 emulator (1080×2340, Cobalto light, font scale 1.0), 2026-10-11, on a build of the working tree before the commit, with a synthetic installation. Before the fix: `sello.db` replaced by text, launch → `FATAL EXCEPTION ... FinancialStorageUnavailable ... SQLiteDatabaseCorruptException`, no process, twice. After: the same file → [the screen](../../../testing/evidence/SELLO-045/01-could-not-open.png), process alive, no fatal exception; "Intentar de nuevo" → "Todavía no se pueden abrir tus datos." and the file's checksum unchanged; "Empezar de cero" → [the warning](../../../testing/evidence/SELLO-045/02-start-over-warning.png), confirmation disabled with `borrar` and with nothing typed, Back and Cancelar close it with the files unchanged; with `BORRAR` → [an empty Recibo](../../../testing/evidence/SELLO-045/03-started-from-zero.png) in a new task, `sello-unreadable.db` present with the damaged file's checksum beside a new `sello.db`. Separately: launch on a damaged file, put a good file back, "Intentar de nuevo" → Recibo without restarting. The ordinary create → receipt → Recibo and restart journeys ran in the gate (`WalkingSliceJourneyTest` and the other 106 app device tests). Not executed: a second API level (only the API 30 emulator was connected), a newer-schema file through the installed app's own screen (covered on real files in `:data` only), TalkBack, and rotation on the failure screen by hand.
- **Review:** executor self-review of the diff, captures, reports and logs. The merge of this ticket's pull request is the project owner's acceptance, including the listed choices and the Spanish wording; the pull request and its checks are that record. No independent technical review.

## SELLO-046 — Stabilize expense keypad geometry and separate financial submission

- **Type:** Task
- **Source classification:** Task
- **Classification:** Bug + related submission-separation UX risk; original reports 5 and 7
- **Priority:** P1
- **Status:** Done
- **Depends on:** SELLO-017, SELLO-008, SELLO-009, SELLO-004
- **Gate:** G2
- **Decision needed:** approve increased keypad-to-submit separation using existing Sello spacing tokens
- **Source alias:** SELLO-BUG-005
- **Source report:** [SELLO-BUG-005](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** High input correctness: reproduced movement changes intended digits; accidental submission remains a risk, not a demonstrated commit.

### Outcome
Typing changes the amount, not the touch coordinates of the keypad; financial submission is clearly separated from digit entry.

### Deliverables
Stable pinned-control layout, removal of the redundant initial Escribe un monto helper as requested, retained accessible validation/errors, reviewed submit separation, and geometry/behavior regression tests. Group these two reports because both fixes own ExpenseEntryScreen's Controls/Stacked layout.

### Reproduction and evidence
Open empty Anotar with Exploracion selected. Key 1 target is `[33,1487][359,1619]`. Enter 1: it moves to `[33,1552][359,1684]`, **65 px ≈23.6dp downward**, while submit stays `[33,2153][1047,2307]`. The Escribe un monto line disappears. See 09/10 screenshots/XML and `10-keypad.mp4`.

Controlled miskey reproduction: tap 1 at (195,1550), then (190,1805), a point inside the **original 7** target `[33,1785][359,1917]`. After relayout it is inside **4** `[33,1701][359,1833]`; the draft becomes **14 rather than intended 17** (`16-old-key-position.png/.xml`). This draft was not submitted.

In the valid state the bottom keypad target ends at y=2131 and submit begins at y=2153: **22 px =8dp separation**. Proximity is measured; an accidental persisted submission was not reproduced. Do not describe this as proven financial duplication or storage corruption.

### Acceptance criteria
- At a fixed viewport/font/IME configuration, digit targets do not move when amount transitions empty → valid → empty or when helper/error text changes.
- Remove the redundant initial Escribe un monto text; keep the empty amount's accessible description and actionable validation/read/unknown-outcome feedback.
- Keypad and submit hit regions do not overlap and have an owner-approved increased gap using existing tokens. Recommended minimum: `SelloTheme.spacing.lg` (16dp in the current `SelloShapes.kt`), doubling the observed 8dp gap; verify adaptive layouts before finalizing it.
- The old-key-coordinate reproduction yields intended 17 after correction, without submitting; tapping any digit never invokes Submit.
- Submit remains accessible/reachable with large fonts, compact/short-landscape layouts and IME changes; no extra confirmation step or altered Enter behavior is invented.
- Existing busy, duplicate-submit, operation-ID and receipt-recovery behavior remains intact.

### Tests
Connected bounds assertions before/after first digit, clear and validation transitions; coordinate-based regression for intended 17; digit-versus-submit isolation; virtual-time busy/double-submit tests; large-font/landscape/IME layout cases. Control time/reads instead of arbitrary sleeps.

### Working checkpoint
Stable keypad entry and deliberately separated submission; a confirmed save still produces exactly one matching receipt/expense.

### Context and starting points
ExpenseEntryScreen Stacked pins a content-sized Controls column to the bottom. Controls conditionally inserts ENTRY_MISSING_TAG between AmountKeypad and SelloButton; removal changes total height. Read ExpenseEntryState/ViewModel and design-system AmountKeypad/SelloButton/token contracts. Do not change financial command services to solve layout.

### Implementation plan
1. Add meaningful red geometry/miskey tests demonstrating the observed defect.
2. Remove the initial redundant helper and make control geometry independent of dynamic feedback; keep important feedback in a stable, accessible area.
3. Apply approved separation without causing clipping, unreachable submit or gesture-only interactions.
4. Extend applicable catalog states and rerun create/receipt/recovery journeys.

### Concrete cases and pitfalls
Increasing the gap alone does not fix the 65px movement. Conversely, merely removing one helper may leave movement when another conditional error appears. Fix the sizing cause across feedback states. Never add a successful no-op save or relax validation to make the form appear stable.

### Verification recipe
Use `./scripts/verify-ticket SELLO-046 --retain` for the final G2 gate.
Focused entry state tests and connected geometry/intent assertions first; run the original coordinate sequence and empty/valid/clear/error cases at agreed configurations; reconcile one real save to stored expense/receipt; G2 with retained report and before/after captures.

### Execution progress
2026-10-11: started on branch `sello-046-keypad-geometry-submit-separation` after
SELLO-045 merged. The claim was verified first on the API 30 emulator: after the
first digit every key dropped 65 px, and 1 followed by a tap on the upper part of
where 7 had been gave 14. The keypad ended 22 px (8dp) above the button. Nothing was
saved.

Owner decisions taken before the fix (2026-10-11): work on it; **16dp**
(`spacing.lg`) between the keypad and the button, which is the decision this ticket
needed; and what the form says goes **above the keypad** instead of in a reserved
area between the keypad and the button.

Delivered, in `ExpenseEntryScreen` only. What is missing or went wrong is laid out
above the keypad and outside its scroll; the keypad and the button are one group,
16dp apart, held from the bottom in both layouts. "Escribe un monto." is gone, as
the acceptance asks; the slip still says "Sin monto" and the button stays off. No
view model, command, design-system component or string other than that one changed,
so the catalog is unchanged.

Choices made during the work, for owner review:
- Sideways or wide, the keypad and button now sit at the bottom of the right column
  and messages at its top. Before, they started at the top and a message pushed the
  button down.
- In a window too short for a message and the keys together (360dp high, sideways),
  the top row of keys is partly covered while the message shows; the keys do not move.
- At twice the font size "Comprobar si se guardó" takes two lines and lifts the
  keys by the extra line. The keys are off in that state.
- A failed save's message is now always in view; before it could need scrolling.

Not done: no confirmation step and no change to what Enter does, as the acceptance
says. The system-keyboard case (keypad steps aside for the note) was not changed.

### Delivery evidence
- **Revision:** branch `sello-046-keypad-geometry-submit-separation`; tested snapshot is commit `72c08f1`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** digit targets do not move through empty → valid → empty or when helper/error text changes → `ExpenseEntryGeometryTest.onAPhoneNoKeyAndNoButtonMovesWhateverTheFormSays`, `sidewaysNoKeyAndNoButtonMovesWhateverTheFormSays` and `atTwiceTheFontSizeNoKeyAndNoButtonMovesWhateverTheFormSays`, which compare where keys 1, 7, 0, 000 and the button are laid out across an amount typed, no category, a note to correct, a failed save, a category gone, an unknown outcome and empty again. Initial text removed, accessible description and feedback kept → `ExpenseEntryScreenTest.anEmptyFormSaysTheAmountIsMissingOnTheSlipOnlyAndCannotBeSubmitted` ("Sin monto" present, "Escribe un monto." absent, button off) and the unchanged error and unknown-outcome tests. No overlap and the approved 16dp gap → `theButtonIsSixteenDpBelowTheKeypadInEveryLayout`, `sidewaysTheButtonIsSixteenDpBelowTheKeypadToo`. The old-coordinate reproduction yields 17 and no digit submits → `aFingerGoingBackToWhereSevenWasStillTypesSeven`, `noKeyEverRecordsTheExpense` (every key and the keypad's lowest pixels). Submit reachable with large fonts, short landscape and the keyboard → `atTwiceTheFontSizeEveryControlCanStillBeReached` (existing) and the sideways cases above; the keyboard path is unchanged code. Busy, duplicate-submit, operation-ID and receipt recovery intact → no view model or command changed; `ExpenseFormViewModelTest`, `ExpenseEntryJourneyTest` and `WalkingSliceJourneyTest` pass unchanged in the gate.
- **Red / Green:** red first, on the emulator against the unchanged screen: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.software.sello.feature.expense.ExpenseEntryGeometryTest,com.software.sello.feature.expense.ExpenseEntryScreenTest` failed 7 of 20: the returning finger typed `Digit(4)` instead of `Digit(7)`; keys moved on a phone and at twice the font size; sideways the button moved; the gap was 8dp on a phone and 12dp sideways; and "Escribe un monto." was present. `noKeyEverRecordsTheExpense` passed before the change and is a guard, not a red test. After the change all 20 pass. Two expectations of mine were corrected on the way: positions are now compared as laid out, not as clipped by a scroll, and the two-line "Comprobar si se guardó" case at twice the font size is excluded because the keys are off then. One older test stopped scrolling to a message that is now always in view.
- **Gate results:** local `./scripts/verify-ticket SELLO-046 --gate G2 --retain` passed on the tested commit: 331 host tests (196 app, 93 domain, 31 design-system, 8 data, 3 catalog), 277 device tests on the API 30 emulator (114 app, 97 data, 56 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 29 warnings, the same number as before. No dependency, lock file, permission or schema changed.
- **Quality run:** run 20261011T201801Z-54dac6f7; SELLO-046 G2 passed; HEAD 72c08f1, inputs sha256 e4d0e80776cd; report docs/planning/mvp/quality-reports/SELLO-046.json sha256 2121d6aa9c66b9b740e14832a0a340475e56c7546bbe9a065749513de44c4a84
- **Device / Artifact:** the debug app on the API 30 emulator (1080×2340, density 440, Cobalto light, font scale 1.0), 2026-10-11, synthetic category "Exploracion". Before: key 1's label at y=1384 [empty](../../../testing/evidence/SELLO-046/01-before-empty.png) and y=1449 [after one digit](../../../testing/evidence/SELLO-046/02-before-keys-moved.png); 1 then a tap at (196,1690) gave 14; keypad to button 22 px. After, on a build of the working tree before the commit: key 1 at y=1427 and key 7 at y=1725 [empty](../../../testing/evidence/SELLO-046/03-after-empty.png), after one digit and after clearing; 1 then a tap on the upper part of 7 gave [17](../../../testing/evidence/SELLO-046/04-after-same-place.png); keypad to button 44 px (16dp). No expense was saved in either run. Not executed by hand: sideways and wide windows, twice the font size and the system keyboard (covered by the tests above, except the keyboard), TalkBack, and a real save after the change (covered by the journey tests in the gate).
- **Review:** executor self-review of the diff, captures, reports and logs. The merge of this ticket's pull request is the project owner's acceptance, including the listed choices; the pull request and its checks are that record. No independent technical review.

## SELLO-047 — Reject effective dates that the storage codec cannot read

- **Type:** Task
- **Source classification:** Bug
- **Priority:** P1
- **Status:** Backlog
- **Confidence:** 99%, conditional on a negative-year `LocalDate` reaching a command.
- **Depends on:** SELLO-004, SELLO-010, SELLO-012, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-002
- **Source report:** [SELLO-TECH-BUG-002](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** High integrity at the command boundary: accepted dates cannot round-trip; no ordinary UI entry path was identified.

### Outcome
Every accepted command date/period round-trips through the strict persisted format.

### What is happening and how
`EffectiveDates.forManualEntry` rejects only dates after today. A value such as
`LocalDate.of(-1, 1, 1)` is accepted. The expense writer persists `date.toString()`
(`-0001-01-01`), while `EffectiveDates.parseDate` accepts only four unsigned year
digits. A by-ID decode therefore rejects the row the command was allowed to write.
ISO-text month queries can also exclude that date instead of surfacing it.

Evidence: `domain/src/main/kotlin/com/software/sello/domain/policy/EffectiveDates.kt:20`;
`data/src/main/java/com/software/sello/data/repository/RoomExpenseCommands.kt:87`;
`data/src/main/java/com/software/sello/data/mapper/RecordMapper.kt:34`.

The executed pure-domain probe returned `Success(value=-0001-01-01)` for manual
validation and `Failure(error=Malformed(original=-0001-01-01))` for storage parsing.
No negative-year database write was executed. No ordinary UI/deep-link path producing
such a date was identified; this is a command-boundary defect, not a claimed exploit.

### Future consequences
Future adapters, synthetic clocks or incorrectly validated import callers can commit
an expense with a receipt that cannot be read back normally. It may disappear from
period queries and make future portable validation fail. The analogous month grammar
must be reviewed at month-limit/selection boundaries, not merely fixed in a picker.

### Deliverables
Validate canonical date/month representability at domain and command boundaries,
using the actual approved storage grammar. Return a typed invalid-period rejection
before writing. Do not invent a 2000–2099 restriction, repair the date, or relax the
decoder to accept a different portable format. Preserve historically valid dates.

### Acceptance criteria
- Every accepted effective date/month parses back to the identical value.
- Negative years and other noncanonical period representations are rejected before writes.
- A rejected command leaves expense, receipt, category configuration and revision unchanged.
- Valid older dates, leap days and the current financial-day cutoff keep existing behavior.
- Invalid format and future date remain distinguishable, with accurate UI error mapping.

### Tests
Add a red domain round-trip case using `-0001-01-01`, then tests at accepted grammar
boundaries. Add real-Room expense/month-limit command rejection tests and an unchanged
rows/receipts/revision assertion. Retain existing strict-mapper and future-date tests.
Run focused domain and Room suites, then the approved G2 mapping.

### Working checkpoint
Own effective-period policies and affected command rejections; inspect existing
callers and presentation mapping before changing signatures. No schema change is
expected. If one is proposed, require separate portable-format/migration review.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Add the negative-year round-trip red case and audit date/month command callers.
2. Reject nonrepresentable canonical periods before mutation with typed errors.
3. Verify grammar edges, financial cutoff and real-Room unchanged rows/receipts/revision.
4. Review portable-format impact and retain focused plus G2 evidence.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-047 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

## SELLO-048 — Move expensive monthly computation off the main thread

- **Type:** Task
- **Source classification:** Performance risk
- **Priority:** P1
- **Status:** Backlog
- **Confidence:** 90%, conditional on a sufficiently large month refreshing on a constrained device.
- **Depends on:** SELLO-004, SELLO-005, SELLO-014, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-005
- **Source report:** [SELLO-TECH-BUG-005](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** High responsiveness risk: full-month computation can reach the UI thread; no jank or ANR has been measured.

### Outcome
Exact financial snapshot processing does not occupy the UI thread as history grows.

### What is happening and how
Room runs transactional reads on its configured query context. After that transaction
returns, `readAsOf` directly calls `MonthlyBudgetPolicy.snapshot` on the caller's
context. Recibo collects through `viewModelScope` with no computation-dispatcher shift.
The policy filters/groups expenses and computes widened totals; render mapping then
sorts/formats categories. `MonthReads.pages` bounds individual SQL calls but accumulates
all rows, so pagination does not bound the total in-memory workload.

Evidence: `data/src/main/java/com/software/sello/data/repository/RoomMonthlySnapshots.kt:37`;
`data/src/main/java/com/software/sello/data/repository/MonthReads.kt:27`;
`domain/src/main/kotlin/com/software/sello/domain/policy/MonthlyBudgetPolicy.kt:100`;
`app/src/main/java/com/software/sello/feature/recibo/ReciboViewModel.kt:31`.

### Future consequences
Busy months can delay visible updates or interrupt interaction during save/rollover/
month changes. Repeated pipelines amplify the risk. Main-thread reachability is visible
in source; jank, memory exhaustion and ANRs were not measured and are not claimed.

### Deliverables
Inject the existing computation dispatcher into the owned pipeline and explicitly
place substantial domain processing off main. Keep domain platform-neutral and exact.
Measure allocations as well as execution time; adopt streaming exact aggregation only
if measurements justify it and consistent-revision semantics can be preserved. Never
truncate source history or substitute floating-point totals to make processing faster.

### Acceptance criteria
- A controlled-dispatcher test proves financial computation does not run on UI context.
- Existing worked financial examples remain unchanged, including overflow failures.
- Cancellation/month switching prevents obsolete results from replacing newer state.
- Related rows retain one consistent revision; computation uses captured as-of data.
- Device measurements meet targets approved before the run, not targets invented here.

### Tests
Start with a red execution-context assertion; retain financial policy/snapshot tests.
Use the reviewed SELLO-035 workload/targets, including its proposed 20-category,
10,000-expense profile only once approved. Seed synthetic history through real commands,
record repeated refresh/interaction measurements on the agreed device, and run approved
G2. Passing host tests or a smooth small debug demo is not performance proof.

### Working checkpoint
Own snapshot execution placement and minimal composition changes. Coordinate sharing
with SELLO-055; no extra dispatcher globals, dependencies or benchmark framework
without explicit approval. Do not redesign domain policy merely to move its execution.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Add a red controlled-dispatcher assertion for snapshot calculation.
2. Move substantial computation onto the injected computation dispatcher without changing domain money rules.
3. Prove captured revision/as-of identity and cancellation on month changes.
4. Agree synthetic workloads/device targets with SELLO-035, measure and run G2.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-048 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

## SELLO-049 — Bound history refresh work and identify retained rows as refreshing

- **Type:** Task
- **Source classification:** Performance / presentation-consistency risk
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 80%, conditional on revisions changing after a deep history window is loaded.
- **Depends on:** SELLO-004, SELLO-014, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-007
- **Source report:** [SELLO-TECH-BUG-007](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** Medium presentation consistency: retained old rows can accompany a new header without refreshing identity; race remains unmeasured.

### Outcome
Snapshot updates retain useful list continuity without unmarked old/new data mixing
or an unexplained full-window reread cost on every invalidation.

### What is happening and how
Each snapshot emission restarts `list(id, snapshot)`. That flow starts with an empty
accumulator and reads up to `pagesWanted`, so ten previously requested pages can be
read again after a single revision change. The interim Ready state combines the new
category header with `kept` rows identified only by month/category, not revision or
as-of. A previously successful retained list does not acquire a dedicated refreshing
marker in that initial state. History reads themselves carry no snapshot revision.

Evidence: `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt:99`
and `:119`; `domain/src/main/kotlin/com/software/sello/domain/port/ExpenseReads.kt`;
`data/src/main/java/com/software/sello/data/repository/RoomExpenseReads.kt:31`.

### Future consequences
Save/edit/delete bursts can repeatedly rebuild a large loaded window and prolong the
interval in which the header is current but listed rows are from earlier data. Slow
or failed reads make the distinction more important. This is a predicted refresh/race
risk from the code, not a reproduced permanent accounting mismatch. The existing
rebuild strategy deliberately favors simplicity and has useful failure/retry tests.

### Deliverables
Track the identity of the displayed list and publish explicit refreshing/stale state
when retaining older rows. Define revision reconciliation at the read/presentation
boundary; do not calculate totals from pages. Measure reread cost first, then select
a bounded refresh strategy that preserves correctness. If incremental refresh is not
justified, retain rebuilding with explicit progress and a reviewed work budget. Do not
skip invalidations merely because a change looks unrelated, or cache across generations.

### Acceptance criteria
- Retained rows are not presented as fully reconciled with a newer header before refresh.
- A delayed read with an intervening save produces coherent eventual results and no duplicates.
- Category/month/generation changes cannot reuse another history's retained rows.
- First/later page failures remain explicit and retry the intended page.
- Work for a deeply loaded window is counted and measured; adopted bounds never truncate totals.
- Later edits/deletes/undo preserve defined scroll and cursor behavior when those commands ship.

### Tests
Use controlled suspended reads, commit a revision between header and page reads, and
assert interim marking plus final rows/figures. Count page requests after deep loading
and rapid revisions; exercise cancellation and same-page retry. Verify actual Room
reconciliation and device accessibility/progress before approved G2. Do not add future
edit/delete implementations as fixtures before their owning ticket ships.

### Working checkpoint
Own detail refresh state and, only if required by the agreed contract, the domain read
port/data adapter. Coordinate with sharing and lazy rendering without one broad UI
rewrite. No schema change or dormant per-category revision infrastructure is assumed.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Suspend a page read across an intervening save and assert the missing refresh identity.
2. Define retained-list revision/generation/as-of identity and visible refreshing semantics.
3. Count rereads; adopt a bounded strategy only after measurement and correctness review.
4. Prove final reconciliation, cancellation, no duplicates and real Room/device progress before G2.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-049 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

## SELLO-050 — Distinguish selected month from current month in accessible labels

- **Type:** Task
- **Source classification:** Task
- **Classification:** Bug discovered during exploration; not in original seven reports
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-015, SELLO-004
- **Gate:** G2
- **Source alias:** SELLO-BUG-007
- **Source report:** [SELLO-BUG-007](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** Medium accessibility correctness: a historical selected month is announced as current.

### Outcome
The month switcher does not tell assistive-technology users that a selected historical month is the current month.

### Deliverables
Accurate resource wording and presentation mapping, with current/historical/rollover semantics regression tests. No financial changes and no dependency on adding SELLO-059's shortcut.

### Reproduction and evidence
Financial date is October 10, 2026. Select September 2026. `13-past-month.xml` contains **Cambiar de mes. Mes actual: Septiembre 2026**, although the visible selected heading is historical. This is confirmed in the accessibility hierarchy; a live TalkBack walkthrough was not executed.

Expected: identify September as the selected month. If current month is also announced, identify October separately and truthfully.

### Acceptance criteria
- Historical selected periods are announced as selected, never as current.
- The current-period case and optional separate current-period wording remain accurate under financial rollover/restoration.
- Spanish resources and exact localized month/year values are used; do not read wall time ad hoc.
- Visual heading, spoken selected period, and shared MonthSession agree; no change to selected-month financial data.

### Tests
Semantics assertions for selected=current and selected<historical current, year boundary, restored historical selection and controlled rollover; device screen-reader verification before acceptance.

### Working checkpoint
Selecting September while current is October exposes an accurate selected-month description and preserves ordinary picker navigation.

### Context and starting points
ShellScreen in `app/src/main/java/com/software/sello/navigation/SelloApp.kt` passes the selected title into `month_picker_open` in `app/src/main/res/values/strings.xml`, whose resource says Mes actual. Existing `ShellState.currentMonth` and selectedMonth already distinguish the concepts. Inspect NavigationStateTest and shell connected tests before editing.

### Implementation plan
1. Add a red semantics assertion for the recorded September/October case.
2. Correct copy/mapping without duplicating month calculations.
3. Cover current/historical, restored and rollover cases.
4. Verify with TalkBack and keyboard focus; retain evidence.

### Concrete cases and pitfalls
Do not fix this by announcing October while the selected data is September without identifying both roles. This is a wording/mapping defect, not a reason to force selection back to current or rewrite financial dates.

### Verification recipe
Use `./scripts/verify-ticket SELLO-050 --retain` for the final G2 gate.
Focused shell/month semantics tests, connected current/past selection plus manual TalkBack walkthrough, then G2 with a fresh retained report. Existing XML evidence establishes the defect but does not replace the future screen-reader acceptance.

## SELLO-051 — Show readable grouped COP category limits without changing input meaning

- **Type:** Task
- **Source classification:** Task
- **Classification:** Amount-entry readability issue; original report 4
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-016, SELLO-008, SELLO-007, SELLO-004
- **Gate:** G2
- **Source alias:** SELLO-BUG-004
- **Source report:** [SELLO-BUG-004](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** Medium financial-entry readability: ungrouped limits make magnitude harder to verify; stored money was correct.

### Outcome
Users can read the magnitude of a category limit while entering it or selecting a suggested value.

### Deliverables
Presentation-only grouped limit display, correct editing/cursor/accessibility behavior, original-input validation coverage, and actual catalog examples if the reusable input primitive changes.

### Reproduction and evidence
In the category form, choose 500.000: field shows `500000` (`05-category-suggestion.png/.xml`). Replace manually with `400000`: field shows `400000` (`06-category-manual.png/.xml`). Saving this draft correctly produced 400.000 in Recibo; the demonstrated defect is readability, not a changed stored amount.

### Acceptance criteria
- Valid 400000 and suggested 500000 are visually readable as 400.000 and 500.000 while editing; expose exact currency-aware accessibility information.
- Grouping remains presentation-only: preserve original input for validation and maintain correct selection, cursor movement, insert/delete, paste and draft restoration.
- Accept approved plain digits/correctly grouped dots; malformed `1e3`, `1.23`, commas, negative signs and overflow are not silently repaired.
- No floating-point money and no new category-limit range policy. Submission persists the exact validated value.
- Disabled/hidden unlimited-mode behavior integrates with the owner-approved SELLO-058 decision, without requiring that decision to fix readability.

### Tests
Raw-to-display mapping, cursor/edit operations around grouping separators, suggestion values, valid/invalid paste, exact saved amount and restored drafts. Test valid category-limit bounds from the actual domain contract, not the expense transaction cap.

### Working checkpoint
Manual and shortcut entry are readable and save the same exact limit as before.

### Context and starting points
CategoryEditorScreen passes raw limitText to FormField; SuggestionPicked stores pesos.toString(). Inspect existing FormField and MoneyFormatter APIs rather than adding a duplicate financial parser. D05/architecture §4 require validation of original input.

### Implementation plan
1. Add red presentation/editing tests with independently specified expected display strings.
2. Introduce presentation grouping through existing input conventions, preserving raw validation and selection mapping.
3. Cover shortcuts, accessibility and restoration; extend actual shared examples if needed.
4. Save a synthetic limit and reconcile its persisted value with Recibo/detail.

### Concrete cases and pitfalls
Do not reformat malformed input into a valid amount, strip arbitrary characters, impose a new cap, or round budget values. Grouped display must not make separator deletion corrupt the raw draft.

### Verification recipe
Use `./scripts/verify-ticket SELLO-051 --retain` for the final G2 gate.
Focused formatter/editor tests, connected typing/paste/suggestion/restoration checks, exact Room-backed creation result, then G2 with fresh retained evidence. Coordinate write order with SELLO-058.

## SELLO-052 — Keep short narrow expense windows usable with one shared width policy

- **Type:** Task
- **Classification:** Handoff defect; source-inspected, not freshly device-reproduced
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-004, SELLO-009, SELLO-017, SELLO-018
- **Gate:** G2
- **Source alias:** HANDOFF-LAYOUT
- **Source report:** [Known loose ends](../../../development/HANDOFF.md#known-loose-ends)
- **Severity rationale:** Medium adaptive usability: short narrow windows split the expense form despite insufficient horizontal space.

### Outcome
A short window does not force two expense-entry columns unless sufficient horizontal space exists.

### Deliverables
A reviewed design-system width/height decision, consistent consuming layouts and
regression examples in the existing catalog; no copied feature-specific breakpoint.

### Acceptance criteria
- A short narrow window keeps all amount, keypad and submit controls reachable.
- A reviewed minimum width governs two-pane layout consistently; Recibo and Anotar agree.
- Valid wide/landscape layouts, insets, Back and large fonts remain usable.
- Resize and restoration retain drafts/operation IDs without duplicate submission.

### Tests
Add a red LayoutRulesTest case for a short narrow window, then boundary cases
for the approved width and height. Connected form/Recibo tests exercise half-screen,
landscape, expanded and font-scale-2.0 windows with observable control reachability.

### Working checkpoint
The existing create→expense→receipt→Recibo flow remains usable; the affected
layout/accessibility setting obeys the shared contract without financial mutation.

### Context and starting points
`WindowLayout.kt` selects twoPane=true whenever height<480dp regardless of width.
`ExpenseEntryScreen.kt` consumes it directly; `SelloApp.kt` separately requires
width>=600dp for detail. Inspect LayoutRulesTest and existing adaptive device tests.
Own shared design-system layout rules and minimal consumer reconciliation.

### Implementation plan
1. Reproduce the short/narrow rule and inspect actual available content width.
2. Obtain the shared width/height presentation decision; do not invent a breakpoint silently.
3. Update the shared rule and reconcile the existing Recibo guard and Anotar consumer.
4. Verify adaptive/draft behavior and catalog examples, then retain G2.

### Concrete cases and pitfalls
Short height alone does not prove a wide device. Do not disable all landscape
layouts, shrink touch targets, hide submission, or fix only one feature with a
duplicate breakpoint. No schema, money or window-management permission changes.

### Verification recipe
Run focused host/Compose red→green tests and affected connected scenarios, including
font scale 2.0, restoration and resize/resume. Extend the actual catalog example.
Run `./scripts/verify-ticket SELLO-052 --retain` for fresh G2 evidence; record
manual accessibility/adaptive checks separately before designated review.

## SELLO-053 — Observe reduced-motion changes while the app is running

- **Type:** Task
- **Classification:** Handoff defect; source-inspected, not freshly device-reproduced
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-004, SELLO-006, SELLO-009, SELLO-015
- **Gate:** G2
- **Source alias:** HANDOFF-MOTION
- **Source report:** [Known loose ends](../../../development/HANDOFF.md#known-loose-ends)
- **Severity rationale:** Medium accessibility: a running theme retains the old system reduced-motion preference.

### Outcome
Turning system animations off is reflected by the live Sello theme without requiring process restart.

### Deliverables
Lifecycle-owned observation of the existing system preference, preserving the
explicit catalog/test override and all stateless component motion contracts.

### Acceptance criteria
- Turning reduced motion on updates the running/resumed theme and uses instant end states.
- Turning it off restores eligible token-based motion without forced navigation.
- Observer registration/disposal is bounded; recomposition does not leak observers.
- Rotation, background/resume and restoration preserve screen/draft/financial state.
- Explicit reducedMotion overrides in tests/catalog remain deterministic.

### Tests
Add a controlled preference-change red test showing the currently remembered
value stays stale. Cover both directions, observer disposal and explicit overrides.
On an isolated device toggle the setting while alive and while backgrounded;
verify affected component/shell end states without arbitrary timing sleeps.

### Working checkpoint
The existing create→expense→receipt→Recibo flow remains usable; the affected
layout/accessibility setting obeys the shared contract without financial mutation.

### Context and starting points
`theme/ReducedMotion.kt` uses remember(resolver) around a one-time read of
Settings.Global.ANIMATOR_DURATION_SCALE. `Theme.kt` passes it to the local used
by actual components. Inspect SelloMotionTest and connected reduced-motion tests.
Own theme preference observation and actual catalog coverage, not financial clocks.

### Implementation plan
1. Add a red observable preference-update test with controlled changes.
2. Define lifecycle and override precedence at the existing theme boundary.
3. Observe/resynchronize the system preference with proper disposal and no globals.
4. Verify live/resumed changes, accessibility and navigation preservation before G2.

### Concrete cases and pitfalls
Do not poll, add a permission, read financial time, or recreate the process to
make the value refresh. A normal recomposition of the remembered resolver does not
prove a reread. Coordinate new navigation motion with SELLO-060 without merging
this accessibility setting defect into broad transition work.

### Verification recipe
Run focused host/Compose red→green tests and affected connected scenarios, including
font scale 2.0, restoration and resize/resume. Extend the actual catalog example.
Run `./scripts/verify-ticket SELLO-053 --retain` for fresh G2 evidence; record
manual accessibility/adaptive checks separately before designated review.

## SELLO-054 — Return a typed rollback outcome when counters overflow

- **Type:** Task
- **Source classification:** Bug
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 99%, conditional on incrementing an exhausted persisted counter.
- **Depends on:** SELLO-004, SELLO-012, SELLO-013, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-003
- **Source report:** [SELLO-TECH-BUG-003](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** Medium boundary robustness: exhausted counters escape typed rejection; natural exhaustion is extraordinarily unlikely.

### Outcome
Checked counter exhaustion does not escape the financial command result protocol.

### What is happening and how
Revision, expense sequence and category version increments use `Math.addExact`.
The mappers admit nonnegative/positive `Long` counters including `Long.MAX_VALUE`.
`runCommand` catches SQLite and illegal-state failures, but not the resulting
`ArithmeticException`. Room rolls back the transaction; the arithmetic exception
escapes instead of becoming the command's typed rejection. An unhandled ViewModel
call can consequently lose normal failure presentation or terminate the process.

Evidence: `data/src/main/java/com/software/sello/data/repository/CommandProtocol.kt:45`
and `:125`; `data/src/main/java/com/software/sello/data/repository/RoomExpenseCommands.kt:114`;
`data/src/main/java/com/software/sello/data/repository/RoomCategoryCommands.kt:141`.

An executed JDK probe confirmed `Math.addExact(Long.MAX_VALUE, 1)` throws
`ArithmeticException`, not `IllegalStateException`. A real-Room exhausted-counter
command was not executed. Natural exhaustion through manual use is not a credible
near-term scenario; synthetic/corrupt/future imported metadata is the exposure.

### Future consequences
Boundary fixtures or admitted extreme metadata can break the otherwise deliberate
Rejected/Committed/OutcomeUnknown contract. This is a robustness failure, not evidence
of wrapped money or a partially committed transaction.

### Deliverables
Handle audited counter exhaustion explicitly, either before mutation or inside the
transaction with a narrow typed rollback mapping. Preserve checked increments and
rollback; never wrap, saturate, reset counters or blanket-catch programming errors.
Classify a known pre-commit arithmetic rollback separately from an uncertain commit.

### Acceptance criteria
- Exhausted revision, sequence and record version return a typed rejection.
- Real Room proves no partial category, expense, receipt or revision mutation.
- A counter at `Long.MAX_VALUE - 1` can advance once; the following change is rejected.
- Cancellation is rethrown and uncertain-commit lookup retains its existing behavior.

### Tests
Seed each counter independently in an isolated real Room fixture, execute the command
and first demonstrate the escaping exception. Assert the corrected typed result and
all stored rows, not only a mocked DAO call. Repeat duplicate/cancellation/commit-failure
contract tests, then the approved G2 mapping.

### Working checkpoint
Own the command protocol and affected command validations. Resolve the precise typed
failure at that boundary; no new money policy, schema or metadata-reset behavior.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Seed each exhausted counter in isolated real Room and reproduce the escaping exception.
2. Agree the narrow typed exhaustion result and preserve checked transaction increments.
3. Prove rollback and the final valid increment independently for revision, sequence and version.
4. Rerun cancellation, replay and unknown-commit recovery before G2.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-054 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

## SELLO-055 — Avoid duplicate monthly snapshot pipelines in simultaneous views

- **Type:** Task
- **Source classification:** Technical debt / efficiency risk
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 95%, conditional on simultaneous consumers of the same month.
- **Depends on:** SELLO-004, SELLO-014, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-004
- **Source report:** [SELLO-TECH-BUG-004](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** Medium efficiency: simultaneous monthly observers duplicate work; measured user impact is not established.

### Outcome
Consumers displaying the same month reuse one bounded, lifecycle-owned snapshot
observation instead of independently rebuilding identical financial results.

### What is happening and how
`RoomMonthlySnapshots.observe` constructs a cold flow. Recibo and detail each invoke
it and each `stateIn` shares only its own invocation. `ReciboDestination` collects
both ViewModels. Every applicable revision/day update can therefore cause two full
month reads/calculations. Active expense entry adds its own observation. Detail also
sorts/formats all categories before finding one category. A singleton repository does
not make those cold-flow invocations share work.

Evidence: `data/src/main/java/com/software/sello/data/repository/RoomMonthlySnapshots.kt:71`;
`app/src/main/java/com/software/sello/feature/recibo/ReciboViewModel.kt:31`;
`app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt:54` and `:99`;
`app/src/main/java/com/software/sello/navigation/SelloApp.kt:156`.

### Future consequences
Expanded layouts and later Resumen/chart consumers can multiply database, allocation
and exact-calculation work per update. Independent transactionally valid reads may
temporarily display different revisions. Neither measurable latency nor an actual
cross-view mismatch was reproduced in this assessment.

### Deliverables
Share observation at an explicit lifecycle/session boundary for consumers of the
same month, retaining generation/revision/as-of identity and consumer retry semantics.
Keep other months independent; bound retained observations and stop them when unused.
Map the selected category directly where full-list formatting is unnecessary. No
global cache, service locator, second financial policy or generic event bus.

### Acceptance criteria
- Two same-month consumers do not establish two underlying reads per stable update.
- Different-month entry previews are not incorrectly replaced by the selected month.
- Rollover, generation change, retry and failure/last-good state reach every consumer.
- Observation lifetime is bounded; switching months or closing views leaks no readers.
- Both panes render coherent revision/as-of data without UI-side arithmetic.

### Tests
Use a counted underlying source and controlled scheduling to demonstrate duplicate
work first. Test sharing, consumer departure, month changes, simultaneous retry and
failure recovery. Use real Room for save→both panes reconciliation and execute compact/
expanded restoration journeys. Measure large-history work before/after; run approved G2.

### Working checkpoint
Own the monthly observation boundary, composition wiring and its consumers. Implement
this separately from dispatcher placement, but preserve the off-main contract if
SELLO-048 is already adopted. No feature-module split is required.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Count underlying reads for two simultaneous same-month consumers.
2. Choose a bounded session/lifecycle sharing owner and define identity/retry contracts.
3. Share compatible observations and map selected detail without formatting unrelated categories.
4. Prove cancellation, separate months, invalidation and disposal; measure before G2.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-055 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

## SELLO-056 — Render growing expense history lazily with stable identity

- **Type:** Task
- **Source classification:** Performance risk
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 85%, conditional on repeatedly loading many expense pages.
- **Depends on:** SELLO-004, SELLO-009, SELLO-018
- **Gate:** G2
- **Source alias:** SELLO-TECH-BUG-006
- **Source report:** [SELLO-TECH-BUG-006](../../technical/ASSESSMENT-2026-10-11.md)
- **Severity rationale:** Medium scaling: every loaded history row is eagerly composed; the device threshold is unmeasured.

### Outcome
Loading older history does not require composing every loaded expense outside the
viewport, and row/scroll identity remains stable during updates.

### What is happening and how
Category detail uses `Column.verticalScroll`; the expense slip calls
`list.rows.forEach`. The ViewModel retrieves 20 rows per page, accumulates them and
copies the accumulated list for emissions. Query paging is real, but UI composition
is eager: every loaded row participates in the scroll layout. Tests for long lists
prove page access and truthful figures, not viewport-bounded rendering performance.

Evidence: `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailScreen.kt:59`
and `:204`; `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt:119`.

### Future consequences
Deep exploration retains increasing layout/semantics work and repeated list-copy
allocations. Later edit/delete/undo can make stable row identity and scroll anchoring
more important. No specific row-count threshold or measured slowdown is asserted.

### Deliverables
Use lazy rendering for potentially long history, keyed by stable expense ID, while
preserving the approved paper/slip appearance and Sello primitives. Keep bounded reads
and full exact totals. Do not nest an unbounded lazy list inside the current scrolling
column or add a paging dependency automatically. Reassess full-list copying separately
only if measured allocations warrant a different collection strategy.

### Acceptance criteria
- A synthetic long history demonstrates viewport-bounded row composition.
- Every row remains reachable; paging never truncates financial totals or date range.
- New saves and refreshes preserve defined scroll behavior and stable row identity.
- Loading-more/failure/retry, exact spoken amounts and font scale 2.0 remain usable.
- Compact, landscape and expanded layouts preserve the existing inset/Back behavior.

### Tests
Add a device composition-count/visibility regression rather than relying only on text
assertions. Exercise many pages, loading failure, resize, restoration and insertion at
the top. Record scroll/heap evidence with reviewed targets and run approved G2. Use
synthetic isolated data, not personal histories or timing sleeps.

### Working checkpoint
Own detail rendering and focused UI tests. Extend design-system/catalog only if a
genuinely reusable primitive is needed; do not copy Slip internals or move feature
financial models into design-system. Coordinate list refresh with the next ticket.

### Context and starting points
Inspect the concrete implementation and adjacent tests named above and the source assessment.
The original probes are evidence of the stated mechanism, not regression acceptance.

### Implementation plan
1. Add a long-history device regression for bounded composition and stable row identity.
2. Use lazy rendering within one compatible scroll owner, preserving Sello slips.
3. Exercise loading, insertion, retry, large font, adaptive layouts and restoration.
4. Measure composition/heap/scroll behavior against reviewed targets and run G2.

### Concrete cases and pitfalls
Preserve the explicit trigger, limitations, failure distinctions and ownership above.
Do not substitute a successful empty read, global cache/dispatcher, silent format
repair, truncated financial history or hypothetical performance result for the fix.

### Verification recipe
Run the focused red→green cases described in Tests, then the affected real-Room
and device scenarios. Run `./scripts/verify-ticket SELLO-056 --retain`;
record the fresh G2 Quality run before proposing Done under QUALITY_FLOW.md.
Performance/accessibility acceptance additionally needs the reviewed measurements
and manual checks stated above; host tests or assembly alone are insufficient.

## SELLO-057 — Keep category-detail identity stable while data loads

- **Type:** Task
- **Source classification:** Task
- **Classification:** Bug; original report 1
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-018, SELLO-015, SELLO-004
- **Gate:** G2
- **Source alias:** SELLO-BUG-001
- **Source report:** [SELLO-BUG-001](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** Lower user impact within P2: the detail header briefly loses the selected identity while loading.

### Outcome
Entering a known category does not briefly present a different generic or previous-category identity.

### Deliverables
Selection-aware title presentation and focused asynchronous-load/navigation regression coverage in the Recibo/detail feature and shell. No schema changes.

### Reproduction and evidence
On baseline Recibo, tap Exploracion. `08-navigation.mp4` and `08-detail-entry-frames.png` show Categoría during loading, followed by Exploracion. The contact sheet includes resampled frames 46–49 with the fallback, before frame 50 shows the name; sampling is not a frame-perfect duration measurement.

The individually cited sampled frames are also retained for direct inspection:
[46](../../bugs/evidence/20261010/navigation-frames/frame-046.png),
[47](../../bugs/evidence/20261010/navigation-frames/frame-047.png),
[48](../../bugs/evidence/20261010/navigation-frames/frame-048.png),
[49](../../bugs/evidence/20261010/navigation-frames/frame-049.png), and
[50](../../bugs/evidence/20261010/navigation-frames/frame-050.png).

Expected: stable selected-category identity, or a neutral loading treatment when identity is genuinely unavailable; no wrong-category title.

### Acceptance criteria
- Known selected category name remains consistent through initial load, refresh, and retry.
- Switching A → B never renders A's name as B's header, including slow/failing reads.
- Unknown/missing identifiers have truthful loading/error/not-found treatment, not an invented name.
- Rotation/restoration and compact versus list/detail layouts preserve selected identity; title handling does not turn failed financial reads into success.

### Tests
Controlled delayed and failed reads, rapid category changes, restored selection, compact UI and expanded selection tests. Check the header before releasing the controlled read, not only its final text.

### Working checkpoint
Recibo → detail → Back works without the recorded title substitution; financial and stale/error states remain truthful.

### Context and starting points
`app/src/main/java/com/software/sello/navigation/SelloApp.kt`, ReciboDestination: title comes only from `CategoryDetailState.Ready`; otherwise it immediately uses `category_detail_title_fallback`. Selection is sent through a LaunchedEffect. Inspect `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt`, its state/tests, and SELLO-018's actual contracts. These are presentation boundaries, not financial title calculations.

### Implementation plan
1. Add a meaningful red test for header identity before the detail read completes.
2. Bind title identity to the selected ID and available presentation metadata; retain ID-only navigation and reject mismatched stale state.
3. Define unavailable-identity loading/error behavior without concealing read failures.
4. Cover selection changes/restoration and rerun existing detail journeys.

### Concrete cases and pitfalls
Do not use a global name cache, pass domain objects through routes, or remove loading/error handling merely to hide the flash. A renamed or unavailable category must not leave a misleading cached title.

### Verification recipe
Use `./scripts/verify-ticket SELLO-057 --retain` for the final G2 gate.
Run affected detail ViewModel/navigation tests first, then connected header/selection journeys and G2 with retained evidence under SELLO-057. Capture initial, delayed, failed, and restored states. SELLO-018's historical evidence does not verify this independently registered fix.

## SELLO-058 — Make unlimited category budgets an explicit opt-in

- **Type:** Task
- **Source classification:** Task
- **Classification:** Requested UX improvement; original report 3
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-016, SELLO-008, SELLO-004
- **Gate:** G2
- **Decision needed:** confirm finite-empty default and finite-draft behavior when toggling unlimited
- **Source alias:** SELLO-BUG-003
- **Source report:** [SELLO-BUG-003](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** Lower user impact within P2: owner-requested opt-in default, not a violation of the previously delivered unlimited default.

### Outcome
A new category begins with a visible budget-entry choice; choosing unlimited is deliberate and removes irrelevant budget-entry controls.

### Deliverables
Revised new-category presentation/default/restoration behavior, truthful validation, and applicable form/catalog tests. Existing persisted categories and financial limit meanings remain unchanged.

### Reproduction and evidence
Open Crear categorías on a fresh installation. `04-category-default.png/.xml` shows Sin límite selected, an empty disabled Límite en pesos field, and active amount suggestions. Source defaults `unlimited` to true in CategoryEditorState and SavedStateHandle initialization.

This reproduces the current behavior, **not a violation of an approved requirement that new categories must be finite**. The source request “I think this should…” is treated as a proposal, not recorded policy approval.

### Acceptance criteria
- After approval, a genuinely new draft defaults to finite with an empty amount; empty is neither unlimited nor an implicit zero.
- Explicit Sin límite opt-in hides both the numeric entry and finite-amount shortcuts.
- A finite submission without an amount is rejected accessibly; explicit zero and unlimited retain their approved distinct meanings.
- Restored existing drafts keep their chosen mode; persisted category/budget history is not rewritten.
- Toggling back to finite follows a documented draft policy. Recommended: retain the unsaved finite draft, but never submit it while unlimited; owner must confirm before implementation.

### Tests
Initial state, explicit unlimited opt-in, hide/show semantics, suggestion eligibility, missing finite amount, explicit-zero versus unlimited contracts, mode/draft restoration, duplicate submit and existing first-run journey.

### Working checkpoint
Create a finite category and an explicitly unlimited category through the real command path; restart preserves their distinct meanings.

### Context and starting points
CategoryEditorState, CategoryEditorViewModel, CategoryEditorScreen, CategoryEditorViewModelTest and FirstCategoryJourneyTest. D04 owns zero/unlimited meaning; D05 does not impose the transaction cap on category limits. Current toggling to unlimited clears limitText—changing that needs the stated decision.

### Implementation plan
1. Confirm default and toggle/draft policy; record it in the approved ticket context.
2. Add red state/UI tests for approved behavior.
3. Update new-draft defaults, conditional presentation and restoration without changing domain semantics.
4. Verify finite/unlimited creation and persisted history; coordinate with SELLO-051's overlapping editor scope.

### Concrete cases and pitfalls
Do not migrate unlimited categories to zero/finite, invent a default monetary limit, or turn an empty field into a successful zero-budget command. Hiding a control must not erase a previously restored draft without the approved policy.

### Verification recipe
Use `./scripts/verify-ticket SELLO-058 --retain` for the final G2 gate.
Focused editor state/UI tests and real finite/unlimited device creation/restart, then G2 with retained evidence. Decision remains outstanding; this ticket is not Ready just because dependencies are Done.

## SELLO-059 — Offer a direct return to the financial current month

- **Type:** Task
- **Source classification:** Task
- **Classification:** Requested navigation improvement; original report 6
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-015, SELLO-009, SELLO-004
- **Gate:** G2
- **Decision needed:** approve current-month action placement/copy
- **Source alias:** SELLO-BUG-006
- **Source report:** [SELLO-BUG-006](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** Lower user impact within P2: requested quick-return affordance; existing manual month selection works.

### Outcome
Users browsing history can return to the current financial month without searching month/year grids.

### Deliverables
One explicit Mes actual action in the month sheet and adaptive side-panel equivalent, using existing shared MonthSession behavior; focused rollover/restoration/accessibility coverage.

### Reproduction and evidence
Open month picker, select September 2026, reopen and go to 2025. `12-month-picker.png/.xml` and `14-previous-year-picker.png/.xml` show only year controls and month cells, with no direct current-month action. The absence is confirmed; this is a UX addition rather than a breach of an existing quick-return acceptance criterion.

### Acceptance criteria
- From any browsed year/month, one explicit action selects the financial current month and dismisses the picker consistently with normal selection.
- Sheet and side panel expose equivalent keyboard/screen-reader-accessible actions; visible copy names the destination clearly.
- Current month comes from the injected observable financial clock/session, not a new wall-clock read or cached picker year.
- Month rollover while the picker is open updates the action's destination; future selection remains prohibited.
- Selection sharing/restoration and existing >30-minute inactivity behavior remain intact; drafts are not cleared and no financial write occurs.

### Tests
Historic selections several years back, already-current selection, financial rollover while open, restored picker/selection, close/back behavior, draft preservation, and compact/expanded accessibility semantics.

### Working checkpoint
Browse a past year → Mes actual → correct current Recibo, with the draft and persisted history unchanged.

### Context and starting points
`app/src/main/java/com/software/sello/navigation/MonthPicker.kt`, `app/src/main/java/com/software/sello/navigation/ShellViewModel.kt` and `app/src/main/java/com/software/sello/navigation/MonthSession.kt` already own current versus selected periods. Reuse normal selection rather than inventing another date controller. The action belongs in presentation/session ownership, not the financial database.

### Implementation plan
1. Approve placement/copy; add red action/state/UI tests.
2. Expose a direct action through existing shell/session pathways and resources.
3. Cover rollover, restoration, layout and draft invariants.
4. Verify no new write/receipt and preserve existing picker navigation.

### Concrete cases and pitfalls
Do not treat selected month as current month, use LocalDate.now(), add a fake swipe capability, or mutate financial time to return home. The incorrect existing accessibility label is tracked independently in SELLO-050.

### Verification recipe
Use `./scripts/verify-ticket SELLO-059 --retain` for the final G2 gate.
Focused MonthSession/NavigationState tests, connected historic-year → current-month journeys and controlled rollover checks, then G2 with retained evidence. Owner placement approval is required before Ready.

## SELLO-060 — Make first-slice navigation motion consistent and accessible

- **Type:** Task
- **Source classification:** Task
- **Classification:** Presentation bug/refinement; original report 2
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-018, SELLO-017, SELLO-015, SELLO-009, SELLO-006, SELLO-004
- **Gate:** G2
- **Source alias:** SELLO-BUG-002
- **Source report:** [SELLO-BUG-002](../../bugs/reports/bugs_revision.md)
- **Severity rationale:** Lower user impact within P2: inconsistent navigation communication; broad flashing claim only partly confirmed.

### Outcome
Category entry/return, dock → Anotar, and month changes communicate the change of context consistently, without depending on animation for meaning.

### Deliverables
A bounded transition matrix for these three journeys, Sello-token-based implementation, reduced-motion branches, and catalog examples for reusable changes. Not a rewrite of navigation or a whole-app motion overhaul.

### Reproduction and evidence
`08-navigation.mp4`: Recibo/detail replace in place, while Anotar uses a plain fade. `23-month-change.mp4`: picker animates, but receipt content changes without the reference's directional slip movement. Existing animation scales were 1.0. **Not all transitions are absent**; the broad intermittent-flash report is only partially reproduced. SELLO-057 owns the confirmed generic-title flash.

### Acceptance criteria
- Document expected enter/exit behavior for each scoped journey and implement it using existing Sello motion tokens.
- Dock → Anotar follows the approved reference's emphasized transition or an explicitly reviewed plain-rise fallback; month slips use the reference's directional 24dp/cross-fade treatment.
- Detail entry/return has a reviewed coherent transition; expanded selection need not pretend to be full-screen navigation.
- No blank intermediate surface, duplicate navigation, blocked input after completion, or replayed command.
- Reduced motion presents the correct end state immediately, with labels/focus making the context understandable.

### Tests
Controlled Compose animation-clock tests for intermediate/end states, rapid Back/re-entry, reduced motion, restored navigation, and compact/expanded behavior. Device recordings supplement tests; avoid arbitrary sleeps.

### Working checkpoint
The three scoped journeys have consistent reviewed motion and still work with animations disabled.

### Context and starting points
`app/src/main/java/com/software/sello/navigation/SelloApp.kt` mixes NavHost destinations and direct detail branches. Use `design-system/src/main/java/com/software/sello/designsystem/theme/SelloMotion.kt`, architecture §6, and `docs/design/sello-spec.html` “Every animation” table. Do not prescribe a new dependency or shared-transition API before checking the pinned toolchain and existing components.

### Implementation plan
1. Ratify a scoped transition matrix; add red tests for missing/inconsistent behavior.
2. Reuse tokens and the existing navigation engine; keep lifecycle/data loading separate from animation.
3. Add reduced-motion and interrupted-navigation branches.
4. Extend relevant component examples if reusable primitives change; record normal/reduced-motion journeys.

### Concrete cases and pitfalls
Do not mask failed reads with animation, animate financial truth using approximate values, or fold SELLO-057's identity correction into a motion-only fix. Deferred chart/theme/receipt refinements are not silently included.

### Verification recipe
Use `./scripts/verify-ticket SELLO-060 --retain` for the final G2 gate.
Focused animation/navigation tests, scoped device recordings at normal and zero animation scales, applicable catalog verification, then G2 and fresh retained report. General flashing needs further reproduction if it persists after the confirmed title defect is corrected.
