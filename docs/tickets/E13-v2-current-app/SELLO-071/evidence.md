# SELLO-071 — Bring income entry, edit and history to v2

- **Revision:** tested commit `b5fa2ff`, the ticket's commit merged with `main` at
  `7a055f0` (two-runner device CI, no overlap), with the ticket In Progress; the commit
  that marks it Done changes only its status and this evidence.
- **Decisions:** the owner chose, on 2026-10-20, to drop the income form's month
  preview as v2 and SELLO-070 did (`IncomePreviewPolicy` stays in the domain, unused by
  the form, which no longer takes `ResumenSnapshots`), and to keep income history on the
  shell's titled bar worded "Ingresos de octubre" rather than `V2TitleBar`, which has no
  back control or month picker; back, the picker and the SELLO-064 ribbon stay.
- **Requirement mapping:** `IncomeFormScreen` is rebuilt on `V2EntryLayout` in the gain
  tone: "Ingreso en pesos", the amount drawn with `+` (read as the amount), the date
  pill, source chips without icons, for Otro a "¿De dónde vino?" `EntryLine` whose name
  is written in the shared `NoteComposer` (24 code points, an empty name allowed and
  still reported as missing, kept when the composer closes), the note row and composer,
  **Anotar ingreso** in green and the compact pad. The composer (`IncomeComposer.Note`
  or `SourceName`) and its text live in `SavedStateHandle`; a 13th digit sets `atLimit`
  ("Máximo 12 dígitos"); what is missing or went wrong sits above the button, including
  "Escribe el nombre de la fuente." while Otro has no name. Saving, recovery, read-back,
  edit, delete, discard and past-month rules are unchanged. `IncomeHistoryViewModel`
  also observes `ResumenSnapshots` for the selected month and exposes `IncomeSplit`
  (income, allocated, signed unallocated, `over`, and the bar's solid share clamped to
  0–1), or null while read or when the read failed. `IncomeHistoryScreen` shows an
  `AnswerSlip` hero "Entró en octubre" with the total (the stamp is now optional in the
  shared component, tested), a tear line, a `SplitBar` (solid in budgets, striped
  unassigned, no negative segment) with its spoken split, **En presupuestos de
  categorías** and **Sin asignar** lines, and when unassigned is negative a red signed
  amount and "Tus presupuestos suman …; anotaste …" with no suggestion; without the
  summary only the total. Then **Lo que entró**: each income (source, date · note,
  amount) opening its edit, paging and page retry, and a total line. Empty is "Aún no
  anotas ingresos en octubre / Sello funciona sin ingresos…" with Anotar ingreso; a
  failed read says "No pudimos leer tus ingresos", never zero. `ShellScreen` gains
  `titleLabel` and `dock`; income history uses them for its title and **+ Anotar
  ingreso**, and every other screen keeps the expense dock. No expected income, plan
  box or suggestion is drawn.
- **Red / Green:** with the new actions and fields doing nothing, five
  `IncomeFormViewModelTest` cases (note composer, Otro's name kept on close, 25 code
  points refused, the composer restored after process death, the 12-digit message)
  failed; with `split` always null, four `IncomeHistoryViewModelTest` cases (the split,
  the negative case, nothing in and a failed summary, following the month) failed. All
  passed after the change. New device tests: `IncomeHistoryScreenTest` (8),
  `IncomeFormScreenTest` (4 more) and `ShellDockTest` (2); the income and Resumen
  journeys open income entry from the dock, and the income journey checks the hero and
  list after saving.
- **Gate results:** `./scripts/verify-ticket SELLO-071 --retain` passed all six checks.
  Host: 533 tests; device: 591 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. A first run on the working tree passed with the same
  counts before `main` moved; this run followed the merge.
- **Quality run:** run 20261021T043839Z-ae6bfada; SELLO-071 G2 passed; HEAD b5fa2ff, inputs sha256 3937592f3e58; report docs/tickets/E13-v2-current-app/SELLO-071/quality-report.json sha256 bb417b6d22e4bb2a67459a12d0682a8262834934d25204dd37d661343eeb6cd7
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  showed the empty "Ingresos de octubre" screen with its dock, the green form (+60.000),
  Otro with "Escribe el nombre de la fuente." above the off button, the name composer
  (4/24), and after saving "Ingreso anotado: 60.000" over the hero (60.000, all
  unassigned with no categories), the striped bar, both lines, the "Bono" row and the
  total. The negative case is covered by the screen and view model tests. No personal
  history or physical device was used; no manual TalkBack session or hosted CI run is
  claimed.
- **Review:** executor self-review covered the shared form rules, the composer's two
  uses, the summary as the split's only source, failed and empty states, and the dock
  only on this screen. Done is proposed by this ticket's pull request; its merge is the
  designated owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or
  hosted CI pass is claimed.

