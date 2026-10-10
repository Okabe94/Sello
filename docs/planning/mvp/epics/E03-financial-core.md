# SELLO-E03 — Financial truth and persistence

- **Status:** In Progress
- **Goal:** Make amounts, budgets, dates and saved outcomes trustworthy before the UI relies on them.
- **Exit:** Tested exact policies, Room contracts/migrations, durable command receipts and same-revision snapshots; no dormant post-MVP schema.

## SELLO-010 — Implement exact money, dates and command validation

- **Type:** Task
- **Priority:** P0
- **Status:** Done
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
- Enforce approved D05 name/note/Other-source boundaries and M01–M05/T01–T06;
  enforce 1–999,999,999,999 COP per expense/income across UI/domain/storage/backup.
  Follow ADR 0003: reject oversized transactions, never clip them; aggregate values
  are independently checked Long amounts, not subject to the transaction maximum.
  Category uniqueness normalization is shared, deterministic and enforced by real
  command/storage boundaries when they ship, not a UI-only duplicate warning.

### Tests
Table/property-style tests around Long bounds, grouping/separators, empty drafts,
checked addition/subtraction, reordered sums, leap days, and future-date rejection.
Include field-length Unicode boundaries, original invalid-input rejection and the
maximum valid amount preserved by an unrelated-field edit at its owning workflow;
M05 oversized backup rejection and M06 totals above the individual record cap.
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

### Execution progress
2026-10-09: started on branch `sello-010-money-dates-validation` after SELLO-006 merged.
No new product decision was needed: D05 and ADR 0003 were already approved.

Delivered in `:domain`, with no Android, DI or new dependency: `Outcome`; `Currency`
(COP scale 0 as the only MVP entry currency, USD/EUR/GBP scale 2 nameable only);
`Money` with checked `+`/`-`; `ExactTotal` for widened sums; `TransactionAmount`
(1–999.999.999.999 COP); `CopAmountInput.parse`; `EffectiveDates` (strict date and
month parsing, manual-entry and month-selection rules against an injected today);
`CategoryName` with `uniquenessKey`, `Note`, `SourceName` and `IncomeSource`.
`docs/development/money-dates-text.md` states the contracts for controls, Room and backup.

Interpretations taken where the approved text is silent, each the stricter reading,
for owner review:
- Leading zeros (`007`, `0.123`) are rejected, not dropped. A single `0` parses.
- Only whitespace at the ends is ignored; a space or non-breaking space inside a
  number is rejected, as are non-ASCII digits.
- Text is stored with composed accents (NFC), so `Café` counts 4 code points however it
  was typed. The uniqueness key also ignores compatibility forms (full-width letters),
  `ß`/`SS` and repeated inner spaces; `Año` and `Ano` therefore conflict.
- Control characters and line breaks are rejected in names, notes and source names, so
  notes are single-line.
- A name supplied with a source other than Otro is rejected, not ignored.
- Dates accept only four-digit years in `yyyy-MM-dd`; no earliest-date limit was invented.
- Month selection up to the current month is included here because architecture §6
  forbids future months and it is the same rule as future-dated entries.

Not delivered here because the owning workflow does not exist yet: the keypad's
thirteenth-digit feedback (M04, SELLO-008; `MAX_DIGITS` is provided), the maximum
amount surviving a note edit and backup rejection before mutation (M05, SELLO-015 and
SELLO-028/030), and uniqueness enforced by storage (T01/T02, SELLO-011). This ticket
supplies and tests the shared rule each of them must call. Currency conversion
rounding has no code: no MVP path converts.

2026-10-09: owner approved pull request 5, including the interpretations above; moved
to Done for a squash merge.

### Delivery evidence
- **Revision:** branch `sello-010-money-dates-validation`; tested snapshot is commit `a053c5f`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** valid COP round-trips and invalid fraction, zero or negative where forbidden, exponent, overflow and ambiguous paste reject the original input → `MoneyPolicyTest` grammar tables (M01–M03) with every error carrying the received text. Summation order creates no false overflow and a final unrepresentable value is an explicit failure → `anIntermediateBeyondLongDoesNotFailWhenTheFinalFigureFits`, `aFinalFigureBeyondLongIsAnExplicitFailure`, and `everyOrderOfTheSameTermsGivesTheSameResult` over 700 seeded shuffles with a hand-worked expected total. Transaction range and uncapped totals → `transactionAmountsAreOneTo999Billion` (0, 1, maximum, maximum plus one), `totalsMayExceedTheTransactionMaximum` (M06). No future manual dates against the injected day → `DatePolicyTest`, including leap days and impossible dates. No audit Instant or label in financial identity → the model types hold neither; `IncomeSource` exposes keys only. D05 text bounds and T01–T06 → `TextPolicyTest` with composed, decomposed and supplementary characters. Runs without Android or DI → `:domain` is a Kotlin/JVM module and the architecture rule `domain-pure` passes.
- **Red / Green:** the 35 tests were first run against a deliberately naive implementation (digits filtered out of any text, unchecked `Long` arithmetic, no range, future or length checks, lower-case-only uniqueness): 27 failed on those behaviours and 8 passed. With the rules implemented all 35 pass. Nine mutations of the finished code (maximum off by one, narrower overflow check, leading zeros accepted, loose grouping, today treated as future, accents kept in the key, UTF-16 length, currency check removed, no NFC) each failed the expected tests and were reverted.
- **Gate results:** local `./scripts/verify-ticket SELLO-010` passed G1: 80 host tests (35 domain, 29 app, 13 design-system, 3 catalog), ktlint, architecture rules, lint with 0 errors and 22 warnings, all in `:app` and present before this ticket. No dependency or lock file changed.
- **Quality run:** run 20261009T153955Z-9e316e7b; SELLO-010 G1 passed; HEAD a053c5f, inputs sha256 254d9593b3e0; report docs/planning/mvp/quality-reports/SELLO-010.json sha256 e448ff6c3d08e39ec158819f21019a4c0811783f80818251af6cce8bd7754092
- **Device / Artifact:** no device behaviour changes; nothing in the app or catalog calls these rules yet. Hosted run 37953517920 on the pull request's merge commit passed the `quality` check with the ticket in Review.
- **Review:** executor self-review of the diff, tables and reports. Project owner reviewed pull request 5 and approved on 2026-10-09, including the listed interpretations. This is owner acceptance, not an independent technical review; GitHub does not let the account that opened a pull request approve it.

## SELLO-011 — Create Room v1, integrity constraints and recovery metadata

- **Type:** Task
- **Priority:** P0
- **Status:** Done
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

### Execution progress
2026-10-09: started on branch `sello-011-room-v1` after SELLO-009 merged and E02 closed.
No product decision was needed: D04, D05, D07 and D09 are approved and say the schema
is this ticket's choice.

Delivered. `:data`: Room schema version 1 exported to `data/schemas`, with tables
`profile`, `category`, `default_limit`, `month_limit`, `expense`, `income` and
`operation_receipt`; entities, DAOs with bounded ordered reads, strict mappers, the
profile store, and `openFinancialStorage` as the one entry point. `:domain`: record
identifiers, `Category`, `IconKey`, `BudgetLimit`, `DefaultLimit`, `MonthLimit`,
`Expense`, `Income`, `FinancialProfile`, `StorageFailure` and the
`FinancialProfileStore` port. `:app`: startup reads the financial zone from the
database, and system backup and device transfer are switched off.
`docs/development/storage.md` describes the tables, formats and how to change them.

One dependency was added, for tests only: `androidx.room:room-testing` at the Room
version already pinned, which provides the migration harness this ticket requires.

Choices made during the work, for owner review:
- Identifiers are lower-case hyphenated UUID text. Any other spelling is rejected,
  not normalized.
- Limits use two tables: a category's default limit from a month onwards, and the
  limit set for one month, which wins. A month with neither is unconfigured. This
  answers H01 to H04 without creating a row for every month. SELLO-013 owns the
  commands and how archiving pauses a default.
- A category and its limits share the category's version; limit rows have none.
- Each expense and income has a sequence that is unique in its table and decides the
  order of records on the same day.
- A receipt stores a digest of the submitted input, not the input, and has no link
  to the record it describes, so it can outlive a replacement without keeping erased
  amounts, names or notes. SELLO-012 defines the operation kinds and the digest.
- Room cannot declare value checks, so ranges are enforced by the domain types on
  the way in and the mappers on the way out. No hand-written triggers.
- Android deletes a damaged database file and starts an empty one by default. That
  is removed: the damaged file is kept and the open fails.
- If the database cannot be read at startup the app stops instead of continuing on
  the device zone or an empty database. Showing that as a screen belongs to the app
  shell (SELLO-015); today it is a crash with a clear cause.
- Startup waits for one small read of the profile row, because the financial zone
  must be known before any date is computed.
- An icon key is any well-formed lower-case key; which keys have a drawing is the
  app's concern, so a file from a newer version stays readable.

Left for later, each with its owner: the meaning of receipts and their lookup
(SELLO-012); limit and category commands (SELLO-013); deletion and undo records
(SELLO-019), which will add their own schema version; restore and reset, which
advance the generation and remove old receipts (SELLO-030, SELLO-031). The first
later schema change adds the first real migration and its upgrade test; none was
invented here.

2026-10-09: owner approved pull request 9, including the choices above; moved to Done
for a squash merge.

### Delivery evidence
- **Revision:** branch `sello-011-room-v1`; tested snapshot is commit `0d16435`, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** only MVP tables and no preference in Room → host `ExportedSchemaTest.theSchemaHoldsOnlyTheMvpTables` reads the committed export and expects exactly the seven tables. Unknown stored kind, currency or date is a failure, not a default or empty list → `StrictMapperTest`, which writes damaged rows into a real database and reads them through the real queries: 76 damaged values across expense, income, both limit tables, category and profile, each expected to name its table, row and column, plus three malformed identifiers and `oneDamagedRowFailsTheWholeReadInsteadOfShorteningIt`. Recovery metadata survives replacement → `receiptsAndTheGenerationOutliveTheFinancialRowsTheyDescribe`. Generation and revision initialize atomically, first use is deterministic and the zone persists → `ProfileInitializationTest` (first use, later device zone ignored in the same process and after reopening, 32 concurrent first calls on an unopened file leaving one row and one answer) and app `FinancialStorageOnDeviceTest`. No destructive fallback → `aFileFromANewerSchemaIsRefusedAndLeftExactlyAsItWas`, `anUnreadableFileIsAFailureAndIsNotDeletedOrRecreated` (bytes compared) and host `noSourceAsksRoomForADestructiveFallback`. Storage rejects a foreign category reference and a duplicate operation → `DatabaseIntegrityTest` (T01 and T02 name uniqueness, missing category for expense and limits, no cascade, one limit per month, second receipt refused with the first kept, unique sequence, ordered bounded pages, exact read-back after closing and reopening the file, including 999.999.999.999 and a limit of `Long.MAX_VALUE`). Exported schema validation and migration harness → `ExportedSchemaOnDeviceTest` builds a database from `1.json` alone and opens it with this build. System backup and transfer off before any financial data → app `BackupDisabledTest` on the installed app, and the manifest of both built APKs.
- **Red / Green:** the 25 `:data` device tests first ran against a naive version (no foreign keys or unique indexes, receipts keyed by row number, profile written with replace, mappers that check nothing, read an unknown currency as COP and drop a row they cannot read, and Android's default handling of a damaged file): 22 failed for those reasons and 3 passed (ordered pages, first use, newer schema refused), then all 25 passed with the real implementation. One test was wrong in that run: its "upper-case" identifier had no letters, so it was not malformed; it now uses one that is. The 6 new `:app` device tests failed before the startup and manifest change (3 because the database did not exist yet, 3 on backup) and pass after. The domain tests for identifiers, icon keys and limits were written with the code. Twenty mutations of the finished code were then run: 19 failed the expected tests (page order, inclusive end date, repeated last row, unchecked name key, archived flag, note, currency, version, unlimited with an amount, two profile rows, zone spelling, revision bound, cascade, uncaught newer schema, replace on profile, default damage handling, icon length, zero limit, upper-case in the last identifier block). One survived, upper-case accepted in the first identifier block; the test gained a case per block and now fails it. All were reverted and the exported schema compared unchanged.
- **Gate results:** local `./scripts/verify-ticket SELLO-011 --gate G2` passed: 120 host tests (49 domain, 34 app, 30 design-system, 4 data, 3 catalog), 95 device tests on an isolated API 30 emulator (25 data, 12 app, 48 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 24 warnings, all in `:app`: the 22 present before plus two more "newer version available" notes, one of them for the new `room-testing` entry. `data/gradle.lockfile` gained `room-testing` 2.8.4 for the device-test classpaths only; no other lock file changed.
- **Quality run:** run 20261010T001629Z-1cb139c5; SELLO-011 G2 passed; HEAD 0d16435, inputs sha256 e6cf535e78ad; report docs/planning/mvp/quality-reports/SELLO-011.json sha256 68bc68a1a2a5be4e916ed5643f0ccd520cd54450ae74fb02ac7094295cfcc18b
- **Device / Artifact:** the debug app was installed fresh on the API 30 emulator and started: `sello.db` was created at version 1 with the seven tables and one profile row, `America/Bogota`, generation 1, revision 0. The app was then force-stopped, the emulator's zone changed to `Asia/Tokyo`, and the app cold-started again: the row was unchanged and still single. The emulator's zone was restored. `bmgr backupnow` for the app answered "Backup is not allowed". `aapt2` shows `allowBackup=false` with both rule files in the debug and release APKs. Hosted run 38008531479 on the pull request's merge commit passed the `quality` check with the ticket in Review.
- **Review:** executor self-review of the diff, exported schema, reports and logs. Project owner reviewed pull request 9 and approved on 2026-10-09, including the listed choices. This is owner acceptance, not an independent technical review; GitHub does not let the account that opened a pull request approve it.

## SELLO-012 — Save expenses with atomic receipts and uncertain-outcome recovery

- **Type:** Story
- **Priority:** P0
- **Status:** Done
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

### Execution progress
2026-10-09: started on branch `sello-012-expense-commands` after SELLO-011 merged. No
product decision or dependency was needed, and the schema did not change.

Delivered. `:domain`: `OperationId`, `OperationKind`, `OperationReceipt`,
`CreateExpense`, the `ExpenseCommands` port with typed `Committed`, `Rejected` and
`OutcomeUnknown`, and `RecordIdSource`. `:data`: `RoomExpenseCommands`, the input
digest and the strict receipt mapper. `:app`: a random identifier source and the
workflow bound in the graph; no screen calls it yet. `docs/development/commands.md`
states the protocol the next commands reuse.

Choices made during the work, for owner review:
- The order inside the transaction is generation, replay, validation, write. A
  command prepared against a replaced history is refused even if it once committed;
  its original result stays available through lookup.
- Replaying a committed operation returns its receipt even if the category has been
  archived since, because nothing new is written.
- Once the transaction starts it always finishes, and the caller is checked for
  cancellation before and after. A cancelled caller always gets the cancellation,
  never a result.
- A database error before the commit is `Rejected`, because the rollback is certain.
  Only an error from the commit itself is `OutcomeUnknown`.
- The store creates the expense's identifier; the caller supplies only the operation's.
- The input identity is a SHA-256 of the meaningful fields, so a receipt holds no
  amount or note.
- No totals are checked when saving: a month would need over nine million maximum
  records to exceed what a total can hold. Totals are checked where they are computed.
- `Committed` carries no follow-up state because nothing follows a save yet.

Left for later: the form, drafts and success stamp (SELLO-017); category and limit
commands (SELLO-013); edit, delete and undo (SELLO-019); the sandbox scenario that
replays expenses through this workflow (SELLO-024).

2026-10-09: owner approved pull request 11, including the choices above; moved to Done
for a squash merge.

### Delivery evidence
- **Revision:** branch `sello-012-expense-commands`; tested snapshot is commit `d6beb36`, which is the implementation commit `aecf2ce` with `main` merged in after the SELLO-044 roadmap ticket landed, with only this ticket's status and evidence text, the retained report and regenerated board views added afterwards, all of which the input fingerprint excludes by design.
- **Requirement mapping:** all in `ExpenseCommandContractTest` on a real database file, each case reading the stored rows, receipt count and revision itself. Identical operation and input creates one expense and returns the original receipt → `submittingTheSameOperationAndInputAgainReturnsTheOriginalAndWritesNothing` and `manyIdenticalSubmissionsAtOnceSaveOneExpense` (32 at once: one written, 31 replays). Changed input under the same operation conflicts → `reusingAnOperationWithAnyDifferentInputIsAConflictAndChangesNothing` (amount 12.000, date, note, no note, category). Stale generation, missing and archived category reject → `aCommandPreparedAgainstAReplacedHistoryIsRejected`, `aMissingCategoryIsRejectedAndNothingIsWritten`, `aCategoryArchivedAfterItWasChosenIsRejectedInsideTheTransaction`. A failed transaction leaves row and receipt absent → `aRealConstraintFailureLeavesNeitherExpenseNorReceiptNorRevision`, a real primary-key failure after the revision was already advanced inside the transaction. A failure at commit is not reported as "not saved" → `aFailureAtTheCommitItselfIsReportedAsUnknownAndRecoveredByLookup`, a real refusal by the database at commit. Cancellation propagates and the result is recoverable with the same operation → `aCallerCancelledBeforeSubmittingWritesNothing` and `aCallerCancelledWhileSavingGetsTheCancellationAndCanRecoverTheSavedExpense`. Receipt time is audit time, not sandbox time → `theDateLimitIsTheFinancialDayNotTheRealClock`, with the financial day in December and the receipt stamped in October. Survives reopening → `theReceiptAndReplaySurviveClosingAndReopeningTheFile`. Canonical identity → two digests worked out outside the app are asserted exactly. Strict receipt and row decoding → `aDamagedReceiptIsAFailureForBothSubmissionAndLookup` (8 damaged values) and `aDamagedProfileOrCategoryRejectsTheCommandWithoutWriting`. No network, file, DataStore or notification work in the transaction → the transaction body calls only DAOs, the two clocks and the identifier source. Workflow bound in the app → `FinancialStorageOnDeviceTest.theExpenseWorkflowIsWiredToTheRealDatabaseAndAnswersWithoutWriting`.
- **Red / Green:** the 18 contract tests first ran against a naive version (no transaction, no replay check, no validation, every exception including cancellation turned into a rejection): 14 failed for those reasons and 4 passed (first save, save without a note, second operation's sequence, and the constraint case, which a naive insert also refuses). With the workflow implemented all 18 pass. Twenty-one mutations of the finished code followed. Twenty failed the expected tests: no transaction, cancellable transaction, result handed to a cancelled caller, no check before starting, commit failure reported as rejected, every failure reported as unknown, generation unchecked, replay ignoring input, replay never recognised, archived accepted, date limit off by one, revision not advanced, constant sequence, digest without amount, note flag, date, category or field lengths, receipt kind unchecked, receipt digest unchecked. One survived: not truncating timestamps to milliseconds changed nothing, because storing already does; that line was redundant and was removed.
- **Gate results:** local `./scripts/verify-ticket SELLO-012 --gate G2` passed, and passed again with the same counts after `main` was merged in (the earlier run 20261010T035429Z-e7842566 on `aecf2ce` became stale when the roadmap ticket changed tracked inputs): 121 host tests (49 domain, 35 app, 30 design-system, 4 data, 3 catalog), 114 device tests on an isolated API 30 emulator (43 data, 13 app, 48 design-system, 10 catalog), ktlint, architecture rules, lint with 0 errors and 24 warnings, all in `:app` and present before this ticket. No dependency, lock file or schema changed.
- **Quality run:** run 20261010T043327Z-dc3bce9e; SELLO-012 G2 passed; HEAD d6beb36, inputs sha256 d6f2a229a17b; report docs/planning/mvp/quality-reports/SELLO-012.json sha256 41f2bc23fcba4fd3bd6d0209ea9597564646bb6277d1adb0a997abcb27a8052c
- **Device / Artifact:** no visible change; no screen calls the workflow yet. The app's device tests confirm it starts with the workflow bound to its real database and no record written. Hosted run 38022319554 on the pull request's merge commit passed the `quality` check with the ticket in Review.
- **Review:** executor self-review of the diff, reports and logs. Project owner reviewed pull request 11 and approved on 2026-10-09, including the listed choices. This is owner acceptance, not an independent technical review; GitHub does not let the account that opened a pull request approve it.

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
- Historical months resolve defaults effective then, including unopened/skipped months;
  explicit monthly overrides win. Snapshot creation is idempotent/atomic and independent
  of app-open timing; no backward extrapolation before known configuration.
- Approved D07 archives preserve the current monthly limit, exclude automatic budgets
  in following archived months and retain rows in periods with saved limits/expenses.
  Same-month unarchive preserves its limit; later unarchive preserves any explicit
  period override, otherwise uses the last configured default. Rename keeps identity.
- Stale versions/missing rows report conflicts; failed edits do not partially
  mutate budgets or revision. First category creation needs no sample data.

### Tests
Real-Room command races/rollback/missing rows, rename/archive retention, multiple
month rollover, unlimited/zero limits and prior-month stability. Run G2.
Cover approved H03/H04: October default 100,000, unopened November, December default
120,000 → November limit 100,000; backdated November expense 10,000 → remaining
90,000. Reopen/repeated materialization preserves that history and explicit overrides.
Cover AR01–AR06 archive/unarchive/history/override cases on real Room, including
zero-expense saved-budget visibility and no retrospective budget after restoration.

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
- Retain dated financial facts and per-month configured limits needed by MVP;
  future signed carryover must not require reconstructing history from today's
  default. ADR 0002 records that direction, not permission for carryover schema now.

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
- Approved B08: a category deficit does not zero still-positive overall allowance.
  On Oct 22, three limits of 100,000 with one category spending 110,000 yield
  overall remaining 190,000 and daily allowance 19,000; preserve category deficit
  −10,000/Pasado without silently reallocating limits. Test category/global scope separately.
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
- Approved D03 rounds only the displayed daily planning allowance down to COP 50
  multiples: 3,529 → 3,500; 3,564 → 3,550. Cover A01–A08 from the versioned corpus,
  including exact 50/100 boundaries and positive sub-50 allowance without loss ink.
  Preserve transaction/remaining-budget precision and exact forecast verdict comparisons.
- Concurrent expense and limit edit cannot show old budget with new spend in one
  snapshot. Last-good data from another month/date must be labeled with its source.
- Unrepresentable report totals fail explicitly; SQL SUM promotion or Float conversion
  cannot rescue them by inventing a different amount.
- Keep configured limit, recorded spending and derived allowance distinct in the
  domain snapshot. Future signed carryover has a separate base/carry/capacity meaning
  (ADR 0002); do not hard-code discarded history or add dormant carryover modes.

### Verification recipe
Create/run `./gradlew :domain:test --tests '*MonthlyBudgetPolicyTest'` and real-Room
`MonthlySnapshotConsistencyTest`, then G2. Include approved day-cutoff/rational-rounding
examples, first/subsequent read faults and observable clock-triggered recomputation.
