# Screen exploration — history

**2026-10-15 · History only.** The final design is [Sello v2](../v2/README.md)
([ADR 0013](../../decisions/0013-sello-v2-design.md)). This HTML is not a design or
implementation specification.

It is kept as the record of the 2026-10-15 exploration that led to the
[screen and interaction briefs](../screen-briefs/README.md). Do not build from it;
its dimensions, card arrangement and chart choices were illustrative.

Open [the interactive review](index.html). From the repository root:

```bash
python3 -m http.server 8765 --bind 127.0.0.1
```

Then visit `http://localhost:8765/docs/design/proposals/`. All fonts, icons,
scripts and styles are local. Use HTTP, because frame messages require the same
origin; opening the files directly is not the supported review route.

This is a static HTML approximation for discussing Android layouts. It is **not**
the Compose application, the component catalog, a financial engine, or a sandbox
using production commands/storage. It imports bundled Sello fonts and reproduces
the existing four color schemes and Material Symbols vectors for visual continuity.
No dependencies, Android code, schema, release scope or ticket statuses change.
Nothing is persisted, sent to a service or restored after a reload. Success notices
are explicitly labeled synthetic steps by the surrounding review page.

## What to review

1. **Recibo / Home:** remaining capacity is the first answer, daily guidance is
   secondary, reservation context appears only while needed. One compact goal sits
   between the answer and categories. An exceeded category retains its own exact
   deficit without turning a healthy overall budget negative. Anotar stays visible.
2. **Expense and income entry:** one tap from Recibo focuses the amount. Category
   or source chips and date remain direct controls. The owner-selected note dialog
   keeps the main form unchanged underneath. The Add action
   stays above the pad. Note editing happens in a dialog without rearranging the
   underlying form. Ordinary
   synthetic saves return to the caller with brief feedback and no receipt screen.
   A changed draft can be retained or discarded; an uncertain outcome offers
   recovery rather than another write. Production durable receipts remain required.
3. **Resumen:** compare the same days in both months, then show recorded monthly
   history and categories ranked by actual spending. Future patterns and a single
   small suggestion section help adjust budgets. Increased spending is a neutral
   fact rather than an automatic failure. Exact chart values are available by
   selecting a mark; partial October is labeled explicitly.
4. **Income:** Resumen retains a visible income entry point. Recorded income and
   category-only budget allocation have their own hierarchy. Expected income is a
   separate future planning section and never changes Recibo's daily amount.

The default scope includes future Metas, recurring expense reservations and
weekday/category insights. **Sin Metas ni recurrencia** demonstrates a two-tab
composition without those features. Both modes are proposals: the same-cutoff
comparison, new hierarchy and entry behavior are not claims about delivered code.
See the [current app baseline](../../development/current-app-experience.md).

The early-month review invitation is a separate layout scene, not a scheduling
rule. Metas is represented by a compact picker/detail overlay to review pinning;
its complete destination, contribution flow and celebrations are not designed here.
Inversiones is deliberately outside these three screen proposals, retaining the
interview direction and later financial-design work.

## Scripted review flows

- Choose **Recorrido** to navigate one frame at a time, or **Comparar** for all
  three. Try Recibo → Anotar → category → Agregar nota → Listo → Anotar gasto. The guided amount is
  **10.000**. It updates the example once and returns to the caller.
- Switch to Ingreso, choose a source and use **150.000**. The income example
  increases while budgets and Recibo stay unchanged. Expense/income drafts remain
  independent. Otro requires a 1–24 code-point trimmed source name.
- Valid amounts outside the two guided fixtures remain editable but do not produce
  invented results. The outer review page explains this limitation. Guided saves
  require the fixed date and ordinary ready scene; alternate financial scenes are
  independent snapshots, not a composable event simulator. Each guided record can
  be added once per reset. Use **Reiniciar ejemplo** to repeat a flow.
- Try **Pago reservado → registrado**: the headline stays **400.000**, the daily
  figure stays **40.000**, and the reservation line disappears entirely.
- Try invalid input, empty, busy, uncertain, rejected and restored-draft form
  scenes. `1e3` stays intact and cannot submit. Notes over 60 code points remain
  intact with an error. Busy/uncertain scenes preserve the draft and block editing.
  Checking an uncertain result only explains the intended recovery contract; there
  is no operation to recover in this prototype.
- In Metas, pin Viaje a la costa; Recibo changes its featured goal. Reset selects
  Fondo de emergencia again, the fixture with the highest completion percentage.
- Switch ink/theme, text scale and frame size. Form content scrolls while Add
  stays outside it. At enlarged text in short landscape, the mode switch joins the
  scrolling form. Dense charts scroll horizontally; selected/spoken money is exact.

Settings/month selection, category create/edit, income-record edit and goal
contribution retain their existing or future owned journeys. Their controls explain
that boundary in the **outer review page**, rather than pretending to save data.
This proposal does not remove those journeys or redesign their screens. Dates can
be changed locally, but guided results use only the fixture date. The restored
draft selector is a render state, not proof of process-death restoration.

## Independently specified fixtures

COP has no peso symbol. Financial date is fixed at **2026-10-22**, with **10 days
including today**. Figures are explicitly authored examples; the only numeric
derivation in charts is approximate geometry, never financial truth or policy.

| Scene | Limits | Recorded expenses | Upcoming reserved | Home answer | Daily |
| --- | ---: | ---: | ---: | ---: | ---: |
| Default, future | 1.000.000 | 400.000 | 200.000 | 400.000 | 40.000 |
| Default, no recurrence | 1.000.000 | 400.000 | — | 600.000 | 60.000 |
| After guided expense, future | 1.000.000 | 410.000 | 200.000 | 390.000 | 39.000 |
| After guided expense, no recurrence | 1.000.000 | 410.000 | — | 590.000 | 59.000 |
| Reserved rent recorded | 1.000.000 | 600.000 | 0, context omitted | 400.000 | 40.000 |
| No free margin | 1.000.000 | 900.000 | 100.000 | 0 · Sin margen libre | 0 |
| Commitment shortfall | 1.000.000 | 900.000 | 150.000 | 50.000 to cover · Por ajustar | 0 |
| Actual overrun | 1.000.000 | 1.080.000 | — | −80.000 remaining · red | 0 |
| No limits | None | 400.000 | — | 400.000 recorded spending | Omitted |

Default category fixtures are Hogar 600.000 limit / 200.000 spent, Comida
200.000 / 50.000, Transporte 100.000 / 40.000, and Café 100.000 / 110.000.
The default expense goes to Café; changing category uses a corresponding fixed
row result. Actual overall/category deficits consistently use signed negative remainders and
semantic red; the overall heading is Restante del presupuesto. Color is reinforced
by the amount and status text. Category remaining is before future payments: labels distinguish
category budget from the Home answer after reservations. Distribution bars show
shares of total recorded spending, not consumption of category limits.

Recorded income is 2.000.000, category allocation 1.000.000, recorded unassigned
capacity 1.000.000. Future expected income adds 1.000.000, for 2.000.000 planned
unassigned capacity. The guided **additional manual** income of 150.000 is separate
from that expected occurrence: recorded total becomes 2.150.000, recorded
unassigned 1.150.000 and planned unassigned 2.150.000. This does not model occurrence
matching. With no category limits allocation is zero; goal/investment tracking never
adds another allocation deduction.

September days 1–22 have 450.000 spending versus October's 400.000: 50.000 less,
11,1% lower. Full-month totals May–September are 720.000 / 680.000 / 810.000 /
760.000 / 920.000. October is partial and never extrapolated. Weekday averages and
the “3 of 4 months” Café insight are synthetic layout examples, not approved
recommendation eligibility thresholds or implemented analytical policies.

## Mapping back to the actual design system

| Proposed surface | Existing production building blocks / implementation boundary |
| --- | --- |
| Answer receipt | `Slip`, `TearLine`, `MoneyText`, `Stamp`, `LeaderLine`; change screen composition, with reviewed domain snapshots for changed meaning. |
| Category summary | `CategoryGrid`, `CategoryCell`, `CategoryCircle`; retain ordering, details, archive/history behavior and accessibility. |
| Entry | `AmountField`, `AmountKeypad`, `FormField`, chips, `SegmentedSwitch`, `SelloButton`; extend the actual components/catalog for the fixed-pad and note-dialog alternatives, including IME/insets. HTML input behavior is not proof those APIs already support it. |
| Save feedback | Existing `SelloNotice` family; preserve durable receipt read-back, operation IDs, unknown-outcome recovery and correction/undo handling. |
| Comparisons/charts | `SlipHeading`, `ColumnsChart`, exact `MoneyText` selections; eligibility and fair period aggregation belong in domain, not chart code. |
| Navigation | Existing `SelloNavigationBar`/rail, `EntryDock`, `MonthSwitcher`; only expose working destinations. This mock does not validate Android rail or back-stack behavior. |
| Loading/empty/error | `SlipSkeleton`, `EmptySlip`, `ErrorSlip`; retain distinct typed failures and visibly stale last-good snapshots. |
| Goal preview | Existing paper/progress styling informs a later real component with catalog examples; no dormant production goal implementation is introduced. |

Use the written briefs and the designer's subsequent reviewed designs to scope
implementation around entry/feedback, Home's approved classifier and Resumen's
hierarchy/comparison data. Directional acceptance of this exploration is not that
visual review. Extend actual
components and catalog examples rather than porting these HTML replicas. Recurrence,
Metas and recommendation rules retain their own dependencies and approval inputs.

## Review limits and sources

The default **Pad fijo** is an interactive HTML candidate with digits, `000` and
backspace, occupying a fixed 216px bottom area; Add stays directly above. Short
landscape places the pad/Add beside the form. There is no moving financial preview.
**Entrada → Numérico** retains the previous system-keyboard alternative.

Tapping **Agregar nota** opens a dialog with a text editor, Cancelar and Listo.
Closing it restores the underlying layout without clearing amount/category.
An Otro source name uses the same dialog pattern as a proposed extension.
Raw overlong text is retained with an error; only valid text can be accepted.
This HTML dialog is not proof that Android's native IME leaves the underlying form
unchanged; inset/focus/Back/restoration behavior still needs device review. The
owner's keypad preference is conditional on achieving that stability.

The system keyboard pictured inside the note dialog or system-keyboard alternative
is an **inert illustration**. Type through the actual input or physical keyboard. Coarse-pointer
browsers omit the illustration to avoid doubling a native keyboard. Android focus,
IME/insets, keyboard variation, TalkBack, process death, durable recovery, device
font scaling and real adaptive navigation still require implementation/device
verification. A browser layout check cannot certify them. The mock has no animation;
reduced-motion preferences also disable transitions.

No complete recurrence/status lifecycle is inferred. Occurrence matching,
overdue/skipped/paused/changed payments, classifier precedence, explicit-zero/past
copy and future-income planning remain the open inputs in
[ADR 0012](../../decisions/0012-budget-guidance-contract.md). A positive prototype
scene is not approval of unseen financial cases.

Sources: [philosophy](../philosophy.md), [direction and supersessions](../../decisions/0011-goal-centered-design.md),
[financial definitions](../../decisions/0012-budget-guidance-contract.md),
[unchanged visual snapshot](../sello-spec.html), and
[bundled font/icon notices](../../../design-system/THIRD_PARTY_NOTICES.md).
The original visual snapshot remains byte-for-byte unchanged.
