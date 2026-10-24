# SELLO-036 — Accept the complete MVP and prepare release handoff

- **Revision:** the gate tested commit `bf3d690` on `sello-036-acceptance`, on `main` at
  `6f79598`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards.
- **Decisions:** as recorded under Execution progress (owner as independent reviewer and
  go/no-go, option b on the owner's phone, four accepted issues, version `0.1.0`, the
  backup fix here). The product go/no-go is the owner's merge of this pull request.
- **Requirement mapping:** [MVP checklist](../../../release/mvp-checklist.md): candidate
  `sello-0.1.0-1.apk`, 4,458,295 bytes, sha256
  `1e28bb8c14d093b67846036fc96282bc94c3cbae06a7e60c95125a89fefa16a9`, signed (v2) by the
  owner-confirmed certificate `abf6ccf5…d952`, mapping sha256
  `c63995b6b3baca59ba15737923618672a119f2eab2c34e9f534100c101c424e5`; the G3 build is
  byte-identical to the APK walked on the phone. Full flow on the Samsung SM-S731B
  (Android 16) from a fresh install: category, expense, edit, delete and undo, income,
  Resumen, appearance and order, backup, change, restore with restart, force-stop,
  erase all, reuse, every figure as worked out beforehand. Catalog and sandbox (G01, 4
  of 4 figures) launched on the API-30 emulator; neither is in the candidate. 48 other
  MVP tickets Done with valid evidence; no out-of-scope feature or link present.
- **Red / Green:** `aClockFinerThanMillisecondsStillMakesABackupStampedToTheMillisecond`
  failed first with `Unrestorable(InvalidValue(exportedAt))`, as on the phone, and passed
  once `BackupExport` rounds its timestamp to the millisecond.
- **Tests:** device `BackupSnapshotConsistencyTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-036 --retain` passed all eleven G3
  checks, full coverage. Host: 694 tests; device: 729 tests (data 236, design-system
  133, app 339, catalog 21); zero failures, errors or skips. Lint had zero errors and
  the same 33 warnings as before; `release-production` and `release-launch` passed.
- **Quality run:** run 20261023T202433Z-3d80d646; SELLO-036 G3 passed; HEAD bf3d690, inputs sha256 a704cb74f639; report docs/tickets/E08-release/SELLO-036/quality-report.json sha256 e699f95401478220a25c9dd9dff8a121a4a9338a30913874753442c75fa30c9d
- **Device / Artifact:** the owner's Samsung SM-S731B (`R5GYC4FA50N`, Android 16) for
  the walkthrough, synthetic data only; the API-30 AVDs `ci30` (gate) and
  `sello022_api30` (reproduction, catalog and sandbox). Left on the phone: Sello `0.1.0`
  with synthetic data and two backup files in Downloads (14:58, from the failed attempt;
  15:15). Nothing else on the phone was opened or changed.
- **Review:** independent technical review and product go/no-go by the project owner;
  executor self-review covered the expected values, the blocker's cause and the
  candidate's identity. Accepted non-blocking: SELLO-075–078 and the licence notice.
  Distribution, upload or tagging is not part of this ticket.
- **Acceptance migrated:** 59d4c276a38a65858a3266b8c89b5ce2a7e8fb7e7410f0c602509320b8c488f6 → 062d18523877daff456cfccfbb6f62473973f5b8bd9e2d96f2c1084d68468c2a on 2026-10-24; only link paths changed when the ticket moved into its folder.
