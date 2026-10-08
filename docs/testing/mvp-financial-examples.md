# MVP financial example corpus

**Version:** 2 · **Updated:** 2026-10-08 · **Ticket:** SELLO-001
**Status:** D01/D03 policies approved; other policies and independent example review
remain pending. These are not passing app tests.
Numbers use comma grouping here for readability; amounts are integer COP unless
an exact intermediate is shown. UI grouping is es-CO (`1.234`).
Policies/assumptions: [decision record](../decisions/0001-mvp-contract.md).
M/B/V/H/I/G baseline cases originate in [decision inputs](../planning/mvp/DECISION_INPUTS.md).
All reads below succeed unless explicitly stated. MVP recurringDue = 0.

## Input and exact-money boundaries — D05

| ID | Input | Expected result under proposal |
| --- | --- | --- |
| M01 | `1.234` | 1,234 COP; valid grouped original text |
| M02 | `1e3`, `-100`, `9.223.372.036.854.775.808` | Reject syntax/sign/Long overflow respectively; never repair into a different number |
| M03 | `12.34`, `1,5`, `$1234`, empty, expense `0` | Reject; zero finite budget is separately valid |
| M04 | keypad `999999999999`; one more digit | First accepted; further digit does not change value and exposes accessible limit feedback |
| M05 | portable expense 1,000,000,000,000 | Preserve/display full value; note edit retains it; do not truncate to keypad maximum |

## Budget and verdict — D03/D04

| ID | Inputs | Expected outcome |
| --- | --- | --- |
| B01 | finite limit 100,000/spend 30,000; unlimited spend 20,000 | Total spent 50,000; remaining 50,000 |
| B02 | all limits absent; spent 50,000 | Has gastado / Sin límite; no invented capacity/allowance/verdict |
| B03 | 2026-10-31; limit 100,000; spent 90,000 | Remaining 10,000; 1 available day; allowance 10,000; Para hoy |
| B04 | limit 100,000; spent 110,000 | Remaining −10,000; allowance 0 in loss ink; Pasado |
| B05 | explicit limit 0; spent 0, then spent 1 | Al día then Pasado; no percentage division by zero |
| B06 | configured limit 100,000; successfully loaded empty expense list | Actual 0; forecast 0; Al día; zero is not unavailable data |
| B07 | same configuration; storage read fails | Failed, not B06; optional visibly stale last-good value |
| V01 | limit 100,000; actual 80,000; exact forecast 95,000 | Al día |
| V02 | same actual/limit; exact forecast 95,001 or 100,000 | Justo |
| V03 | same actual/limit; exact forecast 100,001 | Pasado |
| V04 | actual 100,001/limit 100,000; hypothetical lower forecast | Pasado; independently exercise actual-overspend precedence |
| V05 | 2027-02-15; spent 50,893; limit 100,000 | Exact forecast 1,425,004/15 = 95,000 + 4/15; display 95,000; **Justo**, not Al día |

V01–V04 are classifier unit inputs, not assertions that every actual/forecast pair
can be produced by the chosen pace model. For V05, compare
`100 × 1,425,004 = 142,500,400` against `95 × 100,000 × 15 = 142,500,000`.
Compare before display rounding; the former is larger by 400.

## Forecast and available days — approved D03

Use elapsed `d`, future `N − d`, available `N − d + 1`. Exact forecast numerator
is `spent × N`, denominator `d`; only final display rounds HALF_UP. Displayed
daily allowance floors the nonnegative rational to a multiple of COP 50:
`50 × floor(remaining / (50 × availableDays))`. No transaction, remaining-budget
or forecast value is quantized to COP 50. All examples have finite limit 100,000.

| ID | Financial date; spent | Exact forecast → displayed COP | Available days; allowance COP | Forecast verdict |
| --- | --- | --- | --- | --- |
| F01 | 2026-10-01; 10,000 | 310,000 → 310,000 | 31; 2,900 | Pasado (estimate, not actual overspend) |
| F02 | 2026-10-15; 40,000 | 1,240,000/15 → 82,667 | 17; 3,500 | Al día |
| F03 | 2026-10-31; 90,000 | 90,000 → 90,000 | 1; 10,000 | Al día |
| F04 | 2027-02-14; 30,000 | 60,000 → 60,000 | 15; 4,650 | Al día |
| F05 | 2028-02-15; 45,000 | 87,000 → 87,000 | 15; 3,650 | Al día |
| F06 | 2026-10-01; 0, with limits and successful read | 0 → 0 | 31; 3,200 | Al día |
| F07 | 2026-10-31; 110,000 | 110,000 → 110,000 | 1; 0 | Pasado |
| F08 | first-run no categories/limits/expenses | No budget forecast | No allowance | First-run state, no invented limit |

F01 intentionally reveals the early-month estimate's sensitivity: one large first-day
expense projects 310,000, not 10,000. The owner explicitly approved this model
after that consequence was explained on 2026-10-08.
F04/F05 verify 28/29-day February; both include today only in observed pace and
allowance, never in future-day multiplication. Past months suppress F-style forecast.

## COP 50 daily-allowance boundaries — approved D03

These are derived planning outputs, never rules for rounding recorded money.
Use checked/widened intermediates; these cases also establish that quantization
is downward, not nearest-increment or down-to-hundreds rounding.

| ID | Remaining COP; available days | Exact unrounded allowance | Displayed allowance COP |
| --- | --- | --- | --- |
| A01 | 59,993; 17 | 3,529 | 3,500 (owner's example) |
| A02 | 60,588; 17 | 3,564 | 3,550 (owner's example) |
| A03 | 849; 17 | 849/17, below 50 | 0; remaining stays positive, not loss semantics |
| A04 | 850; 17 | 50 | 50 |
| A05 | 1,699; 17 | 1,699/17, below 100 | 50 |
| A06 | 1,700; 17 | 100 | 100 |
| A07 | 49; 1 | 49 | 0; last-day remaining still 49 |
| A08 | −10,000; 1 | Clamped to 0 | 0 in loss ink; remaining stays −10,000 |

## History, income and graph reconciliation — D04/D06

| ID | Inputs/action | Expected outcome |
| --- | --- | --- |
| H01 | September limit 100,000/spent 20,000; October default changed to 120,000 | September remaining remains 80,000; no past daily allowance |
| H02 | explicit correction: September limit becomes 90,000 | September remaining 70,000; October still 120,000; no default propagation |
| H03 | October configured; November never materialized; December opened | December snapshots active default; November remains unconfigured, not copied retrospectively |
| H04 | add backdated November expense 10,000 to H03 | November actual 10,000 and unconfigured limit; no fabricated November forecast/budget |
| I01 | income 150,000; limits 100,000; expenses 30,000 | Net recorded cash flow 120,000; Sin destinar 50,000; neither is an account balance |
| I02 | income 50,000; limits 100,000; expenses 30,000 | Net recorded cash flow 20,000; Sin destinar −50,000; planning capacity not clamped to 0 |
| G01 | Oct 1 expense 10,000; Oct 2 +15,000; Oct 3 +5,000; limit 100,000; income 200,000 | Cumulative 10,000/25,000/30,000; remaining 70,000; net cash flow 170,000; Sin destinar 100,000 |
| G02 | edit Oct 2 expense in G01 to 12,000 | Cumulative 10,000/22,000/27,000; remaining 73,000; net cash flow 173,000 |

## Recovery contracts to instantiate in downstream tests — D07–D09

- Same operation/input twice: one record/revision and the same durable receipt;
  same ID with changed input conflicts, with no new mutation.
- Cancellation after transaction commit: reopen and recover the original receipt;
  follow-up failure cannot relabel it as rejected or remove persisted money.
- Stale record version/generation: reject without modifying other records/budgets.
- Delete then conflicting edit/recreation: undo refuses overwrite. At six seconds
  the offer expires; financial-time stepping never changes its deadline. Rotation
  preserves remaining time; proposed process-death policy expires the offer.
- Archive category: history remains and no new entry can target it. Unarchive
  restores eligibility, not altered amounts/limits. Rename changes labels only.
- Invalid/truncated/over-limit backup: no financial or settings changes. Valid
  replacement plus settings failure: new data committed, visible pending settings;
  retry completes settings without replacing financial data a second time.
- Restore/reset advances generation; old draft/undo/import continuation cannot
  resurrect data. Reset clears preferences/history but external backup survives.
- Interrupted G01 sandbox replay: exactly three expenses and one income after
  resume/restart; replay and chart/receipt/Room totals agree.

## Review and verification record

Executor arithmetic verification is distinct from independent product review.
Product approved D01/D03 on 2026-10-08, including the two explicit allowance examples;
the approval log is in the decision record. This does not approve D02/D04–D10 or
claim independent arithmetic review of the full table; that review remains pending.
Future tests must hardcode independently checked expectations, not compute them
with the financial implementation being tested. SELLO-001 cannot be Done until
required decisions, arithmetic review and its documentation/G0 checks are complete.
