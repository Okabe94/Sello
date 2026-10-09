# MVP decisions and independent examples

**Status:** approved inputs from completed SELLO-001; delivery evidence is in the
[canonical ticket](epics/E01-foundation.md#delivery-evidence). D01–D10 were
approved by the project owner on 2026-10-08; see the
[decision record](../../decisions/0001-mvp-contract.md). D04 zero/unlimited,
historical stability and corrections with user guidance are also approved;
unopened months use configuration effective then, not today's default.
signed carryover is deferred beyond MVP, whose budgets renew without carryover.
Detailed carryover policy belongs to SELLO-037; SELLO-001's independent agent
technical/example review passed; see the
[review report](../../testing/reviews/SELLO-001-independent-review.md). Later
implementation/release reviewers must be separately assigned. User owns physical-device testing and release
build/signing; actual evidence is still required. Policy approval is not gate completion.
The guided owner walkthrough is complete, including the revised transaction range,
expense-addition ordering and explicit partial-restore wording. It is not executed
feature/device evidence or a substitute for independent technical/fixture review.
Architecture rules are binding baseline proposals; the choices below must be
ratified/clarified in `docs/decisions/0001-mvp-contract.md`. The executor must name
approver/date and transfer approved inputs into tests. Never set status Accepted
merely because a default seems reasonable.

## Approval checklist

| Decision | Required answer and downstream consumers |
| --- | --- |
| D01 scope | Ratify the exact MVP in architecture §1, two completed product tabs, developer catalog/sandbox, post-MVP recurrence. Consumers: navigation, sandbox, release. |
| D02 identities/support | Approved `com.software.sello`, `.debug` and `.catalog` IDs, distinct Sello / Sello Debug / Sello Catalog labels and isolated side-by-side installation. Approved SDK 30 floor subject to SELLO-002 compatibility verification. Private APK distribution only for MVP; no Play/AAB publication scope. Production signing/device evidence remains required. Iden01/Iden02 cover installation expectations. |
| D03 projection | Approved: elapsed day-of-month includes today; future days exclude today; forecast from day 1. Allowance includes today and rounds down to COP 50 multiples only for the derived daily planning amount. Forecast display rounds HALF_UP to integer COP; verdict compares exact pre-rounding projection. Detailed examples/independent review remain SELLO-001 deliverables. |
| D04 budget lifecycle | Approved: zero differs from unlimited; today's edits cannot rewrite previous limits; historical corrections need guidance; MVP budgets renew without carryover; unopened months use configuration effective then; archive interactions follow D07. Storage implementation belongs to SELLO-011/013; future carryover scope/algorithm to SELLO-037. |
| D05 fields | Approved: trimmed 24-character category/Other-source names, optional 60-character notes, category uniqueness ignoring case/accents including archived names; each expense/income is 1–999,999,999,999 COP across UI/domain/storage/backup, strict plain/grouped-dot input and no silent repair. Oversized transactions invalidate the complete restore before mutation; earlier wider-import support is superseded by ADR 0003. Aggregate totals use checked Long independently of the per-record cap. Stable income keys/labels are in D05. |
| D06 ordering | Approved: Más usadas counts selected-month expenses descending; Alfabético ascends; Monto gastado/Límite descend using selected-month values. Último gasto agregado uses the newest original real creation Instant among selected-month nondeleted expenses; backdated additions count in their effective month, edits/restore preserve creation times, deletion falls back to next surviving addition. No-expense categories remain last within their partition in both directions. Direction reverses comparison, not newest-per-category selection; ties stay alphabetical then stable ID. Unlimited always last; archived rows follow D07. O01–O14 replace the old configuration comparator. |
| D07 archive/undo | Approved archive-only removal/restore/history visibility, retained current budget and no automatic later budgets while archived, with prior explanation. Expense/income undo lasts six real elapsed seconds; same-process recreation cannot extend it; process death expires the offer but recovers durable outcomes. Guard conflicts/reset. |
| D08 privacy/files | Approved manual plaintext JSON/readable-content disclosure, no password/encryption promise, disabled OS backup/device transfer and manual-recovery responsibility. Selected cloud providers may sync independently. Back up history/zone/portable preferences, not device/debug/secrets/undo state; clean temporary files, retain external copies across reset. Wire keys/filename/final copy are SELLO-028/029/032 deliverables. |
| D09 restore/reset | Approved validated, counted replacement (never merge), restored financial zone/portable preferences and invalidated old drafts/undo. Settings-pending recovery retries only settings. Reset requires counts, two confirmations and typed BORRAR; clears finances/preferences to first-run Sistema/Cobalto/Más usadas, with next setup establishing device financial zone. Retain only minimal nonfinancial fencing/completion metadata; external backups survive with explicit warning; system permissions unchanged. R01–R06 are downstream test expectations. |
| D10 release verification | Approved minimum/target SDK emulators using SELLO-002's verified matrix; compact/short-landscape/medium/expanded windows, font scales 1.0/1.3/2.0, four themes and accessibility checks; production-signed physical-device release/recovery test. SELLO-035 measures histories and obtains concrete target approval. User owns product/release go/no-go, periodic physical-device testing and release build/signing. Independent technical reviewer remains unassigned; actual test/signing evidence is still required. Missing evidence is a prerequisite, never an optional pass. |

The decision record should include chosen value, reason, rejected alternative,
example/test implications and any unresolved blocker. Downstream tickets start only
when the answers they depend on are accepted; refer to the record rather than copying
an incompatible policy into a ViewModel.

## Exact baseline examples

These are derived from architecture semantics; SELLO-001 reviews them independently.
All numbers below are integer COP amounts, with zero recurringDue in MVP.

| ID | Inputs | Exact expected outcome |
| --- | --- | --- |
| M01 | COP text `1.234`, approved grouped-input syntax | 1,234 units; draft/display does not convert through floating point |
| M02 | `1e3`, negative expense, amount outside Long range | Typed rejection; original input is not transformed into another amount |
| B01 | Limited category limit 100,000/spend 30,000; unlimited category spend 20,000 | Total spent 50,000; total remaining 50,000; unlimited category still counts |
| B02 | No configured limits; spent 50,000 | Has gastado / Sin límite; no invented budget capacity |
| B03 | 2026-10-31, limit 100,000/spent 90,000 | Remaining 10,000; one day available including today; allowance 10,000; Para hoy |
| B04 | Limit 100,000/spent 110,000 | Remaining −10,000; allowance zero in loss ink; Pasado |
| V01 | Actual 80,000/limit 100,000; projected 95,000 | Al día |
| V02 | Same actual/limit; projected 95,001 or 100,000 | Justo |
| V03 | Same actual/limit; projected 100,001 | Pasado |
| V04 | Actual 100,001/limit 100,000; any lower projection | Pasado; actual overspend cannot be hidden |
| H01 | September limit 100,000/spent 20,000; October limit changed to 120,000 | September remaining stays 80,000; no daily allowance on past-month Recibo |
| I01 | Income 150,000; limits 100,000; actual expenses 30,000 | Ingresos menos gastos 120,000; Restante del presupuesto 70,000; Por asignar al presupuesto 50,000; distinct figures, not verified account balances |
| G01 | Oct 1 expense 10,000; Oct 2 +15,000; Oct 3 +5,000; limit 100,000; income 200,000 | Cumulative spending 10,000/25,000/30,000; final remaining 70,000; Ingresos menos gastos 170,000; Por asignar al presupuesto 100,000 |

Additional D03 projection/allowance examples follow the approved day-cutoff/precision
in the [versioned corpus](../../testing/mvp-financial-examples.md), including COP 50
boundaries. Unknown/read failure has no valid monetary
expected value; it is Failed, optionally with clearly marked last-good data.

## Recovery cases to turn into tests

- Same operation/input submitted twice: one row, one financial revision increment,
  same durable receipt. Same ID/changed input: conflict, no second mutation.
- Cancellation after the Room transaction commits: original receipt is recoverable
  after DB reopen; a follow-up failure cannot remove or mislabel committed data.
- Stale version or stale generation: reject; row and unrelated budgets unchanged.
- Delete then a later conflicting edit/recreation: compensation refuses overwrite.
  Advancing financial time does not shorten or extend real six-second undo.
- Invalid/truncated/over-limit backup: no financial or preference mutation. Valid
  financial replacement + preference failure: committed data and pending settings
  visible after restart; retry resumes settings, not replacement.
- Reset generation followed by old submit/undo/import continuation: no resurrection.
- Replaying interrupted G01 scenario: exactly three expenses and one income;
  graph/receipt/Room totals agree after resume and after a fresh replay.
