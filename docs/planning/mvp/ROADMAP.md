# Sello post-MVP roadmap — Kanban board

Generated from `epics/*.md`; do not edit independently.

**Tickets Done:** 1/48 · **Epics Done:** 0/8

Backlog: 47 · Ready: 0 · In Progress: 0 · Review: 0 · Blocked: 0 · Done: 1

[Workflow, gates and definition of done](README.md)

## Epic outcomes

| Epic | Outcome | Status | Done |
| --- | --- | --- | --- |
| [SELLO-E09](epics/E09-signed-carryover.md) | Preserve surplus and overspending across enabled budget periods without confusing plans with account balances. | Backlog | 0/7 |
| [SELLO-E10](epics/E10-legacy-import.md) | Let someone bring their history from the previous app into Sello without weakening Sello's rules. | Backlog | 0/1 |
| [SELLO-E14](epics/E14-assistive-technology.md) | Confirm that a person using TalkBack, Switch Access or a hardware keyboard can use the whole released app. | Backlog | 0/1 |
| [SELLO-E15](epics/E15-release-fixes.md) | Fix the defects found while measuring and accepting the MVP, each with a test. | In Progress | 1/4 |
| [SELLO-E16](epics/E16-v2-today-gaps.md) | Add the parts of the approved v2 Recibo, Resumen and income screens that the MVP left out, which need no new kind of data. | Backlog | 0/6 |
| [SELLO-E17](epics/E17-goals.md) | Let a person create goals, record contributions and withdrawals, earn milestone stamps and complete goals, as designed in Sello v2. | Backlog | 0/10 |
| [SELLO-E18](epics/E18-recurrence.md) | Let a person schedule recurring payments and incomes that Sello records on their dates, reserving payments still to come. | Backlog | 0/10 |
| [SELLO-E19](epics/E19-investments.md) | Let a person record investments in COP or USD with contributions, withdrawals, dividends and values, and see the portfolio and each investment's simple return; then release goals, recurrence and investments together. | Backlog | 0/9 |

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
| [SELLO-078](epics/E15-release-fixes.md#sello-078--say-what-failed-when-a-backup-does-not-pass-its-own-check) | Say what failed when a backup does not pass its own check | SELLO-E15 | P2 | G2 | None |
| [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing) | Verify what exists for today's Recibo, Resumen and income screens and what is missing | SELLO-E16 | P1 | G1 | None |
| [SELLO-080](epics/E16-v2-today-gaps.md#sello-080--lead-resumen-with-the-same-days-comparison) | Lead Resumen with the same-days comparison | SELLO-E16 | P1 | G2 | [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing) |
| [SELLO-081](epics/E16-v2-today-gaps.md#sello-081--show-the-average-month-and-the-weekday-answer-in-resumen) | Show the average month and the weekday answer in Resumen | SELLO-E16 | P2 | G2 | [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing) |
| [SELLO-082](epics/E16-v2-today-gaps.md#sello-082--offer-one-budget-suggestion-in-resumen) | Offer one budget suggestion in Resumen | SELLO-E16 | P2 | G2 | [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing) |
| [SELLO-083](epics/E16-v2-today-gaps.md#sello-083--suggest-assigning-unassigned-income) | Suggest assigning unassigned income | SELLO-E16 | P2 | G2 | [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing) |
| [SELLO-084](epics/E16-v2-today-gaps.md#sello-084--invite-a-look-at-the-month-just-closed) | Invite a look at the month just closed | SELLO-E16 | P2 | G2 | [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing) |
| [SELLO-085](epics/E17-goals.md#sello-085--verify-what-exists-for-metas-and-what-is-missing) | Verify what exists for Metas and what is missing | SELLO-E17 | P1 | G1 | None |
| [SELLO-086](epics/E17-goals.md#sello-086--approve-goal-rules-and-worked-examples) | Approve goal rules and worked examples | SELLO-E17 | P1 | G1 | [SELLO-085](epics/E17-goals.md#sello-085--verify-what-exists-for-metas-and-what-is-missing) |
| [SELLO-087](epics/E17-goals.md#sello-087--store-goals-and-their-movements) | Store goals and their movements | SELLO-E17 | P1 | G2 | [SELLO-086](epics/E17-goals.md#sello-086--approve-goal-rules-and-worked-examples) |
| [SELLO-088](epics/E17-goals.md#sello-088--carry-goals-through-backup-restore-and-erase) | Carry goals through backup, restore and erase | SELLO-E17 | P1 | G2 | [SELLO-087](epics/E17-goals.md#sello-087--store-goals-and-their-movements) |
| [SELLO-089](epics/E17-goals.md#sello-089--add-the-metas-tab-and-goal-detail) | Add the Metas tab and goal detail | SELLO-E17 | P1 | G2 | [SELLO-087](epics/E17-goals.md#sello-087--store-goals-and-their-movements) |
| [SELLO-090](epics/E17-goals.md#sello-090--create-edit-and-delete-goals) | Create, edit and delete goals | SELLO-E17 | P1 | G2 | [SELLO-089](epics/E17-goals.md#sello-089--add-the-metas-tab-and-goal-detail) |
| [SELLO-091](epics/E17-goals.md#sello-091--record-contributions-and-withdrawals) | Record contributions and withdrawals | SELLO-E17 | P1 | G2 | [SELLO-089](epics/E17-goals.md#sello-089--add-the-metas-tab-and-goal-detail) |
| [SELLO-092](epics/E17-goals.md#sello-092--earn-stamps-and-complete-goals) | Earn stamps and complete goals | SELLO-E17 | P1 | G2 | [SELLO-091](epics/E17-goals.md#sello-091--record-contributions-and-withdrawals) |
| [SELLO-093](epics/E17-goals.md#sello-093--show-one-goal-on-recibo) | Show one goal on Recibo | SELLO-E17 | P1 | G2 | [SELLO-092](epics/E17-goals.md#sello-092--earn-stamps-and-complete-goals) |
| [SELLO-094](epics/E17-goals.md#sello-094--cover-goals-in-the-sandbox-and-catalog) | Cover goals in the sandbox and catalog | SELLO-E17 | P1 | G2 | [SELLO-092](epics/E17-goals.md#sello-092--earn-stamps-and-complete-goals) |
| [SELLO-095](epics/E18-recurrence.md#sello-095--verify-what-exists-for-recurrence-and-reserved-payments-and-what-is-missing) | Verify what exists for recurrence and reserved payments and what is missing | SELLO-E18 | P1 | G1 | None |
| [SELLO-096](epics/E18-recurrence.md#sello-096--approve-recurrence-rules-and-worked-examples) | Approve recurrence rules and worked examples | SELLO-E18 | P1 | G1 | [SELLO-095](epics/E18-recurrence.md#sello-095--verify-what-exists-for-recurrence-and-reserved-payments-and-what-is-missing) |
| [SELLO-097](epics/E18-recurrence.md#sello-097--calculate-schedule-occurrences) | Calculate schedule occurrences | SELLO-E18 | P1 | G2 | [SELLO-096](epics/E18-recurrence.md#sello-096--approve-recurrence-rules-and-worked-examples) |
| [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) | Store schedules and record due occurrences on opening | SELLO-E18 | P1 | G2 | [SELLO-097](epics/E18-recurrence.md#sello-097--calculate-schedule-occurrences) |
| [SELLO-099](epics/E18-recurrence.md#sello-099--carry-schedules-through-backup-restore-and-erase) | Carry schedules through backup, restore and erase | SELLO-E18 | P1 | G2 | [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) |
| [SELLO-100](epics/E18-recurrence.md#sello-100--manage-recurring-payments-and-incomes) | Manage recurring payments and incomes | SELLO-E18 | P1 | G2 | [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) |
| [SELLO-101](epics/E18-recurrence.md#sello-101--show-reservations-on-recibo) | Show reservations on Recibo | SELLO-E18 | P1 | G2 | [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) |
| [SELLO-102](epics/E18-recurrence.md#sello-102--show-expected-income-in-plan-del-mes) | Show expected income in Plan del mes | SELLO-E18 | P1 | G2 | [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) |
| [SELLO-103](epics/E18-recurrence.md#sello-103--tell-what-sello-recorded-on-opening) | Tell what Sello recorded on opening | SELLO-E18 | P1 | G2 | [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) |
| [SELLO-104](epics/E18-recurrence.md#sello-104--cover-recurrence-in-the-sandbox) | Cover recurrence in the sandbox | SELLO-E18 | P1 | G2 | [SELLO-098](epics/E18-recurrence.md#sello-098--store-schedules-and-record-due-occurrences-on-opening) |
| [SELLO-105](epics/E19-investments.md#sello-105--verify-what-exists-for-inversiones-and-what-is-missing) | Verify what exists for Inversiones and what is missing | SELLO-E19 | P1 | G1 | None |
| [SELLO-106](epics/E19-investments.md#sello-106--approve-investment-rules-and-worked-examples) | Approve investment rules and worked examples | SELLO-E19 | P1 | G1 | [SELLO-105](epics/E19-investments.md#sello-105--verify-what-exists-for-inversiones-and-what-is-missing) |
| [SELLO-107](epics/E19-investments.md#sello-107--store-investments-movements-values-and-the-usd-rate) | Store investments, movements, values and the USD rate | SELLO-E19 | P1 | G2 | [SELLO-106](epics/E19-investments.md#sello-106--approve-investment-rules-and-worked-examples) |
| [SELLO-108](epics/E19-investments.md#sello-108--carry-investments-through-backup-restore-and-erase) | Carry investments through backup, restore and erase | SELLO-E19 | P1 | G2 | [SELLO-107](epics/E19-investments.md#sello-107--store-investments-movements-values-and-the-usd-rate) |
| [SELLO-109](epics/E19-investments.md#sello-109--add-the-inversiones-tab-overview) | Add the Inversiones tab overview | SELLO-E19 | P1 | G2 | [SELLO-107](epics/E19-investments.md#sello-107--store-investments-movements-values-and-the-usd-rate) |
| [SELLO-110](epics/E19-investments.md#sello-110--record-investment-movements-and-values) | Record investment movements and values | SELLO-E19 | P1 | G2 | [SELLO-109](epics/E19-investments.md#sello-109--add-the-inversiones-tab-overview) |
| [SELLO-111](epics/E19-investments.md#sello-111--set-the-usd-rate) | Set the USD rate | SELLO-E19 | P1 | G2 | [SELLO-107](epics/E19-investments.md#sello-107--store-investments-movements-values-and-the-usd-rate) |
| [SELLO-112](epics/E19-investments.md#sello-112--cover-investments-in-the-sandbox-and-catalog) | Cover investments in the sandbox and catalog | SELLO-E19 | P1 | G2 | [SELLO-110](epics/E19-investments.md#sello-110--record-investment-movements-and-values) |
| [SELLO-113](epics/E19-investments.md#sello-113--release-020-with-goals-recurrence-and-investments) | Release 0.2.0 with goals, recurrence and investments | SELLO-E19 | P1 | G3 | [SELLO-094](epics/E17-goals.md#sello-094--cover-goals-in-the-sandbox-and-catalog), [SELLO-099](epics/E18-recurrence.md#sello-099--carry-schedules-through-backup-restore-and-erase), [SELLO-100](epics/E18-recurrence.md#sello-100--manage-recurring-payments-and-incomes), [SELLO-101](epics/E18-recurrence.md#sello-101--show-reservations-on-recibo), [SELLO-102](epics/E18-recurrence.md#sello-102--show-expected-income-in-plan-del-mes), [SELLO-103](epics/E18-recurrence.md#sello-103--tell-what-sello-recorded-on-opening), [SELLO-104](epics/E18-recurrence.md#sello-104--cover-recurrence-in-the-sandbox), [SELLO-108](epics/E19-investments.md#sello-108--carry-investments-through-backup-restore-and-erase), [SELLO-111](epics/E19-investments.md#sello-111--set-the-usd-rate), [SELLO-112](epics/E19-investments.md#sello-112--cover-investments-in-the-sandbox-and-catalog) |

## Done

| Ticket | Deliverable | Epic | Priority | Gate | Unmet dependencies |
| --- | --- | --- | --- | --- | --- |
| [SELLO-077](epics/E15-release-fixes.md#sello-077--make-the-sandbox-scenario-replay-tests-reliable) | Make the sandbox scenario replay tests reliable | SELLO-E15 | P1 | G2 | None |

## Ready promotion candidates

[SELLO-037](epics/E09-signed-carryover.md#sello-037--approve-signed-carryover-policy-and-independent-examples), [SELLO-044](epics/E10-legacy-import.md#sello-044--import-legacy-data-through-the-validated-restore-path), [SELLO-074](epics/E14-assistive-technology.md#sello-074--review-the-app-with-talkback-switch-access-and-a-keyboard), [SELLO-075](epics/E15-release-fixes.md#sello-075--group-thousands-in-every-count-the-app-shows), [SELLO-076](epics/E15-release-fixes.md#sello-076--show-resumens-recorded-until-date-in-words), [SELLO-078](epics/E15-release-fixes.md#sello-078--say-what-failed-when-a-backup-does-not-pass-its-own-check), [SELLO-079](epics/E16-v2-today-gaps.md#sello-079--verify-what-exists-for-todays-recibo-resumen-and-income-screens-and-what-is-missing), [SELLO-085](epics/E17-goals.md#sello-085--verify-what-exists-for-metas-and-what-is-missing), [SELLO-095](epics/E18-recurrence.md#sello-095--verify-what-exists-for-recurrence-and-reserved-payments-and-what-is-missing), [SELLO-105](epics/E19-investments.md#sello-105--verify-what-exists-for-inversiones-and-what-is-missing)
