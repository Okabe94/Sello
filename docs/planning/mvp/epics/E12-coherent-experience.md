# SELLO-E12 — Coherent Sello experience and navigation redesign

- **Status:** Backlog
- **Increment:** MVP
- **Goal:** Restore a coherent answer-first Sello experience across Recibo, detail, entry, confirmation, Resumen and Configuración while preserving exact financial truth, accessibility and approved navigation contracts.
- **Exit:** The approved experience architecture is recorded; every adopted redesign ticket has meaningful red→green coverage and fresh retained G2 evidence; the integrated adaptive/accessibility/navigation matrix passes; and no contested product contract is changed without owner approval.

Registered 2026-10-15 from the owner's [`ui_issues.md`](../../../../ui_issues.md),
the [emulator audit](../../ui-ux/reports/2026-10-15-ui-ux-audit.md), the approved
[visual reference](../../../design/README.md), architecture and the merged current
experience/navigation inventory in [PR 47](https://github.com/Okabe94/Sello/pull/47).
This registration is planning only: it does not approve a redesign, establish that
every subjective report is a defect, deliver a fix or claim device/reviewer acceptance.

## Design and decision boundaries

Sello is a financial notebook, not a generic dashboard. Figures live on paper; the
answer comes first and explanation follows the tear line; hierarchy, icon and position
precede prose; adding is one motion; and no screen carries more than one large verdict
stamp. Text remains at least 12sp, targets at least 48dp, motion reducible, and exact
money available to accessibility services. Recibo and Resumen are the two MVP tabs.

The audit deliberately separates defects from product reconsiderations. The approved
reference fills a category circle with ink as spending grows. Architecture requires a
basic Resumen with actual totals, charts, projection and shared budget verdict, while
[ADR 0010](../../../decisions/0010-income-resumen-entry.md) makes Resumen the working
income/history entry point. Receipts currently communicate confirmed commits and
recovery truth. SELLO-061 must ratify replacements before dependent tickets reverse,
remove or relocate those behaviors. No implementation ticket may treat the owner's
desired direction as pre-approved merely because it is recorded here.

## Severity and request classification

Each child has one primary request class. **Actual bug** requires reproduced behavior
that violates an existing usable/accessibility contract. **Feature change request**
changes product behavior, navigation or an approved contract even when motivated by a
current usability problem. **UI change request** changes presentation and shared visual
language without changing financial meaning or destination capability.

Severity is ordered within each class. P1 is a blocking decision or high-impact
journey/input failure; P2 is important discoverability, consistency or presentation
work with a usable path still available. Neither label represents a security incident.

### Actual bugs — severity order

| Severity order | Ticket | Priority | Reproduced defect |
| --- | --- | --- | --- |
| 1 | SELLO-065 | P1 | Entry forms lose usable context under IME/preview insertion, use the wrong historical-month default and contain a dark-theme contrast failure. |
| 2 | SELLO-063 | P2 | Recibo hides its add-category action as the collection grows and does not visibly expose archived/unlimited state. |

### Feature change requests — severity order

| Severity order | Ticket | Priority | Requested behavior change |
| --- | --- | --- | --- |
| 1 | SELLO-061 | P1 | Ratify the contested contracts before implementation changes approved behavior. |
| 2 | SELLO-062 | P1 | Change shell/dock scope, destination hierarchy and expense/income entry structure. |
| 3 | SELLO-067 | P1 | Redefine the question, information and navigation provided by Resumen. |
| 4 | SELLO-064 | P2 | Change category browsing, transaction actions, swipe behavior and icon vocabulary. |
| 5 | SELLO-066 | P2 | Change the post-save receipt/confirmation lifecycle. |

### UI change requests — severity order

| Severity order | Ticket | Priority | Requested presentation change |
| --- | --- | --- | --- |
| 1 | SELLO-068 | P2 | Align hierarchy, affordances and state treatments through reusable Sello patterns. |
| 2 | SELLO-069 | P2 | Complete and prove the integrated adaptive/accessibility UI acceptance pass. |

Dependencies, not table position alone, determine execution eligibility. Product
approval can change feature/UI scope. Implement sequentially where tickets share
shell, route, form or component composition.

## Shared execution boundaries

- Every child is Backlog, depends directly or transitively on SELLO-004 and requires
  G2. Registration is not readiness. Use the execution guide, ticket executor brief
  and QUALITY_FLOW.md; a final implementation needs its own fresh retained run.
- SELLO-061 owns approval and resulting ADR/architecture changes. Until it completes,
  category fill direction, the Resumen content contract, income entry location,
  receipt lifecycle and keypad strategy remain as implemented/approved.
- SELLO-062 overlaps the still-Backlog SELLO-060 transition scope. Refinement must
  record whether SELLO-060 is completed first, narrowed or explicitly superseded;
  do not implement and close the same acceptance twice.
- SELLO-058 already removed the finite-empty category default and SELLO-059 already
  added “Mes actual” inside the picker. SELLO-046 addressed expense keypad movement.
  This epic must not reopen those Done changes without a demonstrated regression.
- Financial commands, exact money, original-input validation, operation IDs,
  generation checks and commit recovery are constraints, not UI details to relax.
  No schema, dependency, permission, backup policy or destructive behavior change is
  authorized by this epic unless separately approved and recorded.
- Shared component work belongs in design-system and its actual catalog; financial
  rendering/state belongs in presentation; navigation/composition owns route wiring.
  Debug scenarios remain synthetic and absent from customer artifacts.

## SELLO-061 — Ratify the experience architecture and contested contracts

- **Type:** Task
- **Source classification:** Feature change request
- **Priority:** P1
- **Severity rationale:** Blocks every contract-changing feature/UI request; implementing those requests first could remove working financial or navigation behavior.
- **Status:** Backlog
- **Depends on:** SELLO-004, SELLO-006, SELLO-009, SELLO-015, SELLO-017, SELLO-021, SELLO-022, SELLO-023
- **Gate:** G2
- **Source report:** [2026-10-15 UI/UX audit](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
The team has one approved experience architecture that answers the audit's contested
questions without contradictory implementation tickets or silent architecture drift.

### Deliverables
An owner-reviewed decision record defining: the question answered by Recibo and
Resumen; whether category ink fills or empties; the home and route for income;
expense/income route separation; global dock scope; custom versus platform keypad;
and whether confirmed receipts remain full screens, become compact confirmed states,
or close automatically. Update architecture, ADR 0010, the visual reference and
navigation documentation only where the decision actually changes their authority.
Include a before/after journey map and explicit non-goals.

### Acceptance criteria
- Every contested item has one choice, rationale, owner and affected contract named.
- Resumen and income still have a truthful reachable MVP path; no working capability
  disappears while its replacement is hypothetical.
- Receipt treatment waits for confirmed commit, distinguishes unknown outcome, and
  defines create versus edit behavior and restoration.
- Category progress meaning is unambiguous visually and semantically, including
  unlimited/archived categories and over-limit states.
- The decision keeps exact money, two-tab MVP scope, non-gesture access, 12sp/48dp,
  large text, reduced motion and private storage constraints explicit.
- Dependent tickets are reconciled with the decision; contradictions become recorded
  scope changes rather than acceptance silently rewritten during implementation.

### Tests
This is a product/architecture decision. Validate links and planning structure; walk
the proposed journey map against current routes, real financial commands, restoration
and the audit evidence. Prototype only where needed to compare concepts, with no
prototype fixture or route shipped as completion evidence.

### Working checkpoint
The current app remains unchanged and usable while the approved record makes SELLO-062
through SELLO-068 implementable without further policy invention.

### Context and starting points
Read architecture §§1, 6 and 9, ADR 0010, the design reference's principles,
accessibility and motion sections, PR 47's current-experience/navigation documents,
and the audit's conflict classifications. The owner's inventory is an input, not by
itself an override of those sources.

### Implementation plan
1. Turn each contested audit statement into a concrete alternative and constraint set.
2. Map alternatives onto the current routes, domain truth and recovery behavior.
3. Review a compact journey/wireflow with the owner, including sparse/error/restored states.
4. Record the approved choice and update only the authorities it changes.
5. Reconcile dependent ticket wording/dependencies and regenerate board views.

### Concrete cases and pitfalls
Do not equate fewer screens with fewer states, remove receipts before defining unknown
commit handling, reverse a progress metaphor without accessible wording, or remove
income/budget content without a replacement route and architecture update. Do not
use mocked financial success in a decision prototype.

### Verification recipe
Run Markdown/link review, `python3 docs/planning/mvp/board.py --check`, planning tests,
architecture rules and the approved journey walkthrough. For the final ticket, run
`./scripts/verify-ticket SELLO-061 --retain`; record the fresh G2 Quality run even if
the production UI remains unchanged, because its real routes/device checkpoint are
part of acceptance.

## SELLO-062 — Rework shell context, navigation and entry structure

- **Type:** Task
- **Source classification:** Feature change request
- **Priority:** P1
- **Severity rationale:** Changes primary action availability and financial-entry routes across most top-level journeys; an incoherent migration could make working entry paths unreachable.
- **Status:** Backlog
- **Depends on:** SELLO-061, SELLO-004, SELLO-015, SELLO-021, SELLO-023
- **Gate:** G2
- **Source report:** [H6, E2, E4 and S1–S2](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
Every destination communicates where the person is, which month is relevant and what
the primary action does; expense and income open through the approved structure; and
transitions never expose a misleading intermediate shell.

### Deliverables
Implement the approved shell/dock scope; make Configuración a clearly nested,
non-month-scoped destination without an unrelated expense action; implement the
approved expense/income route separation and keypad strategy; and stabilize
Recibo/Resumen enter, Back and restoration behavior. Define normal and reduced-motion
transition states. Reconcile the overlap with SELLO-060 explicitly before editing
shared transition code. SELLO-068 owns visual styling of the dock and month state.

### Acceptance criteria
- Configuración has Back, stable identity and only context-relevant controls; appearance
  settings do not appear scoped to a selected financial month.
- The global dock appears only in approved contexts and routes to the approved financial
  entry kind without leaving a stale local mode behind.
- Expense and income have the approved typed route/entry point; switching, Back and
  restoration cannot reinterpret, silently save or discard a draft.
- Recibo ↔ Resumen has no blank/mixed intermediate surface; interrupted navigation and
  rapid Back do not duplicate routes, actions or financial commands.
- Shared month and each tab's saved stack restore according to the approved map across
  recreation and process restoration; reduced motion reaches the same end state immediately.

### Tests
Start with red shell/navigation tests for Configuración context, dock presence/absence,
typed entry destinations, draft preservation, Back ordering and restored tab/month
state. Use controlled animation clocks for intermediate/end states and device
recordings for normal/reduced motion; no arbitrary sleeps.

### Working checkpoint
Recibo, Resumen and Configuración remain reachable, Back is predictable, and the
existing create→receipt→Recibo journey works with the revised shell.

### Context and starting points
Begin with `SelloApp.kt`, `Routes.kt`, `ShellViewModel.kt`, `MonthSession.kt`,
`EntryDestination`, `SelloScaffold`, `EntryDock`, the PR 47 navigation graph and
SELLO-060's parked findings. Confirm actual route/state ownership before changing
composition.

### Implementation plan
1. Ratify the destination/shell matrix from SELLO-061 and resolve SELLO-060 ownership.
2. Add red state/navigation tests for each affected destination and entry kind.
3. Separate financial month/dock concerns from nested nonfinancial destinations and
   migrate expense/income drafts to the approved typed structure.
4. Implement transition and reduced-motion behavior using Sello motion tokens.
5. Exercise recreation, process restoration, Back and ordinary financial entry on device.

### Concrete cases and pitfalls
Do not create a third tab, reset shared month on Back, reinterpret an expense draft as
income, replay a pending deep link, hide a loading/read failure behind motion, or infer
settings scope from whichever tab happened to open it.

### Verification recipe
Run focused navigation/state/Compose tests, compact and expanded device journeys at
normal and reduced motion, then `./scripts/verify-ticket SELLO-062 --retain`. Record
the final transition capture and fresh G2 report; assembly is not navigation acceptance.

## SELLO-063 — Keep Recibo actions and category states reachable

- **Type:** Task
- **Source classification:** Actual bug
- **Priority:** P2
- **Severity rationale:** Category creation remains possible after scrolling and state exists in accessibility text, but the visible action/state communication fails as the collection grows.
- **Status:** Backlog
- **Depends on:** SELLO-004, SELLO-015, SELLO-019, SELLO-023
- **Gate:** G2
- **Source report:** [H3 and H7](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
Recibo remains an immediate monthly answer as the category set grows, with adding and
important category states discoverable without decoding accessibility-only metadata.

### Deliverables
A persistently discoverable add-category action and visible non-color-only archived
and unlimited treatments using the approved existing collection/progress behavior.
Preserve ordering, selection, exact spent/limit values, loading/empty/error/stale
states and compact/expanded composition. SELLO-064 owns requested browsing or progress-
metaphor changes; this ticket fixes the reproduced reachability/state defects.

### Acceptance criteria
- With zero, one, nine and a reviewed high category count, the monthly answer and a
  reachable add action remain clear without overlapping the dock/navigation/IME.
- Archived, unlimited, finite, over-limit and selected states are visually distinct,
  announced accurately and never communicated by color alone.
- Existing collection scrolling, screen-reader focus and restored selection remain
  predictable; no new browsing model is introduced as a bug fix.
- Category creation, editing, archive flow and detail navigation still work.

### Tests
Red Compose/layout tests reproduce the hidden add action and indistinguishable visible
states. Cover collection counts, order changes, compact/short/expanded windows,
1.0/1.3/2.0 font, all themes, restored scroll/selection and screen-reader descriptions.
Use device journeys for real category commands.

### Working checkpoint
The current monthly Recibo answer, create/edit/archive and expense-detail journeys
remain usable with a large synthetic category set.

### Context and starting points
Inspect Recibo screen/root/state, `CategoryGrid`, `CategoryCell`, `CategoryCircle`,
SELLO-058/059 delivery evidence and the audit's before/after-scroll pair. Resolve real
available viewport/insets rather than assuming the captured phone height.

### Implementation plan
1. Turn the two reproduced defects into red layout/visible-state tests.
2. Implement reusable action/status primitives in design-system where warranted.
3. Recompose Recibo so its answer and add action survive collection growth.
4. Preserve ordering/selection/restoration while exercising real category commands.
5. Validate visual and spoken states across adaptive/theme/font configurations.

### Concrete cases and pitfalls
Do not solve discoverability with a second conflicting CTA, collapse exact values into
color, treat archived as deleted, give unlimited a fabricated limit, or silently change
circle-fill meaning/collection interaction. Synthetic audit names are not copy examples.

### Verification recipe
Run focused category component/Recibo tests and real create/edit/archive journeys,
then `./scripts/verify-ticket SELLO-063 --retain`. Record the zero/nine/high-count and
archived/unlimited device matrix with fresh G2 evidence.

## SELLO-064 — Revise category browsing and transaction interactions

- **Type:** Task
- **Source classification:** Feature change request
- **Priority:** P2
- **Severity rationale:** Existing browsing and edit/delete alternatives work, but the requested model changes interaction behavior and icon capability across category journeys.
- **Status:** Backlog
- **Depends on:** SELLO-061, SELLO-063, SELLO-004, SELLO-019, SELLO-020
- **Gate:** G2
- **Source report:** [H1–H2 and C3–C5](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
Category browsing and detail use the approved progress, collection and transaction-
action models, with a broader but coherent category vocabulary.

### Deliverables
Implement the SELLO-061 category-progress direction and a reviewed scalable collection/
scroll model; adopt one primary transaction action model plus an accessible non-gesture
equivalent; repair or remove swipe behavior according to that model; and expose an
expanded, curated icon vocabulary through the shared picker/catalog. SELLO-068 owns
the final row/edit-action visual hierarchy.

### Acceptance criteria
- The collection remains predictable with screen reader, keyboard/Switch Access and
  restored selection at zero, one, nine and a reviewed high category count.
- Category progress follows the approved financial metaphor and exact ratios, including
  zero limit, unlimited and over-limit boundaries; visible figures remain exact.
- Edit, delete, guarded undo and destructive confirmation have one coherent hierarchy;
  TalkBack/Switch Access never depend on swipe or long press alone.
- Partial/cancelled gestures always settle to a valid state if swipe remains; rapid
  actions cannot target the wrong transaction or submit twice.
- Icon choices are meaningfully broader, uniquely described, selected visibly and
  usable at 2.0 font scale without adding an unreviewed asset/dependency source.

### Tests
Red collection/progress behavior cases plus gesture settle/cancel tests if retained.
Cover tap/edit/delete/undo identity, pagination/order changes, stale/missing rows,
rotation/restoration and accessibility actions. Snapshot/catalog tests cover the icon
set, selection and focus order; SELLO-068 owns full visual-theme acceptance.

### Working checkpoint
Real expense edit/delete/undo and category edit still reconcile to Recibo/detail after
restart; no transaction is mutated solely by opening or partially gesturing on a row.

### Context and starting points
Inspect Recibo `CategoryGrid`/`CategoryCircle`, category detail `ExpenseList`, shared
row/action components, correction coordinator and category editor `IconChoiceGrid`.
The audit did not reproduce the stuck half-swipe in one attempt; begin with a meaningful
failing case before claiming a fix.

### Implementation plan
1. Approve the progress, collection, primary/alternative action and icon contracts.
2. Add red behavior, identity and gesture-state tests against current behavior.
3. Implement collection/progress, action and icon-picker changes in their owning modules.
4. Reconcile edits/deletes/undo with real storage across restart and list changes.
5. Update catalog examples and verify screen-reader/keyboard/Switch Access paths.

### Concrete cases and pitfalls
Removing long press is not automatically simpler if swipe becomes gesture-only. Avoid
reversing the circle metaphor without updated semantics, losing selection during
reflow, index-based mutation after reorder, optimistic deletion that loses unknown
outcomes, or dozens of unlabeled near-duplicate icons.

### Verification recipe
Run focused detail/action/icon tests and real edit/delete/undo journeys, then
`./scripts/verify-ticket SELLO-064 --retain`. Include cancelled-swipe evidence whether
the accepted result is removal or repair, plus fresh G2 and catalog results.

## SELLO-065 — Fix expense and income entry layout, date and contrast defects

- **Type:** Task
- **Source classification:** Actual bug
- **Priority:** P1
- **Severity rationale:** Reproduced viewport movement can prevent amount completion, historical context produces an unexpected date, and dark-theme text becomes unreadable in core financial entry.
- **Status:** Backlog
- **Depends on:** SELLO-004, SELLO-017, SELLO-021
- **Gate:** G2
- **Source report:** [E1, E3 and I1–I4](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
On the approved routes and input controls, expense and income retain stable, visible
editing context from the first digit through confirmed submission.

### Deliverables
Default a new entry date according to approved selected-month rules; make amount,
preview, input controls, fields and primary action coexist predictably; and make note
plus conditional income-source fields IME-safe. Correct the dark-theme hint token and
preserve restorable drafts. SELLO-062 owns route separation/keypad product changes;
this ticket fixes root layout/date/contrast behavior without requiring that request.

### Acceptance criteria
- A new entry opened from a historical month uses the approved in-month default unless
  an explicit/restored date exists; invalid dates are rejected with original input shown.
- Empty, first-digit, valid, error, preview and busy states do not move keys under a
  stationary pointer or make amount/required controls unreachable.
- Note and “Otro” source editing remain usable with IME at compact/short windows and
  2.0 font; focus, scroll and submit behavior are deterministic.
- Text contrast passes in all four themes; hardware keyboard and Switch Access work.
- Exact validation, operation IDs, duplicate-submit prevention, cancellation and
  unknown-commit reconciliation remain unchanged in strength.

### Tests
First reproduce income first-digit clipping, dark contrast, historical-date default and
expense-note IME behavior. Add the not-yet-exercised income-note/“Otro” cases. Cover
geometry/focus with controlled insets, draft restoration, edit versus create,
invalid/overflow input, duplicate submit, cancellation/unknown outcome and a real save.

### Working checkpoint
Expense and income can each be created and edited through real commands, confirmed,
restored and read back exactly; existing deep-link expense drafts remain bounded and safe.

### Context and starting points
Inspect `EntryDestination`, `ExpenseEntryRoot/Screen`, `IncomeFormRoot/Screen`,
`AmountKeypad`, `FormField`, route definitions and SELLO-046 evidence before changing
shared geometry. Proposed route names are targets, not proof that APIs exist.

### Implementation plan
1. Add red regressions for every reproduced layout/date/contrast defect.
2. Apply the approved date rule and stabilize adaptive/IME geometry and conditional focus.
3. Preserve the current or subsequently approved input/route structure without duplicating commands.
4. Preserve saved drafts, operation identity and unknown-outcome reconciliation.
5. Exercise create/edit/restart for expense and income across input methods/themes.

### Concrete cases and pitfalls
Do not fix clipping by hiding the typed amount, moving submit beside digits, clearing a
draft on route change, reading the wall clock ad hoc, or falling back to today's date
when month/date resolution fails. Do not weaken exact money for any input method.

### Verification recipe
Run focused route/state/validation/layout tests, IME and font-scale device cases, then
real expense/income create/edit/restart journeys. Finish with
`./scripts/verify-ticket SELLO-065 --retain` and record fresh G2 plus contrast evidence.

## SELLO-066 — Unify post-save confirmation without losing commit truth

- **Type:** Task
- **Source classification:** Feature change request
- **Priority:** P2
- **Severity rationale:** Both receipt paths work and preserve commit truth; this request changes how long and where confirmed success is presented.
- **Status:** Backlog
- **Depends on:** SELLO-061, SELLO-065, SELLO-004, SELLO-017, SELLO-021
- **Gate:** G2
- **Source report:** [ER1 and IR1–IR2](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
Expense and income saves share one purposeful confirmation grammar that is as brief as
approved while always telling the truth about what committed.

### Deliverables
Implement the approved full-screen, compact or automatic confirmation lifecycle for
both transaction kinds; align hierarchy, stamp use, action placement and motion; show
transaction-specific exact facts only when useful; and define create versus edit,
Back, interruption, restoration and unknown-outcome behavior.

### Acceptance criteria
- Confirmation appears only from a confirmed receipt tied to the submitted operation;
  busy/unknown/failure cannot look successful or navigate away as if committed.
- Expense and income use one recognizable composition and motion grammar while retaining
  correct signs, labels, dates, category/source and exact totals.
- Editing is not celebrated as a new transaction; duplicate delivery/recomposition
  cannot replay the command, stamp, haptic or navigation.
- The accepted duration/dismiss behavior is accessible, controllable with reduced
  motion and restorable after recreation/process death.
- The next destination reconciles against storage and retains a route back to the
  newly saved record where the approved journey requires it.

### Tests
Red comparison tests establish the current inconsistency. Cover create/edit receipts,
duplicate events, busy/failure/unknown outcomes, Back during confirmation, recreation,
process restoration, reduced motion and real stored read-back for expense and income.

### Working checkpoint
Both create journeys still prove one exact stored transaction and land on their
approved destination without an optimistic or duplicate success state.

### Context and starting points
Inspect `ConfirmSlip`, `Stamp`, expense `Receipt`, income form confirmation, operation
receipt/event types and recovery tests. The visual reference includes receipt motion,
but SELLO-061 decides whether that remains a screen rather than an immutable truth boundary.

### Implementation plan
1. Translate the approved lifecycle into a shared state/event contract and red cases.
2. Build/reuse the smallest shared confirmation primitive without merging domain commands.
3. Wire create/edit and failure/unknown/restored outcomes for both transaction kinds.
4. Add reduced-motion and single-consumption guarantees.
5. Reconcile device journeys to actual Room rows and destination state.

### Concrete cases and pitfalls
Do not use a timer as proof of commit, consume an event before navigation can restore,
show `RECIBIDO` for an edit, lose an unknown operation ID, or infer success because a
screen closed. A compact confirmation still needs accessible exact context.

### Verification recipe
Run focused receipt/event/recovery tests and real expense/income save/edit/restart
journeys at normal/reduced motion. Run `./scripts/verify-ticket SELLO-066 --retain`;
record the approved before/after capture and fresh G2 report.

## SELLO-067 — Redesign Resumen around the approved monthly question

- **Type:** Task
- **Source classification:** Feature change request
- **Priority:** P1
- **Severity rationale:** Resumen is a top-level MVP destination and working income entry point; changing its question or removing content has broad navigation and architecture impact.
- **Status:** Backlog
- **Depends on:** SELLO-061, SELLO-062, SELLO-065, SELLO-066, SELLO-004, SELLO-022
- **Gate:** G2
- **Source report:** [R1–R6](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
Resumen answers the approved question about the selected month with useful hierarchy,
sparse-data behavior and predictable drill-down instead of presenting disconnected figures.

### Deliverables
Implement the SELLO-061 information hierarchy and income/budget/dock decisions; calibrate
or remove the verdict stamp according to that decision; redesign empty/sparse/populated
states; and give charts and exact-value details one consistent interaction grammar.
Update ADR 0010/architecture/navigation docs if the approved income route changes.

### Acceptance criteria
- The first viewport states the selected month's primary answer before explanation;
  every shown figure supports that question and uses exact accessible values.
- Zero/single/multi-month and zero/single/many-category histories remain informative;
  charts do not consume dominant space merely to display one bar.
- At most one large verdict stamp appears and only when it has approved decision value;
  its text, color and motion are not the sole communication.
- Chart marks consistently reveal details or navigate according to one documented
  grammar; different outcomes are visibly labeled, keyboard reachable and announced.
- Income/history, budget verdict/projection, category detail and entry remain reachable
  exactly where the approved contracts place them, with truthful loading/error/stale states.
- Shared selected month, Back stack and selection restore across recreation/process death.

### Tests
Red hierarchy/interaction cases cover the current sparse columns and differing taps.
Test empty/error/stale plus sparse/populated histories, exact chart semantics, selection
and navigation, all themes/font scales/window classes, reduced motion and restored month/
selection. Real Room journeys reconcile all displayed totals independently.

### Working checkpoint
Recibo and the redesigned Resumen agree on the selected month and exact underlying
transactions/budget snapshot; every visible drill-down and income route works end to end.

### Context and starting points
Inspect `ResumenRoot/Screen`, summary/chart render models, chart wrappers, PR 47's
documented tap contracts, architecture MVP scope, ADR 0010 and the audit recordings.
Approximate chart geometry never becomes financial truth.

### Implementation plan
1. Convert SELLO-061's approved question into empty/sparse/populated render contracts.
2. Add red hierarchy, chart-action, exact-semantics and restoration tests.
3. Recompose summary information and shared chart interactions using design-system patterns.
4. Rewire income/dock routes only as approved; update governing docs in the same change.
5. Reconcile real totals and navigation across compact/expanded configurations.

### Concrete cases and pitfalls
Do not remove required figures without changing architecture, invent advice from
approximate chart data, use a huge empty chart as an empty state, make chart shape the
only label, or leave income reachable only through a hidden gesture.

### Verification recipe
Run focused Resumen render/domain/navigation tests and independent exact-value fixtures,
then sparse/populated/error/restoration device journeys. Finish with
`./scripts/verify-ticket SELLO-067 --retain`; record fresh G2 and the approved state matrix.

## SELLO-068 — Apply coherent UI hierarchy and shared visual patterns

- **Type:** Task
- **Source classification:** UI change request
- **Priority:** P2
- **Severity rationale:** The issues reduce visual comprehension and consistency, while working semantic or navigational alternatives remain available.
- **Status:** Backlog
- **Depends on:** SELLO-063, SELLO-064, SELLO-065, SELLO-066, SELLO-067, SELLO-004, SELLO-009
- **Gate:** G2
- **Source report:** [H4–H5, H7, C1–C2, IR2 and R2–R3](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
The redesigned experience uses a coherent answer-first hierarchy and a small, documented
Sello component vocabulary rather than screen-local copies or prose compensating for
weak visual affordances.

### Deliverables
Make the dock unmistakably actionable; provide persistent historical-month orientation;
make archived/unlimited category states and transaction-row affordances visible;
demote edit-category in the hierarchy; align expense/income confirmation composition;
and calibrate Resumen stamp/chart hierarchy. Consolidate the reusable action dock/CTA,
financial row, category status, chart-detail and confirmation patterns in design-system.
Extend actual catalog examples for all meaningful states, themes, font scales and
reduced motion; remove superseded paths only after every consumer migrates. Behavior
and financial meaning remain those approved/delivered by SELLO-061–067.

### Acceptance criteria
- Each new primitive has one responsibility, token-based visuals, immutable state/action
  API, accessibility semantics and an actual interactive catalog example.
- Historical versus current month, archived versus active, unlimited versus finite and
  actionable versus static content are visually legible and announced consistently.
- The dock reads as an action rather than a text field; transaction content dominates
  category administration; expense/income confirmations share one visual grammar.
- Resumen uses at most one approved large verdict stamp and chart outcomes are visually
  distinguishable without changing the interaction contract delivered by SELLO-067.
- Four color schemes meet required contrast; state is not color/motion alone; text/targets
  retain 12sp/48dp minimums and layouts work through 2.0 font scale.
- Money APIs accept exact/rendered values and separate approximate chart geometry;
  components do not calculate financial policy or read clocks/storage.
- Screen consumers use the shared patterns without visual forks; old paths are removed
  or explicitly retained for a named distinct use, not left dormant.
- Catalog/debug code and fixtures remain absent from customer artifacts.

### Tests
Red screen/component comparisons cover the audit's ambiguous dock, missing month/status
indicators, weak row affordance, dominant edit action and divergent confirmations.
Component unit/Compose tests cover semantics, interaction, layout bounds, themes, font
scales, RTL-safe geometry where applicable and reduced motion. Catalog device tests
enumerate every registered example; architecture checks prove module/source-set
boundaries and release-artifact isolation.

### Working checkpoint
The product journeys from SELLO-063–067 remain unchanged while their reusable visual
contracts can be inspected independently in the catalog.

### Context and starting points
Inventory existing `designsystem/component` APIs and catalog registrations before
adding anything. Reuse Sello tokens; treat names proposed by this ticket as targets,
not existing APIs. E02 and the approved visual reference remain component authority.

### Implementation plan
1. Freeze the approved behavior from SELLO-061–067 and map each visual audit finding.
2. Add red screen/component/catalog cases before extracting or extending primitives.
3. Migrate consumers sequentially with immutable state/action APIs and reviewed hierarchy.
4. Validate accessibility/theme/font/motion matrices and release isolation.
5. Delete only proven superseded paths and document distinct retained variants.

### Concrete cases and pitfalls
Do not create a universal mega-component, change route/domain behavior during visual
polish, move policy into design-system, copy composables to satisfy previews, pass
floating-point money as truth, or add an icon/chart dependency incidentally. Catalog
screenshots alone do not prove interaction.

### Verification recipe
Run focused design-system and catalog host/device tests, architecture rules and product
consumer tests, then `./scripts/verify-ticket SELLO-068 --retain`. Record all-theme,
1.0/1.3/2.0 font and reduced-motion catalog evidence with the fresh G2 report.

## SELLO-069 — Prove integrated adaptive, accessibility and navigation acceptance

- **Type:** Task
- **Source classification:** UI change request
- **Priority:** P2
- **Severity rationale:** This is the final cross-feature UI acceptance pass; it becomes execution-critical after the higher-severity bug and feature tickets deliver their changes.
- **Status:** Backlog
- **Depends on:** SELLO-062, SELLO-063, SELLO-064, SELLO-065, SELLO-066, SELLO-067, SELLO-068, SELLO-004
- **Gate:** G2
- **Source report:** [2026-10-15 UI/UX audit](../../ui-ux/reports/2026-10-15-ui-ux-audit.md)

### Outcome
The redesigned experience works as one accessible, adaptive, restorable financial
journey rather than a set of individually polished screens.

### Deliverables
An integrated acceptance matrix and repeatable device journeys spanning first run,
growing categories, historical months, expense/income create/edit/confirmation,
category actions, Resumen drill-down and Configuración. Cover compact/short/medium/
expanded windows, 1.0/1.3/2.0 fonts, four themes, reduced motion, screen reader and
hardware/alternative input. Preserve a privacy-reviewed manual evidence package.

### Acceptance criteria
- All audit reproductions have an explicit resolved/deferred/rejected disposition with
  links to owner decision and final evidence; limitations are not rewritten as passes.
- End-to-end journeys preserve exact money, operation identity and confirmed commit
  truth through Back, rapid taps, rotation, process death and restart.
- No clipped/unreachable primary action, ambiguous destination, blank transition,
  accessibility-only visual state, contrast failure or gesture-only operation appears
  in the agreed configuration matrix.
- Recibo/Resumen saved stacks and selected month restore; Configuración returns to its
  caller; screen-reader focus and announcements follow the visible hierarchy.
- Evidence contains synthetic data only and records artifact/revision/device/API,
  executed versus skipped cases and any residual risk.

### Tests
Add only missing cross-feature regression automation; do not duplicate focused tests.
Run real Room/device journeys with controlled time and process restoration, Compose
accessibility/geometry checks, screen-reader/manual input checks and the complete G2
gate. No arbitrary sleeps, live network or fixture-only success paths.

### Working checkpoint
On the tested revision, a new installation can configure categories, record and revise
expense/income, understand Recibo/Resumen, change settings and restart without losing
truth or navigation context.

### Context and starting points
Use each predecessor's retained report, the audit package and PR 47's journey/test map.
This ticket integrates delivered contracts; it does not redesign them again or claim
unexecuted APIs/configurations as passed.

### Implementation plan
1. Build a requirement-to-existing-test/device matrix and identify real coverage gaps.
2. Add meaningful red cross-feature tests only for those gaps.
3. Execute synthetic journeys across the approved window/font/theme/motion matrix.
4. Exercise screen reader, alternate input, rotation, process death and restart.
5. Reconcile audit dispositions, retain privacy-safe evidence and run final G2.

### Concrete cases and pitfalls
Rotation is not process death, assembly is not a device run, visual screenshots are not
TalkBack acceptance, and one phone is not adaptive coverage. Do not derive expected
financial totals from the implementation, hide skipped configurations, or reuse the
audit APK as evidence for a later revision.

### Verification recipe
Run focused missing regressions first, then the complete documented matrix and
`./scripts/verify-ticket SELLO-069 --retain` on the final revision. Record test counts,
device/API/window/font/theme/motion details, artifact checksum, process-restoration
method, manual accessibility outcomes, skips/warnings and the fresh validated G2 report.
