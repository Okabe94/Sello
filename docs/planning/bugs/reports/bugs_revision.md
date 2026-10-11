# Sello — revised bug inventory and proposed tickets

**Prepared:** October 10, 2026 (America/Bogota)  
**Input:** `/home/okabe94/bugs.md`  
**Repository:** `/home/okabe94/AndroidStudioProjects/Sello`  
**Scope:** read-only source review, synthetic emulator exploration, and ticket drafting. No project source, acceptance criteria, canonical tickets, or board statuses were changed.

## Recommendation and registration rules

Create small, independently verifiable fixes, not one omnibus stabilization ticket and not a minimum of two bugs per ticket. Reports 5 and 7 belong together because they concern the same pinned expense-entry control layout. Keep the title-loading defect separate from general transition work.

The seven drafts use the owner-requested bug namespace **SELLO-BUG-001–007**, numbered separately from feature tickets. These IDs are assigned in this document only; they have not been registered in canonical epic files or hosted issue trackers. They remain MVP fixes/improvements.

Recommended canonical home: `docs/planning/mvp/epics/E04-first-slice.md`, subject to owner approval. Do not create another epic merely to hold these findings. The current board validator accepts **Task and Story, not Bug** (`board.py`, type validation). These drafts therefore use **Type: Task** and separate Classification metadata. This avoids an incidental workflow/tooling change just to introduce a Bug issue type. Separately, the current board parser and quality tooling expect IDs in the `SELLO-###` format and do not yet support `SELLO-BUG-###`. Supporting this namespace in canonical planning and verification requires a separately authorized tooling change; this document update does not make those tools compatible.

All drafts remain **Backlog**, with **G2** and the mandatory SELLO-004 dependency. The shared financial domain, schema, dependency pins, operation protocol, and time contracts are outside their write scope unless a newly demonstrated root cause requires an explicitly approved scope change.

SELLO-018 is still Backlog in the canonical epic, despite substantial uncommitted implementation. Findings in that unfinished capability, particularly SELLO-BUG-001, should be resolved within SELLO-018 before acceptance when practical. The standalone drafts provide a usable backlog form if the owner chooses separate delivery; their SELLO-018 prerequisites must be approved/merged before independent dependent implementation. Do not call the current working tree an accepted release or reopen completed tickets merely to accommodate a new UX preference.

Before registering: approve the UX choices identified below, add validated board/quality-tool support for the new bug namespace, confirm ID uniqueness, place the tickets in the canonical epic, then regenerate/check board views. Before a later fix is proposed Done: meaningful focused red→green tests, `./scripts/verify-ticket <actual-registered-ID> --retain`, fresh Quality run evidence, canonical Delivery evidence, generated-view checks, and designated acceptance through the PR merge. Do not run the proposed IDs through the runner before registration or fabricate a Quality run for this drafting exercise.

## Tested baseline and evidence

- HEAD: `53533bf34f15cfddb8b37966e9d5386f36a25c4b`, plus the existing 41 modified/untracked Git-status entries. This was a **dirty working-tree build**, not a tested clean commit.
- Source manifests before/after exploration are byte-identical. Manifest SHA-256: `0204663f0d60f6d1bc84c5a174c551849b0d864462dccc9e5123e5806d58473f`. Manifest coverage: Git-tracked and nonignored untracked files. Ignored build outputs are excluded.
- Debug APK SHA-256: `f9c5be3d6dcd61499aae97ec1253145724d75ee6a50283cfa555a77886ddd8e7`.
- Fresh, throwaway AVD `sello_bug_exploration`, private AVD directory `/tmp/sello-bug-exploration-avd.VX6bom`, serial **emulator-5582**, Android API 30 / Android 11. All agent-issued device commands explicitly targeted that serial; the existing emulator-5580 and physical device were not used for exploration.
- Primary evidence configuration: 1080×2340 px, density 440 dpi, font scale 1.0, all three animation scales 1.0. Financial date October 10, 2026. A limited font-scale-2.0 expense-screen pass was also captured.
- T3 device access was disabled, so local SDK emulator/ADB tools were used. An initial short run used a smaller display and was interrupted; files 00–02 are preliminary, **not** the primary reproduction baseline. The restarted AVD was launched with `-skin 1080x2340`; primary evidence starts at file 03.
- Evidence root: `/home/okabe94/bugs_revision_evidence/20261010/`. Screenshots, XML accessibility hierarchies, MP4 recordings, copied synthetic database snapshots, source manifests, build log, and focused test results are retained there.

### Verification actually executed

```bash
export ANDROID_HOME=/home/okabe94/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew :app:assembleDebug --console=plain
./gradlew :app:testDebugUnitTest \
  --tests '*CategoryEditorViewModelTest' \
  --tests '*MonthSessionTest' \
  --tests '*NavigationStateTest' \
  --tests '*ReciboRenderModelTest' --console=plain
```

Assembly passed (75 tasks up-to-date). Focused host tests **executed and passed: 72 tests, zero failures/errors/skips**: CategoryEditorViewModelTest 26, MonthSessionTest 14, NavigationStateTest 21, ReciboRenderModelTest 11. Passing existing tests does not mean the reported defects have regression coverage.

Manual exploratory checks used the real app screens, validated commands and Room storage:

| Check | Observed result | Evidence under evidence root |
| --- | --- | --- |
| Empty install → category form | Unlimited selected; disabled amount field and active suggestions remain visible | `03-baseline.png`, `04-category-default.png/.xml` |
| Suggested and manually entered limit | Suggestion 500.000 renders field text `500000`; manual 400000 renders `400000` | `05-category-suggestion.png/.xml`, `06-category-manual.png/.xml` |
| Save finite category | Synthetic `Exploracion` category has limit 400.000; real Recibo displays it | `07-created.png/.xml` |
| First category-detail entry | Header briefly says Categoría before Exploracion | `08-navigation.mp4`, `08-detail-entry-frames.png`, `08-detail.png/.xml` |
| Dock and month navigation | Category switch is immediate; dock uses a plain fade; month content replacement lacks the reference's directional slip transition | `08-navigation.mp4`, `23-month-change.mp4`, `23-month-change-contact-sheet.png` |
| First amount digit | Keypad shifts down 65 px; submit stays fixed | `09-expense-empty.png/.xml`, `10-expense-first-digit.png/.xml`, `10-keypad.mp4` |
| Tap a previously valid key position | After entering 1, tapping the old 7 target at (190,1805) enters 4: draft becomes **14**, not intended **17**; not submitted | `16-old-key-position.png/.xml` |
| Historical month and previous-year picker | No quick current-month action; September selection is announced as Mes actual: Septiembre 2026 although financial current month is October | `12-month-picker.png/.xml`, `13-past-month.png/.xml`, `14-previous-year-picker.png/.xml` |
| Invalid deep link `sello://anotar?monto=1e3` | Opens empty amount, does not turn it into 13 or create an expense | `15-invalid-deep-link.png/.xml`, final database |
| Background process death with draft 14 | Successful second attempt: Home → verified launcher resumed → `am kill` → verified old process absent → cold reopen, new PID, draft remains 14 | `19-process-restoration.txt`, `19-restored-draft.png/.xml` |
| Large-font expense view | Amount and submit control present at 2.0; upper draft is scrollable. This was a limited pass, not complete accessibility acceptance | `18-large-font.png/.xml` |
| Deliberate double submit of 12.500 | One new expense. Final stored expenses are exactly 1 and 12,500 COP; count 2, total 12,501; Recibo remaining 387,499 from limit 400,000 | `20-before-double-submit.png/.xml`, `21-double-submit-receipt.png/.xml`, `22-final-recibo.png/.xml`, `db-final/` |

The first process-kill attempt (`17-process-restoration.txt`) retained the same PID and was a hot return, **not process-death evidence**. Only the verified second attempt above supports restoration. The draft 14 was deliberately cleared before the later 12,500 submission; it was never persisted.

**Limits/warnings:** no code fixes or new automated regression tests were written. G2/G3, connected test suites, release artifact inspection, TalkBack/Switch Access, landscape/expanded layouts, all themes, font scale 1.3, and reduced-motion navigation were not executed. Emulator logs contain startup `adb -e` ambiguity warnings because another emulator was present; exploration commands used `-s emulator-5582`. No crash or duplicate persisted expense was observed in the bounded checks; this is not proof that none exists elsewhere. General intermittent flashing beyond the recorded title/loading switch remains unconfirmed.

## Inventory and ordering

| Proposed ticket | Source report | Classification | Priority | Suggested order |
| --- | --- | --- | --- | --- |
| SELLO-BUG-005 | 5 + 7 | Reproduced input-layout defect + submission-separation UX risk | P1 | First: can change intended input |
| SELLO-BUG-004 | 4 | Reproduced amount-readability issue | P2 | Category-form pass, coordinated with SELLO-BUG-003 |
| SELLO-BUG-007 | New exploration finding | Reproduced incorrect accessibility wording | P2 | Small independent correction |
| SELLO-BUG-001 | 1 | Reproduced transient title defect | P2 | Prefer closing inside unfinished 018 |
| SELLO-BUG-003 | 3 | Reproduced current behavior; requested UX change | P2 | After owner confirms defaults/draft behavior |
| SELLO-BUG-006 | 6 | Reproduced absence; requested navigation improvement | P2 | After owner confirms action placement |
| SELLO-BUG-002 | 2 | Reproduced motion inconsistency; broad flashing claim only partly confirmed | P2 | After title/input correctness |

Implement sequentially. SELLO-BUG-003 and SELLO-BUG-004 share category-editor files; SELLO-BUG-001, SELLO-BUG-002, SELLO-BUG-006 and SELLO-BUG-007 overlap shell/composition/presentation files. Coordinate their order rather than assigning overlapping parallel writes. No minimum bug count is imposed.

## SELLO-BUG-001 — Keep category-detail identity stable while data loads

- **Type:** Task
- **Classification:** Bug; original report 1
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-018, SELLO-015, SELLO-004
- **Gate:** G2

### Outcome
Entering a known category does not briefly present a different generic or previous-category identity.

### Deliverables
Selection-aware title presentation and focused asynchronous-load/navigation regression coverage in the Recibo/detail feature and shell. No schema changes.

### Reproduction and evidence
On baseline Recibo, tap Exploracion. `08-navigation.mp4` and `08-detail-entry-frames.png` show Categoría during loading, followed by Exploracion. The contact sheet includes resampled frames 46–49 with the fallback, before frame 50 shows the name; sampling is not a frame-perfect duration measurement.

Expected: stable selected-category identity, or a neutral loading treatment when identity is genuinely unavailable; no wrong-category title.

### Acceptance criteria
- Known selected category name remains consistent through initial load, refresh, and retry.
- Switching A → B never renders A's name as B's header, including slow/failing reads.
- Unknown/missing identifiers have truthful loading/error/not-found treatment, not an invented name.
- Rotation/restoration and compact versus list/detail layouts preserve selected identity; title handling does not turn failed financial reads into success.

### Tests
Controlled delayed and failed reads, rapid category changes, restored selection, compact UI and expanded selection tests. Check the header before releasing the controlled read, not only its final text.

### Working checkpoint
Recibo → detail → Back works without the recorded title substitution; financial and stale/error states remain truthful.

### Context and starting points
`app/src/main/java/com/software/sello/navigation/SelloApp.kt`, ReciboDestination: title comes only from `CategoryDetailState.Ready`; otherwise it immediately uses `category_detail_title_fallback`. Selection is sent through a LaunchedEffect. Inspect `app/src/main/java/com/software/sello/feature/recibo/CategoryDetailViewModel.kt`, its state/tests, and SELLO-018's actual contracts. These are presentation boundaries, not financial title calculations.

### Implementation plan
1. Add a meaningful red test for header identity before the detail read completes.
2. Bind title identity to the selected ID and available presentation metadata; retain ID-only navigation and reject mismatched stale state.
3. Define unavailable-identity loading/error behavior without concealing read failures.
4. Cover selection changes/restoration and rerun existing detail journeys.

### Concrete cases and pitfalls
Do not use a global name cache, pass domain objects through routes, or remove loading/error handling merely to hide the flash. A renamed or unavailable category must not leave a misleading cached title.

### Verification recipe
Run affected detail ViewModel/navigation tests first, then connected header/selection journeys and G2 with retained evidence under the registered ID. Capture initial, delayed, failed, and restored states. If handled in SELLO-018 instead, retain fresh evidence for 018 rather than claiming this proposed ticket was delivered.

## SELLO-BUG-002 — Make first-slice navigation motion consistent and accessible

- **Type:** Task
- **Classification:** Presentation bug/refinement; original report 2
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-018, SELLO-017, SELLO-015, SELLO-009, SELLO-006, SELLO-004
- **Gate:** G2

### Outcome
Category entry/return, dock → Anotar, and month changes communicate the change of context consistently, without depending on animation for meaning.

### Deliverables
A bounded transition matrix for these three journeys, Sello-token-based implementation, reduced-motion branches, and catalog examples for reusable changes. Not a rewrite of navigation or a whole-app motion overhaul.

### Reproduction and evidence
`08-navigation.mp4`: Recibo/detail replace in place, while Anotar uses a plain fade. `23-month-change.mp4`: picker animates, but receipt content changes without the reference's directional slip movement. Existing animation scales were 1.0. **Not all transitions are absent**; the broad intermittent-flash report is only partially reproduced. SELLO-BUG-001 owns the confirmed generic-title flash.

### Acceptance criteria
- Document expected enter/exit behavior for each scoped journey and implement it using existing Sello motion tokens.
- Dock → Anotar follows the approved reference's emphasized transition or an explicitly reviewed plain-rise fallback; month slips use the reference's directional 24dp/cross-fade treatment.
- Detail entry/return has a reviewed coherent transition; expanded selection need not pretend to be full-screen navigation.
- No blank intermediate surface, duplicate navigation, blocked input after completion, or replayed command.
- Reduced motion presents the correct end state immediately, with labels/focus making the context understandable.

### Tests
Controlled Compose animation-clock tests for intermediate/end states, rapid Back/re-entry, reduced motion, restored navigation, and compact/expanded behavior. Device recordings supplement tests; avoid arbitrary sleeps.

### Working checkpoint
The three scoped journeys have consistent reviewed motion and still work with animations disabled.

### Context and starting points
`app/src/main/java/com/software/sello/navigation/SelloApp.kt` mixes NavHost destinations and direct detail branches. Use `design-system/src/main/java/com/software/sello/designsystem/theme/SelloMotion.kt`, architecture §6, and `docs/design/sello-spec.html` “Every animation” table. Do not prescribe a new dependency or shared-transition API before checking the pinned toolchain and existing components.

### Implementation plan
1. Ratify a scoped transition matrix; add red tests for missing/inconsistent behavior.
2. Reuse tokens and the existing navigation engine; keep lifecycle/data loading separate from animation.
3. Add reduced-motion and interrupted-navigation branches.
4. Extend relevant component examples if reusable primitives change; record normal/reduced-motion journeys.

### Concrete cases and pitfalls
Do not mask failed reads with animation, animate financial truth using approximate values, or fold SELLO-BUG-001's identity correction into a motion-only fix. Deferred chart/theme/receipt refinements are not silently included.

### Verification recipe
Focused animation/navigation tests, scoped device recordings at normal and zero animation scales, applicable catalog verification, then G2 and fresh retained report. General flashing needs further reproduction if it persists after the confirmed title defect is corrected.

## SELLO-BUG-003 — Make unlimited category budgets an explicit opt-in

- **Type:** Task
- **Classification:** Requested UX improvement; original report 3
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-016, SELLO-008, SELLO-004
- **Gate:** G2
- **Decision needed:** confirm finite-empty default and finite-draft behavior when toggling unlimited

### Outcome
A new category begins with a visible budget-entry choice; choosing unlimited is deliberate and removes irrelevant budget-entry controls.

### Deliverables
Revised new-category presentation/default/restoration behavior, truthful validation, and applicable form/catalog tests. Existing persisted categories and financial limit meanings remain unchanged.

### Reproduction and evidence
Open Crear categorías on a fresh installation. `04-category-default.png/.xml` shows Sin límite selected, an empty disabled Límite en pesos field, and active amount suggestions. Source defaults `unlimited` to true in CategoryEditorState and SavedStateHandle initialization.

This reproduces the current behavior, **not a violation of an approved requirement that new categories must be finite**. The source request “I think this should…” is treated as a proposal, not recorded policy approval.

### Acceptance criteria
- After approval, a genuinely new draft defaults to finite with an empty amount; empty is neither unlimited nor an implicit zero.
- Explicit Sin límite opt-in hides both the numeric entry and finite-amount shortcuts.
- A finite submission without an amount is rejected accessibly; explicit zero and unlimited retain their approved distinct meanings.
- Restored existing drafts keep their chosen mode; persisted category/budget history is not rewritten.
- Toggling back to finite follows a documented draft policy. Recommended: retain the unsaved finite draft, but never submit it while unlimited; owner must confirm before implementation.

### Tests
Initial state, explicit unlimited opt-in, hide/show semantics, suggestion eligibility, missing finite amount, explicit-zero versus unlimited contracts, mode/draft restoration, duplicate submit and existing first-run journey.

### Working checkpoint
Create a finite category and an explicitly unlimited category through the real command path; restart preserves their distinct meanings.

### Context and starting points
CategoryEditorState, CategoryEditorViewModel, CategoryEditorScreen, CategoryEditorViewModelTest and FirstCategoryJourneyTest. D04 owns zero/unlimited meaning; D05 does not impose the transaction cap on category limits. Current toggling to unlimited clears limitText—changing that needs the stated decision.

### Implementation plan
1. Confirm default and toggle/draft policy; record it in the approved ticket context.
2. Add red state/UI tests for approved behavior.
3. Update new-draft defaults, conditional presentation and restoration without changing domain semantics.
4. Verify finite/unlimited creation and persisted history; coordinate with SELLO-BUG-004's overlapping editor scope.

### Concrete cases and pitfalls
Do not migrate unlimited categories to zero/finite, invent a default monetary limit, or turn an empty field into a successful zero-budget command. Hiding a control must not erase a previously restored draft without the approved policy.

### Verification recipe
Focused editor state/UI tests and real finite/unlimited device creation/restart, then G2 with retained evidence. Decision remains outstanding; this draft is not Ready just because dependencies are Done.

## SELLO-BUG-004 — Show readable grouped COP category limits without changing input meaning

- **Type:** Task
- **Classification:** Amount-entry readability issue; original report 4
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-016, SELLO-008, SELLO-007, SELLO-004
- **Gate:** G2

### Outcome
Users can read the magnitude of a category limit while entering it or selecting a suggested value.

### Deliverables
Presentation-only grouped limit display, correct editing/cursor/accessibility behavior, original-input validation coverage, and actual catalog examples if the reusable input primitive changes.

### Reproduction and evidence
In the category form, choose 500.000: field shows `500000` (`05-category-suggestion.png/.xml`). Replace manually with `400000`: field shows `400000` (`06-category-manual.png/.xml`). Saving this draft correctly produced 400.000 in Recibo; the demonstrated defect is readability, not a changed stored amount.

### Acceptance criteria
- Valid 400000 and suggested 500000 are visually readable as 400.000 and 500.000 while editing; expose exact currency-aware accessibility information.
- Grouping remains presentation-only: preserve original input for validation and maintain correct selection, cursor movement, insert/delete, paste and draft restoration.
- Accept approved plain digits/correctly grouped dots; malformed `1e3`, `1.23`, commas, negative signs and overflow are not silently repaired.
- No floating-point money and no new category-limit range policy. Submission persists the exact validated value.
- Disabled/hidden unlimited-mode behavior integrates with the owner-approved SELLO-BUG-003 decision, without requiring that decision to fix readability.

### Tests
Raw-to-display mapping, cursor/edit operations around grouping separators, suggestion values, valid/invalid paste, exact saved amount and restored drafts. Test valid category-limit bounds from the actual domain contract, not the expense transaction cap.

### Working checkpoint
Manual and shortcut entry are readable and save the same exact limit as before.

### Context and starting points
CategoryEditorScreen passes raw limitText to FormField; SuggestionPicked stores pesos.toString(). Inspect existing FormField and MoneyFormatter APIs rather than adding a duplicate financial parser. D05/architecture §4 require validation of original input.

### Implementation plan
1. Add red presentation/editing tests with independently specified expected display strings.
2. Introduce presentation grouping through existing input conventions, preserving raw validation and selection mapping.
3. Cover shortcuts, accessibility and restoration; extend actual shared examples if needed.
4. Save a synthetic limit and reconcile its persisted value with Recibo/detail.

### Concrete cases and pitfalls
Do not reformat malformed input into a valid amount, strip arbitrary characters, impose a new cap, or round budget values. Grouped display must not make separator deletion corrupt the raw draft.

### Verification recipe
Focused formatter/editor tests, connected typing/paste/suggestion/restoration checks, exact Room-backed creation result, then G2 with fresh retained evidence. Coordinate write order with SELLO-BUG-003.

## SELLO-BUG-005 — Stabilize expense keypad geometry and separate financial submission

- **Type:** Task
- **Classification:** Bug + related submission-separation UX risk; original reports 5 and 7
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-017, SELLO-008, SELLO-009, SELLO-004
- **Gate:** G2
- **Decision needed:** approve increased keypad-to-submit separation using existing Sello spacing tokens

### Outcome
Typing changes the amount, not the touch coordinates of the keypad; financial submission is clearly separated from digit entry.

### Deliverables
Stable pinned-control layout, removal of the redundant initial Escribe un monto helper as requested, retained accessible validation/errors, reviewed submit separation, and geometry/behavior regression tests. Group these two reports because both fixes own ExpenseEntryScreen's Controls/Stacked layout.

### Reproduction and evidence
Open empty Anotar with Exploracion selected. Key 1 target is `[33,1487][359,1619]`. Enter 1: it moves to `[33,1552][359,1684]`, **65 px ≈23.6dp downward**, while submit stays `[33,2153][1047,2307]`. The Escribe un monto line disappears. See 09/10 screenshots/XML and `10-keypad.mp4`.

Controlled miskey reproduction: tap 1 at (195,1550), then (190,1805), a point inside the **original 7** target `[33,1785][359,1917]`. After relayout it is inside **4** `[33,1701][359,1833]`; the draft becomes **14 rather than intended 17** (`16-old-key-position.png/.xml`). This draft was not submitted.

In the valid state the bottom keypad target ends at y=2131 and submit begins at y=2153: **22 px =8dp separation**. Proximity is measured; an accidental persisted submission was not reproduced. Do not describe this as proven financial duplication or storage corruption.

### Acceptance criteria
- At a fixed viewport/font/IME configuration, digit targets do not move when amount transitions empty → valid → empty or when helper/error text changes.
- Remove the redundant initial Escribe un monto text; keep the empty amount's accessible description and actionable validation/read/unknown-outcome feedback.
- Keypad and submit hit regions do not overlap and have an owner-approved increased gap using existing tokens. Recommended minimum: `SelloTheme.spacing.lg` (16dp in the current `SelloShapes.kt`), doubling the observed 8dp gap; verify adaptive layouts before finalizing it.
- The old-key-coordinate reproduction yields intended 17 after correction, without submitting; tapping any digit never invokes Submit.
- Submit remains accessible/reachable with large fonts, compact/short-landscape layouts and IME changes; no extra confirmation step or altered Enter behavior is invented.
- Existing busy, duplicate-submit, operation-ID and receipt-recovery behavior remains intact.

### Tests
Connected bounds assertions before/after first digit, clear and validation transitions; coordinate-based regression for intended 17; digit-versus-submit isolation; virtual-time busy/double-submit tests; large-font/landscape/IME layout cases. Control time/reads instead of arbitrary sleeps.

### Working checkpoint
Stable keypad entry and deliberately separated submission; a confirmed save still produces exactly one matching receipt/expense.

### Context and starting points
ExpenseEntryScreen Stacked pins a content-sized Controls column to the bottom. Controls conditionally inserts ENTRY_MISSING_TAG between AmountKeypad and SelloButton; removal changes total height. Read ExpenseEntryState/ViewModel and design-system AmountKeypad/SelloButton/token contracts. Do not change financial command services to solve layout.

### Implementation plan
1. Add meaningful red geometry/miskey tests demonstrating the observed defect.
2. Remove the initial redundant helper and make control geometry independent of dynamic feedback; keep important feedback in a stable, accessible area.
3. Apply approved separation without causing clipping, unreachable submit or gesture-only interactions.
4. Extend applicable catalog states and rerun create/receipt/recovery journeys.

### Concrete cases and pitfalls
Increasing the gap alone does not fix the 65px movement. Conversely, merely removing one helper may leave movement when another conditional error appears. Fix the sizing cause across feedback states. Never add a successful no-op save or relax validation to make the form appear stable.

### Verification recipe
Focused entry state tests and connected geometry/intent assertions first; run the original coordinate sequence and empty/valid/clear/error cases at agreed configurations; reconcile one real save to stored expense/receipt; G2 with retained report and before/after captures.

## SELLO-BUG-006 — Offer a direct return to the financial current month

- **Type:** Task
- **Classification:** Requested navigation improvement; original report 6
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-015, SELLO-009, SELLO-004
- **Gate:** G2
- **Decision needed:** approve current-month action placement/copy

### Outcome
Users browsing history can return to the current financial month without searching month/year grids.

### Deliverables
One explicit Mes actual action in the month sheet and adaptive side-panel equivalent, using existing shared MonthSession behavior; focused rollover/restoration/accessibility coverage.

### Reproduction and evidence
Open month picker, select September 2026, reopen and go to 2025. `12-month-picker.png/.xml` and `14-previous-year-picker.png/.xml` show only year controls and month cells, with no direct current-month action. The absence is confirmed; this is a UX addition rather than a breach of an existing quick-return acceptance criterion.

### Acceptance criteria
- From any browsed year/month, one explicit action selects the financial current month and dismisses the picker consistently with normal selection.
- Sheet and side panel expose equivalent keyboard/screen-reader-accessible actions; visible copy names the destination clearly.
- Current month comes from the injected observable financial clock/session, not a new wall-clock read or cached picker year.
- Month rollover while the picker is open updates the action's destination; future selection remains prohibited.
- Selection sharing/restoration and existing >30-minute inactivity behavior remain intact; drafts are not cleared and no financial write occurs.

### Tests
Historic selections several years back, already-current selection, financial rollover while open, restored picker/selection, close/back behavior, draft preservation, and compact/expanded accessibility semantics.

### Working checkpoint
Browse a past year → Mes actual → correct current Recibo, with the draft and persisted history unchanged.

### Context and starting points
`app/src/main/java/com/software/sello/navigation/MonthPicker.kt`, `app/src/main/java/com/software/sello/navigation/ShellViewModel.kt` and `app/src/main/java/com/software/sello/navigation/MonthSession.kt` already own current versus selected periods. Reuse normal selection rather than inventing another date controller. The action belongs in presentation/session ownership, not the financial database.

### Implementation plan
1. Approve placement/copy; add red action/state/UI tests.
2. Expose a direct action through existing shell/session pathways and resources.
3. Cover rollover, restoration, layout and draft invariants.
4. Verify no new write/receipt and preserve existing picker navigation.

### Concrete cases and pitfalls
Do not treat selected month as current month, use LocalDate.now(), add a fake swipe capability, or mutate financial time to return home. The incorrect existing accessibility label is tracked independently in SELLO-BUG-007.

### Verification recipe
Focused MonthSession/NavigationState tests, connected historic-year → current-month journeys and controlled rollover checks, then G2 with retained evidence. Owner placement approval is required before Ready.

## SELLO-BUG-007 — Distinguish selected month from current month in accessible labels

- **Type:** Task
- **Classification:** Bug discovered during exploration; not in original seven reports
- **Priority:** P2
- **Status:** Backlog
- **Depends on:** SELLO-015, SELLO-004
- **Gate:** G2

### Outcome
The month switcher does not tell assistive-technology users that a selected historical month is the current month.

### Deliverables
Accurate resource wording and presentation mapping, with current/historical/rollover semantics regression tests. No financial changes and no dependency on adding SELLO-BUG-006's shortcut.

### Reproduction and evidence
Financial date is October 10, 2026. Select September 2026. `13-past-month.xml` contains **Cambiar de mes. Mes actual: Septiembre 2026**, although the visible selected heading is historical. This is confirmed in the accessibility hierarchy; a live TalkBack walkthrough was not executed.

Expected: identify September as the selected month. If current month is also announced, identify October separately and truthfully.

### Acceptance criteria
- Historical selected periods are announced as selected, never as current.
- The current-period case and optional separate current-period wording remain accurate under financial rollover/restoration.
- Spanish resources and exact localized month/year values are used; do not read wall time ad hoc.
- Visual heading, spoken selected period, and shared MonthSession agree; no change to selected-month financial data.

### Tests
Semantics assertions for selected=current and selected<historical current, year boundary, restored historical selection and controlled rollover; device screen-reader verification before acceptance.

### Working checkpoint
Selecting September while current is October exposes an accurate selected-month description and preserves ordinary picker navigation.

### Context and starting points
ShellScreen in `app/src/main/java/com/software/sello/navigation/SelloApp.kt` passes the selected title into `month_picker_open` in `app/src/main/res/values/strings.xml`, whose resource says Mes actual. Existing `ShellState.currentMonth` and selectedMonth already distinguish the concepts. Inspect NavigationStateTest and shell connected tests before editing.

### Implementation plan
1. Add a red semantics assertion for the recorded September/October case.
2. Correct copy/mapping without duplicating month calculations.
3. Cover current/historical, restored and rollover cases.
4. Verify with TalkBack and keyboard focus; retain evidence.

### Concrete cases and pitfalls
Do not fix this by announcing October while the selected data is September without identifying both roles. This is a wording/mapping defect, not a reason to force selection back to current or rewrite financial dates.

### Verification recipe
Focused shell/month semantics tests, connected current/past selection plus manual TalkBack walkthrough, then G2 with a fresh retained report. Existing XML evidence establishes the defect but does not replace the future screen-reader acceptance.

## Remaining owner decisions and handoff

1. Confirm SELLO-BUG-003's finite-empty default and whether toggling unlimited preserves the unsaved finite draft. Both modes retain existing financial meaning.
2. Approve SELLO-BUG-005's larger submit separation and SELLO-BUG-006's action placement. Suggested defaults are documented, not claimed approved.
3. Decide which SELLO-018-related findings are acceptance fixes inside 018 versus independently registered follow-ups. Never declare incomplete acceptance Done merely because a follow-up draft exists.
4. Approve canonical placement and separately authorize board/quality-tool support for `SELLO-BUG-###` before registration. These drafts create no hosted issues, PRs, Jira records or Done statuses.

After renumbering, all seven IDs/references, required ticket sections, allowed types/priorities, Backlog/G2 metadata and dependency existence were checked with a draft-only `SELLO-BUG-###` heading matcher and the current board metadata/section helpers; result: passed (`draft-validation-bug-ids.txt`). The original `draft-validation.txt` remains historical evidence for the previous draft IDs. Neither check proves native board/quality-tool support for the new namespace. This is structural validation, not canonical registration, acceptance approval or a ticket Quality run.

Evidence comes only from synthetic data in the throwaway AVD. Final source and Git-status comparison is unchanged. The throwaway emulator was shut down and its agent-created temporary AVD directory was deleted with avdmanager; the report and evidence directory remain for review. Cleanup needed a retry with the documented JAVA_HOME and then completed (`cleanup.txt`). The pre-existing emulator was left running. Do not reuse the copied synthetic database as a production fixture or describe this bounded exploration as complete MVP/release verification.
