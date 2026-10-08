# SELLO-E03 — Financial truth and persistence

- **Status:** Backlog
- **Goal:** Make amounts, budgets, dates and saved outcomes trustworthy before the UI relies on them.
- **Exit:** Tested exact policies, Room contracts/migrations, durable command receipts and same-revision snapshots; no dormant post-MVP schema.

## SELLO-010 — Implement exact money, dates and command validation

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-001, SELLO-003, SELLO-004
- **Gate:** G1

### Outcome
Create one pure domain boundary for amounts and effective periods, not scattered screen helpers.

### Deliverables
Typed COP money, currency scale policy, strict locale-aware input parsing, checked
arithmetic with exact widened totals, typed failures, LocalDate/YearMonth validation
and bounded name/note rules. Future currency policy can be represented without
exposing unsupported MVP entry currencies or adding rate infrastructure.

### Acceptance criteria
- Valid COP amounts round-trip exactly; invalid fraction, zero/negative where
  forbidden, exponent notation, overflow and ambiguous paste reject original input.
- Intermediate summation order does not create false overflow; final unrepresentable
  values are explicit failures, never zero/wrap/saturation.
- Effective dates cannot exceed the financial current date for manual MVP entries;
  audit Instants and translated labels are not embedded in financial identity.

### Tests
Table/property-style tests around Long bounds, grouping/separators, empty drafts,
checked addition/subtraction, reordered sums, leap days, and future-date rejection.
Capture red→green evidence and run G1.

### Working checkpoint
Policies run without Android or DI. Existing app/catalog compile; no unvalidated
money enters a newly exposed product workflow.

### Context and starting points
Integer storage alone does not protect parsing, sums or dates. Inputs are the
approved D03/D05 decisions and example corpus from SELLO-001; read architecture §4,
[M/B examples](../DECISION_INPUTS.md#exact-baseline-examples) and module map.
There is no legacy helper to transplant wholesale.

### Implementation plan
1. Define typed money/currency/effective-date/month values and typed validation failures
   in domain/model/policy; define a draft parse result separately from valid positive money.
2. Implement strict approved `es-CO` input grammar, currency scale/rounding and exact
   Long conversion. Validate the original lexeme; do not strip arbitrary characters.
3. Implement checked add/subtract and widened aggregate sums; final representation
   checks must be independent of iteration order. Keep rates/floating-point out of MVP.
4. Implement approved field/date bounds with injected financial date, including
   manual future-date rejection; codecs/render strings belong to other boundaries.
5. Create table-driven domain tests before production logic and document stable
   public contracts for controls/Room/backup consumers. Do not add pass-through use cases.

### Concrete cases and pitfalls
- M01/M02 must hold. A mathematical sum `Long.MAX_VALUE + 1 - Long.MAX_VALUE` is
  1 even if a narrow intermediate would overflow; an actual final overflow must fail.
- COP fractional amounts cannot round silently unless the approved boundary explicitly
  permits conversion. Zero/empty draft and zero valid budget are different concepts.
- A valid calendar date after the injected current date is still invalid manual entry;
  date parsing failure must not return raw text as if valid.

### Verification recipe
Create/run `./gradlew :domain:test --tests '*MoneyPolicyTest'` and `*DatePolicyTest`,
then G1. Record independent boundary tables and red→green output. Tests must execute
on JVM without Android, database or DI initialization.

## SELLO-011 — Create Room v1, integrity constraints and recovery metadata

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-010, SELLO-005, SELLO-004
- **Gate:** G2

### Outcome
Establish the minimum durable MVP model and migration safety rather than inheriting legacy entities.

### Deliverables
Exported Room v1 schema for profile/financial zone/generation/revision, categories,
monthly limits, expenses/income, operation receipts and actual recovery/compensation
metadata as required. Stable IDs/logical ordering, strict mappers, foreign keys,
unique operation identities, indexes, bounded queries and DB construction/composition.

### Acceptance criteria
- No recurrence, goals, investments, widgets or capture tables. Preferences that
  are nonfinancial remain outside Room; financial configuration stays inside it.
- Unknown stored kind/currency/date is an integrity failure, not a default or empty list.
- Recovery metadata can survive reset/restore financial replacement; generation
  and revision initialize atomically. Destructive fallback is prohibited.
- First-use/upgrade startup is deterministic and keeps financial zone persistent.
- Disable unapproved OS/cloud backup and device transfer before storing the first
  user financial data; SELLO-032 later verifies/hardens that boundary, not first disables it.

### Tests
Real-Room create/reopen, uniqueness, reference and strict-mapper tests; exported
schema validation and initial migration harness. Test malformed persisted rows and
DB initialization races. Run G2 with device evidence, not mocked DAO assertions.

### Working checkpoint
Real DB opens/reopens in the app; the existing UI still launches without seeded
financial records or fake reads. Future schema changes add tested migrations.

### Context and starting points
Room is financial authority, including revisions/recovery, not just CRUD storage.
Inputs are SELLO-005 clocks and SELLO-010 types. Read architecture §5,
[Room/migration sources](../EXECUTION_GUIDE.md#source-library) and accepted D04/D09.
Use `data/local` for schema ownership; app sees it only through composition.

### Implementation plan
1. Draft/implement minimum entity relationships: persistent profile zone/generation/
   revision; stable category ID/name/icon/archive/version; month limits; expense/income
   ID/date/sequence/amount/source or category/version/audit; receipts/control recovery rows.
2. Define foreign keys, operation uniqueness, ordered query indexes and schema checks.
   Use stable wire/storage keys and strict date/currency decoding, not translated enums.
3. Implement one real Room database/open factory and domain mappers, plus bounded
   read/transaction access. Export v1 schema and create migration-test configuration.
4. Initialize profile once atomically using the real financial-zone port. Separate
   rows retained for restore/reset recovery from the financial rows being replaced.
5. Disable unapproved OS backup/transfer before real money storage. Add real-Room
   creation/reopen/constraint/integrity tests and document schema/version ownership.

### Concrete cases and pitfalls
- Foreign category reference or duplicate operation origin must be rejected by storage,
  not merely an in-memory Mutex. Unknown persisted kind/date is integrity failure.
- Receipts must not disappear during the operation they recover. Conversely reset
  cannot retain erased user payloads disguised as recovery metadata.
- Do not invent v0 migrations; initial schema is v1. Adding a field in later tickets
  requires updating/exporting a real schema and testing every supported upgrade.

### Verification recipe
Create `DatabaseIntegrityTest`/`ProfileInitializationTest` in data/androidTest and
run `./gradlew :data:connectedDebugAndroidTest`, followed by G2. Inspect exported
schema, merged backup rules and reopened-file results; mocked DAOs are insufficient.

## SELLO-012 — Save expenses with atomic receipts and uncertain-outcome recovery

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-011, SELLO-004
- **Gate:** G2

### Outcome
A recorded expense has a truthful, recoverable outcome even under double submit or cancellation.

### Deliverables
Narrow create-expense command/workflow port and Room implementation with operation
ID, input identity, data generation, transactional validation/write/receipt, typed
Rejected/Committed/OutcomeUnknown handling and receipt lookup. Establish the reusable
protocol, not a generic universal financial repository.

### Acceptance criteria
- Identical ID/input creates one expense and returns its original receipt; changed
  input under that ID conflicts. Stale generation and missing/archived category reject.
- A failed transaction leaves both financial row and receipt absent. Post-commit
  observer/platform failure cannot become “expense not saved”.
- Cancellation propagates; callers can recover a committed result using the same
  ID without blind resubmission. Receipt timestamps use audit time, not sandbox time.
- No network, file, DataStore or notification work occurs inside the transaction.

### Tests
Real-Room concurrent duplicate submit, same-ID conflicting input, rollback/fault
injection, reopen/receipt lookup and cancellation-around-commit tests; G2.

### Working checkpoint
Tests can record/read an expense through the real workflow after creating validated
test categories; app/catalog still launch. Production UI is connected in SELLO-017.

### Context and starting points
The legacy failure was “saved money reported failed” after follow-up work. Use the
real schema from SELLO-011 and [recovery cases](../DECISION_INPUTS.md#recovery-cases-to-turn-into-tests).
Architecture §5 defines the command outcomes; no form UI belongs to this ticket.

### Implementation plan
1. Define an expense-specific command with operation ID, generation, category ID,
   amount/date/note and creation intent; expose receipt lookup separately from submission.
2. In one Room transaction, reject stale generation/reference/invalid input, resolve
   identical/conflicting operation replay, validate required representable totals, insert
   expense, increment revision and insert the durable receipt with real audit time.
3. Encode canonical command identity consistently so replay compares meaningful input,
   not a mutable UI label or unstable serialized object. Document conflict reasons.
4. Distinguish rollback-established rejection from committed receipt and uncertain
   outcome; propagate cancellation. Follow-up observers never sit inside commit error handling.
5. Build contract tests against real Room with synchronized concurrent submits and
   before/after-commit cancellation points. Preserve IDs/results through DB reopen.

### Concrete cases and pitfalls
- Submit op A twice with identical amount 10,000: one expense/revision/receipt.
  Reuse A with 12,000: conflict and original row unchanged.
- A category deleted/archived after form selection must reject inside the transaction.
- A caller canceled after commit receives cancellation, but later receipt lookup
  proves save; do not “undo” money or silently create op B. A real constraint failure
  must leave both row and receipt absent.

### Verification recipe
Create `ExpenseCommandContractTest` in data/androidTest; use its instrumented class
selector from the guide, then data connected suite and G2. Record row/receipt/revision
assertions independently, including actual rollback and reopen—not only returned results.

## SELLO-013 — Implement category commands and historical monthly budget storage

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-011, SELLO-012, SELLO-004
- **Gate:** G2

### Outcome
Categories and period limits can evolve without silently changing past financial meaning.

### Deliverables
Typed category create/rename/icon/archive and monthly-limit commands using the
receipt/version/generation protocol; current-month initialization/rollover policy
from SELLO-001, archived-category query rules and atomic readable budget history.

### Acceptance criteria
- Identity survives rename; archival prevents new expenses without deleting history.
  No category color column or translated name used as ID.
- A limit change targets its explicit month; current/future defaults never rewrite
  previous periods. Unlimited versus zero limit is defined, not conflated.
- Stale versions/missing rows report conflicts; failed edits do not partially
  mutate budgets or revision. First category creation needs no sample data.

### Tests
Real-Room command races/rollback/missing rows, rename/archive retention, multiple
month rollover, unlimited/zero limits and prior-month stability. Run G2.

### Working checkpoint
Real category/budget workflows are callable and observed through domain ports;
first-run UI can be implemented without bypassing validated writes.

### Context and starting points
Renaming or changing today's budget must not redefine past spending. Use operation
protocol from SELLO-012, actual schema from SELLO-011 and accepted D04/D05/D07.
Read architecture §§4/5; user workflows consume these ports in SELLO-016/020.

### Implementation plan
1. Define narrow create/edit/archive/limit commands with explicit category ID/month,
   expected version, operation ID and generation. Category color is not a new field.
2. Persist name/icon separately from identity; apply archive/new-entry eligibility
   rules and reject malformed/missing/version-conflicting updates transactionally.
3. Implement accepted month-limit creation/carry-forward/skipped-period behavior,
   preserving historical facts and distinguishing unlimited from numeric zero.
4. Atomically update revision/receipt for category and related limit changes. Return
   domain values/read ports suitable for entry chips and historical detail.
5. Document the port contract and tests so UI cannot implement its own shortcut
   budget writes or silently cascade archived expenses.

### Concrete cases and pitfalls
- Rename category ID C: original expenses still reference C. Archive C: old September
  totals remain; new October expense in C rejects.
- H01 remains exact after October edit. An absent historical limit is not license
  to substitute the current limit; D04 decides the missing-period presentation.
- Repeating an archive operation must follow its receipt protocol; an unrelated
  stale editor is a conflict, not an idempotent success.

### Verification recipe
Create/run `CategoryCommandContractTest` and `MonthlyBudgetHistoryTest` on real Room,
plus pure budget-policy tests and G2. Test current/past month commands, concurrent
edit/archive and rollback with exact retained IDs/amounts.

## SELLO-014 — Calculate consistent Recibo and Resumen budget snapshots

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-001, SELLO-012, SELLO-013, SELLO-004
- **Gate:** G2

### Outcome
Define shared actuals/projections once so slips, details and charts cannot disagree.

### Deliverables
Snapshot read port at one revision/as-of/zone, exact category/month aggregates,
remaining/allowance/projected spending/verdict policies and explicit read states.
Provide approved cutoffs/rounding from SELLO-001; MVP recurringDue is zero because
recurrence is absent, not because a failed schedule read is discarded.

### Acceptance criteria
- Unlimited spending still affects totals; no limits yields Sin límite. Past months
  use historical limits/actuals and omit allowance. Today counts as an available day.
- Actual over-limit always wins; projected ≤95%, >95–100%, and >100% boundaries
  match examples. Exact source amounts remain available to chart render models.
- Read failures retain explicit failed/last-good states, not financial zero.
  Future entries are excluded; concurrent writes cannot produce mixed snapshots.

### Tests
Worked-example domain tests for first/last day, short months, 95/100% edges,
negative remaining, no limits, large sums; real-Room snapshot consistency/read faults.

### Working checkpoint
Consumer ports provide truthful current/historical totals to the coming slice.
G2 passes; no screen duplicates financial formulas.

### Context and starting points
Home/detail/summary need one shared policy, not separately combined DAO flows.
Inputs are actual command/schema contracts, approved D03/D04 and the independent
examples. Read [snapshot checklist](../EXECUTION_GUIDE.md#contract-and-failure-checklist)
and architecture §4; forecasts cannot be copied from reference drawings.

### Implementation plan
1. Define a monthly snapshot carrying generation/revision/as-of/zone, exact category
   actuals/limits and totals, plus explicit loading/failed/last-good read semantics.
2. Read related financial rows at one consistent Room revision; perform exact bounded
   aggregation off main. Recompute on real/injected date changes and committed revisions.
3. Implement approved remaining/allowance/pace/projection/verdict policy and past/no-limit
   branches; future rows and recurringDue rules must be explicit, not hidden filters.
4. Return exact numeric/provenance values, not formatted Spanish labels or Float chart
   series. Keep forecast distinct from actuals and mark unavailable totals as failures.
5. Implement tests before screen wiring; publish the read/policy contract for Recibo,
   previews, Resumen and eventual alerts without constructing those later adapters.

### Concrete cases and pitfalls
- B01–B04/V01–V04/H01 must pass. A negative remaining allowance is zero with loss
  semantics; no-budget is not a numeric-zero budget. On Oct 31 allowance includes today.
- Concurrent expense and limit edit cannot show old budget with new spend in one
  snapshot. Last-good data from another month/date must be labeled with its source.
- Unrepresentable report totals fail explicitly; SQL SUM promotion or Float conversion
  cannot rescue them by inventing a different amount.

### Verification recipe
Create/run `./gradlew :domain:test --tests '*MonthlyBudgetPolicyTest'` and real-Room
`MonthlySnapshotConsistencyTest`, then G2. Include approved day-cutoff/rational-rounding
examples, first/subsequent read faults and observable clock-triggered recomputation.
