# MVP decisions and independent examples

**Status:** inputs for SELLO-001, not a completed product approval. D01/D03–D08 were
approved by the project owner on 2026-10-08; see the
[decision record](../../decisions/0001-mvp-contract.md). D04 zero/unlimited,
historical stability and corrections with user guidance are also approved;
unopened months use configuration effective then, not today's default.
signed carryover is deferred beyond MVP, whose budgets renew without carryover.
Detailed carryover policy belongs to SELLO-037; remaining decisions are not approved.
Architecture rules are binding baseline proposals; the choices below must be
ratified/clarified in `docs/decisions/0001-mvp-contract.md`. The executor must name
approver/date and transfer approved inputs into tests. Never set status Accepted
merely because a default seems reasonable.

## Approval checklist

| Decision | Required answer and downstream consumers |
| --- | --- |
| D01 scope | Ratify the exact MVP in architecture §1, two completed product tabs, developer catalog/sandbox, post-MVP recurrence. Consumers: navigation, sandbox, release. |
| D02 identities/support | Confirm existing `com.software.sello` product ID, proposed `.debug`/`.catalog` IDs, minSdk/support matrix and release channel. SELLO-002 validates—not guesses—the compatible toolchain. |
| D03 projection | Approved: elapsed day-of-month includes today; future days exclude today; forecast from day 1. Allowance includes today and rounds down to COP 50 multiples only for the derived daily planning amount. Forecast display rounds HALF_UP to integer COP; verdict compares exact pre-rounding projection. Detailed examples/independent review remain SELLO-001 deliverables. |
| D04 budget lifecycle | Approved: zero differs from unlimited; today's edits cannot rewrite previous limits; historical corrections need guidance; MVP budgets renew without carryover; unopened months use configuration effective then; archive interactions follow D07. Storage implementation belongs to SELLO-011/013; future carryover scope/algorithm to SELLO-037. |
| D05 fields | Approved: trimmed 24-character category/Other-source names, optional 60-character notes, category uniqueness ignoring case/accents including archived names; positive 12-digit COP keypad with strict plain/grouped-dot input and no silent repair; wider valid backup amounts remain exact. Stable income keys and displayed labels are in D05. Domain/storage/UI/backup must agree. |
| D06 ordering | Approved: Más usadas counts selected-month expenses descending; Alfabético ascends; Monto gastado/Límite descend using selected-month values; Última actualización descends by real category/budget audit time, not expense date. Direction reverses only the primary metric; ties stay alphabetical then stable ID. Unlimited always last; archived rows follow approved D07 visibility. |
| D07 archive/undo | Approved archive-only removal/restore/history visibility, retained current budget and no automatic later budgets while archived, with prior explanation. Expense/income undo lasts six real elapsed seconds; same-process recreation cannot extend it; process death expires the offer but recovers durable outcomes. Guard conflicts/reset. |
| D08 privacy/files | Approved manual plaintext JSON/readable-content disclosure, no password/encryption promise, disabled OS backup/device transfer and manual-recovery responsibility. Selected cloud providers may sync independently. Back up history/zone/portable preferences, not device/debug/secrets/undo state; clean temporary files, retain external copies across reset. Wire keys/filename/final copy are SELLO-028/029/032 deliverables. |
| D09 restore/reset | Confirm which preferences reset/restore, financial-zone policy, source counts, draft invalidation and what minimal completion metadata survives erasure. |
| D10 release verification | Agree API/device/window matrix, reviewer/approval owner and performance target-setting process. Unavailable credentials/devices are explicit prerequisites, not optional passes. |

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
| I01 | Income 150,000; limits 100,000; actual expenses 30,000 | Net cash flow 120,000; undestined planning capacity 50,000; these are distinct figures |
| G01 | Oct 1 expense 10,000; Oct 2 +15,000; Oct 3 +5,000; limit 100,000; income 200,000 | Cumulative spending 10,000/25,000/30,000; final remaining 70,000; net cash flow 170,000; undestined 100,000 |

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
