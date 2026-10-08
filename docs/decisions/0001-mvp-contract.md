# 0001 — MVP product and financial contract

**Ticket:** SELLO-001 · **Version:** 14 · **Updated:** 2026-10-08
**Status:** D01–D10 product policies/process approved; implementation, independent
example review and named technical/device/signing owners remain outstanding.
Signed carryover is deferred beyond MVP; detailed policy belongs to SELLO-037.
**Decision owner/approver:** project owner (user). **Executor:** coding agent.
**Approval/date:** project owner (user), 2026-10-08, for the logged D01–D10 choices;
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

**Approved choice (project owner, 2026-10-08):** customer `com.software.sello`; debug `com.software.sello.debug`;
catalog `com.software.sello.catalog`. Distinct launcher labels: Sello, Sello Debug,
Sello Catalog. Install all three side by side with isolated private data.
Retain minimum SDK 30 as the approved product floor, subject to SELLO-002's
compatibility verification. This approval does not establish toolchain compatibility.
MVP distribution is private APK only; Google Play/AAB publication is outside the
current release scope. Production signing and release verification remain mandatory;
distribution itself requires separate explicit authorization.

**Reason/alternative:** retain customer identity and prevent debug/customer storage
collisions. Reject shared IDs or developer controls in customer releases.
**Consequences/tests:** install coexistence and release dependency/artifact inspection
in SELLO-003/004/032/035. SDK 37 is an inspected compile/target setting, not a
ratified support guarantee or a claim about store compliance.
**Open:** SELLO-002 compatibility evidence; signing custody and actual device/release
owners remain D10/SELLO-035 prerequisites. Exact library versions remain outside this record.

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

**Approved distinction (project owner, 2026-10-08):** finite zero is a real zero budget;
unlimited is explicit absence
of a limit. Zero does not mean unlimited. Unlimited-category expenses still consume
the overall finite budget. A zero-only finite budget is Al día at zero spending and
Pasado for any positive spending; do not divide by zero for a ratio/progress graphic.

**Approved historical stability and renewal:** changing today's configured budget must not
rewrite prior months. Category creation/default-limit changes
snapshot the current month's limit and set the default for following months.
The owner confirmed future signed carryover of surplus **and** overspending, then
approved post-MVP deferral. **Approved MVP renewal:** each month renews its configured
budget without carrying surplus or deficit; this describes a plan, not a reset of
actual money. Retain the historical results. Optional scope/detailed carryover rules
remain **unresolved**; see [direction ADR](0002-signed-carryover-direction.md) and
[SELLO-E09](../planning/mvp/epics/E09-signed-carryover.md), SELLO-037–043.

**Approved skipped-month behavior (project owner, 2026-10-08):** an unopened month
uses the budget configuration effective in that month, not the configuration at
the time it is later viewed. October default 100,000 and December default 120,000
therefore resolve November to 100,000 even if the app stayed closed in November.
Keep effective-month history of default-limit changes, alongside explicit monthly
limits. Persist materialized monthly snapshots idempotently and atomically; do not
precreate future month rows or require the app to run at midnight. When an unopened
month is later read, derive its limit from the default that was effective **then**,
not today's default. An explicit override for that month takes precedence. For
months before any known configuration/category existence, show limits as unconfigured;
never extrapolate backward. Snapshot materialization is an implementation mechanism,
not a requirement to precreate every month; actual schema choices belong to SELLO-011/013.

**Approved historical corrections:** allow past-dated expenses and explicit
corrections to historical limits, with information that makes the functionality
discoverable and identifies the affected month. A past-month limit correction does
not change other months' configured limits/defaults. Correcting dated records
recomputes affected actuals/graphs. If rollover is selected, its effect on later
derived carry amounts needs a separate decision; do not silently promise isolation
of every future calculated result.

**Required user guidance:** announce historical correction capability in context
on first relevant use; keep an accessible explanation available from month selection
and the relevant editor after dismissal. No push notification or recurring blocking
onboarding is required. Always show the effective month/date and the scope of a limit
change before submission; after confirmed commit explain which period was updated.
Proposed Spanish copy (final wording follows UI review):
- Discovery: “Puedes corregir gastos y límites de meses anteriores. Elige el mes
  que quieres revisar.” Income forms must explain the equivalent income correction.
- Historical limit: “Estás cambiando el límite de septiembre de 2026. Los límites
  configurados de otros meses no cambian.”
- Confirmed completion: “Límite de septiembre actualizado. Sus totales se recalcularon.”
Guidance must not claim success before a receipt, rely on color only, or disappear
as the only way to discover the capability. SELLO-019/020/021 deliver applicable
expense, limit and income messaging; no implementation is claimed here.

**Reason/alternative:** preserve known history without inventing historical settings
or making monthly budgets depend on how often the user opens the app. The previous
unapproved draft treated all skipped months as unconfigured; this revised proposal
reconstructs only from retained effective-month evidence. Reject copying today's
default backward, rolling balances over implicitly or prohibiting legitimate corrections.
**Consequences/tests:** H01–H06, zero/unlimited, skipped months, restart/idempotency,
discovery and target-month/completion guidance with accessibility coverage.
**Open:** none for MVP financial meaning; archive interactions are approved in D07.
Final presentation copy review belongs to the implementing UI tickets and detailed
post-MVP carryover policy to SELLO-037. Implementation/migration choices remain
the storage tickets' responsibility; approval is not executed acceptance evidence.

### Carryover options — signed direction confirmed, post-MVP activation

Amounts below are planning capacity, not saved cash or account balance. Assume a
100,000 monthly base limit and an isolated category with no prior carry. These
one-step illustrations do not define a multi-month algorithm.

| Option | Previous spend 20,000 | Previous spend 120,000 | Trade-off |
| --- | --- | --- | --- |
| Fresh monthly budget | Next capacity 100,000 | Next capacity 100,000 | Simple monthly comparisons; unused capacity cannot accumulate for irregular purchases. |
| Surplus-only carryover | Next capacity 180,000 | Next capacity 100,000 | Supports saving category capacity; overspending is forgiven at renewal and capacity can grow without a cap. |
| Signed carryover | Next capacity 180,000 | Next capacity 80,000 | Surplus and overspending both persist; deficits can squeeze later months and complicate corrections. |

The owner's honesty requirement selects signed surplus/deficit as the future direction;
surplus-only behavior does not meet it. Fresh-budget mode is approved for MVP;
carryover activation/configuration requires SELLO-037 approval after MVP release.
Per-category opt-in could suit clothing/travel without changing groceries or rent;
global rollover is a different policy. This is **not** approved MVP scope expansion.
If chosen, settle positive-only versus signed carry, caps/negative-capacity behavior,
start month, archive/unlimited transitions, historical correction cascades and whether
budget verdicts compare base or effective capacity. Separate monthly planned allocation
from carried capacity so income is not allocated a second time. Extend the domain,
backup, sandbox and example contracts only after approval, not with dormant tables.

## D05 — Fields, input grammar and income labels

**Approved choice (project owner, 2026-10-08):** category names 1–24 Unicode code points,
trimmed, unique ignoring
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
large imported amounts, mandatory Otro name. **Open:** none for these product rules;
normalization/parser implementation and independent boundary review remain required.

## D06 — Ordering

**Approved ordering (project owner, 2026-10-08):** default Más usadas = count of nondeleted expenses in the selected
month, descending. Other design choices: Alfabético = normalized name ascending;
Monto gastado = selected-month spent descending; Límite = that month's finite limit
descending; Última actualización = real category/configuration audit Instant descending
(not latest expense date). Expose direction for each metric. Unlimited categories
always last regardless of direction; equal metrics tie by normalized name ascending,
then stable category ID. Unlimited-last applies even to alphabetical/reversed orders.
Approved D07 archive visibility/eligibility also applies: archived categories remain
visible in periods with a saved limit or expenses, but cannot accept new entries
until unarchived. Keep those historical rows under the same ordering contract.

**Reason/alternative:** selected-month ordering explains what the user is viewing;
reject undocumented lifetime counts and financial-time changes affecting audit order.
**Consequences/tests:** O01–O08 and cross-month/direction/tie/unlimited cases in
SELLO-023; archive interactions use approved D07.
**Open:** none for ordering/visibility rules; UI/test execution remains outstanding.

## D07 — Archive and undo

**Approved choice (project owner, 2026-10-08):** category removal archives only;
retain history and monthly limits.
Offer explicit unarchive; renaming updates displayed category name across history,
but no dates/amounts/limits. Preserve the current month's configured limit when
archiving; exclude the category from automatic budgets for following months while
archived. Keep it visible in historical/current periods with a saved limit or expenses,
marked archived, so totals remain explainable. Unarchive in the same month preserves
its existing monthly limit; in a later month use the last configured default unless
an explicit limit for that month exists. Announce these budget consequences before
archive/unarchive confirmation. No destructive aggregate category deletion in MVP.
A six-second record-delete undo uses durable guarded
compensation. Its live deadline is monotonic, independent of financial-time travel.
Rotation/recreation within the same live process never restarts the timer. Following
process death, recover commit status but expire the interactive undo offer rather
than guess remaining time across restart; no hidden automatic restoration.
Still recover the actual deletion/compensation result from durable receipts;
expiring the offer does not cancel or roll back an already committed undo.
Conflicting changes/generation invalidation reject compensation.

**Reason/alternative:** no destructive category cascades or extended undo from restart.
Restoring the remaining offer across process death is a possible alternative but
needs a reviewed boot/clock discontinuity policy, not a wall-clock guess.
**Consequences/tests:** six-second boundary, cancellation/unknown commit, archive/
unarchive, conflict, process restart, zone/time travel; AR01–AR06/U01–U05 examples.
**Open:** none for these lifecycle/timer rules; actual implementation and independent
boundary review remain outstanding. Final message wording is reviewed in UI delivery.

## D08 — Privacy and portable files

**Approved choice (project owner, 2026-10-08):** manual backup is versioned plaintext JSON,
explicitly disclosed
before choosing its destination. Suggested filename
`sello-backup-v1-YYYYMMDD-HHmmss.json`; content version is independent of Room.
Portable payload includes categories (IDs/names/icons/archive state), explicit
monthly limits/defaults, dated expenses/income (IDs, amounts, currency, category/source,
notes, audit timestamps/order), financial zone and appearance/order preferences.
No credentials, paths/URIs, device permissions, debug state or pending undo/jobs.
Operation receipts/generation/version recovery machinery is local runtime metadata,
not portable replay permission; restore re-establishes fresh guarded runtime identities.
SELLO-028 owns exact wire keys, validation bounds and format documentation.

No automatic cloud/device-transfer backup in MVP. Tell users that loss/uninstallation
can mean loss of private history without a retained manual backup. A user-selected
cloud-backed document location may sync through its provider; Sello does not perform
cloud synchronization itself or promise control over that provider's file lifecycle.
Sello does not automatically delete externally saved copies; reset cannot erase them. Remove
app-private temporary staging on success/failure/cancellation and startup recovery;
no retention of extra shared copies. No custom encryption or password promise.
**Reason/alternative:** explicit portable recovery without a misleading security claim.
**Consequences/tests:** plaintext disclosure, round trip, limits/unknown versions,
private-cache cleanup, release manifest inspection; P01–P04 policy examples.
**Open:** none for the approved privacy/file choices. Exact format keys, suggested
filename convention and final disclosure wording are reviewed in SELLO-028/029/032;
actual implementation/manifest checks remain required, not claimed by this approval.

## D09 — Restore and reset

**Approved choice (project owner, 2026-10-08):** restore replaces, never merges.
Validate the whole file, preview source/current counts and explicitly warn that
current data will be replaced before confirmation. Restore backed-up financial zone and portable
appearance/order preferences; never restore permissions, debug controls or URI grants.
Advance generation and invalidate old drafts/undo/commands. Commit financial data
atomically; settings failure stays visibly incomplete and retry resumes settings only.

Reset requires counts, two confirmations and typed BORRAR; delete finances, categories,
monthly limits and portable preferences, return to first-run Recibo. Defaults are
Sistema/Cobalto/Más usadas. The next setup establishes the device financial zone.
Retain only the minimal fresh generation and nonfinancial reset-completion identity
needed to reject stale operations/recover completion; no former amounts/names/notes.
Externally exported backups survive; explicitly warn that reset does not erase them.
Neither action changes system permissions.
**Reason/alternative:** reject accidental merge, incomplete-success stamps and stale
commands resurrecting deleted data. **Consequences/tests:** backup/reset recovery
cases R01–R06 in the example corpus and SELLO-028–032. **Open:** none for these
approved product choices; implementation, failure recovery and independent review
remain required, not established by approval.

## D10 — Release acceptance and review

**Approved choice (project owner, 2026-10-08):** minimum-SDK and target-SDK emulator
coverage using SELLO-002's verified matrix; compact, short landscape, medium and expanded windows; font scales
1.0/1.3/2.0; four themes; TalkBack, keyboard, Switch Access and reduced motion.
Validate production-signed release installation/recovery on at least one physical
device. Emulator assembly or debug signing is not that evidence.

Product owner approves product/privacy examples and release acceptance; a designated
independent technical reviewer approves financial/recovery correctness and quality
evidence. Agent self-review cannot supply that approval. SELLO-035 establishes measured
startup/large-history baselines and numerical performance targets before its acceptance;
no arbitrary latency guarantee in this contract. Missing reviewer/device/key/host
is an explicit prerequisite, never an optional skipped pass.
**Reason/alternative:** verify meaningful release boundaries without promising unmeasured
performance. **Consequences/tests:** SELLO-033–036 matrices/artifacts/approvals.
**Open:** name technical reviewer, physical-device testing owner and signing
custodian; approve concrete performance targets in SELLO-035 after baseline measurement.
Approval of this process assigns no unnamed owners or technical review result.
Private APK channel is approved in D02, not an unresolved choice.

| Responsibility | Owner/status | Required evidence |
| --- | --- | --- |
| Product/privacy and release go/no-go | Project owner (user) | Logged policy approval; final candidate walkthrough and explicit release acceptance still required |
| Independent financial/recovery technical review | Unassigned | Named reviewer, independently checked example corpus and later implementation/quality review |
| Physical-device release testing | Unassigned | Identified device/API, production-signed candidate and recorded installation/recovery journeys |
| Customer-distribution signing custody | Unassigned | Secure external key custody/access and certificate identity; no keys/passwords in source or reports |

Unassigned responsibilities remain explicit acceptance prerequisites, not waived
gates. Work with satisfied dependencies may proceed; required review/device/signing
evidence must exist before the corresponding ticket or release is declared complete.

## Approval log and next discussion

| Date | Approver | Decision | Recorded answer and example implications |
| --- | --- | --- | --- |
| 2026-10-08 | Project owner (user) | D01 | Approved the proposed MVP scope and two-tab/developer-tools boundary. |
| 2026-10-08 | Project owner (user) | D03 model | Approved month-to-date pace including today, future days excluding today, and forecasts from day one; F01/F02 demonstrate the discussed behavior. |
| 2026-10-08 | Project owner (user) | D03 allowance | Approved today-inclusive allowance, changed downward rounding to multiples of COP 50; explicit examples 3,529 → 3,500 and 3,564 → 3,550. F/A expectations updated. |
| 2026-10-08 | Project owner (user) | D03 forecast/verdict | Approved nearest-peso forecast display and exact pre-rounding 95%/100% verdict comparisons. V05 distinguishes display from classification. |
| 2026-10-08 | Project owner (user) | D04 zero/unlimited | Agreed on the difference between a zero budget and no limit; B05 illustrates the distinction. |
| 2026-10-08 | Project owner (user) | D04 historical stability | Agreed today's budget changes must not affect previous months. This does not approve the skipped-month implementation mechanism. |
| 2026-10-08 | Project owner (user) | D04 historical corrections/guidance | Approved allowing corrections, requiring messages/information to announce the capability and explain it; H02/H06 and SELLO-019/020/021 cover applicable behavior. |
| 2026-10-08 | Project owner (user) | D04 future signed carryover | Confirmed that both underspending and overspending should affect later availability when the future feature is enabled; optional/category scope and possible post-MVP delivery remain open. Preparation means retaining history and boundaries, not building dormant features. |
| 2026-10-08 | Project owner (user) | D04 MVP renewal/post-MVP plan | Approved deferring carryover beyond MVP, renewing the configured monthly budget without carryover, and adding a concrete follow-up epic/tasks. SELLO-E09/037–043 cannot delay or become dependencies of MVP acceptance. |
| 2026-10-08 | Project owner (user) | D04 skipped-month configuration | Approved using the configuration effective in the historical month even when the app was unopened. October default 100,000, December default 120,000 → November limit 100,000. H03/H04 are policy-aligned examples; independent arithmetic review remains pending. |
| 2026-10-08 | Project owner (user) | D05 fields and duplicates | Approved 24-character trimmed category names, 60-character optional notes and case/accent-insensitive name uniqueness including archived names. |
| 2026-10-08 | Project owner (user) | D05 amounts and original input | Approved the 12-digit positive COP keypad limit, plain/grouped-dot paste grammar and rejection rather than input repair; larger valid backup amounts remain exact/displayable and survive other-field edits. |
| 2026-10-08 | Project owner (user) | D05 income sources | Approved Salario/Freelance/Ingresos pasivos/Transferencia/Otro, required 24-character-bounded Otro source name and manually recorded inflow meaning rather than inferred tracked-account transfers. |
| 2026-10-08 | Project owner (user) | D06 ordering | Approved selected-month expense-count default, the four other metric meanings and reversible direction, unlimited categories always last and alphabetical/stable-ID ties. Archive lifecycle/visibility is still D07. |
| 2026-10-08 | Project owner (user) | D07 category lifecycle | Approved archive-only category removal, retained historical visibility/identity, historical rename labels and explicit restoration without erasing records. |
| 2026-10-08 | Project owner (user) | D07 archive budgets/guidance | Approved preserving the current-month limit, excluding automatic budgets while archived in following months and same/later-month restoration semantics, with consequences explained before confirmation. Closes D04/D06 archive choices. |
| 2026-10-08 | Project owner (user) | D07 record undo | Approved six real elapsed seconds for expense/income undo, no timer reset on rotation/backgrounding, process-death offer expiry with durable outcome recovery and no overwrite/resurrection after conflicts/reset. |
| 2026-10-08 | Project owner (user) | D08 manual plaintext backup | Approved versioned, unencrypted JSON with explicit readable-content disclosure before saving; no password/encryption promise in MVP. |
| 2026-10-08 | Project owner (user) | D08 platform/provider privacy | Approved no OS-managed cloud backup/device transfer, manual-recovery responsibility and disclosure that a selected cloud-backed provider may sync the file independently of Sello. |
| 2026-10-08 | Project owner (user) | D08 contents/retention | Approved financial history/zone/appearance/order recovery without permissions/secrets/debug/pending-undo state, temporary-file cleanup and external copies surviving app reset. |
| 2026-10-08 | Project owner (user) | D09 replacement | Approved whole-file validation, counted preview and explicit replacement warning; restore never merges and includes financial zone/portable preferences, not permissions/debug state. R01/R02. |
| 2026-10-08 | Project owner (user) | D09 recovery/fencing | Approved invalidating old drafts/undo, atomic financial replacement and visible settings-pending recovery that retries settings only, never a second financial replacement. R03/R04. |
| 2026-10-08 | Project owner (user) | D09 reset | Approved counted two-confirmation reset with typed BORRAR, first-run defaults, minimal nonfinancial recovery metadata and explicit warning that external backups survive. R05/R06. |
| 2026-10-08 | Project owner (user) | D02 identities/support | Approved customer/debug/catalog IDs and labels, side-by-side isolated installation, and SDK 30 product floor subject to SELLO-002 compatibility verification. Iden01/Iden02 are downstream acceptance expectations, not executed installation tests. |
| 2026-10-08 | Project owner (user) | D02 distribution | Approved private APK distribution only for now; no Google Play/AAB release work in MVP. Production signing/device verification still required; actual publishing is not authorized. |
| 2026-10-08 | Project owner (user) | D10 verification | Approved minimum/target SDK emulators, adaptive/font/theme/accessibility matrix and production-signed physical-device installation/recovery evidence; configuration and execution remain deliverables. |
| 2026-10-08 | Project owner (user) | D10 performance | Approved measuring representative histories and approving concrete targets before release acceptance; no numerical guarantee approved. SELLO-035 owns this work. |
| 2026-10-08 | Project owner (user) | D10 ownership/process | Approved product-owner go/no-go and independent technical review, with device/signing owners required. No names supplied for those roles; they remain unassigned prerequisites, not permission to self-approve or skip gates. |

Approval source: user's numbered answers to the scope, forecast and rounding
discussion, followed by an explicit instruction to make the changes and record approval;
then the user's D04 answers approving the distinction/stability/correction guidance
while explicitly requesting further discussion of carryover.
The subsequent carryover answer confirms signed carryover as an eventual capability,
without approving its implementation details or category scope. The next explicit
“yes” to MVP deferral/renewal approved that boundary and requested the follow-up plan.
The following explicit agreement approved skipped-month effective configuration.
The subsequent “yes to all of these” approved all three presented D05 recommendations.
The following “yes, sounds good” approved the three presented D06 ordering rules.
The next “yes, approved” approved all three presented D07 lifecycle/budget/undo recommendations.
The following “yes, continue” approved the three presented D08 privacy/file choices;
the accompanying commit request authorizes committing this accumulated work.
The subsequent “yes, approved” approves all three presented D09 replacement,
recovery/fencing and reset recommendations.
The following “approved. Just private apk distribution at this point” approves
the two D02 identity/support recommendations and selects private APK distribution.
The next “yes, approved” approves the presented D10 verification, performance and
ownership process; it does not answer the request for named review/device/key owners.
This approves those choices, not every other proposal or a completed independent
review of every numerical case.

Remaining execution inputs:
1. Assign the independent technical reviewer and obtain full-corpus example review.
2. Assign physical-device testing and signing custody before affected release gates.
3. Verify toolchain/support in SELLO-002 and establish measured targets in SELLO-035.

Each approval entry must name decision/subchoice, chosen answer, approver, date and
example implications. Partial approval does not ratify unrelated proposals. SELLO-001
remains In Progress until all required inputs and independent review are complete.
Detailed signed carryover choices move to SELLO-037; SELLO-001 need not decide an
unshipped post-MVP algorithm to become Done.
