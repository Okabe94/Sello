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
- [Exploration evidence](evidence/20261010/): 81 retained files, approximately
  6.7 MiB, including PNG/XML, MP4, host-test output, manifests and synthetic database
  snapshots. Relative evidence filenames in the revised report resolve here.

Reports and retained evidence were copied byte-for-byte; the originals under
`/home/okabe94` remain untouched. Archive-scoped Git attributes disable line-ending
normalization for reports/evidence so committed copies retain those same bytes.
Their absolute paths, draft aliases, proposed
placement and historical statuses are provenance, not current execution instructions.
In particular, the revised report predates completion of SELLO-018. Use E11 for
current registration, dependencies, order and acceptance. The assessment remains
unchanged, including its distinction between executed probes and unmeasured risks.

## Evidence retention decision

On 2026-10-11 the owner approved a conservative trim: remove only the 200 extracted
PNGs in `navigation-frames/` (26 unique images, approximately 8.33 MiB). The original
`08-navigation.mp4`, both navigation contact sheets and `08-detail-entry-frames.png`
remain, preserving the temporal recording and convenient loading-title comparisons.
The complete source archive under `/home/okabe94/bugs_revision_evidence/20261010/`
is untouched. Archived reports describe the original capture, not this curated count.

No other evidence was removed: primary and preliminary screenshots/XML, all three
videos, restoration records, build/test logs, source manifests/status snapshots,
historical patch and both synthetic database snapshots with their WAL/SHM files
remain. This reduces checkout evidence size by approximately 55% without dropping
any reported bug's primary recording, hierarchy or persisted-state evidence.

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
