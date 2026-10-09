# SELLO-E01 — Contracts and engineering foundation

- **Status:** In Progress
- **Goal:** Make decisions explicit and build/test the app reproducibly before financial work.
- **Exit:** Approved MVP conventions, enforced module graph, usable CI and injected clocks/composition; existing app remains launchable.

## SELLO-001 — Approve the MVP contract and financial examples

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** None
- **Gate:** G0

### Outcome
Remove ambiguity before implementing figures or recovery. Ratify the architecture's
MVP, two-tab exception and developer tools; do not expand scope to recurrence/goals.

### Deliverables
An approved scope/decision record and versioned worked examples for day inclusion,
variable pace, rounding, projection, 95/100% verdict boundaries, no-budget behavior,
historical limits and income allocation. Record privacy/plaintext backup policy,
supported API range and app/debug/catalog identities. Verify the existing local
Sello snapshot/provenance; do not fetch another worktree as a build prerequisite.

### Acceptance criteria
- Product owner explicitly approves the open decisions; unresolved policy cannot
  be replaced by a developer guess. Examples include day 1, last day, February,
  zero spending, unlimited categories, over-budget and insufficient-history states.
- Category rename/archive and current versus prior-month budget edits have defined
  outcomes. Forecast labels never imply saved actuals; savings is not account balance.
- Design snapshot has no build-time external-path dependency or unrelated legacy files.

### Tests
Review example arithmetic independently, validate Markdown links and the board,
and verify baseline G0 still passes. Documentation-only changes need no invented red test.

### Working checkpoint
Approved decisions are usable inputs to domain/UI tickets; generated app still
builds/launches. Missing approvals keep this ticket out of Done.

### Context and starting points
The design contains sample amounts and legacy migration hints, not approved new
financial policies. Without this task, separate actors will choose different pace,
budget history and reset semantics. Read [decision inputs](../DECISION_INPUTS.md),
[baseline](../BASELINE.md), architecture §§1/4/7/9 and the
[local design](../../../design/sello-spec.html#build). No predecessor implementation exists.

### Implementation plan
1. Create `docs/decisions/0001-mvp-contract.md` using D01–D10 as individual decisions;
   record owner, chosen answer, rationale, alternatives, approval/date and unresolved items.
2. Resolve each budget/forecast/order/field/reset question explicitly. Define formulas,
   included dates, rounding and labels; distinguish fixed rules from reviewed proposals.
3. Create `docs/testing/mvp-financial-examples.md` with M/B/V/H/I/G examples, at least
   three approved forecast calculations, February/leap-year and first/last-day cases.
4. Review examples independently with the product owner. If answers are unavailable,
   deliver the proposal and blocker; never invent “approved” finance/privacy semantics.
5. Verify design provenance; update architecture only for accepted guardrail changes.
   Leave implementation, toolchain changes and downstream ticket statuses untouched.

### Concrete cases and pitfalls
- Spending 50,000 across limited/unlimited categories must reconcile to B01, not
  discard unlimited expenses. Past limits cannot be read from today's category defaults.
- Decide whether forecast comparison uses exact or rounded projection at 95%/100%;
  the allowance's inclusive today is not automatically the forecast's day convention.
- UI keypad limits and Long import/display range need separate explicit answers.
  A theme preference reset does not imply permission/device state can be restored.

### Verification recipe
Run `python3 docs/planning/mvp/board.py --check` and G0 from
[gate profiles](../README.md#gate-profiles); check decision/example links and snapshot
hash. Evidence must include approver/date and independently checked inputs/outputs.
No behavior red test or device run is required for this documentation-only task.

### Execution progress
2026-10-08: drafted [D01–D10 proposals](../../../decisions/0001-mvp-contract.md)
and [versioned worked examples](../../../testing/mvp-financial-examples.md).
2026-10-08: project owner approved D01 scope and D03 forecast/day-inclusion/verdict
rules, with daily allowance rounded down to COP 50 multiples. Recorded per-decision
approval and updated example/architecture contracts; no other choices were approved.
Next: owner reviews D04–D07, followed by D08/D09/D02/D10. Independent full-corpus
review remains outstanding. Ticket stays In Progress; downstream prerequisites
are not satisfied by this partial approval.
2026-10-08 continuation: refined the still-unapproved D04 skipped-month proposal to
resolve known historical defaults from effective-month evidence, independent of app
opening, and explicitly exclude leftover/overspend rollover. Updated H03–H05;
request owner approval of zero/unlimited, monthly carry-forward and historical edits.
2026-10-08 follow-up: owner approved zero/unlimited distinction, prior-month stability
and historical corrections with discoverable user information. D04 is partially
approved; carryover is explicitly still under discussion. Added option comparison
and H06 guidance case; SELLO-019/020/021 must deliver applicable correction messaging.
2026-10-08 direction: owner confirmed eventual signed surplus/deficit carryover,
possibly optional and outside MVP. Added ADR 0002 and retention/ownership guardrails;
no carryover code/schema or new tickets. MVP activation/default and detailed policy
still need approval; next clarify deferral before moving to the remaining D04/D05 inputs.
2026-10-08 plan approval: owner approved MVP budget renewal without carryover and
post-MVP deferral. Added SELLO-E09/037–043 with release/policy/quality dependencies
and separate generated roadmap views; MVP remains eight epics/36 tickets. Detailed
carryover decisions are owned by SELLO-037, not blockers for SELLO-001.
2026-10-08 skipped-month approval: owner agreed that unopened months resolve budget
configuration effective then, not today's default. Recorded D04 approval, aligned
H03/H04 and added real-Room history/materialization acceptance to SELLO-013.
Next: D05 field/input/source rules, then D06/D07 (including archive budget interactions).
2026-10-08 D05 approval: owner approved all presented name/note/uniqueness, amount
input/range-preservation and income-source rules. Recorded approval, added T01–T06
policy examples and aligned SELLO-010 validation acceptance. Next: D06 ordering,
then D07 archive/undo and the remaining privacy/support/release decisions.
2026-10-08 D06 approval: owner approved all presented metric/direction/tie and
unlimited-last ordering rules. Recorded approval, added O01–O08 fixture expectations
and aligned SELLO-023. Archive visibility/budget interactions remain proposed D07;
prepared explicit current/future-month archive consequences for the next discussion.
2026-10-08 D07 approval: owner approved archive/restore/history labels, explained
current/future-month budget effects and six-second real-time undo with process-death
offer expiry/outcome recovery. Closes D04/D06 archive decisions; added AR01–AR06/
U01–U05 and aligned SELLO-013/019/020/021. Next: D08 privacy/portable backup policy.
2026-10-08 D08 approval: owner approved all presented manual plaintext/privacy/
contents/retention choices and requested committing accumulated work. Recorded D08,
added P01–P04 and aligned SELLO-029/032; implementation is still outstanding.
Next: D09 restore/reset, then D02 identities/support and D10 release/review ownership.
2026-10-08 D09 approval: owner approved all three replacement, settings-only recovery
and counted/BORRAR reset recommendations. Recorded approval and R01–R06 expectations;
aligned SELLO-030/031 acceptance. Implementation and independent review remain pending.
Next: D02 identities/support, then D10 release/review ownership.
2026-10-08 D02 approval: owner approved isolated customer/debug/catalog identities
and SDK 30 floor subject to SELLO-002 verification, selecting private APK distribution
only. Added Iden01/Iden02 and aligned toolchain/release guidance; no signing custody,
device owner, D10 process or independent review is implied. Next: D10.
2026-10-08 D10 approval: owner approved release matrix, measured performance target
process and independent review/ownership requirements. D01–D10 policy choices are
now approved. Named technical/device/signing owners were not supplied; full-corpus
independent review remains outstanding, so SELLO-001 stays In Progress. Aligned
the performance owner reference to SELLO-035, whose ticket owns measurements/targets.
2026-10-08 ownership/walkthrough: user will handle periodic physical-device testing
and release build/signing. Recorded ownership without claiming completed evidence;
independent technical reviewer remains unassigned. Started illustrative owner review
in four batches; B01/B02/B04/B05/B06/B07 response pending. No batch or full corpus
is marked reviewed merely because the user requested a walkthrough.
2026-10-08 batch 1 feedback: owner confirmed B01/B02/B05/B06/B07 and reiterated
the future funds-reflection/carryover intent. B04 allowance scope needs clarification:
one category's deficit is not necessarily an overall deficit. Explicitly scoped
B04 and added B08's 300,000 limit/110,000 spend/190,000 remaining/19,000 daily
allowance illustration (Oct 22; ten days including today). Owner response pending;
no new financial policy or independent technical review is inferred.
2026-10-08 batch 1 confirmation: owner explicitly confirmed category/global allowance
distinction and B08. Marked the first batch owner-reviewed, aligned architecture and
SELLO-014 acceptance; full-corpus independent technical review remains outstanding.
Presented F01/F02/F03/A01/A02 as the next small calendar/rounding walkthrough;
response pending, with February/zero-history/verdict-boundary examples still to follow.
2026-10-08 batch 2 partial confirmation: owner confirmed the first-day estimate,
mid-month allowance/forecast, last-day behavior and rounding examples. Recorded
F01 forecast, F02/F03/B03/A01/A02 review without extending it to unseen boundaries.
Now presenting remaining F/A/V cases: short/leap months, successful zero versus
no-budget, first-day allowance, sub-50/50/100/negative allowance and exact verdict
boundaries. Owner response pending; SELLO-001 remains In Progress.
2026-10-08 batch 2 completion: owner confirmed remaining F/A/V edge cases, including
short/leap calendars, zero/no-budget, small/negative allowance and V05's exact-value
verdict despite display rounding. Marked this batch owner-reviewed, not executed or
independently technically reviewed. Presenting H01–H06/I01–I02/G01–G02 next;
responses pending and SELLO-001 remains In Progress.
2026-10-08 batch 3 partial confirmation: owner answered items 1–4, confirming
I01/I02 and H01–H06 income/history/renewal examples. Item 5 (G01/G02 cumulative
graph correction and total reconciliation) was omitted; re-presented for explicit
response, not marked reviewed. No implementation or independent review is claimed.
2026-10-08 figure presentation approval: owner accepted keeping net recorded flow,
budget remaining and allocation with explicit labels and Resumen/Recibo hierarchy.
Recorded Ingresos menos gastos / Restante del presupuesto / Por asignar al presupuesto,
replacing ambiguous Sin destinar; aligned architecture, I/G cases and SELLO-022
acceptance. Allocation is not income minus spending; formulas unchanged. Full graph
example review and independent technical review remain pending; no UI implementation exists.
2026-10-08 graph confirmation: owner explicitly confirmed G01/G02 correction
effects after the allocation-label clarification. Batch 3 is owner-reviewed;
independent technical review remains separate. Presenting M01–M05 money input
cases next, with response pending. No validation/UI implementation exists yet.
2026-10-08 money input response: owner confirmed M01–M04 and rejected wider backup
transactions. Revised D05/M05 to the same 12-digit per-transaction range everywhere,
recorded ADR 0003, added M06 aggregate-scope case and aligned SELLO-010/030. Original
approval history is retained but superseded; no persisted data or financial code
exists to migrate. Other batch 4 cases and independent review remain outstanding.
2026-10-08 naming/source walkthrough: presented T01–T06 category trim/case/accent
duplicates including archived identities, name/note limits, required Otro name and
manual income-source meanings. Owner response pending; policies are previously
approved D05, not newly implemented validation or independent technical review.
2026-10-08 naming/source confirmation: owner explicitly confirmed all five items,
covering T01–T06. Recorded review without claiming implementation/technical review.
Presented AR01–AR06 archive/restore/history scenarios next; response pending.
O01–O08 ordering follows separately; SELLO-001 remains In Progress.
2026-10-08 archive confirmation: owner confirmed all five AR01–AR06 behaviors.
Marked those examples owner-reviewed, not executed or independently technically
reviewed. Presented O01–O08 selected-month metric/direction/tie/unlimited and
configuration-audit-time ordering next; owner response pending.
2026-10-08 sorting response: owner confirmed count/amount/limit/alphabetical and
direction/tie/unlimited cases but changed latest activity to last expense addition,
not modification. Updated D06/architecture/SELLO-023 and retired O06's configuration
expectation. Latest-addition selected-month/global scope, creation/effective time,
deletion/empty and restored-time proposals need clarification; no implementation
or blanket example approval is claimed.
2026-10-08 recency confirmation: owner approved all three selected-month/original
addition/surviving-and-empty recommendations and clearer label. Finalized D06,
ADR 0004, architecture/SELLO-023; replaced O06/O08 and added O09–O14 boundary
fixtures from approved policy, with independent fixture review still pending.
Presenting U01–U05 undo/restart/conflict examples next; response pending.
2026-10-08 undo confirmation: owner explicitly approved all U01–U05 scenarios.
Marked those owner-reviewed without claiming storage/device/technical verification.
Presenting P01–P04 backup privacy/contents/provider/recovery/retention scenarios
next; response pending. Restore/reset and installation review remains outstanding.
2026-10-08 backup confirmation: owner approved all four P01–P04 cases. Marked
those owner-reviewed, not platform/file tested. Presented R01–R06 restore/reset
and duplicate-submit/unknown-commit contracts next; response pending. Installation
examples and independent technical review remain outstanding.
2026-10-08 recovery confirmation: owner approved all six R01–R06 and duplicate/
unknown-commit scenarios, asking to replace vague ajustes pendientes. Recorded
named failed-preference/committed-history/targeted-retry copy requirements in D09,
R03 and SELLO-030; literal Spanish proposal awaits response/UI review. Installation
review and independent technical review remain outstanding; no recovery code exists.
2026-10-08 restore-copy approval: owner accepted concrete Spanish wording with
named failed preferences and targeted retry/no-second-replacement reassurance.
Recorded D09 baseline approval and aligned SELLO-030; actual rendering/retry tests
remain future implementation. Presenting Iden01/Iden02 installation/isolation/
customer-artifact/private-APK scenarios next; response pending, not device evidence.
2026-10-08 installation confirmation: owner approved all four Iden01/Iden02 items.
Owner walkthrough is complete, with all D01–D10 choices and requested revisions
recorded. No further owner question remains in the presented batches. Independent
technical/fixture review is still outstanding; SELLO-001 stays In Progress rather
than claiming that owner approval executed feature/device gates. SELLO-002 remains
independently Ready and can verify the actual toolchain while review is arranged.
Walkthrough-completion verification: base `d8d58a9` plus accumulated uncommitted
architecture/decision/input/example, ADR 0004, foundation/workflow/recovery and
generated MVP CSV edits. 32 board tests, generated-board and diff checks passed;
G0 passed (97 tasks: 2 executed, 95 up-to-date; cached sample JVM test and existing
lint warnings), log `/tmp/sello-owner-walkthrough-complete-g0.log`. Docs-only; no
behavioral red/device test applicable. Independent technical/fixture review remains
required; no feature, device, hosted CI or completed-ticket evidence is claimed.
Restore-copy verification: base `d8d58a9` plus accumulated uncommitted architecture,
decision/input/example, ADR 0004, foundation/workflow/recovery and generated MVP
CSV edits. 32 board tests, generated-board and diff checks passed; G0 passed (97
tasks: 2 executed, 95 up-to-date; cached sample JVM test and existing lint warnings),
log `/tmp/sello-installation-walkthrough-g0.log`. Docs-only; no behavioral red/device
test applicable. Installation response and independent review remain pending.
Recovery-wording verification: base `d8d58a9` plus accumulated uncommitted
architecture/decision/input/example, ADR 0004, foundation/workflow/recovery and
generated MVP CSV edits. 32 board tests, generated-board and diff checks passed;
G0 passed (97 tasks: 2 executed, 95 up-to-date; cached sample JVM test and existing
lint warnings), log `/tmp/sello-restore-wording-g0.log`. Docs-only; no behavioral
red/device test applicable. Literal copy response/installation/technical review pending.
Backup-confirmation verification: base `d8d58a9` plus accumulated uncommitted
architecture/decision/input/example, ADR 0004, foundation/workflow and generated
MVP CSV edits. 32 board tests, generated-board and diff checks passed; G0 passed
(97 tasks: 2 executed, 95 up-to-date; cached sample JVM test and existing lint
warnings), log `/tmp/sello-backup-confirmation-g0.log`. Docs-only; no behavioral
red/device test applicable. Recovery response and independent review remain pending.
Undo-confirmation verification: base `d8d58a9` plus accumulated uncommitted
architecture/decision/input/example, ADR 0004, foundation/workflow and generated
MVP CSV edits. 32 board tests, generated-board and diff checks passed; G0 passed
(97 tasks: 2 executed, 95 up-to-date; cached sample JVM test and existing lint
warnings), log `/tmp/sello-undo-confirmation-g0.log`. Docs-only; no behavioral
red/device test applicable. Backup response and independent review remain pending.
Recency-confirmation verification: base `d8d58a9` plus uncommitted architecture,
decision/input/example, ADR 0004, foundation/workflow and generated MVP CSV edits.
Executor fixture checks passed for recency/reverse, delete fallback and time ties;
not production sorting tests or independent review. 32 board tests, generated-board
and diff checks passed; G0 passed (97 tasks: 2 executed, 95 up-to-date; cached
sample JVM test and existing lint warnings), log `/tmp/sello-recency-confirmation-g0.log`.
Docs-only; no behavioral red/device test applicable. Undo response/review pending.
Latest-addition direction verification: base `d8d58a9` plus uncommitted architecture,
decision/input/example, ADR 0004, foundation/workflow and generated MVP CSV edits.
32 board tests, generated-board and diff checks passed; G0 passed (97 tasks: 2
executed, 95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-latest-addition-direction-g0.log`. Docs-only; no behavioral red/device
test applicable. Comparator details and independent review remain pending.
Archive-confirmation verification: base `d8d58a9` plus uncommitted decision/example,
foundation progress and generated MVP CSV edits. 32 board tests, generated-board
and diff checks passed; G0 passed (97 tasks: 2 executed, 95 up-to-date; cached
sample JVM test and existing lint warnings), log `/tmp/sello-ordering-walkthrough-g0.log`.
Docs-only; no behavioral red/device test applicable. Sorting response and independent
technical review remain pending; no implementation or completed ticket is claimed.
Naming-confirmation verification: base `d8d58a9` plus uncommitted decision/example,
foundation progress and generated MVP CSV edits. 32 board tests, generated-board
and diff checks passed; G0 passed (97 tasks: 2 executed, 95 up-to-date; cached
sample JVM test and existing lint warnings), log `/tmp/sello-archive-walkthrough-g0.log`.
Docs-only; no behavioral red/device test applicable. Archive response and independent
technical review remain pending; no implementation or completed ticket is claimed.
Naming walkthrough verification: base `d8d58a9` plus uncommitted example/progress
and generated MVP CSV changes. 32 board tests, generated-board and diff checks
passed; G0 passed (97 tasks: 4 executed, 93 up-to-date; cached sample JVM test and
existing lint warnings), log `/tmp/sello-naming-walkthrough-g0.log`. Docs-only;
no behavioral red/device test applicable; owner response and technical review pending.
Transaction-range revision verification: base `cae26a3` plus accumulated architecture,
decision/input/example, ADR 0003, foundation/core/workflow/recovery acceptance and
generated MVP CSV edits. 32 board tests, generated-board checks, diff checks and
G0 passed (97 tasks: 2 executed, 95 up-to-date; cached sample JVM test and existing
lint warnings), log `/tmp/sello-transaction-range-g0.log`. Docs-only policy revision;
no behavioral red/device test or actual boundary implementation is claimed.
Graph-confirmation verification: base `cae26a3` plus accumulated architecture,
decision/input/example, foundation/core/workflow and generated MVP CSV edits.
32 board tests, generated-board checks, diff checks and G0 passed (97 tasks: 2
executed, 95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-graph-confirmation-g0.log`. Docs-only; no behavioral red/device test
applicable. Input walkthrough response and independent technical review remain pending.
Figure-label verification: base `cae26a3` plus accumulated architecture/decision/input,
example review, foundation/core/workflow acceptance and generated MVP CSV edits.
32 board tests, generated-board and diff checks passed; G0 passed (97 tasks: 2
executed, 95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-figure-labels-g0.log`. Docs-only label/direction approval, unchanged
arithmetic; no behavioral red/device test applicable or independent review claimed.
Batch 3 partial-approval verification: base `cae26a3` plus accumulated architecture,
decision/input/example, foundation/core and generated MVP CSV changes. 32 board
tests, generated-board checks, diff checks and G0 passed (97 tasks: 2 executed,
95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-history-partial-approval-g0.log`. Docs-only; no behavioral red/device
test applicable. G01/G02 response and independent technical review remain pending.
History/income continuation verification: base `cae26a3` plus accumulated ownership,
architecture/decision/input/example review, foundation/core acceptance and generated
MVP CSV changes. Independent integer checks passed for H01/H02/H04, I01/I02 and
G01/G02; these are executor arithmetic checks, not app tests or independent review.
32 board tests, generated-board and diff checks passed. G0 passed (97 tasks: 2
executed, 95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-history-income-g0.log`. Docs-only; no behavioral red/device test applicable.
Batch 2 continuation verification: base `cae26a3` plus accumulated architecture,
decision/input/example review, foundation/core acceptance and generated CSV edits.
Independent Fraction/integer scratch checks passed for F01/F04/F05/F06, A03–A08
and V05; these are arithmetic checks, not feature tests or independent review.
32 board tests, generated-board and diff checks passed. G0 passed (97 tasks: 2
executed, 95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-forecast-edges-g0.log`. Docs-only; no behavioral red/device test applicable.
Batch 1 confirmation verification: base `cae26a3` plus accumulated ownership/review
decision/input/example, architecture, foundation/core acceptance and generated MVP
CSV edits. Independently checked F02: 60,000/17 → 3,500 downward COP 50 allowance;
40,000 × 31/15 → 82,667 displayed forecast. F01/F03 and A01/A02 match the corpus.
32 board tests, generated-board checks, diff checks and G0 passed (97 tasks: 2
executed, 95 up-to-date; cached sample JVM test and existing lint warnings), log
`/tmp/sello-batch1-confirmation-g0.log`. Docs-only; no behavioral red or device test
applicable. New example approval and independent technical review are not claimed.
Batch 1 clarification verification: base `cae26a3` plus accumulated uncommitted
ownership/review decision/input/example, foundation and generated MVP CSV changes.
B08 independently checked: 3 × 100,000 − 110,000 = 190,000; Oct 22–31 = 10
available days; 190,000 / 10 = 19,000 (already a COP 50 multiple). 32 board tests,
generated-board checks, diff checks and G0 passed (97 tasks: 2 executed, 95 cached;
existing lint warnings); log `/tmp/sello-budget-scope-g0.log`. No device/behavioral
tests applicable to documentation-only clarification; no independent review claimed.
Ownership/walkthrough verification: base `cae26a3` plus uncommitted decision/input,
example review log, foundation progress and generated MVP CSV changes. 32 board
tests, generated-board checks and diff whitespace checks passed; G0 passed (97
tasks: 4 executed, 93 up-to-date; cached sample JVM test and 17 existing lint
warnings), log `/tmp/sello-owner-walkthrough-g0.log`. Docs-only; no behavioral red
or device run applicable. Owner response and independent review remain pending.
D10 verification: base `2c8e9ef` plus accumulated uncommitted D09/D02/D10 decision,
architecture/input/example, epic/shared release guidance and generated CSV changes.
32 board tests, generated MVP/roadmap consistency checks and `git diff --check`
passed; G0 passed (97 tasks: 2 executed, 95 up-to-date; cached sample JVM test and
17 existing lint warnings), log `/tmp/sello-d10-g0.log`. Documentation-only approval
does not require invented red testing or device execution. No independent review,
toolchain support verification or production-signing/device evidence is claimed.
D02 verification: base `2c8e9ef` plus accumulated uncommitted D09/D02 architecture,
decision/input/example, foundation/recovery/release, board README and generated CSV
changes. 32 board tests, both generated-board consistency checks and diff whitespace
checks passed. G0 passed (97 tasks: 2 executed, 95 up-to-date; cached sample JVM
test and 17 existing lint warnings); log `/tmp/sello-d02-g0.log`. No behavioral code
changed, so red testing/device execution is inapplicable to this approval update.
SDK compatibility, side-by-side installation and production signing remain unverified.
D09 verification: base `2c8e9ef` plus uncommitted architecture, decision/input,
foundation/recovery acceptance, example corpus and regenerated MVP CSV changes.
32 board tests, both generated-board consistency checks and `git diff --check`
passed. G0 passed (97 tasks: 4 executed, 93 up-to-date; one cached sample JVM test,
17 existing lint warnings); log `/tmp/sello-d09-g0.log`. Documentation-only approval
needs no behavioral red test or device run. Owner policy approval is not independent
technical review; SELLO-001 remains In Progress. No financial implementation shipped.
Executor checks: 32 board unit tests passed, including meaningful red→green scope
checks (five intended assertion failures before enforcement). Both board/export
sets passed consistency checks; 325 local
documentation file/anchor links, design snapshot SHA-256 and exact arithmetic checks
passed, including 15 COP 50 allowance cases and pre-rounding verdict precision.
G0 (`./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease --continue`)
passed with cached JVM test/lint results (one sample JVM test, 17 existing lint
warnings). Latest continuation tested base: `491eb26` plus uncommitted revised
D04 decision/example documents and carryover-direction ADR, architecture/root README/
decision-input/workflow changes, foundation/financial-core/correction-messaging/
recovery-privacy edits,
new post-MVP epic, board generator/tests and generated MVP/roadmap export views.
Carryover/archive arithmetic, field/money-range/deadline boundaries and O01–O08 ordering
illustrations were executor-checked, not executed feature tests or independent review. No device run
required for this documentation task;
D01–D10 product approvals and future signed-carryover direction are recorded
above; no independent review is claimed.

2026-10-08 closure: product owner explicitly authorized independent review agent
Singer (`01a11de6-b349-7b51-9d2d-300c3f9b2b39`). Its complete version-32
technical/example review passed all 88 case IDs, C01/C02 and unnumbered
illustrations, including independently recalculated money and 17 ordering
comparisons. No blocking findings or policy changes were needed. Recorded
review-status/authority clarifications without changing formulas or fixtures;
earlier dated statements remain historical. SELLO-001 is Done. This does not
complete financial implementation, toolchain compatibility or release acceptance.

### Delivery evidence
- **Revision:** `1b0894b23697a356c8aa0b2ccefbc7e6e80bc4b3` plus uncommitted review-status/closure changes in `docs/decisions/0001-mvp-contract.md`, `docs/testing/mvp-financial-examples.md`, `docs/planning/mvp/DECISION_INPUTS.md`, `docs/planning/mvp/epics/E01-foundation.md`, regenerated `docs/planning/mvp/BOARD.md`/`ROADMAP.md`/`jira-import.csv` and new `docs/testing/reviews/SELLO-001-independent-review.md`; existing SELLO-002 progress in the epic is preserved. This is not a tested clean commit.
- **Requirement mapping:** owner approval → ADR 0001 D01–D10 and dated walkthrough; arithmetic/boundaries/history/lifecycle → version-32 example corpus and independent full-corpus review; local design/provenance → recorded snapshot hash, local file/anchor checks and no external-worktree build dependency; scaffold continuity → passing G0. Actual SDK compatibility and implementation/device/signing evidence belong to later tickets.
- **Red / Green:** documentation-only approvals/review/status work; no financial behavior changed and no invented behavioral red test. Existing board tests and G0 were run successfully.
- **Gate results:** `python3 -m unittest discover -s docs/planning/mvp -p test_board.py` passed 32 tests; generator and `--check` validated 9 epics/43 tickets; local documentation file/anchor checks and design SHA-256 passed; `git diff --check` passed. G0 `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease --continue` passed, 97 tasks (2 executed/95 up-to-date), cached one sample JVM test and 17 existing lint warnings; local log `/tmp/sello-001-independent-review-g0.log`. This bootstrap ticket is exempt from SELLO-004's not-yet-implemented runner; no hosted CI evidence is claimed.
- **Device / Artifact:** device run is explicitly inapplicable to SELLO-001's documentation-only verification recipe. Debug/release assembly succeeded; no device test, financial implementation, production signing or distribution acceptance is implied. Design snapshot SHA-256 `77ad0040e382c892c03ac05d1748d11e897b5e2b26e593ece5fcc697443ca097` matches provenance.
- **Review:** project owner approved D01–D10 and all walkthrough batches on 2026-10-08; separately authorized agent Singer (`01a11de6-b349-7b51-9d2d-300c3f9b2b39`) returned independent technical/example Pass. [Durable review](../../../testing/reviews/SELLO-001-independent-review.md) identifies reviewed hashes, full case coverage, calculations and nonblocking wording observations. Agent review is not human technical review; no blanket future reviewer assignment or release approval is claimed.

## SELLO-002 — Establish and verify the supported Android toolchain

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** None
- **Gate:** G0

### Outcome
Replace accidental template compatibility with a reproducible supported matrix,
without unnecessary upgrades or changing product behavior.

### Deliverables
Validated AGP/Gradle/Kotlin/Compose/KSP/JDK/SDK matrix, pinned catalog/wrapper and
setup instructions. Explain Gradle daemon toolchain versus Java bytecode target;
audit the existing JDK-25 daemon configuration and Java-11 target rather than assuming
JAVA_HOME alone controls the daemon. Establish debug versus production signing paths.

### Acceptance criteria
- A clean checkout builds using documented installed/downloaded prerequisites;
  wrapper checksum and AGP built-in Kotlin integration are valid.
- Dependency versions are pinned, source/build artifacts contain no credentials,
  and ignored machine-local configuration stays untracked.
- Approved D02: verify SDK 30 floor and customer/debug/catalog identities against
  the selected toolchain; record actual compatibility rather than inferring it from approval.
- Release compilation succeeds without distribution-signing secrets. Production signing remains
  an explicit later release gate; no keys/passwords enter source control.

### Tests
Run G0 on a clean build/cache-independent verification where practical; compile
the instrumented APK. Exercise missing-SDK/JDK/signing setup instructions and
record actual daemon/JDK versions plus dependency/lint warnings.

### Working checkpoint
App's current greeting works on an emulator/device; if no device exists, record
assembly separately and arrange smoke evidence before concluding launch acceptance.

### Context and starting points
The scaffold currently builds, but its mixed versions, JDK daemon criteria and SDK
targets have not been declared supported. Read [baseline](../BASELINE.md),
[tool commands](../EXECUTION_GUIDE.md#tools-and-host-verification), the catalog,
wrapper, `gradle.properties`, daemon criteria and `app/build.gradle.kts`.

### Implementation plan
1. Record actual `./gradlew --version`, installed SDK/platform/build tools and resolved
   catalog versions; distinguish launcher JVM, daemon JDK and Java/Kotlin bytecode targets.
2. Consult the guide's official AGP/Gradle/Kotlin sources and select the smallest
   compatible stable matrix; document why existing pins stay or change, including KSP.
3. Create `docs/decisions/0005-toolchain.md` and `docs/development/setup.md` with exact
   prerequisites/install locations, untracked SDK configuration and commands for a new actor.
4. Update only necessary pins/targets/wrapper checksum. Do not independently raise
   minSdk or change product identity before SELLO-001 approval.
5. Test without relying on cached outputs; document signing setup without credentials.
   Prepare/install the scaffold on the agreed emulator/device and record launcher smoke.

### Concrete cases and pitfalls
- `JAVA_HOME` pointing at Android Studio does not override daemon criteria by itself.
- A supported matrix must include the Compose plugin/BOM and selected KSP processor
  compatibility, not just successful dependency resolution with an old cache.
- A missing device is an unmet launch check. Unsigned release assembly is not a
  production-signed private APK; do not create a distribution key just to silence a build failure.

### Verification recipe
Run `./gradlew --version`, `./gradlew clean`, G0 and
`./gradlew :app:assembleDebugAndroidTest`; inspect dependency/lint reports. Follow
[device setup](../EXECUTION_GUIDE.md#device-and-process-restoration) for launch smoke.
Record the successful matrix, warnings and any missing prerequisites.

### Execution progress
2026-10-08: started after committing the owner walkthrough as `1b0894b`.
SELLO-001 remains In Progress: its policy approvals are complete, but the named
independent technical reviewer and full-corpus review are still outstanding.
SELLO-002 has no predecessor dependency and can proceed independently.

Inspected the actual single-module scaffold, catalog, wrapper, daemon criteria,
installed SDKs and Git exclusions. No build pins or product behavior changed.
`./gradlew --version` reports Gradle 9.5.0, launcher JDK 25.0.2 and daemon criteria
for Java 25. `:app:buildEnvironment` identifies the actual daemon as JetBrains
25.0.2 at `/opt/android-studio/jbr`. Gradle's reported embedded Kotlin 2.3.20 is
not proof of the app compiler version; the catalog's Compose plugin pin remains
2.2.10 and built-in Kotlin compiler compatibility still needs resolution.
`javap -verbose` on the freshly compiled MainActivity reports class major version
55 (Java 11), confirming that daemon JDK and output bytecode are different concerns.

Installed platforms: 32, 33, 35, 36, 36.1 and 37.0; build tools: 35.0.0, 36.0.0
and 36.1.0. The merged debug manifest declares minimum API 30, target API 37 and
package `com.software.sello`; the approved debug/catalog identities remain future
SELLO-003 deliverables, not installed variants. No API-30 runtime check occurred.
`adb devices -l` returned no attached devices. Release output is explicitly
`app-release-unsigned.apk`; no signing material or machine-local properties are
tracked. Owner retains production signing responsibility.

Uncached-output verification on clean commit `1b0894b`:
`./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease :app:assembleDebugAndroidTest --no-build-cache --no-configuration-cache --rerun-tasks --continue`
passed: 128 tasks executed, one sample JVM test with zero failures/errors/skips,
17 existing lint warnings. Log: `/tmp/sello-002-clean-g0.log` (local temporary
evidence, not a committed or durable CI artifact). This bypassed task/build and
configuration caches, not downloaded dependency caches; it does not yet prove
fresh dependency resolution or clean-checkout setup. Instrumented APK assembly
passed; no instrumented test or launch smoke ran.

Next: verify the official version-specific compatibility matrix, actual app
compiler/Compose integration and wrapper checksum; settle KSP compatibility for
bootstrap; write the toolchain ADR and reproducible setup/error instructions;
obtain launcher smoke evidence before completion. Use ADR 0005 because ADR 0002
already owns signed carryover; do not overwrite or duplicate its decision number.
No independent review, release signing or SELLO-002 completion is claimed.

2026-10-08 completion: recorded [ADR 0005](../../../decisions/0005-toolchain.md),
[reproducible setup](../../../development/setup.md) and
[verification evidence](../../../testing/SELLO-002-toolchain-verification.md).
Retained AGP 9.3.3/Gradle 9.5/JDK 25/SDK 37/30/BOM 2026.02.01. Aligned Kotlin
and Compose compiler to 2.4.20 because the old JVM plugin's published fully
supported range excludes Gradle 9.5; documented the tested AGP 9.3.3 patch versus
Kotlin's published 9.3.1 bound. Added catalog JVM/KSP aliases, KSP 2.3.12 pin
and root JVM declaration before AGP. No runtime feature, production module,
Room schema/dependency, signing secret or product identity changed.
The old KSP candidate failed with built-in Kotlin source-set validation;
the selected KSP2/Room/JVM combination generated/exported/compiled successfully.
Final clean builds and API-30 greeting/context-test execution passed. SELLO-002
is Done; earlier progress entries describe superseded inspected configurations.

### Delivery evidence
- **Revision:** `b76214d` plus tested uncommitted `build.gradle.kts`, `gradle/libs.versions.toml`, `docs/planning/mvp/EXECUTION_GUIDE.md`, `docs/planning/mvp/epics/E01-foundation.md`, new `docs/decisions/0005-toolchain.md`, `docs/development/setup.md`, `docs/testing/SELLO-002-toolchain-verification.md` and regenerated `docs/planning/mvp/BOARD.md`/`jira-import.csv`. Final cold-source export includes the working-tree build changes; build-input hashes match the evidence record. Not a tested clean commit.
- **Requirement mapping:** reproducible matrix/pins/daemon-versus-output → ADR 0005, catalog, root plugin declaration and compiler/class inspection; clean-checkout/environment handling → setup and empty-cache source exports/negative SDK/JDK checks; SDK-30 floor/current identity → merged manifest, lint and isolated API-30 greeting/context test; KSP capability → temporary real Room processor/schema/export/compile/lint probe; secret-free release assembly → unsigned release output, ignored local.properties/signing files and owner signing boundary. Debug/catalog implementation and coexistence remain SELLO-003, not substituted with synthetic production modules.
- **Red / Green:** no product behavior change or invented financial red test. Actual negative integration: old KSP rejected forbidden sourceSets, selected KSP2 generated and compiled; child-only JVM plugin request failed, root declaration passed. Invalid JDK/SDK environments exited nonzero with documented messages. Old Kotlin JVM compilation succeeded but its vendor support range did not include Gradle 9.5, motivating the coordinated Kotlin/Compose compatibility change rather than claiming a behavioral red failure.
- **Gate results:** final `./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease :app:assembleDebugAndroidTest --no-build-cache --no-configuration-cache --rerun-tasks --continue` passed locally (128 executed tasks) and in an initially empty-cache isolated source export (128 tasks, 127 executed/1 up-to-date). One sample JVM test passed, zero failures/errors/skips; lint passed with 18 warnings, including one additional JVM-plugin newer-version advisory, no suppressed checks. Selected Room/KSP/JVM probe passed 30 executed tasks with 21 temporary-fixture lint warnings. Board unit tests, generation/consistency, local links and diff checks passed on final documents. Logs `/tmp/sello-002-final-g0.log`, `/tmp/sello-002-final-fresh-checkout.log`, `/tmp/sello-002-kotlin-2.4.20-probe.log`; bootstrap runner exemption applies, no hosted CI claim.
- **Device / Artifact:** newly isolated `sello002_api30`, `emulator-5580`, API 30, Google APIs x86_64 revision 16, 320×640/density 160; selected-pin debug APK install/launch returned Status ok and UIAutomator contained Hello Android!. `:app:connectedDebugAndroidTest --no-configuration-cache` executed one context test, zero failures/errors/skips, 68 tasks (1 executed/67 up-to-date). Emulator shut down after verification. Final release output is unsigned; no physical-device/production-signing, target-API runtime, financial journey or debug/catalog isolation acceptance is claimed.
- **Review:** executor technical self-review cross-checked exact diff, official version-specific sources, compiler/plugin/daemon versions, matching source-export build hashes, probe artifacts, JVM/device XML and lint reports. SELLO-002 does not assign an independent financial/release reviewer; no independent human/agent approval or release acceptance is claimed. No product-policy question was introduced; identities/floor and signing ownership follow approved D02/D10.

## SELLO-003 — Bootstrap enforced modules and variant identities

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-001, SELLO-002
- **Gate:** G1

### Outcome
Create the architecture's compilation boundaries without introducing unfinished features.

### Deliverables
`:domain` Kotlin/JVM, `:data` and `:design-system` Android libraries, existing
`:app`, and developer `:catalog` application. Configure test source sets, Room schema
export location/KSP capability, catalog launcher, separate debug/catalog application
IDs and the allowed dependency graph. Move template theme ownership without duplication.

### Acceptance criteria
- App and minimal catalog independently install/launch; customer app never depends
  on catalog. Domain has no Android/UI/storage/DI dependency.
- Data import access is reserved for app composition; no reverse module edge or
  screen-to-screen implementation import is introduced.
- Existing instrumentation package assertions are variant-aware; sample tests do
  not produce a false failure after adding the debug ID suffix.

### Tests
Run G1 and compile affected test APKs. Inspect Gradle dependency graphs and package
IDs. Intentionally attempt a prohibited domain Android import/module edge to prove
the compilation boundary rejects it, then remove the negative fixture.

### Working checkpoint
Both launchers show honest minimal UI; no new product destination or fake save exists.

### Context and starting points
Today all code is in `:app`, so source packages do not enforce ownership. Inputs
are accepted identity/toolchain records from SELLO-001/002 and the
[target map](../EXECUTION_GUIDE.md#module-and-file-map). Read architecture §2 and
official build-variant guidance in the source library before altering Gradle.

### Implementation plan
1. Add the four target siblings to `settings.gradle.kts`; configure JVM domain and
   Android library/application plugins using the accepted matrix/catalog, not copy-pasted versions.
   Declare the catalog's versioned JVM plugin at root with `apply false` before
   AGP/Compose and apply it in the child; SELLO-002's probe exposed a child-only
   version request's unknown-classpath-version failure. Use pinned KSP2 without
   suppressing built-in Kotlin source-set validation; see ADR 0005.
2. Create build/test/resource/manifest skeletons for each module; set namespaces,
   SDK/bytecode/test-runner settings and allowed dependency edges explicitly.
3. Give app debug and catalog independent accepted IDs; move reusable theme ownership
   to design-system and keep MainActivity functional. Catalog gets its own real launcher.
4. Add approved Room/KSP/schema export configuration to data, but no speculative
   database tables. Compile test source sets and correct variant-specific package assertions.
5. Document module task/dependency inventory in setup instructions; verify no app→catalog,
   design-system→domain or reverse data edge. Do not add feature navigation yet.

### Concrete cases and pitfalls
- Installing debug app and catalog must not replace production or one another.
- Domain's Android imports must fail; app's unavoidable data Gradle edge is composition-only
  in source, which SELLO-004 enforces separately. Gradle alone cannot enforce that package rule.
- Empty modules still need usable documented test/build tasks; no-matching-tests
  cannot be described as executed business tests.

### Verification recipe
Run `./gradlew projects`, module `dependencies` reports and G1. Compile app/catalog
test APKs and install both launchers. Demonstrate a temporary forbidden domain import
fails compilation, remove it, rerun green, and attach the dependency/package-ID evidence.

### Execution progress
2026-10-08: started from clean `475d3f8` after the owner chose SELLO-003 and the
split "agent implements, owner reviews". G0 passed on the untouched scaffold first.

Added `:domain` (Kotlin/JVM), `:data` and `:design-system` (Android libraries) and
`:catalog` (Android application) to settings; root declares the Android-library and
KSP aliases `apply false` after the JVM plugin. Moved the template theme files from
`app/.../ui/theme` to `design-system/.../designsystem/theme` (package renamed, content
unchanged); app and catalog both consume that one `SelloTheme`. App debug gained
`.debug` and the "Sello Debug" label; catalog has its own ID, label, launcher icon,
activity and disabled backup/transfer. The package assertion moved to
`app/src/androidTestDebug` with the debug ID and label; catalog has an equivalent test.

Catalog additions, all pinned to already verified or already resolved versions: Room
2.8.4 runtime/compiler (the SELLO-002 probe candidate) in `:data` only, and
`androidx.test:runner` 1.5.2, the version `:app` already resolved transitively. No
existing pin changed. `:data` sets `room.schemaLocation` to `data/schemas`; no
database, entity or placeholder schema ships. `:domain`, `:data` and `:design-system`
have no test sources, and `:domain`/`:data` no production sources yet.

Not done here by design: source-level composition-only `:data` import rule and other
architecture checks (SELLO-004), Room migration-test configuration and the customer
app's OS-backup disablement (SELLO-011), Sello tokens and catalog examples (SELLO-006).

2026-10-08: owner approved the delivery and authorized the commit; moved to Done.

### Delivery evidence
- **Revision:** `475d3f8` plus tested uncommitted changes: modified `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/java/com/software/sello/MainActivity.kt`, `README.md`, `docs/development/setup.md`, `docs/development/HANDOFF.md`, this epic and regenerated board exports; deleted `app/src/main/java/com/software/sello/ui/theme/` and `app/src/androidTest/`; new `domain/`, `data/`, `design-system/`, `catalog/`, `app/src/debug/`, `app/src/androidTestDebug/`. Not a tested clean commit.
- **Requirement mapping:** five modules → `./gradlew projects` lists `:app`, `:catalog`, `:data`, `:design-system`, `:domain`. Allowed graph → runtime-classpath reports: app debug/release = `:data`, `:design-system`, `:domain`; catalog = `:design-system` only; data = `:domain` only; design-system = no project; domain = `kotlin-stdlib` only; Room is absent from app's compile classpath. Customer never depends on catalog → no `:catalog` edge in app release classpath. Identities → `aapt2 dump badging`: debug APK `com.software.sello.debug` / Sello Debug, unsigned release APK `com.software.sello` / Sello, catalog APK `com.software.sello.catalog` / Sello Catalog. Variant-aware assertion → `app/src/androidTestDebug/.../ExampleInstrumentedTest.kt` and `catalog/src/androidTest/.../CatalogIdentityTest.kt`, both executed. Theme ownership without duplication → files exist only under `design-system`. Room/KSP/schema capability → temporary Room database in `:data` generated `ProbeDatabase_Impl.kt` and exported `data/schemas/com.software.sello.data.probe.ProbeDatabase/1.json`; fixture and schema then removed. Module inventory → `docs/development/setup.md`. Composition-only source rule for `:data` is not enforced by this ticket; it is SELLO-004's.
- **Red / Green:** build/configuration ticket, no financial behavior. Boundary red: a temporary `:domain` file importing `android.os.Bundle` failed `:domain:compileKotlin` with `Unresolved reference 'android'`; a temporary `:design-system` file importing a `:domain` class failed `:design-system:compileDebugKotlin` with `Unresolved reference 'domain'`. Both fixtures removed, gate rerun green. Identity red: with the former literal `com.software.sello`, `:app:connectedDebugAndroidTest` failed with a ComparisonFailure (expected `com.software.sello`, was `com.software.sello.debug`); with the debug expectation it passed.
- **Gate results:** G1 (`:domain:test :data:testDebugUnitTest :design-system:testDebugUnitTest :app:testDebugUnitTest :catalog:testDebugUnitTest lintDebug :app:assembleDebug :app:assembleRelease :catalog:assembleDebug --continue`) passed on the final tree. Superset `./gradlew clean :domain:test testDebugUnitTest lintDebug assembleDebug :app:assembleRelease assembleDebugAndroidTest --no-build-cache --no-configuration-cache --rerun-tasks --continue` passed, 421 executed tasks, including all four module test APKs. Executed host tests: one sample JVM test in `:app`, zero failures/errors/skips; `:domain:test` and the `:data`, `:design-system`, `:catalog` unit-test tasks are NO-SOURCE, not executed suites. Lint: zero errors; `:app` 22 warnings (18 pre-existing plus four newer-version advisories for the Android-library plugin, Room runtime/compiler and test runner), `:catalog`, `:data`, `:design-system` zero; nothing suppressed or baselined. Board generation, `--check` and board unit tests passed. Logs `/tmp/sello003-g1-final.log`, `/tmp/sello003-final-clean.log`; bootstrap runner exemption applies, no hosted CI claim.
- **Device / Artifact:** newly created isolated `sello_api30`, `emulator-5580`, API 30, Google APIs x86_64, 320×640, on the final build. `:app:connectedDebugAndroidTest` and `:catalog:connectedDebugAndroidTest` each executed one test, zero failures/errors/skips; `:data` and `:design-system` connected tasks ran with no tests. Debug app and catalog installed side by side, both `am start -W` returned Status ok, UIAutomator showed `Hello Android!` in `com.software.sello.debug` and `Sello Catalog` / `No components yet.` in `com.software.sello.catalog`; both stayed installed with separate `/data/user/0/` directories named after each application ID. Emulator shut down and its AVD removed. Not executed: installing the customer `com.software.sello` build beside them (release APK is unsigned; owner signs), physical device, launcher-icon visual check, API levels other than 30.
- **Review:** executor self-review of the exact diff, dependency reports, built package IDs and test XML. Project owner reviewed the handoff and approved on 2026-10-08, including the Room 2.8.4 and test-runner 1.5.2 catalog pins, and authorized the commit. This is owner acceptance, not an independent technical review; customer-build coexistence on a device remains for the owner's signed build.

## SELLO-004 — Install CI, quality gates and architecture enforcement

- **Type:** Task
- **Priority:** P0
- **Status:** Review
- **Depends on:** SELLO-003
- **Gate:** G1

### Outcome
Prevent boundary/build/test regressions from becoming normal before feature growth.
Make quality verification a required completion workflow for every subsequent task,
not a collection of optional tests. This ticket owns the enforcement tools.

### Deliverables
Host/device CI definitions, documented aggregate gate, formatting/static analysis,
composition-only import rules, dependency review and retained test/lint artifacts.
Validate the board with `board.py --check`. Keep configuration compatible with the
actual Git host; do not claim a hosted run without a remote/runner.
Deliver `scripts/verify-ticket` (target path), a versioned structured quality-report
schema, shared completion-evidence validator and tested executor/CI integration
implementing [QUALITY_FLOW.md](../QUALITY_FLOW.md). Extend the existing Python
board validator to verify reports, not merely the presence of an evidence field.
Document invocation, prerequisites, report retention, snapshot coverage and recovery
from failures in the execution guide. Require the host quality check/protected merge
policy with actual host/admin evidence; local hooks alone are not enforcement.

### Acceptance criteria
- Rules reject a feature importing data, domain importing Android, cross-feature
  internals, customer modules depending on catalog and debug tools in release.
- CI compiles customer release and catalog debug; tests fail the job instead of
  being swallowed. No blanket lint baseline hides new errors.
- Secrets are external; external contributions cannot access upload credentials.
- The runner derives the canonical ticket's gate without downgrade; failures,
  missing checks/devices/keys and skipped requirements return nonzero, never success.
- Review/Done transitions require a successful matching report with current input
  fingerprint. Wrong-ticket/profile, malformed, stale or unavailable evidence fails.
  Historical Done evidence remains verifiable without rerunning every predecessor.
- CI runs the same entrypoint against the merge candidate; required-check/merge
  protection is verified with the chosen host. Missing remote/admin access blocks
  hosted acceptance, not permission to mark configured YAML as executed CI.

### Tests
Demonstrate failing boundary fixtures and a failing test are caught, followed by
clean G1. Execute local gate and hosted CI when infrastructure is available; retain
reports and explicitly distinguish configured CI from executed CI.
Add runner/validator tests for absent reports, wrong IDs, insufficient gate,
nonzero checks, missing device, malformed schema and edited acceptance/code inputs.
Prove status/evidence-only edits do not stale a report, older completed tickets
remain valid, and checks can run before their own completion evidence exists.
Exercise a sample downstream ticket through run → evidence → Review; remove test
fixtures afterward without changing real ticket statuses. Verify host protection
with an authorized failing-check example; record actual results, never fabricate it.

### Working checkpoint
Document one reproducible gate used by every later ticket; app/catalog remain buildable.
An independent executor can follow the shared quality flow, and incomplete evidence
cannot pass the completion validator. Passing checks still require designated review.

### Context and starting points
Module files from SELLO-003 are the inputs; policy violations must fail before they
spread into features. Read architecture §§2/10, current Python board tests and
[verification guidance](../EXECUTION_GUIDE.md#tools-and-host-verification). No Git remote
or hosted CI is assumed available.

### Implementation plan
1. Inventory actual host/device/assembly tasks; add a documented aggregate Gradle or
   script entry point running the agreed G1 checks with retained machine-readable reports.
2. Configure formatting/Detekt/lint and architecture checks for production/debug source
   sets and module edges. Cover direct imports and qualified data references in features.
3. Add negative fixtures for prohibited edges and a deliberately failing test to
   prove checks return nonzero; fixtures must not remain compiled into customer code.
4. Configure host CI and pinned-device jobs for the chosen host; collect reports
   without exposing secrets. Device jobs may be scheduled separately but cannot vanish.
5. Add board check/tests to CI. Record which workflow actually executed; if no remote
   exists, retain local gate evidence and request runner/host access for hosted acceptance.
6. Implement the ticket-derived runner and report schema from QUALITY_FLOW.md.
   Inventory required tasks per gate; record actual execution, environment and
   input fingerprints. Avoid requiring final evidence before running the gate itself.
7. Upgrade board completion validation to consume retained reports, including
   current transition freshness versus historical Done evidence. Define artifact
   retention/retrieval and fail explicitly when reports cannot be retrieved.
8. Add negative bypass/freshness tests; update agent/runbook instructions with the
   working command. Configure and verify the chosen host's required quality check
   and merge protection with its administrator; leave acceptance incomplete until
   hosted results and protection evidence are available.

### Concrete cases and pitfalls
- A feature importing a concrete DAO through an alias/qualified name must not bypass
  the source boundary. Explain the checker coverage/limits rather than claiming complete compiler proof.
- New warnings cannot disappear into a blanket baseline; dependency review is separate
  from a lint version suggestion. PR code must never receive upload credentials.
- Static tooling belongs in the build/test path, not domain or release dependencies.
- A report for SELLO-017 cannot complete SELLO-018; G1 cannot satisfy G2. A copied
  success field is not an executed report. Changed code/acceptance requires rerun;
  adding evidence or regenerating board exports must not create a freshness loop.

### Verification recipe
Execute each negative fixture, remove it, then run the aggregate gate and board unit
tests. Archive output plus actual CI run IDs if available. Device tests require the
[device procedure](../EXECUTION_GUIDE.md#device-and-process-restoration), not test-APK assembly alone.
Run the new entrypoint for SELLO-004 itself after completed predecessor inputs,
validate its retained report, and demonstrate rejected invalid completion fixtures.
Attach local report and hosted check/protection evidence to `Quality run` and delivery
fields; request review only after required checks pass. Do not mark Done without
hosted acceptance or silently replace it with local-only verification.

### Execution progress
2026-10-08: started after SELLO-003 was committed and pushed. Owner inputs settled:
GitHub remote `Okabe94/Sello` (public, `main`) with push and admin access verified;
device tests run in CI on every pull request; ktlint 1.8.0 through ktlint-gradle
14.2.0 approved subject to a compatibility probe; Detekt deferred because the latest
stable 1.23.8 predates this toolchain and 2.0 is still alpha (to be recorded in an
ADR); architecture rules will be an in-repo script, not a library.

Part 1 of 5 delivered, uncommitted: `scripts/verify-ticket` with `scripts/quality/`
(`fingerprint.py`, `gates.py`, `runner.py`) and 29 unit tests written before the
implementation. The runner derives the gate from the canonical ticket, refuses
unknown tickets, unfinished prerequisites, downgrades, a missing toolchain or device
and the not-yet-available G3, and writes a schema-version-1 report under ignored
`build/reports/quality/`. A real G1 run for this ticket passed. G2 has only been
exercised with a simulated device.

Part 2 of 5 delivered, uncommitted: `scripts/quality/report-schema-v1.json` and
`evidence.py`, plus `board.py --check` now validating the retained report of every
Review/Done ticket from SELLO-004 onward (`--skip-evidence` for use inside a run,
`--base` to compare statuses with a merge base). Owner chose to commit final reports
under `docs/planning/mvp/quality-reports/` and keep CI artifacts as well;
`verify-ticket --retain` copies a passed report there and prints the `Quality run`
field. 60 quality-tool tests and the 32 existing board tests pass; disabling the
report hook in `board.py` makes seven of the nine end-to-end tests fail.

Part 3 of 5 delivered, uncommitted: `scripts/quality/architecture.py` with 26 tests
(module edges, module boundaries, domain purity, composition-only `:data`,
composition-is-root, feature isolation, debug tools in release; coverage and limits
are stated in the file header). Eight deliberate violations placed in the real tree
were all reported and then removed. ktlint 1.8.0 through ktlint-gradle 14.2.0 passed
its probe on Kotlin 2.4.20, AGP 9.3.3 built-in Kotlin and Gradle 9.5.0 and is applied
to all five modules with `.editorconfig` (Android Studio style, Composable naming);
existing sources were auto-formatted and two wildcard test imports made explicit. Both
checks are in the G1 profile, and a test keeps the documented gate in the board README
identical to `gates.py`. 91 quality-tool tests pass; a real G1 run passed after first
failing, correctly, on stale generated board views. Lint remains at 22 warnings.

Parts 4 and 5 delivered on branch `sello-004-quality-gates`, pull request 1:
`.github/workflows/quality.yml` runs `verify-ticket --merge-candidate BASE --gate G2`
on an API 30 emulator for the merge commit of every pull request and for `main`,
then `board.py --check --base BASE`, and uploads reports for 90 days. Actions are
pinned to commit SHAs, permissions are read-only and no secret exists. `main` is
protected. Owner chose dependency review by strict Gradle dependency locking alone
(`scripts/update-dependency-locks`) over GitHub's dependency review. ADR 0006, the
execution guide, quality flow, setup, README and agent rules describe the result,
and the owner's pull request format is the repository template.

Defect found and fixed during hosted verification: CI and the local machine computed
different fingerprints for the same commit, because `gradlew.bat` has CRLF endings
in this working tree and LF in the repository. The fingerprint now hashes files as
Git stores them; a regression test compares a working tree with a fresh clone.

Not delivered, by decision or dependency: Detekt (deferred, ADR 0006); an executable
G3 profile (SELLO-035); vulnerability and licence scanning of dependencies; locking
of Gradle plugin classpaths. The architecture checker's stated limits apply.

### Delivery evidence
- **Revision:** branch `sello-004-quality-gates`; tested snapshot is commit `9639a99`, with only this ticket's status, progress and evidence text and regenerated board views added afterwards. Those later edits are bookkeeping that the input fingerprint excludes by design. Hosted runs tested the pull request merge commits named below.
- **Requirement mapping:** boundary rules (feature importing data, domain importing Android, cross-feature internals, customer modules depending on catalog, debug tools in release) → `scripts/quality/architecture.py`, 26 tests, eight real-tree violations. CI compiles customer release and catalog debug, failing tests fail the job → `gates.py` task list run by `.github/workflows/quality.yml`; no lint baseline exists. Secrets external → the workflow uses none, has read-only permissions and runs on `pull_request`. Runner derives the gate without downgrade and returns nonzero on failures, missing checks, devices or toolchain → `scripts/quality/runner.py`, `test_runner.py`, `test_merge_candidate.py`. Review/Done need a matching current report; wrong ticket, lower gate, malformed, stale or absent evidence fails; historical Done stays verifiable → `evidence.py`, `board.py`, `test_evidence.py`, `test_board_evidence.py`. CI runs the same entrypoint on the merge candidate with verified protection → hosted runs and protection settings below. Dependency review → strict Gradle locking. Schema → `scripts/quality/report-schema-v1.json`. Documentation → execution guide "Quality runner", QUALITY_FLOW, ADR 0006.
- **Red / Green:** each tool's tests were written first and run against a deliberately naive implementation: fingerprint 5 of 13 failing, runner all 16, evidence 16 of 22, architecture 24 of 26, merge candidate 10 of 12, then all passing. Disabling the report hook in `board.py` fails 7 of the 9 end-to-end board tests. Real negative runs: eight architecture violations in the real tree reported with file and line; a badly spaced function failed `ktlintCheck`; a deliberately failing JVM test made `verify-ticket SELLO-004` exit 1 with "gradle-host reported 1 failing tests" (run `20261009T031303Z-843bf00d`); a library version bump without a refreshed lock failed resolution with "Dependency version enforced by Dependency Locking"; stale generated board views failed a real gate run. All fixtures removed.
- **Gate results:** 104 quality-tool tests and 32 board tests pass. Local `./scripts/verify-ticket SELLO-004` passed G1, and passed G2 on an isolated API 30 emulator with strict locking (run `20261009T030417Z-45975c74`, two device tests). One JVM test executes; `:domain`, `:data`, `:design-system` and `:catalog` unit-test tasks have no sources. Lint has zero errors and 22 warnings, all newer-version advisories or scaffold leftovers, none suppressed. ktlint prints a JDK 25 `sun.misc.Unsafe` deprecation warning.
- **Quality run:** run 20261009T031336Z-dca51595; SELLO-004 G1 passed; HEAD 9639a99, inputs sha256 57cf02463ba3; report docs/planning/mvp/quality-reports/SELLO-004.json sha256 2f2333560833552c1382646fd814ae4eae1ac16bf5f8a78045500b7ff032d083
- **Device / Artifact:** hosted GitHub Actions runs on `ubuntu-latest`, Temurin 25, API 30 Google APIs x86_64 emulator. Run 37876804478 (first push) and run 37877672196 (with locking and documentation) passed every step; the second run's retained artifact `quality-reports` holds report `20261009T030712Z-4eaf3fda`: SELLO-004, G2, passed, clean merge commit, one host test, two device tests. Protection on `main`: required check `quality`, branch must be up to date, pull request required, administrators included, force pushes and deletions off; a direct push was rejected. Pull request 2 added a forbidden Android import to `:domain`: run 37877708840 failed with "architecture exited 1", merge state was BLOCKED, and both a normal merge and an administrator override were refused; it was closed unmerged and its branch deleted. Not executed: physical device, API levels other than 30, a pull request from a fork.
- **Review:** executor self-review of the diff, reports, hosted logs and protection settings. Designated review by the project owner is outstanding; no independent approval is claimed.

## SELLO-005 — Wire production composition and distinct time sources

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-001, SELLO-003, SELLO-004
- **Gate:** G1

### Outcome
Make financial dates, audit time and lifetime ownership injectable without shipping debug controls.

### Deliverables
Required constructor-based Koin composition, platform-neutral observable financial
clock and zone contract, separately injected audit/monotonic timing ports, dispatchers
and owned scopes. Production clock reacts to midnight/resume/zone/time changes;
debug composition can replace only financial time. Preference contracts are neutral.

### Acceptance criteria
- Logic never calls global DI or reads wall time ad hoc; cancellation propagates.
- Financial zone is initialized once and later travel does not rewrite effective
  history. Until Room setup exists, use an explicit initialization path, not fake persistence.
- Missing required bindings fail graph construction; no successful no-op financial services.
- Financial stepping cannot change undo timers or real audit timestamps.

### Tests
Controlled-clock rollover/resume/time-zone tests, virtual-time lifecycle/cancellation
tests, and production/debug graph construction tests; run G1.

### Working checkpoint
Existing app/catalog launch under production time; pure tests exercise the future
sandbox seam without introducing its controls into release.

### Context and starting points
Legacy date-sensitive code diverged through wall-clock reads and global services.
Use the approved financial-zone decision and SELLO-003 module graph. Read architecture
§§2/4/9 and [contract checklist](../EXECUTION_GUIDE.md#contract-and-failure-checklist).
Domain clock ports are neutral; Android system observation belongs in app/platform.

### Implementation plan
1. Define financial date/zone/as-of observation, audit Instant and monotonic elapsed
   ports separately; add coroutine dispatcher/scope ownership without global lifetime lookup.
2. Implement real system adapters with explicit startup/resume/midnight/time-change
   recomputation and cancellation. Persisting financial zone is SELLO-011, not a fake default save.
3. Build app composition modules using required constructors; future workflows bind
   only as they exist. Do not require a successful dummy repository for graph creation.
4. Provide controlled ports in test/debug source sets and production graph checks;
   centralize mapping from adapters to consumer contracts.
5. Document lifecycle ownership and disposal so later sandbox session switches can
   cancel old observers and replace financial time without changing the other clocks.

### Concrete cases and pitfalls
- Month rollover while backgrounded must be observed on resume; travel must not
  change stored effective dates. Audit time and monotonic undo/inactivity remain real.
- Cancellation cannot be converted into Failed/empty financial data. A required
  missing binding is a wiring error, not a successful no-op.
- A mutable test clock must publish changes so snapshots update; changing a value
  in a field without an observable signal is not a working simulation seam.

### Verification recipe
Create/run `*FinancialClockTest`, `*ClockOwnershipTest` and composition graph tests
with controlled coroutine time. Run G1; retain midnight/resume/zone/cancellation
assertions and confirm app/catalog launch with production adapters.
