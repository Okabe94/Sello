# MVP financial example corpus

**Version:** 23 · **Updated:** 2026-10-08 · **Ticket:** SELLO-001
**Status:** D01–D10 product policies/process approved; independent example review
and named independent technical reviewer remain pending. User owns physical-device
testing and release build/signing; actual evidence is still required. These are not passing app tests.
Numbers use comma grouping here for readability; amounts are integer COP unless
an exact intermediate is shown. UI grouping is es-CO (`1.234`).
Policies/assumptions: [decision record](../decisions/0001-mvp-contract.md).
M/B/V/H/I/G baseline cases originate in [decision inputs](../planning/mvp/DECISION_INPUTS.md).
All reads below succeed unless explicitly stated. MVP recurringDue = 0.

## Installation and distribution — approved D02

| ID | Action/boundary | Expected policy outcome |
| --- | --- | --- |
| Iden01 | Install customer, debug and catalog builds together | IDs `com.software.sello`, `.debug`, `.catalog`; labels Sello / Sello Debug / Sello Catalog; each has isolated private data; catalog/debug are separate developer artifacts |
| Iden02 | Verify supported release candidate | SDK 30 floor is compatibility-verified in SELLO-002; customer deliverable is a production-signed private APK, not an AAB/store upload or debug-key substitute; actual distribution needs separate authorization |

These are SELLO-002/003/035/036 acceptance expectations, not executed installation,
toolchain, signing or distribution evidence. D10's process is approved; independent
technical review and actual verification remain pending.

## Input and exact-money boundaries — D05

| ID | Input | Expected result under approved D05 policy |
| --- | --- | --- |
| M01 | `1.234` | 1,234 COP; valid grouped original text |
| M02 | `1e3`, `-100`, `9.223.372.036.854.775.808` | Reject syntax/sign/Long overflow respectively; never repair into a different number |
| M03 | `12.34`, `1,5`, `$1234`, empty, expense `0` | Reject; zero finite budget is separately valid |
| M04 | keypad `999999999999`; one more digit | First accepted; further digit does not change value and exposes accessible limit feedback |
| M05 | portable expense or income 1,000,000,000,000; separate valid record 999,999,999,999 | First invalidates complete restore before any mutation; no clipping/skipping. Second accepted and preserved on note edit; same bound as manual entry |
| M06 | two valid expenses of 600,000,000,000 each | Exact aggregate 1,200,000,000,000; transaction cap does not cap totals; true checked Long overflow remains an explicit failure |

## Field/name/source boundaries — approved D05

| ID | Input/action | Expected outcome |
| --- | --- | --- |
| T01 | category name ` Café `, then `cafe` | First saves displayed name `Café`; second conflicts with case/accent-insensitive uniqueness |
| T02 | archived `Café`, then new `CAFE` | Duplicate rejected; archival does not release the name |
| T03 | trimmed name of 24 Unicode code points; 25; whitespace only | First valid; second over-limit rejection; third required-name rejection |
| T04 | note absent; 60 Unicode code points; 61 | First two valid; third rejected rather than truncated |
| T05 | source Otro with no name, then a trimmed 24-code-point name | First rejected; second valid; 25 is over-limit |
| T06 | each approved income source; Transferencia recorded manually | Stable keys map to the approved labels; no inferred account debit or matched bank transfer |

Code-point counting is not UTF-16 code-unit length; boundary tests must include
composed/decomposed accents and supplementary characters. Display normalization and
uniqueness must be consistent across UI/domain/storage/backup. These are policy
expectations, not evidence that the scaffold implements validation.

## Budget and verdict — D03/D04

| ID | Inputs | Expected outcome |
| --- | --- | --- |
| B01 | finite limit 100,000/spend 30,000; unlimited spend 20,000 | Total spent 50,000; remaining 50,000 |
| B02 | all limits absent; spent 50,000 | Has gastado / Sin límite; no invented capacity/allowance/verdict |
| B03 | 2026-10-31; limit 100,000; spent 90,000 | Remaining 10,000; 1 available day; allowance 10,000; Para hoy |
| B04 | overall finite limits total 100,000; overall spent 110,000 | Overall remaining −10,000; overall allowance 0 in loss ink; Pasado; this is not a single-category overspend with other finite capacity remaining |
| B05 | explicit limit 0; spent 0, then spent 1 | Al día then Pasado; no percentage division by zero |
| B06 | configured limit 100,000; successfully loaded empty expense list | Actual 0; forecast 0; Al día; zero is not unavailable data |
| B07 | same configuration; storage read fails | Failed, not B06; optional visibly stale last-good value |
| B08 | 2026-10-22; Food/Transport/Other each has finite limit 100,000; Food spent 110,000, others spent 0 | Owner-confirmed: Food remaining −10,000 and category Pasado; overall limit 300,000/spent 110,000/remaining 190,000; 10 available days → overall allowance 19,000. A category overrun alone must not zero the overall allowance |
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
| H03 | default 100,000 effective October; November unopened; December default changes to 120,000 | Later November read resolves 100,000 from effective-month history; December uses 120,000; no app-open dependency or backward copy of 120,000 |
| H04 | add backdated November expense 10,000 to H03 | November actual 10,000; limit 100,000; remaining 90,000; no past daily allowance/forecast |
| H05 | September spent 20,000/limit 100,000; unchanged default through October | Approved MVP: October limit 100,000, not 180,000; September remaining 80,000 retained in history; no claim that actual funds reset |
| H06 | user discovers past-month editing, then corrects September limit | Contextual announcement and persistent help explain capability; editor names September and configured-limit scope; committed receipt precedes completion message and refreshed totals |
| I01 | income 150,000; limits 100,000; expenses 30,000 | Ingresos menos gastos 120,000; Restante del presupuesto 70,000; Por asignar al presupuesto 50,000; none is a verified account balance |
| I02 | income 50,000; limits 100,000; expenses 30,000 | Ingresos menos gastos 20,000; Restante del presupuesto 70,000; Por asignar al presupuesto −50,000, not clamped to 0 |
| G01 | Oct 1 expense 10,000; Oct 2 +15,000; Oct 3 +5,000; limit 100,000; income 200,000 | Cumulative 10,000/25,000/30,000; remaining 70,000; Ingresos menos gastos 170,000; Por asignar al presupuesto 100,000 |
| G02 | edit Oct 2 expense in G01 to 12,000 | Cumulative 10,000/22,000/27,000; remaining 73,000; Ingresos menos gastos 173,000; Por asignar al presupuesto remains 100,000 |

H02/H06 reflect approved historical correction and guidance; B05 reflects the
approved zero/unlimited distinction. H03/H04 reflect approved skipped-month effective
configuration, independent of app opening; H05 reflects approved MVP no-carry renewal.
Future signed carryover direction and conditional C01/C02 illustrations are recorded
in [ADR 0002](../decisions/0002-signed-carryover-direction.md); they are not active
MVP test expectations. An earlier month before any
known category/default configuration stays unconfigured; H03 is not permission to
invent such history. An explicit November-only correction supersedes its resolved
default without changing October/December or the default version history.

## Category ordering — approved D06

Synthetic fixture; all categories active. Real category/configuration audit times
are independent of selected month and simulated financial time.

| ID/name | Monthly limit COP | October count/spent | September count/spent | Last configuration edit (UTC) |
| --- | --- | --- | --- | --- |
| cat-a / Alimentación | 100,000 | 2 / 30,000 | 1 / 10,000 | 2026-10-05T12:00:00Z |
| cat-c / Café | 50,000 | 3 / 15,000 | 0 / 0 | 2026-10-02T12:00:00Z |
| cat-t / Transporte | 200,000 | 1 / 40,000 | 4 / 20,000 | 2026-10-04T12:00:00Z |
| cat-u / Arte | unlimited | 8 / 80,000 | 6 / 60,000 | 2026-10-06T12:00:00Z |

| Case | Order/action | Expected category IDs |
| --- | --- | --- |
| O01 | October Más usadas descending | cat-c, cat-a, cat-t, cat-u |
| O02 | September Más usadas descending | cat-t, cat-a, cat-c, cat-u |
| O03 | Alfabético ascending; reverse | cat-a, cat-c, cat-t, cat-u; cat-t, cat-c, cat-a, cat-u |
| O04 | October Monto gastado descending | cat-t, cat-a, cat-c, cat-u |
| O05 | Límite descending | cat-t, cat-a, cat-c, cat-u |
| O06 | Última actualización descending | cat-a, cat-t, cat-c, cat-u |
| O07 | October fixture but Café count becomes 2; count descending, then ascending | cat-a, cat-c, cat-t, cat-u; cat-t, cat-a, cat-c, cat-u |
| O08 | Step financial date without any audit edit, keep selection/fixture fixed | Last-update order remains O06; simulated time is not a configuration edit |

Arte remains last despite highest count/spending, newest edit and alphabetic placement;
reversing the metric never reverses the unlimited partition or alphabetical tie-break.
Amounts/counts/limits come from the same selected-month snapshot. Archive visibility
and budget lifecycle cases follow approved D07 below.

## Archive and undo lifecycle — approved D07

All records are synthetic. Monetary values are exact; clock times below are controlled
monotonic elapsed times, not financial calendar dates or audit timestamps.

| ID | Inputs/action | Expected outcome |
| --- | --- | --- |
| AR01 | October category limit 100,000/spent 20,000; archive in October | October remaining 80,000 and saved limit retained; row visible as archived; absent from new-entry choices |
| AR02 | AR01 remains archived throughout November | No automatic November budget; October history unchanged; no invisible expense/limit deletion |
| AR03 | Unarchive AR01 within October | Existing October limit remains 100,000; eligibility returns; no duplicate category identity |
| AR04 | Unarchive in December; latest configured default 100,000; no December override | December limit uses 100,000; explain restoration before commit; no retrospective November budget |
| AR05 | Same as AR04, but explicit December limit 90,000 already exists | Preserve December override 90,000 instead of replacing it with the default |
| AR06 | Archived category has a saved limit but no expenses; rename it | Relevant period row remains visible; historical label changes, identity/limits/amounts do not |
| U01 | Confirmed delete at elapsed t=0; undo at t=5.999s, then separate case t=6s | First eligible if versions/generation match; at deadline expired; real-Room tests prove boundaries |
| U02 | Rotate/background at t=4s within same process | Original deadline remains t=6s, never a new six-second interval |
| U03 | Process ends at t=2s; reopen | No renewed undo offer, even if reopened quickly; recover committed deletion outcome |
| U04 | Compensation commits; process ends before success feedback | Reopen recovers compensation receipt and restored original row; expiry cannot roll back committed undo |
| U05 | Advance financial clock or attempt undo after newer conflict/reset | Financial stepping does not change timer; stale/conflicting/reset-generation compensation rejects without overwrite/resurrection |

## Backup privacy and retention — approved D08

| ID | Action/boundary | Expected policy outcome |
| --- | --- | --- |
| P01 | Export manual backup | Before destination/save, explain unencrypted readable financial contents and no password protection; successful close/write precedes saved feedback |
| P02 | Choose cloud-backed destination | Explain provider may sync/store the copy; do not claim Sello performs cloud sync or can control/delete provider copies |
| P03 | Reset app after successful external backup | External copy is not deleted by Sello; private staging is cleaned on completion/failure/cancellation/startup recovery |
| P04 | Inspect portable payload and merged release backup rules | Approved history/zone/portable settings retained; permissions/debug/secrets/pending undo excluded; OS-managed cloud/device-transfer backup disabled; manual recovery responsibility disclosed |

These are acceptance expectations for SELLO-028/029/032, not executed backup/device
tests. The generated manifest's baseline backup setting still needs remediation
before financial data is stored, as required by SELLO-011.

## Restore and reset — approved D09

| ID | Action/boundary | Expected policy outcome |
| --- | --- | --- |
| R01 | Invalid/truncated/unsupported backup, or cancel validated replacement preview | Financial data, generation and preferences unchanged; no success feedback |
| R02 | Current history contains 2 expenses; confirm valid G01 backup containing 3 expenses/1 income | Preview both counts and replacement warning; final history is exactly the backed-up 3 expenses/1 income, not merged; restore backed-up zone/appearance/order, not permissions/debug/URI grants |
| R03 | R02 commits financially; preference write fails, then reopen/retry | Exact restored financial history remains committed; show settings pending, not complete Restaurado; resume settings only without a second replacement or generation advance |
| R04 | Submit old draft or undo after restore/reset, even with matching restored record IDs | Reject stale generation; no mutation or resurrection; uncertain original replacement outcome is recovered by its original operation ID |
| R05 | Cancel reset at either confirmation or enter text other than exact BORRAR | All financial data and preferences unchanged; no reset completion |
| R06 | Confirm counted reset with BORRAR, then restart | No prior finances/categories/month limits/names/notes/portable preferences; first-run defaults Sistema/Cobalto/Más usadas; next setup uses device financial zone; retain only minimal nonfinancial fencing/completion identity; permissions unchanged; external backups survive with explicit warning |

These are acceptance expectations for SELLO-030/031, not executed recovery tests.
Real-Room rollback/reopen and device journeys must prove the eventual implementation.

## Recovery contracts to instantiate in downstream tests — D07–D09

- Same operation/input twice: one record/revision and the same durable receipt;
  same ID with changed input conflicts, with no new mutation.
- Cancellation after transaction commit: reopen and recover the original receipt;
  follow-up failure cannot relabel it as rejected or remove persisted money.
- Stale record version/generation: reject without modifying other records/budgets.
- Delete then conflicting edit/recreation: undo refuses overwrite. At six seconds
  the offer expires; financial-time stepping never changes its deadline. Rotation
  preserves remaining time; approved process-death policy expires the offer but
  recovers actual deletion/compensation outcomes.
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

### Guided owner walkthrough

Requested on 2026-10-08; review in small illustrative batches, recording the user's
response against explicit example IDs. Requesting the review is not approving its
results. Executor explanation/checks are not independent technical review or app tests.

| Batch | Examples | Review focus | Status |
| --- | --- | --- | --- |
| 1 | B01/B02/B04/B05/B06/B07/B08 | Remaining budget; unlimited expenses still count; no-limit versus zero; category versus overall overspending; failed reads are not empty histories | Owner-confirmed, including B04/B08 scope clarification; independent technical review remains separate |
| 2 | F01–F08/A01–A08/V01–V05 | Day inclusion, estimate sensitivity, COP 50 planning rounding and exact verdict boundaries | Owner-confirmed across both walkthroughs; implementation and independent technical review remain separate |
| 3 | H01–H06/I01–I02/G01–G02 | Historical stability/renewal, cash-flow versus budget allocation and graph reconciliation | Owner-confirmed, including G01/G02 after allocation-label clarification; independent technical review remains separate |
| 4 | M01–M06/T01–T06/O01–O08/AR01–AR06/U01–U05/P01–P04/R01–R06/Iden01–Iden02 | Input/order/archive/undo/file/recovery/installation edge cases | Owner confirmed M01–M04 and rejected wider-import support; M05 revised to rejection, M06 clarifies aggregate scope; other cases not yet reviewed |

2026-10-08 batch 1 feedback: owner sees the plan as a useful reflection of personal
funds and money movement, with future signed carryover important. Accepted focusing
on the MVP plan first, not claiming verified bank reconciliation. Confirmed unlimited
spending counts, zero versus no-limit and failed-read versus empty-history behavior.
Questioned zero allowance for one over-budget category when others have unspent
capacity. B04 originally described exhausted overall capacity; B08 makes the scope
distinction explicit. No blanket acceptance of B04/B08 or independent review is claimed.
Follow-up on 2026-10-08: owner confirmed the explicit distinction and B08's 19,000
overall allowance. B04's zero applies only to exhausted overall finite capacity;
it is not a bank balance or an entry prohibition. Batch 1 is owner-reviewed.

Batch 2 first walkthrough uses October's 31-day calendar: F02 has 40,000 spent by
Oct 15, 60,000 remaining and 17 available days → 3,500 displayed daily allowance;
exact forecast 40,000 × 31 / 15 → 82,667 displayed COP. F01's first-day 10,000
expense forecasts 310,000 but is not actual overspending. F03's last-day 90,000
spend leaves 10,000 for today and forecast equals actual. A01/A02 reinforce
downward COP 50 planning rounding; these walkthrough outcomes await owner response.
2026-10-08 follow-up: owner confirmed the three presented calendar examples and
rounding. This covers F01's forecast, F02/F03/B03 and A01/A02, not F01's unpresented
allowance or every F/A/V boundary. The next walkthrough covers first-day allowances
2,900/3,200 (F01/F06), 28/29-day February (F04/F05), no-budget/overspend (F08/F07),
sub-50/50/100/negative rounding boundaries (A03–A08) and exact verdicts (V01–V05).
Responses to these remaining cases are pending; full independent review is not claimed.
2026-10-08 follow-up: owner confirmed all remaining batch 2 examples, including
positive remaining money with rounded-zero allowance and exact-value verdicts.
No executed feature/device tests or independent technical review are implied.

Batch 3 walkthrough distinguishes income minus recorded expenses (net cash flow)
from income minus configured limits (Sin destinar), and both from remaining budget.
It presents stable prior limits, explicit historical correction/guidance, unopened
months/backdated expenses, MVP no-carry renewal and cumulative graph correction.
H01–H06/I01–I02/G01–G02 response is pending; future carryover examples remain
conditional directions, not a newly approved implementation formula.
2026-10-08 response: owner confirmed items 1–4, covering I01/I02 and H01–H06.
Item 5's graph recalculation/reconciled totals (G01/G02) was not answered; do not
record it as reviewed by implication. Re-present that example before closing batch 3.
2026-10-08 label/meaning clarification: owner accepted keeping both planning and
recorded movement, with Ingresos menos gastos prominent in Resumen, remaining/
allowance in Recibo and Por asignar al presupuesto secondary. Current I/G labels
reflect that approval; old Sin destinar references in this chronological log are
historical wording. Allocation meaning is confirmed, but the full G01/G02 numeric
graph review is not claimed complete from acceptance of the presentation suggestion.
2026-10-08 graph follow-up: owner explicitly confirmed the G01/G02 expense-only
correction effects and unchanged allocation. Batch 3 is owner-reviewed; this is
not execution evidence or full independent technical review.

Batch 4 money walkthrough presents plain/grouped whole-peso input, rejection rather
than repair, positive exact expenses/income versus zero budgets, 12-digit keypad
feedback, bounded overflow rejection and valid wider backup values preserved when
editing notes. M01–M05 response pending; COP 50 rounding applies only to derived
daily planning amounts, not recorded transactions. No new input policy is inferred.
2026-10-08 response: owner confirmed input items 1–3 and rejected oversized
backup-only transaction support in item 4. M05's old preservation expectation is
superseded: UI/domain/storage/backup share the 12-digit transaction cap. ADR 0003
and M06 distinguish that cap from aggregate totals; no storage/restore tests or
review of other batch 4 examples is claimed.

Executor arithmetic verification is distinct from independent product review.
Product approved D01–D10 on 2026-10-08, including the two explicit allowance examples;
the approval log is in the decision record. Future signed carryover direction is
confirmed and deferred beyond MVP; this does not approve its full algorithm,
or claim independent arithmetic review of the full table; that review remains pending.
The approved D10 process requires an independent technical reviewer, still unassigned.
Future tests must hardcode independently checked expectations, not compute them
with the financial implementation being tested. SELLO-001 cannot be Done until
required decisions, arithmetic review and its documentation/G0 checks are complete.
