# First run and the category editor

**Owner:** SELLO-016 · Decisions: [D04, D05](../decisions/0001-mvp-contract.md) ·
Design: [category editor](../design/sello-spec.html#s-catform) (v1.1; not redesigned in [Sello v2](../design/v2/README.md)) ·
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
| Name | Required; the button is off without one. Validated with `CategoryName`: up to 24 characters, no line breaks. A name already used, ignoring case and accents and including archived categories, is said on the field **while it is typed** and the button stays off until it changes (owner decision, SELLO-020): the form knows the existing names from the read it opens with. The command refuses it as well, for a name taken meanwhile |
| Icon | One of `CategoryIcons.options`. A new form starts on the first icon no category uses |
| Limit | A new category asks for an amount: "Sin límite" is off and the amount is empty (owner decision, SELLO-058). Empty is never read as zero or as no limit, so creating without an amount is refused with "Escribe el límite o activa Sin límite." `0` is a real zero budget. No limit is only ever chosen: with "Sin límite" on, the amount field and the suggestions are not shown. The amount typed is kept while it is on, is not sent, and is back when it is turned off. The amount goes through `CopAmountInput`; anything else is refused and left as typed |

The limit applies to the current financial month and is the default afterwards, as
`CreateCategory` defines. Text that fails a rule stays exactly as typed next to the
message; nothing is trimmed or corrected in the field.

The limit is **drawn** in groups (`500000` as `500.000`) through `DigitGrouping`, so
its size can be read while typing and a suggestion reads the way its chip does. The
draft, what is validated and what is saved stay the typed text, without the dots. An error on the name scrolls
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

## Changing an existing category

The same form (SELLO-020), opened by `CategoryEditRoute(categoryId, month)` from
"Editar categoría" on a category's detail, or from a long press or screen-reader
action on its cell in Recibo. `CategoryManagementViewModel` drives it; the form for a
new category is untouched by it. It is titled "Editar categoría" and its button is
"Guardar cambios", off until something differs from what is stored.

**Which month's limit.** The form shows and changes the limit of the month it was
opened from, and says so under the field:

| Opened from | The limit typed | Command |
| --- | --- | --- |
| The current month | "Vale desde Octubre 2026 en adelante. Los meses anteriores no cambian." | `SetDefaultLimit` |
| A finished month | "Cambias solo el límite de Septiembre 2026. Los demás meses y el límite habitual no cambian." | `SetMonthLimit` of that month |

A month that has a limit cannot be left without one; a month that never had one can
stay so. "Sin límite" is still only ever chosen.

**One save, up to three commands.** Name, icon and limit are separate commands, each
with its own identifier and receipt, sent in that order, each against the version the
one before left. If one fails, those before it are saved and stay saved: the form
says "Se guardó: nombre. Lo demás no se guardó; inténtalo de nuevo.", keeps the rest
of the draft, and the next attempt sends only what still differs. Nothing is rolled
back and nothing is reported saved without its receipt.

**It is read once.** What was read, with its version, is in saved state beside the
draft, so a restored draft is compared with and sent against the category as it was
when the form opened. After an outcome that was not known, or a save interrupted by
the process dying, `find` is asked about that same operation and the category is read
again: what the draft still has that storage does not is what remains to be saved.

| What happened | What the person sees | Then |
| --- | --- | --- |
| The category changed meanwhile | "Esta categoría cambió mientras la editabas." | "Volver a cargar" starts again from what it is now; "Conservar mis cambios" reads it again and keeps the draft, to save over the new version on purpose |
| It no longer exists | "Esta categoría ya no existe. No se cambió nada." | Nothing can be sent |
| The name is taken | On the name, while typing | The button is off |
| Storage failed | "No se pudo guardar. La categoría quedó como estaba…", or which parts were saved | The draft is kept |

**Archiving.** "Archivar" in the top bar asks first, with what it does: new expenses
stop, this month keeps its limit, from next month there is no budget until it is
unarchived, and how many expenses of this month stay, with those of earlier months.
The count comes from the month's snapshot; if it cannot be read no number is given.
Unsaved changes are said to be discarded. An archived category shows "Archivada",
keeps its name and icon editable, has no limit to change from the current month on,
and offers "Desarchivar", which says whether its last limit comes back (archived in an
earlier month) or nothing else changes (the same month). Nothing is deleted either
way, so the confirmation is not drawn as destructive. There is no way to delete a
category.

After a confirmed change the form closes and the frame says "Categoría actualizada",
"Límite de septiembre actualizado. Sus cifras ya lo usan.", "Categoría archivada" or
"Categoría desarchivada".

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

- The dismissible first-time announcement that a past month's limit can be corrected:
  SELLO-023. The line that says so on a finished month is always there.
- Deleting a category: there is none in the MVP (D07).
- The preview of the category cell and the "your month's total limit would be" line
  from the reference.
- Suggested limits from the person's own most used ones; the three fixed ones are shown.
- The form as a side panel on wide windows; it is a full screen everywhere.
