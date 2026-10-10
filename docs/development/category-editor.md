# First run and the category editor

**Owner:** SELLO-016 · Decisions: [D04, D05](../decisions/0001-mvp-contract.md) ·
Design: [category editor](../design/sello-spec.html#s-catform) ·
Builds on [commands](commands.md), [categories and limits](categories-and-limits.md)
and the [app shell](app-shell.md).

## What a person sees

- A new installation opens on an empty Recibo with one action, "Crear categorías".
  There is no backup action until restore exists (SELLO-030).
- Once a category exists, Recibo lists each with its limit for the month and offers
  "Agregar categoría". SELLO-018 replaces that list with the real Recibo.
- The form (`feature/category`) asks for a name, an icon and a monthly limit.

## The form's rules

| Field | Rule |
| --- | --- |
| Name | Required; the button is off without one. Validated with `CategoryName`: up to 24 characters, no line breaks. A name already used, ignoring case and accents and including archived categories, is refused by the command and shown on the field |
| Icon | One of `CategoryIcons.options`. A new form starts on the first icon no category uses |
| Limit | "Sin límite" is on by default. Turned off, an amount must be typed: empty is never read as zero. `0` is a real zero budget. The amount goes through `CopAmountInput`; anything else is refused and left as typed |

The limit applies to the current financial month and is the default afterwards, as
`CreateCategory` defines. Text that fails a rule stays exactly as typed next to the
message; nothing is trimmed or corrected in the field. An error on the name scrolls
the name into view.

## Saving once

`CategoryEditorViewModel` follows the command protocol:

1. On "Crear categoría" it validates, takes a new operation identifier and **saves
   it** before sending the command.
2. While the command is out the form is busy and locked; a second tap does nothing.
3. `Committed` is the only thing that ends the form as created. A rejection clears
   the identifier, so the next attempt is a new operation.
4. `OutcomeUnknown`, or finding a saved identifier when the form comes back after
   the process was killed, asks `find` about that same identifier. A receipt means
   created; no receipt means it was not saved and the draft is ready again; if the
   answer cannot be read the form stays locked and offers "Comprobar si se guardó",
   which asks again and never creates.

The draft (name, icon, limit text, the switch), the identifier of a save in flight
and a created result are all in `SavedStateHandle`.

## Leaving

Back on an untouched form leaves. With a draft it asks "¿Descartar los cambios?".
While saving it does nothing.

## What the editor returns

The route is `CategoryEditorRoute(forEntry)`. When it ends with a category, the
new identifier is left for the screen that opened it under `CREATED_CATEGORY_RESULT`
in that screen's saved state: an identifier, never a category object. The opener
reads the category itself.

`forEntry = true` is for "an expense was asked for and there is no category that
can take one": the form then explains that a category comes first. The dock and an
entry link both lead here, and after the category is created the app goes on to
[Anotar](expense-entry.md) with it chosen.

## Icons

`CategoryIcons` maps the stored key to a drawing and a spoken name. Keys are stored
and exported, so a key is never renamed or reused. A key this version does not know
still shows a neutral icon. Fourteen icons ship; the reference shows twenty-eight
and names fourteen, so the rest need choosing.

## Not here yet

- Editing, archiving and changing a limit later: SELLO-020.
- The preview of the category cell and the "your month's total limit would be" line
  from the reference.
- Suggested limits from the person's own most used ones; the three fixed ones are shown.
- The form as a side panel on wide windows; it is a full screen everywhere.
