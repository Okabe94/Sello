# SELLO-E16 — Finish Sello v2 on today's screens

- **Status:** In Progress
- **Increment:** Post-MVP
- **Goal:** Add the parts of the approved v2 Recibo, Resumen and income screens that the MVP left out, which need no new kind of data.
- **Exit:** Resumen leads with the same-days comparison and shows the average month, the weekday answer and one suggestion; income history suggests assigning unassigned income; Recibo invites a look at the month just closed.

**Owner direction (2026-10-23):** E13 built only what existing data supports and left
these for separate tickets. Decisions for their open points are in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); the
rest of each rule is in the [v2 README](../../../design/v2/README.md) and the
[Resumen and income brief](../../../design/screen-briefs/summary-and-income.md).

**Owner decision (2026-10-23):** SELLO-080, SELLO-081 and SELLO-082 were built before
the SELLO-079 audit, in owner-directed design work on the running app, and are
delivered without waiting for it; they no longer depend on SELLO-079, which credits
them. The weekday period is the last six months (ADR 0017, amended).

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
- **Status:** Done
- **Depends on:** SELLO-004
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

### Delivery evidence

- **Revision:** tested snapshot is branch `sello-080-082-resumen-v2` with `main`
  (`f5864bf`) merged in, identified exactly by the retained input fingerprint below;
  the commit and pull request follow this evidence.
- **Decisions:** the owner chose, on 2026-10-23, to deliver this before the SELLO-079
  audit (E16 note). The kept daily chart ("Así crecen tus gastos") is removed, as the
  SELLO-072 decision allowed once the hero replaced it; the domain still computes the
  daily series, unused by Resumen.
- **Requirement mapping:** `ResumenPolicy` adds `SpendingComparison`: the month in
  progress compares day 1 to today with the same days of the month before, capped at
  its length; a finished month compares whole months; there is no comparison when the
  month before has nothing recorded. The difference is never negative, with a
  direction; the rate is in tenths of a per cent, half up, or absent against zero.
  `ResumenRenderModel` builds "¿Cómo va este mes?" / "¿Cómo cerró …?" and
  `ComparisonSlip` draws it: the difference, "más/menos/lo mismo que en …", the rate
  with ↑/↓ in the same neutral ink, the range ("Del 1 al 24 de cada mes", both ranges
  when capped, or "Meses completos") and one line per month with its bar on one scale
  and its exact amount. Without a comparison the month's spending stands alone; a first
  month adds "Cuando completes otro mes podrás comparar los mismos días.". Loading,
  failed (never zero) and stale states are unchanged.
- **Red / Green:** with the new snapshot fields still null, `ResumenInsightsTest`'s
  comparison cases (same days, 31 Mar vs 28 Feb, 31 Oct vs 30 Sep, whole months, a
  zero rate base) and `ResumenRenderModelTest`'s hero cases failed; all passed after
  the change. `ResumenScreenTest` covers the hero in progress, capped ranges, a finished
  month, no rate, a first month, the one-line bars and the explanation's 16sp size.
- **Gate results:** `./scripts/verify-ticket SELLO-080 --retain` passed all seven checks (board, board tests, quality tests, performance tests, architecture, host, device) on the merged working tree. Host: 743 tests, results reused from Gradle's cache; device: 753 tests run fresh (app 360, data 236, design-system 136, catalog 21); zero failures, errors or skips. Lint had zero errors and 34 warnings.
- **Quality run:** run 20261023T214246Z-081b97a6; SELLO-080 G2 passed; HEAD f01d983, inputs sha256 0a5aa7a8b1bf; report docs/planning/mvp/quality-reports/SELLO-080.json sha256 e5b101792e559c1876cdfed28a075dac97bbfa8a821e23d84a5b437df91572d7
- **Device / Artifact:** the gate ran on an isolated API-30 AVD (320 × 640dp) the
  executor created; screenshots on a second isolated Pixel 6 AVD with the "Seis meses"
  sandbox scenario are in `docs/testing/evidence/resumen-v2/`. No personal data or
  physical device was used for evidence.
- **Review:** executor self-review only. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

## SELLO-081 — Show the average month and the weekday answer in Resumen

- **Type:** Task
- **Priority:** P2
- **Status:** Done
- **Depends on:** SELLO-004
- **Gate:** G2

### Outcome
Mes a mes starts with "Un mes normal" and Resumen answers "¿Qué días gastas más?".

### Deliverables
The average of the full months shown and the per-weekday average over the last six months (the selected one included), from domain, with their v2 slips and accessible exact values.

### Acceptance criteria
- "Un mes normal" averages only the full months shown, in whole pesos, rounding stated in its examples.
- The weekday answer names the day first ("Los sábados") with its average, then the weekday columns, over the last six months or from the first recorded day, saying which.
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
- Average per weekday divides by how many of that weekday the window has had, not by recorded days.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-081 --retain`.

### Delivery evidence

- **Revision:** tested snapshot is branch `sello-080-082-resumen-v2` with `main`
  (`f5864bf`) merged in, identified exactly by the retained input fingerprint below;
  the commit and pull request follow this evidence.
- **Decisions:** delivered before the SELLO-079 audit (owner, 2026-10-23). The owner
  chose a six-month weekday window after seeing the selected-month and four-month
  versions on a phone; ADR 0017 and this ticket's text are amended to match.
- **Requirement mapping:** `NormalMonth` averages the complete months with something
  recorded, half up to whole pesos; a month with nothing recorded is left out, not
  counted as zero, and it is hidden while history is insufficient. "Mes a mes" shows
  "Un mes normal" with "Promedio de N meses completos" above the columns, whose short
  labels ("561 mil", "Límite 1 M") are exact when selected or spoken.
  `WeekdayPattern` averages each weekday over how many times it came round, from the
  first recorded day but no further back than six months including this one; it shows
  after a week and names the busiest day only when one stands out. "¿Qué días gastas
  más?" answers "Los sábados" with "36.353 en promedio · últimos 6 meses" (or "desde el
  3 de mayo"), then seven columns, the busiest with its short value.
- **Red / Green:** `ResumenInsightsTest`'s normal-month and weekday cases (half-up
  averages, gaps, finished months, ties, the six-month cap) and `ResumenRenderModelTest`
  failed before the calculations and passed after; `ChartLabelTest` failed on a stub
  and passed. `ResumenScreenTest` covers both slips, exact selection and both periods.
- **Gate results:** `./scripts/verify-ticket SELLO-081 --retain` passed all seven checks (board, board tests, quality tests, performance tests, architecture, host, device) on the merged working tree. Host: 743 tests, results reused from Gradle's cache; device: 753 tests run fresh (app 360, data 236, design-system 136, catalog 21); zero failures, errors or skips. Lint had zero errors and 34 warnings.
- **Quality run:** run 20261023T215417Z-732ec24d; SELLO-081 G2 passed; HEAD f01d983, inputs sha256 0a5aa7a8b1bf; report docs/planning/mvp/quality-reports/SELLO-081.json sha256 83ae99a3fa29d5f6ca46ece88f78d5a63c895c4fa43a52d17957ce6773d17ca1
- **Device / Artifact:** as SELLO-080; `2-un-mes-normal.png` and
  `3-que-dias-gastas-mas.png` in `docs/testing/evidence/resumen-v2/`.
- **Review:** executor self-review only. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

## SELLO-082 — Offer one budget suggestion in Resumen

- **Type:** Task
- **Priority:** P2
- **Status:** Done
- **Depends on:** SELLO-004
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

### Delivery evidence

- **Revision:** tested snapshot is branch `sello-080-082-resumen-v2` with `main`
  (`f5864bf`) merged in, identified exactly by the retained input fingerprint below;
  the commit and pull request follow this evidence.
- **Decisions:** delivered before the SELLO-079 audit (owner, 2026-10-23). A finished
  month never shows a suggestion, as the v2 past-month state shows.
- **Requirement mapping:** `LimitAdjustment` is offered only for the month in progress:
  a category still taking expenses, with a finite limit now, over its limit in each of
  the two complete months before; the bigger limit wins. `SuggestionNote` says the fact
  ("… superó su límite en agosto y septiembre: gastaste … en promedio y su límite es
  …"), "Podrías ajustar su límite…" and one action, **Ajustar el presupuesto de …**,
  which opens that category's editor and changes nothing itself. × stores the month
  and category in preferences (`resumen_suggestion_dismissed_v1`, `SuggestionPreferences`);
  it stays hidden that month and may return the next; a dismissal that cannot be read
  hides nothing.
- **Red / Green:** `ResumenInsightsTest`'s suggestion cases, the dismissal store tests
  in `DataStoreUserPreferencesTest` and `ResumenViewModelTest`'s dismissal cases failed
  on stubs and passed after; `ResumenScreenTest` covers the note, its action and ×.
- **Gate results:** `./scripts/verify-ticket SELLO-082 --retain` passed all seven checks (board, board tests, quality tests, performance tests, architecture, host, device) on the merged working tree. Host: 743 tests, results reused from Gradle's cache; device: 753 tests run fresh (app 360, data 236, design-system 136, catalog 21); zero failures, errors or skips. Lint had zero errors and 34 warnings.
- **Quality run:** run 20261023T220238Z-0a419ebd; SELLO-082 G2 passed; HEAD f01d983, inputs sha256 0a5aa7a8b1bf; report docs/planning/mvp/quality-reports/SELLO-082.json sha256 8661c127e852be4002977167315c2554bec650771327346b1d2c6dc7af058776
- **Device / Artifact:** as SELLO-080. The sample scenarios have no category over its
  limit twice in a row, so the note is shown by screen tests, not a screenshot.
- **Review:** executor self-review only. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

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
