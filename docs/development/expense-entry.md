# Anotar: recording an expense

**Owner:** SELLO-017 · Decisions: [D03, D05](../decisions/0001-mvp-contract.md) ·
Design: [Anotar](../design/sello-spec.html#s-anotar) · Builds on
[commands](commands.md), the [monthly snapshot](monthly-snapshot.md) and the
[app shell](app-shell.md).

The first screen that writes money. Its rule: what is shown as saved is only ever
what storage confirmed.

## Getting there

- The **dock** on Recibo ("Anotar un gasto"). With no category that can take an
  expense it opens the [category editor](category-editor.md) with the reason, and
  after the category is created goes on to the entry form with it chosen.
- A **link**, `sello://anotar?categoria=<identifier>&monto=<amount>`, declared in
  the manifest for apps on the device (not for web pages). It follows the same path.
- The route is `ExpenseEntryRoute(categoryId, amount)`. Both are hints for the draft
  from outside the form; the form checks them like any input and drops what is not
  valid. Neither is a record or a result.

## The draft

| Field | Rule |
| --- | --- |
| Amount | Keypad only, through `AmountDraft`: up to twelve digits, a thirteenth is refused and signalled. Pasted text is taken whole or kept exactly as it was with the reason. Never zero, never changed into another amount |
| Category | One of the categories that are not archived. Chosen at opening, once: the one the caller asked for if it can take an expense; otherwise the one used last; otherwise the only one. Otherwise none, and the person chooses |
| Date | The financial day the form was opened; it stays that day if the draft is restored later. Any earlier day can be picked; a later one cannot |
| Note | Optional, up to 60 characters, one line. Blank means no note |

The button is off until there is an amount, a category and a valid note, and a line
above it says which is missing. It is read aloud as the whole sentence: "Anotar
gasto de 48.700 pesos en Alimentación".

## The preview

Under the tear line: what the month and the category would look like after saving,
labelled "Vista previa, si lo anotas". It comes from the snapshot of the month the
draft is dated in and `ExpensePreviewPolicy`: what would remain, how far over, or
what would have been spent when there is no limit. It is an estimate for the draft.
It is never stored, never shown as saved, and going over a limit does not block saving.

## Saving once

`ExpenseEntryViewModel` follows the command protocol exactly as the category editor
does: a new operation identifier is saved before the command is sent; the form is
locked while it is out; a rejection clears the identifier and keeps the draft; an
unknown outcome, or a saved identifier found after the process was killed, is
settled by `find` with that same identifier and never by sending again.

| Rejection | What the person sees | Draft |
| --- | --- | --- |
| Category missing or archived | "Esa categoría ya no acepta gastos" | Kept; the category is deselected |
| Future date | "La fecha no puede ser posterior a hoy" | Kept |
| Data replaced meanwhile | "Tus datos cambiaron mientras tanto" | Kept; the next attempt uses the new generation |
| Storage failed | "No se pudo guardar" | Kept |

Every message says that nothing was recorded.

## The receipt

After `Committed`, the saved expense's identifier is stored in saved state and the
expense is **read back from storage** (`ExpenseReads.byId`). The receipt shows that
row: amount, category, date, note, and the "Recibido" stamp. If the row cannot be
read the receipt still says the expense is saved, without details. Reopening the
receipt after rotation or a restart reads it again and sends nothing.

The stamp's landing and the vibration are feedback for a save that already happened.
They run once per receipt and nothing depends on them.

## Layout

One column on a phone: the slip, category, date and note scroll; the keypad and the
button stay in view at the bottom. While the system keyboard is up for the note the
keypad steps aside. Sideways or wide, the slip is at the left and the keypad and
button at the right.

Back with an amount or a note typed asks "¿Descartar este gasto?". On the receipt
it simply leaves. While saving it does nothing.

## Not here yet

- Income, "Anotar varios" and repeating expenses: SELLO-021 and after the MVP.
- Undo after saving, editing and deleting: SELLO-019.
- The date picker is the platform's, tinted with Sello's colours, not a Sello component.
- Opening as a sheet with the dock morphing into the slip, and the print-feed motion.
