# App shell, month session and navigation

**Owner:** SELLO-015 · Rules: [architecture](../../ARCHITECTURE.md) §§2, 6 ·
Design: [structure and global behaviour](../design/sello-spec.html).

The frame every screen sits in, and the state that belongs to the whole app instead
of to one screen. All of it is in `:app`, package `navigation`.

## What is on screen today

`SelloAppRoot` hosts one destination, Recibo. The title is the selected month and
opens the month picker. There is no tab bar, because one tab is not a choice, and no
"Anotar" dock, because the entry form does not exist yet. A tab or the dock is added
when what it opens works: add the tab to `ShellTab` and its route to `Routes.kt`
(SELLO-022 for Resumen), and give the scaffold a dock when SELLO-017 lands.

Recibo shows the shared [monthly snapshot](monthly-snapshot.md): a skeleton while
loading, an explanation when there are no categories, the exact total spent when
there are, or an error with retry. SELLO-018 replaces that minimal content.

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

## Routes and links

- Routes are `@Serializable` objects in `Routes.kt` and carry identifiers only. A
  route never carries an amount, a record or a result.
- `EntryLinks` understands `sello://anotar?categoria=<identifier>&monto=<amount>`.
  A link is outside input: anything not exactly that shape is ignored, and each value
  must pass the same rules as typed input or it is dropped, never repaired.
- A link only becomes `ShellState.pendingEntry`, a request to prefill a draft. It
  cannot save anything: the shell has no access to a command. The entry form
  (SELLO-017) takes the request, checks that the category exists and still accepts
  entries, and reports `EntryTaken`.
- The manifest does not declare the link yet. Declaring it now would let other apps
  open Sello on a link that visibly does nothing. SELLO-017 adds the declaration
  together with the form.

## View models

`composition/PresentationModule.kt` builds every view model in one factory. Screens
ask for a view model by type through that factory and never touch the dependency
graph; Koin stays in `composition`. A root composable (`…Root`) owns the view model
and lifecycle effects; the screen composable below it takes state and callbacks only.
