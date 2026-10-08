# MVP decisions and independent examples

**Status:** inputs for SELLO-001, not a completed product approval.
Architecture rules are binding baseline proposals; the choices below must be
ratified/clarified in `docs/decisions/0001-mvp-contract.md`. The executor must name
approver/date and transfer approved inputs into tests. Never set status Accepted
merely because a default seems reasonable.

## Approval checklist

| Decision | Required answer and downstream consumers |
| --- | --- |
| D01 scope | Ratify the exact MVP in architecture §1, two completed product tabs, developer catalog/sandbox, post-MVP recurrence. Consumers: navigation, sandbox, release. |
| D02 identities/support | Confirm existing `com.software.sello` product ID, proposed `.debug`/`.catalog` IDs, minSdk/support matrix and release channel. SELLO-002 validates—not guesses—the compatible toolchain. |
| D03 projection | Define elapsed variable-pace days, whether today counts, forecast remaining days, day-1/no-history handling, precision and status comparison before/after rounding. Proposed MVP: elapsed day-of-month includes today; future days exclude today; allowance includes today. Approve worked February/month-end examples. |
| D04 budget lifecycle | Define zero versus unlimited, period creation/carry-forward, missing/skipped months, archived categories and scope of limit edits. Never fill historical gaps using today's configuration without an explicit historical policy. Decide if old-month editing is allowed and how affected months are displayed. |
| D05 fields | Ratify name/note limits, keypad digit limit versus wider import/display range, duplicate category names and stable supported income-source keys. Domain/storage/UI/backup must agree. |
| D06 ordering | Define each order's metric/time range, ascending/descending and ties; unlimited last. “Most used” requires an explicit count window, not guessing between lifetime and selected month. |
| D07 archive/undo | Confirm archive-only category removal in MVP, six-second record undo, restoration/conflict policy and how restart consumes remaining offer time without extension. |
| D08 privacy/files | Approve disclosed plaintext recovery files, exact portable fields, Sello filename/version, retention/cleanup and disabled unapproved OS backup/device transfer. No custom encryption. |
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

Additional D03 projection examples must specify the approved day-cutoff/precision;
do not hardcode a guessed forecast here. Unknown/read failure has no valid monetary
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
