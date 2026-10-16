# Recibo and category detail

Decisions: [D03, D06](../decisions/0001-mvp-contract.md) ·
Design: [Sello v2 Recibo](../design/v2/home.html); [Detalle de
categoría](../design/sello-spec.html#s-categoria) (v1.1; not redesigned in v2) ·
Builds on the [monthly snapshot](monthly-snapshot.md), [Anotar](expense-entry.md) and
the [app shell](app-shell.md).

The screens that answer "how much is left, and where did it go". Their rule: every
figure comes from one snapshot of the month, and no screen adds, subtracts or judges
anything itself.

## One snapshot, one mapper

`ReciboRenderModel` turns a `SnapshotState` into what both screens draw. It picks the
sentence, formats the amounts the snapshot already holds and names the verdict:

| Month | With limits | Without any limit |
| --- | --- | --- |
| In progress | "Te quedan" what remains, or "Te pasaste por" how far over; the verdict Al día, Justo or Pasado; "Para N días" ("Para hoy" on the last day) and what can be spent a day | "Has gastado" the total, "Sin límite", nothing per day |
| Finished | "Te sobraron" or "Te pasaste por"; Bajo el límite or Sobre el límite; nothing per day | the same as above |

A finished month uses the limits that applied in that month, because the snapshot
does. The amount in a sentence never carries a sign: the words say which way it
goes. A category's cell keeps the minus.

The verdict is always a word, on screen and in what a screen reader says, never a
colour alone. Each block is read as one sentence with its exact amounts: "Te quedan
937.200 pesos. Al día.", "Alimentación. Te quedan 287.600 pesos de 900.000 pesos.
Al día."

How full a category's circle is drawn is the only number that is not exact. It is
geometry: it is never shown as a figure, compared or turned back into money.

The approved [v2 Recibo](../design/v2/home.html) changes the stamps
([ADR 0012](../decisions/0012-budget-guidance-contract.md)), always shows the per-day
line in the current month, closes a past month with one line and adds a review
invitation; none of it is implemented yet.

## Order of the categories

`CategoryOrderPolicy` (D06). Configuración offers Más usadas (the default),
Alfabético, Monto gastado, Límite and Último gasto agregado, each from highest to lowest or
lowest to highest, using only the expenses of the month on screen. Whatever the choice,
categories with a finite limit that month come first, those with no expenses that
month come last within each group, and ties go by name ignoring case and accents,
then by identifier. If the setting cannot be read, the last good one is used, or Más
usadas.

## Category detail

The header is Recibo's rules for one category ("Te quedan aquí"), with what was
spent and the limit. A category without a limit shows what was spent and why there
is nothing to judge: Sin límite, En pausa or Sin configurar.

Its expenses are listed newest day first and, within a day, the one added last
first, through `ExpenseReads.inCategory`: a bounded page (20 on screen, at most 200
per read) and a cursor for the next. Rules:

- A page is only ever a list. The figures and the count ("13 gastos") come from the
  snapshot, so listing part never changes a total, and the list says "Se muestran 20
  de 45" with "Ver más" while there is more.
- No expenses and a list that could not be read are different states: "Sin gastos en
  octubre" with "Anotar aquí", or an error with retry. A later page that fails keeps
  the rows already listed and asks for that same page again.
- Each new snapshot lists again everything that was listed, in as few reads as
  `ExpenseReads.MAX_PAGE` allows, keeping the rows on screen meanwhile. A saved
  expense therefore appears by itself; nothing on screen is adjusted by hand.
- The rows kept on screen during that read are marked `Rows.refreshing`: they are of
  an earlier snapshot than the figures above them. They are replaced whole when the
  read finishes, so the list never drops to its first page and grows back, and "Ver
  más" asks nothing meanwhile. Nothing is drawn for `refreshing`: the read takes
  milliseconds and an indicator would flicker.
- Every listed row is drawn, on screen or not, inside one slip; there is no lazy list,
  because the slip is one surface and a lazy list would mean splitting it. Each row is keyed by its expense identifier, so a new expense at the top adds
  one row instead of redrawing all of them. Measured on the API 30 emulator, debug
  build, including about 80 ms of test overhead: one new expense at the top of 40 /
  200 / 500 rows took 100 / 250 / 400 ms before the key and 46 / 66 / 100 ms with it;
  drawing 200 / 500 / 1,000 rows from nothing takes 316 / 750 / 1,333 ms. A category
  opens on 20 rows and "Ver más" adds 20, so the long case needs many presses.
- Kept rows are reused only for the same month, category and generation. If the read
  fails the list is `Failed`, not the old rows, and retry reads all of it again.
- The header and the rows are two reads. A save between them can leave them one
  revision apart until that save's own snapshot lists the rows again.
- "Anotar aquí" and the dock open [Anotar](expense-entry.md) with the category
  chosen. A finished month or an archived category does not offer "Anotar aquí".

## Changing the category

"Editar categoría" in the detail's header opens
[the form on that category](category-editor.md#changing-an-existing-category) for the
month on screen. A long press on a category's cell in Recibo offers the same in a
menu, and a screen reader is offered it by name; pressing a cell still only opens it.

The detail of a month that is over always says that its limit can still be corrected
and that other months do not change. Until dismissed, it also shows the first-time
announcement "Puedes corregir el límite de este mes anterior.", alone or beside Recibo. An archived category stays listed, marked
"Archivada", in every month where it has a limit or expenses.

## Correcting and deleting an expense

Each listed expense is an `ActionLine`:

| How | What it does |
| --- | --- |
| Press | Opens [the form on that expense](expense-entry.md#correcting-a-recorded-expense) |
| Long press | A menu with "Editar" and "Borrar" |
| Swipe towards the start, past 40 % of the width | Deletes it |
| Screen reader | The actions "Editar" and "Borrar", by name |

Nothing depends on a gesture alone. A line under the count always says how: "Toca un
gasto para corregirlo. Para borrarlo, deslízalo o mantenlo presionado."

- **A deletion is of the row as listed.** The view model keeps each listed expense's
  version and the generation it was listed from, and `ExpenseCorrections.delete`
  sends exactly those. A row that is out of date is refused and said so above the
  rows ("Ese gasto cambió mientras tanto. No se borró; revísalo."); it is not deleted
  as something it no longer is.
- **Nothing is taken off the screen by hand.** The row leaves, and comes back after an
  undo, when the month is read again, like any other change. The figures move with it.
- **The offer to undo** is the frame's, not this screen's: see
  [the app shell](app-shell.md#what-is-said-about-a-correction).

## When a read fails

| What happened | Recibo | Detail |
| --- | --- | --- |
| Nothing could be read | error with retry, no figure, never the first-run slip | error with retry |
| A refresh failed after a good read | the last good figures under a notice that they may be out of date, with retry | the same |
| The category has nothing that month | (not listed) | "Nada aquí este mes" |

## One screen or two

`showsDetailBeside`: in a window laid out in two columns and at least 600dp wide,
the detail sits beside Recibo, the chosen category is marked as selected, and before
any is chosen the first one is shown. Anywhere else an opened category takes the
screen under its own title, Back returns to Recibo, and the dock records in that
category. A window that is short and narrow, such as half a phone's screen, shows
one at a time even though the layout rules call it two-column.

The title over a detail, and the detail under it, are always of the category that
was chosen (`navigation/DetailIdentity.kt`). The detail is asked for by identifier
and read a moment later, so meanwhile the name comes from Recibo's own list, which
is how the category was chosen in the first place; once the detail is read its own
name is used. A detail still holding the category shown before is treated as
loading. "Categoría" appears only when neither screen has read the category yet,
such as just after the app is restored. Nothing is cached and no name travels
through navigation.

`SelloScaffold` keeps both panes and the dock clear of a system bar or cutout at the
side of the screen, as it already did at the top and bottom.

## Not here yet

The comparison with last month, swiping between months, and the count-up and
stamp-landing motion. The charts are on
[Resumen](current-app-experience.md#4-resumen).
