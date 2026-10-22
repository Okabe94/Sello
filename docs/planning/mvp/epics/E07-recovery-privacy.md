# SELLO-E07 — Backup, restore, reset and privacy

- **Status:** In Progress
- **Goal:** Ship a local financial app whose data can be recovered and deliberately removed.
- **Exit:** Real document workflows, bounded portable validation, durable restore/reset recovery and verified privacy boundaries.

## SELLO-028 — Define portable MVP backups and bounded validation

- **Type:** Task
- **Priority:** P0
- **Status:** Done
- **Depends on:** SELLO-011, SELLO-020, SELLO-021, SELLO-023, SELLO-004
- **Gate:** G2

### Outcome
Recovery files remain valid independently of Room tables, enums and future refactors.

### Deliverables
Explicit versioned serialization DTOs/codecs, consistent financial snapshot reader,
portable preference capture, strict validation and documented format/sample files.
Include category/history/month limits, profile financial zone and stable order;
exclude jobs, permissions, secrets, simulation state and internal execution receipts.

### Acceptance criteria
- No entity serialization/class-name/enum-ordinal identity; unsupported format versions
  reject clearly. COP-only MVP records cannot silently accept other currency rows.
- Default limits are 10 MiB, 100,000 records, 10,000 characters/field and bounded
  nesting, with stricter domain field limits still enforced. Validate dates, amounts,
  references, duplicate IDs and category/budget/history consistency before mutation.
- ADR 0003/M05: apply the same 1–999,999,999,999 COP expense/income bound as manual
  entry; maximum+1 rejects the file, not just that row. Valid maximum round-trips
  exactly, while aggregate totals may exceed the per-record cap (M06).
- Financial snapshot has one revision; separate preference capture does not pretend
  Room/DataStore are one transaction. Plaintext is documented per approved policy.

### Tests
Round-trip exact values/order/zone/preferences; malformed/truncated/oversized/deep
payloads, unsupported versions, dangling references and overflow fixtures. Real-Room
snapshot consistency under concurrent writes; G2.

### Working checkpoint
Valid backup can be produced/validated programmatically without modifying source
data; no restore success or UI action is exposed before the real workflow lands.

### Context and starting points
A recovery file must outlive Room implementation changes and survive hostile input.
Use real schema/history from SELLO-011/020/021, portable preferences from SELLO-023
and approved D05/D08/D09. Read architecture §7, serialization/Room official sources
and [contract checklist](../EXECUTION_GUIDE.md#contract-and-failure-checklist).

From SELLO-019 and SELLO-021 (2026-10-12): `deleted_expense` and `deleted_income`
hold what an undo would put back. They are not financial facts and must not be
exported or imported.

### Implementation plan
1. Document the v1 envelope/field schema in `docs/data/backup-format.md`, including
   stable keys, timestamps/zone/order, currency representation, exclusions and version policy.
2. Implement independent DTOs/codecs in data/backup; capture financial rows at one
   revision and preference values separately, with explicit provenance and bounds.
3. Build read/validation staging that checks file/nesting/count/text bounds before
   deep allocation where practical, then domain/date/reference/duplicate/aggregate integrity.
4. Produce validated immutable import plan/counts independent of Room entities;
   rejection must carry actionable typed reason and cause no storage mutation.
5. Add committed synthetic valid/invalid fixtures under test resources and publish
   format contracts for document exporter/restore tickets. No legacy database translator.

### Concrete cases and pitfalls
- Round-trip H01/G01, archived categories and exact large amounts/order without
  Float, translated enum names or newly assigned history ordering.
- 10 MiB/count/nesting limits are structural caps; a shorter note violating D05
  still fails. Duplicate category IDs or dangling expense references reject the whole plan.
- Restoring execution receipts/jobs would resurrect internal operations; portable
  financial record identity is necessary, internal command execution history is not.

### Verification recipe
Create/run `./gradlew :data:testDebugUnitTest --tests '*BackupValidatorTest'` and
`*BackupRoundTripTest`, plus real-Room BackupSnapshotConsistencyTest; G2. Keep fixtures
synthetic, inspect independent expected counts/bytes/values and confirm validation never writes.

### Delivery evidence

- **Revision:** the gate tested commit `ddd0fa2` on `sello-028-backup-format`, a
  clean tree rebased on `main` at `d2b82ea`. This evidence is added to that commit
  afterwards; status and evidence lines are outside the input fingerprint.
- **Decisions:** the owner chose, on 2026-10-22: `kotlinx-serialization-json` 1.11.0
  (the catalog's existing serialization version) with the serialization plugin in
  `:data`; amounts as bare JSON integers; UTC ISO-8601 moments, `yyyy-MM-dd` dates
  and `yyyy-MM` months; strict keys, so any change is a new version; theme, ink,
  order and direction as the only portable preferences; a backup whose preferences
  cannot be read is still made, with `"preferences": null`; the file name
  `sello-respaldo-AAAA-MM-DD-HHmm.json` in the financial zone.
- **Requirement mapping:** [format](../../../data/backup-format.md). Wire DTOs
  (`BackupWire.kt`) are separate from entities and domain classes; their spellings
  are the file's own (`WireNames`). `BackupCodec.read` checks size (a stream is read
  one byte past 10 MiB at most), then `scanStructure` before parsing (UTF-8, depth 8,
  100,000 array elements, 10,000 characters per string, duplicate keys,
  truncation), then the header, so another version is named, then the exact v1 keys,
  then `validate`: D05 text, ADR 0003 amounts (maximum + 1 rejects the file; limits
  and totals are not capped), COP only, real dates and canonical moments,
  duplicate IDs/names/sequences/entries, references and the 1,200-entry default
  history. A rejection names a path, never file contents; nothing is written.
  `RoomBackupSnapshots` reads every table in one transaction with its revision;
  `BackupExport` reads preferences afterwards and separately, encodes in a fixed
  order and reads its own output back. Receipts, undo copies, versions, generation,
  jobs, sandbox state and notice dismissals are not exported. No restore or UI
  action exists yet (SELLO-029/030).
- **Red / Green:** mutations each turned tests red: no duplicate-key check (the
  duplicate-key test), the JSON library's own number reading (`"10000"`, `1e4`,
  `010000`, a quoted version and a negative revision accepted), and no amount range
  (seven tests, including the hand-written G01 file and maximum + 1). The JSON
  library was first shown to accept quoted numbers, exponents, leading zeros and
  repeated keys, which is why numbers stay raw until validation.
- **Tests:** `BackupValidatorTest` (35) and `BackupRoundTripTest` (9) on the host,
  against the hand-written [G01 file](../../../../data/src/test/resources/backup/valid-g01.json)
  and five named invalid samples; `BackupSnapshotConsistencyTest` (5) on real Room:
  the commands' G01 history read at revision 8, export → read back equal to storage
  with no row or revision change, missing preferences, identical bytes, and 40
  concurrent saves where every snapshot's expense count matches its revision.
- **Gate results:** `./scripts/verify-ticket SELLO-028 --retain` passed all six checks.
  Host: 614 tests, results reused by Gradle from the earlier run of the identical
  sources in this session, where they ran fresh (44 new backup tests); device: 622
  tests run fresh (5 new), full coverage of data, design-system, app and catalog; zero failures, errors or skips. Lint had zero errors and
  33 warnings, the same app dependency-update and unused-colour notices as SELLO-024
  and 027; none in changed code. Earlier runs failed on an over-long test name
  (ktlint) and on the board check before its views were regenerated; both fixed.
- **Quality run:** run 20261022T185926Z-5304c2a0; SELLO-028 G2 passed; HEAD ddd0fa2, inputs sha256 ce38612f2916; report docs/planning/mvp/quality-reports/SELLO-028.json sha256 624ef63c98b0843d35a879cd029e13a4f9ba84dc54704550961d609911f0fdee
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`,
  serial `emulator-5582`. All fixtures and test data are synthetic. No physical device
  was used; there is no user-facing change to show.
- **Review:** executor self-review covered hand-written expected values, the
  per-record bound at maximum and maximum + 1, the structural caps at and past their
  limits, and that reading writes nothing. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.

## SELLO-029 — Deliver real document-picker backup creation

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-028, SELLO-015, SELLO-004
- **Gate:** G2

### Outcome
A user can save a usable recovery file and distinguish success from canceled/failed output.

### Deliverables
Backup screen accessible from settings, Android document port/adapter, bounded
off-main serialization/write/close and progress/error/last-success presentation.
Use an approved Sello filename and accessible file/date/size description.

### Acceptance criteria
- “Guardado” and last-success time update only after successful write/flush/close;
  picker cancellation is not success or data loss. Provider/write/close faults are visible.
- UI does not freeze on large valid data; no broad storage permission or arbitrary
  shared-path write. Retrying file creation cannot mutate financial records.
- Process recreation restores workflow state honestly; an uncertain file outcome
  is not stamped successful. Backup contents exclude sandbox metadata/internal jobs.
- Approved D08/P01–P04 disclosure precedes destination/save: plaintext financial
  contents, no password protection, manual-recovery responsibility and provider-managed
  sync for cloud-backed locations. Explain that app reset cannot erase external copies.

### Tests
Document-port cancellation/write/close fault tests and device picker save→read→validate
journey; large-file responsiveness and active-write snapshot tests. Catalog backup
feedback states, plus G2.
Cover accessible disclosure/cancel/save flows, no encryption claims and temporary
cleanup without deleting user/provider-owned copies; final copy needs UI review.

### Working checkpoint
User-generated backup parses with SELLO-028 and matches actual financial history;
all manual workflows remain available. Restore action stays absent until SELLO-030.

### Context and starting points
Returning a document URI is not a successful backup. Use SELLO-028 encoder/snapshot
and app platform/Root contracts; read
the [Sello v2 components](../../../design/v2/components.html) and, until v2 designs it, the v1.1 [backup screen](../../../design/sello-spec.html#s-respaldo), approved D08 and official
document-picker sources in the guide. Files may be plaintext and provider failures are real.

### Implementation plan
1. Define app-owned document create/write port with typed canceled/failed/completed
   outcomes; Android adapter uses the system create-document flow and URI permissions.
2. Add backup feature states for idle/picker/preparing/writing/error/completed using
   bounded off-main serialization and owned cancellation; choose approved Sello filename.
3. Record last-success metadata only after stream write/flush/close completes. Distinguish
   provider/caller/file failures from any unrelated financial mutation.
4. Wire actual settings backup route and status/copy/accessibility; do not expose
   an unimplemented restore button. Handle recreation without blindly starting another file.
5. Clean owned private staging/cache and disclose partial external-file possibility;
   never delete arbitrary documents or claim atomicity across Room/provider/DataStore.

### Concrete cases and pitfalls
- Cancel picker: no success stamp/date change. Fail on close after some bytes: failed
  file outcome, unchanged money and last-success value.
- Create file then kill process: pending/unknown completion must not display Guardado
  just because a URI exists. Recovery may require verifying/creating a new output file.
- A successful G01 backup must revalidate to three expenses/one income and exact
  category/limit history; progress UI must not block main-thread input.

### Verification recipe
Create/run `*BackupExportViewModelTest`, `*DocumentWriteContractTest` and BackupFileJourneyTest;
G2. Select a real provider/document, reopen/validate resulting bytes, inject write/close/
cancellation failures and record exact file size/count/hash plus last-success behavior.

## SELLO-030 — Implement staged atomic restore and resumable preference completion

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-028, SELLO-029, SELLO-012, SELLO-023, SELLO-004
- **Gate:** G2

### Outcome
Restore replaces financial data safely and never hides incomplete settings recovery.

### Deliverables
Read/stage/validate/preview/confirm flow with source date and replacement counts,
persisted staging/recovery IDs, atomic replacement/generation advance, surviving
restore receipt and separate resumable preferences completion. Clean private staging
on completion/cancel/expiry; route back to first/current Recibo only on truthful outcome.

### Acceptance criteria
- Invalid/canceled restore does not mutate anything. Confirmed restore applies all
  financial rows atomically and fences old drafts/commands; failure before commit rolls back.
- ADR 0003/M05: any expense/income outside 1–999,999,999,999 COP invalidates the
  entire backup before replacement. No truncation, skipped rows or partial settings
  updates; verify unchanged financial rows, generation and preferences on rejection.
- Approved D09/R01–R04: whole-file validation precedes current/replacement-count
  preview and explicit replacement warning. Never merge; restore backed-up financial
  zone and portable appearance/order, without system permission or URI-grant changes.
- Financial commit plus preference failure is “data restored, settings pending”,
  not generic failure or complete Restaurado. Restart resumes without replacing twice.
- R03 copy must explicitly confirm restored financial history and list only the
  preferences that failed to apply, with backed-up mode/color/order/direction where
  applicable. Do not show standalone ajustes pendientes or claim applied fields
  failed. Match retry action to the affected preferences and explain no second
  financial replacement. Use D09's owner-approved copy baseline with actual failed
  preference values; retain implementation-time accessible/adaptive UI review;
  test full/appearance-only/order-only failure messages and preferences-only retry.
- Unknown commit outcome recovers by original operation ID. Staging survives eligible
  restart but cannot be reinterpreted as a new operation after a generation change.
- Backup does not resurrect debug jobs, permissions or internal execution receipts.

### Tests
Real-Room rollback/reopen/generation tests, cancellation-around-commit, failing
DataStore recovery and staging cleanup; device valid/invalid restore and process
restart journeys. Extend sandbox replay/recovery scenarios; G2.

### Working checkpoint
Create backup → change data → restore → restart returns exact prior history and
preferences, with partial settings progress visibly recoverable until complete.

### Context and starting points
Restore spans two stores, so a boolean success/failure hides committed-data versus
pending-settings state. Use validated import plans from SELLO-028, document picker
from SELLO-029 and generation/receipt/prefs contracts. Read architecture §§5/7,
approved D09 and [recovery cases](../../../testing/mvp-financial-examples.md#recovery-contracts-to-instantiate-in-downstream-tests--d07d09).

### Implementation plan
1. Add choose/read/private-stage/validate flow; persist owned staging ID/format/counts/
   source date and operation identity needed for eligible restart, under bounded limits.
2. Show ConfirmSlip with current/replacement counts and expected generation/revision;
   cancel leaves data untouched. If confirmation becomes stale, refresh/reconfirm safely.
3. Implement one financial replacement transaction with reference-aware ordering,
   generation advance, revision and surviving control receipt. No DataStore/file calls inside.
4. Apply portable preferences after commit and persist completion progress; on restart
   resolve operation receipt before resuming, so only pending preferences repeat.
5. Invalidate stale forms/undo/import continuation, clean owned staging and display
   committed-but-settings-pending distinctly from complete Restaurado or rollback-established error.

### Concrete cases and pitfalls
- Invalid file or canceled confirmation: zero changes in both stores.
- Restore valid G01, fail preference write: exact three expenses/one income restored,
  pending settings visible; reopening/retry must not replace financial data again.
- Cancel after Room commit: durable operation lookup recovers truth. Old generation
  submit/undo cannot cross replacement, even if restored record IDs resemble prior ones.
- Owner request, 2026-10-11 (SELLO-045, [ADR 0008](../../../decisions/0008-unreadable-storage-start-over.md)):
  restore from a backup must be reachable from the screen shown when the data cannot
  be opened at start, where no financial graph exists; a restore offered only inside
  the working app is unreachable exactly then. Decide here whether the file kept
  aside as `sello-unreadable.db` can be recovered or only replaced.

### Verification recipe
Create/run RestoreCommandContractTest on real Room, `*RestoreRecoveryViewModelTest`
and RestoreJourneyTest; G2. Test failures before/inside/after commit, actual staged-file
reopen and pending preferences. Compare restored fields/order/zone/totals with original backup.

## SELLO-031 — Deliver fenced two-confirmation reset and recovery

- **Type:** Story
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-030, SELLO-020, SELLO-021, SELLO-004
- **Gate:** G2

### Outcome
Users can intentionally erase app data without stale drafts, undo or recovery recreating it.

### Deliverables
Counted reset preview, second confirmation requiring exact BORRAR, atomic financial
replacement/generation advance and minimal surviving completion record. Apply the
approved preference-reset policy with separate recovery; fence open forms/undo/
staging/sandbox sessions and return to truthful first-run state.

### Acceptance criteria
- Cancel/wrong confirmation leaves all data unchanged. Confirmed reset removes
  categories, budgets, expenses/income and user payloads according to the approved policy.
- Approved D09/R05–R06: counted preview then exact BORRAR confirmation; clear
  portable preferences to first-run Sistema/Cobalto/Más usadas. Next setup establishes
  device financial zone; system permissions remain unchanged. Explicitly warn that
  external backups survive; surviving control metadata contains no old amounts/names/notes.
- Old submits/undo/restore continuation cannot cross the new generation. Retained
  recovery metadata is minimal and does not preserve erased financial payloads.
- Restart/cancellation around reset resolves its original outcome; partial preference
  reset is explicit and resumes rather than fake complete or an unguarded second wipe.
- Existing external backup files are not silently deleted or claimed erased.

### Tests
Real-Room reset races, stale draft/undo/import rejection and recovery reopen;
UI counts/two confirmations; device reset→restart→new category→expense journey.
Add sandbox reset-fencing scenarios and run G2.

### Working checkpoint
Reset app can be used from empty again; canceled reset preserves the prior working
state. Backup/restore still works across the deliberate generation boundary.

### Context and starting points
Reset is deliberate data erasure; stale recovery or undo must not recreate old money.
Use SELLO-030 replacement/control patterns and actual workflows, but not the backup
payload. Read the [Sello v2 components](../../../design/v2/components.html) and, until v2 designs it, the v1.1 [settings reset](../../../design/sello-spec.html#s-config), architecture
§5 and approved D09's preference/zone/minimal-metadata policy.

From SELLO-019 and SELLO-021 (2026-10-12): reset must also erase `deleted_expense`
and `deleted_income`. Undo already refuses a kept copy from an earlier generation,
but the amount and note in it are erased content and must not outlive the reset.

### Implementation plan
1. Read actual affected counts at a generation/revision and show first ConfirmSlip;
   second requires exact BORRAR. Reject canceled/incorrect/stale confirmation.
2. Implement reset command with operation identity, atomic financial erase/profile
   transition/generation advance and minimum surviving completion metadata.
3. Remove erased payloads from receipts/compensation/private staging according to
   approved policy; preserve only recovery control needed to truthfully resolve this reset.
4. Apply/track preference-reset completion separately and fence open drafts/undo/import/
   debug runners. Process interruption must recover the original operation, not wipe twice.
5. Return to real empty Recibo and rerun onboarding; external backups remain outside
   app erasure scope. Explain that boundary and test reuse after reset.

### Concrete cases and pitfalls
- Type `borrar` or cancel: unchanged records/preferences. Changed counts while
  confirming must trigger the approved stale-confirmation flow, not silent erase.
- After reset, old draft/undo/import ID cannot restore an expense; recovery record
  cannot contain the entire “deleted” dataset as a hidden tombstone archive.
- Keep an external backup file intact and demonstrate deliberate new restore still
  works; reset must not pretend that external plaintext copies were erased.
- A start over after unreadable data (SELLO-045, [ADR 0008](../../../decisions/0008-unreadable-storage-start-over.md))
  leaves `sello-unreadable.db` and its companion files in private storage. Reset must
  erase them too: a kept copy that survives reset is the hidden archive ruled out above.

### Verification recipe
Create/run ResetGenerationContractTest, `*ResetViewModelTest` and ResetReuseJourneyTest;
G2. Interrupt before/after commit/preferences, reopen DB, replay stale operations,
inspect retained metadata, and create a new real category/expense from empty afterward.

## SELLO-032 — Verify privacy, file boundaries and developer-data isolation

- **Type:** Task
- **Priority:** P0
- **Status:** Backlog
- **Depends on:** SELLO-029, SELLO-030, SELLO-031, SELLO-024, SELLO-004
- **Gate:** G2

### Outcome
Default platform/file/log behavior matches the local-only promise in every variant.

### Deliverables
Explicit manifest/platform backup and device-transfer exclusions, permission audit,
private staging/cache lifecycle and narrowly scoped file-sharing configuration only
where an actual consumer exists; redacted diagnostics and privacy disclosure.
Do not add a production FileProvider solely for deferred analytical exports.

### Acceptance criteria
- Unapproved OS backup/transfer is disabled/excluded; INTERNET, notification-listener,
  widget, broad storage and deferred notification permissions are absent from MVP.
- Shared diagnostic files are debug-only/synthetic or explicitly redacted; no whole
  database/private-directory provider exposure. Temporary data is cleaned safely.
- Plaintext document backup is disclosed; checksum is not called encryption.
- Verify approved D08 manual-backup responsibility, cloud-provider/external-copy
  disclosures and P01–P04 boundaries with actual merged manifests and file journeys,
  not an approval record mistaken for implemented privacy protection.
- Release resources/classes contain no fixtures, clock controllers or tools route;
  normal debug profile, sandbox and production cannot cross storage boundaries.

### Tests
Merged-manifest/resource/dependency assertions, malformed URI/path and cache cleanup
tests, device permission/file flow review and log inspection with synthetic sensitive
markers. Run G2 and retain artifact inspection output.

### Working checkpoint
Real backup/restore/reset continues working without broad permissions, exposed
private files or developer artifacts in the customer package.

### Context and starting points
Generated Android backup defaults were unsafe for the local-only promise; SELLO-011
must already disable them before storing money. This ticket audits the complete
file/recovery/variant boundary, using SELLO-024/029–031. Read architecture §7,
approved D08/D09 and official backup/documents sources in the executor guide.

### Implementation plan
1. Inspect merged debug/release manifests and extraction rules, not only main XML;
   verify cloud/device-transfer exclusions and absence of unsupported permissions/services.
2. Inventory documents/private staging/cache/logs and every actual share consumer.
   Permit only narrow owned temporary paths; do not add unused production FileProvider.
3. Test URI/path/provider failure and cleanup/retention boundaries; separate personal
   normal-debug profile from synthetic sandbox output and customer storage.
4. Inspect release compile/runtime dependencies/resources/classes with and without
   shrink assistance so obfuscation cannot masquerade as debug-source isolation.
5. Write `docs/privacy/mvp-data-handling.md` describing exact stored/shared data,
   plaintext disclosure, platform exclusions and diagnostic redaction; fix observed gaps.

### Concrete cases and pitfalls
- A provider pointing at whole app private storage is a leak even if the UI only
  shares one file. Logs must not contain synthetic sensitive markers used in tests.
- INTERNET/notification-listener/widget/broad-storage permissions remain absent;
  opening a system document does not justify broad filesystem access.
- Old staging cleanup must not delete unrelated external user documents or another
  sandbox/profile's pending recovery file.
- `sello-unreadable.db` (SELLO-045, [ADR 0008](../../../decisions/0008-unreadable-storage-start-over.md))
  is private plaintext financial data: verify it is never shared, backed up by the
  system or left behind by reset.

### Verification recipe
Create/run ManifestPrivacyTest, FileBoundaryTest and DebugReleaseIsolationTest;
G2. Retain merged-manifest/artifact inventories, synthetic-marker log checks and
executed real file/restore/reset journeys under minimal permissions.
