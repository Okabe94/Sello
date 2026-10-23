# SELLO-E19 — Inversiones, and the 0.2.0 release

- **Status:** Backlog
- **Increment:** Post-MVP
- **Goal:** Let a person record investments in COP or USD with contributions, withdrawals, dividends and values, and see the portfolio and each investment's simple return; then release goals, recurrence and investments together.
- **Exit:** Inversiones is a tab with the v2 overview and detail; USD uses one dated app-wide rate; backups carry investments; `0.2.0` passes G3 and the owner's acceptance.

**Owner direction (2026-10-23):** decisions in [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md): simple return since the first
contribution, COP and USD with one app-wide manual rate, USD returns in USD, values show
their date and are never stale, dividends count in the gain, corrections with undo; no
network. The last ticket releases E17, E18 and E19 as one `0.2.0`.

## SELLO-105 — Verify what exists for Inversiones and what is missing

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-004
- **Gate:** G1

### Outcome
An exact, current list of what the app, the design system and the catalog already have for Inversiones, against the approved design, so the following tickets build only what is missing.

### Deliverables
A gap list in `docs/design/v2/gaps/` for Inversiones: each designed element marked present (with its code and tests), partial (what differs) or missing; the following tickets updated, split or closed to match, with owner approval for any scope change.

### Acceptance criteria
- Every element of the v2 Inversiones page is checked against the running app and the code, not only the docs.
- Each present element names its code path and test; each partial one says exactly what differs.
- Work done outside these tickets (other branches, merged PRs, catalog components) is found and credited.
- SELLO-106 to SELLO-112 are revised to the list before any starts; changed scope is recorded and owner-approved.

### Tests
The audit itself, with screenshots of the running app where it differs from the design; G1.

### Working checkpoint
The gap list is merged and every later ticket in this epic matches it.

### Context and starting points
The screens have been worked on outside these tickets (owner, 2026-10-23); [ADR 0017](../../../decisions/0017-goals-recurrence-investments.md) records the decisions. Design: [v2 README](../../../design/v2/README.md) and `docs/design/v2/components.html` (Exists / Extend / New).

### Implementation plan
1. List every designed element, state and rule.
2. Check each in the app on the emulator and in the code and catalog.
3. Write the gap list and revise the following tickets; ask the owner about scope changes.

### Concrete cases and pitfalls
- Do not trust a document's claim that something exists: open the screen and the code.
- Do not start building while auditing; findings go into the list and tickets.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-105 --retain`.

## SELLO-106 — Approve investment rules and worked examples

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-105, SELLO-004
- **Gate:** G1

### Outcome
Reviewed rules and independent examples for investments before any code.

### Deliverables
Examples for contributions, withdrawals (never above the last value), dividends, values, the simple return, missing values, USD with the app-wide rate and corrections.

### Acceptance criteria
- Every ADR 0017 investment decision has examples with exact amounts.
- Rate validation (positive, bounded, decimal) and its date are defined.
- The owner approves; open points are listed.

### Tests
Owner review; G1.

### Working checkpoint
Approved examples exist.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); Inversiones brief; ARCHITECTURE FX rules.

### Implementation plan
1. Draft.
2. Review.
3. Record approval.

### Concrete cases and pitfalls
- Never a 0 % return without contributions.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-106 --retain`.

## SELLO-107 — Store investments, movements, values and the USD rate

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-106, SELLO-004
- **Gate:** G2

### Outcome
Investments and their history live in Room, exactly, in COP or USD, with a dated app-wide rate.

### Deliverables
Domain models and policies, schema migration, commands with receipts and undo, reads with typed failures.

### Acceptance criteria
- Decimal rates, never floating point; amounts exact in their currency.
- Withdrawals above the last value are refused.
- Corrections recompute totals as the examples.

### Tests
Real-Room and domain example tests; migration tests; G2.

### Working checkpoint
An investment history reads back with the approved figures.

### Context and starting points
Approved examples (SELLO-106); ARCHITECTURE §4–5, §8 FX.

### Implementation plan
1. Domain.
2. Schema.
3. Commands and reads.

### Concrete cases and pitfalls
- USD returns stay in USD; only the total converts.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-107 --retain`.

## SELLO-108 — Carry investments through backup, restore and erase

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-107, SELLO-004
- **Gate:** G2

### Outcome
Backups include investments and rates; restore and erase handle them.

### Deliverables
Format additions with docs; restore and reset updates.

### Acceptance criteria
- Older files still restore; new ones round-trip exactly, including decimal rates.

### Tests
Codec and round-trip tests; G2.

### Working checkpoint
A backup with investments restores with the same figures.

### Context and starting points
`docs/data/backup-format.md`.

### Implementation plan
1. Format.
2. Restore and reset.
3. Tests.

### Concrete cases and pitfalls
- Rates as exact decimals.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-108 --retain`.

## SELLO-109 — Add the Inversiones tab overview

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-107, SELLO-004
- **Gate:** G2

### Outcome
Inversiones is a main tab leading with value, a Gana/Pierde stamp and the signed simple return.

### Deliverables
The v2 overview with capital and gain, distribution, each value's date, and missing-value handling.

### Acceptance criteria
- The total converts USD at the current rate and says its date.
- An investment without a value is left out of the total and only its row says so.
- Loading, empty and failed reads are distinct; never zero.

### Tests
Screen and ViewModel tests across windows and themes; G2.

### Working checkpoint
The overview shows the approved figures.

### Context and starting points
v2 Inversiones page.

### Implementation plan
1. Tab.
2. Overview.
3. States.

### Concrete cases and pitfalls
- Goals are never added to the total.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-109 --retain`.

## SELLO-110 — Record investment movements and values

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-109, SELLO-004
- **Gate:** G2

### Outcome
Each investment's detail records contributions, withdrawals, dividends and values, with corrections.

### Deliverables
The detail with its history, the movement form with the Sello pad and date, edit, delete and undo.

### Acceptance criteria
- The first value defaults to the first contribution and is editable.
- Dividends add to gain, not value.
- Unknown outcomes are recovered before another write.

### Tests
ViewModel, screen and journey tests; G2.

### Working checkpoint
Movements and values update the investment exactly.

### Context and starting points
v2 Inversiones page.

### Implementation plan
1. Detail.
2. Form.
3. Corrections.

### Concrete cases and pitfalls
- Recording a movement is not a fresh valuation.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-110 --retain`.

## SELLO-111 — Set the USD rate

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-107, SELLO-004
- **Gate:** G2

### Outcome
A person sets one USD-to-COP rate with its date; changing it records a new dated rate.

### Deliverables
A rate setting with validation and its history, used by the overview total.

### Acceptance criteria
- Rates are positive, bounded decimals with the date entered.
- The overview states which rate and date it used.

### Tests
Validation and screen tests; G2.

### Working checkpoint
Changing the rate updates the total and says its date.

### Context and starting points
[ADR 0017](../../../decisions/0017-goals-recurrence-investments.md); ARCHITECTURE FX rules.

### Implementation plan
1. Setting.
2. History.
3. Tests.

### Concrete cases and pitfalls
- No network or automatic rates.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-111 --retain`.

## SELLO-112 — Cover investments in the sandbox and catalog

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-110, SELLO-004
- **Gate:** G2

### Outcome
Sandbox scenarios and catalog states exercise investments through the real paths.

### Deliverables
Scenarios for returns, missing values, USD and corrections; catalog examples for new components.

### Acceptance criteria
- Synthetic data, real commands; none ships in the release build.

### Tests
Scenario and catalog tests; G2.

### Working checkpoint
Each investment scenario reaches its check.

### Context and starting points
ARCHITECTURE §9.

### Implementation plan
1. Scenarios.
2. Catalog.
3. Tests.

### Concrete cases and pitfalls
- Keep clocks distinct.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-112 --retain`.

## SELLO-113 — Release 0.2.0 with goals, recurrence and investments

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-094, SELLO-099, SELLO-100, SELLO-101, SELLO-102, SELLO-103, SELLO-104, SELLO-108, SELLO-111, SELLO-112, SELLO-004
- **Gate:** G3

### Outcome
A signed, accepted `0.2.0` with Metas, recurrence and Inversiones.

### Deliverables
A release checklist like the MVP's, the owner's walkthrough on a phone, measurements against the approved targets with the new features, and the go/no-go.

### Acceptance criteria
- Full G3 passes on the signed candidate.
- The owner walks goals, schedules and investments on a phone, including backup, restore and catch-up.
- Performance targets still hold with goals, schedules and investments in the large history.

### Tests
G3, the owner walkthrough and performance runs; G3.

### Working checkpoint
The owner approves `0.2.0`.

### Context and starting points
`docs/release/mvp-checklist.md` as the model; ADR 0016.

### Implementation plan
1. Checklist.
2. Walkthrough.
3. Go/no-go.

### Concrete cases and pitfalls
- Distribution stays a separate owner-authorized action.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-113 --retain`.
