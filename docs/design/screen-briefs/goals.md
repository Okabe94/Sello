# Metas brief

**Confirmed direction · Optional dates, withdrawal records and a celebratory
completed-goals section confirmed; v2 then changed the milestone display after a
withdrawal and the completion trigger (see the note). Remaining open decisions are not
yet approved. Final visual design: Sello v2.**

> **Designed in [Sello v2](../v2/README.md):** [the Metas tab](../v2/metas.html), [goal detail](../v2/goal.html), [Aportar / Retirar](../v2/aportar.html), [create and edit](../v2/create.html) and [celebrations](../v2/celebrate.html). v2 changed two answers recorded here: an earned stamp stays (faded) after a withdrawal, and goals complete with **Marcar como lograda** instead of automatically. Where this brief and v2 differ,
> v2 and [ADR 0013](../../decisions/0013-sello-v2-design.md) apply; the open policies below stay open.

Use the [brief workflow and decision labels](README.md). Metas answers “What am I
working toward, how far have I come, and what can I do next?” Reaching personally
meaningful goals is the purpose behind the budgeting routine.

## Confirmed intent from the interview

- Goals are created and defined by the user. Do not prescribe the same goal or
  savings ratio to everyone.
- Target dates are optional. A person can create a goal without a deadline.
- Money used from savings before goal completion is recorded as a withdrawal,
  preserving contribution history rather than editing the saved total directly.
  Exact ledger, limits and remaining milestone lifecycle effects need scoped decisions.
- *(Superseded by v2: the earned stamp stays, faded.)* If a withdrawal drops progress
  below a milestone, its current stamp no longer appears earned. Reflect the current amount/progress quietly, without a warning
  ceremony, shame or dramatic loss-of-achievement feedback. Preserve underlying
  contribution/withdrawal history; this is a display decision, not history erasure.
- Reaching that milestone again through later recorded progress earns another
  small celebration. Simply reopening/restoring/redrawing the same state is not
  a new crossing or another financial operation.
- Put completed goals in an accessible **Completadas** section, with a celebratory
  feel when the person browses and revisits them. These achievements should be
  enjoyable to return to rather than an administrative archive.
- *(Superseded by v2.)* When confirmed recorded savings reach or exceed the target
  amount, announce it; **Marcar como lograda** completes the goal, with a short
  Deshacer, and a completed goal is read-only. Target edits/corrections and later reopening remain
  scoped lifecycle decisions.
- Give Metas a primary destination when it ships. It should not depend on opening
  investments or a Patrimonio switch. Keep at most four working main destinations.
- Show one compact goal reminder on Home. Respect the user's choice; until they
  choose, use the active goal with the highest completion percentage.
- Accept a recorded contribution as goal progress. There is no bank connection,
  receipt/proof requirement or separate transfer-verification step.
- Keep goals and investments independent. A user can represent the same money in
  both; do not introduce linking, automatic reconciliation or a combined wealth
  total from those records.
- Contributions do not independently allocate income or change Home's category
  budget. Users can create category budgets for saving/investing if desired.
- Celebrate milestones modestly and completion substantially. Encourage progress
  and adjustments without making people feel they failed.

## Recommended information hierarchy

Lead with the user's goals and their progress rather than a large aggregate sum.
For each goal, make its name, saved amount, target and completion percentage easy
to read. Keep a direct contribution action and access to detail. Remaining amount
and the next milestone can support that answer without turning each goal into a
dashboard. Do not require long press to discover Aportar.

In goal detail, explain progress toward that specific purpose, followed by its
contribution history and relevant milestone history. Keep editing available without
giving management actions the same emphasis as progressing toward the goal.

Keep completed goals accessible in Completadas as evidence of progress, with an
inviting, celebratory presentation on revisit. Exact navigation/composition and
treatment of paused/archived goals remain open. Do not infer the Metas list's
ordering from the separate Home featured-goal default.

## Contribution and feedback direction

**Recommendation:** a short contribution flow should collect only the information
needed to record it and return to the goal with updated progress after confirmation.
Reuse the entry principles where appropriate: exact amount, stable primary action,
retained draft and truthful busy/error/recovery states. No bank-transfer claim or
extra allocation step belongs in that flow.

An ordinary contribution can receive brief feedback. A confirmed milestone crossing
can add a small celebration; completing the goal earns the larger moment. Respect
reduced motion and provide readable equivalents. Celebration is feedback about a
confirmed result, never evidence that an uncertain contribution was saved.

Revisiting completed goals also carries fanfare, as the owner requested. The
designer chooses its concrete visual/motion treatment; a revisit does not create
another completion record or financial operation. Do not infer that every revisit
must replay an unskippable full-screen animation.

Milestone dates come from actual recorded crossings under architecture. Any
projected arrival date must state its assumptions. The v1.1 reference's
25%/50%/75%/completion stamps are a visual starting point, not a new approval of
thresholds, projection assumptions or replay behavior in this redesign.

## Tone and visual direction

Use Sello's paper, stamps and bold exact amounts to make progress tangible. Give
achievements more emotional weight than routine recording. When someone changes
their plan, acknowledge what is already saved and make the next action clear.

Avoid shame-based overdue labels or treating a slower month as failure. Date-based
warnings and red budget-deficit styling do not automatically belong on goals.
Do not promise a completion date based only on an assumed future contribution.

## Scenarios for review

| Situation | Intended reading/behavior and status |
| --- | --- |
| No goals | Recommend a welcoming invitation to name something meaningful, with a direct create action. |
| A goal has no target date | Allow it without requiring a deadline or inventing an overdue status. |
| Two eligible active goals are 57% and 38%; no Home choice exists | The 57% goal is featured on Home under the confirmed default. |
| The user explicitly chooses the 38% goal for Home | Keep that choice; automatic ranking must not override it. |
| A contribution is recorded | Accept the manual record as progress; category limits and income allocation do not change. |
| Saved money is used before completion | Record a withdrawal and retain contribution history. Show the true current progress and remove the earned appearance of milestones now above it, without making a fuss. Detailed validation/re-crossing rules remain open. |
| The same money is also tracked as an investment | Keep the records independent; no second allocation deduction or combined wealth claim. |
| A confirmed milestone is crossed or the goal completes | Small or substantial celebration respectively; exact triggers/replay rules remain open. |
| Progress falls below a milestone, then later reaches it again | Restore its current earned appearance and give another small celebration. Reopening/redrawing the same state alone is not another crossing. |
| A confirmed contribution brings savings to or above the target | *(v2)* Announce 100 %; Marcar como lograda completes the goal with the substantial celebration and a short Deshacer. (Earlier: complete automatically, no manual completion step. |
| The person browses or opens a completed goal | Keep it in an accessible Completadas section and make revisiting the achievement feel celebratory. Exact treatment belongs to the designer. |
| The person needs to adjust a plan | Retain a supportive tone and visible progress; specific target/date/withdrawal effects need explicit rules. |
| Loading, failed read or uncertain contribution outcome | Distinguish unavailable data from no goals; preserve the draft and recover an uncertain operation before another write. |

These are review scenarios, not an implemented goal ledger or independently
approved financial example corpus.

## Follow-up decisions confirmed

The owner chose **Optional target date** and **Record a withdrawal** on
2026-10-15 in response to the two presented questions. The second choice accepts
a withdrawal entry preserving contribution history. Neither answer approves
withdrawal bounds, corrections, milestone replay, completion reopening or automatic
effects on expense/income/category records. Those meanings must be explicit before
dependent implementation.

The owner then rejected keeping a milestone stamp earned after progress falls
below it: **“We should be honest with the progress, just not make much fuss about
it.”** The current milestone display follows actual progress. This supersedes the
previous recommendation to retain the stamp as earned; contribution/withdrawal
history remains intact. The owner accepted a separate completed section and asked
for **“fanfare around seeing them, like revisiting them.”** Completadas therefore
has a celebratory revisit direction, without choosing final animation or layout.

When asked whether reaching the target should complete the goal automatically
or require a manual action, the owner chose **automatic completion**. This confirms
the ordinary saved-amount trigger and placement in Completadas. It does not settle
target-edit/correction effects, later use of completed savings or reopening rules.

The owner accepted another small celebration when progress reaches a milestone
again after a withdrawal took it below that threshold. This confirms genuine
re-crossing feedback, not animation replay on every visit or repeated processing
of the same operation. Exact multi-threshold/correction behavior remains scoped work.

## Decisions to discuss next

1. **Other goal lifecycle:** creation fields, optional planned contributions, list order,
   pausing, archive/removal, use of savings after completion and a removed/completed
   Home pin. Completadas is not approval of automatic archival or data removal.
2. **Milestones:** thresholds, correction-driven crossings, multiple
   thresholds in one contribution, target edits, exact replay safeguards and reopening
   a completed goal. Do not invent these rules in a visual design.

## Designer discretion and scope

The designer chooses overview/detail composition, progress treatment, milestone
styling, action placement and animation within the agreed tone and accessibility.
Use the unchanged visual foundations without copying the HTML's goal overlay as
the complete Metas destination.

Metas remains a future capability in the Next increment. This brief adds no routes,
schema, recurring-contribution engine or production catalog component. Applicable
financial decisions, design review and native verification precede implementation.

Sources: [philosophy](../philosophy.md), [ADR 0011](../../decisions/0011-goal-centered-design.md),
[category-only allocation](../../decisions/0012-budget-guidance-contract.md#bg4--category-only-income-allocation-independent-tracking)
and [architecture](../../../ARCHITECTURE.md).
