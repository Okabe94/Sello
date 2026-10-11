# Recibo and category detail

**Owner:** SELLO-018 · Decisions: [D03, D06](../decisions/0001-mvp-contract.md) ·
Design: [Recibo](../design/sello-spec.html#s-recibo),
[Detalle de categoría](../design/sello-spec.html#s-categoria) · Builds on the
[monthly snapshot](monthly-snapshot.md), [Anotar](expense-entry.md) and the
[app shell](app-shell.md).

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

## Order of the categories

`CategoryOrderPolicy.mostUsed` (D06, "Más usadas"): most expenses in the selected
month first; equal ones by name ignoring case and accents, then by identifier; a
category without a finite limit that month always after those with one. Choosing
another order is SELLO-023.

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
  más" asks nothing meanwhile. Nothing is drawn for `refreshing` (owner decision,
  2026-10-11: the read takes milliseconds and an indicator would flicker).
- Kept rows are reused only for the same month, category and generation. If the read
  fails the list is `Failed`, not the old rows, and retry reads all of it again.
- The header and the rows are two reads. A save between them can leave them one
  revision apart until that save's own snapshot lists the rows again.
- "Anotar aquí" and the dock open [Anotar](expense-entry.md) with the category
  chosen. A finished month or an archived category does not offer "Anotar aquí".

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

`SelloScaffold` keeps both panes and the dock clear of a system bar or cutout at the
side of the screen, as it already did at the top and bottom.

## Not here yet

The weekly and six-month charts, "En números" and the comparison with last month;
editing or deleting an expense or a category (SELLO-019, SELLO-020); the gear and
other tabs; swiping between months; and the count-up and stamp-landing motion.
