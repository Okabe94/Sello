# 0013 — Sello v2 is the visual source of truth

**Date:** 2026-10-16 · **Owner:** project owner (user) · **Ticket:** none
**Status:** accepted. The owner reviewed and locked every screen, one at a time,
on 2026-10-15 and 2026-10-16.

## Context

[ADR 0011](0011-goal-centered-design.md) set the product direction and kept the
final concrete design for another designer. The owner then chose to finish that
design in this project. It was done screen by screen, with options for each
screen and a commit when the screen was locked.

## Decision

[Sello v2](../design/v2/README.md) (`docs/design/v2/`) is the approved visual
reference for presentation. It covers Recibo, entry, Resumen, income, Metas
(the tab, goal detail, Aportar / Retirar, create and edit, celebrations),
Inversiones and the [shared components](../design/v2/components.html). Parts that v2
does not show still follow the [v1.1 snapshot](../design/sello-spec.html). The
[HTML exploration](../design/proposals/index.html) is history only.

v2 is a design reference, not a financial specification. Its numbers are synthetic
examples. [Architecture](../../ARCHITECTURE.md) and ADRs 0001–0012 still own money,
dates, recovery and safety. v2 adds no route, ledger or data model.

## What v2 changes in earlier decisions

| Earlier decision | v2 decision |
| --- | --- |
| Entry starts on the system numeric keyboard (ADR 0011, philosophy, entry brief) | Every amount uses the Sello pad. Text fields keep the phone keyboard. The note uses a composer docked to the keyboard. |
| Stamp words: Límite alcanzado, Sobre el límite, Sin margen libre, Por ajustar; link Revisar presupuesto (ADR 0012) | **Al límite**, **Excedido**, **Sin margen**, **Revisar**; link **Ajustar presupuesto**. The per-day line reads "40.000 por día". |
| Past-month status left open; v1.1 used Bajo el límite / Pasado | **Cumplido** for a month that closed within its limit, **Excedido** for one that went over. |
| Resumen leads with net flow (SELLO-022) | Resumen leads with the same-days comparison. Net flow moves to the income slip right below it. The financial meaning does not change. |
| An earned milestone no longer appears earned after a withdrawal (goals brief, philosophy) | The earned stamp stays, faded, with one line saying it is still yours. Percentage and bar stay honest. Reaching it again lights it back up. |
| Goals complete automatically at the target (ADR 0011, goals brief, philosophy) | Reaching 100 % is announced. **Marcar como lograda** completes the goal and plays the big celebration. A toast offers **Deshacer** for about 10 s; after that there is no reopening. The Meta stamp is earned when the goal is marked. |
| Completed-goal lifecycle left open | A completed goal is read-only: no edit, no contributions or withdrawals. Deleting is its only action. |
| v1.1 showed projected months under every stamp | Metas and goal detail show no projected months. Projected months appear only while creating or editing a goal, with the pace they assume stated (architecture: projected dates need explicit assumptions). The monthly amount is shown in whole pesos, never rounded further. |
| Goal withdrawal bounds left open | A withdrawal can be at most what the goal has saved. |
| Investment withdrawals and first valuation left open (investments brief) | A withdrawal is limited to the last recorded value. A new investment's first value defaults to its first contribution, and the person can change it. An investment without a value is left out of the total; only its row says so. |
| Final design reserved for another designer (ADR 0011, design README, architecture) | Resolved by v2. |

Every other v2 choice follows ADRs 0011–0012 and the briefs. The full record,
including rejected alternatives, is in the [v2 README](../design/v2/README.md).

## Consequences

- Implementation tickets use v2 for presentation, and the component catalog for
  what exists, what needs a variant and what is new in `design-system`.
- Manual goal completion, the kept stamp, Deshacer and the investment rules above
  are product rules. They need scoped tickets with reviewed examples before any code.
- Still open: investment ledger, return periods, dividend and FX policy, and goal
  corrections. v2 does not settle them.
