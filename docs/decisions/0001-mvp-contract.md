# 0001 — MVP product and financial contract

**Ticket:** SELLO-001 · **Version:** 2 · **Updated:** 2026-10-08
**Status:** Partially approved: D01 and D03 approved; other decisions remain proposed.
**Decision owner/approver:** project owner (user). **Executor:** coding agent.
**Approval/date:** project owner (user), 2026-10-08, for D01 and D03 only;
reviewing this draft or authorizing ticket execution is not approval of its policies.

## Authority and execution boundary

[Architecture](../../ARCHITECTURE.md) owns mandatory technical contracts; the
[design](../design/sello-spec.html) owns presentation. This record resolves product
choices from [D01–D10](../planning/mvp/DECISION_INPUTS.md), not a second architecture.
[Worked examples](../testing/mvp-financial-examples.md) reflect the approved D03
policy and remaining proposals, not executed feature tests. No financial functionality
exists in the scaffold. Unapproved decisions need approval before dependent implementation. Changes to an
existing guardrail require explicit approval and a corresponding architecture update.

## D01 — Scope and first release

**Approved choice (project owner, 2026-10-08):** the exact architecture MVP: manual COP expense/income,
categories, monthly limits, Recibo/detail, basic Resumen with graphs and clearly
labeled forecast, edit/delete/undo, appearance/order preferences, backup/restore/reset.
Ship two working tabs, Recibo and Resumen. Develop an isolated time-testing sandbox
and a separately installable, grouped component catalog alongside the first slice.

**Already expressed intent:** user requested modernization, no widgets, MVP-first
growth, live temporal testing and a separate component showcase. This is evidence
of intent, not blanket approval of all financial/recovery choices in this record.

**Reason/alternative:** ship complete daily-use journeys instead of empty destinations.
Do not add Patrimonio, Metas, investments, recurrence, batch entry, FX, analytical
exports or alerts to MVP. Keep them on the roadmap; never add a hidden recurrence
engine merely for sandbox fixtures. No legacy database import in MVP.
**Consequences/tests:** two-tab navigation; release excludes developer tools; sandbox
uses real manual commands/storage. **Open:** none for scope; approval does not
ratify the separate historical-budget, privacy or destructive-action proposals.

## D02 — Identities and supported installation

**Proposed choice:** customer `com.software.sello`; debug `com.software.sello.debug`;
catalog `com.software.sello.catalog`. Distinct launcher labels: Sello, Sello Debug,
Sello Catalog. Install all three side by side with isolated private data.
Keep the inspected minimum SDK 30 as the proposed product floor; SELLO-002 must
validate compatibility before any toolchain/support decision is accepted.

**Reason/alternative:** retain customer identity and prevent debug/customer storage
collisions. Reject shared IDs or developer controls in customer releases.
**Consequences/tests:** install coexistence and release dependency/artifact inspection
in SELLO-003/004/032/035. SDK 37 is an inspected compile/target setting, not a
ratified support guarantee or a claim about store compliance.
**Open:** minimum SDK approval; release channel (private distribution or store);
SELLO-002 compatibility evidence. Exact library versions remain outside this record.

## D03 — Forecast, allowance and verdict

**Approved choice (project owner, 2026-10-08):** use recorded effective expenses from day 1 through today in
the selected current month. Let `S` be spent, `L` the sum of finite monthly limits,
`d` today's day-of-month and `N` month length. In MVP recurringDue is zero:

- Daily pace = exact rational `S / d`; unrecorded days contribute no recorded spend.
- Forecast = `S + (S / d) × (N − d)` = `S × N / d`. Today counts in observed
  pace, but not in future days. Every current-month forecast is labeled an estimate
  based on recorded expenses; never imply complete bank activity or saved actuals.
- Available days = `N − d + 1`; daily allowance =
  exact rational `max(L − S, 0) / availableDays`. The planning amount shown to
  the user MUST round down to a multiple of COP 50:
  `50 × floor(max(L − S, 0) / (50 × availableDays))`, using widened arithmetic.
  Thus 3,529 → 3,500 and 3,564 → 3,550. This includes today and never rounds
  above the remaining budget. It affects only the derived daily planning amount:
  expenses, income, remaining budget, exact pace/forecast and verdict comparisons
  retain their original precision. Positive allowance below 50 displays 0 without
  implying overspending; only negative remaining budget uses loss semantics.
- Show forecast rounded HALF_UP to integer COP, but classify its **exact** value:
  Al día when `100 × forecast ≤ 95 × L`; Justo above that through `forecast ≤ L`;
  Pasado if actual or forecast exceeds L. Use widened exact comparisons.
- Successful empty reads with configured limits yield S=0, forecast=0 and Al día,
  matching the design. No finite limit means Has gastado / Sin límite, not a
  projected budget verdict. Read failure is Failed, never S=0 or Sin límite.
- Forecast on day 1 is allowed but still labeled an estimate; no invented minimum
  history threshold. Past months show actuals only: Bajo el límite or Pasado;
  no daily allowance. Resumen's actual ratio must not masquerade as Recibo's forecast.

**Reason/alternative:** one predictable month-to-date model. Reject active-spending-day
averages (ignore zero-recorded-spend days), counting today twice, and comparing
rounded projections. Reject rounding allowance to the nearest 50 or down to hundreds;
both differ from the approved increments. An elapsed-complete-days model would avoid partial-day pace but
requires distinct day-1/no-history behavior; it is an alternative, not implemented.
**Consequences/tests:** F01–F08, A01–A08, V01–V05 and B03/B04 below; graph calculations
reuse the same snapshot. **Open:** none for model, early-month behavior and rounding;
related zero-budget/history choices in D04 remain proposed. Independent example
review and implementation verification remain outstanding.

## D04 — Budget lifecycle and historical corrections

**Proposed choice:** finite zero is a real zero budget; unlimited is explicit absence
of a limit. Zero does not mean unlimited. Unlimited-category expenses still consume
the overall finite budget. A zero-only finite budget is Al día at zero spending and
Pasado for any positive spending; do not divide by zero for a ratio/progress graphic.

Category creation/default-limit changes snapshot the current month's limit and
set the default for subsequently opened current months. Never rewrite older months.
Persist each materialized month's limits; materialization is idempotent and atomic.
Do not precreate future months. For skipped historical months without a snapshot,
show limits as unconfigured, not reconstructed from today's defaults. Backdated
expenses may be entered up to today; their month gets explicit unconfigured limits
unless that month's snapshot already exists. A user can explicitly configure/correct
a past month's limits, with confirmation saying which month is affected; it does
not change current/future defaults. Correcting dated records recomputes affected
actuals/graphs, not unrelated monthly budgets.

**Reason/alternative:** preserve known history without inventing historical settings.
Reject silent retrospective carry-forward or prohibiting all legitimate corrections.
**Consequences/tests:** H01–H04, zero/unlimited, skipped months, restart/idempotency.
**Open:** approve explicit past-month correction and skipped-month policy.

## D05 — Fields, input grammar and income labels

**Proposed choice:** category names 1–24 Unicode code points, trimmed, unique ignoring
case and accents (including archived names); notes optional, maximum 60 code points.
Normalize category uniqueness consistently and reject control characters; preserve
display accents. Unicode normalization/case-fold details require deterministic tests.
Income keys: `salary`, `freelance`, `passive_income`, `transfer`, `other`, displayed
as Salario, Freelance, Ingresos pasivos, Transferencia, Otro. Otro requires a trimmed
1–24-code-point source name. All income records are manually declared inflows, not
bank-reconciled revenue; Transferencia does not infer a transfer between tracked accounts.

Expense/income keypad maximum is 12 digits (999,999,999,999 COP), following design.
Domain/storage/backup can preserve any positive checked Long COP value; imports
with larger values remain displayable/editable without truncation, and editing
other fields preserves that amount. Pasted input accepts ASCII digits or correctly
grouped es-CO dots (`1.234`); reject decimal commas, exponent notation, negatives,
currency symbols, malformed grouping or overflow. Leading/trailing whitespace may
be trimmed explicitly; no other character removal. Keypad emits ungrouped digits.

**Reason/alternative:** preserve design limits without constraining portable financial
truth to the keypad. Reject sanitizing invalid input into a different amount.
**Consequences/tests:** M01–M05, accents/emoji boundaries, archived duplicate names,
large imported amounts, mandatory Otro name. **Open:** approve grammar and limits.

## D06 — Ordering

**Proposed choice:** default Más usadas = count of nondeleted expenses in the selected
month, descending. Other design choices: Alfabético = normalized name ascending;
Monto gastado = selected-month spent descending; Límite = that month's finite limit
descending; Última actualización = real category/configuration audit Instant descending
(not latest expense date). Expose direction for each metric. Unlimited categories
always last regardless of direction; equal metrics tie by normalized name ascending,
then stable category ID. Archived categories with selected-month expenses remain
visible in history, but cannot be selected for new expenses until unarchived.

**Reason/alternative:** selected-month ordering explains what the user is viewing;
reject undocumented lifetime counts and financial-time changes affecting audit order.
**Consequences/tests:** cross-month/direction/tie/unlimited/archive cases in SELLO-023.
**Open:** approve metric windows, update meaning, and archive visibility.

## D07 — Archive and undo

**Proposed choice:** category removal archives only; retain history and monthly limits.
Offer explicit unarchive; renaming updates displayed category name across history,
but no dates/amounts/limits. A six-second record-delete undo uses durable guarded
compensation. Its live deadline is monotonic, independent of financial-time travel.
Rotation/recreation within the same live process never restarts the timer. Following
process death, recover commit status but expire the interactive undo offer rather
than invent a reliable remaining monotonic interval across restart; no hidden
automatic restoration. Conflicting changes/generation invalidation reject compensation.

**Reason/alternative:** no destructive category cascades or extended undo from restart.
Restoring the remaining offer across process death is a possible alternative but
needs a reviewed boot/clock discontinuity policy, not a wall-clock guess.
**Consequences/tests:** six-second boundary, cancellation/unknown commit, archive/
unarchive, conflict, process restart, zone/time travel. **Open:** approve archive and
the deliberately conservative process-death undo behavior.

## D08 — Privacy and portable files

**Proposed choice:** manual backup is versioned plaintext JSON, explicitly disclosed
before choosing its destination. Suggested filename
`sello-backup-v1-YYYYMMDD-HHmmss.json`; content version is independent of Room.
Portable payload includes categories (IDs/names/icons/archive state), explicit
monthly limits/defaults, dated expenses/income (IDs, amounts, currency, category/source,
notes, audit timestamps/order), financial zone and appearance/order preferences.
No credentials, paths/URIs, device permissions, debug state or pending undo/jobs.
Operation receipts/generation/version recovery machinery is local runtime metadata,
not portable replay permission; restore re-establishes fresh guarded runtime identities.
SELLO-028 owns exact wire keys, validation bounds and format documentation.

No automatic cloud/device-transfer backup in MVP. A picker-created external file
remains until its owner deletes it; reset cannot erase external copies. Remove
app-private temporary staging on success/failure/cancellation and startup recovery;
no retention of extra shared copies. No custom encryption or password promise.
**Reason/alternative:** explicit portable recovery without a misleading security claim.
**Consequences/tests:** plaintext disclosure, round trip, limits/unknown versions,
private-cache cleanup, release manifest inspection. **Open:** explicit privacy approval.

## D09 — Restore and reset

**Proposed choice:** restore replaces, never merges. Preview source counts and validate
the whole file before confirmation. Restore backed-up financial zone and portable
appearance/order preferences; never restore permissions, debug controls or URI grants.
Advance generation and invalidate old drafts/undo/commands. Commit financial data
atomically; settings failure stays visibly incomplete and retry resumes settings only.

Reset requires counts, two confirmations and typed BORRAR; delete finances, categories,
monthly limits and portable preferences, return to first-run Recibo. Defaults are
Sistema/Cobalto/Más usadas. The next setup establishes the device financial zone.
Retain only the minimal fresh generation and nonfinancial reset-completion identity
needed to reject stale operations/recover completion; no former amounts/names/notes.
Externally exported backups survive. Neither action changes system permissions.
**Reason/alternative:** reject accidental merge, incomplete-success stamps and stale
commands resurrecting deleted data. **Consequences/tests:** backup/reset recovery
cases in the example corpus and SELLO-028–032. **Open:** explicit destructive-policy approval.

## D10 — Release acceptance and review

**Proposed choice:** minimum-SDK and target-SDK emulator coverage once D02/SELLO-002
are accepted; compact, short landscape, medium and expanded windows; font scales
1.0/1.3/2.0; four themes; TalkBack, keyboard, Switch Access and reduced motion.
Validate production-signed release installation/recovery on at least one physical
device. Emulator assembly or debug signing is not that evidence.

Product owner approves product/privacy examples and release acceptance; a designated
independent technical reviewer approves financial/recovery correctness and quality
evidence. Agent self-review cannot supply that approval. SELLO-034 establishes measured
startup/large-history baselines and numerical performance targets before its acceptance;
no arbitrary latency guarantee in this contract. Missing reviewer/device/key/host
is an explicit prerequisite, never an optional skipped pass.
**Reason/alternative:** verify meaningful release boundaries without promising unmeasured
performance. **Consequences/tests:** SELLO-033–036 matrices/artifacts/approvals.
**Open:** approve matrix/process; name technical reviewer and release channel/device owner.

## Approval log and next discussion

| Date | Approver | Decision | Recorded answer and example implications |
| --- | --- | --- | --- |
| 2026-10-08 | Project owner (user) | D01 | Approved the proposed MVP scope and two-tab/developer-tools boundary. |
| 2026-10-08 | Project owner (user) | D03 model | Approved month-to-date pace including today, future days excluding today, and forecasts from day one; F01/F02 demonstrate the discussed behavior. |
| 2026-10-08 | Project owner (user) | D03 allowance | Approved today-inclusive allowance, changed downward rounding to multiples of COP 50; explicit examples 3,529 → 3,500 and 3,564 → 3,550. F/A expectations updated. |
| 2026-10-08 | Project owner (user) | D03 forecast/verdict | Approved nearest-peso forecast display and exact pre-rounding 95%/100% verdict comparisons. V05 distinguishes display from classification. |

Approval source: user's numbered answers to the scope, forecast and rounding
discussion, followed by an explicit instruction to make the changes and record approval.
This approves those choices, not every other proposal or a completed independent
review of every numerical case.

Next discussion batches:
1. D04 historical budgets; D05 fields; D06 ordering; D07 archive/undo.
2. D08 privacy; D09 destructive actions; D02 identities/support; D10 release owners.

Each approval entry must name decision/subchoice, chosen answer, approver, date and
example implications. Partial approval does not ratify unrelated proposals. SELLO-001
remains In Progress until all required inputs and independent review are complete.
