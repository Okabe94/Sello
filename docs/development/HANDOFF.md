# Handoff — SELLO-019 onward

**Snapshot:** 2026-10-11, after SELLO-018 merged. Epics E01–E04 are Done.
This is a starting-state guide, not a second architecture or permission to bypass
ticket dependencies. Verify current HEAD, Git status, code and ticket status first;
where this file and the code disagree, the code and the canonical tickets win.

**Bug backlog (2026-10-11):** [SELLO-E11](../planning/mvp/epics/E11-bug-stabilization.md)
consolidates the UX reports, technical assessment and the two layout/motion defects
below, with severity order and original-ID mappings. All fixes remain Backlog;
[preserved source reports and evidence](../planning/bugs/README.md) are historical.

## Read before editing

1. Root [AGENTS.md](../../AGENTS.md) and [ARCHITECTURE.md](../../ARCHITECTURE.md).
2. [Board README and gates](../planning/mvp/README.md), the
   [execution guide](../planning/mvp/EXECUTION_GUIDE.md), the
   [quality flow](../planning/mvp/QUALITY_FLOW.md), the
   [current board](../planning/mvp/BOARD.md) and the whole brief of the ticket.
3. The [approved product contract](../decisions/0001-mvp-contract.md) (D01–D10), the
   [worked examples](../testing/mvp-financial-examples.md) and ADRs 0002–0008.
4. The guide for each area the ticket touches (below) and the Delivery evidence and
   "Choices made" of the tickets it depends on.
5. The [visual reference and its provenance](../design/README.md).

## What exists

Four modules plus a catalog app, with the dependency graph enforced by
`scripts/quality/architecture.py`:

| Module | Holds | Guide |
| --- | --- | --- |
| `:domain` (Kotlin/JVM) | exact money, dates, bounded text, identifiers, budget policies, the monthly snapshot, ports | [money, dates, text](money-dates-text.md), [categories and limits](categories-and-limits.md), [monthly snapshot](monthly-snapshot.md) |
| `:data` | Room schema version 1, strict mappers, the command protocol, reads | [storage](storage.md), [commands](commands.md) |
| `:design-system` | tokens, slips, money text, inputs, category cells, scaffold, charts | [layout and charts](layout-and-charts.md) |
| `:app` | composition, clocks, navigation, features | [time and composition](time-and-composition.md), [app shell](app-shell.md), [category editor](category-editor.md), [Anotar](expense-entry.md), [Recibo and detail](recibo-and-detail.md) |
| `:catalog` | every design-system component with real examples | — |

What a person can do today: start empty, create categories with or without a
limit, record an expense and see its stored receipt, see what is left in Recibo,
open a category to see its figures and its expenses, look at earlier months, and
find all of it again after the process is killed or the app restarted.

What does not exist: editing or deleting anything, undo, income, Resumen, settings,
the sandbox, backup, restore and reset. Schema version 1 has an `income` table and
no code that writes to it.

## What is next

Eligible now, by their dependencies: **SELLO-019** (edit, delete, guarded undo),
**SELLO-020** (category management, month limits) and **SELLO-024** (sandbox
sessions and time controls). 021 needs 019; 022 needs 021; 023 needs 020; 025 needs
022 and 024. Work is done one ticket at a time; running tickets in parallel was
considered and dropped.

Things earlier tickets left for these:

- **019** adds the first schema change (deletion and undo records), so it also owns
  the first real migration, its upgrade test and the exported schema. Tapping an
  expense in the category detail and swiping it are the entry points the reference
  gives. The snackbar with undo does not exist in the design system yet. After a
  save, Anotar shows a receipt with "Listo"; the reference's "Gasto anotado ·
  Deshacer" message was left for this ticket to decide.
- **020** owns the pencil in the category detail, archiving, changing limits, the
  long-press menu on a category, and removing a month's own limit. The editor is a
  full screen and only creates; the reference makes editors a side panel in wide
  windows. Only 14 of the reference's 28 icons are named and delivered.
- **021/022** add income, net flow and "por asignar" to the snapshot, and per-day
  series for charts; the snapshot covers spending and budgets only.
- **023** owns choosing the category order (only "Más usadas" exists), saving the
  ink and theme, and the gear.
- **024** replaces the debug clock (`ControlledFinancialClock`,
  `financialTimeOverride`) with real sandbox control. Device journeys cannot move
  the financial day today, which is why they assert verdicts only where the day does
  not matter.
- **SELLO-045 (E11):** a database that cannot be opened at start now shows a screen
  with retry and a typed-BORRAR "Empezar de cero" that keeps the unreadable file
  aside ([ADR 0008](../decisions/0008-unreadable-storage-start-over.md)). It leaves
  three things to E07: restore reachable from that screen (030), reset erasing the
  kept copy (031) and the kept copy among the private files verified (032).

## How the work is done

Decided by the project owner; none of this is waived by a later ticket.

- **Roles:** the agent implements, the owner reviews and approves. Explanations are
  in plain language, and choices the agent made are listed for the owner in the
  ticket's Execution progress.
- **One branch and one pull request per ticket** (`sello-0NN-<slug>`). The agent may
  branch, commit, push and open the pull request without asking. Merging waits for
  the owner's explicit approval and is a squash merge. `main` is protected: pull
  requests only, with a passing `quality` check.
- **Done is proposed in the pull request** ([quality flow](../planning/mvp/QUALITY_FLOW.md#done-is-proposed-merging-approves)):
  the ticket is already Done with full Delivery evidence when the pull request
  opens, and the merge is the approval. On the **last ticket of an epic the epic is
  marked Done in that same commit**, so no hosted run is spent on a status line.
- **Pull requests** use the repository template: the title
  `<Epic> - <Ticket> | <Ticket title>`, a link to the ticket, then Description,
  Solution, Things that changed and Evidence ending in "Not covered". Commits and
  pull requests carry no tool attribution lines.
- **Gate:** `./scripts/verify-ticket SELLO-0NN --gate G2 --retain` on a clean,
  committed tree with a booted API 30 emulator, then the printed `Quality run` line
  goes into the evidence. Any later change to fingerprinted inputs makes it stale.
  If another pull request merges first, merge `main` in and run the gate again.
- **After merging**, the next ticket may start while `main`'s run finishes; a
  failure there is reported and fixed before anything else merges.
- **Tests:** red→green where a defect or a rule exists first. Where tests are
  written with the code, the finished code is checked by mutation and the result
  reported honestly, survivors included.
- **Not allowed without a current, explicit go-ahead:** delegation to other agents;
  dependency, toolchain, permission or schema changes outside a ticket that owns
  them; asking for signing material or tokens.

## Decisions so far

### Product (owner-approved)

- D01–D10 and all worked examples are approved; do not reopen them. Exact integer
  COP; one 12-digit limit per transaction (ADR 0003); COP 50 flooring only for the
  daily allowance; a zero limit and no limit are different things; a month's limits
  never change because a later month's did; undo is guarded; backups are plain text
  and say so.
- Budgets renew each month with no carryover. Signed carryover is planned after the
  MVP (ADR 0002, E09) and has no dormant tables or code.
- Categories can be ordered by the latest expense **added** (ADR 0004); the default
  order is most used in the selected month, unlimited ones last (D06).
- Pesos are shown with no currency symbol; other currencies as code, space, figure
  (ADR 0007). This overrides the reference's peso sign everywhere, the dock included.
- Importing the old app's data is after the MVP (E10, SELLO-044) and goes through the
  validated restore path: as much as possible is brought over, and anything that
  breaks Sello's rules is skipped, not repaired. The data itself is to be obtained
  when that ticket starts.
- The MVP has Recibo and Resumen. Widgets, recurrence, goals, investments, FX and
  batch entry are out.

### Tooling (owner-approved)

- Pinned toolchain in [ADR 0005](../decisions/0005-toolchain.md): AGP 9.3.3, Gradle
  9.5.0, JDK 25, Kotlin 2.4.20, Compose BOM 2026.02.01, compile/target 37, minimum 30.
  A "newer version" lint notice is not an instruction to upgrade.
- ktlint 1.8.0, strict Gradle dependency locking (`scripts/update-dependency-locks`)
  instead of hosted dependency review, Detekt deferred
  ([ADR 0006](../decisions/0006-quality-tooling.md)).
- Koin **core** only, confined to `:app` composition; `koin-android` was rejected
  because it would raise several AndroidX versions. View models are built by one
  factory in `composition/PresentationModule.kt`.
- Room 2.8.4. Navigation Compose 2.9.8 (2.10 was rejected: it forces upgrades).
  Charts are small Compose drawings, not a library. Icons are individual vectors.
- The repository is public by the owner's choice: no personal financial data,
  credentials or signing material in source, tests, captures or reports.

### Rules the code now relies on

Later tickets build on these; changing one is a decision, not a refactor.

- **Money and text:** amounts are parsed strictly (no leading zeros, no inner
  spaces, ASCII digits); text is stored NFC; names are unique ignoring case, accents
  and width; notes are one line. Nothing is trimmed, cut or repaired silently.
- **Storage:** identifiers are lower-case UUID text. A damaged database file is kept
  and the open fails; nothing falls back to an empty database. Ranges are enforced
  by domain types on the way in and strict mappers on the way out; a row that
  breaks a rule is a typed failure, never a default. Each expense and income has a
  sequence that orders records within a day.
- **Limits:** a default limit from a month onwards plus a limit for one month, which
  wins; no row per month. Archiving pauses the default from the next month. A
  category and its limits share one version. A month's own limit can be set for the
  current or a past month, never a future one.
- **Commands:** generation check, replay, validation, write, in one transaction that
  always finishes once started. An error before the commit is `Rejected`; only a
  failed commit is `OutcomeUnknown`, and it is looked up, never blindly retried. A
  receipt stores a digest of the input, not the input. An operation identifier
  belongs to one kind of command.
- **Snapshot:** one place works out a month (`MonthlyBudgetPolicy`), with exact
  arithmetic in the app over paged rows; SQL adds nothing. Expenses dated after the
  financial day are left out and counted. A future month is refused. A failed
  refresh keeps the last good snapshot next to the failure.
- **Screens:** State / Action / ViewModel / Root / Screen. Drafts, the identifier of
  a save in flight and what is open live in `SavedStateHandle`; results are state,
  not one-off events. Routes carry identifiers and hints only, never a record.
  A screen formats what the snapshot says and never does its own sums; Recibo and
  the category detail share `ReciboRenderModel`. A failed read is its own state:
  never zero, never "empty".
- **Month:** one `MonthSession` for every screen. It follows the current month
  unless an earlier one was picked, and returns to the current month after 30
  minutes away, also across process death.
- **Layout:** the dock and tab bar sit under the content, not over it. No tab bar
  with one tab, and no control that opens nothing. Detail beside its list needs a
  two-column window at least 600dp wide. Amounts are read aloud in full, and a
  verdict is always a word, never a colour alone. Keypad keys are 48dp.

### Where the reference was not followed

Recorded so the next person does not "fix" them by accident; each is in the named
ticket's Execution progress.

- No peso sign (ADR 0007). Tabular figures on figures only (SELLO-007).
- After saving, a receipt screen with "Listo" instead of a snackbar (SELLO-017).
- Anotar and the category editor are full screens, not sheets or side panels; no
  print-feed, count-up or stamp-landing motion yet (SELLO-016, 017, 018).
- A new category starts as "Sin límite" (SELLO-016). Recibo has an "Agregar
  categoría" button (SELLO-016).
- A finished month over its limit is stamped "Sobre el límite"; the reference says
  that in its stamps table and "Pasado" in its Recibo text (SELLO-018).
- The category detail is header, spent, limit and a list paged 20 at a time on the
  same screen; no charts, comparison, "En números" or separate list screen with sort
  and search (SELLO-018).

## Known loose ends

- A window that is short **and** narrow (half a phone's screen) is classed as
  two-column by `selloWindowLayout`. Recibo's detail guards against it; Anotar's
  form splits there on purpose: SELLO-052 was closed without a shared width rule
  because one column is worse for the form in such a window
  ([layout guide](layout-and-charts.md#frame-and-navigation)).
- The reduced-motion setting is followed while the theme is on screen (SELLO-053):
  `systemReducedMotion()` listens for the system setting and stops when it leaves the
  composition. A value passed to `SelloTheme` still wins, which is what tests and the
  catalog use.
- Lint reports 29 warnings in `:app`, unchanged since SELLO-017. The number must not
  grow unexplained.
- Not done anywhere yet: a physical-device run by the owner since SELLO-006, a
  tablet image (wide layouts are tested in sized windows), Switch Access, an
  independent technical review. Every ticket since SELLO-002 is executor
  self-review plus the owner's merge.

## Running things

Environment and emulator provisioning are in [setup](setup.md). In short:

```bash
export ANDROID_HOME=/home/okabe94/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Use a dedicated synthetic AVD (API 30, port 5580); never wipe or reuse the owner's
device or emulator. Do not rely on anything in `/tmp`, another session's emulator or
helper scripts: they do not survive a restart, and the AVD may need recreating.

Lessons from the device tests so far:

- The app reads its database off the main thread. Wait for what you expect
  (`Waits.kt`) instead of reading the screen right after a step.
- After typing, press through semantics (`press()`), and hide the keyboard in
  `@After`; a keyboard left open takes the next test's focus.
- The database, and the month selection, live as long as the process and are shared
  by every test in a run. Reset both (`TestData.reset()`,
  `TestCommands.showCurrentMonth()`).
- Tests that need history use the app's own commands (`TestCommands`), never rows
  written around them, so the app hears of each change.
- In a window larger than the screen, assert that something exists and where it is
  laid out, not that it is displayed.
- Host tests cannot see Java APIs missing on Android 11 (`BigInteger.TWO` was one);
  only the device run can.
- A system dialog on the emulator (a crashed launcher after a display resize)
  makes every Back press fail. Check what holds focus before blaming the code.
- Returning to the app by hand must use the same launcher intent it was started
  with, or Android opens a second copy and restoration seems lost.

## Ownership

The owner handles physical-device testing and release building, signing and key
custody; the APK is private. Epic files are the canonical ticket source; board and
CSV views are generated (`python3 docs/planning/mvp/board.py`, then `--check`).
Preserve other people's work, including uncommitted files. This handoff grants no
Git or review waiver beyond what "How the work is done" states.
