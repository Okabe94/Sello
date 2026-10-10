# Money, dates and bounded text

The rules in `:domain` that every later boundary reuses. They are pure Kotlin: no
Android, DI, database or clock reads. A rule returns `Outcome.Success(value)` or
`Outcome.Failure(error)` with a typed error; it never throws for invalid input and
never substitutes zero, empty or a "repaired" value.

| Need | Use | Notes |
| --- | --- | --- |
| An exact amount | `Money(minorUnits, currency)` | May be zero or negative. `+` and `-` are checked and return an `Outcome`. |
| A total of many amounts | `ExactTotal.of(currency, amounts)`, `+`, `-`, then `toMoney()` | Widened while building, so order never matters; only the final figure is range-checked. Use it for every sum of records, not chained `Money +`. |
| One expense or income amount | `TransactionAmount.of(money)` | 1 to 999.999.999.999 COP, positive, COP only. Apply it at entry, commands, row decoding and backup validation (ADR 0003). Not a bound on totals or category limits. |
| Typed or pasted COP text | `CopAmountInput.parse(text)` | Plain ASCII digits or correctly grouped dots. Zero parses; the caller decides whether zero is allowed. Errors carry the original text. |
| A stored or exchanged date or month | `EffectiveDates.parseDate`, `parseMonth` | Strict `yyyy-MM-dd` and `yyyy-MM`, real calendar values only. |
| A manual entry's date | `EffectiveDates.forManualEntry(date, today)` | `today` comes from `FinancialClock`; later dates are `InFuture`. |
| A selectable month | `EffectiveDates.forSelection(month, today)` | Up to the current month. |
| A category name | `CategoryName.of(raw)` | Trimmed, composed (NFC), 1–24 code points. Compare `uniquenessKey` for duplicates, including archived categories; never display or store the key as the name. |
| A note | `Note.of(raw)` | Null or blank means no note; otherwise up to 60 code points. |
| An income source | `IncomeSource.of(key, otherName)` | Stable keys `salary`, `freelance`, `passive_income`, `transfer`, `other`. Labels are resources in presentation. |

## For consumers

- **Controls (SELLO-008):** the keypad emits ungrouped digits and stops at
  `TransactionAmount.MAX_DIGITS`; pasted text goes through `CopAmountInput.parse`
  unchanged. Show the error for the original text; do not strip characters first.
- **Room ([storage](storage.md)) and backup (SELLO-028/030):** decode a stored amount with
  `TransactionAmount.of` and a stored name with `CategoryName.of`. A failure is a typed
  read or validation failure for the whole operation, not a skipped or clipped record.
  Enforce category uniqueness on `uniquenessKey` with a real constraint.
- **Calculations (SELLO-012 onward):** build totals with `ExactTotal`. A
  `MoneyError.Overflow` is a result to surface, not to replace with a maximum.
- Financial identity holds no `Instant` and no label. Audit time comes from
  `AuditClock` and belongs to the record's audit fields.
- `Currency.USD`, `EUR` and `GBP` exist only so future policies can be named. They
  cannot become a `TransactionAmount`, and there is no rate or rounding code yet.

## Showing money

Formatting and drawing have different owners (SELLO-007):

- `MoneyFormatter` in `:app` (`presentation.money`) turns a `Money` into a
  `MoneyTextValue`: sign, currency code (none for pesos, ADR 0007), the full es-CO
  grouped figure and the words a
  screen reader says, taken from string resources through `ResourceMoneyLabels`. It
  never abbreviates; short forms such as `3,53 M` are for chart axes only.
- `MoneyText` in `:design-system` only draws a `MoneyTextValue`. It does not know
  `Money`, compute or round. `CountingMoneyText` animates between two amounts but always
  announces, and ends on, the exact target; with reduced motion it does not count.
- A failed read has no `MoneyTextValue`. Show `ErrorSlip`, never a zero.

## Entering an amount

- `AmountDraft` in `:domain` is the amount being typed for one expense or income:
  `Empty`, `Digits` (1–12, no leading zero) or `Rejected` (pasted text kept exactly as
  received, with its typed reason). `digit`, `tripleZero` and `backspace` return the next
  draft; a refused thirteenth digit reports `limitReached`. `paste` runs the whole text
  through `CopAmountInput` and `TransactionAmount`. An empty draft is not zero, and only
  `toTransactionAmount()` yields something a command may receive, which the command
  validates again.
- The controls in `:design-system` hold no rules. `AmountKeypad` reports presses,
  `AmountField` shows the amount, an empty draft or refused text, and `FormField` returns
  exactly what was typed. A feature maps its typed error to a `FieldMessage`; the
  control shows it and never changes the value instead.
- A loading `SelloButton` ignores presses, which prevents an accidental double tap. It
  does not make a command idempotent; operation IDs do (SELLO-013).
