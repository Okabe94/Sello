# Sello development handoff

> **Informative only.** This file records delivery context and grants no permissions.
> Project permissions and working rules live in [AGENTS.md](../../AGENTS.md),
> subject to the user's current instructions.

**Snapshot:** 2026-10-23 · **Product baseline:** `1159f8a`, plus the category-detail branch.
SELLO-001–035, SELLO-061–073 and epics E01–E07, E12 and E13 are recorded Done in the canonical tickets.
The links below contain their full delivery evidence; this summary adds no new
verification or release-acceptance claims.

## Current app

- Offline, local-only COP expenses/income, with Recibo and Resumen as working tabs.
- Category creation/editing, archive/unarchive, monthly limits and past corrections.
- Category detail follows the owner's hero → latest expenses → En números → weekly
  spending → historical average order, with real insights and a searchable expense
  browser. Its current-month chart point shares the hero's live limit; the average
  still uses completed months. See [the detail guide](recibo-and-detail.md#category-detail).
- Main and tools launchers now use the existing Marca artwork, including themed icons.
- Expense/income history, editing, deletion and guarded six-second undo.
- Exact totals, budget remaining/allowance, Resumen charts, net flow and allocation.
- Shared month, restored drafts/navigation, appearance and category-order settings.
- Room schema 3 for finances; DataStore for nonfinancial preferences.
- Backups save and restore through the system file picker, also from the start-up
  failure screen; retry and confirmed start-over preserve an unreadable database privately.
- Configuración → Borrar todos los datos erases the history after counts and BORRAR.
- No network permission or OS backup; every build checks its manifests, release
  libraries and APK against [MVP data handling](../privacy/mvp-data-handling.md).

The modules are `:app`, `:domain`, `:data`, `:design-system` and the separate
`:catalog`. The extra `:prototype` app renders v1.1 sample screens with no storage.
[Sello v2](../design/v2/README.md) is the approved design. Category detail retains
the v1.1 presentation with the owner's section order and the calculations in
[ADR 0015](../decisions/0015-category-detail-insights.md). Recibo's status is factual
under ADR 0012 BG1 (SELLO-061); [ADR 0012](../decisions/0012-budget-guidance-contract.md)
tracks the remaining financial inputs.

## Completed ticket outcomes

| Ticket | Delivered result |
| --- | --- |
| [SELLO-001](../planning/mvp/epics/E01-foundation.md#sello-001--approve-the-mvp-contract-and-financial-examples) | Approved MVP scope, financial examples and recovery choices. |
| [SELLO-002](../planning/mvp/epics/E01-foundation.md#sello-002--establish-and-verify-the-supported-android-toolchain) | Verified and pinned the Android build toolchain; documented setup. |
| [SELLO-003](../planning/mvp/epics/E01-foundation.md#sello-003--bootstrap-enforced-modules-and-variant-identities) | Created production modules and separate debug/catalog app identities. |
| [SELLO-004](../planning/mvp/epics/E01-foundation.md#sello-004--install-ci-quality-gates-and-architecture-enforcement) | Added the shared quality runner, architecture checks, CI and retained evidence. |
| [SELLO-005](../planning/mvp/epics/E01-foundation.md#sello-005--wire-production-composition-and-distinct-time-sources) | Added constructor wiring and separate financial, audit and elapsed-time clocks. |
| [SELLO-006](../planning/mvp/epics/E02-design-system.md#sello-006--implement-sello-tokens-and-the-independent-catalog-shell) | Added four themes, fonts, design tokens and the independent component catalog. |
| [SELLO-007](../planning/mvp/epics/E02-design-system.md#sello-007--deliver-paper-exact-money-and-feedback-components) | Added paper, exact money, stamps, loading/error and confirmation components. |
| [SELLO-008](../planning/mvp/epics/E02-design-system.md#sello-008--build-accessible-amount-first-inputs-and-entry-states) | Added the shared amount pad, fields, chips and validation states. |
| [SELLO-009](../planning/mvp/epics/E02-design-system.md#sello-009--build-category-adaptive-navigation-and-exact-chart-primitives) | Added category visuals, adaptive navigation and accessible chart components. |
| [SELLO-010](../planning/mvp/epics/E03-financial-core.md#sello-010--implement-exact-money-dates-and-command-validation) | Added exact money, checked totals, date/text validation and strict amount parsing. |
| [SELLO-011](../planning/mvp/epics/E03-financial-core.md#sello-011--create-room-v1-integrity-constraints-and-recovery-metadata) | Created the Room financial model, strict storage decoding and recovery metadata. |
| [SELLO-012](../planning/mvp/epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery) | Added expense saves with duplicate protection, durable receipts and outcome recovery. |
| [SELLO-013](../planning/mvp/epics/E03-financial-core.md#sello-013--implement-category-commands-and-historical-monthly-budget-storage) | Added category commands and dated budget defaults/overrides that preserve past limits. |
| [SELLO-014](../planning/mvp/epics/E03-financial-core.md#sello-014--calculate-consistent-recibo-and-resumen-budget-snapshots) | Added shared monthly totals, remaining budget, daily allowance and pace-based forecasts. |
| [SELLO-015](../planning/mvp/epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation) | Added typed navigation, a shared month and draft/navigation restoration. |
| [SELLO-016](../planning/mvp/epics/E04-first-slice.md#sello-016--deliver-first-run-category-creation-and-amount-entry-prerequisites) | Added first-run category creation, icon/limit selection and restored drafts. |
| [SELLO-017](../planning/mvp/epics/E04-first-slice.md#sello-017--ship-anotar-expense-and-its-durable-receipt-end-to-end) | Connected expense entry to real storage, saved receipts and recovery. |
| [SELLO-018](../planning/mvp/epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history) | Added real Recibo totals, category detail and paged expense history. |
| [SELLO-019](../planning/mvp/epics/E05-mvp-workflows.md#sello-019--implement-expense-edits-deletion-and-guarded-six-second-undo) | Added expense editing/deletion and guarded six-second undo, with a Room migration. |
| [SELLO-020](../planning/mvp/epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing) | Added category editing, archive/unarchive and current/past-month limit changes. |
| [SELLO-021](../planning/mvp/epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows) | Added income entry/history, editing/deletion and shared guarded undo, with a Room migration. |
| [SELLO-022](../planning/mvp/epics/E05-mvp-workflows.md#sello-022--deliver-basic-resumen-with-exact-actuals-and-history-charts) | Added Resumen income/spending, net flow, allocation and exact history charts. |
| [SELLO-023](../planning/mvp/epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings) | Added persisted appearance, category ordering/direction and working settings. |
| [SELLO-024](../planning/mvp/epics/E06-live-testing.md#sello-024--create-isolated-sandbox-sessions-and-financial-time-controls) | The debug app has a sandbox with its own data, opened from **Sello Herramientas**: a badge on every screen, and a financial date to step, jump or play forward while device, audit and elapsed time stay real. Switching restarts the app after writes finish; release builds are checked to contain none of it. A finger can't tap the status-bar badge. |
| [SELLO-025](../planning/mvp/epics/E06-live-testing.md#sello-025--implement-seeded-histories-replay-and-live-graph-growth-scenarios) | The sandbox replays versioned scenarios (G01, month and year ends, six months) through the real commands, step by step or playing, with derived operation IDs so a resumed step never writes twice; Recibo and Resumen grow live. |
| [SELLO-026](../planning/mvp/epics/E06-live-testing.md#sello-026--add-truthful-failure-receipt-and-recovery-diagnostics) | Sandbox-only simulated faults (failed read, lost answer or interruption after a real save, failure before storage), an operation inspector that reads real receipts, an edge-case scenario and a redacted diagnostic shared on request. |
| [SELLO-027](../planning/mvp/epics/E06-live-testing.md#sello-027--establish-the-growing-temporal-regression-scenario-suite) | Nine sandbox scenarios with checks taken from the approved examples (G01, H01–H05, I01–I02, AR01–AR02) or derived by hand and labelled; `TemporalScenarioCorpusTest` runs each fresh and resumed; [corpus](../testing/sandbox-scenarios.md) with replay steps and the recurrence obligation. E06 is done. |
| [SELLO-028](../planning/mvp/epics/E07-recovery-privacy.md#sello-028--define-portable-mvp-backups-and-bounded-validation) | A versioned plaintext JSON [backup format](../data/backup-format.md), independent of Room: one-revision snapshot, preferences captured separately (left out if unreadable), and a strict reader that rejects a whole bad file with where it is and writes nothing. No screen yet: saving is SELLO-029, restoring SELLO-030. |
| [SELLO-029](../planning/mvp/epics/E07-recovery-privacy.md#sello-029--deliver-real-document-picker-backup-creation) | Configuración → Respaldo saves a backup through the system file picker, after an always-visible plaintext disclosure; "Guardado" only after the document is closed, a failed save keeps the last backup, and a save cut off by a dead process shows as unknown. No restore yet (SELLO-030). |
| [SELLO-030](../planning/mvp/epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion) | Respaldo → Restaurar checks a whole file, previews current against backup counts and replaces the history in one transaction (new generation, backup zone), then applies its preferences, naming any that failed with a retry that never replaces twice; the app restarts. The start-up failure screen offers the same restore, setting the unreadable file aside first. |
| [SELLO-031](../planning/mvp/epics/E07-recovery-privacy.md#sello-031--deliver-fenced-two-confirmation-reset-and-recovery) | Configuración → Borrar todos los datos counts what goes, then needs the exact word BORRAR; one transaction erases every financial row, undo copy and receipt (keeping one `history.reset` receipt), the kept unreadable copy and staged restores go, the file is compacted and preferences return to first-run, then the app restarts empty. Backup files outside Sello are untouched. |
| [SELLO-032](../planning/mvp/epics/E07-recovery-privacy.md#sello-032--verify-privacy-file-boundaries-and-developer-data-isolation) | Build-time checks on the merged manifests (backup off, allowed permissions and exported parts only), the release libraries and the release APK; the picker adapters accept only provider documents; device tests for file boundaries and a log free of synthetic markers; [MVP data handling](../privacy/mvp-data-handling.md). E07 is done. |
| [SELLO-033](../planning/mvp/epics/E08-release.md#sello-033--execute-accessibility-adaptive-and-process-restoration-journeys) | Every daily-use screen checked in five windows, three text sizes and four themes, plus process-death, rotation and cold-start walks on an emulator. Large text now wraps titles, chips and rail labels; limit amounts are read with "pesos"; the note composer clears a side navigation bar. [UI acceptance matrix](../testing/ui-acceptance-matrix.md). |
| [SELLO-034](../planning/mvp/epics/E08-release.md#sello-034--prove-integrated-data-retention-and-recovery-failure-safety) | A v1 history survives upgrade, backup, restore, reset and restore again on real files; all nine sandbox scenarios round-trip through a backup with their last figures; after a reset nothing comes back in a new process; Respaldo's top-bar arrow restarts after a restore. [Recovery matrix](../testing/recovery-matrix.md). |
| [SELLO-035](../planning/mvp/epics/E08-release.md#sello-035--validate-performance-and-optimized-release-composition) | A synthetic year (10,000 expenses) meets the approved start, scroll, Resumen, backup and restore targets; the release build is shrunk by R8 and signed with the owner's key from their own Gradle properties; G3 now runs (ADR 0016). [Performance profile](../testing/performance-profile.md), [release](../release/README.md). Open for the owner: an in-app licence notice. |
| [SELLO-061](../planning/mvp/epics/E12-v2-recibo.md#sello-061--replace-the-pace-verdict-with-the-factual-budget-status) | Recibo and category detail judge recorded spending, not pace: Al día, Al límite, Excedido; past months Cumplido or Excedido. Resumen keeps its words. |
| [SELLO-062](../planning/mvp/epics/E12-v2-recibo.md#sello-062--rebuild-the-recibo-answer-slip-to-v2) | Recibo's answer slip reads "Restante del presupuesto" with a signed remainder, the per-day figure under a tear line and a closing line for past months. The Ajustar presupuesto link is SELLO-067. |
| [SELLO-063](../planning/mvp/epics/E12-v2-recibo.md#sello-063--update-the-recibo-categories-card-and-first-use-and-failed-states) | Recibo's categories card has a header **+**; first use and a failed read use the v2 copy and centred slips. |
| [SELLO-064](../planning/mvp/epics/E12-v2-recibo.md#sello-064--restyle-the-month-bar-and-add-the-past-month-ribbon) | A past month shows "Estás viendo septiembre" with "Volver a octubre" on every screen that shows the month, Configuración aside; at large text the button goes under the message. |
| [SELLO-065](../planning/mvp/epics/E12-v2-recibo.md#sello-065--use-the-full-width-anotar-dock-and-the-v2-tab-bar) | The dock is one full-width **+ Anotar** button (still read "Anotar un gasto") and the tab bar marks the selected tab with a pill; the rail is unchanged. `EntryDock` and `SelloNavigationBar` wait for SELLO-073. |
| [SELLO-066](../planning/mvp/epics/E12-v2-recibo.md#sello-066--replace-the-saved-receipt-with-brief-feedback-after-a-save) | A confirmed new expense or income closes the form and the screen it came from says "Gasto anotado: 48.700 en Comida" or "Ingreso anotado: …" for 4 seconds; the receipt and Listo are gone. |
| [SELLO-067](../planning/mvp/epics/E12-v2-recibo.md#sello-067--add-the-ajustar-presupuesto-action-to-an-overspent-recibo) | An Excedido month in progress offers **Ajustar presupuesto ›**, which brings the categories card to the top and focuses its heading. E12 is complete. |
| [SELLO-068](../planning/mvp/epics/E12-v2-recibo.md#sello-068--wire-the-v2-month-bar-and-past-month-ribbon-into-the-app-frame) | Recibo and Resumen use the v2 month bar; the frame has an empty ribbon slot for SELLO-064. Pushed screens keep their titled bar. |
| [SELLO-069](../planning/mvp/epics/E12-v2-recibo.md#sello-069--wire-the-confirmation-notice-into-the-shell) | The shell can show a confirmed save with the v2 check look above the dock; nothing sends one until SELLO-066. |
| [SELLO-070](../planning/mvp/epics/E13-v2-current-app.md#sello-070--bring-expense-entry-and-edit-to-v2) | Expense entry and edit use the v2 form: no category chosen at opening, chips ending in **Nueva**, a docked note composer, "Máximo 12 dígitos" and no live preview. Income entry is SELLO-071. |
| [SELLO-071](../planning/mvp/epics/E13-v2-current-app.md#sello-071--bring-income-entry-edit-and-history-to-v2) | Income entry uses the v2 form in green, with Otro's name in the composer; income history is "Ingresos de octubre" with the hero, split bar and list, and its dock records income. |
| [SELLO-072](../planning/mvp/epics/E13-v2-current-app.md#sello-072--bring-resumen-to-v2-for-todays-figures) | Resumen follows the v2 order: income receipt lines, month-by-month columns with the current month striped, categories ranked with a total, then the kept daily chart. No forecast or "Gasto real" stamp. |
| [SELLO-073](../planning/mvp/epics/E13-v2-current-app.md#sello-073--retire-the-v1-components-v2-replaced) | One component set: `AmountField` and the v1 dock and tab bar are deleted, the v2 components lost their `V2` prefix and package, and the catalog lists each component once in ten groups. The prototype uses `EntryAmountSlip`. |

## Remaining work

- SELLO-036: MVP acceptance, with signed installation on a physical device and the
  independent review D10 asks for. The current app is not a completed MVP release.
- SELLO-074 ([E14](../planning/mvp/epics/E14-assistive-technology.md)): the manual
  TalkBack, Switch Access and keyboard pass, moved post-MVP by the owner on
  2026-10-23. E13 is done without it.
- Goal/investment features, recurrence and other deferred capabilities remain
  outside MVP; signed carryover, legacy import and the assistive-technology pass
  are on the post-MVP roadmap.

The [board](../planning/mvp/BOARD.md) currently lists E08's SELLO-036 without unmet
ticket dependencies. Its other prerequisites remain in the ticket brief.

## Detail references

- [Current app inventory](current-app-experience.md) and [navigation graph](navigation-graph.md).
- [Architecture](../../ARCHITECTURE.md), [current decisions](../decisions/0001-mvp-contract.md) and [design guide](../design/README.md).
- [Storage](storage.md), [backup format](../data/backup-format.md), [command recovery](commands.md) and [monthly snapshots](monthly-snapshot.md).
- [Setup](setup.md), [device-test notes](../planning/mvp/EXECUTION_GUIDE.md#device-test-notes) and [quality flow](../planning/mvp/QUALITY_FLOW.md).
- [Post-MVP roadmap](../planning/mvp/ROADMAP.md).
