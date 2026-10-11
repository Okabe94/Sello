# Bug backlog and source evidence

The canonical consolidated backlog is [SELLO-E11](../mvp/epics/E11-bug-stabilization.md),
registered on 2026-10-11 at the owner's request. Its 16 children cover the seven
revised UX tickets, seven technical findings, and two additional handoff defects.
The epic orders them by severity/exposure, not the technical report's confidence.

## Preserved inputs

- [Original owner reports](reports/bugs.md).
- [Revised inventory and executor briefs](reports/bugs_revision.md), prepared
  2026-10-10 against a dirty working-tree build of `53533bf`.
- [Technical assessment](../technical/ASSESSMENT-2026-10-11.md), prepared against
  `7f5c7ca` after SELLO-018 was recorded Done.
- [Development handoff](../../development/HANDOFF.md).
- [Exploration evidence](evidence/20261010/): 67 retained files, approximately
  5.8 MiB, including PNG/XML, MP4, host-test output, manifests and a synthetic database
  snapshot. Retained relative evidence filenames in the revised report resolve here;
  omitted historical artifacts are documented below.

Reports and retained evidence were copied byte-for-byte; the originals under
`/home/okabe94` remain untouched. Archive-scoped Git attributes disable line-ending
normalization for reports/evidence so committed copies retain those same bytes.
Their absolute paths, draft aliases, proposed
placement and historical statuses are provenance, not current execution instructions.
In particular, the revised report predates completion of SELLO-018. Use E11 for
current registration, dependencies, order and acceptance. The assessment remains
unchanged, including its distinction between executed probes and unmeasured risks.

## Evidence retention decision

On 2026-10-11 the owner approved trimming redundant extracted PNGs and then asked
to retain the specific frames cited by SELLO-057. Of the 200 extracted PNGs in
`navigation-frames/` (26 unique images), frames 46–50 are retained for direct
inspection; the other 195 are omitted (approximately 8.08 MiB). The original
`08-navigation.mp4`, `08-detail-entry-frames.png` and the five cited individual PNGs
remain, preserving the temporal recording and convenient loading-title comparisons.
The complete source archive under `/home/okabe94/bugs_revision_evidence/20261010/`
is untouched. Archived reports describe the original capture, not this curated count.

The owner also approved omitting these 19 artifacts after a ticket-by-ticket audit:

- Six preliminary PNG/XML captures numbered `00`–`02`, from the interrupted run,
  not the primary reproduction baseline.
- `17-restored-draft.png/.xml`, byte-identical to `16-old-key-position.png/.xml`;
  retain `17-process-restoration.txt` to document that this was not process death.
- `08-navigation-header-contact-sheet.png`, an uncited navigation overview that
  does not show the title flash; the defect-focused composite and videos remain.
- `draft-validation.txt` and `draft-validation-bug-ids.txt`, obsolete draft-only
  checks superseded by canonical board validation, not financial acceptance.
- `emulator.log`, `emulator-restart.log` and `avd-home.txt`, startup diagnostics and
  a temporary path; device configuration and cleanup confirmation remain.
- `git-status-final.txt` and `source-manifest-final.sha256`, third byte-identical
  copies; both before/after pairs remain directly inspectable.
- The three-file `db-after-one-expense/` bundle. Isolated-copy SQLite checks proved
  integrity and that `db-final` retains every earlier category, limit, expense and
  receipt row unchanged, plus the later expense. The complete final DB/WAL/SHM
  bundle remains, with exactly 1 and 12,500 COP expenses and a 400,000 COP limit.

All primary bug screenshot/XML pairs, all three videos, cited frames 46–50, verified
process-death restoration, large-font/invalid-input/duplicate-submit baseline evidence,
build/test output, APK/environment metadata, before/after source manifests/status,
historical patch and final persisted-state evidence remain. The additional audit
trim saves approximately 1.14 MiB without dropping a ticket's primary defect evidence.
Current artifacts establish the historical baseline, not completed fixes: each ticket
still needs fresh red→green tests, its retained G2 report and specified device/manual
acceptance. Neither archival reduction nor passing planning tests replaces those gates.

## Registration and safety

`SELLO-BUG-###` and `SELLO-TECH-BUG-###` are retained as source aliases. Canonical
children use supported `SELLO-045`–`SELLO-060` IDs and `Task` type, so board/quality
tooling needs no namespace or issue-type changes. This authorizes backlog placement,
not financial/UX decisions, implementation, completion or independent acceptance.

All captured financial histories are synthetic, as documented in the source report.
Copied databases and historical patches are evidence only: never install them as
customer data, seed production fixtures from them, or apply the archived patch.
Historical logs contain local paths and device metadata; review before external
sharing. No new device, hosted CI or financial acceptance is claimed by this import.
