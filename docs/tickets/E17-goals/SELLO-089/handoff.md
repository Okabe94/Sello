# SELLO-089 — Add the Metas tab and goal detail

Metas is the third tab (`MetasRoute`, `feature/goals`): goal cards best first, the band's total and this month's aportes, completed goals with LOGRADA, and a goal's detail (`GoalRoute`) with stamps, facts, aportes by month, movements, the Recibo star and, for a completed goal, deletion. `GoalReads.observeBoard` keeps both screens live; `GoalPolicy` adds the order, band totals, aportes per month and which aporte earned each stamp.
Still open: `MetasDestination`/`GoalDestination` take `onCreate`, `onContribute`, `onEdit` and `onWithdraw`, which SELLO-090 and SELLO-091 pass once their screens exist; Recibo shows the starred goal from SELLO-093; catalog examples for the dock tone, LOGRADA landing and band without aportes are SELLO-094's.
