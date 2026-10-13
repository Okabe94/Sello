# SELLO-021 income verification

Use only the isolated debug installation and synthetic records. No personal fixtures,
database dumps or real financial inputs belong in evidence.

## Entry and correction

1. With no categories, open Resumen → Entró → Ver ingresos → Anotar ingreso.
2. Record 150,000 COP from Freelance. The receipt must describe the stored income,
   and Resumen's selected-month total must be 150,000. Expense/budget rows stay unchanged.
3. Change its source to Salario: same ID, amount and original creation order; a new
   version and edit receipt, no creation celebration.
4. Delete and undo: exactly the original row returns. A newer expense/income deletion
   replaces the shared offer. Financial time must not advance the six real seconds.
5. Repeat creation while busy, switch Gasto / Ingreso with a draft, recreate the
   activity and reopen the app. No duplicate rows and no draft loss.

Automated paths: `IncomeFormViewModelTest`, `IncomeHistoryViewModelTest`,
`ResumenViewModelTest`, `ExpenseCorrectionsTest`, `IncomeJourneyTest`,
`IncomeFormScreenTest` and the real-Room `IncomeCommandContractTest`.

## Restart and temporal scenario

Activity recreation is not process-death proof. For a real saved-task check, enter
a 5,000 COP income draft, press Home, kill only the background debug process with
`adb -s <isolated emulator> shell am kill com.software.sello.debug`, confirm its PID
is gone and reopen the preserved task through Recents. The draft must return and
submit once. Cold start/force-stop are different checks.

Repeat with a committed deletion: a new process must not revive the interactive undo
offer. A committed compensation must remain restored, with its durable receipt.
U01–U05 are executed against real Room in `IncomeCommandContractTest`; controlled
financial time changes effective dates, never audit timestamps or monotonic deadlines.

The current debug infrastructure provides `ControlledFinancialClock`, not the sandbox
session UI or command-script runner. SELLO-024/025 own those. This recipe is the income
scenario to carry into that runner; no alternative formulas or unchecked seed writes
are introduced here. The catalog's `inputs.income-entry` renders real primitives with
synthetic states; it does not pretend to save money.

## Required final gate

Run focused tests first, then `./scripts/verify-ticket SELLO-021 --retain` with an
isolated API-30 device. The canonical ticket's Delivery evidence names the exact
tested snapshot, results and report. Preview configuration is not executed device
coverage, and automated database reopen tests are not a saved-task UI test.

## Executed saved-task check — 2026-10-13

On an isolated API-30 emulator with synthetic data, the final screens implementation
restored a 5,000 COP draft after Home → background process kill → Recents. The old
PID (2479) was absent before reopening; the restored task ran as PID 4683. Submission
produced exactly one income row for 5,000 COP and one `income.create` receipt, with
zero expenses. These counts were independently checked in the debug SQLite database.
The displayed receipt had no false details-read error. This is process-death evidence,
not just activity recreation. The integrated gate and tested revision are recorded
in the canonical ticket's Delivery evidence.

The first integrated run also exposed two obsolete single-tab assertions, updated
to verify the approved working tabs, and an unrelated design-system touch-target
assertion reading 47.999992 dp at the emulator's custom 240 dpi. The final gate uses
standard 320 dpi at 640×1280 pixels (320 dp wide). No design-system code or assertion
is weakened; the failed run remains available as diagnostic evidence.
