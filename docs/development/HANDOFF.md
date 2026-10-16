# Sello development handoff

> **Informative only.** This file records delivery context and grants no permissions.
> Project permissions and working rules live in [AGENTS.md](../../AGENTS.md),
> subject to the user's current instructions.

**Snapshot:** 2026-10-16 · **Product baseline:** `df0aefa`.
SELLO-001–023 and epics E01–E05 are recorded Done in the canonical tickets.
The links below contain their full delivery evidence; this summary adds no new
verification or release-acceptance claims.

## Current app

- Offline, local-only COP expenses/income, with Recibo and Resumen as working tabs.
- Category creation/editing, archive/unarchive, monthly limits and past corrections.
- Expense/income history, editing, deletion and guarded six-second undo.
- Exact totals, budget remaining/allowance, Resumen charts, net flow and allocation.
- Shared month, restored drafts/navigation, appearance and category-order settings.
- Room schema 3 for finances; DataStore for nonfinancial preferences.
- Startup retry and confirmed start-over preserve an unreadable database privately.

The modules are `:app`, `:domain`, `:data`, `:design-system` and the separate
`:catalog`. The extra `:prototype` app renders v1.1 sample screens with no storage.
[Sello v2](../design/v2/README.md) is the approved design; its redesign is not yet
implemented. Current pace-based forecasts remain until reviewed replacement rules
are implemented; [ADR 0012](../decisions/0012-budget-guidance-contract.md) tracks
those definitions and open inputs.

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

## Remaining work

- SELLO-061–069 ([E12](../planning/mvp/epics/E12-v2-recibo.md)): the current focus.
  Brings Recibo to the v2 design: factual status, answer slip, categories card,
  month ribbon, dock and tabs, and brief save feedback.
- SELLO-024–027: sandbox sessions, seeded scenarios and recovery diagnostics.
  A controlled debug financial clock exists; the full sandbox tools do not.
- SELLO-028–032: backup, restore, reset and their privacy verification. Restore
  from startup failure and reset erasure of the kept unreadable copy remain open.
- SELLO-033–036: integrated accessibility, restoration, performance and signed
  release acceptance. The current app is not a completed MVP release.
- Goal/investment features, recurrence and other deferred capabilities remain
  outside MVP; signed carryover and legacy import are on the post-MVP roadmap.

The [board](../planning/mvp/BOARD.md) currently lists SELLO-024 and SELLO-028 without
unmet ticket dependencies, plus the E12 starting tickets (SELLO-061, 063, 065, 068
and 069); SELLO-033 now waits for E12. Their other prerequisites remain in
the ticket briefs; no statuses are changed by this handoff.

## Detail references

- [Current app inventory](current-app-experience.md) and [navigation graph](navigation-graph.md).
- [Architecture](../../ARCHITECTURE.md), [current decisions](../decisions/0001-mvp-contract.md) and [design guide](../design/README.md).
- [Storage](storage.md), [command recovery](commands.md) and [monthly snapshots](monthly-snapshot.md).
- [Setup](setup.md), [device-test notes](../planning/mvp/EXECUTION_GUIDE.md#device-test-notes) and [quality flow](../planning/mvp/QUALITY_FLOW.md).
- [Post-MVP roadmap](../planning/mvp/ROADMAP.md).
