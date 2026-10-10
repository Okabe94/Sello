# Categories and monthly limits

**Owner:** SELLO-013 · Decisions: [D04, D05, D07](../decisions/0001-mvp-contract.md) ·
Rules: [architecture](../../ARCHITECTURE.md) §§4, 5.

How a category and its budget change over time without changing what past months
meant. Screens use the ports in `:domain` (`CategoryCommands`, `CategoryReads`);
they never write limits themselves.

## What a month's limit is

Each category has a history of **defaults**, each effective from a month onwards,
and optionally a limit set for **one month in particular**. For any month:

1. A limit set for that month wins.
2. Otherwise the default in force that month applies: the latest one effective that
   month or earlier.
3. If that default is a pause, the category is archived for that month: no budget.
4. If there is no default that early, the month is unconfigured. Nothing is assumed
   backwards from a later default.

`CategoryBudgetPolicy.stateFor` is that rule and the only place it lives. Its answer
is `Limited(limit, explicit)`, `Paused` or `Unconfigured`; a limit is `Finite`
(zero is a real budget of nothing) or `Unlimited`. These five are never merged.

Nothing is created per month. A month nobody opened answers from the same history
whenever it is read, so the answer cannot depend on when the app was running. There
is no carryover: each month starts from its own limit.

## Commands

All follow [commands and receipts](commands.md): operation identifier, generation,
replay, one transaction, a receipt. Edits also carry the category `version` the
caller last saw; a category and its limits share that version, and every committed
change raises it by one. An edit against an older version is `VersionConflict`.

| Command | Effect | Refused when |
| --- | --- | --- |
| `CreateCategory` | New category; its limit is this month's and the default after it | The name is taken, ignoring case and accents, even by an archived category |
| `RenameCategory` | Only the shown name; identity, expenses and limits stay | The name is taken by another category |
| `ChangeCategoryIcon` | Only the icon | |
| `SetDefaultLimit` | The limit from the current month onwards; a second change in the same month replaces the first | The category is archived |
| `SetMonthLimit` | The limit of exactly that month, current or past | The month has not started |
| `ArchiveCategory` | No new expenses; this month keeps its limit; paused from next month | Already archived |
| `UnarchiveCategory` | Same month: the pause is removed and nothing else changes. Later: the last default applies again from this month; the paused months stay paused | Not archived |

"Current month" is the financial month from `FinancialClock`. No command deletes a
category, an expense or a past limit.

## Reading

`CategoryReads.monthBudget(month)` returns every category, archived ones included,
each with its state in that month, plus the generation and revision the answer was
read at, all in one transaction. A damaged row fails the whole read.

- **Choosing a category for a new expense:** only those with `archived == false`.
  The expense command refuses an archived category anyway.
- **Showing a month:** archived categories stay in the answer so that a month where
  they had a limit or spending remains explainable. Whether one is shown also
  depends on its expenses that month, which the monthly snapshot adds (SELLO-014).

## Not here yet

- Observing changes, and combining limits with spending: SELLO-014.
- Removing a month's own limit to fall back to the default: no screen needs it yet.
- Carryover of surplus or overspending: post-MVP (SELLO-E09).
