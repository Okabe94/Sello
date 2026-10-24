# Current app experience baseline

**Describes the app at:** `8ae3d17`.

This is the starting-point map for UI, UX, screen, component and navigation work. It
describes what the application does today; it is not a redesign proposal or a promise
that every current placement must remain. The code is authoritative if this document
drifts. Product and safety rules stay in [ARCHITECTURE.md](../../ARCHITECTURE.md).

The approved [Sello v2](../design/v2/README.md) redesign will replace much of what is
described here; it is not implemented yet.

## Product surface at a glance

Sello is a Spanish-first, offline, local-only Android app for COP expenses, income,
categories and monthly budgets.

- A healthy start opens **Recibo**. The two top-level destinations are **Recibo** and
  **Resumen**.
- The global expense action is **Anotar un gasto**: on ordinary phone layouts a
  full-width **+ Anotar** button (`EntryDock`, read as "Anotar un gasto") above the v2 tab
  bar, whose selected tab has a soft pill behind its icon; a `+` in the navigation rail
  where a rail is present.
- Recibo, Resumen, category detail and income history share one selected month.
- Expenses, income, categories and limits are stored in Room. Appearance, Recibo
  ordering, dismissed correction announcements and the last backup are stored in
  DataStore.
- The app uses the shared `:design-system`. The separate `:catalog` app renders those
  real components with synthetic data and no product repositories.

Not present in customer navigation: sandbox controls, goals,
investments, recurrence, alerts, analytical export or batch entry. There are no
placeholder destinations for them.

## How the interface starts

```text
SelloApplication
└─ Startup opens the financial store
   ├─ Failed ──> Startup failure screen
   │             ├─ Retry
   │             └─ Type BORRAR -> keep unreadable data aside -> fresh task
   └─ Ready ───> MainActivity
                 └─ AppearanceViewModel -> SelloTheme
                    └─ SelloAppRoot -> shell + navigation + feature screens
```

[SelloApplication](../../app/src/main/java/com/software/sello/composition/SelloApplication.kt)
opens storage before building the production graph, so no version of the main UI is
backed by missing or unreadable financial data.
[MainActivity](../../app/src/main/java/com/software/sello/MainActivity.kt) observes
appearance preferences, applies the light/dark and ink tokens, and hosts
[SelloAppRoot](../../app/src/main/java/com/software/sello/navigation/SelloApp.kt).

## Presentation and integration pattern

```text
Root composable -> ViewModel -> immutable State -> stateless Screen
                      ^                         |
                      └──────── Action ─────────┘
```

- A `…Root` owns lifecycle collection, Back handling, navigation callbacks and
  one-time optional feedback such as haptics.
- A `…Screen` renders state and reports typed actions. It does not navigate, access
  Room/DataStore or calculate financial truth.
- View models come from one factory in
  [PresentationModule.kt](../../app/src/main/java/com/software/sello/composition/PresentationModule.kt);
  Koin and concrete data implementations stay behind composition.
- Financial screen models receive already-computed snapshots and format them into
  exact render values. Read failure, loading and a genuinely empty result are
  separate states.
- Small restorable values live in `SavedStateHandle`: drafts, operation IDs, selected
  month, open category, chart selections and in-progress confirmations. Large
  records are read again by ID.

Moving content within a screen can usually stay in the screen composable. Changing
what a control means, what survives restart, or where a route leads crosses into
navigation or ViewModel behavior.

## Navigation and the shell

Every route, entry point and the composables each screen uses are in the
[navigation graph](navigation-graph.md). The shared month, Back order, month picker,
correction notice and `sello://anotar` link are defined in the [app shell](app-shell.md).

What matters for placement work:

- Recibo and Resumen get the two-destination navigation. Configuración, Ingresos and
  category detail opened from Resumen show a Back control and no tab bar or rail, but
  still use `ShellScreen`, with the month title, picker and expense dock. This is a
  current placement, not a requirement.
- Category, expense and income forms use `SelloScaffold` directly. They replace the
  shell, so they show no tabs, month picker, notice or dock.
- Tab switches save and restore tab state with `launchSingleTop`, so they build no
  duplicate top-level destinations.
- The month picker browses back to the year 2000; later months cannot be chosen.
- While an earlier month is selected, Recibo, Resumen, income history and category
  detail show "Estás viendo septiembre" under the month bar with a "Volver a octubre"
  button that selects the current month in one tap.
- The startup-failure screen is outside the graph because the graph could not be built.

## Current screens and behaviors

### 1. Startup failure

**Behavior:** retry stays on the screen if opening still fails. "Empezar de cero" is a
second-step warning that requires the exact trimmed word `BORRAR`; it keeps the
unreadable store aside, clears the old task and launches a fresh one. It never
replaces a failed read with an empty app. The warning and typed text survive
recreation. "Restaurar un respaldo" opens the system open dialog; the whole file is
checked and previewed with its date and counts only. Confirming sets the unreadable
store aside, fills a new one from the backup and shows "Restaurado" (or the pending
preferences); "Ir al recibo" restarts the app. If it fails after the set-aside, "La
restauración no terminó" leads to a restart. A preview does not survive process
death; the file is chosen again.

### 2. Recibo

**Entry:** default destination and first tab.

**Behavior:** the selected month drives the answer slip ("Restante del presupuesto"
with the status stamp and the signed remainder; in the current month, under a tear
line, what can be spent a day and the days left including today; in a past month,
"Mes cerrado · gastaste … de …"; with no finite limit, "Has gastado este mes") and
the category grid. When the month in progress is Excedido, **Ajustar presupuesto ›**
at the end of the slip brings the categories card to the top and focuses its heading. Categories can be ordered by most used,
alphabetically, amount spent, limit or latest expense added, in either direction;
the first-run default is most used, descending. A cell opens detail; long press and
accessibility actions offer edit. The top bar has the settings action and
"Agregar categoría" is below the grid. On wide two-pane layouts the selected category
is marked.

**States:** two skeleton slips while loading; a first-run empty slip with "Crear
categorías"; a failed slip with retry; or last-good content with a stale-data notice
and retry. A failed read is never shown as zero or as first run.

### 3. Category detail

**Entry:** a Recibo category, a category chart or list item in Resumen, or a restored
open Recibo category.

**Behavior:** the figures come from the same month snapshot as Recibo, with an
edit-category action and correction guidance. Expenses are newest first, 20 at a
time. Press edits; long press offers Edit/Borrar; swipe-to-start deletes. Deletion is
version/generation guarded and nothing is removed optimistically: the next snapshot
changes the screen. A current, non-archived category offers "Anotar aquí". A finished
month explains and can announce limit correction.

**States:** closed, loading, ready, category gone and failed. The expense list has its
own loading, empty, rows and failed states; a failed later page keeps earlier rows and
retries that page. A stale snapshot keeps last-good figures marked stale.

**Placement:** Recibo owns compact detail as shell state, not a route. At a two-pane
size at least 600dp wide, detail sits beside Recibo and defaults to the first
category. Resumen uses a separate routed detail.

### 4. Resumen

**Entry:** second tab.

**Behavior:** the v2 order. First **¿Cómo va este mes?** (for a finished month, "¿Cómo
cerró …?"): the difference with the month before over the same days, capped at the
shorter month and naming both ranges when they differ, or whole months for a finished
month, with its rate and both amounts as bars on one scale. With nothing recorded the
month before there is no comparison: what was spent stands alone, and in the first
month a line says another month is needed. Then **Ingresos** as receipt lines
(Ingresos anotados, Ingresos menos gastos, Sin asignar a categorías), the "Movimientos
anotados hasta el 24 de octubre" line and **Ver ingresos y su destino ›**; **Mes a mes**
with "Un mes normal" (the average of complete months with something recorded, half up)
over columns with past months in `pastBar`, the month in progress striped and marked
`*` (read "mes en curso, hasta hoy", with a note under the chart), values printed on
the highest and current columns and one dashed line for the month's limit; **¿Dónde se
va?** ranks categories by spending with share bars, exact amounts and a Total that is
the month's spending; **¿Qué días gastas más?** averages each weekday over the last six
months (this one included), or from the first recorded day when that is later, and
says which; it shows once a week has passed and names the busiest day only when one
stands out; and at most one
**sugerencia** for a category over its limit in each of the two complete months
before this one (the bigger limit wins), with **Ajustar el presupuesto de … ›** to
its editor and × to dismiss it for the month. The daily line chart is gone: the
comparison replaces it, as the owner decided for SELLO-072. Nothing is projected. The
income lines and the link open income history; each ranked category opens its detail.
An empty history offers "Anotar gasto". Selecting a chart mark shows its exact value;
the selection is saved for recreation and expires after three seconds. Chart geometry
may be approximate; selected and spoken amounts are exact.

**States:** loading skeleton, failed with retry, ready, stale ready with retry, and an
empty-data slip. With insufficient monthly history the screen says so instead of
inventing a comparison.

**Placement:** one scrolling column. In a two-pane layout at least 600dp wide, summary
and chart slips scroll in separate columns.

### 5. Income history

**Entry:** the income line or button in Resumen.

**Behavior:** the v2 **Ingresos de octubre** screen (SELLO-071), titled with the
shared month and keeping back, the month picker and the past-month ribbon. Its dock is
**+ Anotar ingreso**. The hero says "Entró en octubre" and the recorded total; under a
tear line a split bar and two lines, **En presupuestos de categorías** and **Sin
asignar**, come from the same summary Resumen shows. A negative unassigned amount is
signed and red, followed by "Tus presupuestos suman …; anotaste …", with no
suggestion; without that summary only the total is shown. Then **Lo que entró**: each
income (source, date · note, amount), tap to edit, and a total line. Rows load newest
first in pages of 50; "Ver más ingresos" requests the next page. A failed later page
keeps the rows already shown. No expected income, plan box or suggestion exists.

**States:** loading, failed with retry ("No pudimos leer tus ingresos", never zero),
or "Aún no anotas ingresos en octubre / Sello funciona sin ingresos…" with Anotar
ingreso, or ready with the hero, rows, pagination busy state and page-error retry.

### 6. Expense entry and edit

**Entry:** the dock or rail `+`, "Anotar aquí", Resumen's empty action, the device
link, or an expense row for edit.

**Behavior:** the v2 form (SELLO-070): amount slip with a date pill, category chips in
at most two rows ending with **Nueva**, a note row that opens a composer docked to the
keyboard, **Anotar gasto** fixed above the compact Sello pad. Amount, category, date
and note are a restorable draft. A new entry has an expense/income switch; no category
is chosen at opening unless the caller asked for one. Amount input is integer COP up
to 12 digits ("Máximo 12 dígitos" past that); future dates are unavailable. There is no
live preview. Saving is single-operation and
receipt-backed; an unknown outcome changes the action to "Comprobar si se guardó".
A confirmed save closes the form; the screen it was opened from shows "Gasto anotado:
48.700 en Comida" (or "Ingreso anotado: …") above the dock, worded from the record
read back from storage. Optional haptics run only after commit.

Edit reuses the form, requires a real change, has no mode switch, offers delete in the
top bar and closes after a confirmed update instead of showing a receipt. A changed
draft prompts before Back. Past-month edits name the affected months and can show a
dismissible correction announcement.

**Placement:** full screen outside the shell. In one column the draft scrolls while
keypad and action stay pinned. In any two-pane layout, draft and controls split into
independent columns. The system keyboard hides the custom keypad.

### 7. Income entry and edit

**Entry:** the switch on a new expense form, the **Anotar ingreso** dock in income
history, or an income row for edit.

**Behavior:** the v2 entry form in gain green (SELLO-071): "Ingreso en pesos" and a `+`
amount, source chips without icons (Salario, Freelance, Ingresos pasivos,
Transferencia, Otro), for **Otro** a "¿De dónde vino?" row whose name is written in the
same docked composer (24 characters, required only for Otro, kept when the composer
closes; "Escribe el nombre de la fuente." is said above the button while it is
missing), the note row and composer, **Anotar ingreso** and the compact pad. There is
no live preview of the month. Saving, recovery, read-back, edit/delete, discard
confirmation, past-month notice and adaptive split follow the expense form. Income
deletion uses the same six-second undo as expenses.

### 8. Category create and edit

**Entry:** Recibo's first-run or add action, the entry prerequisite when no active
category exists, the Recibo cell's secondary action, or category detail's edit action.

**Behavior:** create requires a name, a stable icon and either a valid limit or an
explicit "Sin límite"; the limit has fixed suggestion chips. A category created as an
entry prerequisite continues into expense entry with it selected. Edit changes name,
icon and the current or selected past-month limit. Archive/unarchive is confirmed and
deletes no history. Partial multi-command saves are reported honestly and uncertain
results are recovered by operation ID. Full rules: [category editor](category-editor.md).

**Placement:** always a full screen outside the shell, not a sheet or side panel.

### 9. Configuración

**Entry:** the gear on Recibo only.

**Behavior:** an appearance slip (theme switch, Cobalto / Violeta chips) and a Recibo
ordering slip (five order chips, ascending/descending switch). Choices persist
immediately. Theme and ink update the root theme without replacing the navigation
stack, selected month or drafts. Controls are disabled while a write runs. A read or
write failure shows an error slip with retry and says financial data is unchanged.

A "Tus datos" slip has the Respaldo row with the last backup's date, or "Nunca"; it
opens Respaldo. Below it, "Borrar todos los datos" (Pide confirmación dos veces)
opens Borrar datos.

**Placement:** a shell route, so it keeps the month title, picker and dock even though
its controls are not month settings. It has Back and no tab bar.

### 10. Respaldo

**Entry:** the Respaldo row in Configuración.

**Behavior:** the last backup (date, counts, size, file name) and, always above the
button, what a backup file is: readable by anyone, no password, the person's to keep,
synced by a cloud provider on its own terms, untouched by deleting Sello's data.
"Crear respaldo" opens the system save dialog; dismissing it changes nothing. Data is
read and written off the main thread and the save continues if the screen is left.
"Guardado" lands only after the document is written and closed. A failed save keeps
the last good backup and warns about a possibly incomplete file; a save the app never
finished (the process died) is reported as unknown, never as saved. Format:
[backup format](../data/backup-format.md).

**Restaurar**, below it: "Elegir archivo" opens the system open dialog; the whole file
is checked before anything is shown, and a file that cannot be restored is named by
kind (not a backup, newer version, damaged, too large, invalid content) with "Tus
datos no cambiaron". A valid file shows current and backup counts, the backup's
date and the replacement warning; nothing changes until "Reemplazar con el respaldo".
The replacement is one transaction with a new generation, so older drafts and undo
offers are refused. The backup's zone, appearance and order follow; if a preference
cannot be applied, D09's "Historial financiero restaurado" names only those, with a
retry that never replaces the history again. "Restaurado" → "Ir al recibo" (or Back,
or the top bar's arrow) restarts the app. A preview or result scrolls into view at the top; once the history
is replaced it is the only thing shown. The start-up failure screen offers the same
restore (see Startup failure).

### 11. Borrar datos

**Entry:** "Borrar todos los datos" in Configuración → Tus datos.

**Behavior:** first the counts read with the history's version ("Se borran 2
categorías, 3 gastos y 1 ingreso, con sus límites y notas"), that appearance and
order return to first-run, and that backup files outside Sello are not erased;
"Continuar" or "Cancelar". Then the exact word: "Borrar todo" is enabled only for
`BORRAR` (spaces around it allowed); "Cancelar" or Back returns to the counts. The
open step and the typed word survive recreation. If the history changed meanwhile
the counts are read again with "Tus datos cambiaron mientras confirmabas" and both
confirmations are needed again. An erase removes every financial row, undo copy and
receipt in one transaction, takes the device's zone and a new generation, deletes
the kept unreadable copy and staged restores, compacts the file and resets every
preference. "Borrado" → "Empezar de nuevo" (or Back) restarts into first-run Recibo.
A preference reset that fails is named with its first-run values and a retry that
erases nothing else; an unknown outcome is checked with the same operation.

**Placement:** a shell route without the dock, like Respaldo.

**Placement:** a shell route with Back and no Anotar dock: its own button is the only
action.

## Appearance and motion

- Default: system light/dark with Cobalto ink. Choices: Sistema / Claro / Oscuro and
  Cobalto / Violeta, applied at `MainActivity` above every screen.
- Schibsted Grotesk is the main family; Saira Stencil One is used for stamps.
- Semantic gain/loss/warn colors are independent of the selected ink.
- Reduced motion follows the Android animator-duration setting. Stamp landing,
  skeleton pulse, category stagger and counting stop or settle accordingly.
- COP is shown without a peso symbol. Exact full amounts stay in UI semantics;
  abbreviated values are chart-only labels.

## Component families used together

Reusable components live under
[`designsystem/component`](../../design-system/src/main/java/com/software/sello/designsystem/component).

| Family | Components normally composed together | Main consumers and coupling |
| --- | --- | --- |
| App frame | `SelloTheme` + `SelloScaffold` + `SelloNavigationBar`/`SelloNavigationRail` + `EntryDock` + `MonthBar` (tabs) or `MonthSwitcher` (pushed screens) + `MonthGrid` + `selloWindowLayout` | Every shell screen; frame changes affect navigation, keyboard/insets, month picking and responsive behavior together. |
| Paper and exact answers | `Slip` + `TearLine` + `LeaderLine`/`KeyValue`/`TotalLine` + `MoneyText` + `Stamp` | Recibo, Resumen, entry previews/receipts and detail. These establish the answer-first paper language. |
| Money entry | `EntryLayout` + `EntryAmountSlip` + `AmountKeypad` + `SelloButton`; `FormField` + `FieldMessage` + `DigitGrouping` for typed limits | Expense/income forms and category limit editing. Key positions, validation space and submit placement are intentionally coordinated. |
| Choices and forms | `ChipRows` (entry) or `ChipFlow` (settings, editor) + `SelloChip`; `SegmentedSwitch`/`SwitchRow`; `FormField`; `IconChoiceGrid` | Entry source/category choices, settings, category editor and note/name fields. |
| Categories | `CategoryCircle` + `CategoryCell` + `CategoryGrid` | Recibo grid and category detail. The app supplies exact amounts, spoken descriptions, selection and icons. |
| Correctable lists | `ActionLine` + `LineAction` + `SelloNotice` + `ConfirmSlip` | Expense rows, long-press menus, swipe deletion, global undo and discard/archive confirmation. No action relies on gesture alone. |
| Async feedback | `SlipSkeleton` + `EmptySlip` + `ErrorSlip` | Every read-driven screen. Loading, real empty, failed and stale remain distinct. |
| Charts | `LineChart`/`ColumnsChart` + `ChartPoint` + exact `MoneyText`/`LeaderLine` selection details | Resumen. Geometry is approximate display data; spoken and selected values are supplied separately and remain exact. |

Changing a primitive in one of these rows should be reviewed against every consumer
in that row, not just the first screen where the change is visible.

## Adaptive and accessibility baseline

`selloWindowLayout` responds to the window, not a device label; its size classes are
in [layout and charts](layout-and-charts.md). Screen-specific qualifications:

- Recibo/detail need at least 600dp before showing reading columns side by side.
- Expense and income forms use every two-pane layout, including short narrow windows,
  because the second pane is the keypad rather than another reading column.
- Resumen splits only when the layout is two-pane and at least 600dp wide.
- Nested shell routes omit the navigation model, so they gain no rail at expanded
  width; their dock stays below the content.
- Category grids use two columns, three when content width reaches 560dp, and one
  from font scale 1.3.
- At large text, screen titles, the month title and chips take a second line before
  any ellipsis; a title with no room beside its actions moves under them. The rail
  widens to its longest label.

The components already provide 48dp minimum targets, full spoken money, headings,
chart summaries and selectable marks, named secondary actions, non-gesture
alternatives, selected-state semantics, live error/notice announcements and layouts
exercised at font scales 1.0, 1.3 and 2.0. The assembled app was checked across
windows, text sizes, themes and process restoration in the
[UI acceptance matrix](../testing/ui-acceptance-matrix.md). Manual TalkBack, Switch
Access and keyboard walks are post-MVP (SELLO-074).

## Component catalog and previews

The separately installable `:catalog` app is the safest place to inspect or change a
shared component before integrating it. It lists each component once in 56 examples
grouped as foundations, paper, stamps, money, categories, inputs, navigation, charts,
feedback and goals ([catalog](catalog.md)). Its
controls switch ink, light/dark, font scale (1.0 / 1.3 / 2.0), window (device /
360dp compact / 760×400 landscape / 600dp medium / 840dp expanded) and full or
reduced motion.

The catalog uses real design-system components but no production screens or graph.
Debug-only Compose previews cover income and Resumen screen states in `:app`. There
is a debug-only sandbox with its own data and financial time controls, opened from the
**Sello Herramientas** launcher ([sandbox](sandbox.md)); seeded histories come later.

## Where to make different kinds of UI changes

| Intended change | Primary ownership today | What else to check |
| --- | --- | --- |
| Color, typography, spacing, shapes or motion | `design-system/theme` | All four theme combinations, reduced motion and catalog foundation examples. |
| Slip, button, input, category, notice or chart appearance | `design-system/component` | Its catalog example, all component consumers above and accessibility semantics. |
| Placement inside one feature | That feature's `…Screen.kt` | Compact/short/medium/expanded and large type; keep its State/Action contract if behavior is unchanged. |
| Global top bar, tabs, rail, dock, notices or panes | `SelloScaffold.kt`, `Navigation.kt`, `SelloApp.kt` | Every shell route, keyboard visibility, insets and Back ordering. |
| Destination, entry point or Back behavior | `Routes.kt`, `SelloApp.kt`, `ShellViewModel.kt` | Saved/restored stacks, process death, pending entry and shared month. |
| Loading, empty, failed, stale or busy behavior | Feature state/ViewModel plus screen | Do not collapse failed into empty/zero or lose last-good data. |
| User-facing wording | `app/src/main/res/values/strings.xml` | Screen-reader descriptions, plurals and tests that assert visible copy. |
| New reusable visual pattern | `:design-system` | Add/extend a real `:catalog` example; keep domain/data types out of its API. |

Behavior suites to keep green are listed in the graph's
[test coverage](navigation-graph.md#navigation-test-coverage), plus the screen tests
beside each feature.

## Current boundaries and intentional gaps

- Recibo and Resumen are the only top-level tabs; settings is reached only from
  Recibo's gear.
- Income history is reached through Resumen; a new-entry form can switch between
  expense and income, but the global dock is labelled and routed as expense.
- Category and transaction forms are full-screen flows, not sheets or side panels.
- The month and date pickers are Material 3 dialogs and sheets with Sello colors, not
  custom calendar components.
- The debug sandbox is a separate
  debug-only activity, not a route in the app.
- There is no production network permission, account, sync, notification or cloud UI.

Changing these may be desirable, but the change must be explicit because tests and
navigation currently encode them.

## Detailed guides

[App shell](app-shell.md), [category editor](category-editor.md),
[Anotar](expense-entry.md), [Recibo and detail](recibo-and-detail.md) and
[layout and charts](layout-and-charts.md) hold each area's rules in detail.
