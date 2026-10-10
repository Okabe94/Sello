# SELLO-E04 — First complete financial walking slice

- **Status:** In Progress
- **Goal:** Turn the greeting into a usable category → expense → receipt → Recibo journey.
- **Exit:** Users can start empty, create a category, save an expense once, and see truthful persisted budget/detail data after restart.

## SELLO-015 — Introduce the app shell, month session and restored navigation

- **Type:** Story
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-005, SELLO-006, SELLO-009, SELLO-004
- **Gate:** G2

### Outcome
Create stable navigation/month ownership before screens introduce divergent session state.

### Deliverables
Typed routes, SelloScaffold and lifecycle-aware Roots; one shared month session,
validated draft/deep-link entry plumbing, adaptive panels/insets and back policy.
Prepare tab restoration; only Recibo is exposed until real Resumen lands.

### Acceptance criteria
- No Patrimonio, widget, recurrence or disabled Resumen destination. Back closes
  a sheet/detail before returning to Recibo/exit; completed tabs later preserve stacks.
- Month cannot exceed current month. More than 30 minutes background resets month,
  not drafts; financial clock and monotonic inactivity timer have distinct ownership.
- Deep links only validate/prefill; they never create money. Unknown category stays
  unselected. Keyboard hides dock/tab bar and does not cover focused content.

### Tests
Root/session tests for time boundaries, process restoration and malicious/invalid
deep links; device back/insets/keyboard tests across compact and expanded windows.

### Working checkpoint
App launches an honest first-run Recibo scaffold with no fake financial success.
Catalog remains independently usable; G2 passes.

### Context and starting points
Navigation and month state otherwise become scattered per-screen globals. Use the
clock ownership from SELLO-005 and adaptive primitives from SELLO-009. Read
[global structure](../../../design/sello-spec.html#navigation), architecture §6 and
[restoration procedure](../EXECUTION_GUIDE.md#device-and-process-restoration).

### Implementation plan
1. Define app-owned typed destination IDs/session state, initial Recibo scaffold and
   Root boundary for navigation/events; rendering receives immutable state/actions.
2. Implement one month selection shared by consumers, validated against the financial
   date; save small selection/draft IDs in SavedStateHandle rather than whole records.
3. Define back/reselection/detail/sheet behavior and retained future tab stacks; expose
   only implemented Recibo now. Wire actual Resumen tab when SELLO-022 passes.
4. Use monotonic background inactivity to reset month after >30 minutes; financial
   clock changes update the current-month bound without silently clearing drafts.
5. Add validated deep-link parsing and responsive/inset/keyboard handling. A prefilled
   entry request waits for the implemented form; it never writes or fakes a receipt.

### Concrete cases and pitfalls
- At exactly 30 minutes selection remains; over 30 it resets without clearing typed
  amount. Advancing sandbox financial time must not simulate background inactivity.
- Back closes expanded side sheet, then detail selection, then returns tab root/Recibo.
- Unknown/malicious category/amount in a link stays invalid/unselected. Never pass
  a financial object or success result in a route as a substitute for repository lookup.

### Verification recipe
Create/run `*MonthSessionTest`, `*NavigationStateTest`, `*DeepLinkDraftTest`; run app
connected Root/back/keyboard tests and G2. Follow the guide for eligible process death,
not just rotation, and record compact/landscape/expanded behavior.

### Execution progress
2026-10-10: started on branch `sello-015-app-shell` after SELLO-014 merged and E03 closed.

Delivered in `:app`: typed route and tab list, `MonthSession`, `ShellViewModel` with
restoration, the back order, `EntryLinks`, `SelloAppRoot` and `ShellScreen` on the
design system's scaffold with an adaptive month picker, a minimal honest Recibo fed
by the monthly snapshot, and one factory for view models. The greeting is gone.
`docs/development/app-shell.md` states the contracts for the screens that follow.
In `:design-system`, a device test for the keyboard hiding the dock and tab bar,
which had none.

Dependencies added to `:app`, all at versions that leave every existing pin as it
was: Navigation Compose 2.9.8 (the architecture's default engine; 2.10 was not used
because it would force newer Compose, lifecycle and activity versions than those
pinned), lifecycle `runtime-compose`, `viewmodel-compose` and `viewmodel-savedstate`
at the 2.9.4 already resolved, and the Kotlin serialization plugin with
`kotlinx-serialization-core` 1.11.0, which typed routes need. Only `app/gradle.lockfile` changed.

Choices made during the work, for owner review:
- No tab bar with a single tab, and no "Anotar" dock until the entry form exists
  (SELLO-017). A button that opens nothing would be a fake.
- Recibo already reads the real monthly snapshot instead of showing fixed text, so
  what it says is true: empty explanation, exact total, or error with retry.
  SELLO-018 replaces this minimal content.
- The month follows the current month across a month change unless an earlier
  month was picked. Picking the current month goes back to following.
- "In the background" means the screen is no longer visible. Returning after a
  device restart counts as a long absence.
- The 30-minute rule also holds when the app was killed in the background: the
  moment it left is saved with the selection.
- The entry link is `sello://anotar?categoria=…&monto=…`. It is parsed and kept as
  a pending request, but the manifest does not declare it yet: declaring it before
  the form exists would let other apps open Sello on a link that does nothing visible.
- A link that is not a valid address, or not exactly that shape, is ignored whole.
  Inside a valid link, an invalid amount or category is dropped and the rest kept.
- The month picker is a bottom sheet, and a side panel in a window wide enough for one.
  It goes back as far as the year 2000.
- View models are built by one factory in the composition package instead of adding
  Koin's Android artifact, which keeps Koin out of the screens as the architecture
  rule requires.

Found on the way: on the real process-death check the picked month first seemed
lost. The app was right; the check had returned to it with a different kind of
launch than it was started with, which makes Android open a second copy. Repeated
through the launcher both times, the month and the open picker were restored.

Left for later: the dock, the entry form and the link declaration (SELLO-017);
first-run category creation and its button on the empty Recibo (SELLO-016); the
real Recibo content (SELLO-018); the Resumen tab (SELLO-022); showing a database
that cannot be opened at startup as a screen instead of a crash, noted in SELLO-011,
which still needs an owner: it fits this shell and is small, but was not in this
ticket's acceptance.

### Delivery evidence
- **Revision:** branch `sello-015-app-shell`; tested snapshot is commit `946ded7`, with only this ticket's status and evidence text, the retained report, the captures and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** no Patrimonio, widget, recurrence or disabled Resumen destination → `NavigationStateTest.onlyReciboIsExposedAsATab`, and on the installed app `ShellOnDeviceTest.aNewInstallationOpensOnAnHonestEmptyReciboForTheCurrentMonth` finds no tab bar and no dock. Back closes a sheet or detail before Recibo and exit → `backClosesAPanelThenADetailThenReturnsToReciboThenLeaves`, `ShellOnDeviceTest.backClosesTheMonthPickerBeforeItLeavesTheApp` (sheet) and `WideWindowBackTest.backClosesTheMonthPanelBesideTheContentWithoutLeaving` (side panel, real root and view models). Month cannot exceed the current month → `MonthSessionTest.aMonthAfterTheCurrentOneCannotBeSelected`, `theMonthGridDisablesMonthsAfterTheCurrentOne` and, on device, `anEarlierMonthCanBePickedAndALaterOneCannot`. More than 30 minutes resets the month, not drafts → `exactlyThirtyMinutesAwayKeepsTheSelection`, `anythingOverThirtyMinutesAwayResetsToTheCurrentMonth` (30 minutes plus one millisecond) and `aLongAbsenceResetsTheMonthAndLeavesThePendingEntryAlone`. Financial clock and inactivity timer have distinct ownership → `movingFinancialTimeIsNotTimeInTheBackground`, and the root reports leaving and returning in `leavingAndReturningAreReportedToTheMonthSession`. Process restoration → `theMonthPickerAndPendingEntryComeBackAfterTheProcessIsKilled`, `aProcessRestoredAfterALongAbsenceResetsTheMonthOnly`, `savedValuesThatWereTamperedWithDoNotBecomeARequest`, plus the real kill described under Device. Deep links only validate and prefill → `DeepLinkDraftTest` (15 invalid amounts, 10 invalid categories, 22 strings that are not entry links, repeated and unknown parameters) and on device `anEntryLinkOnlyLeavesARequestWaitingAndCreatesNoMoney` and `aHostileOrForeignLinkOpensTheAppAndIsOtherwiseIgnored`, which count the financial rows afterwards. Unknown category stays unselected → the request carries only a well-formed identifier; whether it exists is the form's check (SELLO-017), as the document says. Keyboard hides dock and tab bar and does not cover focused content → design-system `KeyboardInsetsTest`. Compact and expanded windows → `ShellScreenLayoutTest` at 360×700, 760×400, 900×700 and at font scale 2.0. Honest first-run Recibo, no fake success → `ReciboViewModelTest` and `ReciboScreenTest` (loading, first run, exact spending, failure with retry and no amount).
- **Red / Green:** the host tests were written together with the code, so the first run was not a red against a naive version; two of them failed on mistakes in the tests (a collector that never ran, and links with raw spaces, which the parser rightly refuses whole) and were corrected. The first device run failed three tests: the keyboard test sent its insets to the window frame, which consumes them, instead of to the Compose view; two layout tests looked a month cell up by its short label although a cell is announced by its full name; and one exposed a real defect, the month sheet opening half-way and not scrolling, so months were cut off at twice the font size. The sheet now opens whole and scrolls. Thirty-five mutations of the finished code were then run. Thirty-three failed the expected tests at once, across the session (30-minute boundary, restart while away, future month, current month pinned, leaving not recorded, away time tied to financial time, month or leaving moment not restored), the back order, the link parser (any scheme, any host, path, user info, port, fragment, no length limit, first of a repeated value, amount outside the record range), the shell state (picker left open, year uncapped, taken entry kept, session not restored or not saved, second link ignored), the month grid, Recibo (empty month shown as spending, older figures shown on failure, retry doing nothing, month change not followed) and the root (panel not closed by Back, picker always a sheet). Two survived and each got a test that now fails them: the picker reopening on a stale year, and the root never reporting that the app left the foreground. The first hosted run of the pull request (38050007885) then failed three of the new device tests, all for one reason: they asserted that things were visible on screen, and the hosted emulator's screen is smaller than the local one. No app code was wrong. The wide-window cases now check what is laid out and where instead of what is on screen, the phone cases use the device's own window, and the keyboard test uses a fixed phone-sized stage; they were re-run locally on 320×640 and normal screens before the gate was repeated.
- **Gate results:** local `./scripts/verify-ticket SELLO-015 --gate G2` passed, on the final commit after the test corrections: 194 host tests (79 domain, 78 app, 30 design-system, 4 data, 3 catalog), 180 device tests on an isolated API 30 emulator (88 data, 33 app, 49 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 29 warnings, all in `:app`: the 24 present before and five "newer version available" notes for the five entries this ticket added, each deliberately held at the version that fits the existing pins. `app/gradle.lockfile` gained Navigation 2.9.8, `lifecycle-viewmodel-compose` 2.9.4 and the serialization plugin, and `kotlinx-serialization` moved from the 1.7.3 that came in indirectly to the declared 1.11.0. No other lock file changed.
- **Quality run:** run 20261010T121327Z-3e269c1b; SELLO-015 G2 passed; HEAD 946ded7, inputs sha256 03ca1960c014; report docs/planning/mvp/quality-reports/SELLO-015.json sha256 cb54e08c027b2dad309d3691520d94f28f75bd1eb808af2960b7340d60cbd1a7
- **Device / Artifact:** the debug app on the API 30 emulator (1080×2340, Cobalto light, font scale 1.0), started from the launcher on cleared data. It opened on the empty Recibo for October 2026. March 2025 was picked and the picker reopened; then Home, `am kill` (process id 10678 gone), and back through the launcher: a new process (10950) showed the picker open on 2025 with March selected, and Back closed it to "Marzo 2025" without leaving. Rotated to landscape, the month and picker were kept. A `sello://anotar?monto=48700` link opened the app and left zero categories, expenses and receipts and revision 0. Six captures are in `docs/testing/evidence/SELLO-015/`. An expanded window was verified by the layout and back tests, not captured, because it is wider than the emulator's screen.
- **Review:** executor self-review of the diff, captures, reports and logs. The merge of this ticket's pull request is the project owner's acceptance, including the listed choices; the pull request and its checks are that record. No independent technical review.

## SELLO-016 — Deliver first-run category creation and amount-entry prerequisites

- **Type:** Story
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-013, SELLO-015, SELLO-008, SELLO-004
- **Gate:** G2

### Outcome
A new user can create the first real category and optional monthly budget without fixtures.

### Deliverables
Category editor State/Action/Event/ViewModel/Root/Screen, name/icon/limit fields,
validated command submission and restored draft/operation ID. First-run Recibo
offers create category; backup action is exposed only after its real workflow exists.

### Acceptance criteria
- Anotar without an eligible category directs to the editor and explains the
  prerequisite. Save commits a real category/current-month limit and returns it selected.
- Invalid/duplicate submission is handled explicitly; archived identities cannot
  reappear as new eligible categories. Empty financial state is not an error fallback.
- Draft survives rotation/process reconstruction; creation stamp requires receipt.

### Tests
ViewModel action/draft/busy/receipt tests; real command/UI creation, invalid name/limit,
double submit and restart retention. Add editor-related component states to catalog.

### Working checkpoint
Fresh install can create/read its first category without test tools. G2 passes and
the next entry screen can use real category IDs.

### Context and starting points
The first category is a real prerequisite for expenses; seed data must not hide an
unusable fresh install. Use category/limit ports from SELLO-013 and controls from
SELLO-008. Read [category editor](../../../design/sello-spec.html#s-catform) and
approved D04/D05; use those accepted rules for field bounds and month limits.

### Implementation plan
1. Add category feature State/Action/Event/ViewModel/Root/Screen and raw name/icon/
   optional-limit draft with required/error/busy states. Use approved lengths/keys.
2. Load eligible category state and implement first-run call-to-action; if none exist,
   the expense dock opens creation with a prerequisite explanation, not an empty form.
3. Submit create command with one preserved operation ID and generation. On uncertain
   outcome recover receipt; show creation state only after confirmed commit.
4. Return created category ID to the caller/selected context using the navigation
   contract, not a mutable category object or invented successful local row.
5. Save drafts across recreation and render commit/reference errors with retry; add
   any genuinely new shared control states to catalog rather than copying editor UI.

### Concrete cases and pitfalls
- Empty name/missing icon/invalid limit cannot create a row. Unlimited budget choice
  follows D04; empty text must not become zero without an explicit user selection.
- Double tap or process restart during save produces one category/period limit.
- Backup-import CTA is absent until SELLO-030 exists; a dead link is not onboarding.

### Verification recipe
Create/run `*CategoryEditorViewModelTest` and device `FirstCategoryJourneyTest`; G2.
On an empty **debug** profile create a category, restart, and verify its actual ID,
limit and selected state. Include raw-input/error and restored-operation assertions.

### Execution progress
2026-10-10: started on branch `sello-016-first-run-category` after SELLO-015 merged. No
dependency was added and the schema did not change.

Delivered. `:app`: the category editor (state, actions, view model, root and screen)
on the real `CreateCategory` command, with its draft, the identifier of a save in
flight and its result kept across rotation and process death; the first-run Recibo
with "Crear categorías"; Recibo listing categories with their limits and "Agregar
categoría"; the editor route with its returned identifier; and the "an entry needs a
category first" path. `:design-system`: `IconChoiceGrid` and nine more category
icons, with a catalog example `inputs.icon-choice`.
`docs/development/category-editor.md` states the form's rules and contracts.

Choices made during the work, for owner review:
- A new category is "Sin límite" unless the switch is turned off and an amount is
  typed. Turning it off and leaving the amount empty is an error, never zero.
- Fourteen icons, the ones the reference names. It shows twenty-eight without
  naming the other fourteen, so those need choosing. Each icon has a spoken name
  (Comida, Hogar, Transporte and so on).
- Recibo gained an "Agregar categoría" button under the list. The reference does
  not place one there, but without it only one category could ever be created
  until the editing ticket.
- The form is a full screen in every window. The reference makes editors a side
  panel on wide windows; that is left for later.
- Left out of the form for now: the preview of the category cell, the line saying
  what the month's total limit would become, and suggestions drawn from the
  person's own limits. The three fixed suggestions are shown.
- The explanation "to record an expense you first need a category" is delivered
  and tested, but today only an entry link triggers it, and the link is not yet
  registered with Android. The "Anotar" dock that will normally trigger it arrives
  with the entry form in SELLO-017, because a dock that opens nothing once a
  category exists would be a fake.
- Back while saving does nothing. Back with a draft asks before discarding.
- When the name is refused, the form scrolls back up to it.

Found on the way: on a small screen the "name already used" message appeared above
the visible area, because the button is at the bottom and the name at the top. The
form now scrolls to the name when it has an error.

Left for later: editing, archiving and limit changes (SELLO-020); the dock and the
entry form (SELLO-017); the real Recibo content (SELLO-018).

### Delivery evidence
- **Revision:** branch `sello-016-first-run-category`; tested snapshot is commit `c1bc912`, with only this ticket's status and evidence text, the retained report, the captures and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** Anotar without an eligible category directs to the editor and explains the prerequisite → `NavigationStateTest.anEntryWithNoCategoryToRecordItInAsksForOneOnceAndKeepsTheRequest`, `onlyACategoryThatIsNotArchivedCountsForAnEntry`, and on the installed app `ShellOnDeviceTest.anEntryLinkWithNoCategoryOpensTheCategoryFormWithTheReasonAndCreatesNoMoney`; the trigger today is the entry link, as the progress notes say. Save commits a real category and current-month limit and returns it selected → `FirstCategoryJourneyTest.aNewInstallationCreatesItsFirstCategoryAndStillHasItAfterTheScreenIsRebuilt`, which reads the category row, the default limit for the current month, the receipt and the revision from the app's database, and the result contract `CREATED_CATEGORY_RESULT` carrying only the identifier. Invalid and duplicate submission handled explicitly; archived identities cannot reappear → `aNameAlreadyInUseIsRefusedOnTheFieldAndTheDraftCanBeDiscarded`, `aLimitThatIsNotAnAmountIsExplainedKeptAsTypedAndCreatesNothing`, and in `CategoryEditorViewModelTest` the name, limit and rejection cases; the archived-name rule itself is the command's, proven in SELLO-013. Empty financial state is not an error fallback → `ReciboScreenTest.theFirstRunOffersToCreateCategoriesAndNothingThatDoesNotExistYet` against a separate failed state. Draft survives rotation and process reconstruction → `aDraftSurvivesTheScreenBeingRebuilt`, `theDraftComesBackAfterTheProcessIsKilled`, and the real kill under Device. Creation requires a receipt → `whileSavingTheFormIsBusyLockedAndASecondTapSendsNothing` (no result until the answer arrives), `anUnknownOutcomeThatTurnsOutNotSavedReturnsToTheFormWithNothingCreated`, `whileTheOutcomeCannotBeReadTheFormStaysLockedAndRetryOnlyAsksAgain`. Double submit and restart during save produce one category → `tappingCreateTwiceCreatesOneCategory` on the real database, `aSaveInterruptedByTheProcessDyingIsRecoveredFromItsReceiptWithoutSendingAgain`, `anInterruptedSaveThatNeverCommittedLeavesTheDraftReadyToSend`. Empty text never becomes zero → `turningTheLimitOffWithNoAmountAsksForOneInsteadOfAssumingZero`, `withoutALimitTheCategoryIsUnlimitedNotZero`, `aTypedZeroIsARealZeroBudget`. No backup action → asserted absent in the first-run test. Editor component states in the catalog → `IconChoiceGrid` with example `inputs.icon-choice`, covered by `IconChoiceGridTest` and the catalog's open-every-example test.
- **Red / Green:** the tests were written with the code, so the first run was not a red against a naive version. The 28 view-model tests passed on their first run; mutation was used to find out whether they would fail when they should. Of 33 mutations of the finished code, 30 failed the expected tests at once: empty limit read as zero, limit on by default, second tap sending again, editable while saving, operation identifier not saved or kept after a rejection, created before the receipt, unknown outcome treated as created or resubmitted, unreadable outcome treated as not saved, no recovery after restart, taken name shown as a storage error, draft name or limit or result not saved, draft discarded without asking, default icon ignoring used ones, unoffered icon accepted, typing while unlimited, archived category counting for an entry, failed read sending to the editor, prerequisite never cleared, unlimited listed wrongly, icon grid ignoring width, keeping its own selection or picking while disabled, result not returned, first-run button doing nothing, prerequisite not navigated or not explained. Three survived and each got an assertion that now fails them: pressing a disabled button showing an error, a replaced history not being re-read, and Back while saving opening the discard question. The first device run failed seven tests, all on how the tests found or pressed things (a disabled field has no "set text" action to search by; a second click after the screen had already changed; Back closing the keyboard first; one test calling its setup three times). One later failure, on a small screen, was a real defect: the "name already used" message appeared above the visible area. The form now scrolls to the name. While this ticket was in progress the hosted run on `main` after SELLO-015 (38053383227) failed one of that ticket's on-device tests once: it looked at the screen before Recibo had finished reading the database, which the test framework does not wait for. It had passed on the pull request, so it is a timing fault in the test, not in the app. Every on-device test now waits for what it expects; the app suite was then run three times locally, once on a small screen, without a failure, and the gate repeated.
- **Gate results:** local `./scripts/verify-ticket SELLO-016 --gate G2` passed, on the final commit: 225 host tests (108 app, 79 domain, 31 design-system, 4 data, 3 catalog), 200 device tests on an isolated API 30 emulator (88 data, 54 design-system, 48 app, 10 catalog), ktlint, architecture rules, lint with 0 errors and 29 warnings, all in `:app` and present before this ticket. The app, design-system and catalog device tests were also run on a 320×640 screen. No dependency, lock file or schema changed.
- **Quality run:** run 20261010T154954Z-122c4a7b; SELLO-016 G2 passed; HEAD c1bc912, inputs sha256 fef0e38a7ea1; report docs/planning/mvp/quality-reports/SELLO-016.json sha256 22af205ef9b9e0bd475ae7d7090e94689c439738bef5da7f95acb3210aed6742
- **Device / Artifact:** the debug app on the API 30 emulator (1080×2340, Cobalto light, font scale 1.0), on cleared data and started from the launcher. "Crear categorías" opened the form; a name was typed and the shopping-cart icon chosen; then Home, `am kill` (process 13242 gone) and back through the launcher: a new process (13475) showed the same name and icon. "Crear categoría" returned to Recibo listing the category with "Sin límite" and "Has gastado 0". After `force-stop` and a fresh start it was still listed, and the database held one category with icon `shopping_cart`, one default limit for 2026-10 of kind `unlimited`, one `category.create` receipt and revision 1. The emulator's keyboard completed the typed word to "Mercador"; that is the keyboard, not the app, and the name stored is exactly what the field showed. Five captures are in `docs/testing/evidence/SELLO-016/`. A finite limit on the installed app is covered by the journey test, not by this manual run.
- **Review:** executor self-review of the diff, captures, reports and logs. The merge of this ticket's pull request is the project owner's acceptance, including the listed choices; the pull request and its checks are that record. No independent technical review.

## SELLO-017 — Ship Anotar expense and its durable receipt end-to-end

- **Type:** Story
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-012, SELLO-014, SELLO-016, SELLO-007, SELLO-008, SELLO-004
- **Gate:** G2

### Outcome
Complete the first money-writing UI path, not an attractive form with a fake save.

### Deliverables
Amount-first Anotar sheet with COP/category/date/note, preview from shared policies,
State/Action/Event/ViewModel/Root/Screen, persisted draft/operation ID, receipt display
and recovery/retry affordances. MVP has no recurrence/batch toggle or dormant routes.

### Acceptance criteria
- Valid confirmation saves exactly one expense and shows Recibido only for its
  confirmed create receipt. Recomposition, navigation and restart never re-save.
- Invalid/future input rejects clearly. Pending submit disables duplicate action;
  unknown outcome recovers using the original ID and preserves draft if rejected.
- Dismiss/rotation/system keyboard follows reference and accessibility contract.
  Success haptics/animation cannot affect whether financial save succeeded.

### Tests
ViewModel submit/cancellation/error/restored-ID tests; real-Room UI duplicate/restart
journeys; exact amount/category/date audit and accessibility assertions. Run G2.

### Working checkpoint
Fresh install → category → Anotar → receipt → persisted expense works on device;
later app startup reads the same record. No developer fixtures are required.

### Context and starting points
This is the first user-visible financial commit; visual save completion is not
financial proof. Inputs are expense command/lookup from SELLO-012, preview policy
SELLO-014, categories from SELLO-016 and controls. Read
[Anotar](../../../design/sello-spec.html#s-anotar), architecture §§5/6 and recovery cases.

### Implementation plan
1. Build expense feature/form/receipt rendering with amount/category/date/note draft;
   default category/date through explicit rules and prefill validated caller context.
2. Derive “after save” preview from the shared snapshot/policy with draft inputs;
   label it as preview, never persist forecast or recompute rules inside the screen.
3. Generate one operation ID per logical submission, save it before invoking the
   command, and make submit/recovery/busy/error states explicit in the ViewModel.
4. On committed result read/display the actual durable receipt and refresh observed
   Recibo; recover unknown/canceled submits by original ID after recreation.
5. Implement dismiss/keyboard/back/accessibility and exact success animation. Do not
   surface recurrence/batch controls or creation celebration for an edit.

### Concrete cases and pitfalls
- Enter 10,000/category C/today, tap twice: one row and receipt. Reopen/recompose
  receipt: no command invocation. Changing draft after rejected submit needs a new logical ID.
- If preview becomes stale while another command commits, final receipt/snapshot
  wins; never announce the preview as saved truth.
- Interrupted save preserves pending identity; UI must not tell the user to blindly
  repeat the same money under a new ID.

### Verification recipe
Create/run `*ExpenseFormViewModelTest` plus `ExpenseEntryJourneyTest` on real Room/UI;
G2. Execute create/repeat-tap/recreate/eligible-process-death/read-back journeys and
compare exact row/category/date/receipt IDs, not just snackbar text.

### Execution progress
2026-10-10: started on branch `sello-017-anotar-expense` after SELLO-016 merged. No
dependency was added and the schema did not change.

Delivered. `:domain`: `ExpensePreviewPolicy` and the `ExpenseReads` port. `:data`:
`RoomExpenseReads`. `:app`: the Anotar form (state, actions, view model, root and
screen) on the real `CreateExpense` command, with its draft, the identifier of a
save in flight and the saved expense's identifier kept across rotation and process
death; the receipt read back from storage; the dock on Recibo; the route; and the
`sello://anotar` link declared in the manifest. `docs/development/expense-entry.md`
states the rules and contracts.

Choices made during the work, for owner review:
- After saving, the screen shows a receipt with the stored expense and "Recibido",
  with a "Listo" button, as the ticket asks. The reference instead closes the sheet
  and shows a "Gasto anotado" message with "Deshacer"; undo belongs to SELLO-019.
- The category that starts selected: the one a link or a just-created category asks
  for; otherwise the one used last; otherwise the only one. With several and no
  history, none, and the person chooses.
- The date is the day the form was opened and stays that day if the draft is
  restored later. Earlier days can be picked with the platform's date picker in
  Sello's colours; later days cannot.
- The preview is labelled "Vista previa, si lo anotas" and says "te pasas por" with
  the amount when a limit would be passed. Going over never blocks saving.
- The keypad and the button stay at the bottom while the rest scrolls. The first
  version scrolled everything and the button ended up below the screen.
- Anotar is a full screen, not a sheet rising from the dock, and has no print-feed
  motion yet. The stamp lands and the phone vibrates once per receipt.
- The link is declared for apps on the device only, not for web pages.
- The route carries the prefilled category and amount as hints. They are drafts
  from outside, checked again by the form; nothing saved travels in a route.
- No Gasto/Ingreso switch, "varios" or repeat control: those features do not exist.

Found on the way: on the first manual run the "Anotar gasto" button was below the
visible screen, behind the system bar, once the preview appeared. The layout was
changed so the keypad and button are always in view. Two test faults were also
fixed: a click aimed by screen position missed while the keyboard was sliding in,
and a keyboard left open by one test held the focus the next one needed.

Left for later: undo, edit and delete (SELLO-019); income (SELLO-021); the real
Recibo content and expense history (SELLO-018); the sandbox scenarios (SELLO-024).

### Delivery evidence
- **Revision:** branch `sello-017-anotar-expense`; tested snapshot is commit `0eace87`, with only this ticket's status and evidence text, the retained report, the captures and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** valid confirmation saves exactly one expense and shows Recibido only for its confirmed receipt → `ExpenseEntryJourneyTest.aNewInstallationRecordsItsFirstExpenseAndShowsTheReceiptForTheStoredRow`, which reads the expense row (amount, currency, category, date, note, sequence, version), checks the `expense.create` receipt's subject is that expense and the revision, and `ExpenseFormViewModelTest.theReceiptShowsWhatStorageHoldsOnlyAfterTheSaveIsConfirmed`. Recomposition, navigation and restart never re-save → `rebuildingTheScreenOnTheReceiptOrOnADraftNeverRecordsAgain`, `aReceiptShownAgainAfterARestartSendsNothingAndIsReadFromStorage`, and the real kills under Device. Invalid and future input rejects clearly → `pastedTextIsUsedWholeOrKeptAsItWasWithTheReason`, `aThirteenthDigitIsRefusedAndSaidSoWithoutChangingTheAmount`, `aDayAfterTodayCannotBeChosenAndAnEarlierOneCan`, `theButtonStaysOffAndSaysWhatIsMissingInOrder`, `everyOtherRejectionKeepsTheDraftAndSaysNothingWasSaved`. A submit in flight disables the duplicate action → `whileSavingASecondTapAndAnyEditAreIgnored` and, on the real database, `pressingAnotarGastoTwiceRecordsOneExpense`. Unknown outcome recovers with the original identifier and keeps the draft if not saved → the three unknown-outcome tests and `aSaveInterruptedByTheProcessDyingIsRecoveredFromItsReceiptWithoutSendingAgain`. Dismiss, rotation and keyboard → `backLeavesAnUntouchedFormAsksAboutATypedOneAndLeavesAReceiptAtOnce`, `aTypedDraftIsNotThrownAwayWithoutAsking`, `theDraftComesBackAfterTheProcessIsKilled`; the keypad steps aside for the system keyboard by the same inset rule the design-system test covers. Accessibility → `ExpenseEntryScreenTest.theAmountIsReadInFullTheChipsAreOneChoiceAndTheButtonSaysTheWholeSentence` and the twice-the-font test. Success feedback cannot affect the save → the vibration and stamp run from the receipt already in state, inside `runCatching`. Preview from shared policies → `ExpensePreviewPolicyTest` with the reference's figures (937.200 and 287.600 becoming 888.500 and 238.900; 8.800 left and 21.100 spent giving "over by 12.300") and `aDateInAnotherMonthPreviewsThatMonth`. No recurrence or batch control → none exists in the screen. Category to Anotar on a new installation → `withNoCategoryAnotarAsksForOneFirstThenGoesOnToTheExpenseWithItChosen`. Link prefill → `aLinkPrefillsTheDraftAndRecordsNothingUntilThePersonConfirms`.
- **Red / Green:** the tests were written with the code. The 43 view-model tests first ran with two failures, both in the tests (a fixture whose figures I had not worked out, and an expectation missing a new field). Of 40 mutations of the finished code, 32 failed the expected tests at once, across the preview policy, saving once, recovery, rejections, the draft, the date, the default category, the labels and navigation. Two were first written against text the formatter had reflowed and were run again correctly; both failed tests. Three survived and each got a test that now fails them: a wrapped overflow in the "over by" figure, a rejected attempt still being asked about after a restart, and a draft's date following the clock. Three more survive because they change nothing a person or the database can observe: picking a category that is not offered, or one asked for that cannot take an expense, is filtered out before it is shown or sent; and passing the new category to the form after "create one first" equals the form choosing the only category there is. The first device run failed three journey tests on an ambiguous lookup: with no limits the preview shows the same figure as the amount. A later failure on a small screen was a press aimed by position while the keyboard was moving, and intermittent failures of unrelated Back tests were traced to a keyboard left open by a typing test; both are fixed in the tests, and the app suite then passed twice at normal size and twice at 320×640.
- **Gate results:** local `./scripts/verify-ticket SELLO-017 --gate G2` passed on the final commit: 278 host tests (152 app, 88 domain, 31 design-system, 4 data, 3 catalog), 222 device tests on an isolated API 30 emulator (91 data, 67 app, 54 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 29 warnings, all in `:app` and present before this ticket. No dependency, lock file or schema changed. The manifest gained `singleTop` and one intent filter for `sello://anotar`.
- **Quality run:** run 20261010T220436Z-c0bedf41; SELLO-017 G2 passed; HEAD 0eace87, inputs sha256 db3816a79059; report docs/planning/mvp/quality-reports/SELLO-017.json sha256 cda6ec0863c217c4ce7ee33ea091e1f7b3e834da1413194ae22f7b86c1f0e121
- **Device / Artifact:** the debug app on the API 30 emulator (1080×2340, Cobalto light, font scale 1.0), from the launcher on a clean install. A category "Plaza" was created, then "Anotar un gasto": 487.000 was typed on the keypad. Home and `am kill` (process 14311 gone), back through the launcher: a new process (14634) showed 487.000 with zero expenses in the database. "Anotar gasto" showed the receipt, and the database held one expense of 487000 in Plaza dated 2026-10-10 with sequence 1 and an `expense.create` receipt at revision 2. Killed again on the receipt and reopened: the receipt again, still one expense and one receipt. "Listo" returned to Recibo showing "Has gastado 487.000", and it showed the same after `force-stop` and a fresh start. `am start -a VIEW -d "sello://anotar?monto=12500"` opened the form with 12.500 and left the expense count at one. Five captures are in `docs/testing/evidence/SELLO-017/`.
- **Review:** executor self-review of the diff, captures, reports and logs. The merge of this ticket's pull request is the project owner's acceptance, including the listed choices; the pull request and its checks are that record. No independent technical review.

## SELLO-018 — Deliver Recibo, category detail and expense history

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-014, SELLO-017, SELLO-009, SELLO-004
- **Gate:** G2

### Outcome
The first slice answers what remains and lets users inspect the source expenses.

### Deliverables
Real Recibo/category-circle grid, current/past month slips, category detail and
bounded expense list using shared snapshots; loading/empty/error/last-good states,
category-scoped Anotar, sort defaults, and adaptive list/detail selection.

### Acceptance criteria
- Saved expense updates total/category at a consistent revision; detail/list amounts
  reconcile to the receipt. No home pace graph, warning card or duplicate rule engine.
- Prior month hides daily allowance and uses its saved budget; unlimited categories
  sort last. All-time/list pagination does not silently truncate totals or date range.
- First/subsequent read failure is visible and retryable, never an empty success.
- Large values/font scales do not clip; category status is not color alone.

### Tests
Snapshot/render-model tests, device save→detail→month→restart journey, injected read
failures, empty versus failed state, expanded layout and exact accessibility amounts.

### Working checkpoint
The complete walking slice works on device and all prior tickets' gates remain green.
Sandbox scenarios can now exercise meaningful real financial state.

### Context and starting points
Users must trace the receipt back to consistent persisted totals and history.
Use SELLO-014 snapshot/failed states and SELLO-017 entry result. Read
[Recibo](../../../design/sello-spec.html#s-recibo), category detail and architecture §6;
MVP detail is budget/actuals/history, not all deferred comparison/record charts.

### Implementation plan
1. Map one snapshot to Recibo totals/verdict/allowance/category cells, keeping exact
   values and original revision/as-of labels. UI formatting cannot calculate finance.
2. Add category detail with spent/limit/remaining and bounded ordered expenses, explicit
   paging/all-record access and category-scoped Anotar prefill.
3. Implement current/past/no-limit/loading/empty/error/last-good branches, selected
   month/detail restoration and adaptive list/detail behavior.
4. Apply approved default ordering (D06), unlimited-last and accessible labels; keep
   unsupported weekly/comparison/export links absent, not successful placeholders.
5. Reconcile new receipt/edit events through repository observation, not imperative
   local subtraction of displayed text. Record example states in previews/catalog as applicable.

### Concrete cases and pitfalls
- B01/H01 produce exact totals across home/detail/list after month change/restart.
- Read fault with previously loaded records shows failed/stale state, never “no expenses”.
  Empty fresh install remains a genuine different state.
- Pagination may limit visible rows, not aggregate totals; no arbitrary 2000–2099
  “all-time” filter or Float source values.

### Verification recipe
Create/run `*ReciboRenderModelTest`, `*CategoryDetailViewModelTest` and the device
WalkingSliceJourneyTest; G2. Save then browse detail/month/history/restart; record
receipt-to-list-to-total reconciliation and a deliberately injected read-failure state.
