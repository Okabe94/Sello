# Sello post-MVP roadmap — Kanban board

Generated from `epics/*.md`; do not edit independently.

**Tickets Done:** 0/13 · **Epics Done:** 0/4

Backlog: 13 · Ready: 0 · In Progress: 0 · Review: 0 · Blocked: 0 · Done: 0

[Workflow, gates and definition of done](README.md)

## Epic outcomes

| Epic | Outcome | Status | Done |
| --- | --- | --- | --- |
| [SELLO-E09](epics/E09-signed-carryover.md) | Preserve surplus and overspending across enabled budget periods without confusing plans with account balances. | Backlog | 0/7 |
| [SELLO-E10](epics/E10-legacy-import.md) | Let someone bring their history from the previous app into Sello without weakening Sello's rules. | Backlog | 0/1 |
| [SELLO-E14](epics/E14-assistive-technology.md) | Confirm that a person using TalkBack, Switch Access or a hardware keyboard can use the whole released app. | Backlog | 0/1 |
| [SELLO-E15](epics/E15-release-fixes.md) | Fix the defects found while measuring and accepting the MVP, each with a test. | Backlog | 0/4 |

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
| [SELLO-037](epics/E09-signed-carryover.md#sello-037--approve-signed-carryover-policy-and-independent-examples) | Approve signed carryover policy and independent examples | SELLO-E09 | P1 | G1 | None |
| [SELLO-038](epics/E09-signed-carryover.md#sello-038--implement-exact-signed-carryover-domain-calculations) | Implement exact signed carryover domain calculations | SELLO-E09 | P1 | G1 | [SELLO-037](epics/E09-signed-carryover.md#sello-037--approve-signed-carryover-policy-and-independent-examples) |
| [SELLO-039](epics/E09-signed-carryover.md#sello-039--persist-carryover-configuration-and-atomic-correction-replay) | Persist carryover configuration and atomic correction replay | SELLO-E09 | P1 | G2 | [SELLO-038](epics/E09-signed-carryover.md#sello-038--implement-exact-signed-carryover-domain-calculations) |
| [SELLO-040](epics/E09-signed-carryover.md#sello-040--extend-portable-backup-restore-and-reset-for-carryover) | Extend portable backup, restore and reset for carryover | SELLO-E09 | P1 | G2 | [SELLO-039](epics/E09-signed-carryover.md#sello-039--persist-carryover-configuration-and-atomic-correction-replay) |
| [SELLO-041](epics/E09-signed-carryover.md#sello-041--deliver-accessible-carryover-opt-in-and-availability-explanations) | Deliver accessible carryover opt-in and availability explanations | SELLO-E09 | P1 | G2 | [SELLO-040](epics/E09-signed-carryover.md#sello-040--extend-portable-backup-restore-and-reset-for-carryover) |
| [SELLO-042](epics/E09-signed-carryover.md#sello-042--extend-live-sandbox-and-regression-corpus-for-signed-carryover) | Extend live sandbox and regression corpus for signed carryover | SELLO-E09 | P1 | G2 | [SELLO-041](epics/E09-signed-carryover.md#sello-041--deliver-accessible-carryover-opt-in-and-availability-explanations) |
| [SELLO-043](epics/E09-signed-carryover.md#sello-043--verify-and-release-the-complete-signed-carryover-increment) | Verify and release the complete signed carryover increment | SELLO-E09 | P1 | G3 | [SELLO-042](epics/E09-signed-carryover.md#sello-042--extend-live-sandbox-and-regression-corpus-for-signed-carryover) |
| [SELLO-044](epics/E10-legacy-import.md#sello-044--import-legacy-data-through-the-validated-restore-path) | Import legacy data through the validated restore path | SELLO-E10 | P2 | G2 | None |
| [SELLO-074](epics/E14-assistive-technology.md#sello-074--review-the-app-with-talkback-switch-access-and-a-keyboard) | Review the app with TalkBack, Switch Access and a keyboard | SELLO-E14 | P1 | G2 | None |
| [SELLO-075](epics/E15-release-fixes.md#sello-075--group-thousands-in-every-count-the-app-shows) | Group thousands in every count the app shows | SELLO-E15 | P2 | G2 | None |
| [SELLO-076](epics/E15-release-fixes.md#sello-076--show-resumens-recorded-until-date-in-words) | Show Resumen's "recorded until" date in words | SELLO-E15 | P2 | G2 | None |
| [SELLO-077](epics/E15-release-fixes.md#sello-077--make-the-sandbox-scenario-replay-tests-reliable) | Make the sandbox scenario replay tests reliable | SELLO-E15 | P1 | G2 | None |
| [SELLO-078](epics/E15-release-fixes.md#sello-078--say-what-failed-when-a-backup-does-not-pass-its-own-check) | Say what failed when a backup does not pass its own check | SELLO-E15 | P2 | G2 | None |

## Done

No tickets.

## Ready promotion candidates

[SELLO-037](epics/E09-signed-carryover.md#sello-037--approve-signed-carryover-policy-and-independent-examples), [SELLO-044](epics/E10-legacy-import.md#sello-044--import-legacy-data-through-the-validated-restore-path), [SELLO-074](epics/E14-assistive-technology.md#sello-074--review-the-app-with-talkback-switch-access-and-a-keyboard), [SELLO-075](epics/E15-release-fixes.md#sello-075--group-thousands-in-every-count-the-app-shows), [SELLO-076](epics/E15-release-fixes.md#sello-076--show-resumens-recorded-until-date-in-words), [SELLO-077](epics/E15-release-fixes.md#sello-077--make-the-sandbox-scenario-replay-tests-reliable), [SELLO-078](epics/E15-release-fixes.md#sello-078--say-what-failed-when-a-backup-does-not-pass-its-own-check)
