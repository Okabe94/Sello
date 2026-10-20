# SELLO-E13 — Bring the rest of today's app to Sello v2

- **Status:** Backlog
- **Increment:** MVP
- **Goal:** Every screen the app already has looks and behaves as approved in Sello v2, using one set of components, before new features are added.
- **Exit:** Expense and income entry, income history and Resumen match their v2 designs for today's scope; superseded v1 components are gone from the app and catalog; a manual screen-reader and switch-access pass is recorded.

Read [ADR 0013](../../../decisions/0013-sello-v2-design.md), the
[v2 README](../../../design/v2/README.md), the [current app inventory](../../../development/current-app-experience.md)
and [E12](E12-v2-recibo.md), which brings Recibo and the shared frame to v2 first.

**Owner direction (2026-10-17):** make today's functionality look and work correctly
in v2 before adding features. Goals, investments, reservations and recurrence stay
out: they have no domain model or storage, and v2 elements that depend on them
(goal pill, reserved payments, **Sin margen**, **Revisar**, expected income) must not
appear, even as placeholders. They can be ticketed later if the owner pulls them in.
Screens v2 does not show (category detail, the category editor, Configuración) keep
their v1.1 look under ADR 0013 and pick up the v2 frame through E12.

The v2 components were built and reviewed in the catalog first (pull request #54,
`catalog-v2-components`), including `V2EntryLayout`, `EntryAmountSlip`,
`V2ChipRows`, `AddChip`, `EntryLine`, `NoteComposer`, `AnswerSlip`, `SplitBar`,
`ComparisonSlip`, `SpendingRankRow` and `V2TitleBar`. Tickets reuse them; a gap
found while wiring a screen is fixed in the shared component with a test, not
worked around in the screen.

## SELLO-070 — Bring expense entry and edit to v2

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-066, SELLO-004
- **Gate:** G2

### Outcome
Anotar opens the v2 form: amount slip, two rows of category chips ending in
**Nueva**, a note row with its docked composer, a fixed **Anotar gasto** button and
the compact Sello pad.

### Deliverables
The expense form rebuilt on `V2EntryLayout`, `EntryAmountSlip`, `V2ChipRows`,
`SelloChip` with category icons, `AddChip`, `EntryLine`, `NoteComposer`, the compact
`AmountKeypad` and `V2Dock`-style action, for both new entries and edits, with its
screen tests updated.

### Acceptance criteria
- Order and behaviour follow the v2 README "Expense and income entry": no category
  preselected (except from "Anotar aquí"), at most two chip rows scrolling sideways,
  the amount, action and pad never moving.
- **Nueva** opens the category editor and returns with the new category selected, as
  today's entry prerequisite does.
- The note row opens the composer docked to the keyboard; over 60 characters the
  text is kept, the counter turns red and ✓ is disabled; editing starts with the
  cursor at the end.
- Amount sizes step down as digits are added; a 13th digit shows **Máximo 12 dígitos**
  in place of the label without moving anything.
- Saving, unknown-outcome recovery ("Comprobar si se guardó"), drafts restored after
  process death, past-month edits and the edit-only delete keep today's rules.
- Short windows scroll the amount with the choices; two-column windows keep today's
  split; large text and 320dp windows keep every control reachable.

### Tests
Updated expense entry screen and journey tests (new entry, edit, unknown outcome,
restoration), the keyboard-inset test and G2.

### Working checkpoint
On a phone, an expense can be entered, edited and recovered after process death in
the v2 form, and the saved confirmation from SELLO-066 appears.

### Context and starting points
Today: `app/.../feature/expense/ExpenseEntryScreen.kt` and its root, render model and
tests; see "6. Expense entry and edit" in the current app inventory. Reference:
`docs/design/v2/entry.html` and the catalog example `v2.entry` in
`catalog/.../v2/V2InputExamples.kt`, which already shows every state with synthetic
data.

### Implementation plan
1. Rebuild the form's layout on `V2EntryLayout` with the v2 pieces.
2. Wire **Nueva** to the existing category-creation route.
3. Move the note to `EntryLine` and `NoteComposer`.
4. Update screen and journey tests; check 320dp, font 2.0 and two columns.

### Concrete cases and pitfalls
- The composer's keyboard must not resize or flip the form behind it.
- Edits have no Gasto/Ingreso switch, as today.
- The note limit counts characters as people see them (code points), not UTF-16 units.

### Verification recipe
`:app` focused screen and journey tests, then `./scripts/verify-ticket SELLO-070 --retain`.

## SELLO-071 — Bring income entry, edit and history to v2

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-070, SELLO-064, SELLO-004
- **Gate:** G2

### Outcome
Income entry matches the v2 form in green, and income history becomes the v2
**Ingresos de octubre** screen: what came in, where it went, and each income.

### Deliverables
Income entry and edit on the SELLO-070 form (source chips without icons, **Otro**
with a full-width source-name row using the same composer, the amount in gain with
`+`, **Anotar ingreso**); income history rebuilt with `V2TitleBar`, an `AnswerSlip`
hero ("Entró en octubre"), a `SplitBar` with **En presupuestos de categorías** and
**Sin asignar** lines, and the income list with a total line.

### Acceptance criteria
- Income entry keeps today's sources, optional name, date, note, saving and
  recovery rules; the source name is required only for Otro and is never lost when
  the composer closes.
- The history hero shows the recorded total; a negative unassigned amount is signed
  and red, followed by the two facts behind it, with no suggestion.
- No expected income, plan box or suggestion appears: recurrence does not exist.
- Rows keep paging, editing and failed-page retry; loading and failed reads never
  show zero.
- A past month shows the shared ribbon.

### Tests
Income entry, history and journey tests updated; split-bar semantics; G2.

### Working checkpoint
From Resumen, income history shows the v2 hero and list for the selected month, and
a new income saved there appears in it.

### Context and starting points
Today: "5. Income history" and "7. Income entry and edit" in the current app
inventory; the allocated and unallocated figures already exist for Resumen. Reference:
`docs/design/v2/income.html`, "Income" in the v2 README and the catalog examples.

### Implementation plan
1. Move income entry onto the SELLO-070 form with the green tone.
2. Rebuild income history's top with the hero and split bar from existing figures.
3. Restyle the list; update tests.

### Concrete cases and pitfalls
- Unassigned income can be negative; the bar must not draw a negative segment.
- "Sello funciona sin ingresos" stays the empty state.

### Verification recipe
`:app` focused tests, then `./scripts/verify-ticket SELLO-071 --retain`.

## SELLO-072 — Bring Resumen to v2 for today's figures

- **Type:** Story
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-064, SELLO-065, SELLO-004
- **Gate:** G2

### Outcome
Resumen shows today's figures in the v2 order and components: income lines first,
then month by month, then where the money goes.

### Deliverables
Resumen rebuilt with v2 components for the figures it already computes: the income
slip as receipt lines with **Ver ingresos y su destino ›**, the monthly columns with
the month in progress striped, and categories ranked by spending with
`SpendingRankRow`, plus updated screen tests.

### Acceptance criteria
- Only figures the app already computes appear; nothing is projected for the month
  in progress.
- Columns give exact values on selection, as today; the striped current month is
  explained to screen readers.
- Each ranked category opens its detail; the total line matches the sum.
- Loading, failed (never zero), empty and stale states keep today's behaviour.
- Large text, 320dp and two-column layouts work.

### Tests
Resumen screen and journey tests updated; chart exact-selection tests; G2.

### Working checkpoint
On a phone, Resumen shows the v2 income lines, columns and ranking for the selected
month, and each opens what it opens today.

### Context and starting points
Today: "4. Resumen" in the current app inventory and `app/.../feature/resumen`.
Reference: `docs/design/v2/resumen.html` and "Resumen" in the v2 README. The v2
hero ("¿Cómo va este mes?" against the same days of last month), "¿Qué días gastas
más?" and the suggestion need new calculations and are not part of this ticket; the
owner can ticket them separately.

### Implementation plan
1. Reorder Resumen's slips to the v2 order.
2. Replace the income and allocation slips with receipt lines.
3. Replace category columns with ranked rows.
4. Update tests.

### Concrete cases and pitfalls
- Removing the cumulative daily line chart changes what Resumen shows; keep it until
  the owner confirms it goes, and record the decision here.
- The month in progress must never be drawn as a full month.

### Verification recipe
`:app` focused tests, then `./scripts/verify-ticket SELLO-072 --retain`.

## SELLO-073 — Retire the v1 components v2 replaced

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-065, SELLO-068, SELLO-070, SELLO-071, SELLO-072, SELLO-004
- **Gate:** G2

### Outcome
The app and catalog use one set of components: each v1 component that a v2 one
replaced is deleted, and the remaining v2 components lose their `V2` prefix.

### Deliverables
Removal of superseded v1 components and their catalog examples and tests (for
example `EntryDock`, `SelloNavigationBar`, `MonthSwitcher`'s title, `FieldPill` where
`EntryLine` replaced it, `ChipFlow` where `V2ChipRows` replaced it), the `V2` names
renamed to their final names, the catalog's V2 section folded into its component
groups, and the remaining screens (category detail, the category editor,
Configuración) moved onto the shared components where an equivalent exists.

### Acceptance criteria
- No production screen imports a deleted component; the architecture check passes.
- Each deletion is backed by a search showing no remaining use.
- Category detail, the editor and Configuración look as before (ADR 0013 v1.1) apart
  from shared pieces now drawn by the v2 component.
- The catalog lists each component once.

### Tests
Full `:design-system`, `:catalog` and `:app` suites; G2.

### Working checkpoint
The catalog lists each component once, and the app runs every journey with no v1
duplicate left.

### Context and starting points
List v1 components with `grep` over `designsystem/component` and compare against
`designsystem/component/v2`. The catalog's `v2-screens` section is review material;
keep or remove it with the owner.

### Implementation plan
1. List each v1/v2 pair and its remaining uses.
2. Move remaining uses, delete the v1 component and its example and tests.
3. Rename v2 components and fold catalog sections.

### Concrete cases and pitfalls
- Some v1 components have no v2 replacement (for example `CategoryCell`); keep them.
- Renames touch many files; do them in one focused commit without behaviour changes.

### Verification recipe
Full suites, then `./scripts/verify-ticket SELLO-073 --retain`.

## SELLO-074 — Review the v2 screens with TalkBack and Switch Access

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-073, SELLO-004
- **Gate:** G2

### Outcome
A person has used every v2 screen with TalkBack and with Switch Access on a device,
and what they found is fixed or ticketed.

### Deliverables
A recorded manual pass (device, Android version, settings, screens and findings) in
`docs/testing/`, with fixes for small issues and tickets for larger ones.

### Acceptance criteria
- Every v2 screen and state in today's scope is covered: Recibo, entry (expense and
  income, including the composer and unknown outcome), income history, Resumen and
  the frame.
- Amounts are spoken exactly; status changes are announced once; focus order follows
  the visual order; every action is reachable by switch.
- Findings are fixed with a test or ticketed; none are left unrecorded.

### Tests
The manual pass itself; regression tests for each fix; G2.

### Working checkpoint
The recorded pass lists no open, unrecorded finding.

### Context and starting points
Accessibility baseline: "Adaptive and accessibility baseline" in the current app
inventory and ARCHITECTURE.md. Automated tests already cover semantics and target
sizes; this ticket is the human check they cannot replace.

### Implementation plan
1. Run the pass with TalkBack, then with Switch Access, on a physical device.
2. Record findings; fix small ones with tests; ticket the rest.

### Concrete cases and pitfalls
- The celebration and ribbons use live regions; check they are not announced twice.
- Use synthetic data only; no personal records on the review device.

### Verification recipe
The recorded pass, focused tests for fixes, then `./scripts/verify-ticket SELLO-074 --retain`.
