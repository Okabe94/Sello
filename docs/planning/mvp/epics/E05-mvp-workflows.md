# SELLO-E05 — Complete everyday MVP workflows

- **Status:** In Progress
- **Goal:** Complete maintainable expense/income history and an honest monthly summary.
- **Exit:** Edits/deletes/undo, category/budget management, income, basic Resumen and preferences all work after restart and preserve prior history.

## SELLO-019 — Implement expense edits, deletion and guarded six-second undo

- **Type:** Story
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-012, SELLO-017, SELLO-018, SELLO-004
- **Gate:** G2

### Outcome
Users can correct/remove a recorded expense without false success or unsafe restoration.

### Deliverables
Versioned expense edit/delete/compensation commands and real transactional adapters;
durable deletion receipt/tombstone as needed; editor and menu/swipe equivalents;
six-second undo offer tied to a real monotonic timer, never simulated financial time.

### Acceptance criteria
- Edit/delete proves affected row/version and updates consistent snapshots. Editing
  a deleted/stale record conflicts instead of reporting success; edit has no creation stamp.
- Undo restores only the original eligible deletion and refuses later conflicting
  edits/recreation/reset. Restart/recomposition cannot extend the original undo offer.
- Apply approved D07/U01–U05: deadline is six real elapsed seconds; rotation and
  backgrounding within the live process preserve it. Process death expires the
  interactive offer, but deletion/compensation receipts still recover actual outcomes.
- Same operation replays safely; failure/cancellation preserves recoverable outcome.
  Both gesture and accessible menu paths exist, and deadlines/receipts are explicit.
- Historical expense correction is discoverable through contextual guidance and
  retained help; effective date/affected period is explicit before saving and
  completion feedback follows the committed receipt (D04/H06).

### Tests
Real-Room stale-edit/delete/undo races, rollback, missing row and reset-generation
tests; virtual monotonic expiry and rotated/recreated UI tests; device history totals
before/after edit/delete/undo. Extend catalog states and sandbox regression inputs.
Include exact deadline, same-process recreation, process-death offer expiry and
already-committed compensation recovery without re-deleting the restored record.

### Working checkpoint
Existing create journey still works; corrections survive restart and never silently
overwrite newer history. Run G2.

### Context and starting points
An expense is historical financial data, so an unchecked update or blind undo is
not a harmless form action. Use SELLO-012's operation protocol, SELLO-017's form,
SELLO-018's lists and approved D07. Read architecture §5 and
[recovery cases](../DECISION_INPUTS.md#recovery-cases-to-turn-into-tests).

### Implementation plan
1. Add expense-specific edit/delete/compensate commands with operation ID, generation
   and expected row version. Check affected rows and increment revision atomically.
2. Persist the original deleted payload and guarded compensation identity/expiry
   proof according to D07; define missing/stale/conflict outcomes explicitly.
3. Open the existing form in edit mode from real history ID; load current version,
   preserve raw draft, display Guardar cambios, and never replay create on restoration.
4. Add accessible delete menu and swipe equivalent, immediate committed deletion
   and six-second Deshacer using monotonic time. Recreation cannot restart the timer.
5. Restore only if compensation's version/generation/eligibility still holds; update
   observed snapshots rather than manually editing rendered totals. Add conflict/error states.

### Concrete cases and pitfalls
- Change 10,000 to 12,000: one record, increased version and totals by 2,000.
  A second editor holding the old version must fail without overwriting 12,000.
- Delete/undo returns the exact original record; repeated undo is idempotent only
  for that operation. Newer recreation/edit/reset must block compensation.
- Advancing financial clock a year leaves the real undo duration unchanged. After
  process death, recover actual outcomes but expire the offer, never grant fresh time.
  A previously committed undo still restores the record after receipt recovery.

### Verification recipe
Create/run `ExpenseEditDeleteContractTest`, `UndoCompensationContractTest` on real
Room and `*ExpenseEditViewModelTest` on host; G2. Execute edit/stale/delete/undo/expiry/
recreate device journeys with exact row/version/receipt/snapshot assertions.

### Execution progress
2026-10-12: started on branch `sello-019-expense-corrections`. Delivered in two pull
requests, as agreed with the project owner; the ticket stays In Progress until the second.

**Decisions taken with the project owner before any code**

- A deleted expense is removed from `expense` and what it was is kept in a separate
  table, not marked as deleted in place, so no read or total can count it by mistake.
- The six seconds are counted on the monotonic clock and held in memory. A process
  that ends takes the offer with it and startup removes the kept copy.
- A second deletion makes the first one final: at most one deletion can be undone.
- An edit is refused when the expense changed since it was read. An expense may be
  edited while it stays in a category archived since, or be moved out of it, but
  never moved into an archived category.
- "Guardar cambios" is available only after a change.
- Wording: "Editar gasto", "Guardar cambios", "Borrar", "Gasto borrado", "Deshacer",
  "Gasto actualizado". The creation flow does not change.
- The dismissible first-time announcement of historical correction is deferred to
  SELLO-023, which owns remembered preferences; a note is on that ticket. This
  ticket delivers the guidance shown in context every time.

**First pull request: storage and commands, no screens**

- Schema version 2 with a tested migration: `deleted_expense`.
- `ExpenseCommands.edit`, `delete`, `restore` and `undoOffer`, with their receipts.
- Real-database contract tests for U01 to U05, stale versions, replays, concurrent
  commands, cancelled callers, rollback and the month's totals.
- Nothing in the app calls the new commands yet, so every existing journey is as it was.
- Verified with the ticket's G2 gate on an API 30 emulator, each time on the
  uncommitted tree that became the commit: run 20261012T190048Z-4eafa3e0 passed
  before the first commits, and run 20261012T200307Z-43c7e3f2 after the date rule
  changed. Its report is not retained
  here; the run that counts as Delivery evidence is the one made when the ticket
  is proposed Done.

**Choices made while implementing, for review**

- An edit that changes nothing is refused by the command (`Unchanged`) and writes no
  receipt, so the rule does not depend on the screen alone.
- The date rule applies only when an edit changes the date (project owner,
  2026-10-12, replacing my first choice of checking every edit). An expense already
  dated after today, possible only if the financial day moved backwards, can have
  its other fields corrected; it cannot be moved to another future date.
- Undo puts the row back without interpreting it, exactly as deletion kept it.
- A new expense's sequence follows the highest of the recorded and the kept one.
  Once a deletion is final its sequence can be used again.

**Second pull request: the screens**

- The entry form corrects a recorded expense: "Editar gasto", "Guardar cambios" only
  after a change, "Borrar" in the top bar, the months it touches named before saving.
- A category's list: press to correct; long press, swipe and screen-reader actions to
  delete; a line that always says how.
- "Gasto borrado" with "Deshacer" over the bottom of Recibo and of a category's detail,
  held by one object for the whole process.
- Design system: `SelloNotice`, `ActionLine` and a notice slot in the scaffold, with
  catalog examples `feedback.notice` and `paper.action-line`.

**Choices made while implementing the screens, for review**

- Deleting does not ask first, from the form or from a row: undo is the safeguard.
- A correction has no receipt screen. The form closes and the list says "Gasto
  actualizado".
- The form shows no preview while correcting: the preview adds the draft to the month,
  and a recorded expense is already counted in it.
- Once the expense is known to have changed or gone underneath the form, nothing more
  can be sent from it; the person leaves and opens it again.
- Texts that were not agreed beforehand: "Gasto restaurado", "Ya no se puede deshacer.
  El gasto quedó borrado.", "No se pudo deshacer. Inténtalo de nuevo.", "No se pudo
  deshacer porque tus datos cambiaron. El gasto quedó borrado.", "Aún no cambiaste
  nada.", "Corregir este gasto cambia las cifras de septiembre.", "Toca un gasto para
  corregirlo. Para borrarlo, deslízalo o mantenlo presionado.", "Seguir editando", and
  the reasons a correction or deletion was refused.
- A notice that needs no answer stays four seconds.
- The offer is shown on Recibo and a category's detail, not over the entry form.

**Not delivered here, and where it goes**

- Sandbox regression inputs: there is no sandbox until SELLO-024; a note is on that
  ticket.
- The dismissible first-time announcement: SELLO-023, as decided above.
- Undo after recording a new expense, which the reference also shows: not part of this
  ticket's acceptance.

### Delivery evidence
- **Revision:** branch `sello-019-expense-correction-screens`, cut from `main` at `bf9f57d`, which holds the first pull request (#39). Tested snapshot is commit `422cc50` with this ticket's progress notes and the note on SELLO-024 uncommitted on top of it, which the gate counted as inputs; only the status, this evidence text, the retained report and regenerated board views were added after the run, all of which the input fingerprint excludes by design. `main` had not moved when the pull request was opened.
- **Requirement mapping:** Edit/delete proves the affected row and version and updates consistent snapshots → `ExpenseEditDeleteContractTest` (one record, version 1 to 2, one receipt; `theMonthsTotalFollowsAnEditADeletionAndAnUndoWithoutBeingToldTo`: 10.000, 12.000, 0, 12.000) and in the installed app `ExpenseCorrectionJourneyTest` (288.000 left after 10.000 becomes 12.000, 300.000 after deleting, 288.000 after undo, each compared with the stored row). Editing a deleted or stale record conflicts → `anEditorHoldingTheOldVersionCannotOverwriteTheNewerAmount`, `anExpenseThatDoesNotExistOrWasDeletedCannotBeEdited`, `ofSeveralEditsMadeAtOnceFromTheSameVersionExactlyOneIsSaved`; on screen `ExpenseEditViewModelTest.aRefusedEditKeepsWhatWasTypedAndSaysWhatHappened` and `onceItChangedOrIsGoneUnderneathNothingMoreCanBeSentFromThisForm`. Edit has no creation stamp → the stored `created_at` and sequence are asserted unchanged; a correction shows no receipt (`savingSendsOneEditOfTheVersionReadThenClosesAndSaysItWasUpdated`). Undo restores only the original eligible deletion and refuses conflicting edits, recreation and reset → `UndoCompensationContractTest` (identifier in use, sequence taken, generation replaced, a later deletion). Restart and recomposition cannot extend the offer → `afterTheProcessEndsThereIsNoOfferAndTheDeletionStands`, `theSameDeletionAgainAnswersWithTheOriginalAndNeverGivesMoreTime`, `ExpenseCorrectionsTest.theOfferIsThereJustBeforeSixSecondsAndGoneAtSixWithNothingAsked`, and the journey recreates the activity before pressing "Deshacer". D07/U01 to U05 → the cases named U01 to U05 in `UndoCompensationContractTest`; the device sleeping → `whenTheDeviceSleptPastTheTimeComingBackRemovesTheOffer`. Same operation replays safely; failure and cancellation keep a recoverable outcome → replay and cancelled-caller cases in both contract tests, `anEditWhoseOutcomeIsUnknownIsLookedUpAndNeverSentAgain`, `afterTheProcessWasKilledMidSaveTheOutcomeIsLookedUpNotSentAgain`, `anUndoThatFailedCanBeTriedAgainInTimeAsTheSameOperation`. Gesture and accessible menu paths → `CorrectionControlsTest` and `CategoryDetailScreenTest` (press, long press, swipe, named screen-reader actions). Deadlines and receipts explicit → the notice is read as "Gasto borrado. Puedes deshacerlo durante 6 segundos."; the journey asserts the receipt kinds in order. Historical correction discoverable, affected period explicit before saving, feedback after the committed receipt → the line in the list (`theListSaysHowAnExpenseIsCorrectedOrDeleted`), `anExpenseOfAnEarlierMonthSaysWhichMonthsWouldChange`, `itSaysWhichOtherMonthsFiguresWouldChangeBeforeAnythingIsSaved`, and "Gasto actualizado" only after `Committed`. Existing create journey still works → the 144 app device tests that existed before pass unchanged.
- **Red / Green:** each group was written before its code and seen failing for the intended reason, then passing. First pull request: 36 real-database tests red against stubs that answered "conflict" and an empty migration ("Migration didn't properly handle: deleted_expense"). This one: `ExpenseCorrectionsTest` 13 of 15 red against a class that did nothing; `ExpenseEditViewModelTest` 17 of 18 red against the form without edit behaviour; 3 new `CategoryDetailViewModelTest` cases red; `CorrectionControlsTest` 11 of 11 red on the emulator against components that drew nothing; 18 screen and journey cases red before the screens were wired. Four of my own expectations were corrected on the way and none weakened a contract: a miscounted fixture, a notice test that compared the dock's padded edge, a swipe test that counted the ordinary press a drag across a button makes, and the swipe itself, first built on a Material component whose callback fired repeatedly and replaced by a plain drag. Written after their code and passing first time: seven real-database cases in the first pull request, three of them then checked by removing the guarded code.
- **Gate results:** local `./scripts/verify-ticket SELLO-019 --gate G2 --retain` passed: 403 host tests (253 app, 101 domain, 37 design-system, 9 data, 3 catalog), 406 device tests on an API 30 emulator (163 app, 155 data, 78 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 29 warnings, the same number as before. An earlier run failed on ktlint alone, three lines one character too long, with every test passing; the run recorded below is the third to pass, repeated because the progress notes written after the second counted as changed inputs. Schema version 2 was added by the first pull request with its exported schema and migration tests; no dependency, lock file or permission changed.
- **Quality run:** run 20261012T210243Z-26e28205; SELLO-019 G2 passed; HEAD 422cc50, inputs sha256 93a3988ffca7; report docs/planning/mvp/quality-reports/SELLO-019.json sha256 ddc322f9caa30c4c128b1955c529c81b0b1d3f2a677fa933eb9248a464afddaf
- **Device / Artifact:** the debug app on an API 30 emulator (320×640 at density 1, the default of the image created for this session; Cobalto light, font scale 1.0), 2026-10-12, a build of the working tree before the commit, driven by a throwaway device test that was not committed, on synthetic data. [The list with its line of help](../../../testing/evidence/SELLO-019/01-list-with-hint.png), [the long-press menu](../../../testing/evidence/SELLO-019/02-long-press-menu.png), [the form opened on an expense](../../../testing/evidence/SELLO-019/03-edit-unchanged.png), [after a change](../../../testing/evidence/SELLO-019/04-edit-changed.png), [deleted, with the offer](../../../testing/evidence/SELLO-019/05-deleted-with-undo.png) and [restored](../../../testing/evidence/SELLO-019/06-restored.png). Not executed by hand or on a device: the process being killed with the form or an offer open (covered by saved-state and reopened-database tests, not by killing the installed app), TalkBack, a keyboard, twice the font size, a wide window with the notice, a phone-sized screen, and the device sleeping during the six seconds.
- **Review:** executor self-review of the diff, captures, reports and logs. The merge of this ticket's pull request is the project owner's acceptance, including the listed choices and what is not delivered here; the pull request and its checks are that record. No independent technical review.

## SELLO-020 — Complete category management and month-specific budget editing

- **Type:** Story
- **Priority:** P0
- **Status:** In Progress
- **Depends on:** SELLO-013, SELLO-018, SELLO-008, SELLO-004
- **Gate:** G2

### Outcome
Users can maintain names/icons/limits and retire categories while history stays meaningful.

### Deliverables
Real category edit and change-limit flows, explicit target-month display, archive
confirmation with affected counts, historical/archived presentation and restored
draft/version conflict handling. Category long-press actions have accessible equivalents.

### Acceptance criteria
- Current-month changes never mutate prior limits; rename updates presentation
  without moving expenses to another identity. Archived history remains readable.
- Archive removes category from new-entry chips but does not cascade history.
  No destructive aggregate-delete capability is exposed unless separately accepted.
- Implement approved AR01–AR06, explain current/future-month budget consequences
  before archive/unarchive confirmation, and keep archived rows with saved limits or
  expenses visible in relevant periods. Reuse original identity on restore/rename.
- Draft conflict includes reload/retry choice; “Guardar cambios” requires a confirmed
  receipt. Removing the final active category returns Anotar to the honest prerequisite.
- Announce that previous months' limits can be corrected; retain accessible help
  after dismissal. Historical editor names the target month and explains that other
  configured limits/defaults are unchanged. Confirmed save explains refreshed totals.

### Tests
Device rename/change-limit/archive with prior-month reconciliation; stale edit,
last-category, empty/archive states and process restoration tests; G2.
Cover H06 discovery/help before and after dismissal, explicit historical target/scope,
TalkBack/large-font readability, no success on failed/unknown commit, and receipt-backed
completion feedback. Extend catalog examples for applicable informational states.
Run archive/unarchive across month boundaries, preserved current/override limits,
historical rename and zero-expense archived-budget visibility with exact reconciled totals.

### Working checkpoint
Previously recorded expense/detail remains readable after archival; new entries
cannot accidentally select archived categories. Update related catalog examples.

### Context and starting points
Users need to maintain categories without losing source history or rewriting past
budgets. Inputs are actual category ports from SELLO-013, current detail SELLO-018
and approved D04/D05/D07. Read
[category editor](../../../design/sello-spec.html#s-catform) and archive/budget example H01.

### Implementation plan
1. Reuse category editor controls but distinguish create/edit/month-limit mode;
   load stable category ID, row version and explicit selected month from navigation.
2. Add rename/icon/change-limit actions with validated commands and durable receipt
   handling. Show which month is being changed before confirmation/submission.
   Add contextual historical-edit announcement plus retained month/editor help using
   resource copy; after confirmed commit identify the updated period. Explain any
   later approved carryover consequences rather than promising unapproved isolation.
3. Implement archive confirmation with real affected counts, preserve historical
   identity/expenses/limits, and refresh eligible-entry chips through observation.
4. Handle stale editor/deleted row by explicit reload/keep-draft choice; save cannot
   silently upsert a replacement identity. Apply approved archived-month budget semantics.
5. Add accessible long-press/menu equivalents and retained detail/history access,
   including the last-active-category path back to creation prerequisites.

### Concrete cases and pitfalls
- Rename C from Café to Comida: historical rows remain attached to C, not matched
  by translated/name text. Archive C does not remove its September expenses.
- October limit 120,000 cannot rewrite September's 100,000; H01 remains 80,000.
- A form selected before archival must fail at the command boundary, not save into
  an ineligible category because its chips were once loaded.

### Verification recipe
Create/run `*CategoryManagementViewModelTest` and real device CategoryManagementJourneyTest;
G2. Verify rename/month-specific limit/archive/stale draft/restart and retained source
history. Archive-only MVP has no implemented or fake destructive aggregate delete.

### Execution progress
2026-10-12: started on branch `sello-020-category-management` after SELLO-019 merged.
The storage commands for every change here already existed (SELLO-013); this ticket
adds no command, schema, dependency or permission.

**Decisions taken with the project owner before any code**

- A name another category already has is announced while it is typed, in the new
  category form as well, not as the error of a save. The button stays off until the
  name changes.
- Changing name, icon and limit together is three separate commands with an honest
  partial result, not a new combined command.
- In the current month the limit applies from this month onwards; there is no "only
  this month" choice for the month in progress.
- Archiving is an action in the form's top bar, with a confirmation; a long press on
  a category in Recibo opens the form.
- The dismissible first-time announcement that a previous month's limit can be
  corrected is deferred to SELLO-023, which owns remembered preferences; a note is on
  that ticket. This ticket ships the help that is always in view.

**Delivered**

- `CategoryManagementViewModel` and the edit mode of the category form: rename, icon,
  the limit of the month it was opened from, conflict with reload or keep-draft,
  archive and unarchive with a confirmation that states the consequences and counts.
- Entry points: "Editar categoría" on a category's detail; a long press or a
  screen-reader action on its cell in Recibo.
- A line on a finished month's detail saying its limit can still be corrected.
- Design system: `CategoryCell` takes actions, offered by long press and by name.

**Choices made while implementing, for review**

- A month that has a limit cannot be left without one from this form; there is no
  command that removes a limit.
- An archived category's limit cannot be changed from the current month on (the
  command refuses it); a finished month's still can. Its name and icon stay editable.
- "Conservar mis cambios" after a conflict reads the category again and keeps the
  draft, so the next save is knowingly over the newer version.
- Archiving from a form with unsaved changes discards them, and the confirmation says so.
- The long press in Recibo offers "Editar categoría" only; archiving is inside the form.
- One existing assertion changed on purpose: a form showing a taken name used to keep
  its button on, and now keeps it off.
- Texts that were not agreed beforehand are listed in the pull request.

**Not delivered here, and where it goes**

- Archive and unarchive across a month boundary in the installed app: that needs
  simulated financial time (SELLO-024). The approved cases AR01 to AR06 are covered
  against a real database by `MonthlyBudgetHistoryTest` since SELLO-013.
- The dismissible announcement: SELLO-023.

## SELLO-021 — Ship income recording, history and guarded correction workflows

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-012, SELLO-015, SELLO-008, SELLO-019, SELLO-004
- **Gate:** G2

### Outcome
Income is a complete real workflow, not a summary number populated by fixtures.

### Deliverables
Income-specific validated commands/read port using the existing operation protocol;
amount/source/date/note entry, receipt/history, edit/delete/undo and restored drafts.
Stable source kinds are separate from translated labels and optional free text.

### Acceptance criteria
- Dock remains expense-first; an explicit income action opens the actual income form.
  No recurrence or batch options appear. COP/current-or-past date policy is consistent.
- Duplicate/canceled saves recover their original receipts; income updates do not
  touch expenses or budgets. Negative/zero invalid income rejects original input.
- Income deletion and edits use guarded versions/compensation, not a copied unsafe
  implementation; source rename/copy changes cannot change classification identity.
- Use the same approved D07 six-second real-time offer and process-death expiry /
  durable outcome recovery as expense deletion; cover U01–U05 for income too.
- Historical income correction includes contextual/persistent guidance, an explicit
  effective date/affected month and receipt-backed completion feedback (D04/H06).

### Tests
Real-Room income operation contracts and cross-type isolation; ViewModel/Compose
entry-history-edit-delete-undo-restart tests, source-key and exact amount checks; G2.

### Working checkpoint
Expense and income journeys coexist, persist independently and are reachable without
debug data. Extend applicable catalog states and sandbox command scripts.

### Context and starting points
Income powers Resumen but is not a refund/account-transfer classifier. Use the
operation/undo contracts from SELLO-012/019, entry controls and approved source keys
(D05). Read [income](../../../design/sello-spec.html#s-ingresos), Anotar and I01.
There is no bank/capture import to reinterpret as income.

### Implementation plan
1. Add income-specific create/edit/delete/read/undo ports and transactional adapters;
   reuse protocol/policies, not a universal expense/investment form with mode booleans.
2. Store positive COP amount/effective date/stable source kind and optional free text/
   note, audit/version/sequence; translated source labels live in app resources.
3. Add explicit income choice from Anotar and history route; dock still opens expense.
   Implement raw draft, busy/recovery/result states and same-operation receipt handling.
4. Add bounded ordered income history, editor and guarded delete/undo using D07;
   expose source values for later snapshot/Resumen rather than calculate budgets here.
5. Add restorable draft/ID UI tests and accessible state fixtures. Existing expenses
   and category budget writes must remain isolated from income commands.

### Concrete cases and pitfalls
- Create income 150,000 twice with one ID: one row. Same ID changed source/amount
  conflicts. Expense totals remain untouched.
- Changing Spanish source copy cannot change its stored classification; custom label
  is metadata, not a new enum inferred from text.
- Income edit/delete uses expected version and durable compensation; it is not safer
  merely because income is positive. Recurrence/batch actions remain absent.

### Verification recipe
Create/run `IncomeCommandContractTest`, `*IncomeFormViewModelTest` and IncomeJourneyTest;
G2. Verify create/list/edit/stale-delete/undo/restart, cross-type isolation and exact
source/date/receipt values through real Room, not a fake summary seed.

## SELLO-022 — Deliver basic Resumen with exact actuals and history charts

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-014, SELLO-018, SELLO-021, SELLO-009, SELLO-004
- **Gate:** G2

### Outcome
Users can inspect actual income/spending and understand planning allocation without account-balance claims.

### Deliverables
Resumen render models/screens/Root, actual monthly spending/income/net cash flow,
budget destination/undestined explanation, basic exact spending-history/category
charts and source history links. Enable the second real tab with restored stacks.

### Acceptance criteria
- Figures reconcile to same-revision expense/income history; net cash flow and
  undestined planning capacity are distinct. No goals/investment contributions are
  fabricated; actual verdict is labeled separately from Recibo's projected verdict.
- Approved presentation: emphasize Ingresos menos gastos in Resumen; keep Restante
  del presupuesto/daily allowance prominent in Recibo and Por asignar al presupuesto
  as secondary planning (replaces Sin destinar). Explain income-minus-expenses versus
  income-minus-limits without generic Disponible/account-balance claims. Preserve
  negative allocation; expense-only corrections leave allocation unchanged.
- Shared month and tab stacks survive switches/restoration. Empty history produces
  truthful empty states, not invented comparison trends or zero on read failure.
- Chart selection/text values match exact source totals even at large amounts.
  No analytical export, detailed record/comparison or unsupported navigation exists.

### Tests
Approved allocation/actual verdict examples, chart/source agreement, tab/month/back
restoration and device entry→summary→restart journeys; all four themes and G2.

### Working checkpoint
Recibo and Resumen are both fully functional product tabs; each link points to an
implemented workflow. Sandbox can now demonstrate real graph growth.

### Context and starting points
Resumen explains actual cash flow and planning allocation; neither is a bank balance.
Use monthly snapshot contracts from SELLO-014, income history from SELLO-021 and
chart primitives. Read [Resumen](../../../design/sello-spec.html#s-resumen), approved
scope/examples I01/G01, and architecture §4. Detailed comparisons/records/export are deferred.

### Implementation plan
1. Extend/read a same-revision snapshot containing actual expense/income totals and
   approved budget allocation; define net flow and undestined as distinct render values.
2. Build Resumen State/ViewModel/Root/Screen with actual verdict, monthly history/
   category chart inputs and source-income/category links to implemented routes.
3. Preserve exact series values/IDs through selection and accessible text. Projection
   labels stay distinct from the actual-spend verdict and never create financial records.
4. Enable the real second tab, retained stacks/month/back/reselection; remove temporary
   one-tab scaffolding without adding Patrimonio or broken future action links.
5. Implement loading/empty/error/last-good and insufficient-history states; add previews
   and any newly required reusable component example states.

### Concrete cases and pitfalls
- I01 shows net cash flow 120,000 and undestined 50,000, not two copies of the same
  number. No goal/investment amount exists in MVP to fill allocation fields.
- G02's corrected expense changes graph total 30,000→27,000, remaining 70,000→73,000
  and net flow 170,000→173,000; Por asignar al presupuesto stays 100,000. Test numeric
  independence, accessible labels and screen hierarchy; updated labels do not change formulas.
- G01 cumulative chart is 10,000/25,000/30,000; selected values/text equal source
  rows even when geometry uses approximate coordinates.
- Resumen and its charts are further consumers of the same month's snapshot, which
  each screen observes separately today. Sharing one observation was parked in
  SELLO-055 (2026-10-12) as not worth its risk for two screens; decide it again here,
  with measurements, before adding more.
- Month switches cannot combine October income with September budgets; no-history
  cannot produce an invented comparison trend or a zero on failed reads.

### Verification recipe
Create/run `*ResumenSnapshotTest`, `*ResumenRenderModelTest` and ResumenJourneyTest;
G2. Compare independently totaled Room rows with slips/chart/selected text, test
tab/month restoration and read faults, and record every exposed link's real destination.

## SELLO-023 — Ship persisted appearance, ordering and essential settings

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-006, SELLO-015, SELLO-020, SELLO-004
- **Gate:** G2

### Outcome
Users can personalize the MVP through a settings surface without financial responsibilities leaking into it.

### Deliverables
Typed DataStore preference adapter/neutral contracts, Sistema/Claro/Oscuro and
Cobalto/Violeta controls, category order/direction with deterministic tie-breaks,
and settings behind Recibo's gear. Data actions become visible only as their tickets ship.

### Acceptance criteria
- Theme changes apply consistently and survive restart; preference errors are visible
  without invalidating committed money. Financial zone/limits remain in Room.
- Every supported category order uses actual data; unlimited categories remain last.
  No investment currency, recurrence, notification or debug controls appear in release.
- Approved D06 defaults/metrics/directions/ties match O01–O14 in the versioned
  corpus. Más usadas counts selected-month expenses, not lifetime count or amount;
  latest activity means last added expense, not configuration or expense modification.
  Último gasto agregado uses newest original real creation Instant among selected-month
  nondeleted expenses; edits/restore preserve it, deletion falls back, and no-expense
  rows stay last within each partition even when reversed. Old configuration ordering
  is superseded. Unlimited-last and
  alphabetical/ID ties are not reversed with the selected metric direction.
- Settings uses narrow collaborators, not a universal manager/ViewModel; drafts
  and selected month are not cleared by appearance changes.

### Tests
DataStore codec/default/failure/restart tests, sorting ties and archive interactions,
device theme/order/settings back-stack tests; G2. Extend catalog preference UI states.
Execute all O01–O14 orders, cross-month selection and reversed-direction/tie cases;
financial-time travel without expense additions and with fixed selection cannot
count as a new addition. Include O09–O14 backdated/edit/delete/restore/empty/time-tie
fixtures, preserving original creation time and reversing only metric comparison.

### Working checkpoint
Daily-use workflows remain functional in all four themes; restart preserves selected
preferences without changing financial data or formulas.

### Context and starting points
Preferences should not make settings a financial transaction coordinator. Use
neutral contracts from SELLO-005, theme from SELLO-006, real categories and approved
D06/D09. Read [settings](../../../design/sello-spec.html#s-config), architecture §§2/7
and the guide's DataStore source. Rate/currency/notification settings are not MVP.

Carried over from SELLO-019 (2026-10-12, project owner): the dismissible first-time
announcement that a past expense can be corrected (D04/H06) needs a remembered
"seen" preference, so it is delivered here. SELLO-019 ships the guidance that is
shown in context every time.

Carried over from SELLO-020 (2026-10-12, project owner) for the same reason: the
dismissible announcement that a previous month's limit can be corrected. SELLO-020
ships the help that stays in view; this ticket adds the announcement and remembers
that it was dismissed, for both.

### Implementation plan
1. Define typed theme/ink/order/direction preferences and stable codecs in data;
   implement DataStore observation/update with explicit failure/default behavior.
2. Wire the adapter only in composition; screen/ViewModel consume neutral preference
   ports and never serialized UI enum names or direct DataStore keys.
3. Add gear-accessible settings UI and deterministic selected-month category ordering
   from approved D06, keeping unlimited last and stable tie-breaking.
4. Apply theme immediately through the root while preserving forms/month/back state;
   expose save/read failure and retry without invalidating financial receipts.
5. Prepare settings data-action slots through actual route availability. Backup/reset
   buttons appear only when those workflows land; debug tools appear only in debug sources.

### Concrete cases and pitfalls
- Sistema/Cobalto is the default; choose Oscuro/Violeta, restart and verify both.
- Equal metric values require the approved tie-break, not nondeterministic SQL order.
- Invalid nonfinancial preference handling is distinct from corrupt financial data;
  do not default a broken currency/budget to a UI setting or write through Room.

### Verification recipe
Create/run `*PreferencesCodecTest`, `*CategoryOrderingTest` and SettingsJourneyTest;
G2. Inject DataStore failure, verify changed theme/restart/order and untouched actual
financial rows/drafts; test every approved ordering metric with explicit fixtures.
