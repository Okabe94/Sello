# Sello UI/UX audit — 2026-10-15

**Status (2026-10-19):** the redesign epic proposed with this audit was not adopted.
[ADR 0013](../../../decisions/0013-sello-v2-design.md) made Sello v2 the design, and
[E12](../../mvp/epics/E12-v2-recibo.md) and [E13](../../mvp/epics/E13-v2-current-app.md)
replaced that plan, reusing ticket IDs SELLO-061–069 for different work. The findings
and evidence below are kept as they were observed; only the last column of the matrix
is updated to show where each finding stands in the current plan.

## Executive finding

The owner's inventory identifies real layout, affordance, contrast and shell-context
problems, but it also proposes changes to deliberate Sello behavior. The evidence
supports a coherent redesign epic, provided implementation does not silently invert
the visual language or remove truthful financial/navigation capabilities.

The most consequential reproduced problems are:

- adding categories falls below the fold as the Recibo collection grows;
- Configuración inherits a financial month title and global expense dock;
- an entry opened from a historical month defaults to today's date;
- the IME removes most entry-form context, and income geometry becomes unusable
  after the first digit;
- a dark-theme income explanation renders with insufficient contrast;
- transaction rows and archived/unlimited category states lack clear visual
  affordances even where accessibility semantics carry the state; and
- Resumen has weak hierarchy, very large sparse charts and different chart-tap
  contracts.

The redesign must preserve Sello's established principles: figures live on paper;
the answer precedes explanation; information should be shown through hierarchy,
icon and position before prose; a screen has at most one large verdict stamp; money
is exact and accessible; and Recibo/Resumen remain the only MVP top-level tabs unless
architecture is explicitly revised. The approved visual reference intentionally
fills category circles with ink as spending grows. Architecture currently requires
a basic Resumen with actual totals, shared budget verdict/projection, and a working
income route under ADR 0010 (since removed in the planning refresh, PR 51).

## Sources and limitations

- Owner inventory: [`ui_issues.md`](../../../../ui_issues.md)
- Architecture: [`ARCHITECTURE.md`](../../../../ARCHITECTURE.md)
- Visual authority: [`docs/design/sello-spec.html`](../../../design/sello-spec.html)
- Implemented experience: [`current-app-experience.md`](../../../development/current-app-experience.md)
- Navigation inventory: [`navigation-graph.md`](../../../development/navigation-graph.md)
- Reviewed merged change: [PR 47](https://github.com/Okabe94/Sello/pull/47)
- Capture index and device details: [package README](../README.md)

All data is synthetic. Scripted category-name concatenation in captures is unrelated
to the findings. The audit used one API 30 phone configuration and is not adaptive,
TalkBack or performance acceptance. The one partial-swipe attempt did not reproduce
a stuck half-swiped row. The Recibo/Resumen recording shows ghosted cross-fade frames,
not a definitive full white flash. Chart evidence demonstrates one exact-value
selection; the documented current graph establishes the different category-navigation
contract, while the second coordinate tap in the recording missed after layout moved.

## Coverage by the current plan

Of the 31 findings, checked against E12 and E13 on 2026-10-19:

| Coverage | Findings |
| --- | --- |
| Covered by a ticket | H3 (SELLO-063, Done), H4, H5, E1, I1, I3, I4, ER1, IR1, IR2, R3 |
| Partly covered | H7, I2, R5 |
| Decided by v2, request not adopted | H1, E2, E4, R1, R4, R6 |
| Not covered | H2, H6, C1, C2, C3, C4, C5, E3, R2, S1, S2 |

The uncovered findings are candidates for new tickets; none is ticketed by this
report. Category detail and the category editor keep their v1.1 look under ADR 0013,
which is why C1–C5 have no v2 ticket.

“Reproduced” does not automatically make a request an actual bug: a capture may prove
that an intentional feature exists while the owner requests different behavior.

## Finding matrix

Evidence links point to both the visual capture and its UI hierarchy when one exists.
The last column refers to [E12](../../mvp/epics/E12-v2-recibo.md) and
[E13](../../mvp/epics/E13-v2-current-app.md) as of 2026-10-19.

| ID | Owner report | Classification | Evidence and observation | Governing contract / disposition | Current plan |
| --- | --- | --- | --- | --- | --- |
| H1 | Category circles should empty instead of fill | Decision + contract conflict | [Recibo state](../evidence/20261015/03-recibo-one-category.png) · [XML](../evidence/20261015/03-recibo-one-category.xml) shows the current treatment. | The approved reference says circles fill with ink as spending grows. Ratify meaning before changing direction. | Decided by v2: "unchanged circle grid" keeps the fill |
| H2 | Current category scrolling is not the best model | Decision, with reproduced scalability pressure | [Nine categories](../evidence/20261015/04-recibo-add-category-below-fold.png) · [XML](../evidence/20261015/04-recibo-add-category-below-fold.xml). A replacement interaction needs product criteria, not merely a different gesture. | Preserve reading order, exact totals, adaptive behavior and non-gesture access. | Not covered |
| H3 | Add category becomes hidden below the screen | Reproduced | [Before scroll](../evidence/20261015/04-recibo-add-category-below-fold.png) · [XML](../evidence/20261015/04-recibo-add-category-below-fold.xml); [after scroll](../evidence/20261015/05-recibo-add-category-after-scroll.png) · [XML](../evidence/20261015/05-recibo-add-category-after-scroll.xml). | Make the action persistently discoverable without hiding category content. | SELLO-063 (Done): **+** in the **Tus categorías** header |
| H4 | “+ Anotar” looks like an input | Reproduced visual ambiguity | [First-run Recibo](../evidence/20261015/01-first-run-recibo.png) · [XML](../evidence/20261015/01-first-run-recibo.xml). Prompt-like copy sits inside a bordered dock beside the CTA. | “Adding should be one motion”; create an unmistakable action/dock contract and catalog state. | SELLO-065: one full-width **+ Anotar** button |
| H5 | Historical month is hard to recognize | Reproduced as visual orientation issue | [Past month](../evidence/20261015/06-past-month-recibo.png) · [XML](../evidence/20261015/06-past-month-recibo.xml). Semantics say “Mes seleccionado,” but no persistent visual historical marker appears outside the picker. | SELLO-059 already added “Mes actual” inside the picker; do not duplicate that fix. | SELLO-068, SELLO-064: past-month ribbon with **Volver a** |
| H6 | Recibo → Resumen flashes | Partly reproduced | [Recording](../evidence/20261015/18-recibo-resumen-transition.mp4) · [sampled frames](../evidence/20261015/18-recibo-resumen-transition-frames.png). Ghosted cross-fade/intermediate frames are visible; a full white flash is not established. | Treat as transition communication/stability, retaining reduced-motion behavior and saved tab state. | Not covered |
| H7 | Archived and unlimited categories have no visual indicator | Reproduced visually | [Category states](../evidence/20261015/17-archived-and-unlimited-indistinct.png) · [XML](../evidence/20261015/17-archived-and-unlimited-indistinct.xml). XML says “Archivada” and “Sin límite,” while visible rows do not. | State must not depend on accessibility-only text or color alone. | Partly: SELLO-061 shows **Sin límite** as a category status; archived is not covered |
| C1 | Expense rows do not look tappable | Reproduced | [Category detail](../evidence/20261015/14-category-detail-actions.png) · [XML](../evidence/20261015/14-category-detail-actions.xml). Rows read like receipt text; helper prose carries the affordance. | Show interaction through structure before instructions; preserve exact amounts and focus semantics. | Not covered: category detail keeps its v1.1 look (ADR 0013, E13) |
| C2 | Edit category is too prominent | Reproduced hierarchy concern | [Category detail](../evidence/20261015/14-category-detail-actions.png). A full-width edit button visually dominates the transaction list. | Demote administrative action without hiding it or creating a gesture-only route. | Not covered: category detail keeps its v1.1 look (ADR 0013, E13) |
| C3 | Partial swipe often remains half open | Not reproduced in one controlled attempt | [Attempt](../evidence/20261015/15-category-partial-swipe.mp4) · [final frame](../evidence/20261015/15-category-partial-swipe.png) · [XML](../evidence/20261015/15-category-partial-swipe.xml). The row returned to rest. | Keep as a targeted regression case; do not claim the report is disproved by one attempt. | Not covered |
| C4 | Tap, swipe and long press over-engineer edit/delete | Reproduced interaction multiplicity + decision | [Helper and rows](../evidence/20261015/14-category-detail-actions.png); [long-press menu](../evidence/20261015/16-expense-long-press-actions.png) · [XML](../evidence/20261015/16-expense-long-press-actions.xml). | Accessibility requires a non-gesture equivalent, not three equally primary paths. Choose one visible primary path plus an accessible alternative. | Not covered |
| C5 | Too few category icons | Reproduced | [Editor choices](../evidence/20261015/02-category-editor-options.png) · [XML](../evidence/20261015/02-category-editor-options.xml) shows 14 choices. | Expand a coherent vocabulary through the shared icon component/catalog, with stable semantics. | Not covered: the category editor keeps its v1.1 look |
| E1 | Expense note focus makes the screen unusable | Reproduced | [Before IME](../evidence/20261015/09a-expense-note-before-ime.png) · [XML](../evidence/20261015/09a-expense-note-before-ime.xml); [with IME](../evidence/20261015/09b-expense-note-ime.png) · [XML](../evidence/20261015/09b-expense-note-ime.xml). Most context disappears and a large blank region remains. | Forms must retain reachable context/actions under IME and large text. | SELLO-070: note composer docked to the keyboard |
| E2 | Custom input pad feels gimmicky | Decision | [Mixed entry](../evidence/20261015/08-expense-entry-mixed-mode.png) · [XML](../evidence/20261015/08-expense-entry-mixed-mode.xml). This is a product judgment, not a defect by itself. | Decide custom versus platform keypad using exact-money entry, hardware keyboard, Switch Access, layout stability and error prevention criteria. | Decided by v2: the compact Sello pad stays (SELLO-070) |
| E3 | Date defaults to today from another month | Reproduced | [September context / October date](../evidence/20261015/08-expense-entry-mixed-mode.png). Entry opened from September but shows “Hoy, jue 15 oct.” | Define selected-month-aware defaulting without silently changing an explicit restored draft. | Not covered: entry still defaults to today |
| E4 | Expense and income in one screen are confusing | Reproduced structure + decision | [Segmented form](../evidence/20261015/08-expense-entry-mixed-mode.png). Current navigation docs confirm a local mode switch rather than separate typed routes. | Separate journeys only after the destination and income-entry contract are ratified. | Decided by v2: one form keeps the Gasto/Ingreso switch (SELLO-070, SELLO-071) |
| I1 | Income shifts after the first digit and hides the amount/keys | Reproduced | [Empty](../evidence/20261015/10a-income-entry-empty.png) · [XML](../evidence/20261015/10a-income-entry-empty.xml); [first digit](../evidence/20261015/10b-income-entry-first-digit.png) · [XML](../evidence/20261015/10b-income-entry-first-digit.xml). Preview insertion leaves only the first keypad row visible. | SELLO-046 fixed expense keypad movement, not this broader income geometry. | SELLO-071: income moves to the SELLO-070 form, where amount, action and pad never move |
| I2 | Income hint is black in dark theme | Reproduced contrast defect | [Dark theme](../evidence/20261015/11-income-dark-contrast.png) · [XML](../evidence/20261015/11-income-dark-contrast.xml). Explanatory text is black on near-black. | All four schemes require readable text contrast. | Partly: SELLO-071 rebuilds the form; dark-theme contrast is not an explicit criterion |
| I3 | Income note has the same IME issue | Not separately exercised | Expense evidence establishes the class of risk, but this exact income-note state was not captured. | Make it an explicit red regression/acceptance case rather than labeling it reproduced. | SELLO-071: the same note composer |
| I4 | “Otro” source field has the same IME issue | Not separately exercised | No direct capture. | Verify the conditional source field independently at compact/short and 2.0 font scale. | SELLO-071: the **Otro** source-name row uses the same composer |
| ER1 | Expense receipt screen has no point | Decision + contract conflict | [Expense receipt](../evidence/20261015/12-expense-receipt.png) · [XML](../evidence/20261015/12-expense-receipt.xml). | A confirmed receipt currently communicates a real stored commit. It may become compact/automatic, but cannot be replaced by optimistic success or a no-op. | SELLO-066: a brief notice after a confirmed save replaces the receipt |
| IR1 | Income receipt screen has no point | Decision + contract conflict | [Income receipt](../evidence/20261015/13-income-receipt.png) · [XML](../evidence/20261015/13-income-receipt.xml). | Same truthful-commit constraint as expense; unknown outcomes and restoration remain explicit. | SELLO-066: a brief notice after a confirmed save replaces the receipt |
| IR2 | Income receipt style differs from expense | Reproduced | Compare [expense](../evidence/20261015/12-expense-receipt.png) with [income](../evidence/20261015/13-income-receipt.png). Composition and hierarchy differ materially. | Establish one post-save grammar while retaining transaction-specific facts. | SELLO-066: both receipts are removed |
| R1 | Income should not live in Resumen | Decision + contract conflict | [Resumen top](../evidence/20261015/19-resumen-top.png) · [XML](../evidence/20261015/19-resumen-top.xml). | Architecture and ADR 0010 put actual income and its working entry/history route in Resumen. A replacement entry point and ADR update are prerequisites. | Decided by v2: income stays in Resumen (SELLO-071, SELLO-072) |
| R2 | Resumen stamp is over the top | Decision, supported hierarchy concern | [Resumen top](../evidence/20261015/19-resumen-top.png) shows a very large “BAJO EL” stamp. | Sello allows at most one large verdict stamp per screen, but size/necessity for this question needs ratification. | Not covered: the v2 Resumen hero has no verdict stamp, but SELLO-072 leaves the hero out |
| R3 | Graphs have inconsistent tap behavior | Reproduced/documented | [Interaction recording](../evidence/20261015/21-resumen-chart-actions.mp4) · [frames](../evidence/20261015/21-resumen-chart-actions-frames.png) shows exact-value selection. The current navigation inventory documents category bars/lines as navigation. | Adopt one legible interaction grammar or make distinct outcomes explicit and accessible. | SELLO-072: columns give exact values; categories become ranked rows that open detail |
| R4 | Global “+ Anotar” should not be in Resumen | Decision | [Resumen top](../evidence/20261015/19-resumen-top.png). | The current dock is globally expense-first. Shell placement must follow the approved navigation hierarchy. | Decided by v2: the dock is on every tab (SELLO-065) |
| R5 | Resumen information is lackluster/useless | Decision, with reproduced sparse-state weakness | [Top](../evidence/20261015/19-resumen-top.png); [charts](../evidence/20261015/20-resumen-charts.png) · [XML](../evidence/20261015/20-resumen-charts.xml). Large one-month columns dominate sparse history. | Define the monthly question and useful empty/sparse states before changing content. | Partly: SELLO-072 reorders today's figures; the v2 comparison hero, weekday answer and suggestion need new calculations and are not ticketed |
| R6 | Resumen should show only the selected month, no income/budget/graphs | Decision + contract conflict | [Resumen captures](../evidence/20261015/19-resumen-top.png), [charts](../evidence/20261015/20-resumen-charts.png). | MVP architecture explicitly requires actual totals, simple charts, projection and shared budget verdict; removing them needs architecture/product approval. | Decided by v2: Resumen keeps income, columns and categories (SELLO-072) |
| S1 | Configuración should not have a month selector | Reproduced shell-context issue | [Settings](../evidence/20261015/07-settings-in-financial-shell.png) · [XML](../evidence/20261015/07-settings-in-financial-shell.xml) shows “Configuración · Septiembre 2026.” | Configuración is a nested nonfinancial destination; it should not imply that appearance settings are month-scoped. | Not covered |
| S2 | Configuración should not have “Anotar” | Reproduced shell-context issue | [Settings](../evidence/20261015/07-settings-in-financial-shell.png) shows the global expense dock. | Decide dock scope once, then keep nonfinancial settings focused and Back behavior intact. | Not covered: SELLO-065 keeps the dock on Configuración |

## Cross-cutting conclusions

1. **Do not implement contested meaning piecemeal.** Category ink direction,
   Resumen's question, income placement, receipt lifecycle and keypad strategy need
   one approved decision record before their dependent UI changes.
2. **Repair the shell before polishing screens.** Month relevance, dock scope,
   nested destination identity and transition/restoration rules affect most findings.
3. **Treat accessibility semantics and visual communication as complementary.**
   Archived/unlimited states prove that correct TalkBack text alone does not make a
   state visually legible; redesign must preserve those semantics while adding a
   visible, non-color-only treatment.
4. **Preserve financial truth.** Any faster post-save flow must wait for a confirmed
   receipt, remain idempotent, distinguish unknown outcomes and reconcile after
   process restoration.
5. **Validate the whole system.** Shared components, adaptive layouts, IME, large
   text, dark themes, reduced motion and navigation restoration require an integrated
   acceptance ticket after the feature tickets, not isolated screenshot approval.
