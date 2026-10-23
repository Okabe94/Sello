# SELLO-E16 — Finish Sello v2 on today's screens

- **Status:** Backlog
- **Increment:** Post-MVP
- **Goal:** Add the parts of the approved v2 Recibo, Resumen and income screens that the MVP left out, which need no new kind of data.
- **Exit:** Resumen leads with the same-days comparison and shows the average month, the weekday answer and one suggestion; income history suggests assigning unassigned income; Recibo invites a look at the month just closed.

**Owner direction (2026-10-23):** E13 built only what existing data supports and left
these for separate tickets. Decisions for their open points are in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); the
rest of each rule is in the [v2 README](../../../design/v2/README.md) and the
[Resumen and income brief](../../../design/screen-briefs/summary-and-income.md).

## SELLO-079 — Verify what exists for today's Recibo, Resumen and income screens and what is missing

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-004
- **Gate:** G1

### Outcome
An exact, current list of what the app, the design system and the catalog already have for today's Recibo, Resumen and income screens, against the approved design, so the following tickets build only what is missing.

### Deliverables
A gap list in `docs/design/v2/gaps/` for today's Recibo, Resumen and income screens: each designed element marked present (with its code and tests), partial (what differs) or missing; the following tickets updated, split or closed to match, with owner approval for any scope change.

### Acceptance criteria
- Every element of the v2 Recibo, Resumen and income pages is checked against the running app and the code, not only the docs.
- Each present element names its code path and test; each partial one says exactly what differs.
- Work done outside these tickets (other branches, merged PRs, catalog components) is found and credited.
- SELLO-080 to SELLO-084 are revised to the list before any starts; changed scope is recorded and owner-approved.

### Tests
The audit itself, with screenshots of the running app where it differs from the design; G1.

### Working checkpoint
The gap list is merged and every later ticket in this epic matches it.

### Context and starting points
The screens have been worked on outside these tickets (owner, 2026-10-23); [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md) records the decisions. Design: [v2 README](../../../design/v2/README.md) and `docs/design/v2/components.html` (Exists / Extend / New).

### Implementation plan
1. List every designed element, state and rule.
2. Check each in the app on the emulator and in the code and catalog.
3. Write the gap list and revise the following tickets; ask the owner about scope changes.

### Concrete cases and pitfalls
- Do not trust a document's claim that something exists: open the screen and the code.
- Do not start building while auditing; findings go into the list and tickets.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-079 --retain`.

## SELLO-080 — Lead Resumen with the same-days comparison

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-079, SELLO-004
- **Gate:** G2

### Outcome
Resumen opens with "¿Cómo va este mes?": the difference with the same days of the previous month, and for a past month "¿Cómo cerró …?" comparing full months.

### Deliverables
The comparison worked out in domain from recorded expenses, the v2 hero with two bars on one scale and exact amounts, replacing the kept daily chart; first-month and failed-read states.

### Acceptance criteria
- Up and down use the same neutral ink and arrow; the percentage is exact to one decimal.
- The month in progress compares day 1 to today with the same days of last month; a past month compares whole months.
- A first month says what is missing and shows no comparison; a failed read is never zero.
- "Así crecen tus gastos" is removed only once the hero replaces it.

### Tests
Domain tests with hand-worked examples (month ends, 31 vs 30 days, February); screen tests; G2.

### Working checkpoint
On a phone, Resumen's first slip answers how this month compares, with exact amounts.

### Context and starting points
E13 SELLO-072 left the hero out; `ComparisonSlip` exists in the catalog. Design: "Resumen" in the v2 README.

### Implementation plan
1. Write worked examples and the domain calculation.
2. Wire the hero and states.
3. Remove the daily chart.

### Concrete cases and pitfalls
- Same days means calendar days 1..today, clamped when last month is shorter.
- Never project the month in progress.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-080 --retain`.

## SELLO-081 — Show the average month and the weekday answer in Resumen

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-079, SELLO-004
- **Gate:** G2

### Outcome
Mes a mes starts with "Un mes normal" and Resumen answers "¿Qué días gastas más?".

### Deliverables
The average of the full months shown and the per-weekday average within the selected month, from domain, with their v2 slips and accessible exact values.

### Acceptance criteria
- "Un mes normal" averages only the full months shown, in whole pesos, rounding stated in its examples.
- The weekday answer names the day first ("Los sábados") with its average, then the weekday columns, within the selected month.
- Months or weekdays without data are said, never shown as zero spending.

### Tests
Domain tests with hand-worked averages; screen tests; G2.

### Working checkpoint
Both answers appear with exact figures for the selected month.

### Context and starting points
Weekday period decided in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md). Design: "Resumen" in the v2 README.

### Implementation plan
1. Worked examples.
2. Domain calculations.
3. Slips and tests.

### Concrete cases and pitfalls
- Average per weekday divides by how many of that weekday the month has had, not by recorded days.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-081 --retain`.

## SELLO-082 — Offer one budget suggestion in Resumen

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-079, SELLO-004
- **Gate:** G2

### Outcome
Resumen shows at most one tinted suggestion when a category went over its limit two months in a row.

### Deliverables
The eligibility rule in domain, the v2 suggestion note with its one action (Ajustar el presupuesto de …), dismissal kept per month.

### Acceptance criteria
- Eligible: over its limit in each of the last two full months; the bigger budget wins a tie.
- The evidence is a plain fact and the advice starts with "Podrías…" and ends in one action.
- Dismissing hides it for that month; it may return next month if still true.
- No suggestion in a first month or for a closed month without the two-month evidence.

### Tests
Domain tests for eligibility and ties; dismissal persistence; screen test; G2.

### Working checkpoint
A category over twice in a row shows the suggestion; dismissing it hides it until next month.

### Context and starting points
Rules in the Resumen brief; return-after-dismissal in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md).

### Implementation plan
1. Eligibility in domain with examples.
2. Note, action and dismissal storage.
3. Tests.

### Concrete cases and pitfalls
- The action opens the category's limit editor; it never changes a budget by itself.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-082 --retain`.

## SELLO-083 — Suggest assigning unassigned income

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-079, SELLO-004
- **Gate:** G2

### Outcome
Income history shows the v2 suggestion while part of the month's income has no budget.

### Deliverables
The suggestion note with **Asignar a una categoría ›**, shown only for the current month with a positive unassigned amount.

### Acceptance criteria
- Shown only while unassigned is positive in the current month.
- Not shown for a closed month or a negative unassigned amount.
- The action opens category creation; nothing is assigned automatically.

### Tests
ViewModel and screen tests for each state; G2.

### Working checkpoint
With unassigned income, the suggestion appears and leads to a new category.

### Context and starting points
E13 SELLO-071 drew no suggestion. Design: "Income" in the v2 README.

### Implementation plan
1. State and visibility rule.
2. Note and action.
3. Tests.

### Concrete cases and pitfalls
- Goal or investment records never assign income.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-083 --retain`.

## SELLO-084 — Invite a look at the month just closed

- **Type:** Task
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-079, SELLO-004
- **Gate:** G2

### Outcome
In the first week of a month, Recibo shows one dashed line "Ver cómo cerraste octubre ›".

### Deliverables
The invitation with its timing and dismissal, opening Resumen for the previous month.

### Acceptance criteria
- Shown on days 1–7 when the previous month had expenses; one line, never a stack.
- Gone for that month once opened or dismissed, kept across restarts.
- Not shown when viewing a past month.

### Tests
Clock-controlled ViewModel tests for days 1, 7 and 8; dismissal persistence; screen test; G2.

### Working checkpoint
On day 2 the line appears; opening it shows last month in Resumen and it stays gone.

### Context and starting points
Timing and dismissal decided in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md).

### Implementation plan
1. Timing rule with the financial clock.
2. Line, navigation and dismissal.
3. Tests.

### Concrete cases and pitfalls
- Use the financial day, not the device clock directly.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-084 --retain`.
