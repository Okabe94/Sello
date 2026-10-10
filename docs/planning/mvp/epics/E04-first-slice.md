# SELLO-E04 — First complete financial walking slice

- **Status:** In Progress
- **Goal:** Turn the greeting into a usable category → expense → receipt → Recibo journey.
- **Exit:** Users can start empty, create a category, save an expense once, and see truthful persisted budget/detail data after restart.

## SELLO-015 — Introduce the app shell, month session and restored navigation

- **Type:** Story
- **Priority:** P0
- **Status:** In Progress
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

## SELLO-016 — Deliver first-run category creation and amount-entry prerequisites

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
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

## SELLO-017 — Ship Anotar expense and its durable receipt end-to-end

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
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
