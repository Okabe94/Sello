# Metas brief

Designed in [Sello v2](../v2/README.md) as [the Metas tab](../v2/metas.html),
[goal detail](../v2/goal.html), [Aportar / Retirar](../v2/aportar.html),
[create and edit](../v2/create.html) and [celebrations](../v2/celebrate.html); labels
and authority are in the [briefs README](README.md). Metas answers “What am I working
toward, how far have I come, and what can I do next?” Reaching personally meaningful
goals is the purpose behind the budgeting routine.

## Confirmed intent

- Goals are created and defined by the user; no goal or savings ratio is prescribed
  to everyone. Target dates are optional.
- Money used before completion is recorded as a withdrawal, preserving contribution
  history rather than editing the saved total. A withdrawal cannot exceed what the
  goal has saved.
- Progress is honest without fuss: after a withdrawal the percentage and bar show
  the true figure, and an earned stamp stays, faded, as a past achievement. Reaching
  it again lights it once more; redrawing or reopening the same state is not a new
  crossing or another financial operation.
- Reaching 100 % is announced; **Marcar como lograda** completes the goal, with a
  short Deshacer and no later reopening. A completed goal is read-only and its only
  action is deletion.
- Completed goals stay in **Completadas**, celebratory to browse and revisit, not an
  administrative archive. A revisit creates no completion record or operation.
- Metas gets its own main destination when it ships, independent of investments,
  with at most four working main destinations.
- One goal is featured on Home. The user's choice wins; until they choose, the
  active goal with the highest completion is shown.
- A recorded contribution is goal progress: no bank connection, proof or transfer
  verification.
- Goals and investments stay independent, even for the same money: no linking,
  reconciliation or combined wealth total. Contributions never allocate income or
  change category budgets; users can create saving/investing categories if they want.

## Tone

Use Sello's paper, stamps and bold exact amounts to make progress tangible. Celebrate
milestones modestly and completion substantially; achievements weigh more than
routine recording. When someone changes their plan, acknowledge what is already
saved and make the next action clear. No shame-based overdue labels, no red
budget-deficit styling and no treating a slower month as failure. A projected date
states its assumptions; never promise a completion date from an assumed future
contribution.

Celebrations follow only a confirmed result, never an uncertain one, and reduced
motion keeps the same meaning.

## States to preserve

| Situation | Intended behavior |
| --- | --- |
| A goal has no target date | Allowed, with no deadline required and no invented overdue status. |
| Two active goals at 57 % and 38 %, no Home choice | The 57 % goal is featured on Home. |
| The user chooses the 38 % goal for Home | Keep that choice; automatic ranking never overrides it. |
| A contribution is recorded | Accepted as progress; category limits and income allocation do not change. |
| The same money is also tracked as an investment | Records stay independent; no second allocation deduction or combined wealth claim. |
| Loading, failed read or uncertain contribution outcome | Unavailable data is not “no goals”; the draft is kept and an uncertain operation is recovered before another write. |

These are review scenarios, not an implemented goal ledger or approved financial
example corpus.

## Open

Corrections, target edits, the Home goal after completion or deletion, pausing (not in
the first version), past-dated movements and the number of goals were decided in
[ADR 0017](../../decisions/0017-goals-recurrence-investments.md). Still open, for the rules ticket (SELLO-086) to settle with examples:

- Exact milestone replay safeguards.

Metas belongs to the Next increment ([architecture](../../../ARCHITECTURE.md) §1), outside the MVP.
This brief adds no routes, schema, recurring-contribution engine or production
component; financial decisions, design review and native verification come first.
