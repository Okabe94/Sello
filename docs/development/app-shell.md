# App shell, month session and navigation

**Owner:** SELLO-015 · Rules: [architecture](../../ARCHITECTURE.md) §§2, 6 ·
Design: [structure and global behaviour](../design/sello-spec.html).

The frame every screen sits in, and the state that belongs to the whole app instead
of to one screen. All of it is in `:app`, package `navigation`.

## What is on screen today

`SelloAppRoot` hosts Recibo, the [category editor](category-editor.md) and
[Anotar](expense-entry.md). On Recibo the title is the selected month and opens the
month picker, and the dock records an expense. There is no tab bar, because one tab
is not a choice: a tab is added when its screen works, by adding it to `ShellTab`
and its route to `Routes.kt` (SELLO-022 for Resumen).

Recibo and the detail of one of its categories are described in
[Recibo and category detail](recibo-and-detail.md). Which category is open is the
frame's own saved state (`ShellState.detailCategoryId`), next to the month and the
picker, so it survives rotation and the process being killed.

## The selected month

`MonthSession` holds the one month Recibo, Resumen and category detail all show.

- It follows the current financial month, or stays on an earlier month the person
  picked. A later month can never be selected.
- After **more than 30 minutes** in the background it returns to the current month.
  Exactly 30 minutes keeps the selection. Nothing else is reset: drafts and a
  pending entry request are untouched.
- Two clocks with separate jobs: `FinancialClock` says which month is current;
  `MonotonicClock` measures time in the background. Moving financial time, real or
  simulated, is not time away.
- "Background" is the activity being stopped, reported by the root, so a dialog or
  the keyboard over the app does not count.

A screen's view model takes `MonthSession` and observes `selectedMonth`. It does not
keep its own month.

## What survives rotation and the process being killed

`ShellViewModel` saves small values in its `SavedStateHandle`: the picked month, the
moment the app left the foreground, whether the month picker is open and on which
year, and a pending entry request as an identifier and a number. On a new process
these are read back through the same validation as any outside input. The 30-minute
rule is applied with the saved moment, so being killed in the background neither
loses the selection nor extends it.

Rules for every screen: save identifiers and small drafts, never records; rebuild
anything large from storage; an operation identifier is saved before its command is sent.

## Back

`backTarget` is the single order: an open sheet or side panel closes first, then a
detail screen, then any other tab returns to Recibo, and Back on Recibo leaves the app.

## Windows

`ShellScreen` uses the design system's `SelloScaffold`. The month picker is a bottom
sheet, opened whole and scrollable so a large font or a short window cannot cut
months off; in a window wide enough for a side panel it is a panel beside the content.
The scaffold keeps content clear of the system bars and hides the dock and tab bar
while the system keyboard is open.

At the right of the picker's title, "Mes actual" goes to the month the financial day
is in from whatever year is being browsed (SELLO-059). It is always there, sends the
same `PickMonth` as that month's cell with the state's own current month, and so
follows a month that turns while the picker is open. A screen reader is told the
destination, "Ir al mes actual: Octubre 2026". With a large font it goes under the
title instead of being cut.

## What is said about a correction

`ExpenseCorrections` (`presentation/corrections`) is one object for the whole process.
It deletes an expense, undoes that, and holds the one notice the frame shows over the
bottom of the content, above the dock:

| Notice | Shown | Action |
| --- | --- | --- |
| "Gasto borrado" | Until the time to undo is over | "Deshacer" |
| "No se pudo deshacer. Inténtalo de nuevo." | The same, after an undo that failed | "Deshacer", the same operation again |
| "Gasto restaurado", "Gasto actualizado" | Four seconds | none |
| "Ya no se puede deshacer…", "No se pudo deshacer porque tus datos cambiaron…" | Four seconds | none |

- **The six seconds are not the screen's.** They belong to the
  [commands](commands.md#the-six-seconds). This object only stops showing the offer
  when the same monotonic clock says the time is over, and checks again when the app
  comes back into view, because waiting does not advance while a device sleeps.
- **Drawing again changes nothing.** Rotation, a resize or another screen in between
  show the same notice with the same time left. A process that ends has no notice.
- **A deletion runs to its end** even if the screen that asked for it is gone, and is
  then on offer all the same.
- **Read aloud** as "Gasto borrado. Puedes deshacerlo durante 6 segundos.", and
  announced when it appears.
- It is shown on Recibo and a category's detail. The entry form does not show it;
  leaving the form does.

## Routes and links

- Routes are `@Serializable` objects in `Routes.kt` and carry identifiers only. A
  route never carries an amount, a record or a result.
- `EntryLinks` understands `sello://anotar?categoria=<identifier>&monto=<amount>`.
  A link is outside input: anything not exactly that shape is ignored, and each value
  must pass the same rules as typed input or it is dropped, never repaired.
- The dock and a link both become `ShellState.pendingEntry`, a request to open the
  entry form. The shell cannot save anything: it has no access to a command. It
  checks whether a category can take an expense and sets `entryReady` or
  `entryNeedsCategory`; the root navigates and reports `EntryTaken`. The form checks
  the prefilled category and amount itself.
- The manifest declares the link for apps on the device. It is not `BROWSABLE`, so
  a web page cannot open it.

## View models

`composition/PresentationModule.kt` builds every view model in one factory. Screens
ask for a view model by type through that factory and never touch the dependency
graph; Koin stays in `composition`. A root composable (`…Root`) owns the view model
and lifecycle effects; the screen composable below it takes state and callbacks only.
