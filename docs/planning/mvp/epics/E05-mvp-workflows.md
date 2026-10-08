# SELLO-E05 — Complete everyday MVP workflows

- **Status:** Backlog
- **Goal:** Complete maintainable expense/income history and an honest monthly summary.
- **Exit:** Edits/deletes/undo, category/budget management, income, basic Resumen and preferences all work after restart and preserve prior history.

## SELLO-019 — Implement expense edits, deletion and guarded six-second undo

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
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

## SELLO-020 — Complete category management and month-specific budget editing

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
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
- Approved D06 defaults/metrics/directions/ties match O01–O08 in the versioned
  corpus. Más usadas counts selected-month expenses, not lifetime count or amount;
  last update uses real category/configuration audit time. Unlimited-last and
  alphabetical/ID ties are not reversed with the selected metric direction.
- Settings uses narrow collaborators, not a universal manager/ViewModel; drafts
  and selected month are not cleared by appearance changes.

### Tests
DataStore codec/default/failure/restart tests, sorting ties and archive interactions,
device theme/order/settings back-stack tests; G2. Extend catalog preference UI states.
Execute all O01–O08 orders, cross-month selection and reversed-direction/tie cases;
financial-time travel without configuration edits cannot change last-update ordering.

### Working checkpoint
Daily-use workflows remain functional in all four themes; restart preserves selected
preferences without changing financial data or formulas.

### Context and starting points
Preferences should not make settings a financial transaction coordinator. Use
neutral contracts from SELLO-005, theme from SELLO-006, real categories and approved
D06/D09. Read [settings](../../../design/sello-spec.html#s-config), architecture §§2/7
and the guide's DataStore source. Rate/currency/notification settings are not MVP.

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
