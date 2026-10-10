# Sello MVP — Kanban board

Generated from `epics/*.md`; do not edit independently.

**Tickets Done:** 11/36 · **Epics Done:** 2/8

Backlog: 24 · Ready: 0 · In Progress: 1 · Review: 0 · Blocked: 0 · Done: 11

[Workflow, gates and definition of done](README.md)

## Epic outcomes

| Epic | Outcome | Status | Done |
| --- | --- | --- | --- |
| [SELLO-E01](epics/E01-foundation.md) | Make decisions explicit and build/test the app reproducibly before financial work. | Done | 5/5 |
| [SELLO-E02](epics/E02-design-system.md) | Build the actual reusable visual language and a separately runnable UI laboratory. | Done | 4/4 |
| [SELLO-E03](epics/E03-financial-core.md) | Make amounts, budgets, dates and saved outcomes trustworthy before the UI relies on them. | In Progress | 2/5 |
| [SELLO-E04](epics/E04-first-slice.md) | Turn the greeting into a usable category → expense → receipt → Recibo journey. | Backlog | 0/4 |
| [SELLO-E05](epics/E05-mvp-workflows.md) | Complete maintainable expense/income history and an honest monthly summary. | Backlog | 0/5 |
| [SELLO-E06](epics/E06-live-testing.md) | Explore time-sensitive financial behavior safely through the actual app pipeline. | Backlog | 0/4 |
| [SELLO-E07](epics/E07-recovery-privacy.md) | Ship a local financial app whose data can be recovered and deliberately removed. | Backlog | 0/5 |
| [SELLO-E08](epics/E08-release.md) | Demonstrate a usable recoverable release, not merely assembled debug APKs. | Backlog | 0/4 |

## Ready

No tickets.

## In Progress

| Ticket | Deliverable | Epic | Priority | Gate | Unmet dependencies |
| --- | --- | --- | --- | --- | --- |
| [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery) | Save expenses with atomic receipts and uncertain-outcome recovery | SELLO-E03 | P0 | G2 | None |

## Review

No tickets.

## Blocked

No tickets.

## Backlog

| Ticket | Deliverable | Epic | Priority | Gate | Unmet dependencies |
| --- | --- | --- | --- | --- | --- |
| [SELLO-013](epics/E03-financial-core.md#sello-013--implement-category-commands-and-historical-monthly-budget-storage) | Implement category commands and historical monthly budget storage | SELLO-E03 | P0 | G2 | [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery) |
| [SELLO-014](epics/E03-financial-core.md#sello-014--calculate-consistent-recibo-and-resumen-budget-snapshots) | Calculate consistent Recibo and Resumen budget snapshots | SELLO-E03 | P0 | G2 | [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery), [SELLO-013](epics/E03-financial-core.md#sello-013--implement-category-commands-and-historical-monthly-budget-storage) |
| [SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation) | Introduce the app shell, month session and restored navigation | SELLO-E04 | P0 | G2 | None |
| [SELLO-016](epics/E04-first-slice.md#sello-016--deliver-first-run-category-creation-and-amount-entry-prerequisites) | Deliver first-run category creation and amount-entry prerequisites | SELLO-E04 | P0 | G2 | [SELLO-013](epics/E03-financial-core.md#sello-013--implement-category-commands-and-historical-monthly-budget-storage), [SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation) |
| [SELLO-017](epics/E04-first-slice.md#sello-017--ship-anotar-expense-and-its-durable-receipt-end-to-end) | Ship Anotar expense and its durable receipt end-to-end | SELLO-E04 | P0 | G2 | [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery), [SELLO-014](epics/E03-financial-core.md#sello-014--calculate-consistent-recibo-and-resumen-budget-snapshots), [SELLO-016](epics/E04-first-slice.md#sello-016--deliver-first-run-category-creation-and-amount-entry-prerequisites) |
| [SELLO-018](epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history) | Deliver Recibo, category detail and expense history | SELLO-E04 | P0 | G2 | [SELLO-014](epics/E03-financial-core.md#sello-014--calculate-consistent-recibo-and-resumen-budget-snapshots), [SELLO-017](epics/E04-first-slice.md#sello-017--ship-anotar-expense-and-its-durable-receipt-end-to-end) |
| [SELLO-019](epics/E05-mvp-workflows.md#sello-019--implement-expense-edits-deletion-and-guarded-six-second-undo) | Implement expense edits, deletion and guarded six-second undo | SELLO-E05 | P0 | G2 | [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery), [SELLO-017](epics/E04-first-slice.md#sello-017--ship-anotar-expense-and-its-durable-receipt-end-to-end), [SELLO-018](epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history) |
| [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing) | Complete category management and month-specific budget editing | SELLO-E05 | P0 | G2 | [SELLO-013](epics/E03-financial-core.md#sello-013--implement-category-commands-and-historical-monthly-budget-storage), [SELLO-018](epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history) |
| [SELLO-021](epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows) | Ship income recording, history and guarded correction workflows | SELLO-E05 | P0 | G2 | [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery), [SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation), [SELLO-019](epics/E05-mvp-workflows.md#sello-019--implement-expense-edits-deletion-and-guarded-six-second-undo) |
| [SELLO-022](epics/E05-mvp-workflows.md#sello-022--deliver-basic-resumen-with-exact-actuals-and-history-charts) | Deliver basic Resumen with exact actuals and history charts | SELLO-E05 | P0 | G2 | [SELLO-014](epics/E03-financial-core.md#sello-014--calculate-consistent-recibo-and-resumen-budget-snapshots), [SELLO-018](epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history), [SELLO-021](epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows) |
| [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings) | Ship persisted appearance, ordering and essential settings | SELLO-E05 | P1 | G2 | [SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation), [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing) |
| [SELLO-024](epics/E06-live-testing.md#sello-024--create-isolated-sandbox-sessions-and-financial-time-controls) | Create isolated sandbox sessions and financial time controls | SELLO-E06 | P0 | G2 | [SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation) |
| [SELLO-025](epics/E06-live-testing.md#sello-025--implement-seeded-histories-replay-and-live-graph-growth-scenarios) | Implement seeded histories, replay and live graph-growth scenarios | SELLO-E06 | P0 | G2 | [SELLO-017](epics/E04-first-slice.md#sello-017--ship-anotar-expense-and-its-durable-receipt-end-to-end), [SELLO-018](epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history), [SELLO-022](epics/E05-mvp-workflows.md#sello-022--deliver-basic-resumen-with-exact-actuals-and-history-charts), [SELLO-024](epics/E06-live-testing.md#sello-024--create-isolated-sandbox-sessions-and-financial-time-controls) |
| [SELLO-026](epics/E06-live-testing.md#sello-026--add-truthful-failure-receipt-and-recovery-diagnostics) | Add truthful failure, receipt and recovery diagnostics | SELLO-E06 | P1 | G2 | [SELLO-019](epics/E05-mvp-workflows.md#sello-019--implement-expense-edits-deletion-and-guarded-six-second-undo), [SELLO-025](epics/E06-live-testing.md#sello-025--implement-seeded-histories-replay-and-live-graph-growth-scenarios) |
| [SELLO-027](epics/E06-live-testing.md#sello-027--establish-the-growing-temporal-regression-scenario-suite) | Establish the growing temporal regression scenario suite | SELLO-E06 | P1 | G2 | [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing), [SELLO-021](epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows), [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings), [SELLO-026](epics/E06-live-testing.md#sello-026--add-truthful-failure-receipt-and-recovery-diagnostics) |
| [SELLO-028](epics/E07-recovery-privacy.md#sello-028--define-portable-mvp-backups-and-bounded-validation) | Define portable MVP backups and bounded validation | SELLO-E07 | P0 | G2 | [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing), [SELLO-021](epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows), [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings) |
| [SELLO-029](epics/E07-recovery-privacy.md#sello-029--deliver-real-document-picker-backup-creation) | Deliver real document-picker backup creation | SELLO-E07 | P0 | G2 | [SELLO-028](epics/E07-recovery-privacy.md#sello-028--define-portable-mvp-backups-and-bounded-validation), [SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation) |
| [SELLO-030](epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion) | Implement staged atomic restore and resumable preference completion | SELLO-E07 | P0 | G2 | [SELLO-028](epics/E07-recovery-privacy.md#sello-028--define-portable-mvp-backups-and-bounded-validation), [SELLO-029](epics/E07-recovery-privacy.md#sello-029--deliver-real-document-picker-backup-creation), [SELLO-012](epics/E03-financial-core.md#sello-012--save-expenses-with-atomic-receipts-and-uncertain-outcome-recovery), [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings) |
| [SELLO-031](epics/E07-recovery-privacy.md#sello-031--deliver-fenced-two-confirmation-reset-and-recovery) | Deliver fenced two-confirmation reset and recovery | SELLO-E07 | P0 | G2 | [SELLO-030](epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion), [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing), [SELLO-021](epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows) |
| [SELLO-032](epics/E07-recovery-privacy.md#sello-032--verify-privacy-file-boundaries-and-developer-data-isolation) | Verify privacy, file boundaries and developer-data isolation | SELLO-E07 | P0 | G2 | [SELLO-029](epics/E07-recovery-privacy.md#sello-029--deliver-real-document-picker-backup-creation), [SELLO-030](epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion), [SELLO-031](epics/E07-recovery-privacy.md#sello-031--deliver-fenced-two-confirmation-reset-and-recovery), [SELLO-024](epics/E06-live-testing.md#sello-024--create-isolated-sandbox-sessions-and-financial-time-controls) |
| [SELLO-033](epics/E08-release.md#sello-033--execute-accessibility-adaptive-and-process-restoration-journeys) | Execute accessibility, adaptive and process-restoration journeys | SELLO-E08 | P0 | G2 | [SELLO-018](epics/E04-first-slice.md#sello-018--deliver-recibo-category-detail-and-expense-history), [SELLO-019](epics/E05-mvp-workflows.md#sello-019--implement-expense-edits-deletion-and-guarded-six-second-undo), [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing), [SELLO-021](epics/E05-mvp-workflows.md#sello-021--ship-income-recording-history-and-guarded-correction-workflows), [SELLO-022](epics/E05-mvp-workflows.md#sello-022--deliver-basic-resumen-with-exact-actuals-and-history-charts), [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings) |
| [SELLO-034](epics/E08-release.md#sello-034--prove-integrated-data-retention-and-recovery-failure-safety) | Prove integrated data retention and recovery failure safety | SELLO-E08 | P0 | G2 | [SELLO-030](epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion), [SELLO-031](epics/E07-recovery-privacy.md#sello-031--deliver-fenced-two-confirmation-reset-and-recovery), [SELLO-032](epics/E07-recovery-privacy.md#sello-032--verify-privacy-file-boundaries-and-developer-data-isolation), [SELLO-027](epics/E06-live-testing.md#sello-027--establish-the-growing-temporal-regression-scenario-suite) |
| [SELLO-035](epics/E08-release.md#sello-035--validate-performance-and-optimized-release-composition) | Validate performance and optimized release composition | SELLO-E08 | P0 | G3 | [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings), [SELLO-027](epics/E06-live-testing.md#sello-027--establish-the-growing-temporal-regression-scenario-suite), [SELLO-032](epics/E07-recovery-privacy.md#sello-032--verify-privacy-file-boundaries-and-developer-data-isolation), [SELLO-033](epics/E08-release.md#sello-033--execute-accessibility-adaptive-and-process-restoration-journeys), [SELLO-034](epics/E08-release.md#sello-034--prove-integrated-data-retention-and-recovery-failure-safety) |
| [SELLO-036](epics/E08-release.md#sello-036--accept-the-complete-mvp-and-prepare-release-handoff) | Accept the complete MVP and prepare release handoff | SELLO-E08 | P0 | G3 | [SELLO-033](epics/E08-release.md#sello-033--execute-accessibility-adaptive-and-process-restoration-journeys), [SELLO-034](epics/E08-release.md#sello-034--prove-integrated-data-retention-and-recovery-failure-safety), [SELLO-035](epics/E08-release.md#sello-035--validate-performance-and-optimized-release-composition) |

## Done

| Ticket | Deliverable | Epic | Priority | Gate | Unmet dependencies |
| --- | --- | --- | --- | --- | --- |
| [SELLO-001](epics/E01-foundation.md#sello-001--approve-the-mvp-contract-and-financial-examples) | Approve the MVP contract and financial examples | SELLO-E01 | P0 | G0 | None |
| [SELLO-002](epics/E01-foundation.md#sello-002--establish-and-verify-the-supported-android-toolchain) | Establish and verify the supported Android toolchain | SELLO-E01 | P0 | G0 | None |
| [SELLO-003](epics/E01-foundation.md#sello-003--bootstrap-enforced-modules-and-variant-identities) | Bootstrap enforced modules and variant identities | SELLO-E01 | P0 | G1 | None |
| [SELLO-004](epics/E01-foundation.md#sello-004--install-ci-quality-gates-and-architecture-enforcement) | Install CI, quality gates and architecture enforcement | SELLO-E01 | P0 | G1 | None |
| [SELLO-005](epics/E01-foundation.md#sello-005--wire-production-composition-and-distinct-time-sources) | Wire production composition and distinct time sources | SELLO-E01 | P0 | G1 | None |
| [SELLO-006](epics/E02-design-system.md#sello-006--implement-sello-tokens-and-the-independent-catalog-shell) | Implement Sello tokens and the independent catalog shell | SELLO-E02 | P0 | G2 | None |
| [SELLO-007](epics/E02-design-system.md#sello-007--deliver-paper-exact-money-and-feedback-components) | Deliver paper, exact money and feedback components | SELLO-E02 | P0 | G2 | None |
| [SELLO-008](epics/E02-design-system.md#sello-008--build-accessible-amount-first-inputs-and-entry-states) | Build accessible amount-first inputs and entry states | SELLO-E02 | P0 | G2 | None |
| [SELLO-009](epics/E02-design-system.md#sello-009--build-category-adaptive-navigation-and-exact-chart-primitives) | Build category, adaptive navigation and exact-chart primitives | SELLO-E02 | P1 | G2 | None |
| [SELLO-010](epics/E03-financial-core.md#sello-010--implement-exact-money-dates-and-command-validation) | Implement exact money, dates and command validation | SELLO-E03 | P0 | G1 | None |
| [SELLO-011](epics/E03-financial-core.md#sello-011--create-room-v1-integrity-constraints-and-recovery-metadata) | Create Room v1, integrity constraints and recovery metadata | SELLO-E03 | P0 | G2 | None |

## Ready promotion candidates

[SELLO-015](epics/E04-first-slice.md#sello-015--introduce-the-app-shell-month-session-and-restored-navigation)
