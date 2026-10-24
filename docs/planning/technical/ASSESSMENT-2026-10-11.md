# Sello — Technical risk reassessment after SELLO-018

- **Status:** Proposed technical backlog; no fixes implemented or approved here.
- **Assessed:** 2026-10-11, America/Bogota.
- **Revision:** `7f5c7ca45e810da3411638d58619ed0453b08a45`; working tree was clean before this document.
- **Completed slice:** SELLO-018 is recorded Done in its canonical epic. Implementation is in `3119832`; delivery evidence is in `7f5c7ca`.
- **Goal:** Preserve the financial foundation while addressing concrete boundary, availability and presentation-scaling risks.
- **Exit:** Adopted findings have meaningful regression coverage and executed verification; deferred concerns remain explicit rather than becoming speculative architecture.
- **Authority:** [Architecture](../../../ARCHITECTURE.md), [execution guide](../../development/execution-guide.md), [quality flow](../../development/quality-gates.md), and [SELLO-018](../../tickets/E04-first-slice/SELLO-018/ticket.md).

## Assessment outcome

The completed slice strengthens the product: real Recibo figures, category detail,
bounded database pages, accessible amounts, adaptive selection and restoration now
have delivered code and retained G2 evidence. Exact financial policies, transactional
commands, strict storage decoding, composition-only data access and shared render
mapping remain sound foundations. This is not a recommendation to rewrite the app.

The earlier concerns were checked against the finished implementation, not assumed
to remain because they appeared in an earlier review. The risks below still have
specific code paths. Performance impact is not benchmarked; source-level mechanisms
and executed checks are distinguished throughout.

### Confidence and ordering

**Confidence** is an uncalibrated reviewer estimate that the stated issue will occur
**if the ticket's trigger occurs**, with the current implementation unchanged. It is
not a measured probability that a user encounters it, a security score, or a forecast
over a specified number of months. For example, counter overflow has high conditional
confidence but is extraordinarily unlikely through ordinary manual use.

Tickets are sorted by confidence descending; ties use impact. **Priority** is separate:
P1 means important integrity/availability or first-line performance hardening; P2
means robustness/efficiency work. Execution need not follow numbering: prerequisites,
real-world exposure and measurements determine adoption order. Reassess percentages
when reproduction or performance evidence becomes available.

| Ticket | Confidence | Priority | Trigger and remaining risk | Evidence strength |
| --- | --- | --- | --- | --- |
| SELLO-TECH-BUG-001 | 99% | P1 | Financial profile open fails; startup throws before a recovery screen exists | Direct startup call-path inspection |
| SELLO-TECH-BUG-002 | 99% | P1 | A command receives a negative-year date; accepted input cannot round-trip through storage | Executed domain probe plus writer/decoder inspection |
| SELLO-TECH-BUG-003 | 99% | P2 | A persisted counter is `Long.MAX_VALUE`; checked increment escapes the command outcome contract | Executed arithmetic probe plus exception-path inspection |
| SELLO-TECH-BUG-004 | 95% | P2 | Recibo and detail observe the same month; independent snapshot pipelines repeat work | Direct cold-flow and collector inspection; cost unmeasured |
| SELLO-TECH-BUG-005 | 90% | P1 | A large month refreshes; snapshot computation can occupy the main thread | Direct execution-context inspection; jank unmeasured |
| SELLO-TECH-BUG-006 | 85% | P2 | Many history pages are loaded; all loaded rows remain eagerly composed | Direct rendering inspection; device threshold unmeasured |
| SELLO-TECH-BUG-007 | 80% | P2 | A revision changes after many pages are loaded; refresh rereads the loaded window and retains unversioned rows | Direct refresh-path inspection; race/cost not reproduced |

### Registration and execution boundary

This document is the source for the **requested technical findings**, not a new MVP
epic inside the board generator. It does not change any existing ticket status,
acceptance, dependencies, generated export or approval. All findings are Backlog.

The current runner recognizes canonical MVP/roadmap IDs, not these new technical IDs.
The G2 entries below are **required verification targets, not configured gates**.
Before implementation, the owner must approve how an adopted technical ticket is
registered with the canonical board/runner or explicitly incorporated into an
existing owning ticket. Do not run an unrelated ticket to claim a technical ticket
passed, manually downgrade a gate, or mark these tickets Review/Done using this
assessment. Runner/board support is a separate explicit workflow decision.

Every implementation follows the existing quality flow: meaningful red→green tests,
focused verification, its approved runner mapping and fresh retained evidence, then
designated review. This assessment is executor self-review, not independent approval.

## SELLO-TECH-BUG-001 — Reach a safe recovery screen when storage startup fails

- **Type:** Bug
- **Priority:** P1
- **Status:** Backlog
- **Confidence:** 99%, conditional on profile establishment returning a failure.
- **Depends on:** SELLO-004, SELLO-011, SELLO-015, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2; coordinate recovery UX with SELLO-030/034 without duplicating their deliverables.

### Outcome
A damaged or unavailable financial store preserves its files and produces an
actionable startup failure, rather than preventing the person from reaching any UI.

### What is happening and how
`SelloApplication.onCreate` calls `openStorage` before creating the application graph.
`openStorage` waits in `runBlocking` for profile establishment, closes storage on a
typed failure and throws `FinancialStorageUnavailable`. No application-level failure
state catches that exception before startup exits. Database work uses an I/O context,
but the main startup thread still waits for its result.

Evidence: `app/src/main/java/com/software/sello/composition/SelloApplication.kt:16`;
`app/src/main/java/com/software/sello/composition/StorageModule.kt:38`.

### Future consequences
An unreadable profile, damaged database or persistent open error leaves the user in
a repeatable launch failure without explanation or retry/recovery access. Slow disk
work also extends main-thread startup blocking; no ANR was measured here. Failing
closed protects truth, but it is not a complete recovery experience.

### Deliverables and recommended solution
Introduce explicit initializing/ready/failed startup presentation. Establish storage
asynchronously under an owned scope; initialize the financial graph only after the
persisted profile is available. Show safe retry and the approved recovery guidance
on failure. Keep required collaborators and composition ownership; do not introduce
global service lookup or default financial services to make the error screen work.

### Acceptance criteria
- Failure reaches a visible, accessible explanation without terminating startup.
- Retry can recover a transient fault; repeated retries do not open competing stores.
- No empty database, device-zone substitute, destructive fallback or mutation occurs.
- Financial destinations remain unavailable until the actual profile is ready.
- Rotation/backgrounding do not start duplicate initialization; failed files survive.

### Tests and verification recipe
First reproduce failed initialization and assert a visible failure instead of an
uncaught startup exception. Use controlled initialization fakes for lifecycle/retry
tests and real database files for corrupt/newer-schema preservation. Execute launch,
retry, normal save/read and restoration journeys on API 30 and the affected supported
API. Run the approved G2 mapping; assembly alone does not prove startup handling.

### Working checkpoint and executor brief
The current create→receipt→Recibo journey remains usable. Own app composition/startup
presentation; reuse storage ports and coordinate recovery actions with their existing
owner. No schema, permission, backup policy or signing change is authorized by this fix.

## SELLO-TECH-BUG-002 — Reject effective dates that the storage codec cannot read

- **Type:** Bug
- **Priority:** P1
- **Status:** Backlog
- **Confidence:** 99%, conditional on a negative-year `LocalDate` reaching a command.
- **Depends on:** SELLO-004, SELLO-010, SELLO-012, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2.

### Outcome
Every accepted command date/period round-trips through the strict persisted format.

### What is happening and how
`EffectiveDates.forManualEntry` rejects only dates after today. A value such as
`LocalDate.of(-1, 1, 1)` is accepted. The expense writer persists `date.toString()`
(`-0001-01-01`), while `EffectiveDates.parseDate` accepts only four unsigned year
digits. A by-ID decode therefore rejects the row the command was allowed to write.
ISO-text month queries can also exclude that date instead of surfacing it.

Evidence: `domain/src/main/kotlin/com/software/sello/domain/policy/EffectiveDates.kt:20`;
`data/src/main/java/com/software/sello/data/repository/RoomExpenseCommands.kt:87`;
`data/src/main/java/com/software/sello/data/mapper/RecordMapper.kt:34`.

The executed pure-domain probe returned `Success(value=-0001-01-01)` for manual
validation and `Failure(error=Malformed(original=-0001-01-01))` for storage parsing.
No negative-year database write was executed. No ordinary UI/deep-link path producing
such a date was identified; this is a command-boundary defect, not a claimed exploit.

### Future consequences
Future adapters, synthetic clocks or incorrectly validated import callers can commit
an expense with a receipt that cannot be read back normally. It may disappear from
period queries and make future portable validation fail. The analogous month grammar
must be reviewed at month-limit/selection boundaries, not merely fixed in a picker.

### Deliverables and recommended solution
Validate canonical date/month representability at domain and command boundaries,
using the actual approved storage grammar. Return a typed invalid-period rejection
before writing. Do not invent a 2000–2099 restriction, repair the date, or relax the
decoder to accept a different portable format. Preserve historically valid dates.

### Acceptance criteria
- Every accepted effective date/month parses back to the identical value.
- Negative years and other noncanonical period representations are rejected before writes.
- A rejected command leaves expense, receipt, category configuration and revision unchanged.
- Valid older dates, leap days and the current financial-day cutoff keep existing behavior.
- Invalid format and future date remain distinguishable, with accurate UI error mapping.

### Tests and verification recipe
Add a red domain round-trip case using `-0001-01-01`, then tests at accepted grammar
boundaries. Add real-Room expense/month-limit command rejection tests and an unchanged
rows/receipts/revision assertion. Retain existing strict-mapper and future-date tests.
Run focused domain and Room suites, then the approved G2 mapping.

### Working checkpoint and executor brief
Own effective-period policies and affected command rejections; inspect existing
callers and presentation mapping before changing signatures. No schema change is
expected. If one is proposed, require separate portable-format/migration review.

## SELLO-TECH-BUG-003 — Return a typed rollback outcome when counters overflow

- **Type:** Bug
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 99%, conditional on incrementing an exhausted persisted counter.
- **Depends on:** SELLO-004, SELLO-012, SELLO-013, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2.

### Outcome
Checked counter exhaustion does not escape the financial command result protocol.

### What is happening and how
Revision, expense sequence and category version increments use `Math.addExact`.
The mappers admit nonnegative/positive `Long` counters including `Long.MAX_VALUE`.
`runCommand` catches SQLite and illegal-state failures, but not the resulting
`ArithmeticException`. Room rolls back the transaction; the arithmetic exception
escapes instead of becoming the command's typed rejection. An unhandled ViewModel
call can consequently lose normal failure presentation or terminate the process.

Evidence: `data/src/main/java/com/software/sello/data/repository/CommandProtocol.kt:45`
and `:125`; `data/src/main/java/com/software/sello/data/repository/RoomExpenseCommands.kt:114`;
`data/src/main/java/com/software/sello/data/repository/RoomCategoryCommands.kt:141`.

An executed JDK probe confirmed `Math.addExact(Long.MAX_VALUE, 1)` throws
`ArithmeticException`, not `IllegalStateException`. A real-Room exhausted-counter
command was not executed. Natural exhaustion through manual use is not a credible
near-term scenario; synthetic/corrupt/future imported metadata is the exposure.

### Future consequences
Boundary fixtures or admitted extreme metadata can break the otherwise deliberate
Rejected/Committed/OutcomeUnknown contract. This is a robustness failure, not evidence
of wrapped money or a partially committed transaction.

### Deliverables and recommended solution
Handle audited counter exhaustion explicitly, either before mutation or inside the
transaction with a narrow typed rollback mapping. Preserve checked increments and
rollback; never wrap, saturate, reset counters or blanket-catch programming errors.
Classify a known pre-commit arithmetic rollback separately from an uncertain commit.

### Acceptance criteria
- Exhausted revision, sequence and record version return a typed rejection.
- Real Room proves no partial category, expense, receipt or revision mutation.
- A counter at `Long.MAX_VALUE - 1` can advance once; the following change is rejected.
- Cancellation is rethrown and uncertain-commit lookup retains its existing behavior.

### Tests and verification recipe
Seed each counter independently in an isolated real Room fixture, execute the command
and first demonstrate the escaping exception. Assert the corrected typed result and
all stored rows, not only a mocked DAO call. Repeat duplicate/cancellation/commit-failure
contract tests, then the approved G2 mapping.

### Working checkpoint and executor brief
Own the command protocol and affected command validations. Resolve the precise typed
failure at that boundary; no new money policy, schema or metadata-reset behavior.

## SELLO-TECH-BUG-004 — Avoid duplicate monthly snapshot pipelines in simultaneous views

- **Type:** Technical debt / efficiency risk
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 95%, conditional on simultaneous consumers of the same month.
- **Depends on:** SELLO-004, SELLO-014, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2; coordinate measurements with SELLO-035.

### Outcome
Consumers displaying the same month reuse one bounded, lifecycle-owned snapshot
observation instead of independently rebuilding identical financial results.

### What is happening and how
`RoomMonthlySnapshots.observe` constructs a cold flow. Recibo and detail each invoke
it and each `stateIn` shares only its own invocation. `ReciboDestination` collects
both ViewModels. Every applicable revision/day update can therefore cause two full
month reads/calculations. Active expense entry adds its own observation. Detail also
sorts/formats all categories before finding one category. A singleton repository does
not make those cold-flow invocations share work.

Evidence: `data/src/main/java/com/software/sello/data/repository/RoomMonthlySnapshots.kt:71`;
`app/src/main/java/com/software/sello/feature/recibo/ReciboViewModel.kt:31`;
`app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt:54` and `:99`;
`app/src/main/java/com/software/sello/navigation/SelloApp.kt:156`.

### Future consequences
Expanded layouts and later Resumen/chart consumers can multiply database, allocation
and exact-calculation work per update. Independent transactionally valid reads may
temporarily display different revisions. Neither measurable latency nor an actual
cross-view mismatch was reproduced in this assessment.

### Deliverables and recommended solution
Share observation at an explicit lifecycle/session boundary for consumers of the
same month, retaining generation/revision/as-of identity and consumer retry semantics.
Keep other months independent; bound retained observations and stop them when unused.
Map the selected category directly where full-list formatting is unnecessary. No
global cache, service locator, second financial policy or generic event bus.

### Acceptance criteria
- Two same-month consumers do not establish two underlying reads per stable update.
- Different-month entry previews are not incorrectly replaced by the selected month.
- Rollover, generation change, retry and failure/last-good state reach every consumer.
- Observation lifetime is bounded; switching months or closing views leaks no readers.
- Both panes render coherent revision/as-of data without UI-side arithmetic.

### Tests and verification recipe
Use a counted underlying source and controlled scheduling to demonstrate duplicate
work first. Test sharing, consumer departure, month changes, simultaneous retry and
failure recovery. Use real Room for save→both panes reconciliation and execute compact/
expanded restoration journeys. Measure large-history work before/after; run approved G2.

### Working checkpoint and executor brief
Own the monthly observation boundary, composition wiring and its consumers. Implement
this separately from dispatcher placement, but preserve the off-main contract if
SELLO-TECH-BUG-005 is already adopted. No feature-module split is required.

## SELLO-TECH-BUG-005 — Move expensive monthly computation off the main thread

- **Type:** Performance risk
- **Priority:** P1
- **Status:** Backlog
- **Confidence:** 90%, conditional on a sufficiently large month refreshing on a constrained device.
- **Depends on:** SELLO-004, SELLO-005, SELLO-014, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2, plus reviewed performance evidence coordinated with SELLO-035.

### Outcome
Exact financial snapshot processing does not occupy the UI thread as history grows.

### What is happening and how
Room runs transactional reads on its configured query context. After that transaction
returns, `readAsOf` directly calls `MonthlyBudgetPolicy.snapshot` on the caller's
context. Recibo collects through `viewModelScope` with no computation-dispatcher shift.
The policy filters/groups expenses and computes widened totals; render mapping then
sorts/formats categories. `MonthReads.pages` bounds individual SQL calls but accumulates
all rows, so pagination does not bound the total in-memory workload.

Evidence: `data/src/main/java/com/software/sello/data/repository/RoomMonthlySnapshots.kt:37`;
`data/src/main/java/com/software/sello/data/repository/MonthReads.kt:27`;
`domain/src/main/kotlin/com/software/sello/domain/policy/MonthlyBudgetPolicy.kt:100`;
`app/src/main/java/com/software/sello/feature/recibo/ReciboViewModel.kt:31`.

### Future consequences
Busy months can delay visible updates or interrupt interaction during save/rollover/
month changes. Repeated pipelines amplify the risk. Main-thread reachability is visible
in source; jank, memory exhaustion and ANRs were not measured and are not claimed.

### Deliverables and recommended solution
Inject the existing computation dispatcher into the owned pipeline and explicitly
place substantial domain processing off main. Keep domain platform-neutral and exact.
Measure allocations as well as execution time; adopt streaming exact aggregation only
if measurements justify it and consistent-revision semantics can be preserved. Never
truncate source history or substitute floating-point totals to make processing faster.

### Acceptance criteria
- A controlled-dispatcher test proves financial computation does not run on UI context.
- Existing worked financial examples remain unchanged, including overflow failures.
- Cancellation/month switching prevents obsolete results from replacing newer state.
- Related rows retain one consistent revision; computation uses captured as-of data.
- Device measurements meet targets approved before the run, not targets invented here.

### Tests and verification recipe
Start with a red execution-context assertion; retain financial policy/snapshot tests.
Use the reviewed SELLO-035 workload/targets, including its proposed 20-category,
10,000-expense profile only once approved. Seed synthetic history through real commands,
record repeated refresh/interaction measurements on the agreed device, and run approved
G2. Passing host tests or a smooth small debug demo is not performance proof.

### Working checkpoint and executor brief
Own snapshot execution placement and minimal composition changes. Coordinate sharing
with SELLO-TECH-BUG-004; no extra dispatcher globals, dependencies or benchmark framework
without explicit approval. Do not redesign domain policy merely to move its execution.

## SELLO-TECH-BUG-006 — Render growing expense history lazily with stable identity

- **Type:** Performance risk
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 85%, conditional on repeatedly loading many expense pages.
- **Depends on:** SELLO-004, SELLO-009, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2, plus large-list evidence coordinated with SELLO-035.

### Outcome
Loading older history does not require composing every loaded expense outside the
viewport, and row/scroll identity remains stable during updates.

### What is happening and how
Category detail uses `Column.verticalScroll`; the expense slip calls
`list.rows.forEach`. The ViewModel retrieves 20 rows per page, accumulates them and
copies the accumulated list for emissions. Query paging is real, but UI composition
is eager: every loaded row participates in the scroll layout. Tests for long lists
prove page access and truthful figures, not viewport-bounded rendering performance.

Evidence: `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailScreen.kt:59`
and `:204`; `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt:119`.

### Future consequences
Deep exploration retains increasing layout/semantics work and repeated list-copy
allocations. Later edit/delete/undo can make stable row identity and scroll anchoring
more important. No specific row-count threshold or measured slowdown is asserted.

### Deliverables and recommended solution
Use lazy rendering for potentially long history, keyed by stable expense ID, while
preserving the approved paper/slip appearance and Sello primitives. Keep bounded reads
and full exact totals. Do not nest an unbounded lazy list inside the current scrolling
column or add a paging dependency automatically. Reassess full-list copying separately
only if measured allocations warrant a different collection strategy.

### Acceptance criteria
- A synthetic long history demonstrates viewport-bounded row composition.
- Every row remains reachable; paging never truncates financial totals or date range.
- New saves and refreshes preserve defined scroll behavior and stable row identity.
- Loading-more/failure/retry, exact spoken amounts and font scale 2.0 remain usable.
- Compact, landscape and expanded layouts preserve the existing inset/Back behavior.

### Tests and verification recipe
Add a device composition-count/visibility regression rather than relying only on text
assertions. Exercise many pages, loading failure, resize, restoration and insertion at
the top. Record scroll/heap evidence with reviewed targets and run approved G2. Use
synthetic isolated data, not personal histories or timing sleeps.

### Working checkpoint and executor brief
Own detail rendering and focused UI tests. Extend design-system/catalog only if a
genuinely reusable primitive is needed; do not copy Slip internals or move feature
financial models into design-system. Coordinate list refresh with the next ticket.

## SELLO-TECH-BUG-007 — Bound history refresh work and identify retained rows as refreshing

- **Type:** Performance / presentation-consistency risk
- **Priority:** P2
- **Status:** Backlog
- **Confidence:** 80%, conditional on revisions changing after a deep history window is loaded.
- **Depends on:** SELLO-004, SELLO-014, SELLO-018; approved technical-ticket gate registration.
- **Verification target:** G2; coordinate future edit/delete/undo coverage with SELLO-019.

### Outcome
Snapshot updates retain useful list continuity without unmarked old/new data mixing
or an unexplained full-window reread cost on every invalidation.

### What is happening and how
Each snapshot emission restarts `list(id, snapshot)`. That flow starts with an empty
accumulator and reads up to `pagesWanted`, so ten previously requested pages can be
read again after a single revision change. The interim Ready state combines the new
category header with `kept` rows identified only by month/category, not revision or
as-of. A previously successful retained list does not acquire a dedicated refreshing
marker in that initial state. History reads themselves carry no snapshot revision.

Evidence: `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt:99`
and `:119`; `domain/src/main/kotlin/com/software/sello/domain/port/ExpenseReads.kt`;
`data/src/main/java/com/software/sello/data/repository/RoomExpenseReads.kt:31`.

### Future consequences
Save/edit/delete bursts can repeatedly rebuild a large loaded window and prolong the
interval in which the header is current but listed rows are from earlier data. Slow
or failed reads make the distinction more important. This is a predicted refresh/race
risk from the code, not a reproduced permanent accounting mismatch. The existing
rebuild strategy deliberately favors simplicity and has useful failure/retry tests.

### Deliverables and recommended solution
Track the identity of the displayed list and publish explicit refreshing/stale state
when retaining older rows. Define revision reconciliation at the read/presentation
boundary; do not calculate totals from pages. Measure reread cost first, then select
a bounded refresh strategy that preserves correctness. If incremental refresh is not
justified, retain rebuilding with explicit progress and a reviewed work budget. Do not
skip invalidations merely because a change looks unrelated, or cache across generations.

### Acceptance criteria
- Retained rows are not presented as fully reconciled with a newer header before refresh.
- A delayed read with an intervening save produces coherent eventual results and no duplicates.
- Category/month/generation changes cannot reuse another history's retained rows.
- First/later page failures remain explicit and retry the intended page.
- Work for a deeply loaded window is counted and measured; adopted bounds never truncate totals.
- Later edits/deletes/undo preserve defined scroll and cursor behavior when those commands ship.

### Tests and verification recipe
Use controlled suspended reads, commit a revision between header and page reads, and
assert interim marking plus final rows/figures. Count page requests after deep loading
and rapid revisions; exercise cancellation and same-page retry. Verify actual Room
reconciliation and device accessibility/progress before approved G2. Do not add future
edit/delete implementations as fixtures before their owning ticket ships.

### Working checkpoint and executor brief
Own detail refresh state and, only if required by the agreed contract, the domain read
port/data adapter. Coordinate with sharing and lazy rendering without one broad UI
rewrite. No schema change or dormant per-category revision infrastructure is assumed.

## Existing release obligations — not duplicate technical tickets

- **Recovery:** manual backup/restore/reset are still owned by SELLO-028–031. OS backup
  remains disabled; do not rely on the current app as the sole copy of irreplaceable data.
- **Privacy/security:** permission minimization, private storage and bounded draft-only
  entry links remain positive controls. SELLO-032 owns file/privacy isolation acceptance.
  No application-level database encryption/app-lock was found; this is a threat-model
  decision, not evidence that device storage lacks OS protection or a new approved feature.
- **Release:** optimization remains disabled in the inspected build; production signing,
  performance and artifact/dependency review remain SELLO-035/036 work, not completed
  merely because debug/G2 succeeds.
- **Supply chain:** central pins, strict resolution locks, a wrapper distribution checksum
  and pinned CI action revisions exist. No dependency-verification metadata was found.
  Review artifact verification with SELLO-035; no vulnerable dependency or exploit was
  established here, and no dependency upgrade is recommended without compatibility review.

## Maintainability watchlist — insufficient evidence for new bug tickets

- **Navigation responsibility growth (40% conditional concern):** the root coordinates
  routes, prerequisites and adaptive assembly, but its current size is not proof of a
  defect. Extract concrete destination responsibilities when new workflows cause coupled
  edits; do not introduce a generic navigation framework or one module per screen now.
- **Form-state combinations (40% conditional concern):** flags/nullable receipt values
  could grow harder to reason about as edit/undo ships. Current render/action guards and
  tests are valuable. Introduce explicit submission phases only when actual transition
  complexity warrants it; do not prebuild a universal form engine.

## Verification and reassessment evidence

### Fresh execution on the assessed revision

No implementation source was changed. The commands below ran on `7f5c7ca`; only this
document is the resulting source addition, left untracked/uncommitted. Build outputs
are generated/ignored.

| Command / check | Actual result |
| --- | --- |
| `python3 -B scripts/quality/architecture.py` | Passed: 221 source files, five module build files |
| `python3 -B docs/planning/mvp/board.py --check` | Passed: ten epics, 44 tickets and generated views/evidence validation |
| `./gradlew :domain:test :app:testDebugUnitTest --rerun-tasks --console=plain` | Passed: 93 domain + 178 app tests; zero failures/errors/skipped tests; 59 tasks executed |
| Pure-domain JShell probe against the freshly compiled domain classes | Negative-year date accepted by manual validation and rejected by storage parsing |
| JDK arithmetic probe | Counter increment throws `ArithmeticException`; it is not an `IllegalStateException` |
| `python3 -B docs/planning/mvp/board.py`, then `--check`, after document creation | Passed; no existing board/export files changed |
| Document structure/link validation | Passed: seven sequential IDs, descending confidence, required ticket sections and existing local link targets |
| `git diff --no-index --check /dev/null docs/planning/technical/ASSESSMENT-2026-10-11.md` | Passed: new-document whitespace check |

Gradle used the documented Android SDK/JBR environment and JDK-25 daemon policy.
The host build emitted one Koin experimental-API warning in `ProductionGraphTest`.
Gradle's Kotlin configuration-error check was marked SKIPPED; Java/resource tasks with
no sources were NO-SOURCE. Neither is a skipped test suite. The probes ran under the
existing JDK 21 JShell; the first JShell attempt failed because the Android Studio
JBR has no `jshell` executable, then succeeded using the installed Gradle JDK 21.
No toolchain/dependency setting was changed.

### Historical evidence, not rerun acceptance

[SELLO-018's retained report](../../tickets/E04-first-slice/SELLO-018/quality-report.json) records run
`20261011T164039Z-ac9f825d`, G2 passed on `3119832`, API-30 emulator, with 29 lint
warnings. Its canonical delivery records 309 host and 260 device tests. The fresh
271 host tests above are a focused subset, not a new integrated G2 run. No hosted
CI, independent approval or merge state was queried or asserted.

### Not executed / still required

No new real-Room defect reproductions, connected device tests, performance benchmarks,
full lint/assembly gate, vulnerability scan, customer-artifact inspection, production
signing or G3 ran during this reassessment. The startup and paging race/performance
claims are code-derived with explicit triggers. This document is not release acceptance
or a substitute for each adopted ticket's red→green and retained quality evidence.
