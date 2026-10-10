# Sello post-MVP roadmap — Kanban board

Generated from `epics/*.md`; do not edit independently.

**Tickets Done:** 0/8 · **Epics Done:** 0/2

Backlog: 8 · Ready: 0 · In Progress: 0 · Review: 0 · Blocked: 0 · Done: 0

[Workflow, gates and definition of done](README.md)

## Epic outcomes

| Epic | Outcome | Status | Done |
| --- | --- | --- | --- |
| [SELLO-E09](epics/E09-signed-carryover.md) | Preserve surplus and overspending across enabled budget periods without confusing plans with account balances. | Backlog | 0/7 |
| [SELLO-E10](epics/E10-legacy-import.md) | Let someone bring their history from the previous app into Sello without weakening Sello's rules. | Backlog | 0/1 |

## Ready

No tickets.

## In Progress

No tickets.

## Review

No tickets.

## Blocked

No tickets.

## Backlog

| Ticket | Deliverable | Epic | Priority | Gate | Unmet dependencies |
| --- | --- | --- | --- | --- | --- |
| [SELLO-037](epics/E09-signed-carryover.md#sello-037--approve-signed-carryover-policy-and-independent-examples) | Approve signed carryover policy and independent examples | SELLO-E09 | P1 | G1 | [SELLO-036](epics/E08-release.md#sello-036--accept-the-complete-mvp-and-prepare-release-handoff) |
| [SELLO-038](epics/E09-signed-carryover.md#sello-038--implement-exact-signed-carryover-domain-calculations) | Implement exact signed carryover domain calculations | SELLO-E09 | P1 | G1 | [SELLO-037](epics/E09-signed-carryover.md#sello-037--approve-signed-carryover-policy-and-independent-examples), [SELLO-014](epics/E03-financial-core.md#sello-014--calculate-consistent-recibo-and-resumen-budget-snapshots) |
| [SELLO-039](epics/E09-signed-carryover.md#sello-039--persist-carryover-configuration-and-atomic-correction-replay) | Persist carryover configuration and atomic correction replay | SELLO-E09 | P1 | G2 | [SELLO-038](epics/E09-signed-carryover.md#sello-038--implement-exact-signed-carryover-domain-calculations), [SELLO-013](epics/E03-financial-core.md#sello-013--implement-category-commands-and-historical-monthly-budget-storage) |
| [SELLO-040](epics/E09-signed-carryover.md#sello-040--extend-portable-backup-restore-and-reset-for-carryover) | Extend portable backup, restore and reset for carryover | SELLO-E09 | P1 | G2 | [SELLO-039](epics/E09-signed-carryover.md#sello-039--persist-carryover-configuration-and-atomic-correction-replay), [SELLO-028](epics/E07-recovery-privacy.md#sello-028--define-portable-mvp-backups-and-bounded-validation), [SELLO-029](epics/E07-recovery-privacy.md#sello-029--deliver-real-document-picker-backup-creation), [SELLO-030](epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion), [SELLO-031](epics/E07-recovery-privacy.md#sello-031--deliver-fenced-two-confirmation-reset-and-recovery) |
| [SELLO-041](epics/E09-signed-carryover.md#sello-041--deliver-accessible-carryover-opt-in-and-availability-explanations) | Deliver accessible carryover opt-in and availability explanations | SELLO-E09 | P1 | G2 | [SELLO-040](epics/E09-signed-carryover.md#sello-040--extend-portable-backup-restore-and-reset-for-carryover), [SELLO-020](epics/E05-mvp-workflows.md#sello-020--complete-category-management-and-month-specific-budget-editing), [SELLO-022](epics/E05-mvp-workflows.md#sello-022--deliver-basic-resumen-with-exact-actuals-and-history-charts), [SELLO-023](epics/E05-mvp-workflows.md#sello-023--ship-persisted-appearance-ordering-and-essential-settings) |
| [SELLO-042](epics/E09-signed-carryover.md#sello-042--extend-live-sandbox-and-regression-corpus-for-signed-carryover) | Extend live sandbox and regression corpus for signed carryover | SELLO-E09 | P1 | G2 | [SELLO-041](epics/E09-signed-carryover.md#sello-041--deliver-accessible-carryover-opt-in-and-availability-explanations), [SELLO-024](epics/E06-live-testing.md#sello-024--create-isolated-sandbox-sessions-and-financial-time-controls), [SELLO-025](epics/E06-live-testing.md#sello-025--implement-seeded-histories-replay-and-live-graph-growth-scenarios), [SELLO-027](epics/E06-live-testing.md#sello-027--establish-the-growing-temporal-regression-scenario-suite) |
| [SELLO-043](epics/E09-signed-carryover.md#sello-043--verify-and-release-the-complete-signed-carryover-increment) | Verify and release the complete signed carryover increment | SELLO-E09 | P1 | G3 | [SELLO-042](epics/E09-signed-carryover.md#sello-042--extend-live-sandbox-and-regression-corpus-for-signed-carryover), [SELLO-033](epics/E08-release.md#sello-033--execute-accessibility-adaptive-and-process-restoration-journeys), [SELLO-034](epics/E08-release.md#sello-034--prove-integrated-data-retention-and-recovery-failure-safety), [SELLO-035](epics/E08-release.md#sello-035--validate-performance-and-optimized-release-composition), [SELLO-036](epics/E08-release.md#sello-036--accept-the-complete-mvp-and-prepare-release-handoff) |
| [SELLO-044](epics/E10-legacy-import.md#sello-044--import-legacy-data-through-the-validated-restore-path) | Import legacy data through the validated restore path | SELLO-E10 | P2 | G2 | [SELLO-028](epics/E07-recovery-privacy.md#sello-028--define-portable-mvp-backups-and-bounded-validation), [SELLO-030](epics/E07-recovery-privacy.md#sello-030--implement-staged-atomic-restore-and-resumable-preference-completion), [SELLO-036](epics/E08-release.md#sello-036--accept-the-complete-mvp-and-prepare-release-handoff) |

## Done

No tickets.

## Ready promotion candidates

None. Complete current Ready tickets first.
