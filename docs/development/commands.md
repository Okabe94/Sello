# Commands and receipts

**Owner:** SELLO-012 · Rules: [architecture](../../ARCHITECTURE.md) §5.

How a change to financial data is made so that its result is always knowable. The
first command is creating an expense (`ExpenseCommands` in `:domain`,
`RoomExpenseCommands` in `:data`). Later commands follow the same steps; there is
deliberately no generic "repository" that does everything.

## What a caller does

1. Pick an `OperationId` (from `RecordIdSource`) **before** submitting and keep it
   with the draft, so it survives rotation and process death.
2. Submit the command with that identifier and the `generation` it was prepared against.
3. Act on the outcome:

| Outcome | Meaning | Next |
| --- | --- | --- |
| `Committed(expense, replayed)` | Saved. `replayed` is true when this operation had already committed and this call wrote nothing | Show the receipt. Celebrate only when `replayed` is false |
| `Rejected(reason)` | Certainly not saved; the reason is typed | Show the reason; the draft is still valid to correct |
| `OutcomeUnknown(operationId)` | The commit itself failed in a way that does not say whether it took effect | Call `find(operationId)` or submit the **same** command again. Never a new identifier |
| a `CancellationException` | The caller was cancelled. It says nothing about the save | On return, `find(operationId)` |

`find` returns the committed result, `null` when the operation did not commit, or a
`StorageFailure`. It never guesses.

## What the transaction does, in order

1. Read the profile. A different `generation` is `StaleGeneration`: the history was
   replaced, so even an operation that once committed is refused.
2. Look for a receipt with this operation identifier. Same input: return the
   original result. Different input: `OperationConflict`.
3. Validate against stored state: the category exists and is not archived, and the
   date is not after the financial day.
4. Advance the revision by one, insert the record, insert the receipt.

Every rejection returns before the first write. A database error during these steps
rolls everything back and is `Rejected(StorageFailed)`. Only an error from the
commit itself is `OutcomeUnknown`.

## Guarantees and how they are kept

- **One operation, one record.** The receipt's primary key is the operation
  identifier, and the check and the insert share a transaction, so two simultaneous
  submissions cannot both write.
- **A cancelled caller cannot leave half a write.** The caller is checked before the
  transaction starts; once started, the transaction always runs to its end, and the
  caller is checked again afterwards. A caller cancelled meanwhile gets the
  cancellation while the receipt is already durable.
- **Input identity.** `inputDigest` is a SHA-256 over the meaningful fields, each
  written as byte length, colon, bytes: kind, format version, generation, category,
  amount, currency, date, note-present flag, note. The operation identifier is not
  part of it. A label, a formatted amount or an object's text form never is.
- **Time.** Rows and receipts are stamped by `AuditClock`. `FinancialClock` is asked
  only which day it is, so simulated time can never appear in a timestamp.
- **No side work in the transaction.** No file, preference, network or notification
  call happens inside it. A later failure of any such follow-up cannot turn a saved
  expense into "not saved"; the receipt already exists.

## Not here yet

- No totals are checked when saving. With each record at most 999.999.999.999 COP, a
  month would need over nine million records to exceed what a total can hold, far
  beyond the 100.000-record backup bound. Totals are checked where they are
  computed (SELLO-014).
- `Committed` has no follow-up state because nothing follows a save yet.
- Editing, deleting and undo (SELLO-019) and category and limit commands
  (SELLO-013) reuse these steps with their own typed commands and receipts.
